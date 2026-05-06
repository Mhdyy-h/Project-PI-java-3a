package org.example.service;

import org.example.model.User;

import com.google.gson.*;
import org.example.DatabaseConnection;
import org.example.model.QuestionAdaptive;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.util.ConfigLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Génération de questions en temps réel via l'API Claude (Anthropic).
 *
 * <h3>Fonctionnement</h3>
 * <ol>
 *   <li>Calcule une <b>clé de cache</b> SHA-256 ({@code theme|niveau|type}).</li>
 *   <li>Vérifie si une question non expirée existe dans {@code cache_question_ia}.</li>
 *   <li>Si absente : appelle Claude API et persiste le résultat en cache.</li>
 *   <li>Si l'API est indisponible (clé manquante ou erreur réseau) :
 *       <b>fallback</b> sur la banque de questions MySQL.</li>
 * </ol>
 *
 * <p>Exemple d'utilisation :</p>
 * <pre>
 *   QuestionGenService svc = new QuestionGenService();
 *
 *   // Génère ou récupère depuis le cache
 *   QuestionAdaptive q = svc.generer("Informatique", 5, "QCM");
 *
 *   // Force la génération (ignore le cache)
 *   QuestionAdaptive q2 = svc.genererSansCache("Mathématiques", 7, "VRAI_FAUX");
 * </pre>
 */
public class QuestionGenService {

    private static final Logger log = LoggerFactory.getLogger(QuestionGenService.class);

    private final OkHttpClient httpClient;
    private final Gson gson;

