package org.example.dao;

import org.example.model.Prescription;
import org.example.model.Consultation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Prescription operations
 */
public class PrescriptionDAO {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/biosync_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    // Create prescription
    public static boolean createPrescription(Prescription prescription) {
        String sql = "INSERT INTO prescription (nom_medicament, dose, frequence, duree, instructions, consultation_id, date_creation, patient_id, specialiste_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, prescription.getNomMedicament());
            pstmt.setString(2, prescription.getDose());
            pstmt.setString(3, prescription.getFrequence());
            pstmt.setInt(4, prescription.getDuree());
            pstmt.setString(5, prescription.getInstructions());
            
            // consultation_id can be null if prescription is created directly
            if (prescription.getConsultation() != null) {
                pstmt.setInt(6, prescription.getConsultation().getId());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }
            
            pstmt.setTimestamp(7, Timestamp.valueOf(prescription.getDateCreation()));
            
            // patient_id and specialiste_id for role-based filtering
            if (prescription.getPatientId() != null) {
                pstmt.setInt(8, prescription.getPatientId());
            } else {
                pstmt.setNull(8, Types.INTEGER);
            }
            
            if (prescription.getSpecialisteId() != null) {
                pstmt.setInt(9, prescription.getSpecialisteId());
            } else {
                pstmt.setNull(9, Types.INTEGER);
            }
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    prescription.setId(generatedKeys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating prescription: " + e.getMessage());
        }
        return false;
    }
    
    // Get prescriptions by consultation ID
    public static List<Prescription> getPrescriptionsByConsultationId(int consultationId) {
        List<Prescription> prescriptions = new ArrayList<>();
        String sql = "SELECT * FROM prescription WHERE consultation_id = ? ORDER BY date_creation DESC";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, consultationId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                prescriptions.add(mapResultSetToPrescription(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting prescriptions: " + e.getMessage());
        }
        return prescriptions;
    }
    
    // Get prescription by ID
    public static Prescription getPrescriptionById(int id) {
        String sql = "SELECT * FROM prescription WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToPrescription(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting prescription: " + e.getMessage());
        }
        return null;
    }
    
    // Update prescription
    public static boolean updatePrescription(Prescription prescription) {
        String sql = "UPDATE prescription SET nom_medicament = ?, dose = ?, frequence = ?, duree = ?, instructions = ? WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, prescription.getNomMedicament());
            pstmt.setString(2, prescription.getDose());
            pstmt.setString(3, prescription.getFrequence());
            pstmt.setInt(4, prescription.getDuree());
            pstmt.setString(5, prescription.getInstructions());
            pstmt.setInt(6, prescription.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating prescription: " + e.getMessage());
        }
        return false;
    }
    
    // Delete prescription
    public static boolean deletePrescription(int id) {
        String sql = "DELETE FROM prescription WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting prescription: " + e.getMessage());
        }
        return false;
    }
    
    // Get all prescriptions for a patient (using direct patient_id field)
    public static List<Prescription> getPrescriptionsByPatientId(int patientId) {
        List<Prescription> prescriptions = new ArrayList<>();
        String sql = "SELECT * FROM prescription WHERE patient_id = ? ORDER BY date_creation DESC";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                prescriptions.add(mapResultSetToPrescription(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting patient prescriptions: " + e.getMessage());
        }
        return prescriptions;
    }
    
    // Get all prescriptions for a specialist (using direct specialiste_id field)
    public static List<Prescription> getPrescriptionsBySpecialisteId(int specialisteId) {
        List<Prescription> prescriptions = new ArrayList<>();
        String sql = "SELECT * FROM prescription WHERE specialiste_id = ? ORDER BY date_creation DESC";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, specialisteId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                prescriptions.add(mapResultSetToPrescription(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting specialist prescriptions: " + e.getMessage());
        }
        return prescriptions;
    }
    
    // Get active prescriptions for a patient
    public static List<Prescription> getActivePrescriptionsByPatientId(int patientId) {
        List<Prescription> prescriptions = new ArrayList<>();
        String sql = "SELECT p.* FROM prescription p " +
                    "JOIN consultation c ON p.consultation_id = c.id " +
                    "JOIN rendez_vous rv ON c.rendez_vous_id = rv.id " +
                    "WHERE rv.patient_id = ? " +
                    "AND p.date_creation >= DATE_SUB(NOW(), INTERVAL p.duree DAY) " +
                    "ORDER BY p.date_creation DESC";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                prescriptions.add(mapResultSetToPrescription(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting active prescriptions: " + e.getMessage());
        }
        return prescriptions;
    }
    
    // Helper method to map ResultSet to Prescription
    private static Prescription mapResultSetToPrescription(ResultSet rs) throws SQLException {
        Prescription prescription = new Prescription();
        prescription.setId(rs.getInt("id"));
        prescription.setNomMedicament(rs.getString("nom_medicament"));
        prescription.setDose(rs.getString("dose"));
        prescription.setFrequence(rs.getString("frequence"));
        prescription.setDuree(rs.getInt("duree"));
        prescription.setInstructions(rs.getString("instructions"));
        prescription.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
        
        // Get patient_id and specialiste_id for role-based filtering
        int patientId = rs.getInt("patient_id");
        if (!rs.wasNull()) {
            prescription.setPatientId(patientId);
        }
        
        int specialisteId = rs.getInt("specialiste_id");
        if (!rs.wasNull()) {
            prescription.setSpecialisteId(specialisteId);
        }
        
        // Get associated consultation (if exists)
        int consultationId = rs.getInt("consultation_id");
        if (!rs.wasNull()) {
            Consultation consultation = ConsultationDAO.getConsultationById(consultationId);
            prescription.setConsultation(consultation);
        }
        
        return prescription;
    }
}
