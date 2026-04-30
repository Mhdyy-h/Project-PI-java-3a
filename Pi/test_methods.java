import org.example.model.User;
import org.example.dao.UserDAO;
import java.util.List;

public class test_methods {
    public static void main(String[] args) {
        // Test User methods
        User u = new User();
        u.setRoles("[\"ROLE_USER\"]");
        
        System.out.println("isAdmin: " + u.isAdmin());
        System.out.println("isSpecialiste: " + u.isSpecialiste()); 
        System.out.println("isPatient: " + u.isPatient());
        
        // Test UserDAO method
        List<User> patients = UserDAO.getAccessiblePatients(u);
        System.out.println("getAccessiblePatients works: " + patients.size());
        
        System.out.println("All methods exist and work!");
    }
}
