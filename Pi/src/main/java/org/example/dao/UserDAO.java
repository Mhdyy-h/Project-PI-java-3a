package org.example.dao;

import org.example.DatabaseConnection;
import org.example.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public static boolean testConnection() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            System.err.println("❌ Connection test failed: " + e.getMessage());
            return false;
        }
    }

    public static void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS utilisateur (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "nom_complet VARCHAR(50) NOT NULL," +
                "email VARCHAR(100) NOT NULL UNIQUE," +
                "mot_de_passe VARCHAR(100) NOT NULL," +
                "roles VARCHAR(100) DEFAULT '[\"ROLE_USER\"]'," +
                "score_global INT DEFAULT 0," +
                "date_inscription DATE," +
                "photo_profil VARCHAR(500)," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            try {
                stmt.execute("ALTER TABLE utilisateur ADD COLUMN IF NOT EXISTS photo_profil VARCHAR(500)");
                stmt.execute("ALTER TABLE utilisateur ADD COLUMN IF NOT EXISTS score_global INT DEFAULT 0");
            } catch (SQLException ignored) { }
        } catch (SQLException e) {
            System.err.println("❌ Error creating table: " + e.getMessage());
        }
    }

    public static User login(String email, String motDePasse) {
        String sql = "SELECT * FROM utilisateur WHERE email = ? AND mot_de_passe = ?";
        System.out.println(" USERDAO DEBUG: Login attempt for email: " + email);
        System.out.println(" USERDAO DEBUG: Password length: " + (motDePasse != null ? motDePasse.length() : "null"));
        System.out.println(" USERDAO DEBUG: SQL: " + sql);
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, motDePasse);
            System.out.println(" USERDAO DEBUG: Prepared statement parameters set");
            
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println(" USERDAO DEBUG: Query executed, checking results...");
                if (rs.next()) {
                    System.out.println(" USERDAO DEBUG: User found in database!");
                    return mapUser(rs);
                } else {
                    System.out.println(" USERDAO DEBUG: No results found from query");
                }
            }
        } catch (SQLException e) {
            System.out.println(" USERDAO DEBUG: SQLException: " + e.getMessage());
        }
        return null;
    }

    public static boolean insertUser(User user) {
        createTableIfNotExists();
        String sql = "INSERT INTO utilisateur (nom_complet, email, mot_de_passe, roles, score_global, date_inscription, photo_profil) VALUES (?, ?, ?, ?, 0, CURDATE(), ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getNomComplet());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getMotDePasse());
            ps.setString(4, user.getRoles() != null ? user.getRoles() : "[\"ROLE_USER\"]");
            ps.setString(5, user.getPhotoProfil());
            if (ps.executeUpdate() > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) user.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) { }
        return false;
    }

    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur ORDER BY id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) users.add(mapUser(rs));
        } catch (SQLException e) { }
        return users;
    }

    // --- NEW / FIXED: This was missing and caused the AdminController error ---
    public static User getUserById(int id) {
        String sql = "SELECT * FROM utilisateur WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ GetUserById Error: " + e.getMessage());
        }
        return null;
    }

    public static boolean updateUser(User user, boolean skipPassword) {
        String sql = skipPassword ?
                "UPDATE utilisateur SET nom_complet=?, email=?, roles=? WHERE id=?" :
                "UPDATE utilisateur SET nom_complet=?, email=?, mot_de_passe=?, roles=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getNomComplet());
            ps.setString(2, user.getEmail());
            if (skipPassword) {
                ps.setString(3, user.getRoles());
                ps.setInt(4, user.getId());
            } else {
                ps.setString(3, user.getMotDePasse());
                ps.setString(4, user.getRoles());
                ps.setInt(5, user.getId());
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public static void updateScore(int userId, int points) {
        String sql = "UPDATE utilisateur SET score_global = score_global + ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, points);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) { }
    }

    public static List<User> getTopUsers(int limit) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur ORDER BY score_global DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) users.add(mapUser(rs));
            }
        } catch (SQLException e) { }
        return users;
    }

    public static boolean deleteUser(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String[] relatedTables = {"activity_log", "certification", "log_event", "membre_groupe"};
            for (String table : relatedTables) {
                String col = table.equals("activity_log") ? "user_id" : "utilisateur_id";
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM " + table + " WHERE " + col + " = ?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                } catch (SQLException ignored) {}
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM utilisateur WHERE id = ?")) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) { return false; }
    }

    public static User getUserByEmail(String email) {
        String sql = "SELECT * FROM utilisateur WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapUser(rs);
            }
        } catch (SQLException e) { }
        return null;
    }

    public static boolean updatePasswordByEmail(String email, String newPassword) {
        String sql = "UPDATE utilisateur SET mot_de_passe = ? WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public static boolean updateUserPhoto(int userId, String photoPath) {
        String sql = "UPDATE utilisateur SET photo_profil = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, photoPath);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public static List<User> getAccessiblePatients(User currentUser) {
        List<User> patients = new ArrayList<>();
        
        if (currentUser == null) {
            // If no current user, return all users with patient role
            String sql = "SELECT * FROM utilisateur WHERE roles LIKE ? ORDER BY nom_complet";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, "%ROLE_USER%");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) patients.add(mapUser(rs));
                }
            } catch (SQLException e) { }
            return patients;
        }
        
        if (currentUser.isPatient()) {
            // Patient can only see themselves
            patients.add(currentUser);
            return patients;
        }
        
        // Admin and specialists see all patients
        String sql = "SELECT * FROM utilisateur WHERE roles LIKE ? ORDER BY nom_complet";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%ROLE_USER%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) patients.add(mapUser(rs));
            }
        } catch (SQLException e) { }
        return patients;
    }

    private static User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setNomComplet(rs.getString("nom_complet"));
        user.setEmail(rs.getString("email"));
        user.setMotDePasse(rs.getString("mot_de_passe"));
        user.setRoles(rs.getString("roles"));
        user.setScoreGlobal(rs.getInt("score_global"));
        user.setPhotoProfil(rs.getString("photo_profil"));
        
        // Handle zero date values properly
        try {
            Date d = rs.getDate("date_inscription");
            if (d != null) {
                user.setDateInscription(String.format("%02d/%02d/%04d", d.getDate(), d.getMonth() + 1, d.getYear() + 1900));
            }
        } catch (SQLException e) {
            // Handle zero date or invalid date
            System.out.println(" USERDAO DEBUG: Handling invalid date_inscription, setting to null");
            user.setDateInscription(null);
        }
        
        return user;
    }}