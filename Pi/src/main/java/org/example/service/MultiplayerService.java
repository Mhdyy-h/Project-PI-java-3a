package org.example.service;

import com.google.gson.*;
import org.example.DatabaseConnection;
import org.example.model.ParticipantMultijoueur;
import org.example.model.QuestionAdaptive;
import org.example.model.SessionMultijoueur;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.util.ConfigLoader;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Mode compétitif multijoueur local via WebSocket (réseau WiFi).
 *
 * <h3>Architecture</h3>
 * <pre>
 *   [Hôte JavaFX] → MultiplayerService.creerServeur()
 *        ↓ WebSocketServer (port 8887)
 *   [Joueur A]  [Joueur B]  [Joueur C]
 *   → MultiplayerService.rejoindre("ws://192.168.1.10:8887")
 * </pre>
 *
 * <h3>Protocole de messages (JSON)</h3>
 * <table border="1">
 *   <tr><th>Type</th><th>Direction</th><th>Description</th></tr>
 *   <tr><td>JOIN</td><td>Client→Serveur</td><td>Rejoindre le salon</td></tr>
 *   <tr><td>PLAYERS_UPDATE</td><td>Serveur→Tous</td><td>Liste des joueurs connectés</td></tr>
 *   <tr><td>START</td><td>Serveur→Tous</td><td>Début de la partie</td></tr>
 *   <tr><td>QUESTION</td><td>Serveur→Tous</td><td>Envoi d'une question</td></tr>
 *   <tr><td>ANSWER</td><td>Client→Serveur</td><td>Réponse à la question</td></tr>
 *   <tr><td>SCORE_UPDATE</td><td>Serveur→Tous</td><td>Classement en temps réel</td></tr>
 *   <tr><td>END</td><td>Serveur→Tous</td><td>Fin de partie + classement final</td></tr>
 * </table>
 *
 * <p>Exemple d'utilisation (côté hôte) :</p>
 * <pre>
 *   MultiplayerService svc = new MultiplayerService();
 *   svc.setListener(new MultiplayerListener() { ... });
 *   SessionMultijoueur salon = svc.creerSalon(userId, "Informatique", 10, 30);
 *   svc.demarrerServeur();
 *   // Quand tous les joueurs ont rejoint :
 *   svc.demarrerPartie();
 * </pre>
 *
 * <p>Exemple d'utilisation (côté joueur) :</p>
 * <pre>
 *   MultiplayerService svc = new MultiplayerService();
 *   svc.setListener(new MultiplayerListener() { ... });
 *   svc.rejoindre("ws://192.168.1.10:8887", "Alice");
 * </pre>
 */
public class MultiplayerService {

    private static final Logger log = LoggerFactory.getLogger(MultiplayerService.class);

    private final Gson gson = new GsonBuilder().create();

    // ── État serveur ─────────────────────────────────────────────
    private QuizServer serveur;
    private SessionMultijoueur salonCourant;
    private List<QuestionAdaptive> questionsPartie;
    private int indexQuestion = 0;

    /** Scores en mémoire pendant la partie : utilisateurId → score */
    private final Map<Integer, Integer> scores = new ConcurrentHashMap<>();
    /** Connexions WebSocket → utilisateurId (côté serveur) */
    private final Map<WebSocket, Integer> connexions = new ConcurrentHashMap<>();
    /** Noms des joueurs : utilisateurId → nom */
    private final Map<Integer, String> nomsJoueurs = new ConcurrentHashMap<>();

    // ── État client ──────────────────────────────────────────────
    private QuizClient client;
    private MultiplayerListener listener;

    // ════════════════════════════════════════════════════════════════
    // Interface de callbacks (JavaFX controller l'implémente)
    // ════════════════════════════════════════════════════════════════

    public interface MultiplayerListener {
        /** Un joueur a rejoint le salon. */
        void onJoueurRejoint(String nom, int total);
        /** Une question arrive (côté client). */
        void onQuestionRecue(QuestionAdaptive question, int tempsRestantSec);
        /** Mise à jour du classement en temps réel. */
        void onScoresMAJ(List<ScoreEntry> classement);
        /** La partie est terminée. */
        void onPartieTerminee(List<ScoreEntry> classementFinal);
        /** Erreur de connexion. */
        void onErreur(String message);
    }

    public void setListener(MultiplayerListener listener) {
        this.listener = listener;
    }

    // ════════════════════════════════════════════════════════════════
    // Côté HÔTE
    // ════════════════════════════════════════════════════════════════

    /**
     * Crée un nouveau salon et le persiste en DB.
     *
     * @return salon créé avec codeSalon généré
     */
    public SessionMultijoueur creerSalon(int createurId, String theme,
                                          int nombreQuestions, int tempsParQuestionSec) {
        SessionMultijoueur salon = new SessionMultijoueur(
                createurId, theme, nombreQuestions, tempsParQuestionSec);
        salonCourant = salon;
        insererSalon(salon);
        log.info("Salon créé : {} (code: {})", salon.getId(), salon.getCodeSalon());
        return salon;
    }

