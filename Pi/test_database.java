// Simple test - copy this code and run it directly
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class SimpleSpecialist {
    String nomDocteur;
    String specialite;
    String email;
    
    public SimpleSpecialist(String nomDocteur, String specialite, String email) {
        this.nomDocteur = nomDocteur;
        this.specialite = specialite;
        this.email = email;
    }
    
    public String getNomDocteur() { return nomDocteur; }
    public String getSpecialite() { return specialite; }
    public String getEmail() { return email; }
}

public class test_database {
    public static void main(String[] args) {
        System.out.println("=== DIRECT DATABASE TEST ===");
        
        Connection conn = null;
        try {
            // Step 1: Test database connection
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:database.db");
            System.out.println("✅ Database connected");
            
            // Step 2: Test if table exists
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "specialiste", null);
            if (tables.next()) {
                System.out.println("✅ Specialist table exists");
            } else {
                System.out.println("❌ Specialist table does not exist");
                return;
            }
            
            // Step 3: Test data
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM specialiste");
            int count = 0;
            if (rs.next()) {
                count = rs.getInt("count");
            }
            System.out.println("✅ Found " + count + " specialists");
            
            if (count == 0) {
                System.out.println("❌ No specialists in database!");
                return;
            }
            
            // Step 4: Show actual data
            rs = stmt.executeQuery("SELECT nom_docteur, specialite, email FROM specialiste LIMIT 5");
            List<SimpleSpecialist> specialists = new ArrayList<>();
            
            while (rs.next()) {
                String name = rs.getString("nom_docteur");
                String specialty = rs.getString("specialite");
                String email = rs.getString("email");
                specialists.add(new SimpleSpecialist(name, specialty, email));
            }
            
            System.out.println("\n📋 Specialist Data:");
            for (int i = 0; i < specialists.size(); i++) {
                SimpleSpecialist s = specialists.get(i);
                System.out.println((i+1) + ". " + s.getNomDocteur());
                System.out.println("   Specialty: " + s.getSpecialite());
                System.out.println("   Email: " + s.getEmail());
            }
            
            System.out.println("\n🎯 RESULT: Database test PASSED");
            System.out.println("If you see this data, your database is working correctly.");
            System.out.println("The issue is in the JavaFX UI loading, not the database.");
            
        } catch (Exception e) {
            System.out.println("❌ Database test FAILED: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (Exception e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
