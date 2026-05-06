package org.example.service;

import org.example.DatabaseConnection;
import org.example.model.FatigueLog;
import org.example.model.ReponseUtilisateur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.util.ConfigLoader;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Détection logicielle de la fatigue cognitive pendant une session de quiz.
 *
 * <p>Algorithme (sans webcam) :</p>
 * <ol>
 *   <li>Les 3 premières réponses établissent un <b>temps de référence</b> (baseline).</li>
 *   <li>Une <b>fenêtre glissante</b> (taille configurable, défaut 5) calcule le temps
 *       moyen récent et le taux d'erreur récent.</li>
 *   <li>Le <b>score de fatigue</b> (0-100) est une combinaison pondérée :
 *       <ul>
 *         <li>50% — dégradation du temps de réponse vs baseline</li>
 *         <li>35% — taux d'erreur dans la fenêtre glissante</li>
 *         <li>15% — durée cumulée de la session</li>
 *       </ul>
 *   </li>
 *   <li>Une <b>suggestion</b> est émise : CONTINUER / PAUSE_COURTE / ARRETER.</li>
 * </ol>
 *
 * <p>Exemple d'utilisation :</p>
 * <pre>
 *   FatigueService svc = new FatigueService();
 *
 *   // Appeler après chaque réponse
 *   FatigueLog log = svc.evaluer(sessionId, utilisateurId, numeroQuestion, toutesLesReponses);
 *   svc.enregistrer(log);
 *
 *   if (log.necessitePause()) {
 *       afficherDialoguePause();
 *   }
 * </pre>
 */
public class FatigueService {

    private static final Logger log = LoggerFactory.getLogger(FatigueService.class);

    /** Nombre minimum de réponses avant d'activer le calcul de fatigue. */
    private static final int BASELINE_MIN = 3;

    // ── Seuils lus depuis config.properties ─────────────────────────
    private final int windowSize;
    private final double pauseThreshold;
    private final double stopThreshold;
    private final int slowThresholdMs;

    public FatigueService() {
        this.windowSize     = ConfigLoader.fatigueSlidingWindowSize();
        this.pauseThreshold = ConfigLoader.fatiguePauseThreshold();
        this.stopThreshold  = ConfigLoader.fatigueStopThreshold();
        this.slowThresholdMs = ConfigLoader.fatigueSlowThresholdMs();
    }

    // ════════════════════════════════════════════════════════════════
    // API publique
    // ════════════════════════════════════════════════════════════════

    /**
     * Évalue la fatigue après la {@code numeroQuestion}-ième réponse.
     *
     * @param sessionId       ID de la session en cours
     * @param utilisateurId   ID de l'utilisateur
     * @param numeroQuestion  Position 1-based dans la session
     * @param toutesReponses  Toutes les réponses de la session (ordre chronologique)
     * @return FatigueLog prêt à être persisté via {@link #enregistrer}
     */
    public FatigueLog evaluer(int sessionId, int utilisateurId,
                               int numeroQuestion, List<ReponseUtilisateur> toutesReponses) {

        FatigueLog fatigueLog = new FatigueLog();
        fatigueLog.setSessionId(sessionId);
        fatigueLog.setUtilisateurId(utilisateurId);
        fatigueLog.setNumeroQuestion(numeroQuestion);
        fatigueLog.setTypeDetection("LOGICIELLE");

        if (toutesReponses == null || toutesReponses.size() < BASELINE_MIN) {
            fatigueLog.setScoreFatigue(0.0);
            fatigueLog.setSuggestion("CONTINUER");
            return fatigueLog;
        }

        double baseline = calculerBaseline(toutesReponses);
        List<ReponseUtilisateur> fenetre = extraireFenetre(toutesReponses);

        int tempsGlissant        = calculerTempsGlissant(fenetre);
        double tauxErreurGlissant = calculerTauxErreur(fenetre);
        double dureeMin           = toutesReponses.size() * (baseline / 60_000.0);

        double scoreTemps   = calculerScoreTemps(tempsGlissant, baseline);
        double scoreErreur  = tauxErreurGlissant * 100.0;
        double scoreDuree   = Math.min(100.0, dureeMin * 2.5);

        double score = Math.min(100.0,
            0.50 * scoreTemps + 0.35 * scoreErreur + 0.15 * scoreDuree);

        fatigueLog.setScoreFatigue(score);
        fatigueLog.setTempsReponseGlissantMs(tempsGlissant);
        fatigueLog.setTauxErreurGlissant(tauxErreurGlissant);
        fatigueLog.setSuggestion(determinerSuggestion(score));

        log.debug("Fatigue Q{}: score={:.1f}, tempsGlissant={}ms, erreur={:.0f}%, suggestion={}",
                numeroQuestion, score, tempsGlissant,
                tauxErreurGlissant * 100, fatigueLog.getSuggestion());

        return fatigueLog;
    }

