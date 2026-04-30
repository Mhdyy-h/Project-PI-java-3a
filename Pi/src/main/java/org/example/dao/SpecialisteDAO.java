package org.example.dao;

import org.example.DatabaseConnection;
import org.example.model.Specialiste;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SpecialisteDAO {
    
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
    
    // READ - Get all specialists
    public static List<Specialiste> getAllSpecialistes() {
        List<Specialiste> specialistesList = new ArrayList<>();
        String sql = "SELECT * FROM specialiste ORDER BY nom_docteur";
        
        try (Connection conn = getFreshConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Specialiste specialiste = mapResultSetToSpecialiste(rs);
                specialistesList.add(specialiste);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all specialists: " + e.getMessage());
            e.printStackTrace();
        }
        
        return specialistesList;
    }
    
    // READ - Get by ID
    public static Specialiste getSpecialisteById(Integer id) {
        String sql = "SELECT * FROM specialiste WHERE id = ?";
        
        try (Connection conn = getFreshConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToSpecialiste(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting specialist by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    // SEARCH
    public static List<Specialiste> searchSpecialistes(String keyword) {
        List<Specialiste> specialistesList = new ArrayList<>();
        String sql = "SELECT s.*, u.email, u.nom_complet " +
                     "FROM specialiste s " +
                     "LEFT JOIN utilisateur u ON s.utilisateur_id = u.id " +
                     "WHERE LOWER(s.nom_docteur) LIKE LOWER(?) OR " +
                     "LOWER(s.specialite) LIKE LOWER(?) OR " +
                     "LOWER(s.ville) LIKE LOWER(?) OR " +
                     "LOWER(u.nom_complet) LIKE LOWER(?) " +
                     "ORDER BY s.nom_docteur";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            pstmt.setString(4, searchPattern);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Specialiste specialiste = mapResultSetToSpecialiste(rs);
                specialistesList.add(specialiste);
            }
            
        } catch (SQLException e) {
            System.err.println("Error searching specialists: " + e.getMessage());
            e.printStackTrace();
        }
        
        return specialistesList;
    }
    
    // CREATE
    public static boolean createSpecialiste(Specialiste specialiste) {
        String sql = "INSERT INTO specialiste (nom_docteur, specialite, telephone, disponibilite, utilisateur_id) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = getFreshConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, specialiste.getNomDocteur());
            pstmt.setString(2, specialiste.getSpecialite());
            pstmt.setString(3, specialiste.getTelephone());
            pstmt.setString(4, specialiste.getDisponibilite());
            pstmt.setInt(5, specialiste.getUtilisateurId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    specialiste.setId(generatedKeys.getInt(1));
                }
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating specialist: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // UPDATE
    public static boolean updateSpecialiste(Specialiste specialiste) {
        String sql = "UPDATE specialiste SET nom_docteur = ?, specialite = ?, telephone = ?, " +
                     "disponibilite = ?, adresse = ?, ville = ?, note = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, specialiste.getNomDocteur());
            pstmt.setString(2, specialiste.getSpecialite());
            pstmt.setString(3, specialiste.getTelephone());
            pstmt.setString(4, specialiste.getDisponibilite());
            pstmt.setString(5, specialiste.getAdresse());
            pstmt.setString(6, specialiste.getVille());
            
            if (specialiste.getNote() != null) {
                pstmt.setInt(7, specialiste.getNote());
            } else {
                pstmt.setNull(7, Types.INTEGER);
            }
            
            pstmt.setInt(8, specialiste.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating specialist: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // DELETE
    public static boolean deleteSpecialiste(Integer id) {
        String sql = "DELETE FROM specialiste WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting specialist: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Helper method to map ResultSet to Specialiste
    private static Specialiste mapResultSetToSpecialiste(ResultSet rs) throws SQLException {
        Specialiste specialiste = new Specialiste();
        specialiste.setId(rs.getInt("id"));
        specialiste.setNomDocteur(rs.getString("nom_docteur"));
        specialiste.setSpecialite(rs.getString("specialite"));
        specialiste.setTelephone(rs.getString("telephone"));
        specialiste.setDisponibilite(rs.getString("disponibilite"));
        specialiste.setUtilisateurId(rs.getInt("utilisateur_id"));
        
        // Set default values for missing columns
        specialiste.setAdresse(""); // Default empty string
        specialiste.setVille("");   // Default empty string
        specialiste.setEmail("");   // Default empty string
        specialiste.setNote(0);     // Default 0
        
        return specialiste;
    }
    
    // Get specialists by specialty
    public static List<Specialiste> getSpecialistesBySpecialite(String specialite) {
        List<Specialiste> specialistesList = new ArrayList<>();
        String sql = "SELECT * FROM specialiste WHERE specialite = ? ORDER BY nom_docteur";
        
        try (Connection conn = getFreshConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, specialite);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                specialistesList.add(mapResultSetToSpecialiste(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting specialists by specialty: " + e.getMessage());
            e.printStackTrace();
        }
        
        return specialistesList;
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
