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
 *       id           INT AUTO_INCREMENT PRIMARY KEY,
 *       quiz_id      INT NOT NULL,
 *       contenu      TEXT NOT NULL,
 *       option_a     VARCHAR(255),
 *       option_b     VARCHAR(255),
 *       option_c     VARCHAR(255),
 *       option_d     VARCHAR(255),
 *       bonne_reponse VARCHAR(10) NOT NULL,   -- "A" | "B" | "C" | "D"
 *       points       INT DEFAULT 1,
 *       explication  TEXT,
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
                   + "(quiz_id, contenu, option_a, option_b, option_c, option_d, bonne_reponse, points, explication) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, question.getQuizId());
            ps.setString(2, question.getContenu());
            ps.setString(3, question.getOptionA());
            ps.setString(4, question.getOptionB());
            ps.setString(5, question.getOptionC());
            ps.setString(6, question.getOptionD());
            ps.setString(7, question.getBonneReponse());
            ps.setInt(8, question.getPoints());
            ps.setString(9, question.getExplication());

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
        String sql = "UPDATE question SET contenu=?, option_a=?, option_b=?, option_c=?, option_d=?, "
                   + "bonne_reponse=?, points=?, explication=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, question.getContenu());
            ps.setString(2, question.getOptionA());
            ps.setString(3, question.getOptionB());
            ps.setString(4, question.getOptionC());
            ps.setString(5, question.getOptionD());
            ps.setString(6, question.getBonneReponse());
            ps.setInt(7, question.getPoints());
            ps.setString(8, question.getExplication());
            ps.setInt(9, question.getId());
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
        String sql = "SELECT COALESCE(SUM(points), 0) FROM question WHERE quiz_id = ?";
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
        q.setContenu(rs.getString("contenu"));
        q.setOptionA(rs.getString("option_a"));
        q.setOptionB(rs.getString("option_b"));
        q.setOptionC(rs.getString("option_c"));
        q.setOptionD(rs.getString("option_d"));
        q.setBonneReponse(rs.getString("bonne_reponse"));
        q.setPoints(rs.getInt("points"));
        q.setExplication(rs.getString("explication"));
        return q;
    }
}
