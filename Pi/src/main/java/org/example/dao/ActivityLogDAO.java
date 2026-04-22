package org.example.dao;

import org.example.DatabaseConnection;
import org.example.model.ActivityLog;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class ActivityLogDAO {

    // ==================== TABLE CREATION ====================
    public static void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS activity_log (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id INT NOT NULL DEFAULT 0," +
                "nom_utilisateur VARCHAR(100) NOT NULL DEFAULT ''," +
                "email VARCHAR(100) NOT NULL DEFAULT ''," +
                "roles VARCHAR(100) NOT NULL DEFAULT ''," +
                "action VARCHAR(100) NOT NULL," +
                "date_heure DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ")";
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error creating activity_log table: " + e.getMessage());
        }
    }

    // ==================== INSERT ====================
    public static boolean insertLog(ActivityLog log) {
        createTableIfNotExists();
        String sql = "INSERT INTO activity_log (user_id, nom_utilisateur, email, roles, action, date_heure) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, log.getUserId());
            ps.setString(2, log.getNomUtilisateur() != null ? log.getNomUtilisateur() : "");
            ps.setString(3, log.getEmail() != null ? log.getEmail() : "");
            ps.setString(4, log.getRoles() != null ? log.getRoles() : "");
            ps.setString(5, log.getAction());
            ps.setTimestamp(6, Timestamp.valueOf(
                    log.getDateHeure() != null ? log.getDateHeure() : LocalDateTime.now()));
            int rows = ps.executeUpdate();
            ps.close();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting log: " + e.getMessage());
            return false;
        }
    }

    // ==================== GET ALL ====================
    public static List<ActivityLog> getAllLogs() {
        createTableIfNotExists();
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM activity_log ORDER BY date_heure DESC";
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                logs.add(mapRow(rs));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error getting logs: " + e.getMessage());
        }
        return logs;
    }

    // ==================== GET WITH FILTERS ====================
    public static List<ActivityLog> getLogsWithFilters(String role, String action,
                                                         LocalDateTime dateDebut, LocalDateTime dateFin) {
        createTableIfNotExists();
        List<ActivityLog> logs = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM activity_log WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (role != null && !role.isEmpty() && !role.equals("Tous les rôles")) {
            sql.append(" AND roles LIKE ?");
            params.add("%" + role + "%");
        }
        if (action != null && !action.isEmpty() && !action.equals("Toutes les actions")) {
            sql.append(" AND action = ?");
            params.add(action);
        }
        if (dateDebut != null) {
            sql.append(" AND date_heure >= ?");
            params.add(Timestamp.valueOf(dateDebut));
        }
        if (dateFin != null) {
            sql.append(" AND date_heure <= ?");
            params.add(Timestamp.valueOf(dateFin.withHour(23).withMinute(59).withSecond(59)));
        }
        sql.append(" ORDER BY date_heure DESC");

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof String) ps.setString(i + 1, (String) p);
                else if (p instanceof Timestamp) ps.setTimestamp(i + 1, (Timestamp) p);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                logs.add(mapRow(rs));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.err.println("Error filtering logs: " + e.getMessage());
        }
        return logs;
    }

    // ==================== CLEAR ====================
    public static boolean clearAllLogs() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            int rows = stmt.executeUpdate("DELETE FROM activity_log");
            stmt.close();
            return rows >= 0;
        } catch (SQLException e) {
            System.err.println("Error clearing logs: " + e.getMessage());
            return false;
        }
    }

    // ==================== STATS ====================
    public static int getTotalCount() {
        return countQuery("SELECT COUNT(*) FROM activity_log");
    }

    public static int getTodayCount() {
        return countQuery("SELECT COUNT(*) FROM activity_log WHERE DATE(date_heure) = CURDATE()");
    }

    public static int getThisWeekCount() {
        return countQuery("SELECT COUNT(*) FROM activity_log WHERE YEARWEEK(date_heure, 1) = YEARWEEK(CURDATE(), 1)");
    }

    public static int getThisMonthCount() {
        return countQuery("SELECT COUNT(*) FROM activity_log WHERE MONTH(date_heure) = MONTH(CURDATE()) AND YEAR(date_heure) = YEAR(CURDATE())");
    }

    /** Returns a map of date-string (dd/MM) -> count for the last 30 days */
    public static Map<String, Integer> getActivityLast30Days() {
        createTableIfNotExists();
        Map<String, Integer> result = new LinkedHashMap<>();
        String sql = "SELECT DATE(date_heure) as jour, COUNT(*) as cnt " +
                "FROM activity_log " +
                "WHERE date_heure >= DATE_SUB(CURDATE(), INTERVAL 29 DAY) " +
                "GROUP BY DATE(date_heure) ORDER BY jour ASC";
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                java.sql.Date d = rs.getDate("jour");
                String label = String.format("%02d/%02d", d.getDate(), d.getMonth() + 1);
                result.put(label, rs.getInt("cnt"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error getActivityLast30Days: " + e.getMessage());
        }
        return result;
    }

    /** Returns role -> count */
    public static Map<String, Integer> getCountByRole() {
        createTableIfNotExists();
        Map<String, Integer> result = new LinkedHashMap<>();
        String sql = "SELECT roles, COUNT(*) as cnt FROM activity_log GROUP BY roles";
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String roles = rs.getString("roles");
                String display = extractDisplayRole(roles);
                result.merge(display, rs.getInt("cnt"), Integer::sum);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error getCountByRole: " + e.getMessage());
        }
        return result;
    }

    /** Returns top 5 users by action count: nom -> count */
    public static Map<String, Integer> getTop5Users() {
        createTableIfNotExists();
        Map<String, Integer> result = new LinkedHashMap<>();
        String sql = "SELECT nom_utilisateur, COUNT(*) as cnt FROM activity_log " +
                "GROUP BY nom_utilisateur ORDER BY cnt DESC LIMIT 5";
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                result.put(rs.getString("nom_utilisateur"), rs.getInt("cnt"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error getTop5Users: " + e.getMessage());
        }
        return result;
    }

    /** Returns hour (0-23) -> count */
    public static int[] getHourlyDistribution() {
        createTableIfNotExists();
        int[] hours = new int[24];
        String sql = "SELECT HOUR(date_heure) as heure, COUNT(*) as cnt FROM activity_log GROUP BY heure";
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int h = rs.getInt("heure");
                if (h >= 0 && h < 24) hours[h] = rs.getInt("cnt");
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error getHourlyDistribution: " + e.getMessage());
        }
        return hours;
    }

    /** Returns top 5 actions by count */
    public static Map<String, Integer> getTop5Actions() {
        createTableIfNotExists();
        Map<String, Integer> result = new LinkedHashMap<>();
        String sql = "SELECT action, COUNT(*) as cnt FROM activity_log GROUP BY action ORDER BY cnt DESC LIMIT 5";
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                result.put(rs.getString("action"), rs.getInt("cnt"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error getTop5Actions: " + e.getMessage());
        }
        return result;
    }

    // ==================== HELPERS ====================
    private static ActivityLog mapRow(ResultSet rs) throws SQLException {
        ActivityLog log = new ActivityLog();
        log.setId(rs.getInt("id"));
        log.setUserId(rs.getInt("user_id"));
        log.setNomUtilisateur(rs.getString("nom_utilisateur"));
        log.setEmail(rs.getString("email"));
        log.setRoles(rs.getString("roles"));
        log.setAction(rs.getString("action"));
        Timestamp ts = rs.getTimestamp("date_heure");
        if (ts != null) log.setDateHeure(ts.toLocalDateTime());
        return log;
    }

    private static int countQuery(String sql) {
        createTableIfNotExists();
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                int count = rs.getInt(1);
                rs.close();
                stmt.close();
                return count;
            }
        } catch (SQLException e) {
            System.err.println("Error countQuery: " + e.getMessage());
        }
        return 0;
    }

    private static String extractDisplayRole(String roles) {
        if (roles == null) return "Utilisateur";
        if (roles.contains("ADMIN")) return "Administrateur";
        if (roles.contains("COACH")) return "Coach";
        if (roles.contains("SPECIALISTE")) return "Spécialiste";
        return "Utilisateur";
    }
}
