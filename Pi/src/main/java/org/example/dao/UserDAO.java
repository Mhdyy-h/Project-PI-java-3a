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
    
    // Create users table if not exists
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
                         "roles VARCHAR(100)" +
                         ")";
            
            statement.execute(sql);
            System.out.println("Table 'utilisateur' created or already exists");
            statement.close();
            
        } catch (SQLException e) {
            System.err.println("Error creating table: " + e.getMessage());
        }
    }
    
    // Insert a new user into 'utilisateur' table
    public static boolean insertUser(User user) {
        try {
            createTableIfNotExists(); // Ensure table exists
            
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO utilisateur (nom_complet, email, mot_de_passe, score_global, date_inscription, roles) VALUES (?, ?, ?, 0, CURDATE(), ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            
            statement.setString(1, user.getNomComplet());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getMotDePasse());
            statement.setString(4, user.getRoles() != null ? user.getRoles() : "[\"ROLE_USER\"]");
            
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
                int scoreGlobal = 0;
                String dateInscription = null;
                try { scoreGlobal = resultSet.getInt("score_global"); } catch (SQLException ignored) {}
                try {
                    java.sql.Date d = resultSet.getDate("date_inscription");
                    if (d != null) {
                        dateInscription = String.format("%02d/%02d/%04d", d.getDate(), d.getMonth() + 1, d.getYear() + 1900);
                    }
                } catch (SQLException ignored) {}
                User user = new User(
                    resultSet.getInt("id"),
                    resultSet.getString("nom_complet"),
                    resultSet.getString("email"),
                    resultSet.getString("mot_de_passe"),
                    resultSet.getString("roles"),
                    scoreGlobal,
                    dateInscription
                );
                users.add(user);
            }
            
            resultSet.close();
            statement.close();
            
        } catch (SQLException e) {
            System.err.println("Error getting users: " + e.getMessage());
        }
        
        return users;
    }

    // Delete user by ID
    public static boolean deleteUser(int id) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM utilisateur WHERE id = ?"
            );
            statement.setInt(1, id);
            int rows = statement.executeUpdate();
            statement.close();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }
    
    // Get user by ID
    public static User getUserById(int id) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM utilisateur WHERE id = ?"
            );
            
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                User user = new User(
                    resultSet.getInt("id"),
                    resultSet.getString("nom_complet"),
                    resultSet.getString("email"),
                    resultSet.getString("mot_de_passe"),
                    resultSet.getString("roles")
                );
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
    
    // Login - authenticate user by email and password
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
                int scoreGlobal = 0;
                String dateInscription = null;
                try { scoreGlobal = resultSet.getInt("score_global"); } catch (SQLException ignored) {}
                try {
                    java.sql.Date d = resultSet.getDate("date_inscription");
                    if (d != null) {
                        dateInscription = String.format("%02d/%02d/%04d", d.getDate(), d.getMonth() + 1, d.getYear() + 1900);
                    }
                } catch (SQLException ignored) {}
                User user = new User(
                    resultSet.getInt("id"),
                    resultSet.getString("nom_complet"),
                    resultSet.getString("email"),
                    resultSet.getString("mot_de_passe"),
                    resultSet.getString("roles"),
                    scoreGlobal,
                    dateInscription
                );
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

    // Update an existing user (optionally skip password update)
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
    
    // Get all patients (users who are not specialists or admins) - FOR SPECIALISTS TO BOOK APPOINTMENTS
    public static List<User> getAllPatients() {
        List<User> patients = new ArrayList<>();
        
        try {
            Connection connection = DatabaseConnection.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(
                "SELECT * FROM utilisateur WHERE roles NOT LIKE '%SPECIALISTE%' AND roles NOT LIKE '%ADMIN%' ORDER BY nom_complet ASC"
            );
            
            while (resultSet.next()) {
                int scoreGlobal = 0;
                String dateInscription = null;
                try { scoreGlobal = resultSet.getInt("score_global"); } catch (SQLException ignored) {}
                try {
                    java.sql.Date sqlDate = resultSet.getDate("date_inscription");
                    if (sqlDate != null) {
                        dateInscription = sqlDate.toString();
                    }
                } catch (SQLException ignored) {}
                
                User user = new User(
                    resultSet.getInt("id"),
                    resultSet.getString("nom_complet"),
                    resultSet.getString("email"),
                    resultSet.getString("mot_de_passe"),
                    resultSet.getString("roles") != null ? resultSet.getString("roles") : "[\"ROLE_USER\"]",
                    scoreGlobal,
                    dateInscription
                );
                patients.add(user);
            }
            resultSet.close();
            statement.close();
            
        } catch (SQLException e) {
            System.err.println("Error getting patients: " + e.getMessage());
        }
        
        return patients;
    }
}