package org.example.service;

import org.example.DatabaseConnection;
import org.example.service.ProgressionIAModel;
import org.example.model.QuizSession;
import org.example.model.ReponseUtilisateur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Prédiction de performance cognitive basée sur la régression linéaire.
 *
 * <p>Réutilise {@link ProgressionIAModel} (déjà présent dans le projet)
 * en l'adaptant aux sessions de quiz plutôt qu'aux séances sportives.</p>
 *
 * <h3>Deux modes de prédiction</h3>
 * <ol>
 *   <li><b>Prédiction historique</b> — entraîne le modèle sur l'historique des sessions
 *       passées et prédit le score des N prochaines sessions.</li>
 *   <li><b>Prédiction précoce</b> — après seulement 3 questions dans une session en cours,
 *       estime le score final à partir du taux de réussite + le temps de réponse normalisé.</li>
 * </ol>
 *
 * <p>Exemple d'utilisation :</p>
 * <pre>
 *   PredictionService svc = new PredictionService();
 *
 *   // Prédiction basée sur l'historique
 *   ResultatPrediction pred = svc.predireProgression(utilisateurId, 3);
 *   System.out.println("Score prédit dans 3 sessions : " + pred.getScorePredit() + "%");
 *
 *   // Prédiction précoce (en direct pendant la session)
 *   double scoreFinal = svc.predirePrecedemment(reponsesSoFar, 20);
 * </pre>
 */
public class PredictionService {

    private static final Logger log = LoggerFactory.getLogger(PredictionService.class);

    /** Cache des modèles par utilisateur pour éviter de réentraîner à chaque appel. */
    private final Map<Integer, ProgressionIAModel> modeleCache = new HashMap<>();

    // ════════════════════════════════════════════════════════════════
    // Prédiction basée sur l'historique
    // ════════════════════════════════════════════════════════════════

    /**
     * Prédit le score d'un utilisateur dans {@code sessionsFutures} sessions,
     * en entraînant un modèle de régression linéaire sur son historique.
     *
     * @param utilisateurId  ID de l'utilisateur
     * @param sessionsFutures nombre de sessions dans le futur à prédire (ex: 3 = "dans 3 sessions")
     * @return résultat de prédiction structuré, ou {@code null} si pas assez d'historique
     */
    public ResultatPrediction predireProgression(int utilisateurId, int sessionsFutures) {
        List<Double> historique = chargerHistoriqueScores(utilisateurId);

        if (historique.size() < 2) {
            log.info("Pas assez d'historique pour l'utilisateur {} ({} session(s))",
                    utilisateurId, historique.size());
            return null;
        }

        ProgressionIAModel modele = getOuCreerModele(utilisateurId, historique);
        if (!modele.isEntrainee()) return null;

        int nbSessionsPassees = historique.size();
        ProgressionIAModel.ResultatPrediction pred =
                modele.predireFutur(nbSessionsPassees, sessionsFutures);

        log.info("Prédiction utilisateur {} : {:.1f}% dans {} sessions (R²={:.2f})",
                utilisateurId, pred.getValeurPredite(), sessionsFutures, modele.calculerR2(historique));

        return new ResultatPrediction(
                pred.getValeurPredite(),
                pred.getProgression(),
                pred.getProgressionPourcentage(),
                pred.getInterpretation(),
                modele.calculerR2(historique),
                historique.size()
        );
    }

    /**
     * Calcule la tendance récente (5 dernières sessions) d'un utilisateur.
     *
     * @return +X.X (amélioration) ou -X.X (dégradation) en points de pourcentage par session
     */
    public double calculerTendance(int utilisateurId) {
        List<Double> historique = chargerHistoriqueScores(utilisateurId);
        if (historique.size() < 3) return 0.0;

        // Prend les 5 dernières sessions max
        List<Double> recentes = historique.subList(
                Math.max(0, historique.size() - 5), historique.size());

        ProgressionIAModel modele = new ProgressionIAModel();
        modele.entrainer(recentes);
        if (!modele.isEntrainee()) return 0.0;

        // La pente du modèle = variation par session
        double scoreActuel    = modele.predire(recentes.size());
        double scoreSuivant   = modele.predire(recentes.size() + 1);
        return scoreSuivant - scoreActuel;
    }

