// Script pour créer un compte spécialiste et gérer les RDVs
import org.example.dao.UserDAO;
import org.example.dao.SpecialisteDAO;
import org.example.dao.RendezVousDAO;
import org.example.model.User;
import org.example.model.Specialiste;
import org.example.model.RendezVous;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class create_specialist_account {
    public static void main(String[] args) {
        System.out.println("=== CRÉATION COMPTE SPÉCIALISTE & GESTION RDV ===");
        
        try {
            // 1. Créer un compte utilisateur pour le spécialiste
            System.out.println("\n👤 1. Création compte utilisateur spécialiste...");
            User specialistUser = createSpecialistUser();
            
            if (specialistUser != null) {
                System.out.println("✅ Compte utilisateur créé: " + specialistUser.getNomComplet());
                System.out.println("   Email: " + specialistUser.getEmail());
                System.out.println("   ID: " + specialistUser.getId());
            }
            
            // 2. Créer le profil spécialiste
            System.out.println("\n🩺 2. Création profil spécialiste...");
            Specialiste specialist = createSpecialistProfile(specialistUser);
            
            if (specialist != null) {
                System.out.println("✅ Profil spécialiste créé: " + specialist.getNomDocteur());
                System.out.println("   Spécialité: " + specialist.getSpecialite());
                System.out.println("   Téléphone: " + specialist.getTelephone());
                System.out.println("   ID: " + specialist.getId());
            }
            
            // 3. Afficher les RDV assignés à ce spécialiste
            System.out.println("\n📅 3. RDV assignés à ce spécialiste:");
            showSpecialistRendezVous(specialist);
            
            // 4. Comment confirmer/annuler un RDV
            System.out.println("\n🔧 4. GESTION DES RDV:");
            System.out.println("Pour confirmer/annuler un RDV, utilisez:");
            System.out.println("   RendezVousDAO.updateRendezVousStatus(rd.getId(), \"confirmé\")");
            System.out.println("   RendezVousDAO.updateRendezVousStatus(rd.getId(), \"annulé\")");
            
        } catch (Exception e) {
            System.out.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== OPÉRATION TERMINÉE ===");
    }
    
    private static User createSpecialistUser() {
        try {
            // Créer un utilisateur avec rôle spécialiste
            User user = new User();
            user.setNomComplet("Dr. Chamem Houssem");
            user.setEmail("dr.chamem@biosync.com");
            user.setMotDePasse("Specialiste123"); // Mot de passe par défaut (première lettre majuscule)
            user.setRoles("specialiste");
            
            // Utiliser une connexion directe pour éviter les problèmes DAO
            String url = "jdbc:mysql://localhost:3306/biosync";
            String username = "root";
            String password = "";
            
            Connection conn = DriverManager.getConnection(url, username, password);
            
            // Insérer dans la table utilisateur
            String sql = "INSERT INTO utilisateur (nom_complet, email, mot_de_passe, roles) " +
                        "VALUES (?, ?, ?, ?)";
            
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, user.getNomComplet());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getMotDePasse());
            pstmt.setString(4, user.getRoles());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
                conn.close();
                return user;
            }
            
            conn.close();
            
        } catch (SQLException e) {
            System.out.println("❌ Erreur création utilisateur: " + e.getMessage());
        }
        
        return null;
    }
    
    private static Specialiste createSpecialistProfile(User user) {
        try {
            // Créer le profil spécialiste associé
            Specialiste specialist = new Specialiste();
            specialist.setNomDocteur(user.getNomComplet());
            specialist.setSpecialite("Médecin Généraliste");
            specialist.setTelephone("555-0123");
            specialist.setDisponibilite("Lundi-Vendredi 8h-18h");
            specialist.setUtilisateurId(user.getId());
            
            // Utiliser une connexion directe
            String url = "jdbc:mysql://localhost:3306/biosync";
            String username = "root";
            String password = "";
            
            Connection conn = DriverManager.getConnection(url, username, password);
            
            String sql = "INSERT INTO specialiste (nom_docteur, specialite, telephone, disponibilite, utilisateur_id) " +
                        "VALUES (?, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, specialist.getNomDocteur());
            pstmt.setString(2, specialist.getSpecialite());
            pstmt.setString(3, specialist.getTelephone());
            pstmt.setString(4, specialist.getDisponibilite());
            pstmt.setInt(5, specialist.getUtilisateurId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    specialist.setId(generatedKeys.getInt(1));
                }
                conn.close();
                return specialist;
            }
            
            conn.close();
            
        } catch (SQLException e) {
            System.out.println("❌ Erreur création spécialiste: " + e.getMessage());
        }
        
        return null;
    }
    
    private static void showSpecialistRendezVous(Specialiste specialist) {
        try {
            // Récupérer tous les RDV pour ce spécialiste
            List<RendezVous> rdvs = RendezVousDAO.getAllRendezVous();
            
            if (rdvs.isEmpty()) {
                System.out.println("   📭 Aucun RDV assigné pour l'instant");
                return;
            }
            
            System.out.println("   📋 " + rdvs.size() + " RDV trouvés:");
            
            for (int i = 0; i < rdvs.size(); i++) {
                RendezVous rdv = rdvs.get(i);
                System.out.println(String.format("   %d. 📅 %s - %s", 
                    i + 1,
                    rdv.getDateHeure().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    rdv.getMotif()
                ));
                System.out.println(String.format("      👤 Patient: %s | 📊 Statut: %s", 
                    rdv.getPatientNom() != null ? rdv.getPatientNom() : "Patient #" + rdv.getPatientId(),
                    rdv.getStatut()
                ));
                System.out.println("      🔧 Actions: Confirmer | Annuler | Modifier");
                System.out.println();
            }
            
        } catch (Exception e) {
            System.out.println("❌ Erreur récupération RDV: " + e.getMessage());
        }
    }
}
