package org.example.util;

import org.example.dao.SpecialisteDAO;
import org.example.dao.UserDAO;
import org.example.model.Specialiste;
import org.example.model.User;


/**
 * Utility class to create sample data for testing
 */
public class SampleDataCreator {
    
    public static void createSampleSpecialists() {
        System.out.println("ℹ️ Using existing specialists from database (no sample creation)");
    }
    
    public static void createSampleSpecialists(boolean forceRecreate) {
        System.out.println("ℹ️ Using existing specialists from database (no sample creation)");
    }
    
    public static void createSamplePatients() {
        // Check if patients already exist
        if (UserDAO.getAllPatients().size() > 3) {
            return;
        }
        
        // Create sample patients
        User[] patients = {
            new User(0, "Alice Martin", "alice.martin@email.com", "password123", "ROLE_PATIENT"),
            new User(0, "Bernard Dupont", "bernard.dupont@email.com", "password123", "ROLE_PATIENT"),
            new User(0, "Claire Leroy", "claire.leroy@email.com", "password123", "ROLE_PATIENT"),
            new User(0, "David Moreau", "david.moreau@email.com", "password123", "ROLE_PATIENT"),
            new User(0, "Emma Laurent", "emma.laurent@email.com", "password123", "ROLE_PATIENT"),
            new User(0, "François Bernard", "francois.bernard@email.com", "password123", "ROLE_PATIENT"),
            new User(0, "Guillaume Rousseau", "guillaume.rousseau@email.com", "password123", "ROLE_PATIENT"),
            new User(0, "Hélène Petit", "helene.petit@email.com", "password123", "ROLE_PATIENT")
        };
        
        for (User patient : patients) {
            UserDAO.insertUser(patient);
        }
        
        System.out.println("✅ Created " + patients.length + " sample patients");
    }
    
    public static void createAllSampleData() {
        System.out.println("🚀 Creating sample data...");
        createSampleSpecialists();
        createSamplePatients();
        System.out.println("✅ Sample data creation completed!");
    }
}