    public QuestionGenService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(ConfigLoader.claudeTimeoutSecondes(), TimeUnit.SECONDS)
                .readTimeout(ConfigLoader.claudeTimeoutSecondes(), TimeUnit.SECONDS)
                .build();
        this.gson = new GsonBuilder().create();
    }

    // ════════════════════════════════════════════════════════════════
    // API publique
    // ════════════════════════════════════════════════════════════════

    /**
     * Génère une question pour le thème, le niveau et le type donnés.
     * Utilise le cache DB et se rabat sur MySQL si Claude est indisponible.
     *
     * @param theme  ex: "Informatique", "Mathématiques"
     * @param niveau difficulté 1-10
     * @param type   "QCM" | "VRAI_FAUX" | "OUVERTE"
     * @return question prête à l'emploi, jamais null (fallback garanti)
     */
    public QuestionAdaptive generer(String theme, int niveau, String type) {
        // 1. Vérifier le cache
        QuestionAdaptive cached = getFromCache(theme, niveau, type);
        if (cached != null) {
            log.debug("Question servie depuis le cache ({}|{}|{})", theme, niveau, type);
            return cached;
        }

        // 2. Claude API si disponible
        if (ConfigLoader.claudeDisponible()) {
            QuestionAdaptive generated = appellerClaude(theme, niveau, type);
            if (generated != null) {
                mettreEnCache(generated, theme, niveau, type);
                sauvegarderEnBanque(generated);
                return generated;
            }
        }

        // 3. Fallback MySQL
        log.info("Fallback banque MySQL pour {}|{}|{}", theme, niveau, type);
        return fallbackMySQL(theme, niveau, type);
    }

    /**
     * Force un appel à Claude (ignore le cache).
     * Utile pour rafraîchir une question ou tester l'intégration API.
     */
    public QuestionAdaptive genererSansCache(String theme, int niveau, String type) {
        if (!ConfigLoader.claudeDisponible()) {
            log.warn("Claude API non configurée — fallback MySQL");
            return fallbackMySQL(theme, niveau, type);
        }
        QuestionAdaptive q = appellerClaude(theme, niveau, type);
        if (q == null) return fallbackMySQL(theme, niveau, type);
        sauvegarderEnBanque(q);
        return q;
    }

    /**
     * Génère plusieurs questions distinctes pour une session.
     *
     * @param theme  thème commun
     * @param niveau difficulté de départ (sera ajustée par AdaptiveService)
     * @param type   type de questions
     * @param nombre nombre de questions à générer
     * @return liste de questions (taille <= {@code nombre} si la banque est trop petite)
     */
    public List<QuestionAdaptive> genererLot(String theme, int niveau, String type, int nombre) {
        List<QuestionAdaptive> liste = new ArrayList<>();
        Set<String> enoncesSeen = new HashSet<>();

        for (int i = 0; i < nombre; i++) {
            QuestionAdaptive q = generer(theme, niveau, type);
            if (q != null && enoncesSeen.add(q.getEnonce())) {
                liste.add(q);
            }
        }
        return liste;
    }

    // ════════════════════════════════════════════════════════════════
    // Appel Claude API
    // ════════════════════════════════════════════════════════════════

    private QuestionAdaptive appellerClaude(String theme, int niveau, String type) {
        String prompt = construirePrompt(theme, niveau, type);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", ConfigLoader.claudeModel());
        body.put("max_tokens", 512);
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        RequestBody requestBody = RequestBody.create(
                gson.toJson(body),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(ConfigLoader.claudeApiUrl())
                .addHeader("x-api-key", ConfigLoader.claudeApiKey())
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("content-type", "application/json")
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("Claude API répondu {} : {}", response.code(), response.message());
                return null;
            }
            String rawBody = response.body() != null ? response.body().string() : "";
            return parserReponse(rawBody, theme, niveau, type);

        } catch (IOException e) {
            log.error("Erreur réseau Claude API : {}", e.getMessage());
            return null;
        }
    }

    /**
     * Construit le prompt envoyé à Claude.
     * Le modèle doit retourner du JSON pur (pas de markdown).
     */
    private String construirePrompt(String theme, int niveau, String type) {
        String formatOptions = switch (type) {
            case "QCM"       -> """
                "options": ["choix A", "choix B", "choix C", "choix D"],
                """;
            case "VRAI_FAUX" -> """
                "options": ["Vrai", "Faux"],
                """;
            default           -> "";
        };

        return String.format("""
            Tu es un générateur de questions de quiz pédagogiques.
            Génère UNE question sur le thème "%s" avec une difficulté de %d/10.
            Type de question : %s.

            Réponds UNIQUEMENT avec du JSON valide, sans markdown, sans explication :
            {
              "enonce": "...",
              %s"reponseCorrecte": "...",
              "explication": "Explication pédagogique concise en 1-2 phrases."
            }

            Contraintes :
            - L'énoncé doit être précis et non ambigu.
            - La réponse correcte doit être exactement l'un des choix (pour QCM/VRAI_FAUX).
            - L'explication doit permettre à l'apprenant de comprendre pourquoi c'est correct.
            - Langue : français.
            """, theme, niveau, type, formatOptions);
    }

    /**
     * Parse la réponse JSON de l'API Claude et construit une {@link QuestionAdaptive}.
     */
    private QuestionAdaptive parserReponse(String rawBody, String theme, int niveau, String type) {
        try {
            JsonObject apiResponse = JsonParser.parseString(rawBody).getAsJsonObject();
            JsonArray content = apiResponse.getAsJsonArray("content");
            if (content == null || content.isEmpty()) return null;

            String texte = content.get(0).getAsJsonObject()
                    .get("text").getAsString().trim();

            // Nettoie les blocs markdown si Claude a quand même renvoyé ```json
            if (texte.startsWith("```")) {
                texte = texte.replaceAll("```(json)?", "").trim();
            }

            JsonObject qJson = JsonParser.parseString(texte).getAsJsonObject();

            QuestionAdaptive q = new QuestionAdaptive();
            q.setEnonce(qJson.get("enonce").getAsString());
            q.setReponseCorrecte(qJson.get("reponseCorrecte").getAsString());
            q.setExplication(qJson.has("explication") ? qJson.get("explication").getAsString() : null);
            q.setTheme(theme);
            q.setNiveauDifficulte(niveau);
            q.setType(type);
            q.setSource("IA_GENEREE");

            if (qJson.has("options")) {
                List<String> opts = new ArrayList<>();
                qJson.getAsJsonArray("options")
                        .forEach(e -> opts.add(e.getAsString()));
                q.setOptions(opts);
            }

            return q;

        } catch (JsonParseException | IllegalStateException e) {
            log.error("Erreur parsing réponse Claude : {}", e.getMessage());
            return null;
        }
    }

    // ════════════════════════════════════════════════════════════════
    // Cache DB
    // ════════════════════════════════════════════════════════════════

    private QuestionAdaptive getFromCache(String theme, int niveau, String type) {
        String cle = cacheKey(theme, niveau, type);
        String sql = """
            SELECT * FROM cache_question_ia
            WHERE cle_cache = ? AND date_expiration > NOW()
            ORDER BY RAND()
            LIMIT 1
            """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, cle);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapperCache(rs);
            }
        } catch (SQLException e) {
            log.error("Erreur lecture cache : {}", e.getMessage());
        }
        return null;
    }

    private void mettreEnCache(QuestionAdaptive q, String theme, int niveau, String type) {
        String cle = cacheKey(theme, niveau, type);
        int ttlHeures = ConfigLoader.claudeCacheTtlHeures();

        String optionsJson = q.getOptions() != null
                ? gson.toJson(q.getOptions()) : null;

        // Utilise INSERT ... ON DUPLICATE KEY UPDATE pour éviter les doublons
        String sql = """
            INSERT INTO cache_question_ia
              (cle_cache, enonce, reponse_correcte, options, explication,
               theme, niveau_difficulte, date_expiration)
            VALUES (?, ?, ?, ?, ?, ?, ?, DATE_ADD(NOW(), INTERVAL ? HOUR))
            ON DUPLICATE KEY UPDATE
              enonce = VALUES(enonce),
              reponse_correcte = VALUES(reponse_correcte),
              options = VALUES(options),
              explication = VALUES(explication),
              date_expiration = VALUES(date_expiration)
            """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, cle);
            ps.setString(2, q.getEnonce());
            ps.setString(3, q.getReponseCorrecte());
            ps.setString(4, optionsJson);
            ps.setString(5, q.getExplication());
            ps.setString(6, theme);
            ps.setInt(7, niveau);
            ps.setInt(8, ttlHeures);
            ps.executeUpdate();

        } catch (SQLException e) {
            log.error("Erreur mise en cache : {}", e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // Fallback MySQL
    // ════════════════════════════════════════════════════════════════

    private QuestionAdaptive fallbackMySQL(String theme, int niveau, String type) {
        // Cherche d'abord exact, puis élargit la marge de difficulté
        for (int marge = 0; marge <= 4; marge++) {
            int nMin = Math.max(1, niveau - marge);
            int nMax = Math.min(10, niveau + marge);

            String whereTheme = (theme != null && !theme.isBlank()) ? "AND theme = ?" : "";
            String whereType  = (type  != null && !type.isBlank())  ? "AND type = ?" : "";
            String sql = String.format("""
                SELECT * FROM question
                WHERE niveau_difficulte BETWEEN ? AND ?
                %s %s
                ORDER BY RAND() LIMIT 1
                """, whereTheme, whereType);

            try (Connection cn = DatabaseConnection.getConnection();
                 PreparedStatement ps = cn.prepareStatement(sql)) {

                int idx = 1;
                ps.setInt(idx++, nMin);
                ps.setInt(idx++, nMax);
                if (theme != null && !theme.isBlank()) ps.setString(idx++, theme);
                if (type  != null && !type.isBlank())  ps.setString(idx, type);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapperQuestion(rs);
                }
            } catch (SQLException e) {
                log.error("Erreur fallback MySQL : {}", e.getMessage());
            }
        }

        log.error("Aucune question disponible pour {}|{}|{} — banque vide ?", theme, niveau, type);
        return questionUrgence(theme, niveau, type);
    }

    /** QuestionAdaptive d'urgence hardcodée si la banque MySQL est vide. */
    private QuestionAdaptive questionUrgence(String theme, int niveau, String type) {
        QuestionAdaptive q = new QuestionAdaptive("Combien font 2 + 2 ?", "QCM", "Mathématiques", 1);
        q.setReponseCorrecte("4");
        q.setOptions(List.of("2", "3", "4", "5"));
        q.setExplication("2 + 2 = 4 par définition de l'addition.");
        q.setSource("BANQUE");
        return q;
    }

    // ════════════════════════════════════════════════════════════════
    // Persistance en banque
    // ════════════════════════════════════════════════════════════════

    /** Sauvegarde une question générée dans la table {@code question} pour réutilisation future. */
    private void sauvegarderEnBanque(QuestionAdaptive q) {
        String sql = """
            INSERT INTO question
              (enonce, type, theme, niveau_difficulte, source,
               reponse_correcte, options, explication)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            String optionsJson = q.getOptions() != null ? gson.toJson(q.getOptions()) : null;
            ps.setString(1, q.getEnonce());
            ps.setString(2, q.getType());
            ps.setString(3, q.getTheme());
            ps.setInt(4, q.getNiveauDifficulte());
            ps.setString(5, "IA_GENEREE");
            ps.setString(6, q.getReponseCorrecte());
            ps.setString(7, optionsJson);
            ps.setString(8, q.getExplication());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) q.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            log.warn("Impossible de sauvegarder la question en banque : {}", e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // Utilitaires
    // ════════════════════════════════════════════════════════════════

    /** Génère une clé de cache SHA-256 reproductible. */
    private String cacheKey(String theme, int niveau, String type) {
        String input = theme + "|" + niveau + "|" + type;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return input.hashCode() + "";
        }
    }

    private QuestionAdaptive mapperCache(ResultSet rs) throws SQLException {
        QuestionAdaptive q = new QuestionAdaptive();
        q.setEnonce(rs.getString("enonce"));
        q.setReponseCorrecte(rs.getString("reponse_correcte"));
        q.setExplication(rs.getString("explication"));
        q.setTheme(rs.getString("theme"));
        q.setNiveauDifficulte(rs.getInt("niveau_difficulte"));
        q.setSource("IA_GENEREE");
        String optJson = rs.getString("options");
        if (optJson != null) {
            List<String> opts = new ArrayList<>();
            gson.fromJson(optJson, JsonArray.class)
                .forEach(e -> opts.add(e.getAsString()));
            q.setOptions(opts);
            q.setType(opts.size() == 2 ? "VRAI_FAUX" : "QCM");
        } else {
            q.setType("OUVERTE");
        }
        return q;
    }

    private QuestionAdaptive mapperQuestion(ResultSet rs) throws SQLException {
        QuestionAdaptive q = new QuestionAdaptive();
        q.setId(rs.getInt("id"));
        q.setEnonce(rs.getString("enonce"));
        q.setType(rs.getString("type"));
        q.setTheme(rs.getString("theme"));
        q.setNiveauDifficulte(rs.getInt("niveau_difficulte"));
        q.setSource(rs.getString("source"));
        q.setReponseCorrecte(rs.getString("reponse_correcte"));
        q.setExplication(rs.getString("explication"));
        String optJson = rs.getString("options");
        if (optJson != null) {
            List<String> opts = new ArrayList<>();
            gson.fromJson(optJson, JsonArray.class)
                .forEach(e -> opts.add(e.getAsString()));
            q.setOptions(opts);
        }
        return q;
    }
}

