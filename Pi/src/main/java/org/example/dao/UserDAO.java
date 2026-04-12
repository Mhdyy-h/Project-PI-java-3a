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
            ResultSet resultSet = statement.executeQuery("SELECT * FROM utilisateur");
            
            while (resultSet.next()) {
                User user = new User(
                    resultSet.getInt("id"),
                    resultSet.getString("nom_complet"),
                    resultSet.getString("email"),
                    resultSet.getString("mot_de_passe"),
                    resultSet.getString("roles")
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
            System.err.println("Error during login: " + e.getMessage());
        }
        
        return null;
    }
}