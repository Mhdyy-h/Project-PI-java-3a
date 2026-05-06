package org.example.service;

import org.example.DatabaseConnection;
import org.example.model.ProfilCognitif;
import org.example.model.QuestionAdaptive;
import org.example.model.ReponseUtilisateur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.util.ConfigLoader;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Parcours d'apprentissage adaptatif basé sur un algorithme ELO.
 *
 * <h3>Principe ELO appliqué aux quiz</h3>
 * <ul>
 *   <li>Chaque utilisateur a une <b>cote ELO</b> (départ : 1000).</li>
 *   <li>Chaque question a une <b>difficulté ELO</b> calculée depuis son niveau 1-10.</li>
 *   <li>Après chaque réponse, la cote évolue selon la formule ELO standard :
 *       {@code nouvelleCote = ancienneCote + K * (résultat - probabilitéAttendue)}</li>
 *   <li>La prochaine question est sélectionnée avec une difficulté proche de la cote actuelle,
 *       légèrement au-dessus pour maintenir le défi.</li>
 * </ul>
 *
 * <h3>Conversion niveau ↔ ELO</h3>
 * <pre>
 *   niveau 1  →  800 ELO   (très facile)
 *   niveau 5  → 1600 ELO   (intermédiaire)
 *   niveau 10 → 2600 ELO   (expert)
 * </pre>
 *
 * <p>Exemple d'utilisation :</p>
 * <pre>
 *   AdaptiveService svc = new AdaptiveService();
 *
 *   // Récupérer ou créer le profil de l'utilisateur
 *   ProfilCognitif profil = svc.getOuCreerProfil(utilisateurId);
 *
 *   // Sélectionner la première question
 *   QuestionAdaptive q = svc.prochaineQuestion(profil, "Informatique", sessionId);
 *
 *   // Après chaque réponse
 *   svc.mettreAJourElo(profil, q, reponse.isEstCorrecte());
 *   svc.sauvegarderProfil(profil);
 * </pre>
 */
public class AdaptiveService {

    private static final Logger log = LoggerFactory.getLogger(AdaptiveService.class);

    private final int eloInitial;
    private final int kFactor;
    private final int eloBase;    // ELO pour niveau 1
    private final int eloStep;    // incrément ELO par niveau

    public AdaptiveService() {
        this.eloInitial = ConfigLoader.eloInitialScore();
        this.kFactor    = ConfigLoader.eloKFactor();
        this.eloBase    = ConfigLoader.getInt("elo.difficulty.base", 800);
        this.eloStep    = ConfigLoader.getInt("elo.difficulty.step", 200);
    }

    // ════════════════════════════════════════════════════════════════
    // Gestion du profil cognitif
    // ════════════════════════════════════════════════════════════════

