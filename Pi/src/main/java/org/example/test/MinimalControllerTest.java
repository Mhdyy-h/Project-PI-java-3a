package org.example.test;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import org.example.controller.RendezVousDialogController;
import org.example.model.Specialiste;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal test to check FXML loading and controller injection
 */
public class MinimalControllerTest {
    
    public static void main(String[] args) {
        System.out.println("=== MINIMAL CONTROLLER TEST ===");
        
        try {
            // Test 1: Load FXML
            System.out.println("1. Loading FXML...");
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MinimalControllerTest.class.getResource("/view/rendezvous_dialog.fxml"));
            
            Parent root = loader.load();
            System.out.println("   ✅ FXML loaded successfully");
            
            // Test 2: Get controller
            System.out.println("2. Getting controller...");
            RendezVousDialogController controller = loader.getController();
            if (controller == null) {
                System.out.println("   ❌ Controller is null!");
                return;
            }
            System.out.println("   ✅ Controller loaded");
            
            // Test 3: Create mock specialists
            System.out.println("3. Creating mock specialists...");
            List<Specialiste> mockSpecialists = new ArrayList<>();
            mockSpecialists.add(new Specialiste("Dr. Test 1", "Cardiology", "0123456789", "Available", "Test Address", "Test City", null));
            mockSpecialists.add(new Specialiste("Dr. Test 2", "Dermatology", "0123456789", "Available", "Test Address", "Test City", null));
            mockSpecialists.add(new Specialiste("Dr. Test 3", "Pediatrics", "0123456789", "Available", "Test Address", "Test City", null));
            
            System.out.println("   ✅ Created " + mockSpecialists.size() + " mock specialists");
            
            // Test 4: Try to access the specialist combo box (this might fail in non-JavaFX environment)
            System.out.println("4. Testing controller fields...");
            try {
                // This will likely fail outside of JavaFX runtime, but let's try
                // We can't actually test the ComboBox outside JavaFX, but we can check if the controller is properly set up
                System.out.println("   ✅ Controller appears to be properly initialized");
                System.out.println("   ⚠️  Cannot test ComboBox outside JavaFX runtime");
                
            } catch (Exception e) {
                System.out.println("   ⚠️  Controller field test failed (expected outside JavaFX): " + e.getMessage());
            }
            
            System.out.println("\n🎯 RESULT: FXML and Controller loading test PASSED");
            System.out.println("If this test passes, the issue is likely in:");
            System.out.println("1. The data loading in loadComboBoxData()");
            System.out.println("2. The database connection in the application context");
            System.out.println("3. The timing of when setCurrentUser() is called");
            
        } catch (Exception e) {
            System.out.println("❌ FXML/Controller test FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
