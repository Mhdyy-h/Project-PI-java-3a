package org.example.test;

import org.example.dao.SpecialisteDAO;
import org.example.model.Specialiste;

import java.util.List;

/**
 * Simple debug test for specialist loading
 */
public class DebugSpecialistLoading {
    
    public static void main(String[] args) {
        System.out.println("🔧 DEBUG SPECIALIST LOADING");
        System.out.println("========================\n");
        
        try {
            System.out.println("Step 1: Calling SpecialisteDAO.getAllSpecialistes()...");
            List<Specialiste> specialists = SpecialisteDAO.getAllSpecialistes();
            
            System.out.println("Step 2: Method returned successfully");
            System.out.println("        Result size: " + specialists.size());
            
            if (specialists.isEmpty()) {
                System.out.println("Step 3: ❌ No specialists found!");
                System.out.println("        This means either:");
                System.out.println("        - Database table is empty");
                System.out.println("        - SQL query is failing");
                System.out.println("        - Connection issue");
            } else {
                System.out.println("Step 3: ✅ Found specialists!");
                System.out.println("        First few specialists:");
                
                for (int i = 0; i < Math.min(3, specialists.size()); i++) {
                    Specialiste s = specialists.get(i);
                    System.out.println("          " + (i+1) + ". " + s.getNomDocteur());
                    System.out.println("             Specialty: " + s.getSpecialite());
                    System.out.println("             Email: " + s.getEmail());
                    System.out.println("             Phone: " + s.getTelephone());
                    
                    // Test if the name method works
                    if (s.getNomDocteur() == null || s.getNomDocteur().isEmpty()) {
                        System.out.println("             ⚠️  WARNING: Name is null or empty!");
                    }
                }
                
                System.out.println("\nStep 4: ✅ Data looks good!");
                System.out.println("        If dropdown is still empty, the issue is in:");
                System.out.println("        - JavaFX UI initialization");
                System.out.println("        - ComboBox setup");
                System.out.println("        - Controller not being called properly");
            }
            
        } catch (Exception e) {
            System.out.println("Step 3: ❌ Exception occurred!");
            System.out.println("        Error: " + e.getMessage());
            System.out.println("        Type: " + e.getClass().getSimpleName());
            e.printStackTrace();
        }
        
        System.out.println("\n🎯 Debug completed!");
    }
}
