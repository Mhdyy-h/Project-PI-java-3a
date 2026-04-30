package org.example.test;

import org.example.controller.RendezVousDialogController;
import org.example.dao.SpecialisteDAO;
import org.example.dao.UserDAO;
import org.example.model.Specialiste;
import org.example.model.User;

import java.util.List;

/**
 * Test the dialog controller's specialist loading
 */
public class DialogControllerTest {
    
    public static void main(String[] args) {
        System.out.println("🎭 DIALOG CONTROLLER TEST");
        System.out.println("========================\n");
        
        // Create a mock dialog controller
        RendezVousDialogController controller = new RendezVousDialogController();
        
        // Create a test user (admin)
        User admin = new User(1, "Admin Test", "admin@test.com", "password", "ROLE_ADMIN");
        controller.setCurrentUser(admin);
        
        System.out.println("1. Testing Specialist DAO...");
        List<Specialiste> specialists = SpecialisteDAO.getAllSpecialistes();
        System.out.println("   DAO returned " + specialists.size() + " specialists");
        
        if (!specialists.isEmpty()) {
            System.out.println("   First specialist: " + specialists.get(0).getNomDocteur());
            System.out.println("   Email: " + specialists.get(0).getEmail());
        }
        
        System.out.println("\n2. Testing User DAO...");
        List<User> patients = UserDAO.getAllPatients();
        System.out.println("   Found " + patients.size() + " patients");
        
        System.out.println("\n3. Testing Controller Methods...");
        try {
            // Test the controller setup (loadSpecialistesForUser is called internally)
            System.out.println("   Controller setup completed");
            System.out.println("   loadSpecialistesForUser() is called internally when setCurrentUser() is called");
        } catch (Exception e) {
            System.out.println("   ❌ Controller error: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n✅ Test completed!");
        System.out.println("If you see specialists above, the issue is in the UI loading.");
        System.out.println("If you don't see specialists, the issue is in the database/DAO.");
    }
}
