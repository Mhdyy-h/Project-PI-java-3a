package org.example.service;

import org.example.DatabaseConnection;
import org.example.model.AnomalieEvent;
import org.example.model.ReponseUtilisateur;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.util.ConfigLoader;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Détection d'anomalies comportementales pendant une session de quiz.
 *
 * <h3>Anomalies détectées</h3>
 * <table border="1">
 *   <tr><th>Type</th><th>Méthode</th><th>Gravité</th></tr>
 *   <tr><td>REPONSE_TROP_RAPIDE</td><td>Seuil absolu (ms)</td><td>CRITIQUE</td></tr>
 *   <tr><td>REPONSE_ZSCORE_BAS</td><td>Z-score &lt; -2.5</td><td>MODERE</td></tr>
 *   <tr><td>REPONSE_ZSCORE_HAUT</td><td>Z-score &gt; +2.5</td><td>FAIBLE</td></tr>
 *   <tr><td>COPIER_COLLER</td><td>Signal depuis le contrôleur</td><td>MODERE</td></tr>
 *   <tr><td>SEQUENCE_CORRECTE_RAPIDE</td><td>Pattern de triche</td><td>CRITIQUE</td></tr>
 * </table>
 *
 * <p>Exemple d'utilisation :</p>
 * <pre>
 *   AnomalyService svc = new AnomalyService();
 *
 *   // Après chaque réponse
 *   List&lt;AnomalieEvent&gt; evts = svc.analyser(sessionId, utilisateurId, toutesLesReponses);
 *   evts.forEach(svc::enregistrer);
 *
 *   // Depuis le contrôleur JavaFX, si Ctrl+V est détecté
 *   svc.signalerCopierColler(sessionId, utilisateurId, questionId);
 * </pre>
 */
public class AnomalyService {

    private static final Logger log = LoggerFactory.getLogger(AnomalyService.class);

    private final int    minResponseMs;
    private final double zScoreThreshold;

    /** Nombre de réponses minimales pour activer la détection par Z-score. */
    private static final int ZSCORE_MIN_SAMPLES = 5;

    /** Nombre de réponses correctes consécutives rapides considérées suspectes. */
    private static final int CORRECT_RAPIDE_SEUIL = 4;

    /** Temps de réponse "rapide" pour le pattern de triche (ms). */
    private static final int CORRECT_RAPIDE_MS = 2000;

    public AnomalyService() {
        this.minResponseMs    = ConfigLoader.anomalyMinResponseMs();
        this.zScoreThreshold  = ConfigLoader.anomalyZscoreThreshold();
    }

    // ════════════════════════════════════════════════════════════════
    // Analyse principale
    // ════════════════════════════════════════════════════════════════

    /**
     * Analyse la dernière réponse et retourne les anomalies détectées.
     * Appeler après chaque réponse ajoutée à la liste.
     *
     * @param sessionId      ID de la session
     * @param utilisateurId  ID de l'utilisateur
     * @param toutesReponses Liste complète des réponses (ordre chronologique)
     * @return liste des anomalies détectées (vide si tout est normal)
     */
    public List<AnomalieEvent> analyser(int sessionId, int utilisateurId,
                                         List<ReponseUtilisateur> toutesReponses) {
        List<AnomalieEvent> anomalies = new ArrayList<>();
        if (toutesReponses == null || toutesReponses.isEmpty()) return anomalies;

        ReponseUtilisateur derniere = toutesReponses.get(toutesReponses.size() - 1);

        // ── Seuil absolu : réponse trop rapide ──────────────────────
        if (derniere.getTempsReponseMs() < minResponseMs) {
            anomalies.add(new AnomalieEvent(
                    sessionId, utilisateurId,
                    "REPONSE_TROP_RAPIDE",
                    "CRITIQUE",
                    derniere.getTempsReponseMs(),
                    minResponseMs,
                    String.format("Réponse en %d ms (seuil minimum : %d ms)",
                            derniere.getTempsReponseMs(), minResponseMs)
            ));
        }

        // ── Z-score sur les temps de réponse ────────────────────────
        if (toutesReponses.size() >= ZSCORE_MIN_SAMPLES) {
            anomalies.addAll(detecterZScore(sessionId, utilisateurId,
                    toutesReponses, derniere));
        }

        // ── Pattern : série de bonnes réponses très rapides ─────────
        AnomalieEvent patternEvent = detecterSequenceRapide(sessionId, utilisateurId, toutesReponses);
        if (patternEvent != null) anomalies.add(patternEvent);

        if (!anomalies.isEmpty()) {
            log.warn("Session {} — {} anomalie(s) détectée(s) sur la question {}",
                    sessionId, anomalies.size(), derniere.getOrdreSession());
        }

        return anomalies;
    }

    /**
     * Signal externe depuis le contrôleur JavaFX quand un Ctrl+V est détecté.
     * Créé l'événement et le persiste immédiatement.
     */
    public void signalerCopierColler(int sessionId, int utilisateurId, int numeroQuestion) {
        AnomalieEvent evt = new AnomalieEvent(
                sessionId, utilisateurId,
                "COPIER_COLLER",
                "MODERE",
                0.0, 0.0,
                "Événement coller (Ctrl+V) détecté à la question " + numeroQuestion
        );
        enregistrer(evt);
        log.warn("Copier-coller détecté — session {}, question {}", sessionId, numeroQuestion);
    }

    // ════════════════════════════════════════════════════════════════
    // Persistance
    // ════════════════════════════════════════════════════════════════

