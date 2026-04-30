package org.example.test;

import org.example.dao.SpecialisteDAO;
import org.example.model.Specialiste;

import java.util.List;

/**
 * Simple database test - just copy and run this main method
 */
public class SimpleDatabaseTest {
    
    public static void main(String[] args) {
        System.out.println("=== SIMPLE DATABASE TEST ===");
        
        try {
            // Test 1: Get all specialists
            List<Specialiste> specialists = SpecialisteDAO.getAllSpecialistes();
            
            System.out.println("Database connection: SUCCESS");
            System.out.println("Total specialists found: " + specialists.size());
            
            if (specialists.isEmpty()) {
                System.out.println("ERROR: No specialists in database!");
                System.out.println("Please add specialists to your database table.");
                return;
            }
            
            // Test 2: Show specialist details
            System.out.println("\nSpecialist Details:");
            for (int i = 0; i < specialists.size(); i++) {
                Specialiste s = specialists.get(i);
                System.out.println((i+1) + ". Name: " + s.getNomDocteur());
                System.out.println("   Specialty: " + s.getSpecialite());
                System.out.println("   Email: " + s.getEmail());
                System.out.println("   Phone: " + s.getTelephone());
                System.out.println("   Address: " + s.getAdresse());
                System.out.println();
            }
            
            System.out.println("RESULT: Database contains " + specialists.size() + " specialists");
            System.out.println("If you see this data, the issue is in the UI loading, not the database.");
            
        } catch (Exception e) {
            System.out.println("Database connection: FAILED");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
