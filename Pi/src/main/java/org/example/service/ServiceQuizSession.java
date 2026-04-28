package org.example.service;

import org.example.DatabaseConnection;
import org.example.model.QuizSession;
import org.example.model.ReponseUtilisateur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD pour les tables {@code quiz_session} et {@code reponse_utilisateur}.
 * Utilisé par {@code QuizViewController} et {@code ResultatsSessionController}.
 *
 * <p>Exemple d'utilisation :</p>
 * <pre>
 *   ServiceQuizSession svc = new ServiceQuizSession();
 *
 *   // Démarre une session
 *   QuizSession s = new QuizSession(userId, "Informatique", 5);
 *   svc.demarrer(s);
 *
 *   // Enregistre chaque réponse
 *   svc.enregistrerReponse(new ReponseUtilisateur(s.getId(), qId, "Paris", true, 2500, 1));
 *
 *   // Termine la session
 *   svc.terminer(s, scoreFinal, nouveauElo, scoreFatigue);
 * </pre>
 */
public class ServiceQuizSession {

    private static final Logger log = LoggerFactory.getLogger(ServiceQuizSession.class);

    // ════════════════════════════════════════════════════════════════
    // QuizSession
    // ════════════════════════════════════════════════════════════════

    /** Persiste une nouvelle session et lui assigne son ID. */
    public void demarrer(QuizSession s) {
        String sql = """
            INSERT INTO quiz_session
              (utilisateur_id, theme, niveau_difficulte, score_elo, nombre_questions, statut, date_debut)
            VALUES (?, ?, ?, ?, ?, 'EN_COURS', NOW())
            """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, s.getUtilisateurId());
            ps.setString(2, s.getTheme());
            ps.setInt(3, s.getNiveauDifficulte());
            ps.setInt(4, s.getScoreElo());
            ps.setInt(5, s.getNombreQuestions());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) s.setId(rs.getInt(1));
            }
            log.info("Session démarrée : id={}, user={}, thème={}",
                    s.getId(), s.getUtilisateurId(), s.getTheme());

        } catch (SQLException e) {
            log.error("Erreur demarrer session : {}", e.getMessage());
        }
    }

    /** Met à jour la session avec le score final, l'ELO et la fatigue, passe en TERMINEE. */
    public void terminer(QuizSession s, double scoreFinal, int scoreElo, double scoreFatigue) {
        s.terminer(scoreFinal, scoreElo);
        s.setScoreFatigue(scoreFatigue);

        String sql = """
            UPDATE quiz_session
            SET score_final=?, score_elo=?, score_fatigue=?, statut='TERMINEE',
                date_fin=NOW(), duree_secondes=TIMESTAMPDIFF(SECOND, date_debut, NOW()),
                nombre_questions=?
            WHERE id=?
            """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setDouble(1, scoreFinal);
            ps.setInt(2, scoreElo);
            ps.setDouble(3, scoreFatigue);
            ps.setInt(4, s.getNombreQuestions());
            ps.setInt(5, s.getId());
            ps.executeUpdate();
            log.info("Session terminée : id={}, score={}%", s.getId(), scoreFinal);

        } catch (SQLException e) {
            log.error("Erreur terminer session : {}", e.getMessage());
        }
    }

    /** Marque la session comme abandonnée. */
    public void abandonner(int sessionId) {
        String sql = "UPDATE quiz_session SET statut='ABANDONNEE', date_fin=NOW() WHERE id=?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Erreur abandonner session : {}", e.getMessage());
        }
    }

    public QuizSession getById(int id) {
        String sql = "SELECT * FROM quiz_session WHERE id=?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapperSession(rs);
            }
        } catch (SQLException e) {
            log.error("Erreur getById session : {}", e.getMessage());
        }
        return null;
    }

    /** Retourne les N dernières sessions terminées d'un utilisateur (ordre antéchronologique). */
    public List<QuizSession> getDernieres(int utilisateurId, int limit) {
        List<QuizSession> liste = new ArrayList<>();
        String sql = """
            SELECT * FROM quiz_session
            WHERE utilisateur_id=? AND statut='TERMINEE'
            ORDER BY date_debut DESC LIMIT ?
            """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, utilisateurId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapperSession(rs));
            }
        } catch (SQLException e) {
            log.error("Erreur getDernieres sessions : {}", e.getMessage());
        }
        return liste;
    }

    public List<QuizSession> getParUtilisateur(int utilisateurId) {
        List<QuizSession> liste = new ArrayList<>();
        String sql = "SELECT * FROM quiz_session WHERE utilisateur_id=? ORDER BY date_debut DESC";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, utilisateurId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapperSession(rs));
            }
        } catch (SQLException e) {
            log.error("Erreur getParUtilisateur sessions : {}", e.getMessage());
        }
        return liste;
    }

    // ════════════════════════════════════════════════════════════════
    // ReponseUtilisateur
    // ════════════════════════════════════════════════════════════════

    /** Persiste une réponse et lui assigne son ID. */
    public void enregistrerReponse(ReponseUtilisateur r) {
        String sql = """
            INSERT INTO reponse_utilisateur
              (session_id, question_id, reponse_donnee, est_correcte, temps_reponse_ms, ordre_session)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, r.getSessionId());
            ps.setInt(2, r.getQuestionId());
            ps.setString(3, r.getReponseDonnee());
            ps.setBoolean(4, r.isEstCorrecte());
            ps.setInt(5, r.getTempsReponseMs());
            ps.setInt(6, r.getOrdreSession());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) r.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            log.error("Erreur enregistrerReponse : {}", e.getMessage());
        }
    }

    /** Récupère toutes les réponses d'une session, triées par ordre. */
    public List<ReponseUtilisateur> getReponsesSession(int sessionId) {
        List<ReponseUtilisateur> liste = new ArrayList<>();
        String sql = "SELECT * FROM reponse_utilisateur WHERE session_id=? ORDER BY ordre_session";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapperReponse(rs));
            }
        } catch (SQLException e) {
            log.error("Erreur getReponsesSession : {}", e.getMessage());
        }
        return liste;
    }

    // ════════════════════════════════════════════════════════════════
    // Mappers
    // ════════════════════════════════════════════════════════════════

    private QuizSession mapperSession(ResultSet rs) throws SQLException {
        QuizSession s = new QuizSession();
        s.setId(rs.getInt("id"));
        s.setUtilisateurId(rs.getInt("utilisateur_id"));
        s.setTheme(rs.getString("theme"));
        s.setNiveauDifficulte(rs.getInt("niveau_difficulte"));
        s.setScoreFinal(rs.getDouble("score_final"));
        s.setScoreElo(rs.getInt("score_elo"));
        s.setScoreFatigue(rs.getDouble("score_fatigue"));
        s.setNombreQuestions(rs.getInt("nombre_questions"));
        s.setStatut(rs.getString("statut"));
        if (rs.getTimestamp("date_debut") != null)
            s.setDateDebut(rs.getTimestamp("date_debut").toLocalDateTime());
        if (rs.getTimestamp("date_fin") != null)
            s.setDateFin(rs.getTimestamp("date_fin").toLocalDateTime());
        s.setDureeSecondes(rs.getInt("duree_secondes"));
        return s;
    }

    private ReponseUtilisateur mapperReponse(ResultSet rs) throws SQLException {
        return new ReponseUtilisateur(
                rs.getInt("id"),
                rs.getInt("session_id"),
                rs.getInt("question_id"),
                rs.getString("reponse_donnee"),
                rs.getBoolean("est_correcte"),
                rs.getInt("temps_reponse_ms"),
                rs.getInt("ordre_session"),
                rs.getTimestamp("date_reponse").toLocalDateTime()
        );
    }
}
