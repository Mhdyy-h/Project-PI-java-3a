package org.example.test;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Test that mimics the exact DAO connection behavior
 */
public class ConnectionTest {
    
    // Exact same method as in SpecialisteDAO
    private static Connection getFreshConnection() throws SQLException {
        try {
            String url = "jdbc:mysql://localhost:3306/biosync";
            String username = "root";
            String password = ""; // Empty password as per config
            String driver = "com.mysql.cj.jdbc.Driver";
            
            Class.forName(driver);
            return DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL driver not found", e);
        }
    }
    
    // Exact same method as in SpecialisteDAO.getAllSpecialistes()
    public static List<String> getAllSpecialists() {
        List<String> specialistesList = new ArrayList<>();
        String sql = "SELECT nom_docteur FROM specialiste ORDER BY nom_docteur";
        
        try (Connection conn = getFreshConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                specialistesList.add(rs.getString("nom_docteur"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all specialists: " + e.getMessage());
            e.printStackTrace();
        }
        
        return specialistesList;
    }
    
    public static void main(String[] args) {
        System.out.println("=== DAO CONNECTION TEST ===");
        
        try {
            // Test the exact same connection method
            System.out.println("1. Testing getFreshConnection()...");
            Connection conn = getFreshConnection();
            System.out.println("   ✅ getFreshConnection() successful");
            conn.close();
            
            // Test the exact same getAllSpecialists method
            System.out.println("2. Testing getAllSpecialists()...");
            List<String> specialists = getAllSpecialists();
            System.out.println("   ✅ getAllSpecialists() returned " + specialists.size() + " specialists");
            
            if (!specialists.isEmpty()) {
                System.out.println("   📋 First few specialists:");
                for (int i = 0; i < Math.min(3, specialists.size()); i++) {
                    System.out.println("      " + (i+1) + ". " + specialists.get(i));
                }
                System.out.println("   ✅ DAO methods working correctly!");
            } else {
                System.out.println("   ❌ No specialists found");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== TEST COMPLETED ===");
    }
}
