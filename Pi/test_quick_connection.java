// Quick connection test - copy and run this directly
import java.sql.*;

public class test_quick_connection {
    public static void main(String[] args) {
        System.out.println("=== QUICK CONNECTION TEST ===");
        
        try {
            // Test 1: Direct connection
            System.out.println("1. Testing direct MySQL connection...");
            String url = "jdbc:mysql://localhost:3306/biosync";
            String username = "root";
            String password = "";
            String driver = "com.mysql.cj.jdbc.Driver";
            
            Class.forName(driver);
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("   ✅ Direct connection successful");
            
            // Test 2: Check specialist table
            System.out.println("2. Testing specialist table...");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM specialiste");
            
            if (rs.next()) {
                int count = rs.getInt("count");
                System.out.println("   ✅ Found " + count + " specialists in database");
                
                if (count > 0) {
                    // Test 3: Show some specialists
                    System.out.println("3. Showing specialist data...");
                    rs = stmt.executeQuery("SELECT nom_docteur, specialite FROM specialiste LIMIT 3");
                    
                    while (rs.next()) {
                        String name = rs.getString("nom_docteur");
                        String specialty = rs.getString("specialite");
                        System.out.println("   - " + name + " (" + specialty + ")");
                    }
                    
                    System.out.println("   ✅ Database connection and data working!");
                } else {
                    System.out.println("   ❌ No specialists found in database");
                    System.out.println("   💡 Please add specialists to your database");
                }
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (ClassNotFoundException e) {
            System.out.println("❌ MySQL driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("❌ Database connection failed: " + e.getMessage());
            System.out.println("   💡 Check if MySQL server is running");
            System.out.println("   💡 Check if database 'biosync' exists");
            System.out.println("   💡 Check if user 'root' has access");
        }
        
        System.out.println("\n=== TEST COMPLETED ===");
    }
}
