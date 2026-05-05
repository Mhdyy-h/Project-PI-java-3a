package org.example.service;

import com.google.gson.*;
import org.example.model.ProfilCognitif;
import org.example.model.QuestionAdaptive;
import org.example.model.ReponseUtilisateur;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.util.ConfigLoader;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Assistant IA pédagogique propulsé par Claude API.
 *
 * <h3>Fonctionnalités</h3>
 * <ul>
 *   <li><b>Explication immédiate</b> — après chaque question, explique la bonne réponse.</li>
 *   <li><b>Coaching de fin de session</b> — analyse les erreurs récurrentes et
 *       génère 3 recommandations actionnables personnalisées.</li>
 *   <li><b>Mode conversationnel</b> — l'utilisateur peut poser des questions de suivi
 *       dans un historique de conversation maintenu en mémoire.</li>
 * </ul>
 *
 * <p>Si Claude API n'est pas configurée, des messages de fallback statiques sont retournés.</p>
 *
 * <p>Exemple d'utilisation :</p>
 * <pre>
 *   AIAssistantService assistant = new AIAssistantService();
 *
 *   // Explication après une question
 *   String explication = assistant.expliquer(question, "Paris", true);
 *
 *   // Coaching de fin de session
 *   CoachingResult coaching = assistant.coacher(profil, reponses, questions);
 *
 *   // Mode chat
 *   assistant.reinitialiserConversation();
 *   String reponse = assistant.chat("Pourquoi Paris est-elle la capitale ?");
 * </pre>
 */
public class AIAssistantService {

    private static final Logger log = LoggerFactory.getLogger(AIAssistantService.class);

    private final OkHttpClient httpClient;
    private final Gson gson;

    /** Historique de la conversation (maintenu pour le mode chat). */
    private final List<Map<String, String>> historiqueConversation = new ArrayList<>();

    /** Contexte système injecté au début de chaque conversation. */
    private static final String SYSTEM_COACH = """
            Tu es un coach pédagogique bienveillant et expert. Tu aides les apprenants à comprendre
            leurs erreurs et à progresser. Tu réponds en français, de manière concise (2-4 phrases max),
            avec un ton encourageant. Tu n'utilises pas de markdown dans tes réponses.
            """;

