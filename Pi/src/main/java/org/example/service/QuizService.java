package org.example.service;

import org.example.DatabaseConnection;
import org.example.model.Quiz;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service CRUD pour l'entité Quiz.
 *
 * Table attendue :
 *   CREATE TABLE IF NOT EXISTS quiz_mental (
 *       id           INT AUTO_INCREMENT PRIMARY KEY,
 *       titre        VARCHAR(255) NOT NULL,
 *       description  TEXT,
 *       categorie    VARCHAR(100),
 *       difficulte   VARCHAR(50),
 *       passing_score INT DEFAULT 50,
 *       actif        BOOLEAN DEFAULT TRUE
 *   );
 */
public class QuizService {

    private Connection getConn() {
        return DatabaseConnection.getConnection();
    }

    // ── CREATE ─────────────────────────────────────────────────

    public boolean ajouterQuiz(Quiz quiz) {
        String sql = "INSERT INTO quiz_mental (titre, description, categorie, difficulte, passing_score, actif) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, quiz.getTitre());
            ps.setString(2, quiz.getDescription());
            ps.setString(3, quiz.getCategorie());
            ps.setString(4, quiz.getDifficulte());
            ps.setInt(5, quiz.getPassingScore());
            ps.setBoolean(6, quiz.isActif());

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

    // ── READ ACTIFS ────────────────────────────────────────────

    public List<Quiz> getQuizActifs() {
        List<Quiz> list = new ArrayList<>();
        String sql = "SELECT * FROM quiz_mental WHERE actif = TRUE ORDER BY id DESC";
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
        String sql = "UPDATE quiz_mental SET titre=?, description=?, categorie=?, difficulte=?, "
                   + "passing_score=?, actif=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, quiz.getTitre());
            ps.setString(2, quiz.getDescription());
            ps.setString(3, quiz.getCategorie());
            ps.setString(4, quiz.getDifficulte());
            ps.setInt(5, quiz.getPassingScore());
            ps.setBoolean(6, quiz.isActif());
            ps.setInt(7, quiz.getId());
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
        String sql = "SELECT * FROM quiz_mental WHERE titre LIKE ? OR categorie LIKE ? ORDER BY id DESC";
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
        q.setDescription(rs.getString("description"));
        q.setCategorie(rs.getString("categorie"));
        q.setDifficulte(rs.getString("difficulte"));
        q.setPassingScore(rs.getInt("passing_score"));
        q.setActif(rs.getBoolean("actif"));
        return q;
    }
}
