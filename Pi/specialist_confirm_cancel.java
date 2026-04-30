// Quick working solution for specialist to confirm/cancel RDVs
import java.sql.*;
import java.util.Scanner;

public class specialist_confirm_cancel {
    public static void main(String[] args) {
        System.out.println("=== SPECIALISTE - CONFIRMER/ANNULER RDVs ===");
        
        try {
            // Database connection
            String url = "jdbc:mysql://localhost:3306/biosync";
            String username = "root";
            String password = "";
            
            Connection conn = DriverManager.getConnection(url, username, password);
            
            Scanner scanner = new Scanner(System.in);
            
            while (true) {
                System.out.println("\n📋 MENU:");
                System.out.println("1. Voir tous les RDVs");
                System.out.println("2. Confirmer un RDV");
                System.out.println("3. Annuler un RDV");
                System.out.println("4. Quitter");
                System.out.print("Choix: ");
                
                int choice = scanner.nextInt();
                
                switch (choice) {
                    case 1:
                        showAllRendezVous(conn);
                        break;
                    case 2:
                        confirmRendezVous(conn, scanner);
                        break;
                    case 3:
                        cancelRendezVous(conn, scanner);
                        break;
                    case 4:
                        System.out.println("👋 Au revoir!");
                        conn.close();
                        return;
                    default:
                        System.out.println("❌ Choix invalide");
                }
            }
            
        } catch (Exception e) {
            System.out.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void showAllRendezVous(Connection conn) throws SQLException {
        System.out.println("\n📅 TOUS LES RENDEZ-VOUS:");
        
        String sql = "SELECT rv.*, s.nom_docteur " +
                    "FROM rendez_vous rv " +
                    "LEFT JOIN specialiste s ON rv.specialiste_id = s.id " +
                    "ORDER BY rv.date_heure ASC";
        
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();
        
        boolean hasRdv = false;
        while (rs.next()) {
            hasRdv = true;
            int id = rs.getInt("id");
            Timestamp dateTime = rs.getTimestamp("date_heure");
            String motif = rs.getString("motif");
            String statut = rs.getString("statut");
            String mode = rs.getString("mode");
            String specialiste = rs.getString("nom_docteur");
            
            System.out.println(String.format("   📋 RDV #%d: %s - %s", 
                id,
                dateTime.toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                motif
            ));
            System.out.println(String.format("      👨‍⚕️ Spécialiste: %s", specialiste != null ? specialiste : "N/A"));
            System.out.println(String.format("      📊 Statut: %s | 🏥 Mode: %s", statut, mode));
            System.out.println();
        }
        
        if (!hasRdv) {
            System.out.println("   📭 Aucun RDV trouvé");
        }
        
        rs.close();
        pstmt.close();
    }
    
    private static void confirmRendezVous(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("🔍 Entrez le numéro du RDV à confirmer: ");
        int rdvId = scanner.nextInt();
        
        // Check if RDV exists and is not already confirmed
        String checkSql = "SELECT statut FROM rendez_vous WHERE id = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkSql);
        checkStmt.setInt(1, rdvId);
        ResultSet rs = checkStmt.executeQuery();
        
        if (rs.next()) {
            String currentStatus = rs.getString("statut");
            
            if ("confirmé".equalsIgnoreCase(currentStatus)) {
                System.out.println("ℹ️ Ce RDV est déjà confirmé");
            } else if ("annulé".equalsIgnoreCase(currentStatus)) {
                System.out.println("⚠️ Ce RDV est déjà annulé - impossible de confirmer");
            } else {
                // Update status to confirmed
                String updateSql = "UPDATE rendez_vous SET statut = 'confirmé' WHERE id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, rdvId);
                
                int affectedRows = updateStmt.executeUpdate();
                
                if (affectedRows > 0) {
                    System.out.println("✅ RDV #" + rdvId + " confirmé avec succès!");
                } else {
                    System.out.println("❌ Erreur lors de la confirmation");
                }
                
                updateStmt.close();
            }
        } else {
            System.out.println("❌ RDV #" + rdvId + " non trouvé");
        }
        
        rs.close();
        checkStmt.close();
    }
    
    private static void cancelRendezVous(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("🔍 Entrez le numéro du RDV à annuler: ");
        int rdvId = scanner.nextInt();
        
        // Check if RDV exists and is not already cancelled
        String checkSql = "SELECT statut FROM rendez_vous WHERE id = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkSql);
        checkStmt.setInt(1, rdvId);
        ResultSet rs = checkStmt.executeQuery();
        
        if (rs.next()) {
            String currentStatus = rs.getString("statut");
            
            if ("annulé".equalsIgnoreCase(currentStatus)) {
                System.out.println("ℹ️ Ce RDV est déjà annulé");
            } else if ("confirmé".equalsIgnoreCase(currentStatus)) {
                System.out.println("⚠️ Ce RDV est déjà confirmé - voulez-vous vraiment l'annuler? (O/N)");
                scanner.nextLine(); // Clear buffer
                String response = scanner.nextLine();
                
                if ("O".equalsIgnoreCase(response)) {
                    // Update status to cancelled
                    String updateSql = "UPDATE rendez_vous SET statut = 'annulé' WHERE id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                    updateStmt.setInt(1, rdvId);
                    
                    int affectedRows = updateStmt.executeUpdate();
                    
                    if (affectedRows > 0) {
                        System.out.println("❌ RDV #" + rdvId + " annulé avec succès!");
                    } else {
                        System.out.println("❌ Erreur lors de l'annulation");
                    }
                    
                    updateStmt.close();
                } else {
                    System.out.println("👍 Annulation annulée");
                }
            } else {
                // Update status to cancelled
                String updateSql = "UPDATE rendez_vous SET statut = 'annulé' WHERE id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, rdvId);
                
                int affectedRows = updateStmt.executeUpdate();
                
                if (affectedRows > 0) {
                    System.out.println("❌ RDV #" + rdvId + " annulé avec succès!");
                } else {
                    System.out.println("❌ Erreur lors de l'annulation");
                }
                
                updateStmt.close();
            }
        } else {
            System.out.println("❌ RDV #" + rdvId + " non trouvé");
        }
        
        rs.close();
        checkStmt.close();
    }
}
