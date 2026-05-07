package org.example.dao;

import org.example.model.Consultation;
import org.example.model.RendezVous;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Consultation operations
 */
public class ConsultationDAO {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/biosync_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    // Create consultation
    public static boolean createConsultation(Consultation consultation) {
        String sql = "INSERT INTO consultation (date_consultation, symptomes, diagnostic, recommandations, rendez_vous_id, statut, date_creation) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setTimestamp(1, Timestamp.valueOf(consultation.getDateConsultation()));
            pstmt.setString(2, consultation.getSymptomes());
            pstmt.setString(3, consultation.getDiagnostic());
            pstmt.setString(4, consultation.getRecommandations());
            pstmt.setInt(5, consultation.getRendezVous().getId());
            pstmt.setString(6, consultation.getStatut());
            pstmt.setTimestamp(7, Timestamp.valueOf(consultation.getDateCreation()));
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    consultation.setId(generatedKeys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating consultation: " + e.getMessage());
        }
        return false;
    }
    
    // Get consultation by rendez-vous ID
    public static Consultation getConsultationByRendezVousId(int rendezVousId) {
        String sql = "SELECT * FROM consultation WHERE rendez_vous_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, rendezVousId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToConsultation(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting consultation: " + e.getMessage());
        }
        return null;
    }
    
    // Get consultation by ID
    public static Consultation getConsultationById(int id) {
        String sql = "SELECT * FROM consultation WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToConsultation(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting consultation: " + e.getMessage());
        }
        return null;
    }
    
    // Update consultation
    public static boolean updateConsultation(Consultation consultation) {
        String sql = "UPDATE consultation SET symptomes = ?, diagnostic = ?, recommandations = ?, statut = ? WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, consultation.getSymptomes());
            pstmt.setString(2, consultation.getDiagnostic());
            pstmt.setString(3, consultation.getRecommandations());
            pstmt.setString(4, consultation.getStatut());
            pstmt.setInt(5, consultation.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating consultation: " + e.getMessage());
        }
        return false;
    }
    
    // Get all consultations for a patient
    public static List<Consultation> getConsultationsByPatientId(int patientId) {
        List<Consultation> consultations = new ArrayList<>();
        String sql = "SELECT c.* FROM consultation c " +
                    "JOIN rendez_vous rv ON c.rendez_vous_id = rv.id " +
                    "WHERE rv.patient_id = ? ORDER BY c.date_consultation DESC";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                consultations.add(mapResultSetToConsultation(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting patient consultations: " + e.getMessage());
        }
        return consultations;
    }
    
    // Get all consultations for a specialist
    public static List<Consultation> getConsultationsBySpecialisteId(int specialisteId) {
        List<Consultation> consultations = new ArrayList<>();
        String sql = "SELECT c.* FROM consultation c " +
                    "JOIN rendez_vous rv ON c.rendez_vous_id = rv.id " +
                    "WHERE rv.specialiste_id = ? ORDER BY c.date_consultation DESC";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, specialisteId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                consultations.add(mapResultSetToConsultation(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting specialist consultations: " + e.getMessage());
        }
        return consultations;
    }
    
    // Helper method to map ResultSet to Consultation
    private static Consultation mapResultSetToConsultation(ResultSet rs) throws SQLException {
        Consultation consultation = new Consultation();
        consultation.setId(rs.getInt("id"));
        consultation.setDateConsultation(rs.getTimestamp("date_consultation").toLocalDateTime());
        consultation.setSymptomes(rs.getString("symptomes"));
        consultation.setDiagnostic(rs.getString("diagnostic"));
        consultation.setRecommandations(rs.getString("recommandations"));
        consultation.setStatut(rs.getString("statut"));
        consultation.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
        
        // Get associated rendez-vous
        int rendezVousId = rs.getInt("rendez_vous_id");
        RendezVous rendezVous = RendezVousDAO.getRendezVousById(rendezVousId);
        consultation.setRendezVous(rendezVous);
        
        return consultation;
    }
}
