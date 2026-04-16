package org.example.dao;

import org.example.DatabaseConnection;
import org.example.model.RendezVous;
import org.example.model.Specialiste;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RendezVousDAO {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // CREATE
    public static boolean createRendezVous(RendezVous rendezVous) {
        String sql = "INSERT INTO rendez_vous (date_heure, motif, statut, mode, lieu, niveau_urgence, patient_id, specialiste_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, rendezVous.getDateHeure().format(DATE_TIME_FORMATTER));
            pstmt.setString(2, rendezVous.getMotif());
            pstmt.setString(3, rendezVous.getStatut());
            pstmt.setString(4, rendezVous.getMode());
            pstmt.setString(5, rendezVous.getLieu());
            
            if (rendezVous.getNiveauUrgence() != null) {
                pstmt.setInt(6, rendezVous.getNiveauUrgence());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }
            
            pstmt.setInt(7, rendezVous.getPatientId());
            pstmt.setInt(8, rendezVous.getSpecialisteId());
            
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
        
        try (Connection conn = DatabaseConnection.getConnection();
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
        
        try (Connection conn = DatabaseConnection.getConnection();
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
        String sql = "UPDATE rendez_vous SET date_heure = ?, motif = ?, statut = ?, mode = ?, lieu = ?, " +
                     "niveau_urgence = ?, patient_id = ?, specialiste_id = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, rendezVous.getDateHeure().format(DATE_TIME_FORMATTER));
            pstmt.setString(2, rendezVous.getMotif());
            pstmt.setString(3, rendezVous.getStatut());
            pstmt.setString(4, rendezVous.getMode());
            pstmt.setString(5, rendezVous.getLieu());
            
            if (rendezVous.getNiveauUrgence() != null) {
                pstmt.setInt(6, rendezVous.getNiveauUrgence());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }
            
            pstmt.setInt(7, rendezVous.getPatientId());
            pstmt.setInt(8, rendezVous.getSpecialisteId());
            pstmt.setInt(9, rendezVous.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating rendez-vous: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // DELETE
    public static boolean deleteRendezVous(Integer id) {
        String sql = "DELETE FROM rendez_vous WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
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
        
        try (Connection conn = DatabaseConnection.getConnection();
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
        
        try (Connection conn = DatabaseConnection.getConnection();
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
        rv.setLieu(rs.getString("lieu"));
        rv.setNiveauUrgence(rs.getObject("niveau_urgence", Integer.class));
        rv.setPatientId(rs.getInt("patient_id"));
        rv.setSpecialisteId(rs.getInt("specialiste_id"));
        rv.setPatientNom(rs.getString("patient_nom"));
        rv.setSpecialisteNom(rs.getString("specialiste_nom"));
        
        return rv;
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