    /**
     * Persiste un événement d'anomalie dans la table {@code anomalie_event}.
     */
    public void enregistrer(AnomalieEvent evt) {
        String sql = """
            INSERT INTO anomalie_event
              (session_id, utilisateur_id, type_anomalie, niveau_gravite,
               valeur_observee, valeur_seuil, description)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, evt.getSessionId());
            ps.setInt(2, evt.getUtilisateurId());
            ps.setString(3, evt.getTypeAnomalie());
            ps.setString(4, evt.getNiveauGravite());
            ps.setDouble(5, evt.getValeurObservee());
            ps.setDouble(6, evt.getValeurSeuil());
            ps.setString(7, evt.getDescription());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) evt.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            log.error("Erreur enregistrement anomalie : {}", e.getMessage());
        }
    }

    /**
     * Récupère toutes les anomalies d'une session, triées par timestamp.
     */
    public List<AnomalieEvent> getParSession(int sessionId) {
        List<AnomalieEvent> liste = new ArrayList<>();
        String sql = "SELECT * FROM anomalie_event WHERE session_id = ? ORDER BY timestamp";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapper(rs));
            }
        } catch (SQLException e) {
            log.error("Erreur getParSession anomalie : {}", e.getMessage());
        }
        return liste;
    }

    /**
     * Retourne {@code true} si la session contient au moins une anomalie CRITIQUE.
     */
    public boolean contientAnomaliesCritiques(int sessionId) {
        return getParSession(sessionId).stream().anyMatch(AnomalieEvent::isCritique);
    }

    /**
     * Calcule un score de suspicion global pour une session (0-100).
     * Pondéré par la gravité des anomalies.
     */
    public double scoreSuspicion(int sessionId) {
        List<AnomalieEvent> evts = getParSession(sessionId);
        if (evts.isEmpty()) return 0.0;

        double total = evts.stream().mapToDouble(e -> switch (e.getNiveauGravite()) {
            case "CRITIQUE" -> 30.0;
            case "MODERE"   -> 15.0;
            default          -> 5.0;
        }).sum();

        return Math.min(100.0, total);
    }

    // ════════════════════════════════════════════════════════════════
    // Algorithmes de détection internes
    // ════════════════════════════════════════════════════════════════

    private List<AnomalieEvent> detecterZScore(int sessionId, int utilisateurId,
                                                List<ReponseUtilisateur> toutes,
                                                ReponseUtilisateur derniere) {
        List<AnomalieEvent> anomalies = new ArrayList<>();

        DescriptiveStatistics stats = new DescriptiveStatistics();
        // Exclure la dernière réponse du calcul de la baseline
        toutes.subList(0, toutes.size() - 1)
              .forEach(r -> stats.addValue(r.getTempsReponseMs()));

        double ecartType = stats.getStandardDeviation();
        if (ecartType < 1.0) return anomalies; // distribution plate → pas de Z-score fiable

        double zScore = (derniere.getTempsReponseMs() - stats.getMean()) / ecartType;

        if (zScore < -zScoreThreshold && derniere.getTempsReponseMs() >= minResponseMs) {
            // Réponse significativement plus rapide que d'habitude (mais > seuil absolu)
            anomalies.add(new AnomalieEvent(
                    sessionId, utilisateurId,
                    "REPONSE_ZSCORE_BAS",
                    "MODERE",
                    zScore,
                    -zScoreThreshold,
                    String.format("Z-score = %.2f (temps %d ms vs μ=%.0f ms)",
                            zScore, derniere.getTempsReponseMs(), stats.getMean())
            ));
        } else if (zScore > zScoreThreshold * 1.5) {
            // Réponse inhabituellement lente (distraction ?)
            anomalies.add(new AnomalieEvent(
                    sessionId, utilisateurId,
                    "REPONSE_ZSCORE_HAUT",
                    "FAIBLE",
                    zScore,
                    zScoreThreshold * 1.5,
                    String.format("Z-score = %.2f (temps %d ms vs μ=%.0f ms)",
                            zScore, derniere.getTempsReponseMs(), stats.getMean())
            ));
        }

        return anomalies;
    }

    /**
     * Détecte une séquence de N bonnes réponses toutes données très rapidement.
     * Ce pattern indique une possible consultation de réponses préparées.
     */
    private AnomalieEvent detecterSequenceRapide(int sessionId, int utilisateurId,
                                                   List<ReponseUtilisateur> toutes) {
        if (toutes.size() < CORRECT_RAPIDE_SEUIL) return null;

        List<ReponseUtilisateur> dernieres = toutes.subList(
                toutes.size() - CORRECT_RAPIDE_SEUIL, toutes.size());

        boolean toutesRapidesEtCorrectes = dernieres.stream().allMatch(
                r -> r.isEstCorrecte() && r.getTempsReponseMs() < CORRECT_RAPIDE_MS);

        if (toutesRapidesEtCorrectes) {
            return new AnomalieEvent(
                    sessionId, utilisateurId,
                    "SEQUENCE_CORRECTE_RAPIDE",
                    "CRITIQUE",
                    CORRECT_RAPIDE_SEUIL,
                    CORRECT_RAPIDE_SEUIL,
                    String.format("%d bonnes réponses consécutives en moins de %d ms chacune",
                            CORRECT_RAPIDE_SEUIL, CORRECT_RAPIDE_MS)
            );
        }
        return null;
    }

    // ════════════════════════════════════════════════════════════════
    // Mapper ResultSet
    // ════════════════════════════════════════════════════════════════

    private AnomalieEvent mapper(ResultSet rs) throws SQLException {
        return new AnomalieEvent(
                rs.getInt("id"),
                rs.getInt("session_id"),
                rs.getInt("utilisateur_id"),
                rs.getString("type_anomalie"),
                rs.getString("niveau_gravite"),
                rs.getDouble("valeur_observee"),
                rs.getDouble("valeur_seuil"),
                rs.getString("description"),
                rs.getTimestamp("timestamp").toLocalDateTime()
        );
    }
}
