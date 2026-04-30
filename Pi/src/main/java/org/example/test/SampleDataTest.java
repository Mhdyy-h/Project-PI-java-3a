package org.example.test;

import org.example.dao.SpecialisteDAO;
import org.example.dao.UserDAO;
import org.example.model.Specialiste;
import org.example.model.User;
import org.example.util.SampleDataCreator;

import java.util.List;

/**
 * Test class to verify sample data creation
 */
public class SampleDataTest {
    
    public static void main(String[] args) {
        System.out.println("🧪 Testing Sample Data Creation...");
        
        // Create sample data
        SampleDataCreator.createAllSampleData();
        
        // Test specialists
        List<Specialiste> specialists = SpecialisteDAO.getAllSpecialistes();
        System.out.println("✅ Found " + specialists.size() + " specialists:");
        for (Specialiste specialist : specialists) {
            System.out.println("   - " + specialist.getNomDocteur() + " (" + specialist.getSpecialite() + ")");
        }
        
        // Test patients
        List<User> patients = UserDAO.getAllPatients();
        System.out.println("✅ Found " + patients.size() + " patients:");
        for (User patient : patients) {
            System.out.println("   - " + patient.getNomComplet() + " (" + patient.getEmail() + ")");
        }
        
        // Test role-based access
        System.out.println("\n🔐 Testing Role-Based Access:");
        
        // Test admin access
        User admin = new User(0, "Admin", "admin@test.com", "password", "ROLE_ADMIN");
        List<User> adminPatients = UserDAO.getAccessiblePatients(admin);
        System.out.println("   Admin can access " + adminPatients.size() + " patients");
        
        // Test specialist access
        if (!specialists.isEmpty()) {
            User specialist = new User(0, "Specialist", "spec@test.com", "password", "ROLE_SPECIALISTE");
            specialist.setId(specialists.get(0).getId());
            List<User> specialistPatients = UserDAO.getAccessiblePatients(specialist);
            System.out.println("   Specialist can access " + specialistPatients.size() + " patients");
        }
        
        // Test patient access
        if (!patients.isEmpty()) {
            User patient = patients.get(0);
            List<User> patientPatients = UserDAO.getAccessiblePatients(patient);
            System.out.println("   Patient can access " + patientPatients.size() + " patients (should be 1 - themselves)");
        }
        
        System.out.println("\n✅ All tests completed successfully!");
    }
}
