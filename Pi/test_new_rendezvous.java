// Test creating a new rendezvous directly
import java.sql.*;
import java.time.LocalDateTime;

public class test_new_rendezvous {
    public static void main(String[] args) {
        System.out.println("=== TEST NEW RENDEZVOUS CREATION ===");
        
        try {
            String url = "jdbc:mysql://localhost:3306/biosync";
            String username = "root";
            String password = "";
            String driver = "com.mysql.cj.jdbc.Driver";
            
            Class.forName(driver);
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("✅ Connected to database");
            
            // Check current count
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM rendez_vous");
            int oldCount = 0;
            if (rs.next()) {
                oldCount = rs.getInt("count");
                System.out.println("📊 Current rendezvous count: " + oldCount);
            }
            rs.close();
            
            // Try to create a new rendezvous
            System.out.println("\n🧪 Creating new rendezvous...");
            String sql = "INSERT INTO rendez_vous (date_heure, motif, statut, mode, patient_id, specialiste_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, LocalDateTime.now().plusDays(1).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            pstmt.setString(2, "Test motif from direct SQL");
            pstmt.setString(3, "en attente");
            pstmt.setString(4, "téléconsultation");
            pstmt.setInt(5, 1); // patient_id
            pstmt.setInt(6, 1); // specialiste_id
            
            int affectedRows = pstmt.executeUpdate();
            System.out.println("📝 Affected rows: " + affectedRows);
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int newId = generatedKeys.getInt(1);
                    System.out.println("✅ New rendezvous created with ID: " + newId);
                }
            }
            
            pstmt.close();
            
            // Check new count
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM rendez_vous");
            if (rs.next()) {
                int newCount = rs.getInt("count");
                System.out.println("📊 New rendezvous count: " + newCount);
                
                if (newCount > oldCount) {
                    System.out.println("✅ SUCCESS: New rendezvous was created!");
                } else {
                    System.out.println("❌ ISSUE: Count didn't increase");
                }
            }
            rs.close();
            
            // Show all rendezvous
            System.out.println("\n📋 All rendezvous:");
            rs = stmt.executeQuery("SELECT id, date_heure, motif, mode, statut FROM rendez_vous ORDER BY id DESC LIMIT 5");
            while (rs.next()) {
                System.out.println("   ID: " + rs.getInt("id") + 
                                 ", Date: " + rs.getString("date_heure") +
                                 ", Motif: " + rs.getString("motif") +
                                 ", Mode: " + rs.getString("mode") +
                                 ", Statut: " + rs.getString("statut"));
            }
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== TEST COMPLETED ===");
    }
}