    // ════════════════════════════════════════════════════════════════
    // Prédiction précoce (en cours de session)
    // ════════════════════════════════════════════════════════════════

    /**
     * Prédit le score final d'une session en cours à partir des premières réponses.
     *
     * <p>Algorithme pondéré :</p>
     * <ul>
     *   <li>60% — taux de réussite sur les réponses actuelles</li>
     *   <li>40% — facteur de confiance lié aux temps de réponse (rapide = confiant = bon score)</li>
     * </ul>
     *
     * @param reponsesActuelles réponses obtenues jusqu'ici (min 3 recommandées)
     * @param totalQuestions    nombre total de questions prévu dans la session
     * @return score final estimé en pourcentage (0-100)
     */
    public double predirePrecedemment(List<ReponseUtilisateur> reponsesActuelles, int totalQuestions) {
        if (reponsesActuelles == null || reponsesActuelles.isEmpty()) return 50.0;

        long correctes = reponsesActuelles.stream()
                .filter(ReponseUtilisateur::isEstCorrecte).count();
        double tauxActuel = (double) correctes / reponsesActuelles.size();

        // Facteur confiance : temps de réponse moyen normalisé
        // < 3s = confiant (0.9), > 10s = hésitant (0.6), 5s = neutre (0.75)
        double tempsMoyenSec = reponsesActuelles.stream()
                .mapToInt(ReponseUtilisateur::getTempsReponseMs)
                .average().orElse(5000) / 1000.0;
        double facteurConfiance = Math.max(0.5, Math.min(0.95, 1.0 - (tempsMoyenSec - 3.0) * 0.04));

        double scorePredit = (0.60 * tauxActuel + 0.40 * facteurConfiance) * 100.0;

        log.debug("Prédiction précoce ({}/{} Q) : taux={:.0f}%, conf={:.2f} → {:.1f}%",
                reponsesActuelles.size(), totalQuestions,
                tauxActuel * 100, facteurConfiance, scorePredit);

        return Math.min(100.0, Math.max(0.0, scorePredit));
    }

    /**
     * Retourne un label de confiance pour la prédiction précoce.
     *
     * @param nbReponses nombre de réponses déjà obtenues
     * @return "Faible", "Modérée" ou "Élevée"
     */
    public String niveauConfiancePrediction(int nbReponses) {
        if (nbReponses < 3)  return "Faible (données insuffisantes)";
        if (nbReponses < 7)  return "Modérée";
        return "Élevée";
    }

    // ════════════════════════════════════════════════════════════════
    // Analyse comparative (vs. moyenne des autres utilisateurs)
    // ════════════════════════════════════════════════════════════════

    /**
     * Compare le score d'un utilisateur à la moyenne globale pour un thème donné.
     *
     * @param utilisateurId ID de l'utilisateur à comparer
     * @param theme         thème de quiz (null = tous)
     * @return delta en points (positif = au-dessus de la moyenne)
     */
    public double comparerALaMoyenne(int utilisateurId, String theme) {
        double moyenneUtilisateur = calculerScoreMoyen(utilisateurId, theme);
        double moyenneGlobale     = calculerScoreMoyenGlobal(theme);
        return moyenneUtilisateur - moyenneGlobale;
    }

    // ════════════════════════════════════════════════════════════════
    // Accès DB
    // ════════════════════════════════════════════════════════════════

