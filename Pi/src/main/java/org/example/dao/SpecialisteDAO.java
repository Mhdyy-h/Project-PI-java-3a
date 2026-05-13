package org.example.dao;

import org.example.DatabaseConnection;
import org.example.model.Specialiste;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SpecialisteDAO {
    
    // Create specialist table if not exists
    public static void createTableIfNotExists() {
        try {
            Connection conn = getFreshConnection();
            Statement stmt = conn.createStatement();
            
            String sql = "CREATE TABLE IF NOT EXISTS specialiste (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY," +
                         "nom_docteur VARCHAR(255) NOT NULL," +
                         "specialite VARCHAR(100) NOT NULL," +
                         "telephone VARCHAR(20)," +
                         "disponibilite VARCHAR(50)," +
                         "adresse TEXT," +
                         "ville VARCHAR(100)," +
                         "note INT," +
                         "utilisateur_id INT," +
                         "FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id)" +
                         ")";
            
            stmt.execute(sql);
            System.out.println("Table 'specialiste' created or already exists");
            
            // Populate specialist table with existing users who have specialist role
            populateSpecialistesFromUsers();
            
            stmt.close();
            
        } catch (SQLException e) {
            System.err.println("Error creating specialiste table: " + e.getMessage());
        }
    }
    
    // Populate specialist table with existing users who have specialist role
    private static void populateSpecialistesFromUsers() {
        try {
            Connection conn = getFreshConnection();
            Statement stmt = conn.createStatement();
            
            // Get users with specialist role and create specialist records for them
            String sql = "SELECT id, nom_complet, roles FROM utilisateur " +
                        "WHERE (roles LIKE '%SPECIALISTE%' OR roles LIKE '%SPECIALISTE%') AND " +
                        "id NOT IN (SELECT utilisateur_id FROM specialiste)";
            
            System.out.println("🔍 DEBUG: Looking for users with ROLE_SPECIALISTE in their roles field...");
            
            System.out.println("🔍 DEBUG: Looking for specialist users...");
            System.out.println("🔍 DEBUG: Executing SQL: " + sql);
            
            ResultSet rs = stmt.executeQuery(sql);
            
            int specialistCount = 0;
            while (rs.next()) {
                specialistCount++;
                int userId = rs.getInt("id");
                String nomComplet = rs.getString("nom_complet");
                String roles = rs.getString("roles");
                
                System.out.println("🔍 DEBUG: Found specialist user: " + nomComplet + " (ID: " + userId + ", Roles: " + roles + ")");
                
                // Check if this user already exists in specialist table
                String checkSql = "SELECT COUNT(*) FROM specialiste WHERE utilisateur_id = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setInt(1, userId);
                    ResultSet checkRs = checkStmt.executeQuery();
                    if (checkRs.next() && checkRs.getInt(1) > 0) {
                        System.out.println("⚠️ WARNING: User " + nomComplet + " already exists in specialist table, skipping...");
                        continue;
                    }
                    checkStmt.close();
                } catch (SQLException e) {
                    // Continue even if check fails
                }
                
                // Insert into specialist table
                
                // Insert into specialist table
                String insertSql = "INSERT INTO specialiste (nom_docteur, specialite, telephone, disponibilite, utilisateur_id) " +
                                 "VALUES (?, ?, ?, ?, ?)";
                
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    pstmt.setString(1, nomComplet);
                    pstmt.setString(2, "Généraliste"); // Default specialty
                    pstmt.setString(3, ""); // Default telephone
                    pstmt.setString(4, "Disponible"); // Default disponibilite
                    pstmt.setInt(5, userId);
                    pstmt.executeUpdate();
                    
                    System.out.println("✅ Created specialist record for user: " + nomComplet);
                }
            }
            
            rs.close();
            stmt.close();
            
            System.out.println("✅ DEBUG: Created " + specialistCount + " specialist records from users");
            
        } catch (SQLException e) {
            System.err.println("Error populating specialists from users: " + e.getMessage());
        }
    }
    
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
    
    // Check if specialist entry exists for a user
    public static boolean specialisteExistsForUser(Integer userId) {
        String sql = "SELECT COUNT(*) FROM specialiste WHERE utilisateur_id = ?";
        
        try (Connection conn = getFreshConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0;
            }
            return false;
            
        } catch (SQLException e) {
            System.err.println("Error checking if specialist exists for user: " + e.getMessage());
            return false;
        }
    }
}
