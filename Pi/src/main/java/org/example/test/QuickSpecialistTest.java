package org.example.test;

import org.example.dao.SpecialisteDAO;
import org.example.model.Specialiste;
import org.example.util.SampleDataCreator;

import java.util.List;

/**
 * Quick test to check existing specialists in database
 */
public class QuickSpecialistTest {
    
    public static void main(String[] args) {
        System.out.println("🏥 QUICK SPECIALIST TEST");
        System.out.println("========================\n");
        
        // Check existing specialists
        System.out.println("Checking existing specialists in database...");
        System.out.println();
        
        // Display results
        List<Specialiste> specialists = SpecialisteDAO.getAllSpecialistes();
        System.out.println("✅ Found " + specialists.size() + " specialists in database:");
        System.out.println();
        
        for (int i = 0; i < specialists.size(); i++) {
            Specialiste s = specialists.get(i);
            System.out.println((i + 1) + ". " + s.getNomDocteur());
            System.out.println("   📧 " + s.getEmail());
            System.out.println("   📱 " + s.getTelephone());
            System.out.println("   🏥 " + s.getSpecialite());
            System.out.println("   📍 " + s.getAdresse());
            System.out.println("   ⏰ " + s.getDisponibilite());
            System.out.println();
        }
        
        System.out.println("🎉 Test completed! Specialists should now appear in the dropdown.");
    }
}
