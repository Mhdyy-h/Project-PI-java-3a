package org.example.dao;

import org.example.DatabaseConnection;
import org.example.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public static boolean testConnection() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            if (connection != null && !connection.isClosed()) {
                System.out.println("Database connection is successful!");
                System.out.println("Database: " + connection.getMetaData().getDatabaseProductName());
                System.out.println("Version: " + connection.getMetaData().getDatabaseProductVersion());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
        return false;
    }

    // Create users table if not exists (with photo_profil column)
    public static void createTableIfNotExists() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            Statement statement = connection.createStatement();

            String sql = "CREATE TABLE IF NOT EXISTS utilisateur (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY," +
                         "nom_complet VARCHAR(50) NOT NULL," +
                         "email VARCHAR(100) NOT NULL," +
                         "mot_de_passe VARCHAR(100) NOT NULL," +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                         "roles VARCHAR(100)," +
                         "score_global INT DEFAULT 0," +
                         "date_inscription DATE," +
                         "photo_profil VARCHAR(500)" +
                         ")";

            statement.execute(sql);
            statement.close();

            // Add photo_profil column if it doesn't exist yet (for existing DBs)
            try {
                Statement alterStmt = connection.createStatement();
                alterStmt.execute("ALTER TABLE utilisateur ADD COLUMN IF NOT EXISTS photo_profil VARCHAR(500)");
                alterStmt.close();
            } catch (SQLException ignored) {
                // MySQL < 8: column might already exist, ignore
            }

        } catch (SQLException e) {
            System.err.println("Error creating table: " + e.getMessage());
        }
    }

    // Insert a new user
    public static boolean insertUser(User user) {
        try {
            createTableIfNotExists();
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO utilisateur (nom_complet, email, mot_de_passe, score_global, date_inscription, roles, photo_profil) VALUES (?, ?, ?, 0, CURDATE(), ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );

            statement.setString(1, user.getNomComplet());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getMotDePasse());
            statement.setString(4, user.getRoles() != null ? user.getRoles() : "[\"ROLE_USER\"]");
            statement.setString(5, user.getPhotoProfil());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
                generatedKeys.close();
            }
            statement.close();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error inserting user: " + e.getMessage());
            return false;
        }
    }

    // Get all users
    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try {
            Connection connection = DatabaseConnection.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM utilisateur ORDER BY id DESC");

            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }

            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            System.err.println("Error getting users: " + e.getMessage());
        }
        return users;
    }

    // Delete user by ID (also deletes related records first)
    public static boolean deleteUser(int id) {
        try {
            Connection connection = DatabaseConnection.getConnection();

            // First, delete related activity logs
            try {
                PreparedStatement deleteLogs = connection.prepareStatement("DELETE FROM activity_log WHERE user_id = ?");
                deleteLogs.setInt(1, id);
                deleteLogs.executeUpdate();
                deleteLogs.close();
            } catch (SQLException e) {
                // Table might not exist, continue
            }

            // Delete related certification requests
            try {
                PreparedStatement deleteCertifications = connection.prepareStatement("DELETE FROM certification WHERE utilisateur_id = ?");
                deleteCertifications.setInt(1, id);
                deleteCertifications.executeUpdate();
                deleteCertifications.close();
            } catch (SQLException e) {
                // Table might not exist, continue
            }

            // Delete related log_event records
            try {
                PreparedStatement deleteLogEvents = connection.prepareStatement("DELETE FROM log_event WHERE utilisateur_id = ?");
                deleteLogEvents.setInt(1, id);
                deleteLogEvents.executeUpdate();
                deleteLogEvents.close();
            } catch (SQLException e) {
                // Table might not exist, continue
            }

            // Then delete the user
            PreparedStatement statement = connection.prepareStatement("DELETE FROM utilisateur WHERE id = ?");
            statement.setInt(1, id);
            int rows = statement.executeUpdate();
            statement.close();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Get user by ID
    public static User getUserById(int id) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM utilisateur WHERE id = ?");
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                User user = mapUser(resultSet);
                resultSet.close();
                statement.close();
                return user;
            }

            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            System.err.println("Error getting user: " + e.getMessage());
        }
        return null;
    }

    // Login
    public static User login(String email, String motDePasse) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM utilisateur WHERE email = ? AND mot_de_passe = ?"
            );
            statement.setString(1, email);
            statement.setString(2, motDePasse);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                User user = mapUser(resultSet);
                resultSet.close();
                statement.close();
                return user;
            }

            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            System.err.println("Error during login: " + e.getMessage());
        }
        return null;
    }

    // Update user (with or without password)
    public static boolean updateUser(User user, boolean skipPassword) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement;
            if (skipPassword) {
                statement = connection.prepareStatement(
                    "UPDATE utilisateur SET nom_complet = ?, email = ?, roles = ? WHERE id = ?"
                );
                statement.setString(1, user.getNomComplet());
                statement.setString(2, user.getEmail());
                statement.setString(3, user.getRoles());
                statement.setInt(4, user.getId());
            } else {
                statement = connection.prepareStatement(
                    "UPDATE utilisateur SET nom_complet = ?, email = ?, mot_de_passe = ?, roles = ? WHERE id = ?"
                );
                statement.setString(1, user.getNomComplet());
                statement.setString(2, user.getEmail());
                statement.setString(3, user.getMotDePasse());
                statement.setString(4, user.getRoles());
                statement.setInt(5, user.getId());
            }
            int rows = statement.executeUpdate();
            statement.close();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }

    // Update only the photo_profil for a user
    public static boolean updateUserPhoto(int userId, String photoPath) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                "UPDATE utilisateur SET photo_profil = ? WHERE id = ?"
            );
            statement.setString(1, photoPath);
            statement.setInt(2, userId);
            int rows = statement.executeUpdate();
            statement.close();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user photo: " + e.getMessage());
            return false;
        }
    }

    // Find user by email (for Face ID lookup)
    public static User getUserByEmail(String email) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM utilisateur WHERE email = ?"
            );
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                User user = mapUser(resultSet);
                resultSet.close();
                statement.close();
                return user;
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            System.err.println("Error getting user by email: " + e.getMessage());
        }
        return null;
    }

    // Update password by email (for password reset)
    public static boolean updatePasswordByEmail(String email, String newPassword) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                "UPDATE utilisateur SET mot_de_passe = ? WHERE email = ?"
            );
            statement.setString(1, newPassword);
            statement.setString(2, email);
            int rows = statement.executeUpdate();
            statement.close();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating password by email: " + e.getMessage());
            return false;
        }
    }

    // ==================== HELPER ====================
    private static User mapUser(ResultSet rs) throws SQLException {
        int scoreGlobal = 0;
        String dateInscription = null;
        String photoProfil = null;

        try { scoreGlobal = rs.getInt("score_global"); } catch (SQLException ignored) {}
        try {
            java.sql.Date d = rs.getDate("date_inscription");
            if (d != null) {
                dateInscription = String.format("%02d/%02d/%04d", d.getDate(), d.getMonth() + 1, d.getYear() + 1900);
            }
        } catch (SQLException ignored) {}
        try { photoProfil = rs.getString("photo_profil"); } catch (SQLException ignored) {}

        User user = new User(
            rs.getInt("id"),
            rs.getString("nom_complet"),
            rs.getString("email"),
            rs.getString("mot_de_passe"),
            rs.getString("roles"),
            scoreGlobal,
            dateInscription
        );
        user.setPhotoProfil(photoProfil);
        return user;
    }
}