package org.example.dao;

import org.example.DatabaseConnection;
import org.example.model.Question;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) pour l'entite Question.
 * Correspond a la table question avec colonnes :
 * id, quiz_id, enonce, reponse_correcte, options_fausses, points_valeur
 */
public class QuestionDAO {

    // Create question table if not exists
    public static void createTableIfNotExists() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            Statement statement = connection.createStatement();

            String sql = "CREATE TABLE IF NOT EXISTS question (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY," +
                         "quiz_id INT NOT NULL," +
                         "enonce TEXT NOT NULL," +
                         "reponse_correcte VARCHAR(255) NOT NULL," +
                         "options_fausses TEXT," +
                         "points_valeur INT DEFAULT 50," +
                         "FOREIGN KEY (quiz_id) REFERENCES quiz_mental(id) ON DELETE CASCADE" +
                         ")";

            statement.execute(sql);
            System.out.println("Table 'question' created or already exists");
            statement.close();

        } catch (SQLException e) {
            System.err.println("Error creating question table: " + e.getMessage());
        }
    }

    // Insert a new question
    public static boolean insertQuestion(Question question) {
        try {
            createTableIfNotExists();

            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO question (quiz_id, enonce, reponse_correcte, options_fausses, points_valeur) " +
                "VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );

            statement.setInt(1, question.getQuizId());
            statement.setString(2, question.getEnonce());
            statement.setString(3, question.getReponseCorrecte());
            statement.setString(4, question.getOptionsFausses());
            statement.setInt(5, question.getPointsValeur());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    question.setId(generatedKeys.getInt(1));
                }
                generatedKeys.close();
            }
            statement.close();

            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error inserting question: " + e.getMessage());
            return false;
        }
    }

    // Get questions by quiz ID
    public static List<Question> getQuestionsByQuizId(int quizId) {
        List<Question> questions = new ArrayList<>();

        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM question WHERE quiz_id = ? ORDER BY id ASC"
            );
            statement.setInt(1, quizId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                questions.add(mapResultSetToQuestion(resultSet));
            }

            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            System.err.println("Error getting questions by quiz id: " + e.getMessage());
        }

        return questions;
    }

    // Get all questions
    public static List<Question> getAllQuestions() {
        List<Question> questions = new ArrayList<>();

        try {
            Connection connection = DatabaseConnection.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM question ORDER BY id ASC");

            while (resultSet.next()) {
                questions.add(mapResultSetToQuestion(resultSet));
            }

            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            System.err.println("Error getting all questions: " + e.getMessage());
        }

        return questions;
    }

    // Get question by ID
    public static Question getQuestionById(int id) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM question WHERE id = ?"
            );
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Question question = mapResultSetToQuestion(resultSet);
                resultSet.close();
                statement.close();
                return question;
            }

            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            System.err.println("Error getting question by id: " + e.getMessage());
        }

        return null;
    }

    // Update question
    public static boolean updateQuestion(Question question) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                "UPDATE question SET enonce=?, reponse_correcte=?, options_fausses=?, " +
                "points_valeur=? WHERE id=?"
            );

            statement.setString(1, question.getEnonce());
            statement.setString(2, question.getReponseCorrecte());
            statement.setString(3, question.getOptionsFausses());
            statement.setInt(4, question.getPointsValeur());
            statement.setInt(5, question.getId());

            int rows = statement.executeUpdate();
            statement.close();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Error updating question: " + e.getMessage());
            return false;
        }
    }

    // Delete question
    public static boolean deleteQuestion(int id) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM question WHERE id = ?"
            );
            statement.setInt(1, id);
            int rows = statement.executeUpdate();
            statement.close();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting question: " + e.getMessage());
            return false;
        }
    }

    // Count questions by quiz
    public static int countQuestionsByQuiz(int quizId) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM question WHERE quiz_id = ?"
            );
            statement.setInt(1, quizId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                resultSet.close();
                statement.close();
                return count;
            }

            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            System.err.println("Error counting questions: " + e.getMessage());
        }

        return 0;
    }

    // Sum points by quiz
    public static int sumPointsByQuiz(int quizId) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                "SELECT COALESCE(SUM(points_valeur), 0) FROM question WHERE quiz_id = ?"
            );
            statement.setInt(1, quizId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int sum = resultSet.getInt(1);
                resultSet.close();
                statement.close();
                return sum;
            }

            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            System.err.println("Error summing points: " + e.getMessage());
        }

        return 0;
    }

    // Delete all questions for a quiz
    public static boolean deleteQuestionsByQuiz(int quizId) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM question WHERE quiz_id = ?"
            );
            statement.setInt(1, quizId);
            int rows = statement.executeUpdate();
            statement.close();
            return rows >= 0;

        } catch (SQLException e) {
            System.err.println("Error deleting questions by quiz: " + e.getMessage());
            return false;
        }
    }

    // Mapping method
    private static Question mapResultSetToQuestion(ResultSet rs) throws SQLException {
        Question question = new Question();
        question.setId(rs.getInt("id"));
        question.setQuizId(rs.getInt("quiz_id"));
        question.setEnonce(rs.getString("enonce"));
        question.setReponseCorrecte(rs.getString("reponse_correcte"));
        question.setOptionsFausses(rs.getString("options_fausses"));
        question.setPointsValeur(rs.getInt("points_valeur"));
        return question;
    }
}
