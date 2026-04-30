package org.example.test;

import org.example.dao.RendezVousDAO;
import org.example.dao.UserDAO;
import org.example.dao.SpecialisteDAO;
import org.example.model.RendezVous;
import org.example.model.User;
import org.example.model.Specialiste;

import java.time.LocalDateTime;
import java.util.List;

public class RendezVousTest {
    
    public static void main(String[] args) {
        System.out.println("=== Testing RendezVous System ===");
        
        // Test database connection
        testDatabaseConnection();
        
        // Test table creation
        testTableCreation();
        
        // Test CRUD operations
        testCRUDOperations();
        
        // Test validation
        testValidation();
        
        // Test search functionality
        testSearchFunctionality();
        
        System.out.println("=== Testing Complete ===");
    }
    
    private static void testDatabaseConnection() {
        System.out.println("\n1. Testing Database Connection...");
        
        boolean userConnection = UserDAO.testConnection();
        boolean specialistConnection = SpecialisteDAO.testConnection();
        boolean rdvConnection = RendezVousDAO.testConnection();
        
        System.out.println("User DAO Connection: " + (userConnection ? "✅ SUCCESS" : "❌ FAILED"));
        System.out.println("Specialist DAO Connection: " + (specialistConnection ? "✅ SUCCESS" : "❌ FAILED"));
        System.out.println("RendezVous DAO Connection: " + (rdvConnection ? "✅ SUCCESS" : "❌ FAILED"));
    }
    
    private static void testTableCreation() {
        System.out.println("\n2. Testing Table Creation...");
        try {
            RendezVousDAO.createTableIfNotExists();
            System.out.println("✅ Table creation test completed");
        } catch (Exception e) {
            System.out.println("❌ Table creation failed: " + e.getMessage());
        }
    }
    
    private static void testCRUDOperations() {
        System.out.println("\n3. Testing CRUD Operations...");
        
        try {
            // Test getting patients and specialists
            List<User> patients = UserDAO.getAllPatients();
            List<Specialiste> specialists = SpecialisteDAO.getAllSpecialistes();
            
            System.out.println("Found " + patients.size() + " patients");
            System.out.println("Found " + specialists.size() + " specialists");
            
            if (patients.isEmpty() || specialists.isEmpty()) {
                System.out.println("⚠️  Need at least 1 patient and 1 specialist to test CRUD operations");
                return;
            }
            
            // Test creating a rendezvous
            RendezVous testRdv = new RendezVous();
            testRdv.setDateHeure(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
            testRdv.setMotif("Test rendez-vous from unit test");
            testRdv.setStatut("en attente");
            testRdv.setMode("présentiel");
            testRdv.setLieu("Cabinet Test");
            testRdv.setNiveauUrgence(1);
            testRdv.setPatientId(patients.get(0).getId());
            testRdv.setSpecialisteId(specialists.get(0).getId());
            
            boolean created = RendezVousDAO.createRendezVous(testRdv);
            System.out.println("Create rendezvous: " + (created ? "✅ SUCCESS" : "❌ FAILED"));
            
            if (created && testRdv.getId() != null) {
                // Test reading
                RendezVous retrieved = RendezVousDAO.getRendezVousById(testRdv.getId());
                System.out.println("Read rendezvous: " + (retrieved != null ? "✅ SUCCESS" : "❌ FAILED"));
                
                // Test updating
                retrieved.setMotif("Updated test rendez-vous");
                boolean updated = RendezVousDAO.updateRendezVous(retrieved);
                System.out.println("Update rendezvous: " + (updated ? "✅ SUCCESS" : "❌ FAILED"));
                
                // Test deleting
                boolean deleted = RendezVousDAO.deleteRendezVous(testRdv.getId());
                System.out.println("Delete rendezvous: " + (deleted ? "✅ SUCCESS" : "❌ FAILED"));
            }
            
        } catch (Exception e) {
            System.out.println("❌ CRUD test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testValidation() {
        System.out.println("\n4. Testing Validation Logic...");
        
        try {
            List<User> patients = UserDAO.getAllPatients();
            List<Specialiste> specialists = SpecialisteDAO.getAllSpecialistes();
            
            if (patients.isEmpty() || specialists.isEmpty()) {
                System.out.println("⚠️  Cannot test validation without patients and specialists");
                return;
            }
            
            // Test conflict detection
            LocalDateTime testTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
            boolean hasConflict1 = RendezVousDAO.hasConflict(specialists.get(0).getId(), testTime, null);
            System.out.println("Conflict detection (no existing): " + (!hasConflict1 ? "✅ SUCCESS" : "❌ FAILED"));
            
            // Create a test appointment
            RendezVous testRdv = new RendezVous();
            testRdv.setDateHeure(testTime);
            testRdv.setMotif("Conflict test");
            testRdv.setStatut("confirmé");
            testRdv.setMode("présentiel");
            testRdv.setPatientId(patients.get(0).getId());
            testRdv.setSpecialisteId(specialists.get(0).getId());
            
            boolean created = RendezVousDAO.createRendezVous(testRdv);
            if (created) {
                // Test conflict detection with existing appointment
                boolean hasConflict2 = RendezVousDAO.hasConflict(specialists.get(0).getId(), testTime, null);
                System.out.println("Conflict detection (with existing): " + (hasConflict2 ? "✅ SUCCESS" : "❌ FAILED"));
                
                // Test conflict detection excluding the same appointment
                boolean hasConflict3 = RendezVousDAO.hasConflict(specialists.get(0).getId(), testTime, testRdv.getId());
                System.out.println("Conflict detection (excluding self): " + (!hasConflict3 ? "✅ SUCCESS" : "❌ FAILED"));
                
                // Clean up
                RendezVousDAO.deleteRendezVous(testRdv.getId());
            }
            
        } catch (Exception e) {
            System.out.println("❌ Validation test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testSearchFunctionality() {
        System.out.println("\n5. Testing Search Functionality...");
        
        try {
            // Test search with no filters
            List<RendezVous> allRdv = RendezVousDAO.getAllRendezVous();
            System.out.println("Get all rendezvous: ✅ SUCCESS (" + allRdv.size() + " found)");
            
            // Test search with keyword
            List<RendezVous> searchResults = RendezVousDAO.searchRendezVous("test", null, null);
            System.out.println("Search by keyword: ✅ SUCCESS (" + searchResults.size() + " found)");
            
            // Test search with status
            List<RendezVous> statusResults = RendezVousDAO.searchRendezVous(null, "en attente", null);
            System.out.println("Search by status: ✅ SUCCESS (" + statusResults.size() + " found)");
            
            // Test search with specialist
            List<Specialiste> specialists = SpecialisteDAO.getAllSpecialistes();
            if (!specialists.isEmpty()) {
                List<RendezVous> specialistResults = RendezVousDAO.searchRendezVous(null, null, specialists.get(0).getId());
                System.out.println("Search by specialist: ✅ SUCCESS (" + specialistResults.size() + " found)");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Search test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