    /**
     * Démarre le serveur WebSocket sur le port configuré.
     * L'hôte écoute les connexions des joueurs.
     */
    public void demarrerServeur() {
        int port = ConfigLoader.websocketPort();
        serveur = new QuizServer(new InetSocketAddress(port));
        serveur.start();
        log.info("Serveur WebSocket démarré sur le port {}", port);
    }

    /**
     * Démarre la partie : charge les questions et envoie la première.
     * À appeler depuis le bouton "Lancer" dans le contrôleur.
     */
    public void demarrerPartie(List<QuestionAdaptive> questions) {
        if (salonCourant == null) throw new IllegalStateException("Aucun salon créé");
        this.questionsPartie = new ArrayList<>(questions);
        this.indexQuestion = 0;
        salonCourant.demarrer();
        mettreAJourStatutSalon("EN_COURS");
        envoyerATous(message("START", Map.of("total", questionsPartie.size())));
        envoyerProchaineQuestion();
    }

    /** Arrête le serveur (appeler à la fermeture de la fenêtre hôte). */
    public void arreterServeur() {
        if (serveur != null) {
            try {
                serveur.stop();
                log.info("Serveur WebSocket arrêté");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    // Côté JOUEUR
    // ════════════════════════════════════════════════════════════════

    /**
     * Connecte ce client au serveur WebSocket de l'hôte.
     *
     * @param serverUrl URL du serveur, ex: {@code ws://192.168.1.10:8887}
     * @param nomJoueur nom affiché dans le classement
     * @param userId    ID de l'utilisateur connecté
     */
    public void rejoindre(String serverUrl, String nomJoueur, int userId) {
        try {
            client = new QuizClient(new URI(serverUrl));
            client.connectBlocking();
            client.send(message("JOIN", Map.of("nom", nomJoueur, "userId", userId)));
            log.info("Connecté au salon : {}", serverUrl);
        } catch (URISyntaxException | InterruptedException e) {
            log.error("Erreur connexion serveur : {}", e.getMessage());
            if (listener != null) listener.onErreur("Impossible de rejoindre : " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /** Envoie une réponse au serveur. */
    public void envoyerReponse(int questionId, String reponse, int tempsMs, int userId) {
        if (client == null || !client.isOpen()) return;
        client.send(message("ANSWER", Map.of(
                "questionId", questionId,
                "reponse", reponse,
                "tempsMs", tempsMs,
                "userId", userId
        )));
    }

    /** Déconnecte le client. */
    public void deconnecter() {
        if (client != null) client.close();
    }

    // ════════════════════════════════════════════════════════════════
    // Persistance
    // ════════════════════════════════════════════════════════════════

    /**
     * Récupère un salon par son code court.
     *
     * @return salon ou {@code null} si introuvable
     */
    public SessionMultijoueur getSalonParCode(String code) {
        String sql = "SELECT * FROM session_multijoueur WHERE code_salon = ?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, code.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapperSalon(rs);
            }
        } catch (SQLException e) {
            log.error("Erreur getSalonParCode : {}", e.getMessage());
        }
        return null;
    }

    /**
     * Sauvegarde le classement final dans {@code participant_multijoueur}.
     */
    public void sauvegarderClassement(List<ScoreEntry> classement) {
        if (salonCourant == null) return;
        String sql = """
            INSERT INTO participant_multijoueur
              (session_multi_id, utilisateur_id, score, rang_final, statut)
            VALUES (?, ?, ?, ?, 'TERMINE')
            ON DUPLICATE KEY UPDATE score = VALUES(score), rang_final = VALUES(rang_final), statut = 'TERMINE'
            """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < classement.size(); i++) {
                ps.setInt(1, salonCourant.getId());
                ps.setInt(2, classement.get(i).getUserId());
                ps.setInt(3, classement.get(i).getScore());
                ps.setInt(4, i + 1);
                ps.addBatch();
            }
            ps.executeBatch();
            mettreAJourStatutSalon("TERMINEE");
        } catch (SQLException e) {
            log.error("Erreur sauvegarderClassement : {}", e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // Logique serveur interne
    // ════════════════════════════════════════════════════════════════

    private void traiterMessage(WebSocket conn, String messageJson) {
        try {
            JsonObject msg = JsonParser.parseString(messageJson).getAsJsonObject();
            String type    = msg.get("type").getAsString();
            JsonObject data = msg.has("data") ? msg.getAsJsonObject("data") : new JsonObject();

            switch (type) {
                case "JOIN" -> {
                    int userId = data.get("userId").getAsInt();
                    String nom = data.get("nom").getAsString();
                    connexions.put(conn, userId);
                    nomsJoueurs.put(userId, nom);
                    scores.put(userId, 0);
                    log.info("Joueur rejoint : {} ({})", nom, userId);
                    // Notifier tous les joueurs
                    envoyerATous(message("PLAYERS_UPDATE", Map.of(
                            "joueurs", nomsJoueurs.values(),
                            "total", connexions.size())));
                    if (listener != null) {
                        javafx.application.Platform.runLater(() ->
                                listener.onJoueurRejoint(nom, connexions.size()));
                    }
                }
                case "ANSWER" -> {
                    int userId     = data.get("userId").getAsInt();
                    int questionId = data.get("questionId").getAsInt();
                    String reponse = data.get("reponse").getAsString();

                    // Valider la réponse
                    if (questionsPartie != null && indexQuestion > 0) {
                        QuestionAdaptive q = questionsPartie.get(indexQuestion - 1);
                        if (q.getId() == questionId && q.estReponseCorrecte(reponse)) {
                            int tempsMs = data.has("tempsMs") ? data.get("tempsMs").getAsInt() : 30000;
                            // Bonus de rapidité : max 100 pts, réduit avec le temps
                            int pts = Math.max(10, 100 - (tempsMs / 1000) * 3);
                            scores.merge(userId, pts, Integer::sum);
                        }
                    }
                    envoyerATous(message("SCORE_UPDATE",
                            Map.of("classement", construireClassement())));
                }
            }
        } catch (JsonParseException e) {
            log.warn("Message malformé reçu : {}", messageJson);
        }
    }

    private void envoyerProchaineQuestion() {
        if (questionsPartie == null || indexQuestion >= questionsPartie.size()) {
            terminerPartie();
            return;
        }
        QuestionAdaptive q = questionsPartie.get(indexQuestion++);
        String msgJson = gson.toJson(Map.of(
                "type", "QUESTION",
                "data", Map.of(
                        "id",               q.getId(),
                        "enonce",           q.getEnonce(),
                        "type",             q.getType(),
                        "options",          q.getOptions() != null ? q.getOptions() : List.of(),
                        "numero",           indexQuestion,
                        "total",            questionsPartie.size(),
                        "tempsSecondes",    salonCourant.getTempsParQuestionSec()
                )
        ));
        envoyerATous(msgJson);

        // Timer automatique pour passer à la prochaine question
        int delaiMs = salonCourant.getTempsParQuestionSec() * 1000 + 500;
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override public void run() { envoyerProchaineQuestion(); }
        }, delaiMs);
    }

    private void terminerPartie() {
        List<ScoreEntry> classement = construireClassement();
        envoyerATous(message("END", Map.of("classement", classement)));
        sauvegarderClassement(classement);
        if (listener != null) {
            javafx.application.Platform.runLater(() ->
                    listener.onPartieTerminee(classement));
        }
    }

    private void envoyerATous(String messageJson) {
        if (serveur != null) serveur.broadcast(messageJson);
    }

    private List<ScoreEntry> construireClassement() {
        return scores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .map(e -> new ScoreEntry(e.getKey(),
                        nomsJoueurs.getOrDefault(e.getKey(), "?"), e.getValue()))
                .toList();
    }

    private String message(String type, Map<String, Object> data) {
        return gson.toJson(Map.of("type", type, "data", data));
    }

    // ════════════════════════════════════════════════════════════════
    // Implémentations WebSocket (classes internes)
    // ════════════════════════════════════════════════════════════════

    private class QuizServer extends WebSocketServer {

        public QuizServer(InetSocketAddress address) { super(address); }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            log.debug("Nouvelle connexion WebSocket : {}", conn.getRemoteSocketAddress());
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            Integer userId = connexions.remove(conn);
            if (userId != null) {
                String nom = nomsJoueurs.remove(userId);
                scores.remove(userId);
                log.info("Joueur déconnecté : {} ({})", nom, userId);
                envoyerATous(message("PLAYERS_UPDATE", Map.of(
                        "joueurs", nomsJoueurs.values(),
                        "total", connexions.size())));
            }
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            traiterMessage(conn, message);
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            log.error("Erreur WebSocket serveur : {}", ex.getMessage());
            if (listener != null) {
                javafx.application.Platform.runLater(() ->
                        listener.onErreur("Erreur serveur : " + ex.getMessage()));
            }
        }

        @Override
        public void onStart() {
            log.info("WebSocketServer démarré et en écoute");
        }
    }

    private class QuizClient extends WebSocketClient {

        public QuizClient(URI serverUri) { super(serverUri); }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            log.debug("Connecté au serveur WebSocket");
        }

        @Override
        public void onMessage(String messageJson) {
            if (listener == null) return;
            try {
                JsonObject msg  = JsonParser.parseString(messageJson).getAsJsonObject();
                String type     = msg.get("type").getAsString();
                JsonObject data = msg.has("data") ? msg.getAsJsonObject("data") : new JsonObject();

                switch (type) {
                    case "PLAYERS_UPDATE" -> {
                        int total = data.get("total").getAsInt();
                        String dernierNom = data.getAsJsonArray("joueurs")
                                .get(total - 1).getAsString();
                        javafx.application.Platform.runLater(() ->
                                listener.onJoueurRejoint(dernierNom, total));
                    }
                    case "QUESTION" -> {
                        QuestionAdaptive q = new QuestionAdaptive();
                        q.setId(data.get("id").getAsInt());
                        q.setEnonce(data.get("enonce").getAsString());
                        q.setType(data.get("type").getAsString());
                        List<String> opts = new ArrayList<>();
                        data.getAsJsonArray("options")
                                .forEach(e -> opts.add(e.getAsString()));
                        q.setOptions(opts);

                        int temps = data.get("tempsSecondes").getAsInt();
                        javafx.application.Platform.runLater(() ->
                                listener.onQuestionRecue(q, temps));
                    }
                    case "SCORE_UPDATE" -> {
                        List<ScoreEntry> classement = parserClassement(data);
                        javafx.application.Platform.runLater(() ->
                                listener.onScoresMAJ(classement));
                    }
                    case "END" -> {
                        List<ScoreEntry> final_ = parserClassement(data);
                        javafx.application.Platform.runLater(() ->
                                listener.onPartieTerminee(final_));
                    }
                }
            } catch (JsonParseException e) {
                log.warn("Message serveur malformé : {}", messageJson);
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            log.info("Déconnecté du serveur : {}", reason);
        }

        @Override
        public void onError(Exception ex) {
            log.error("Erreur WebSocket client : {}", ex.getMessage());
            if (listener != null) {
                javafx.application.Platform.runLater(() ->
                        listener.onErreur("Erreur réseau : " + ex.getMessage()));
            }
        }

        private List<ScoreEntry> parserClassement(JsonObject data) {
            List<ScoreEntry> list = new ArrayList<>();
            if (data.has("classement")) {
                data.getAsJsonArray("classement").forEach(e -> {
                    JsonObject o = e.getAsJsonObject();
                    list.add(new ScoreEntry(
                            o.get("userId").getAsInt(),
                            o.get("nom").getAsString(),
                            o.get("score").getAsInt()));
                });
            }
            return list;
        }
    }

    // ════════════════════════════════════════════════════════════════
    // Persistance interne
    // ════════════════════════════════════════════════════════════════

    private void insererSalon(SessionMultijoueur s) {
        String sql = """
            INSERT INTO session_multijoueur
              (code_salon, theme, nombre_questions, temps_par_question_sec, createur_id)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getCodeSalon());
            ps.setString(2, s.getTheme());
            ps.setInt(3, s.getNombreQuestions());
            ps.setInt(4, s.getTempsParQuestionSec());
            ps.setInt(5, s.getCreateurId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) s.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            log.error("Erreur insererSalon : {}", e.getMessage());
        }
    }

    private void mettreAJourStatutSalon(String statut) {
        if (salonCourant == null) return;
        String sql = "UPDATE session_multijoueur SET statut = ? WHERE id = ?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, salonCourant.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Erreur mettreAJourStatutSalon : {}", e.getMessage());
        }
    }

    private SessionMultijoueur mapperSalon(ResultSet rs) throws SQLException {
        return new SessionMultijoueur(
                rs.getInt("id"),
                rs.getString("code_salon"),
                rs.getString("theme"),
                rs.getInt("nombre_questions"),
                rs.getInt("temps_par_question_sec"),
                rs.getString("statut"),
                rs.getInt("createur_id"),
                rs.getTimestamp("date_creation").toLocalDateTime(),
                rs.getTimestamp("date_debut") != null ? rs.getTimestamp("date_debut").toLocalDateTime() : null,
                rs.getTimestamp("date_fin")   != null ? rs.getTimestamp("date_fin").toLocalDateTime()   : null
        );
    }

    // ════════════════════════════════════════════════════════════════
    // Classe de données publique
    // ════════════════════════════════════════════════════════════════

    /**
     * Entrée du classement : un joueur + son score.
     */
    public static class ScoreEntry {
        private final int    userId;
        private final String nom;
        private final int    score;

        public ScoreEntry(int userId, String nom, int score) {
            this.userId = userId;
            this.nom    = nom;
            this.score  = score;
        }

        public int    getUserId() { return userId; }
        public String getNom()    { return nom; }
        public int    getScore()  { return score; }

        @Override
        public String toString() { return nom + " : " + score + " pts"; }
    }
}