    /**
     * Récupère le profil cognitif d'un utilisateur, ou en crée un nouveau s'il n'existe pas.
     */
    public ProfilCognitif getOuCreerProfil(int utilisateurId) {
        String sql = "SELECT * FROM profil_cognitif WHERE utilisateur_id = ?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, utilisateurId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapperProfil(rs);
            }
        } catch (SQLException e) {
            log.error("Erreur lecture profil : {}", e.getMessage());
        }
        // Crée et persiste un nouveau profil
        ProfilCognitif nouveau = new ProfilCognitif(utilisateurId);
        nouveau.setScoreElo(eloInitial);
        insererProfil(nouveau);
        return nouveau;
    }

    /**
     * Persiste les modifications d'un profil cognitif existant.
     */
    public void sauvegarderProfil(ProfilCognitif profil) {
        String sql = """
            UPDATE profil_cognitif
            SET score_elo = ?, taux_reussite = ?, temps_reponse_moyen_ms = ?,
                sessions_totales = ?, theme_fort = ?, theme_faible = ?,
                niveau_actuel = ?, date_mise_a_jour = ?
            WHERE utilisateur_id = ?
            """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            profil.recalculerNiveau();
            ps.setInt(1, profil.getScoreElo());
            ps.setDouble(2, profil.getTauxReussite());
            ps.setInt(3, profil.getTempsReponseMoyenMs());
            ps.setInt(4, profil.getSessionsTotales());
            ps.setString(5, profil.getThemeFort());
            ps.setString(6, profil.getThemeFaible());
            ps.setString(7, profil.getNiveauActuel());
            ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(9, profil.getUtilisateurId());
            ps.executeUpdate();

        } catch (SQLException e) {
            log.error("Erreur sauvegarde profil : {}", e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // Sélection adaptative de la prochaine question
    // ════════════════════════════════════════════════════════════════

    /**
     * Sélectionne la prochaine question adaptée à la cote ELO courante.
     *
     * <p>La question ciblée est légèrement plus difficile que la cote de l'utilisateur
     * (zone proximale de développement) pour maintenir l'engagement.</p>
     *
     * @param profil    profil cognitif de l'utilisateur (cote ELO à jour)
     * @param theme     thème de la session (null = tous les thèmes)
     * @param sessionId ID de la session, pour exclure les questions déjà posées
     * @return question sélectionnée, ou {@code null} si la banque est épuisée
     */
    public QuestionAdaptive prochaineQuestion(ProfilCognitif profil, String theme, int sessionId) {
        // Cible légèrement au-dessus (+50 ELO) pour maintenir le défi
        int cibleElo = profil.getScoreElo() + 50;
        int niveauCible = elToNiveau(cibleElo);

        // Cherche d'abord exactement le niveau cible, puis élargit la recherche
        for (int marge = 0; marge <= 3; marge++) {
            int nMin = Math.max(1, niveauCible - marge);
            int nMax = Math.min(10, niveauCible + marge);
            QuestionAdaptive q = chercherQuestion(theme, nMin, nMax, sessionId);
            if (q != null) {
                log.debug("Question sélectionnée : id={}, difficulté={} (ELO utilisateur={})",
                        q.getId(), q.getNiveauDifficulte(), profil.getScoreElo());
                return q;
            }
        }
        log.warn("Banque de questions épuisée pour le thème '{}', session {}", theme, sessionId);
        return null;
    }

    // ════════════════════════════════════════════════════════════════
    // Mise à jour ELO
    // ════════════════════════════════════════════════════════════════

    /**
     * Met à jour la cote ELO du profil après une réponse.
     *
     * @param profil      profil à modifier (en mémoire, appeler {@link #sauvegarderProfil} ensuite)
     * @param question    question à laquelle l'utilisateur vient de répondre
     * @param estCorrecte {@code true} si la réponse était correcte
     */
    public void mettreAJourElo(ProfilCognitif profil, QuestionAdaptive question, boolean estCorrecte) {
        int eloQuestion = niveauToElo(question.getNiveauDifficulte());
        double probabiliteReussite = probabiliteAttendue(profil.getScoreElo(), eloQuestion);
        int resultat = estCorrecte ? 1 : 0;

        int delta = (int) Math.round(kFactor * (resultat - probabiliteReussite));
        int nouvelElo = Math.max(100, profil.getScoreElo() + delta);

        log.debug("ELO {} → {} (question niv.{}, correct={}, Δ={})",
                profil.getScoreElo(), nouvelElo,
                question.getNiveauDifficulte(), estCorrecte, delta);

        profil.setScoreElo(nouvelElo);
        profil.recalculerNiveau();
    }

    /**
     * Met à jour les statistiques agrégées du profil en fin de session.
     *
     * @param profil         profil à mettre à jour
     * @param reponses       toutes les réponses de la session
     * @param scoreSession   score de la session (% bonnes réponses)
     */
    public void mettreAJourStats(ProfilCognitif profil,
                                  List<ReponseUtilisateur> reponses,
                                  double scoreSession) {
        if (reponses.isEmpty()) return;

        profil.setSessionsTotales(profil.getSessionsTotales() + 1);

        // Mise à jour du taux de réussite global (moyenne cumulative)
        int total = profil.getSessionsTotales();
        double nouveauTaux = ((profil.getTauxReussite() * (total - 1)) + scoreSession) / total;
        profil.setTauxReussite(nouveauTaux);

        // Mise à jour du temps de réponse moyen global
        int tempsMoyenSession = (int) reponses.stream()
                .mapToInt(ReponseUtilisateur::getTempsReponseMs)
                .average()
                .orElse(0);
        int nouveauTemps = ((profil.getTempsReponseMoyenMs() * (total - 1)) + tempsMoyenSession) / total;
        profil.setTempsReponseMoyenMs(nouveauTemps);

        profil.recalculerNiveau();
    }

    // ════════════════════════════════════════════════════════════════
    // Recommandations thématiques
    // ════════════════════════════════════════════════════════════════

    /**
     * Identifie le thème le plus fort et le plus faible de l'utilisateur
     * en analysant son historique de réponses.
     *
     * @param utilisateurId ID de l'utilisateur
     * @return tableau de 2 éléments : [themeFort, themeFaible], peut contenir null
     */
    public String[] analyserThemes(int utilisateurId) {
        String sql = """
            SELECT q.theme,
                   COUNT(*) as total,
                   SUM(CASE WHEN ru.est_correcte = 1 THEN 1 ELSE 0 END) as correctes
            FROM reponse_utilisateur ru
            JOIN question q ON ru.question_id = q.id
            JOIN quiz_session qs ON ru.session_id = qs.id
            WHERE qs.utilisateur_id = ?
            GROUP BY q.theme
            HAVING COUNT(*) >= 3
            ORDER BY (SUM(CASE WHEN ru.est_correcte = 1 THEN 1 ELSE 0 END) / COUNT(*)) DESC
            """;

        List<String> themes = new ArrayList<>();
        List<Double> taux   = new ArrayList<>();

        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, utilisateurId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    themes.add(rs.getString("theme"));
                    taux.add((double) rs.getInt("correctes") / rs.getInt("total"));
                }
            }
        } catch (SQLException e) {
            log.error("Erreur analyserThemes : {}", e.getMessage());
        }

        if (themes.isEmpty()) return new String[]{null, null};
        String fort   = themes.get(0);
        String faible = themes.size() > 1 ? themes.get(themes.size() - 1) : themes.get(0);
        return new String[]{fort, faible};
    }

    // ════════════════════════════════════════════════════════════════
    // Formules ELO
    // ════════════════════════════════════════════════════════════════

    /** Probabilité attendue que l'utilisateur réussisse une question de cote donnée. */
    public double probabiliteAttendue(int eloUtilisateur, int eloQuestion) {
        return 1.0 / (1.0 + Math.pow(10.0, (eloQuestion - eloUtilisateur) / 400.0));
    }

    /** Convertit un niveau 1-10 en cote ELO. */
    public int niveauToElo(int niveau) {
        return eloBase + (niveau - 1) * eloStep;
    }

    /** Convertit une cote ELO en niveau 1-10 (borné). */
    public int elToNiveau(int elo) {
        int niveau = (int) Math.round((elo - eloBase) / (double) eloStep) + 1;
        return Math.max(1, Math.min(10, niveau));
    }

    // ════════════════════════════════════════════════════════════════
    // Requêtes SQL internes
    // ════════════════════════════════════════════════════════════════

    private QuestionAdaptive chercherQuestion(String theme, int niveauMin, int niveauMax, int sessionId) {
        String whereTheme = (theme != null && !theme.isBlank()) ? "AND theme = ?" : "";
        String sql = String.format("""
            SELECT * FROM question
            WHERE niveau_difficulte BETWEEN ? AND ?
            %s
            AND id NOT IN (
                SELECT question_id FROM reponse_utilisateur WHERE session_id = ?
            )
            ORDER BY RAND()
            LIMIT 1
            """, whereTheme);

        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            int idx = 1;
            ps.setInt(idx++, niveauMin);
            ps.setInt(idx++, niveauMax);
            if (theme != null && !theme.isBlank()) ps.setString(idx++, theme);
            ps.setInt(idx, sessionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapperQuestion(rs);
            }
        } catch (SQLException e) {
            log.error("Erreur chercherQuestion : {}", e.getMessage());
        }
        return null;
    }

    private void insererProfil(ProfilCognitif p) {
        String sql = """
            INSERT INTO profil_cognitif
              (utilisateur_id, score_elo, taux_reussite, temps_reponse_moyen_ms,
               sessions_totales, niveau_actuel)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, p.getUtilisateurId());
            ps.setInt(2, p.getScoreElo());
            ps.setDouble(3, p.getTauxReussite());
            ps.setInt(4, p.getTempsReponseMoyenMs());
            ps.setInt(5, p.getSessionsTotales());
            ps.setString(6, p.getNiveauActuel());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) p.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            log.error("Erreur insererProfil : {}", e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // Mappers ResultSet
    // ════════════════════════════════════════════════════════════════

    private ProfilCognitif mapperProfil(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("date_mise_a_jour");
        return new ProfilCognitif(
                rs.getInt("id"),
                rs.getInt("utilisateur_id"),
                rs.getInt("score_elo"),
                rs.getDouble("taux_reussite"),
                rs.getInt("temps_reponse_moyen_ms"),
                rs.getInt("sessions_totales"),
                rs.getString("theme_fort"),
                rs.getString("theme_faible"),
                rs.getString("niveau_actuel"),
                ts != null ? ts.toLocalDateTime() : LocalDateTime.now()
        );
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
        // options JSON → List<String> parsé dans QuestionGenService
        return q;
    }
}