    public AIAssistantService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(ConfigLoader.claudeTimeoutSecondes(), TimeUnit.SECONDS)
                .readTimeout(ConfigLoader.claudeTimeoutSecondes(), TimeUnit.SECONDS)
                .build();
        this.gson = new GsonBuilder().create();
    }

    // ════════════════════════════════════════════════════════════════
    // Explication immédiate après une question
    // ════════════════════════════════════════════════════════════════

    /**
     * Génère une explication pédagogique immédiate après une réponse.
     *
     * @param question      la question qui vient d'être posée
     * @param reponseDonnee la réponse de l'utilisateur
     * @param estCorrecte   si la réponse était correcte
     * @return texte d'explication (jamais null)
     */
    public String expliquer(QuestionAdaptive question, String reponseDonnee, boolean estCorrecte) {
        // Si la question a déjà une explication stockée, la retourner directement
        if (question.getExplication() != null && !question.getExplication().isBlank()) {
            return question.getExplication();
        }

        if (!ConfigLoader.claudeDisponible()) {
            return fallbackExplication(question, estCorrecte);
        }

        String prompt = String.format("""
                QuestionAdaptive : %s
                Bonne réponse : %s
                Réponse de l'apprenant : %s (%s)

                Explique en 2 phrases pourquoi la bonne réponse est correcte.
                Si l'apprenant a eu faux, commence par une phrase d'encouragement.
                """,
                question.getEnonce(),
                question.getReponseCorrecte(),
                reponseDonnee,
                estCorrecte ? "correct" : "incorrect"
        );

        String reponse = appellerClaude(prompt, SYSTEM_COACH);
        return reponse != null ? reponse : fallbackExplication(question, estCorrecte);
    }

    // ════════════════════════════════════════════════════════════════
    // Coaching de fin de session
    // ════════════════════════════════════════════════════════════════

    /**
     * Analyse les performances et génère un rapport de coaching personnalisé.
     *
     * @param profil    profil cognitif de l'utilisateur
     * @param reponses  toutes les réponses de la session
     * @param questions map questionId → QuestionAdaptive pour retrouver les thèmes/énoncés
     * @return résultat structuré avec résumé + recommandations + encouragement
     */
    public CoachingResult coacher(ProfilCognitif profil,
                                   List<ReponseUtilisateur> reponses,
                                   Map<Integer, QuestionAdaptive> questions) {

        long correctes  = reponses.stream().filter(ReponseUtilisateur::isEstCorrecte).count();
        double scoreNet = reponses.isEmpty() ? 0.0 : (double) correctes / reponses.size() * 100;

        // Identifier les erreurs récurrentes par thème
        Map<String, int[]> statsTheme = new LinkedHashMap<>(); // theme → [total, erreurs]
        for (ReponseUtilisateur r : reponses) {
            QuestionAdaptive q = questions.get(r.getQuestionId());
            if (q == null) continue;
            statsTheme.computeIfAbsent(q.getTheme(), k -> new int[]{0, 0});
            statsTheme.get(q.getTheme())[0]++;
            if (!r.isEstCorrecte()) statsTheme.get(q.getTheme())[1]++;
        }

        // Thème le plus problématique
        String themeFaible = statsTheme.entrySet().stream()
                .filter(e -> e.getValue()[0] > 0)
                .max(Comparator.comparingDouble(e -> (double) e.getValue()[1] / e.getValue()[0]))
                .map(Map.Entry::getKey)
                .orElse(null);

        if (!ConfigLoader.claudeDisponible()) {
            return fallbackCoaching(scoreNet, themeFaible, profil);
        }

        String prompt = construirePromptCoaching(profil, reponses, scoreNet, statsTheme, questions);
        String texteCoaching = appellerClaude(prompt, SYSTEM_COACH);

        if (texteCoaching == null) {
            return fallbackCoaching(scoreNet, themeFaible, profil);
        }

        return new CoachingResult(scoreNet, themeFaible, texteCoaching,
                extraireRecommandations(texteCoaching));
    }

    // ════════════════════════════════════════════════════════════════
    // Mode conversationnel (chat)
    // ════════════════════════════════════════════════════════════════

    /**
     * Envoie un message dans la conversation et obtient une réponse de Claude.
     * L'historique est maintenu en mémoire pour le contexte.
     *
     * @param messageUtilisateur le message de l'utilisateur
     * @return réponse de l'assistant (jamais null)
     */
    public String chat(String messageUtilisateur) {
        if (!ConfigLoader.claudeDisponible()) {
            return repondreLocalement(messageUtilisateur.trim().toLowerCase());
        }

        historiqueConversation.add(Map.of("role", "user", "content", messageUtilisateur));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", ConfigLoader.claudeModel());
        body.put("max_tokens", 512);
        body.put("system", SYSTEM_COACH);
        body.put("messages", historiqueConversation);

        String reponse = envoyerRequete(body);
        if (reponse != null) {
            historiqueConversation.add(Map.of("role", "assistant", "content", reponse));
        }

        if (historiqueConversation.size() > 20) {
            historiqueConversation.subList(0, 2).clear();
        }

        return reponse != null ? reponse : "Je n'ai pas pu générer une réponse. Réessayez.";
    }

    /**
     * Répond localement aux questions fréquentes sans API externe.
     * Utilisé quand claude.api.key est absent de config.properties.
     */
    private String repondreLocalement(String question) {
        if (contient(question, "theme", "thème", "sujet", "categorie", "catégorie", "domaine")) {
            return "Les thèmes disponibles sur BioSync sont : Mémoire, Attention, Logique, " +
                   "Langage, Mathématiques, Culture générale et Vitesse de traitement. " +
                   "Chaque quiz est classé par thème et niveau de difficulté.";
        }
        if (contient(question, "quiz", "quizz", "test", "évaluation", "evaluation")) {
            return "Un quiz contient plusieurs questions à choix multiples. Vous avez un temps " +
                   "limité par question. Votre score ELO s'ajuste automatiquement selon vos " +
                   "performances pour vous proposer des questions adaptées à votre niveau.";
        }
        if (contient(question, "elo", "score", "point", "niveau")) {
            return "Le score ELO mesure votre niveau cognitif. Il part de 1000 et monte quand " +
                   "vous répondez correctement et vite. Un ELO > 1200 = niveau avancé, " +
                   "< 800 = niveau débutant. Il s'adapte à chaque thème indépendamment.";
        }
        if (contient(question, "fatigue", "pause", "repos", "arrêt", "arret")) {
            return "Le système détecte votre fatigue cognitive en temps réel (ralentissement, " +
                   "erreurs répétées). Quand le score de fatigue dépasse 65%, une pause vous " +
                   "est conseillée. Au-delà de 85%, la session s'arrête automatiquement.";
        }
        if (contient(question, "difficulté", "difficulte", "facile", "difficile", "dur")) {
            return "La difficulté s'adapte automatiquement (système adaptatif). Si vous " +
                   "répondez bien, les questions deviennent plus difficiles. En cas d'erreurs, " +
                   "elles s'allègent. L'objectif est de vous maintenir dans votre zone optimale.";
        }
        if (contient(question, "rapport", "pdf", "résultat", "resultat", "bilan")) {
            return "En fin de session, un rapport PDF est généré automatiquement dans votre " +
                   "dossier personnel. Il contient : score, détail question par question, " +
                   "profil de fatigue et recommandations personnalisées.";
        }
        if (contient(question, "compte", "profil", "inscription", "mot de passe", "password")) {
            return "Vous pouvez créer un compte depuis l'écran de connexion. " +
                   "Votre profil cognitif se construit au fil des sessions et garde " +
                   "l'historique de toutes vos performances.";
        }
        if (contient(question, "bonjour", "salut", "bonsoir", "hello", "hi")) {
            return "Bonjour ! Je suis votre assistant BioSync. Je peux vous renseigner sur " +
                   "les thèmes de quiz, le score ELO, la fatigue cognitive, les rapports PDF " +
                   "ou le fonctionnement de la plateforme. Que souhaitez-vous savoir ?";
        }
        if (contient(question, "merci", "super", "parfait", "excellent", "bravo")) {
            return "Avec plaisir ! N'hésitez pas si vous avez d'autres questions.";
        }
        if (contient(question, "aide", "help", "comment", "que faire", "expliquer")) {
            return "Je peux vous aider sur : les thèmes disponibles, le système ELO, " +
                   "la détection de fatigue, les rapports PDF, les niveaux de difficulté " +
                   "ou la création de compte. Posez-moi votre question !";
        }
        return "Je ne suis pas sûr de comprendre votre question. Je peux vous renseigner sur " +
               "les thèmes de quiz, le score ELO, la fatigue cognitive, les niveaux de " +
               "difficulté, les rapports ou votre profil. Précisez votre demande !";
    }

    private boolean contient(String texte, String... mots) {
        for (String mot : mots) {
            if (texte.contains(mot)) return true;
        }
        return false;
    }

    /** Efface l'historique de conversation (nouvelle session). */
    public void reinitialiserConversation() {
        historiqueConversation.clear();
    }

    /**
     * Initialise la conversation avec un contexte (ex: résultats de la session).
     * Doit être appelé avant le premier {@link #chat}.
     */
    public void initialiserAvecContexte(String contexte) {
        reinitialiserConversation();
        // Le contexte est injecté comme premier message système fictif
        historiqueConversation.add(Map.of(
                "role", "user",
                "content", "Contexte de notre session : " + contexte
        ));
        historiqueConversation.add(Map.of(
                "role", "assistant",
                "content", "Compris, je tiens compte de ce contexte pour t'aider au mieux."
        ));
    }

    // ════════════════════════════════════════════════════════════════
    // Appels HTTP Claude API
    // ════════════════════════════════════════════════════════════════

    /** Appel simple (sans historique). */
    private String appellerClaude(String prompt, String systemPrompt) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", ConfigLoader.claudeModel());
        body.put("max_tokens", 512);
        body.put("system", systemPrompt);
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));
        return envoyerRequete(body);
    }

    private String envoyerRequete(Map<String, Object> body) {
        RequestBody requestBody = RequestBody.create(
                gson.toJson(body),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(ConfigLoader.claudeApiUrl())
                .addHeader("x-api-key", ConfigLoader.claudeApiKey())
                .addHeader("anthropic-version", "2023-06-01")
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("Claude API : {} {}", response.code(), response.message());
                return null;
            }
            String rawBody = response.body() != null ? response.body().string() : "";
            JsonObject json = JsonParser.parseString(rawBody).getAsJsonObject();
            return json.getAsJsonArray("content")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString().trim();

        } catch (IOException | JsonParseException e) {
            log.error("Erreur appel Claude : {}", e.getMessage());
            return null;
        }
    }

    // ════════════════════════════════════════════════════════════════
    // Construction du prompt de coaching
    // ════════════════════════════════════════════════════════════════

    private String construirePromptCoaching(ProfilCognitif profil,
                                             List<ReponseUtilisateur> reponses,
                                             double score,
                                             Map<String, int[]> statsTheme,
                                             Map<Integer, QuestionAdaptive> questions) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(
                "L'apprenant vient de terminer une session de quiz. Score : %.0f%%. ELO : %d.\n",
                score, profil.getScoreElo()));

        sb.append("Performances par thème :\n");
        statsTheme.forEach((theme, counts) ->
                sb.append(String.format("  - %s : %d/%d correctes (%.0f%%)\n",
                        theme, counts[0] - counts[1], counts[0],
                        counts[0] > 0 ? (double)(counts[0]-counts[1])/counts[0]*100 : 0)));

        // Les 3 dernières erreurs avec leur question
        List<ReponseUtilisateur> erreurs = reponses.stream()
                .filter(r -> !r.isEstCorrecte()).limit(3).toList();

        if (!erreurs.isEmpty()) {
            sb.append("\nExemples d'erreurs récentes :\n");
            erreurs.forEach(r -> {
                QuestionAdaptive q = questions.get(r.getQuestionId());
                if (q != null) {
                    sb.append(String.format("  - Q: \"%s\" | Réponse donnée: \"%s\" | Correcte: \"%s\"\n",
                            q.getEnonce(), r.getReponseDonnee(), q.getReponseCorrecte()));
                }
            });
        }

        sb.append("""

                Génère un coaching structuré en 3 parties :
                1. Un résumé de la performance en 1 phrase encourageante.
                2. Exactement 3 recommandations concrètes numérotées (1. 2. 3.).
                3. Un conseil de méthode d'apprentissage adapté au profil.
                """);

        return sb.toString();
    }

    /** Extrait les recommandations numérotées du texte de coaching. */
    private List<String> extraireRecommandations(String texte) {
        List<String> recs = new ArrayList<>();
        for (String ligne : texte.split("\n")) {
            if (ligne.matches("^[123][.)].+")) {
                recs.add(ligne.replaceFirst("^[123][.)]\\s*", "").trim());
            }
        }
        return recs;
    }

    // ════════════════════════════════════════════════════════════════
    // Fallbacks sans API
    // ════════════════════════════════════════════════════════════════

    private String fallbackExplication(QuestionAdaptive question, boolean estCorrecte) {
        if (estCorrecte) {
            return "Correct ! La bonne réponse est : " + question.getReponseCorrecte() + ".";
        }
        return "La bonne réponse était : " + question.getReponseCorrecte()
                + ". Prenez le temps de revoir ce concept.";
    }

    private CoachingResult fallbackCoaching(double score, String themeFaible, ProfilCognitif profil) {
        String message = String.format(
                "Session terminée avec %.0f%%. %s",
                score,
                themeFaible != null
                        ? "Travaillez particulièrement le thème : " + themeFaible + "."
                        : "Continuez vos efforts !");
        return new CoachingResult(score, themeFaible, message, List.of());
    }

    // ════════════════════════════════════════════════════════════════
    // Classe de résultat
    // ════════════════════════════════════════════════════════════════

    /**
     * Résultat d'une analyse de coaching.
     */
    public static class CoachingResult {
        private final double score;
        private final String themeFaible;
        private final String texteCoaching;
        private final List<String> recommandations;

        public CoachingResult(double score, String themeFaible,
                               String texteCoaching, List<String> recommandations) {
            this.score = score;
            this.themeFaible = themeFaible;
            this.texteCoaching = texteCoaching;
            this.recommandations = recommandations != null ? recommandations : new ArrayList<>();
        }

        public double getScore()               { return score; }
        public String getThemeFaible()         { return themeFaible; }
        public String getTexteCoaching()       { return texteCoaching; }
        public List<String> getRecommandations() { return recommandations; }
        public boolean hasRecommandations()    { return !recommandations.isEmpty(); }

        @Override
        public String toString() {
            return String.format("CoachingResult{score=%.0f%%, theme_faible='%s', recs=%d}",
                    score, themeFaible, recommandations.size());
        }
    }
}
