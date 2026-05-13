package org.example.dao;

import org.example.DatabaseConnection;

import java.sql.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * DAO pour la gestion du rate limiting en base de données.
 * Persiste les tentatives de connexion entre les redémarrages de l'application.
 */
public class RateLimitingDAO {

    private static final String TABLE_NAME = "rate_limiting";

    /**
     * Crée la table rate_limiting si elle n'existe pas.
     */
    public static void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "email VARCHAR(255) PRIMARY KEY," +
                "attempt_count INT NOT NULL DEFAULT 0," +
                "last_attempt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                System.err.println("[RateLimitingDAO] ⚠️  Connexion DB nulle - table non créée");
                return;
            }
            
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
                System.out.println("[RateLimitingDAO] ✅ Table " + TABLE_NAME + " créée/vérifiée");
            }
        } catch (SQLException e) {
            System.err.println("[RateLimitingDAO] ❌ Erreur création table: " + e.getMessage());
        }
    }

    /**
     * Récupère les informations de rate limiting pour un email.
     * @param email L'email de l'utilisateur
     * @return Map avec "count" et "lastAttempt", ou null si pas trouvé
     */
    public static Map<String, Object> getAttempts(String email) {
        String sql = "SELECT attempt_count, last_attempt FROM " + TABLE_NAME + " WHERE email = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return null;
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email.toLowerCase().trim());
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("count", rs.getInt("attempt_count"));
                    Timestamp lastAttempt = rs.getTimestamp("last_attempt");
                    result.put("lastAttempt", lastAttempt != null ? lastAttempt.toInstant() : Instant.now());
                    return result;
                }
            }
        } catch (SQLException e) {
            System.err.println("[RateLimitingDAO] Erreur getAttempts: " + e.getMessage());
        }
        return null;
    }

    /**
     * Enregistre ou met à jour les tentatives pour un email.
     * @param email L'email de l'utilisateur
     * @param count Le nombre de tentatives
     * @param lastAttempt Le timestamp de la dernière tentative
     */
    public static void saveAttempts(String email, int count, Instant lastAttempt) {
        String sql = "INSERT INTO " + TABLE_NAME + " (email, attempt_count, last_attempt) " +
                "VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE attempt_count = ?, last_attempt = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return;
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                String normalizedEmail = email.toLowerCase().trim();
                Timestamp timestamp = Timestamp.from(lastAttempt);

                pstmt.setString(1, normalizedEmail);
                pstmt.setInt(2, count);
                pstmt.setTimestamp(3, timestamp);
                pstmt.setInt(4, count);
                pstmt.setTimestamp(5, timestamp);

                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[RateLimitingDAO] Erreur saveAttempts: " + e.getMessage());
        }
    }

    /**
     * Supprime les tentatives pour un email (déblocage).
     * @param email L'email de l'utilisateur
     * @return true si une ligne a été supprimée
     */
    public static boolean deleteAttempts(String email) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email.toLowerCase().trim());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("[RateLimitingDAO] Erreur deleteAttempts: " + e.getMessage());
        }
        return false;
    }

    /**
     * Récupère tous les utilisateurs avec leurs tentatives.
     * @return Map avec email -> Map de données (count, lastAttempt)
     */
    public static Map<String, Map<String, Object>> getAllAttempts() {
        Map<String, Map<String, Object>> result = new HashMap<>();
        String sql = "SELECT email, attempt_count, last_attempt FROM " + TABLE_NAME;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> data = new HashMap<>();
                data.put("count", rs.getInt("attempt_count"));
                Timestamp lastAttempt = rs.getTimestamp("last_attempt");
                data.put("lastAttempt", lastAttempt != null ? lastAttempt.toInstant() : Instant.now());
                result.put(rs.getString("email"), data);
            }
        } catch (SQLException e) {
            System.err.println("[RateLimitingDAO] Erreur getAllAttempts: " + e.getMessage());
        }
        return result;
    }

    /**
     * Supprime les entrées expirées (plus vieilles que X minutes).
     * @param minutesAge L'âge maximal en minutes
     * @return Le nombre de lignes supprimées
     */
    public static int cleanupOldEntries(int minutesAge) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE last_attempt < DATE_SUB(NOW(), INTERVAL ? MINUTE)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, minutesAge);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[RateLimitingDAO] Erreur cleanupOldEntries: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Supprime TOUTES les entrées de rate limiting.
     * @return true si l'opération a réussi
     */
    public static boolean clearAllAttempts() {
        String sql = "DELETE FROM " + TABLE_NAME;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int rowsAffected = pstmt.executeUpdate();
            System.out.println("[RateLimitingDAO] ✅ " + rowsAffected + " entrées de rate limiting supprimées");
            return true;
        } catch (SQLException e) {
            System.err.println("[RateLimitingDAO] ❌ Erreur clearAllAttempts: " + e.getMessage());
            return false;
        }
    }
}
