// Utilitaire pour gérer les RDV (confirmer/annuler)
import org.example.dao.RendezVousDAO;
import org.example.model.RendezVous;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class manage_rendezvous {
    public static void main(String[] args) {
        System.out.println("=== GESTION DES RENDEZ-VOUS ===");
        
        try {
            // 1. Afficher tous les RDV
            System.out.println("\n📋 1. Liste de tous les RDV:");
            showAllRendezVous();
            
            // 2. Exemple de confirmation d'un RDV
            System.out.println("\n✅ 2. Exemple de confirmation d'un RDV:");
            confirmRendezVousExample();
            
            // 3. Exemple d'annulation d'un RDV
            System.out.println("\n❌ 3. Exemple d'annulation d'un RDV:");
            cancelRendezVousExample();
            
        } catch (Exception e) {
            System.out.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== OPÉRATION TERMINÉE ===");
    }
    
    private static void showAllRendezVous() {
        try {
            // Utiliser une connexion directe pour récupérer tous les RDV
            String url = "jdbc:mysql://localhost:3306/biosync";
            String username = "root";
            String password = "";
            
            Connection conn = DriverManager.getConnection(url, username, password);
            
            String sql = "SELECT rv.*, s.nom_docteur " +
                        "FROM rendez_vous rv " +
                        "LEFT JOIN specialiste s ON rv.specialiste_id = s.id " +
                        "ORDER BY rv.date_heure DESC";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            
            boolean hasRdv = false;
            while (rs.next()) {
                hasRdv = true;
                RendezVous rdv = new RendezVous();
                rdv.setId(rs.getInt("id"));
                rdv.setDateHeure(rs.getTimestamp("date_heure").toLocalDateTime());
                rdv.setMotif(rs.getString("motif"));
                rdv.setStatut(rs.getString("statut"));
                rdv.setMode(rs.getString("mode"));
                rdv.setPatientId(rs.getInt("patient_id"));
                rdv.setSpecialisteId(rs.getInt("specialiste_id"));
                
                System.out.println(String.format("   📅 RDV #%d: %s", 
                    rdv.getId(),
                    rdv.getDateHeure().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                ));
                System.out.println(String.format("      👨‍⚕️ Spécialiste: %s", 
                    rs.getString("nom_docteur") != null ? rs.getString("nom_docteur") : "Spécialiste #" + rdv.getSpecialisteId()
                ));
                System.out.println(String.format("      💬 Motif: %s", rdv.getMotif()));
                System.out.println(String.format("      📊 Statut: %s", rdv.getStatut()));
                System.out.println(String.format("      🏥 Mode: %s", rdv.getMode()));
                
                // Afficher les actions possibles selon le statut
                if ("en attente".equals(rdv.getStatut())) {
                    System.out.println("      🔧 Actions: [C]onfirmer | [A]nnuler");
                } else if ("confirmé".equals(rdv.getStatut())) {
                    System.out.println("      🔧 Actions: [A]nnuler | [R]eprogrammer");
                } else if ("annulé".equals(rdv.getStatut())) {
                    System.out.println("      🔧 Actions: [R]éactiver");
                } else {
                    System.out.println("      🔧 Actions: [M]odifier");
                }
                
                System.out.println();
            }
            
            if (!hasRdv) {
                System.out.println("   📭 Aucun RDV trouvé");
            }
            
            conn.close();
            
        } catch (SQLException e) {
            System.out.println("❌ Erreur affichage RDV: " + e.getMessage());
        }
    }
    
    private static void confirmRendezVousExample() {
        try {
            // Trouver le premier RDV en attente
            String url = "jdbc:mysql://localhost:3306/biosync";
            String username = "root";
            String password = "";
            
            Connection conn = DriverManager.getConnection(url, username, password);
            
            String sql = "SELECT id FROM rendez_vous WHERE statut = 'en attente' ORDER BY date_heure ASC LIMIT 1";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int rdvId = rs.getInt("id");
                
                // Mettre à jour le statut
                String updateSql = "UPDATE rendez_vous SET statut = 'confirmé' WHERE id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, rdvId);
                int affectedRows = updateStmt.executeUpdate();
                
                if (affectedRows > 0) {
                    System.out.println("   ✅ RDV #" + rdvId + " confirmé avec succès!");
                }
                
                updateStmt.close();
            } else {
                System.out.println("   📭 Aucun RDV en attente à confirmer");
            }
            
            rs.close();
            pstmt.close();
            conn.close();
            
        } catch (SQLException e) {
            System.out.println("❌ Erreur confirmation RDV: " + e.getMessage());
        }
    }
    
    private static void cancelRendezVousExample() {
        try {
            // Trouver le premier RDV confirmé
            String url = "jdbc:mysql://localhost:3306/biosync";
            String username = "root";
            String password = "";
            
            Connection conn = DriverManager.getConnection(url, username, password);
            
            String sql = "SELECT id FROM rendez_vous WHERE statut = 'confirmé' ORDER BY date_heure ASC LIMIT 1";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int rdvId = rs.getInt("id");
                
                // Mettre à jour le statut
                String updateSql = "UPDATE rendez_vous SET statut = 'annulé' WHERE id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, rdvId);
                int affectedRows = updateStmt.executeUpdate();
                
                if (affectedRows > 0) {
                    System.out.println("   ❌ RDV #" + rdvId + " annulé avec succès!");
                }
                
                updateStmt.close();
            } else {
                System.out.println("   📭 Aucun RDV confirmé à annuler");
            }
            
            rs.close();
            pstmt.close();
            conn.close();
            
        } catch (SQLException e) {
            System.out.println("❌ Erreur annulation RDV: " + e.getMessage());
        }
    }
}
