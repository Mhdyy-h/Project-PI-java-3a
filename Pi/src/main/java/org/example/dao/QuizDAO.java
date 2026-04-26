package org.example.dao;

import org.example.DatabaseConnection;
import org.example.model.Quiz;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) pour l'entité Quiz.
 * Correspond a la table quiz_mental avec colonnes :
 * id, titre, niveau_stress_cible, score_resultat, medaille_quiz,
 * date_quiz, utilisateur_id, statut, temps_moyen_reponse, agilite_cognitive
 */
public class QuizDAO {

    // Create quiz_mental table if not exists
    public static void createTableIfNotExists() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            Statement statement = connection.createStatement();

            String sql = "CREATE TABLE IF NOT EXISTS quiz_mental (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY," +
                         "titre VARCHAR(255) NOT NULL," +
                         "niveau_stress_cible INT DEFAULT 0," +
                         "score_resultat INT DEFAULT 0," +
                         "medaille_quiz VARCHAR(50)," +
                         "date_quiz TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                         "utilisateur_id INT NOT NULL," +
                         "statut VARCHAR(50) DEFAULT 'disponible'," +
                         "temps_moyen_reponse DOUBLE," +
                         "agilite_cognitive TEXT" +
                         ")";

            statement.execute(sql);
            System.out.println("Table 'quiz_mental' created or already exists");
            statement.close();

        } catch (SQLException e) {
            System.err.println("Error creating quiz_mental table: " + e.getMessage());
        }
    }

    // Insert a new quiz
    public static boolean insertQuiz(Quiz quiz) {
        try {
            createTableIfNotExists();

            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO quiz_mental (titre, niveau_stress_cible, score_resultat, medaille_quiz, " +
                "date_quiz, utilisateur_id, statut, temps_moyen_reponse, agilite_cognitive) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );

            statement.setString(1, quiz.getTitre());
            statement.setInt(2, quiz.getNiveauStressCible());
            statement.setInt(3, quiz.getScoreResultat());
            statement.setString(4, quiz.getMedailleQuiz());
            if (quiz.getDateQuiz() != null) {
                statement.setTimestamp(5, quiz.getDateQuiz());
            } else {
                statement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            }
            statement.setInt(6, quiz.getUtilisateurId());
            statement.setString(7, quiz.getStatut() != null ? quiz.getStatut() : "disponible");
            if (quiz.getTempsMoyenReponse() != null) {
                statement.setDouble(8, quiz.getTempsMoyenReponse());
            } else {
                statement.setNull(8, java.sql.Types.DOUBLE);
            }
            statement.setString(9, quiz.getAgiliteCognitive());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    quiz.setId(generatedKeys.getInt(1));
                }
                generatedKeys.close();
            }
            statement.close();

            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error inserting quiz: " + e.getMessage());
            return false;
        }
    }

    // Get all quizzes
    public static List<Quiz> getAllQuizzes() {
        List<Quiz> quizzes = new ArrayList<>();

        try {
            Connection connection = DatabaseConnection.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM quiz_mental ORDER BY id DESC");

            while (resultSet.next()) {
                quizzes.add(mapResultSetToQuiz(resultSet));
            }

            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            System.err.println("Error getting all quizzes: " + e.getMessage());
        }

        return quizzes;
    }

    // Get active quizzes (statut = 'disponible')
    public static List<Quiz> getActiveQuizzes() {
        List<Quiz> quizzes = new ArrayList<>();

        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM quiz_mental WHERE statut = 'disponible' ORDER BY id DESC"
            );
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                quizzes.add(mapResultSetToQuiz(resultSet));
            }

            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            System.err.println("Error getting active quizzes: " + e.getMessage());
        }

        return quizzes;
    }

    // Get quiz by ID
    public static Quiz getQuizById(int id) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM quiz_mental WHERE id = ?"
            );
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Quiz quiz = mapResultSetToQuiz(resultSet);
                resultSet.close();
                statement.close();
                return quiz;
            }

            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            System.err.println("Error getting quiz by id: " + e.getMessage());
        }

        return null;
    }

    // Update quiz
    public static boolean updateQuiz(Quiz quiz) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                "UPDATE quiz_mental SET titre=?, niveau_stress_cible=?, score_resultat=?, " +
                "medaille_quiz=?, date_quiz=?, utilisateur_id=?, statut=?, " +
                "temps_moyen_reponse=?, agilite_cognitive=? WHERE id=?"
            );

            statement.setString(1, quiz.getTitre());
            statement.setInt(2, quiz.getNiveauStressCible());
            statement.setInt(3, quiz.getScoreResultat());
            statement.setString(4, quiz.getMedailleQuiz());
            statement.setTimestamp(5, quiz.getDateQuiz());
            statement.setInt(6, quiz.getUtilisateurId());
            statement.setString(7, quiz.getStatut());
            if (quiz.getTempsMoyenReponse() != null) {
                statement.setDouble(8, quiz.getTempsMoyenReponse());
            } else {
                statement.setNull(8, java.sql.Types.DOUBLE);
            }
            statement.setString(9, quiz.getAgiliteCognitive());
            statement.setInt(10, quiz.getId());

            int rows = statement.executeUpdate();
            statement.close();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Error updating quiz: " + e.getMessage());
            return false;
        }
    }

    // Delete quiz
    public static boolean deleteQuiz(int id) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM quiz_mental WHERE id = ?"
            );
            statement.setInt(1, id);
            int rows = statement.executeUpdate();
            statement.close();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting quiz: " + e.getMessage());
            return false;
        }
    }

    // Search quizzes
    public static List<Quiz> searchQuizzes(String keyword) {
        List<Quiz> quizzes = new ArrayList<>();

        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM quiz_mental WHERE titre LIKE ? OR statut LIKE ? ORDER BY id DESC"
            );
            String k = "%" + keyword + "%";
            statement.setString(1, k);
            statement.setString(2, k);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                quizzes.add(mapResultSetToQuiz(resultSet));
            }

            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            System.err.println("Error searching quizzes: " + e.getMessage());
        }

        return quizzes;
    }

    // Mapping method
    private static Quiz mapResultSetToQuiz(ResultSet rs) throws SQLException {
        Quiz quiz = new Quiz();
        quiz.setId(rs.getInt("id"));
        quiz.setTitre(rs.getString("titre"));
        quiz.setNiveauStressCible(rs.getInt("niveau_stress_cible"));
        quiz.setScoreResultat(rs.getInt("score_resultat"));
        quiz.setMedailleQuiz(rs.getString("medaille_quiz"));
        quiz.setDateQuiz(rs.getTimestamp("date_quiz"));
        quiz.setUtilisateurId(rs.getInt("utilisateur_id"));
        quiz.setStatut(rs.getString("statut"));
        double temps = rs.getDouble("temps_moyen_reponse");
        quiz.setTempsMoyenReponse(rs.wasNull() ? null : temps);
        quiz.setAgiliteCognitive(rs.getString("agilite_cognitive"));
        return quiz;
    }
}
