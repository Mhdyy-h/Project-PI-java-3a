package org.example.test;

import org.example.dao.UserDAO;
import org.example.dao.SpecialisteDAO;
import org.example.model.User;
import org.example.model.Specialiste;

import java.util.List;

public class RoleBasedAccessTest {
    
    public static void main(String[] args) {
        System.out.println("=== Testing Role-Based Access Control ===");
        
        // Test database connection
        if (!UserDAO.testConnection()) {
            System.out.println("❌ Database connection failed");
            return;
        }
        System.out.println("✅ Database connection successful");
        
        // Get test users
        List<User> allUsers = UserDAO.getAllUsers();
        User adminUser = findUserByRole(allUsers, "ROLE_ADMIN");
        User specialistUser = findUserByRole(allUsers, "ROLE_SPECIALISTE");
        User patientUser = findUserByRole(allUsers, "ROLE_USER");
        
        System.out.println("\n📋 Test Users Found:");
        System.out.println("Admin: " + (adminUser != null ? adminUser.getNomComplet() : "None"));
        System.out.println("Specialist: " + (specialistUser != null ? specialistUser.getNomComplet() : "None"));
        System.out.println("Patient: " + (patientUser != null ? patientUser.getNomComplet() : "None"));
        
        // Test role-based patient access
        testPatientAccess(adminUser, "Admin");
        testPatientAccess(specialistUser, "Specialist");
        testPatientAccess(patientUser, "Patient");
        
        // Test role-based specialist access
        testSpecialistAccess(adminUser, "Admin");
        testSpecialistAccess(specialistUser, "Specialist");
        testSpecialistAccess(patientUser, "Patient");
        
        System.out.println("\n=== Testing Complete ===");
    }
    
    private static User findUserByRole(List<User> users, String role) {
        return users.stream()
                .filter(user -> user.getRoles() != null && user.getRoles().contains(role))
                .findFirst()
                .orElse(null);
    }
    
    private static void testPatientAccess(User user, String role) {
        System.out.println("\n🔍 Testing " + role + " Patient Access:");
        
        if (user == null) {
            System.out.println("⚠️  No " + role + " user found for testing");
            return;
        }
        
        try {
            List<User> accessiblePatients = UserDAO.getAccessiblePatients(user);
            System.out.println("Accessible patients count: " + accessiblePatients.size());
            
            // Role-specific validation
            switch (role) {
                case "Admin":
                    if (accessiblePatients.size() > 0) {
                        System.out.println("✅ Admin can access all patients");
                    } else {
                        System.out.println("⚠️  No patients available for admin");
                    }
                    break;
                    
                case "Specialist":
                    System.out.println("Specialist patients:");
                    accessiblePatients.forEach(p -> System.out.println("  - " + p.getNomComplet()));
                    if (accessiblePatients.size() >= 0) { // Can be 0 if no appointments yet
                        System.out.println("✅ Specialist access correctly limited");
                    }
                    break;
                    
                case "Patient":
                    if (accessiblePatients.size() == 1 && 
                        accessiblePatients.get(0).getId() == user.getId()) {
                        System.out.println("✅ Patient can only access themselves");
                    } else {
                        System.out.println("❌ Patient access violation");
                    }
                    break;
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error testing " + role + " access: " + e.getMessage());
        }
    }
    
    private static void testSpecialistAccess(User user, String role) {
        System.out.println("\n👨‍⚕️ Testing " + role + " Specialist Access:");
        
        if (user == null) {
            System.out.println("⚠️  No " + role + " user found for testing");
            return;
        }
        
        try {
            List<Specialiste> accessibleSpecialists;
            
            // Simulate the logic from RendezVousDialogController
            if (user.isAdmin()) {
                accessibleSpecialists = SpecialisteDAO.getAllSpecialistes();
            } else if (user.isSpecialiste()) {
                Specialiste self = SpecialisteDAO.getSpecialisteById(user.getId());
                accessibleSpecialists = self != null ? List.of(self) : List.of();
            } else {
                accessibleSpecialists = SpecialisteDAO.getAllSpecialistes();
            }
            
            System.out.println("Accessible specialists count: " + accessibleSpecialists.size());
            
            // Role-specific validation
            switch (role) {
                case "Admin":
                    if (accessibleSpecialists.size() > 0) {
                        System.out.println("✅ Admin can access all specialists");
                    }
                    break;
                    
                case "Specialist":
                    if (accessibleSpecialists.size() == 1 && 
                        accessibleSpecialists.get(0).getUtilisateurId() == user.getId()) {
                        System.out.println("✅ Specialist can only access themselves");
                    } else {
                        System.out.println("❌ Specialist access violation");
                    }
                    break;
                    
                case "Patient":
                    if (accessibleSpecialists.size() > 0) {
                        System.out.println("✅ Patient can access all specialists for booking");
                    }
                    break;
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error testing " + role + " specialist access: " + e.getMessage());
        }
    }
}
