package org.example.dao;

import org.example.DatabaseConnection;
import org.example.model.RendezVous;
import org.example.model.Specialiste;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RendezVousDAO {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Helper method to get fresh connection
    private static Connection getFreshConnection() throws SQLException {
        try {
            String url = "jdbc:mysql://localhost:3306/biosync";
            String username = "root";
            String password = ""; // Empty password as per config
            String driver = "com.mysql.cj.jdbc.Driver";
            
            Class.forName(driver);
            return DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL driver not found", e);
        }
    }
    
    // Create rendezvous table if not exists
    public static void createTableIfNotExists() {
        try {
            Connection conn = getFreshConnection();
            Statement stmt = conn.createStatement();
            
            String sql = "CREATE TABLE IF NOT EXISTS rendez_vous (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY," +
                         "date_heure DATETIME NOT NULL," +
                         "motif VARCHAR(500) NOT NULL," +
                         "statut VARCHAR(20) DEFAULT 'en attente'," +
                         "mode VARCHAR(20) DEFAULT 'présentiel'," +
                         "lieu VARCHAR(200)," +
                         "niveau_urgence INT," +
                         "patient_id INT NOT NULL," +
                         "specialiste_id INT NOT NULL," +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                         "FOREIGN KEY (patient_id) REFERENCES utilisateur(id)," +
                         "FOREIGN KEY (specialiste_id) REFERENCES utilisateur(id)" +
                         ")";
            
            stmt.execute(sql);
            System.out.println("Table 'rendez_vous' created or already exists");
            stmt.close();
            
        } catch (SQLException e) {
            System.err.println("Error creating rendez_vous table: " + e.getMessage());
        }
    }
    
    // CREATE
    public static boolean createRendezVous(RendezVous rendezVous) {
        try {
            createTableIfNotExists(); // Ensure table exists
        } catch (Exception e) {
            System.err.println("Warning: Could not create table: " + e.getMessage());
        }
        
        String sql = "INSERT INTO rendez_vous (date_heure, motif, statut, mode, patient_id, specialiste_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getFreshConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, rendezVous.getDateHeure().format(DATE_TIME_FORMATTER));
            pstmt.setString(2, rendezVous.getMotif());
            pstmt.setString(3, rendezVous.getStatut());
            pstmt.setString(4, rendezVous.getMode());
            pstmt.setInt(5, rendezVous.getPatientId());
            pstmt.setInt(6, rendezVous.getSpecialisteId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    rendezVous.setId(generatedKeys.getInt(1));
                }
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating rendez-vous: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // READ - Get all rendez-vous
    public static List<RendezVous> getAllRendezVous() {
        List<RendezVous> rendezVousList = new ArrayList<>();
        String sql = "SELECT rv.*, u1.nom_complet as patient_nom, u2.nom_complet as specialiste_nom " +
                     "FROM rendez_vous rv " +
                     "LEFT JOIN utilisateur u1 ON rv.patient_id = u1.id " +
                     "LEFT JOIN utilisateur u2 ON rv.specialiste_id = u2.id " +
                     "ORDER BY rv.date_heure DESC";
        
        System.out.println("🔍 DEBUG: Executing SQL: " + sql);
        
        try (Connection conn = DatabaseConnection.getFreshConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                RendezVous rv = mapResultSetToRendezVous(rs);
                rendezVousList.add(rv);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all rendez-vous: " + e.getMessage());
            e.printStackTrace();
        }
        
        return rendezVousList;
    }
    
    // READ - Get by ID
    public static RendezVous getRendezVousById(Integer id) {
        String sql = "SELECT rv.*, u1.nom_complet as patient_nom, u2.nom_complet as specialiste_nom " +
                     "FROM rendez_vous rv " +
                     "LEFT JOIN utilisateur u1 ON rv.patient_id = u1.id " +
                     "LEFT JOIN utilisateur u2 ON rv.specialiste_id = u2.id " +
                     "WHERE rv.id = ?";
        
        try (Connection conn = getFreshConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToRendezVous(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting rendez-vous by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    // UPDATE
    public static boolean updateRendezVous(RendezVous rendezVous) {
        String sql = "UPDATE rendez_vous SET date_heure = ?, motif = ?, statut = ?, mode = ?, patient_id = ?, specialiste_id = ? WHERE id = ?";
        
        try (Connection conn = getFreshConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, rendezVous.getDateHeure().format(DATE_TIME_FORMATTER));
            pstmt.setString(2, rendezVous.getMotif());
            pstmt.setString(3, rendezVous.getStatut());
            pstmt.setString(4, rendezVous.getMode());
            pstmt.setInt(5, rendezVous.getPatientId());
            pstmt.setInt(6, rendezVous.getSpecialisteId());
            pstmt.setInt(7, rendezVous.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating rendez-vous: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Get rendezvous by specialist and date
    public static List<RendezVous> getRendezVousBySpecialisteAndDate(Integer specialisteId, LocalDate date) {
        List<RendezVous> rendezVousList = new ArrayList<>();
        String sql = "SELECT * FROM rendez_vous WHERE specialiste_id = ? AND DATE(date_heure) = ?";
        
        try (Connection conn = getFreshConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, specialisteId);
            pstmt.setString(2, date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                rendezVousList.add(mapResultSetToRendezVous(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting rendezvous by specialist and date: " + e.getMessage());
            e.printStackTrace();
        }
        
        return rendezVousList;
    }
    
    // Get rendezvous by patient
    public static List<RendezVous> getRendezVousByPatient(Integer patientId) {
        List<RendezVous> rendezVousList = new ArrayList<>();
        String sql = "SELECT * FROM rendez_vous WHERE patient_id = ? ORDER BY date_heure DESC";
        
        try (Connection conn = getFreshConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                rendezVousList.add(mapResultSetToRendezVous(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting rendezvous by patient: " + e.getMessage());
            e.printStackTrace();
        }
        
        return rendezVousList;
    }
    
    // Get rendezvous by date range
    public static List<RendezVous> getRendezVousByDateRange(LocalDate startDate, LocalDate endDate) {
        List<RendezVous> rendezVousList = new ArrayList<>();
        String sql = "SELECT * FROM rendez_vous WHERE DATE(date_heure) BETWEEN ? AND ? ORDER BY date_heure";
        
        try (Connection conn = getFreshConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            pstmt.setString(2, endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                rendezVousList.add(mapResultSetToRendezVous(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting rendezvous by date range: " + e.getMessage());
            e.printStackTrace();
        }
        
        return rendezVousList;
    }
    
    // DELETE
    public static boolean deleteRendezVous(Integer id) {
        String sql = "DELETE FROM rendez_vous WHERE id = ?";
        
        try (Connection conn = getFreshConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting rendez-vous: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // SEARCH
    public static List<RendezVous> searchRendezVous(String keyword, String statut, Integer specialisteId) {
        List<RendezVous> rendezVousList = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT rv.*, u1.nom_complet as patient_nom, u2.nom_complet as specialiste_nom " +
            "FROM rendez_vous rv " +
            "LEFT JOIN utilisateur u1 ON rv.patient_id = u1.id " +
            "LEFT JOIN utilisateur u2 ON rv.specialiste_id = u2.id " +
            "WHERE 1=1"
        );
        
        List<Object> params = new ArrayList<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(rv.motif) LIKE LOWER(?) OR LOWER(rv.lieu) LIKE LOWER(?))");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        
        if (statut != null && !statut.trim().isEmpty()) {
            sql.append(" AND rv.statut = ?");
            params.add(statut);
        }
        
        if (specialisteId != null) {
            sql.append(" AND rv.specialiste_id = ?");
            params.add(specialisteId);
        }
        
        sql.append(" ORDER BY rv.date_heure DESC");
        
        try (Connection conn = DatabaseConnection.getFreshConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                RendezVous rv = mapResultSetToRendezVous(rs);
                rendezVousList.add(rv);
            }
            
        } catch (SQLException e) {
            System.err.println("Error searching rendez-vous: " + e.getMessage());
            e.printStackTrace();
        }
        
        return rendezVousList;
    }
    
    // Check for conflicts
    public static boolean hasConflict(Integer specialisteId, LocalDateTime dateHeure, Integer excludeId) {
        String sql = "SELECT COUNT(*) as count FROM rendez_vous " +
                     "WHERE specialiste_id = ? AND DATE(date_heure) = DATE(?) " +
                     "AND HOUR(date_heure) = HOUR(?) " +
                     "AND (date_heure BETWEEN ? AND DATE_ADD(?, INTERVAL 1 HOUR))";
        
        if (excludeId != null) {
            sql += " AND id != ?";
        }
        
        try (Connection conn = getFreshConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, specialisteId);
            pstmt.setString(2, dateHeure.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            pstmt.setString(3, dateHeure.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            pstmt.setString(4, dateHeure.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            pstmt.setString(5, dateHeure.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            if (excludeId != null) {
                pstmt.setInt(6, excludeId);
            }
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking conflicts: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Helper method to map ResultSet to RendezVous
    private static RendezVous mapResultSetToRendezVous(ResultSet rs) throws SQLException {
        RendezVous rv = new RendezVous();
        rv.setId(rs.getInt("id"));
        
        Timestamp timestamp = rs.getTimestamp("date_heure");
        if (timestamp != null) {
            rv.setDateHeure(timestamp.toLocalDateTime());
        }
        
        rv.setMotif(rs.getString("motif"));
        rv.setStatut(rs.getString("statut"));
        rv.setMode(rs.getString("mode"));
        
        // Handle optional columns that may not exist
        try {
            rv.setLieu(rs.getString("lieu"));
        } catch (SQLException e) {
            rv.setLieu(""); // Default value
        }
        
        try {
            rv.setNiveauUrgence(rs.getObject("niveau_urgence", Integer.class));
        } catch (SQLException e) {
            rv.setNiveauUrgence(null); // Default value
        }
        
        rv.setPatientId(rs.getInt("patient_id"));
        rv.setSpecialisteId(rs.getInt("specialiste_id"));
        
        // Handle optional joined columns
        try {
            rv.setPatientNom(rs.getString("patient_nom"));
        } catch (SQLException e) {
            rv.setPatientNom("Patient");
        }
        
        try {
            String specialistName = rs.getString("specialiste_nom");
            System.out.println("🔍 DEBUG: Raw specialist_name from DB: " + specialistName);
            rv.setSpecialisteNom(specialistName);
        } catch (SQLException e) {
            System.out.println("🔍 DEBUG: specialist_nom column not found, using default");
            rv.setSpecialisteNom("Spécialiste");
        }
        
        return rv;
    }
    
    // Update rendezvous status
    public static boolean updateRendezVousStatus(int id, String status) {
        String sql = "UPDATE rendezvous SET statut = ? WHERE id = ?";
        
        try (Connection conn = getFreshConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating rendezvous status: " + e.getMessage());
            return false;
        }
    }
    
    // Get rendezvous by status
    public static List<RendezVous> getRendezVousByStatut(String statut) {
        List<RendezVous> rendezVous = new ArrayList<>();
        String sql = "SELECT * FROM rendezvous WHERE statut = ?";
        
        try (Connection conn = getFreshConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, statut);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    rendezVous.add(mapResultSetToRendezVous(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting rendezvous by status: " + e.getMessage());
        }
        
        return rendezVous;
    }
    
    // Test connection
    public static boolean testConnection() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
