// Test table structure - copy and run this directly
import java.sql.*;

public class test_table_structure {
    public static void main(String[] args) {
        System.out.println("=== TABLE STRUCTURE TEST ===");
        
        try {
            String url = "jdbc:mysql://localhost:3306/biosync";
            String username = "root";
            String password = "";
            String driver = "com.mysql.cj.jdbc.Driver";
            
            Class.forName(driver);
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("✅ Connected to database");
            
            // Check if specialist table exists
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "specialiste", null);
            
            if (tables.next()) {
                System.out.println("✅ Specialist table exists");
                
                // Get table structure
                ResultSet columns = meta.getColumns(null, null, "specialiste", null);
                System.out.println("\n📋 Specialist table columns:");
                
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String columnType = columns.getString("TYPE_NAME");
                    int columnSize = columns.getInt("COLUMN_SIZE");
                    System.out.println("   - " + columnName + " (" + columnType + ", " + columnSize + ")");
                }
                
                // Test a simple query
                System.out.println("\n🧪 Testing simple query...");
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM specialiste LIMIT 1");
                
                if (rs.next()) {
                    System.out.println("✅ Query successful");
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int columnCount = rsmd.getColumnCount();
                    
                    System.out.println("📊 Sample data:");
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = rsmd.getColumnName(i);
                        String value = rs.getString(i);
                        System.out.println("   " + columnName + ": " + value);
                    }
                } else {
                    System.out.println("⚠️  No data in specialist table");
                }
                
                rs.close();
                stmt.close();
                
            } else {
                System.out.println("❌ Specialist table does not exist");
                System.out.println("💡 Please create the specialist table");
            }
            
            tables.close();
            conn.close();
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== TEST COMPLETED ===");
    }
}
