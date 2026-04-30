package org.example.test;

import org.example.dao.SpecialisteDAO;
import org.example.dao.UserDAO;
import org.example.model.Specialiste;
import org.example.model.User;

import java.util.List;

/**
 * Complete diagnostic test to find why specialists aren't loading
 */
public class CompleteDiagnosticTest {
    
    public static void main(String[] args) {
        System.out.println("🔍 COMPLETE DIAGNOSTIC TEST");
        System.out.println("==========================\n");
        
        // Step 1: Test basic database connection
        System.out.println("1️⃣ Testing Database Connection...");
        try {
            List<Specialiste> allSpecialists = SpecialisteDAO.getAllSpecialistes();
            System.out.println("   ✅ Database connection successful");
            System.out.println("   📊 Found " + allSpecialists.size() + " specialists in database");
            
            if (allSpecialists.isEmpty()) {
                System.out.println("   ❌ No specialists found in database!");
                System.out.println("   💡 Solution: Add specialists to your database table");
                return;
            }
            
            // Show first few specialists
            System.out.println("   📋 First 3 specialists:");
            for (int i = 0; i < Math.min(3, allSpecialists.size()); i++) {
                Specialiste s = allSpecialists.get(i);
                System.out.println("      " + (i+1) + ". " + s.getNomDocteur() + " - " + s.getSpecialite());
                System.out.println("         Email: " + (s.getEmail() != null ? s.getEmail() : "NULL"));
                System.out.println("         Phone: " + s.getTelephone());
            }
            
        } catch (Exception e) {
            System.out.println("   ❌ Database error: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        System.out.println();
        
        // Step 2: Test user creation and role-based loading
        System.out.println("2️⃣ Testing User Roles...");
        try {
            // Create test users
            User admin = new User(1, "Admin User", "admin@test.com", "password", "ROLE_ADMIN");
            User patient = new User(2, "Patient User", "patient@test.com", "password", "ROLE_PATIENT");
            User specialist = new User(3, "Specialist User", "spec@test.com", "password", "ROLE_SPECIALISTE");
            
            System.out.println("   👤 Created test users:");
            System.out.println("      Admin: " + admin.isAdmin());
            System.out.println("      Patient: " + patient.isPatient());
            System.out.println("      Specialist: " + specialist.isSpecialiste());
            
            // Test role-based specialist loading
            System.out.println("\n   🔄 Testing role-based specialist loading:");
            
            // Test admin access
            List<Specialiste> adminSpecialists = SpecialisteDAO.getAllSpecialistes();
            System.out.println("      Admin can see: " + adminSpecialists.size() + " specialists");
            
            // Test patient access
            List<User> accessiblePatients = UserDAO.getAccessiblePatients(patient);
            System.out.println("      Patient can see: " + accessiblePatients.size() + " patients");
            
            // Test specialist access
            List<User> specialistPatients = UserDAO.getAccessiblePatients(specialist);
            System.out.println("      Specialist can see: " + specialistPatients.size() + " patients");
            
        } catch (Exception e) {
            System.out.println("   ❌ User role error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
        
        // Step 3: Test the exact data that would be loaded
        System.out.println("3️⃣ Testing Exact Loading Logic...");
        try {
            List<Specialiste> specialists = SpecialisteDAO.getAllSpecialistes();
            System.out.println("   📊 Specialists that would be loaded:");
            
            for (int i = 0; i < specialists.size(); i++) {
                Specialiste s = specialists.get(i);
                System.out.println("      " + (i+1) + ". Name: '" + s.getNomDocteur() + "'");
                System.out.println("         Specialty: '" + s.getSpecialite() + "'");
                System.out.println("         Email: '" + s.getEmail() + "'");
                System.out.println("         Phone: '" + s.getTelephone() + "'");
                System.out.println("         Available: '" + s.getDisponibilite() + "'");
                
                // Check for potential issues
                if (s.getNomDocteur() == null || s.getNomDocteur().trim().isEmpty()) {
                    System.out.println("         ⚠️  WARNING: Name is null or empty!");
                }
                if (s.getSpecialite() == null || s.getSpecialite().trim().isEmpty()) {
                    System.out.println("         ⚠️  WARNING: Specialty is null or empty!");
                }
            }
            
        } catch (Exception e) {
            System.out.println("   ❌ Loading logic error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
        
        // Step 4: Recommendations
        System.out.println("4️⃣ Recommendations:");
        System.out.println("   If specialists are found but not showing in dropdown:");
        System.out.println("   1. Check if FXML fx:id='specialisteComboBox' exists");
        System.out.println("   2. Check if controller is properly initialized");
        System.out.println("   3. Check if setCurrentUser() is being called");
        System.out.println("   4. Check if loadComboBoxData() is being executed");
        System.out.println("   5. Check for JavaFX initialization errors");
        
        System.out.println("\n🎯 Diagnostic completed!");
        System.out.println("If you see specialists above, the issue is in the UI/controller loading.");
        System.out.println("If you don't see specialists, the issue is in the database/DAO.");
    }
}
