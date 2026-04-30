package org.example.test;

import org.example.dao.SpecialisteDAO;
import org.example.model.Specialiste;
import org.example.util.SampleDataCreator;

import java.util.List;

/**
 * Test class to verify existing specialists in database
 */
public class SpecialistCreationTest {
    
    public static void main(String[] args) {
        System.out.println("🧪 Testing Specialist Creation...\n");
        
        // Check existing specialists
        System.out.println("=== CHECKING EXISTING SPECIALISTS ===");
        System.out.println();
        
        // Then verify they were created
        System.out.println("=== VERIFYING SPECIALISTS ===");
        List<Specialiste> specialists = SpecialisteDAO.getAllSpecialistes();
        
        System.out.println("Total specialists found: " + specialists.size());
        System.out.println();
        
        if (specialists.isEmpty()) {
            System.out.println("❌ No specialists found in database!");
            return;
        }
        
        System.out.println("📋 Specialist List:");
        for (int i = 0; i < specialists.size(); i++) {
            Specialiste specialist = specialists.get(i);
            System.out.println((i + 1) + ". " + specialist.getNomDocteur());
            System.out.println("   Specialty: " + specialist.getSpecialite());
            System.out.println("   Phone: " + specialist.getTelephone());
            System.out.println("   Email: " + specialist.getEmail());
            System.out.println("   Address: " + specialist.getAdresse());
            System.out.println("   Availability: " + specialist.getDisponibilite());
            System.out.println("   City: " + specialist.getVille());
            System.out.println();
        }
        
        System.out.println("✅ Specialist creation test completed!");
    }
}
