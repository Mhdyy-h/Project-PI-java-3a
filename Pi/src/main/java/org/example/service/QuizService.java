package org.example.service;

import org.example.DatabaseConnection;
import org.example.model.Quiz;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service CRUD pour l'entité Quiz.
 * Correspond à la table quiz_mental avec colonnes :
 * id, titre, niveau_stress_cible, score_resultat, medaille_quiz,
 * date_quiz, utilisateur_id, statut, temps_moyen_reponse, agilite_cognitive
 */
public class QuizService {

    private Connection getConn() {
        return DatabaseConnection.getConnection();
    }

    // ── CREATE ─────────────────────────────────────────────────

    public boolean ajouterQuiz(Quiz quiz) {
        String sql = "INSERT INTO quiz_mental (titre, niveau_stress_cible, score_resultat, "
                   + "medaille_quiz, date_quiz, utilisateur_id, statut, temps_moyen_reponse, agilite_cognitive) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, quiz.getTitre());
            ps.setInt(2, quiz.getNiveauStressCible());
            ps.setInt(3, quiz.getScoreResultat());
            ps.setString(4, quiz.getMedailleQuiz());
            if (quiz.getDateQuiz() != null) {
                ps.setTimestamp(5, quiz.getDateQuiz());
            } else {
                ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            }
            ps.setInt(6, quiz.getUtilisateurId());
            ps.setString(7, quiz.getStatut());
            if (quiz.getTempsMoyenReponse() != null) {
                ps.setDouble(8, quiz.getTempsMoyenReponse());
            } else {
                ps.setNull(8, java.sql.Types.DOUBLE);
            }
            ps.setString(9, quiz.getAgiliteCognitive());

            if (ps.executeUpdate() > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) quiz.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[QuizService] ajouterQuiz : " + e.getMessage());
        }
        return false;
    }

    // ── READ ALL ───────────────────────────────────────────────

    public List<Quiz> getAllQuiz() {
        List<Quiz> list = new ArrayList<>();
        String sql = "SELECT * FROM quiz_mental ORDER BY id DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("[QuizService] getAllQuiz : " + e.getMessage());
        }
        return list;
    }

    // ── READ ACTIFS (statut = 'disponible') ────────────────────

    public List<Quiz> getQuizActifs() {
        List<Quiz> list = new ArrayList<>();
        String sql = "SELECT * FROM quiz_mental WHERE statut = 'disponible' ORDER BY id DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("[QuizService] getQuizActifs : " + e.getMessage());
        }
        return list;
    }

    // ── READ BY ID ─────────────────────────────────────────────

    public Quiz getQuizById(int id) {
        String sql = "SELECT * FROM quiz_mental WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            System.err.println("[QuizService] getQuizById : " + e.getMessage());
        }
        return null;
    }

    // ── UPDATE ─────────────────────────────────────────────────

    public boolean modifierQuiz(Quiz quiz) {
        String sql = "UPDATE quiz_mental SET titre=?, niveau_stress_cible=?, score_resultat=?, "
                   + "medaille_quiz=?, date_quiz=?, utilisateur_id=?, statut=?, "
                   + "temps_moyen_reponse=?, agilite_cognitive=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, quiz.getTitre());
            ps.setInt(2, quiz.getNiveauStressCible());
            ps.setInt(3, quiz.getScoreResultat());
            ps.setString(4, quiz.getMedailleQuiz());
            ps.setTimestamp(5, quiz.getDateQuiz());
            ps.setInt(6, quiz.getUtilisateurId());
            ps.setString(7, quiz.getStatut());
            if (quiz.getTempsMoyenReponse() != null) {
                ps.setDouble(8, quiz.getTempsMoyenReponse());
            } else {
                ps.setNull(8, java.sql.Types.DOUBLE);
            }
            ps.setString(9, quiz.getAgiliteCognitive());
            ps.setInt(10, quiz.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[QuizService] modifierQuiz : " + e.getMessage());
        }
        return false;
    }

    // ── DELETE ─────────────────────────────────────────────────

    public boolean supprimerQuiz(int id) {
        String sql = "DELETE FROM quiz_mental WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[QuizService] supprimerQuiz : " + e.getMessage());
        }
        return false;
    }

    // ── SEARCH ─────────────────────────────────────────────────

    public List<Quiz> rechercherQuiz(String keyword) {
        List<Quiz> list = new ArrayList<>();
        String sql = "SELECT * FROM quiz_mental WHERE titre LIKE ? OR statut LIKE ? ORDER BY id DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            String k = "%" + keyword + "%";
            ps.setString(1, k);
            ps.setString(2, k);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("[QuizService] rechercherQuiz : " + e.getMessage());
        }
        return list;
    }

    // ── MAPPING ────────────────────────────────────────────────

    private Quiz map(ResultSet rs) throws SQLException {
        Quiz q = new Quiz();
        q.setId(rs.getInt("id"));
        q.setTitre(rs.getString("titre"));
        q.setNiveauStressCible(rs.getInt("niveau_stress_cible"));
        q.setScoreResultat(rs.getInt("score_resultat"));
        q.setMedailleQuiz(rs.getString("medaille_quiz"));
        q.setDateQuiz(rs.getTimestamp("date_quiz"));
        q.setUtilisateurId(rs.getInt("utilisateur_id"));
        q.setStatut(rs.getString("statut"));
        q.setTempsMoyenReponse(rs.getDouble("temps_moyen_reponse"));
        if (rs.wasNull()) q.setTempsMoyenReponse(null);
        q.setAgiliteCognitive(rs.getString("agilite_cognitive"));
        return q;
    }
}