    /**
     * Persiste un {@link FatigueLog} dans la table {@code fatigue_log}.
     */
    public void enregistrer(FatigueLog fl) {
        String sql = """
            INSERT INTO fatigue_log
              (session_id, utilisateur_id, numero_question, score_fatigue,
               temps_reponse_glissant_ms, taux_erreur_glissant, type_detection, suggestion)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, fl.getSessionId());
            ps.setInt(2, fl.getUtilisateurId());
            ps.setInt(3, fl.getNumeroQuestion());
            ps.setDouble(4, fl.getScoreFatigue());
            ps.setInt(5, fl.getTempsReponseGlissantMs());
            ps.setDouble(6, fl.getTauxErreurGlissant());
            ps.setString(7, fl.getTypeDetection());
            ps.setString(8, fl.getSuggestion());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) fl.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            log.error("Erreur lors de l'enregistrement du FatigueLog : {}", e.getMessage());
        }
    }

    /**
     * Récupère tous les logs de fatigue d'une session, triés par numéro de question.
     */
    public List<FatigueLog> getParSession(int sessionId) {
        List<FatigueLog> liste = new ArrayList<>();
        String sql = """
            SELECT * FROM fatigue_log
            WHERE session_id = ?
            ORDER BY numero_question
            """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapper(rs));
            }
        } catch (SQLException e) {
            log.error("Erreur getParSession fatigue_log : {}", e.getMessage());
        }
        return liste;
    }

    /**
     * Calcule le score de fatigue moyen sur toute une session.
     *
     * @return score moyen 0-100, ou 0.0 si aucun log
     */
    public double scoreMoyenSession(int sessionId) {
        return getParSession(sessionId).stream()
                .mapToDouble(FatigueLog::getScoreFatigue)
                .average()
                .orElse(0.0);
    }

    // ════════════════════════════════════════════════════════════════
    // Algorithme interne
    // ════════════════════════════════════════════════════════════════

    /** Temps de référence = moyenne des {@value #BASELINE_MIN} premières réponses. */
    private double calculerBaseline(List<ReponseUtilisateur> reponses) {
        return reponses.subList(0, Math.min(BASELINE_MIN, reponses.size()))
                .stream()
                .mapToInt(ReponseUtilisateur::getTempsReponseMs)
                .average()
                .orElse(slowThresholdMs);
    }

    /** Extrait les N dernières réponses (fenêtre glissante). */
    private List<ReponseUtilisateur> extraireFenetre(List<ReponseUtilisateur> reponses) {
        int debut = Math.max(0, reponses.size() - windowSize);
        return reponses.subList(debut, reponses.size());
    }

    /** Moyenne du temps de réponse dans la fenêtre. */
    private int calculerTempsGlissant(List<ReponseUtilisateur> fenetre) {
        return (int) fenetre.stream()
                .mapToInt(ReponseUtilisateur::getTempsReponseMs)
                .average()
                .orElse(0);
    }

    /** Taux d'erreur dans la fenêtre (0.0 à 1.0). */
    private double calculerTauxErreur(List<ReponseUtilisateur> fenetre) {
        if (fenetre.isEmpty()) return 0.0;
        long erreurs = fenetre.stream().filter(r -> !r.isEstCorrecte()).count();
        return (double) erreurs / fenetre.size();
    }

    /**
     * Convertit la dégradation relative du temps de réponse en score 0-100.
     * +67% de temps supplémentaire → score 100.
     */
    private double calculerScoreTemps(int tempsGlissant, double baseline) {
        if (baseline <= 0) return 0.0;
        double relatif = (tempsGlissant - baseline) / baseline;
        // amplificateur 1.5 : 67% plus lent = score max
        return Math.min(100.0, Math.max(0.0, relatif * 150.0));
    }

    private String determinerSuggestion(double score) {
        if (score >= stopThreshold)  return "ARRETER";
        if (score >= pauseThreshold) return "PAUSE_COURTE";
        return "CONTINUER";
    }

    // ════════════════════════════════════════════════════════════════
    // Mapping ResultSet → FatigueLog
    // ════════════════════════════════════════════════════════════════

    private FatigueLog mapper(ResultSet rs) throws SQLException {
        return new FatigueLog(
                rs.getInt("id"),
                rs.getInt("session_id"),
                rs.getInt("utilisateur_id"),
                rs.getInt("numero_question"),
                rs.getDouble("score_fatigue"),
                rs.getInt("temps_reponse_glissant_ms"),
                rs.getDouble("taux_erreur_glissant"),
                rs.getString("type_detection"),
                rs.getString("suggestion"),
                rs.getTimestamp("timestamp").toLocalDateTime()
        );
    }
}
