package org.example.dao;

import org.example.DatabaseConnection;
import java.sql.*;

public class MembreDAO {

    /**
     * Checks if a user is already a member of a group.
     */
    public static boolean isMember(int userId, int groupId) throws SQLException {
        // Matches your columns: utilisateur_id and groupe_id
        String sql = "SELECT 1 FROM membre_groupe WHERE utilisateur_id = ? AND groupe_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Adds a user to a group using your specific column names.
     */
    public static void join(int userId, int groupId) throws SQLException {
        // Matches your columns: utilisateur_id, groupe_id, date_adhesion, role_membre
        String sql = "INSERT INTO membre_groupe (utilisateur_id, groupe_id, date_adhesion, role_membre) VALUES (?, ?, CURDATE(), ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, groupId);
            ps.setString(3, "MEMBRE"); // Default role for new members
            ps.executeUpdate();
        }
    }

    /**
     * Removes a user from a group.
     */
    public static void leave(int userId, int groupId) throws SQLException {
        String sql = "DELETE FROM membre_groupe WHERE utilisateur_id = ? AND groupe_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, groupId);
            ps.executeUpdate();
        }
    }

    /**
     * Returns the current number of members in a group.
     */
    public static int getMemberCount(int groupId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM membre_groupe WHERE groupe_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }
}