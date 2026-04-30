package org.example.test;

import org.example.dao.SpecialisteDAO;
import org.example.model.Specialiste;

import java.util.List;

/**
 * Check existing specialists in your database
 */
public class CheckExistingSpecialists {
    
    public static void main(String[] args) {
        System.out.println("🏥 CHECKING YOUR EXISTING SPECIALISTS");
        System.out.println("===================================\n");
        
        try {
            List<Specialiste> specialists = SpecialisteDAO.getAllSpecialistes();
            
            System.out.println("Found " + specialists.size() + " specialists in your database:");
            System.out.println();
            
            if (specialists.isEmpty()) {
                System.out.println("❌ No specialists found!");
                System.out.println("   Make sure you have specialists in your database table.");
                return;
            }
            
            for (int i = 0; i < specialists.size(); i++) {
                Specialiste s = specialists.get(i);
                System.out.println((i + 1) + ". " + s.getNomDocteur());
                System.out.println("   🏥 Specialty: " + s.getSpecialite());
                System.out.println("   📧 Email: " + (s.getEmail() != null ? s.getEmail() : "No email"));
                System.out.println("   📱 Phone: " + s.getTelephone());
                System.out.println("   📍 Address: " + s.getAdresse());
                System.out.println("   ⏰ Availability: " + s.getDisponibilite());
                System.out.println();
            }
            
            System.out.println("✅ Your specialists are ready to load in the dropdown!");
            
        } catch (Exception e) {
            System.out.println("❌ Error loading specialists: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
