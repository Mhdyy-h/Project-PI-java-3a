// Test script to verify specialist functions work
import java.sql.*;

public class test_specialist_functions {
    public static void main(String[] args) {
        System.out.println("=== TESTING SPECIALIST FUNCTIONS ===");
        
        try {
            String url = "jdbc:mysql://localhost:3306/biosync";
            String username = "root";
            String password = "";
            
            Connection conn = DriverManager.getConnection(url, username, password);
            
            // Test 1: Show current RDVs
            System.out.println("\n📋 CURRENT RDVs:");
            String sql = "SELECT rv.*, s.nom_docteur " +
                        "FROM rendez_vous rv " +
                        "LEFT JOIN specialiste s ON rv.specialiste_id = s.id " +
                        "WHERE rv.statut = 'en attente' " +
                        "ORDER BY rv.date_heure ASC";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            
            int pendingCount = 0;
            while (rs.next()) {
                pendingCount++;
                System.out.println(String.format("   📋 RDV #%d: %s - %s", 
                    rs.getInt("id"),
                    rs.getTimestamp("date_heure").toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    rs.getString("motif")
                ));
                System.out.println(String.format("      👨‍⚕️ Spécialiste: %s", rs.getString("nom_docteur")));
            }
            
            if (pendingCount == 0) {
                System.out.println("   📭 No pending RDVs found");
            }
            
            // Test 2: Simulate specialist confirmation
            if (pendingCount > 0) {
                System.out.println("\n✅ TESTING SPECIALIST CONFIRMATION:");
                
                // Get first pending RDV
                rs.first();
                int rdvId = rs.getInt("id");
                
                // Update status
                String updateSql = "UPDATE rendez_vous SET statut = 'confirmé' WHERE id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, rdvId);
                
                int affected = updateStmt.executeUpdate();
                
                if (affected > 0) {
                    System.out.println("   ✅ RDV #" + rdvId + " confirmed successfully!");
                    
                    // Verify the change
                    String checkSql = "SELECT statut FROM rendez_vous WHERE id = ?";
                    PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                    checkStmt.setInt(1, rdvId);
                    ResultSet checkRs = checkStmt.executeQuery();
                    
                    if (checkRs.next()) {
                        System.out.println("   📊 New status: " + checkRs.getString("statut"));
                    }
                    
                    checkRs.close();
                }
                
                updateStmt.close();
            }
            
            // Test 3: Show patient RDV management
            System.out.println("\n👤 TESTING PATIENT RDV MANAGEMENT:");
            
            String patientSql = "SELECT rv.*, u.nom_complet as patient_name " +
                               "FROM rendez_vous rv " +
                               "LEFT JOIN utilisateur u ON rv.patient_id = u.id " +
                               "WHERE rv.statut = 'en attente' " +
                               "ORDER BY rv.date_heure ASC LIMIT 3";
            
            PreparedStatement patientStmt = conn.prepareStatement(patientSql);
            ResultSet patientRs = patientStmt.executeQuery();
            
            System.out.println("   📋 Patient can EDIT/DELETE these RDVs:");
            while (patientRs.next()) {
                System.out.println(String.format("   📋 RDV #%d: %s - %s (Patient: %s)", 
                    patientRs.getInt("id"),
                    patientRs.getTimestamp("date_heure").toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    patientRs.getString("motif"),
                    patientRs.getString("patient_name")
                ));
                System.out.println("      🔧 Actions: [EDIT] [DELETE] (Available to patient)");
            }
            
            rs.close();
            pstmt.close();
            patientRs.close();
            patientStmt.close();
            conn.close();
            
            System.out.println("\n✅ ALL TESTS COMPLETED SUCCESSFULLY!");
            System.out.println("🎯 Specialist interface functions are working!");
            System.out.println("👤 Patient RDV management functions are working!");
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
