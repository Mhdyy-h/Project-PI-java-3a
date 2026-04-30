package org.example.test;

import org.example.DatabaseConnection;
import org.example.dao.SpecialisteDAO;
import org.example.model.Specialiste;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Diagnostic test to check database connection and specialist data
 */
public class DatabaseDiagnosticTest {
    
    public static void main(String[] args) {
        System.out.println("🔍 DATABASE DIAGNOSTIC TEST");
        System.out.println("==========================\n");
        
        // Test 1: Database Connection
        System.out.println("1. Testing Database Connection...");
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Database connection successful");
                System.out.println("   Database: " + conn.getMetaData().getDatabaseProductName());
                System.out.println("   URL: " + conn.getMetaData().getURL());
                conn.close();
            } else {
                System.out.println("❌ Database connection failed");
                return;
            }
        } catch (SQLException e) {
            System.out.println("❌ Database connection error: " + e.getMessage());
            return;
        }
        System.out.println();
        
        // Test 2: Check if specialist table exists
        System.out.println("2. Checking Specialist Table...");
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet tables = conn.getMetaData().getTables(null, null, "specialiste", null);
            if (tables.next()) {
                System.out.println("✅ Specialist table exists");
            } else {
                System.out.println("❌ Specialist table does not exist");
                return;
            }
            tables.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println("❌ Error checking table: " + e.getMessage());
            return;
        }
        System.out.println();
        
        // Test 3: Direct SQL Query
        System.out.println("3. Testing Direct SQL Query...");
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM specialiste");
            
            if (rs.next()) {
                int count = rs.getInt("count");
                System.out.println("✅ Direct SQL query successful");
                System.out.println("   Total specialists in database: " + count);
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println("❌ Direct SQL query error: " + e.getMessage());
            return;
        }
        System.out.println();
        
        // Test 4: DAO Method
        System.out.println("4. Testing DAO getAllSpecialistes()...");
        try {
            List<Specialiste> specialists = SpecialisteDAO.getAllSpecialistes();
            System.out.println("✅ DAO method executed");
            System.out.println("   Specialists returned: " + specialists.size());
            
            if (specialists.isEmpty()) {
                System.out.println("⚠️  No specialists returned by DAO");
                
                // Test 5: Show what's actually in the table
                System.out.println("\n5. Showing raw table data...");
                try {
                    Connection conn = DatabaseConnection.getConnection();
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM specialiste LIMIT 5");
                    
                    boolean hasData = false;
                    while (rs.next()) {
                        hasData = true;
                        System.out.println("   Row " + rs.getRow() + ":");
                        System.out.println("     ID: " + rs.getInt("id"));
                        System.out.println("     Name: " + rs.getString("nom_docteur"));
                        System.out.println("     Specialty: " + rs.getString("specialite"));
                        System.out.println("     Email: " + rs.getString("email"));
                    }
                    
                    if (!hasData) {
                        System.out.println("   No data found in specialist table");
                    }
                    
                    rs.close();
                    stmt.close();
                    conn.close();
                } catch (SQLException e) {
                    System.out.println("❌ Error reading table data: " + e.getMessage());
                }
            } else {
                System.out.println("✅ Specialists found:");
                for (int i = 0; i < Math.min(3, specialists.size()); i++) {
                    Specialiste s = specialists.get(i);
                    System.out.println("   " + (i+1) + ". " + s.getNomDocteur() + " - " + s.getSpecialite());
                }
            }
        } catch (Exception e) {
            System.out.println("❌ DAO method error: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n🎯 Diagnosis complete!");
    }
}
