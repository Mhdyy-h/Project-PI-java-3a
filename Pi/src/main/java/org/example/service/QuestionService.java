package org.example.service;

import org.example.DatabaseConnection;
import org.example.model.Question;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service CRUD pour l'entité Question.
 *
 * Table attendue :
 *   CREATE TABLE IF NOT EXISTS question (
 *       id               INT AUTO_INCREMENT PRIMARY KEY,
 *       quiz_id          INT NOT NULL,
 *       enonce           TEXT NOT NULL,
 *       reponse_correcte VARCHAR(255) NOT NULL,
 *       options_fausses  TEXT,
 *       points_valeur    INT DEFAULT 50,
 *       FOREIGN KEY (quiz_id) REFERENCES quiz_mental(id) ON DELETE CASCADE
 *   );
 */
public class QuestionService {

    private Connection getConn() {
        return DatabaseConnection.getConnection();
    }

    // ── CREATE ─────────────────────────────────────────────────

    public boolean ajouterQuestion(Question question) {
        String sql = "INSERT INTO question "
                   + "(quiz_id, enonce, reponse_correcte, options_fausses, points_valeur) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, question.getQuizId());
            ps.setString(2, question.getEnonce());
            ps.setString(3, question.getReponseCorrecte());
            ps.setString(4, question.getOptionsFausses());
            ps.setInt(5, question.getPointsValeur());

            if (ps.executeUpdate() > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) question.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[QuestionService] ajouterQuestion : " + e.getMessage());
        }
        return false;
    }

    // ── READ BY QUIZ ────────────────────────────────────────────

    public List<Question> getQuestionsByQuizId(int quizId) {
        List<Question> list = new ArrayList<>();
        String sql = "SELECT * FROM question WHERE quiz_id = ? ORDER BY id ASC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("[QuestionService] getQuestionsByQuizId : " + e.getMessage());
        }
        return list;
    }

    // ── READ ALL ────────────────────────────────────────────────

    public List<Question> getAllQuestions() {
        List<Question> list = new ArrayList<>();
        String sql = "SELECT * FROM question ORDER BY id ASC";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("[QuestionService] getAllQuestions : " + e.getMessage());
        }
        return list;
    }

    // ── UPDATE ─────────────────────────────────────────────────

    public boolean modifierQuestion(Question question) {
        String sql = "UPDATE question SET enonce=?, reponse_correcte=?, options_fausses=?, "
                   + "points_valeur=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, question.getEnonce());
            ps.setString(2, question.getReponseCorrecte());
            ps.setString(3, question.getOptionsFausses());
            ps.setInt(4, question.getPointsValeur());
            ps.setInt(5, question.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[QuestionService] modifierQuestion : " + e.getMessage());
        }
        return false;
    }

    // ── DELETE ─────────────────────────────────────────────────

    public boolean supprimerQuestion(int id) {
        String sql = "DELETE FROM question WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[QuestionService] supprimerQuestion : " + e.getMessage());
        }
        return false;
    }

    // ── COUNT BY QUIZ ───────────────────────────────────────────

    public int countQuestionsByQuiz(int quizId) {
        String sql = "SELECT COUNT(*) FROM question WHERE quiz_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[QuestionService] countQuestionsByQuiz : " + e.getMessage());
        }
        return 0;
    }

    // ── SUM POINTS BY QUIZ ──────────────────────────────────────

    public int sumPointsByQuiz(int quizId) {
        String sql = "SELECT COALESCE(SUM(points_valeur), 0) FROM question WHERE quiz_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[QuestionService] sumPointsByQuiz : " + e.getMessage());
        }
        return 0;
    }

    // ── MAPPING ─────────────────────────────────────────────────

    private Question map(ResultSet rs) throws SQLException {
        Question q = new Question();
        q.setId(rs.getInt("id"));
        q.setQuizId(rs.getInt("quiz_id"));
        q.setEnonce(rs.getString("enonce"));
        q.setReponseCorrecte(rs.getString("reponse_correcte"));
        q.setOptionsFausses(rs.getString("options_fausses"));
        q.setPointsValeur(rs.getInt("points_valeur"));
        return q;
    }
}