    /**
     * Charge les scores des sessions passées dans l'ordre chronologique.
     */
    public List<Double> chargerHistoriqueScores(int utilisateurId) {
        List<Double> scores = new ArrayList<>();
        String sql = """
            SELECT score_final FROM quiz_session
            WHERE utilisateur_id = ? AND statut = 'TERMINEE' AND score_final IS NOT NULL
            ORDER BY date_debut ASC
            """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, utilisateurId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) scores.add(rs.getDouble("score_final"));
            }
        } catch (SQLException e) {
            log.error("Erreur chargerHistoriqueScores : {}", e.getMessage());
        }
        return scores;
    }

    private double calculerScoreMoyen(int utilisateurId, String theme) {
        String sql = """
            SELECT AVG(qs.score_final) FROM quiz_session qs
            WHERE qs.utilisateur_id = ? AND qs.statut = 'TERMINEE'
            """ + (theme != null ? "AND qs.theme = ?" : "");
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, utilisateurId);
            if (theme != null) ps.setString(2, theme);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            log.error("Erreur calculerScoreMoyen : {}", e.getMessage());
        }
        return 0.0;
    }

    private double calculerScoreMoyenGlobal(String theme) {
        String sql = "SELECT AVG(score_final) FROM quiz_session WHERE statut = 'TERMINEE'"
                + (theme != null ? " AND theme = ?" : "");
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            if (theme != null) ps.setString(1, theme);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            log.error("Erreur calculerScoreMoyenGlobal : {}", e.getMessage());
        }
        return 0.0;
    }

    // ════════════════════════════════════════════════════════════════
    // Gestion du cache de modèles
    // ════════════════════════════════════════════════════════════════

    private ProgressionIAModel getOuCreerModele(int utilisateurId, List<Double> historique) {
        // Invalider le cache si l'historique a changé (nouvelle session)
        ProgressionIAModel modele = modeleCache.get(utilisateurId);
        if (modele == null) {
            modele = new ProgressionIAModel();
            modele.entrainer(historique);
            modeleCache.put(utilisateurId, modele);
        }
        return modele;
    }

    /** Invalide le cache d'un utilisateur (appeler après chaque nouvelle session terminée). */
    public void invaliderCache(int utilisateurId) {
        modeleCache.remove(utilisateurId);
    }

    // ════════════════════════════════════════════════════════════════
    // Classe de résultat
    // ════════════════════════════════════════════════════════════════

    /**
     * Résultat structuré d'une prédiction historique.
     */
    public static class ResultatPrediction {

        private final double scorePredit;
        private final double progression;
        private final double pourcentageProgression;
        private final String interpretation;
        /** R² du modèle (0-1) : qualité de la régression. */
        private final double r2;
        /** Nombre de sessions utilisées pour l'entraînement. */
        private final int    nbSessionsEntrainement;

        public ResultatPrediction(double scorePredit, double progression,
                                   double pourcentageProgression, String interpretation,
                                   double r2, int nbSessions) {
            this.scorePredit = scorePredit;
            this.progression = progression;
            this.pourcentageProgression = pourcentageProgression;
            this.interpretation = interpretation;
            this.r2 = r2;
            this.nbSessionsEntrainement = nbSessions;
        }

        public double getScorePredit()              { return scorePredit; }
        public double getProgression()              { return progression; }
        public double getPourcentageProgression()   { return pourcentageProgression; }
        public String getInterpretation()           { return interpretation; }
        public double getR2()                       { return r2; }
        public int    getNbSessionsEntrainement()   { return nbSessionsEntrainement; }

        /** {@code true} si le modèle est suffisamment fiable (R² ≥ 0.6). */
        public boolean estFiable() { return r2 >= 0.6; }

        public String getNiveauConfiance() {
            if (r2 >= 0.85) return "Élevée";
            if (r2 >= 0.60) return "Modérée";
            return "Faible";
        }

        @Override
        public String toString() {
            return String.format("Prédiction{score=%.1f%%, prog=%+.1f%%, R²=%.2f, confiance=%s}",
                    scorePredit, progression, r2, getNiveauConfiance());
        }
    }
}
