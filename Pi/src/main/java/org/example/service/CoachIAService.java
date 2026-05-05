package org.example.service;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import okhttp3.*;
import org.example.dao.DatabaseConnection;
import org.example.model.SeanceSport;
import org.example.model.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoachIAService {

    private static final String API_KEY = "VOTRE_CLE_ICI";
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL   = "llama-3.3-70b-versatile";

    private final OkHttpClient client;
    private final Connection   conn;

    public CoachIAService() {
        this.client = new OkHttpClient();
        this.conn   = DatabaseConnection.getConnection();
    }

    // ── MÉTHODE PRINCIPALE — chat ─────────────────────────────
    public String envoyerMessage(User user, String messageUser,
                                 List<JSONObject> historique) throws IOException {

        String contexte = construireContexte(user);
        JSONArray messages = new JSONArray();

        messages.put(new JSONObject()
                .put("role", "system")
                .put("content",
                        "Tu es un coach sportif personnel professionnel et bienveillant. " +
                                "Tu parles UNIQUEMENT en français. " +
                                "\n\n" +
                                "Tu peux faire plusieurs choses selon ce que demande l'utilisateur : " +
                                "\n" +
                                "1. SI l'utilisateur demande un PROGRAMME d'entraînement → " +
                                "génère un planning structuré jour par jour avec : " +
                                "nom exercice, nombre de séries, répétitions, temps de repos. " +
                                "\n" +
                                "2. SI l'utilisateur demande une ANALYSE ou bilan → " +
                                "analyse ses séances du profil et donne un bilan motivant avec conseils. " +
                                "\n" +
                                "3. SI l'utilisateur demande des EXERCICES pour un muscle → " +
                                "liste 5 exercices adaptés avec séries et répétitions. " +
                                "\n" +
                                "4. SI l'utilisateur demande NUTRITION ou alimentation sportive → " +
                                "donne des conseils nutritionnels adaptés à son activité physique. " +
                                "\n" +
                                "5. SI l'utilisateur demande sa PROGRESSION → " +
                                "analyse les données du profil et explique ses points forts et axes d'amélioration. " +
                                "\n" +
                                "6. RÈGLE ABSOLUE : Si le message de l'utilisateur contient un mot " +
                                "qui n'est PAS directement un terme sportif → " +
                                "'🏋️ Je suis votre coach sportif personnel. Je peux uniquement " +
                                "vous aider sur des sujets liés au sport, à l'entraînement et " +
                                "à votre forme physique. Posez-moi une question sportive !' " +
                                "\n\n" +
                                "Voici le profil de l'athlète que tu coaches :\n" + contexte
                ));

        for (JSONObject msg : historique) {
            messages.put(msg);
        }

        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", messageUser));

        JSONObject body = new JSONObject()
                .put("model",       MODEL)
                .put("messages",    messages)
                .put("max_tokens",  500)
                .put("temperature", 0.7);

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type",  "application/json")
                .post(RequestBody.create(
                        body.toString(),
                        MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return "❌ Erreur API : " + response.code();
            }
            JSONObject json = new JSONObject(response.body().string());
            return json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        }
    }

    // ── CONTEXTE SPORTIF ──────────────────────────────────────
    private String construireContexte(User user) {
        try {
            List<SeanceSport> seances = getSeancesByUser(user.getId());
            int total = seances.size();
            double dureeMoy = seances.stream()
                    .mapToInt(s -> s.dureeMinutes)
                    .average().orElse(0);

            StringBuilder dernieres = new StringBuilder();
            int count = 0;
            for (int i = seances.size() - 1; i >= 0 && count < 3; i--, count++) {
                SeanceSport s = seances.get(i);
                dernieres.append("  - ")
                        .append(s.nomSeance)
                        .append(" (")
                        .append(s.dureeMinutes)
                        .append(" min, le ")
                        .append(s.dateSeance)
                        .append(")\n");
            }

            return String.format(
                    "Nom : %s\n" +
                            "Total séances complétées : %d\n" +
                            "Durée moyenne par séance : %.0f minutes\n" +
                            "Dernières séances :\n%s",
                    user.getNomComplet(), total, dureeMoy,
                    dernieres.length() > 0 ? dernieres : "  Aucune séance\n"
            );
        } catch (Exception e) {
            return "Nom : " + user.getNomComplet() + "\nAucune donnée disponible.";
        }
    }

    // ── RÉCUPÉRER SÉANCES ─────────────────────────────────────
    private List<SeanceSport> getSeancesByUser(int userId) throws SQLException {
        List<SeanceSport> liste = new ArrayList<>();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM seance_sport WHERE utilisateur_id = ?");
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            liste.add(new SeanceSport(
                    rs.getInt("id"), rs.getString("nom_seance"),
                    rs.getString("heure_debut"), rs.getInt("duree_minutes"),
                    rs.getString("medaille_obtenue"), rs.getString("date_seance"),
                    rs.getInt("utilisateur_id"), rs.getString("heure_debut_reelle"),
                    rs.getInt("alerte_envoyee")));
        }
        rs.close(); ps.close();
        return liste;
    }

    // ── ANALYSER PROGRESSION ──────────────────────────────────
    public String analyserProgressionUser(User user,
                                          List<SeanceSport> seances) throws IOException {

        StringBuilder data = new StringBuilder();
        for (SeanceSport s : seances) {
            data.append("- ").append(s.nomSeance)
                    .append(" | ").append(s.dureeMinutes).append(" min")
                    .append(" | ").append(s.dateSeance).append("\n");
        }

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
                .put("role", "system")
                .put("content",
                        "Tu es un coach sportif analyste. " +
                                "Analyse les séances et donne un bilan court en français. " +
                                "Maximum 3 phrases : points forts, points faibles, conseil."
                ));
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content",
                        "Analyse la progression de " + user.getNomComplet() +
                                " :\n" + data));

        JSONObject body = new JSONObject()
                .put("model", MODEL)
                .put("messages", messages)
                .put("max_tokens", 300)
                .put("temperature", 0.3);

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(body.toString(),
                        MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) return "❌ Erreur API";
            JSONObject json = new JSONObject(response.body().string());
            return json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        }
    }

    // ── GÉNÉRER RECOMMANDATION ✅ AMÉLIORÉ ────────────────────
    public String genererRecommandation(User coach, User user,
                                        List<SeanceSport> seances) throws IOException {

        // 1. Lire le vrai profil depuis la base
        String profilComplet = lireProfilAthlete(user.getId(), user.getNomComplet());

        // 2. Construire les données des séances
        StringBuilder data = new StringBuilder();
        for (SeanceSport s : seances) {
            data.append("- ").append(s.nomSeance)
                    .append(" | ").append(s.dureeMinutes).append(" min")
                    .append(" | ").append(s.dateSeance).append("\n");
        }

        JSONArray messages = new JSONArray();

        // ✅ SYSTEM PROMPT AMÉLIORÉ
        messages.put(new JSONObject()
                .put("role", "system")
                .put("content",
                        "Tu es un coach sportif professionnel certifié. " +
                                "Tu génères des recommandations DÉTAILLÉES et PROFESSIONNELLES en français. " +
                                "RÈGLES STRICTES : " +
                                "1. Utilise le prénom de l'athlète dans le message. " +
                                "2. Mentionne EXPLICITEMENT son objectif principal. " +
                                "3. Adapte tout à son IMC, âge, sexe, niveau et disponibilité. " +
                                "4. Si blessures → évite les exercices qui aggravent. " +
                                "5. Si stress >7 → ajoute récupération. " +
                                "6. Si sommeil <5 → réduis intensité. " +
                                "7. Le plan semaine doit respecter sa disponibilité (jours/semaine). " +
                                "Réponds UNIQUEMENT en JSON strictement valide sans texte autour : " +
                                "{" +
                                "\"titre\": \"titre précis avec objectif et prénom\", " +
                                "\"message\": \"message professionnel 3-4 phrases\", " +
                                "\"exercices\": [" +
                                "{\"nom\": \"nom\", \"series\": 4, \"repetitions\": 10, " +
                                "\"temps_repos\": \"90 sec\", \"conseil\": \"conseil spécifique au profil\"}" +
                                "], " +
                                "\"nutrition\": \"conseil nutrition en 1 phrase adapté à l objectif\", " +
                                "\"plan_semaine\": [" +
                                "{\"jour\": \"Lundi\", \"seance\": \"nom séance\", " +
                                "\"duree\": \"45 min\", \"exercices\": \"liste courte exercices\", " +
                                "\"intensite\": \"Faible/Moyenne/Élevée\"}" +
                                "]" +
                                "}"
                ));

        // ✅ MESSAGE USER AMÉLIORÉ
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content",
                        "Génère une recommandation professionnelle et détaillée pour :\n\n" +
                                "=== PROFIL COMPLET ===\n" +
                                profilComplet +
                                "\n=== SÉANCES RÉCENTES ===\n" +
                                (data.length() > 0 ? data : "Aucune séance encore\n") +
                                "\n=== INSTRUCTIONS OBLIGATOIRES ===\n" +
                                "- Propose exactement 3 exercices adaptés à son objectif principal\n" +
                                "- Adapte les séries/répétitions à son objectif\n" +
                                "- Tiens compte de sa disponibilité pour doser l'intensité\n" +
                                "- Tiens compte de son niveau de stress et sommeil\n" +
                                "- Chaque conseil doit expliquer POURQUOI c'est adapté à SON profil\n" +
                                "- Sois précis, professionnel et motivant\n" +
                                "- Utilise son prénom dans le message"
                ));

        // ✅ TOKENS AUGMENTÉS
        JSONObject body = new JSONObject()
                .put("model", MODEL)
                .put("messages", messages)
                .put("max_tokens", 1000)
                .put("temperature", 0.4);

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(body.toString(),
                        MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) return null;
            JSONObject json = new JSONObject(response.body().string());
            return json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        }
    }

    // ── LIRE PROFIL COMPLET DEPUIS LA BASE ────────────────────
    private String lireProfilAthlete(int userId, String nomComplet) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM profil_athlete WHERE user_id = ?");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int age       = rs.getInt("age");
                double poids  = rs.getDouble("poids_kg");
                double taille = rs.getDouble("taille_cm");
                String sexe   = rs.getString("sexe");

                // Calcul IMC
                double imc = 0;
                String imcNote = "Non calculable";
                if (taille > 0) {
                    imc = poids / ((taille / 100) * (taille / 100));
                    imcNote = imc < 18.5 ? "Insuffisance pondérale" :
                            imc < 25   ? "Normal" :
                            imc < 30   ? "Surpoids" : "Obésité";
                }

                String blessures  = rs.getString("blessures");
                String medics     = rs.getString("medicaments");
                String historique = rs.getString("historique_medical");
                String objectif   = rs.getString("objectif");
                String niveau     = rs.getString("niveau_sport");
                int dispo         = rs.getInt("disponibilite_semaine");
                String emotion    = rs.getString("etat_emotionnel");
                int stress        = rs.getInt("niveau_stress");
                int sommeil       = rs.getInt("qualite_sommeil");
                String alim       = rs.getString("alimentation");

                rs.close(); ps.close();

                return String.format(
                        "Nom       : %s\n" +
                                "Âge       : %d ans\n" +
                                "Sexe      : %s\n" +
                                "Poids     : %.1f kg\n" +
                                "Taille    : %.0f cm\n" +
                                "IMC       : %.1f (%s)\n" +
                                "Objectif  : %s\n" +
                                "Niveau    : %s\n" +
                                "Dispo     : %d jours/semaine\n" +
                                "Émotion   : %s\n" +
                                "Stress    : %d/10\n" +
                                "Sommeil   : %d/10\n" +
                                "Aliment.  : %s\n" +
                                "Blessures : %s\n" +
                                "Médic.    : %s\n" +
                                "Historique: %s\n",
                        nomComplet, age,
                        sexe != null ? sexe : "Non renseigné",
                        poids, taille, imc, imcNote,
                        objectif  != null ? objectif  : "Non renseigné",
                        niveau    != null ? niveau    : "Non renseigné",
                        dispo,
                        emotion   != null ? emotion   : "Non renseigné",
                        stress, sommeil,
                        alim      != null ? alim      : "Non renseigné",
                        blessures != null && !blessures.isEmpty() ? blessures : "Aucune",
                        medics    != null && !medics.isEmpty()    ? medics    : "Aucun",
                        historique!= null && !historique.isEmpty()? historique: "Aucun"
                );
            }
            rs.close(); ps.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "Nom : " + nomComplet + "\nProfil non renseigné — recommandation générale.";
    }
}