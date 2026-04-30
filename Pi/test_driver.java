// Simple test to check if MySQL driver is available
public class test_driver {
    public static void main(String[] args) {
        System.out.println("=== MYSQL DRIVER TEST ===");
        
        try {
            // Test if MySQL driver class exists
            Class<?> driverClass = Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ MySQL driver class found: " + driverClass.getName());
            System.out.println("✅ MySQL driver is available in classpath");
            
            // Test if DatabaseConnection class exists
            Class<?> dbClass = Class.forName("org.example.DatabaseConnection");
            System.out.println("✅ DatabaseConnection class found: " + dbClass.getName());
            
            // Test if SpecialisteDAO class exists
            Class<?> daoClass = Class.forName("org.example.dao.SpecialisteDAO");
            System.out.println("✅ SpecialisteDAO class found: " + daoClass.getName());
            
            System.out.println("\n🎯 All required classes are available!");
            System.out.println("The issue might be in the database connection itself.");
            
        } catch (ClassNotFoundException e) {
            System.out.println("❌ Class not found: " + e.getMessage());
            System.out.println("💡 Make sure MySQL connector JAR is in your classpath");
            
            if (e.getMessage().contains("com.mysql.cj.jdbc.Driver")) {
                System.out.println("💡 Run: mvn compile to download dependencies");
                System.out.println("💡 Or add mysql-connector-j-8.4.0.jar to your classpath");
            }
        }
        
        System.out.println("\n=== TEST COMPLETED ===");
    }
}
