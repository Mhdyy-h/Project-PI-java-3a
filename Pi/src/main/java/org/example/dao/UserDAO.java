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
            return false;
        }
    }

    public static User login(String email, String motDePasse) {
        String sql = "SELECT * FROM utilisateur WHERE email = ? AND mot_de_passe = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, motDePasse);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ UserDAO.login: " + e.getMessage());
        }
        return null;
    }

    public static boolean insertUser(User user) {
        String sql = "INSERT INTO utilisateur (nom_complet, email, mot_de_passe, roles, score_global, date_inscription) VALUES (?, ?, ?, ?, 0, CURDATE())";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getNomComplet());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getMotDePasse());
            ps.setString(4, user.getRoles() != null ? user.getRoles() : "UTILISATEUR");
            if (ps.executeUpdate() > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) user.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ UserDAO.insertUser: " + e.getMessage());
        }
        return false;
    }

    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur ORDER BY id DESC";
        try (Connection connection = DatabaseConnection.getConnection();
             Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) users.add(mapResultSetToUser(rs));
        } catch (SQLException e) {
            System.err.println("❌ UserDAO.getAllUsers: " + e.getMessage());
        }
        return users;
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
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteUser(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM utilisateur WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public static void updateScore(int userId, int points) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE utilisateur SET score_global = score_global + ? WHERE id = ?")) {
            ps.setInt(1, points);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private static User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("nom_complet"),
                rs.getString("email"),
                rs.getString("mot_de_passe"),
                rs.getString("roles"),
                rs.getInt("score_global"),
                rs.getString("date_inscription")
        );
    }
}