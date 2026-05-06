package org.example.util;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserSession
 * Tests authentication state management and role checking
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserSessionTest {
    
    private UserSession session;
    
    @BeforeEach
    void setUp() {
        session = UserSession.getInstance();
        session.clearSession();
    }
    
    @AfterEach
    void tearDown() {
        session.clearSession();
    }
    
    @Test
    @DisplayName("Singleton Pattern - Should return same instance")
    void testSingletonPattern() {
        // Act
        UserSession instance1 = UserSession.getInstance();
        UserSession instance2 = UserSession.getInstance();
        
        // Assert
        assertSame(instance1, instance2, "Should return same instance");
    }
    
    @Test
    @DisplayName("Start Session - Should set current user and roles")
    void testStartSession() {
        // Arrange
        User user = createTestUser(1, "John Doe", "[\"ROLE_USER\", \"ROLE_COACH\"]");
        
        // Act
        session.startSession(user);
        
        // Assert
        assertTrue(session.isAuthenticated(), "Should be authenticated");
        assertNotNull(session.getCurrentUser(), "Should have current user");
        assertEquals("John Doe", session.getCurrentUserName(), "Name should match");
        assertEquals(1, session.getCurrentUserId(), "ID should match");
    }
    
    @Test
    @DisplayName("Clear Session - Should reset all values")
    void testClearSession() {
        // Arrange
        User user = createTestUser(1, "John Doe", "[\"ROLE_USER\"]");
        session.startSession(user);
        
        // Act
        session.clearSession();
        
        // Assert
        assertFalse(session.isAuthenticated(), "Should not be authenticated");
        assertNull(session.getCurrentUser(), "Should have no current user");
        assertEquals(-1, session.getCurrentUserId(), "ID should be -1");
        assertEquals("Guest", session.getCurrentUserName(), "Name should be Guest");
    }
    
    @Test
    @DisplayName("Has Role - Should check roles correctly")
    void testHasRole() {
        // Arrange
        User user = createTestUser(1, "Admin User", "[\"ROLE_ADMIN\", \"ROLE_USER\"]");
        session.startSession(user);
        
        // Assert
        assertTrue(session.hasRole("ADMIN"), "Should have ADMIN role");
        assertTrue(session.hasRole("USER"), "Should have USER role");
        assertFalse(session.hasRole("COACH"), "Should not have COACH role");
    }
    
    @Test
    @DisplayName("Is Admin - Should return true for admin users")
    void testIsAdmin() {
        // Arrange - Admin user
        User admin = createTestUser(1, "Admin", "[\"ROLE_ADMIN\"]");
        session.startSession(admin);
        
        // Assert
        assertTrue(session.isAdmin(), "Should be admin");
        assertFalse(session.isCoach(), "Should not be coach");
        assertFalse(session.isUser(), "Admin is not regular user");
    }
    
    @Test
    @DisplayName("Is Coach - Should return true for coach users")
    void testIsCoach() {
        // Arrange - Coach user
        User coach = createTestUser(2, "Coach", "[\"ROLE_COACH\"]");
        session.startSession(coach);
        
        // Assert
        assertFalse(session.isAdmin(), "Should not be admin");
        assertTrue(session.isCoach(), "Should be coach");
        assertTrue(session.isUser(), "Coach is also user");
    }
    
    @Test
    @DisplayName("Session Duration - Should track login time")
    void testSessionDuration() throws InterruptedException {
        // Arrange
        User user = createTestUser(1, "User", "[\"ROLE_USER\"]");
        session.startSession(user);
        
        // Wait a bit
        Thread.sleep(100);
        
        // Act
        long duration = session.getSessionDurationMinutes();
        
        // Assert
        assertEquals(0, duration, "Duration should be less than 1 minute");
    }
    
    @Test
    @DisplayName("Get Roles - Should return copy of roles")
    void testGetRoles() {
        // Arrange
        User user = createTestUser(1, "User", "[\"ROLE_USER\", \"ROLE_ADMIN\"]");
        session.startSession(user);
        
        // Act
        var roles = session.getRoles();
        
        // Assert
        assertNotNull(roles, "Roles should not be null");
        assertEquals(2, roles.size(), "Should have 2 roles");
        assertTrue(roles.contains("ROLE_USER") || roles.contains("USER"), "Should contain USER role");
        assertTrue(roles.contains("ROLE_ADMIN") || roles.contains("ADMIN"), "Should contain ADMIN role");
    }
    
    @Test
    @DisplayName("Null Roles - Should handle null gracefully")
    void testNullRoles() {
        // Arrange
        User user = createTestUser(1, "User", null);
        
        // Act
        session.startSession(user);
        
        // Assert
        assertTrue(session.isAuthenticated(), "Should still be authenticated");
        assertFalse(session.hasRole("ADMIN"), "Should not have any roles");
    }
    
    @Test
    @DisplayName("Empty Roles - Should handle empty string gracefully")
    void testEmptyRoles() {
        // Arrange
        User user = createTestUser(1, "User", "");
        
        // Act
        session.startSession(user);
        
        // Assert
        assertTrue(session.isAuthenticated(), "Should still be authenticated");
        assertTrue(session.getRoles().isEmpty(), "Should have empty roles");
    }
    
    // Helper method
    private User createTestUser(int id, String name, String roles) {
        User user = new User();
        user.setId(id);
        user.setNomComplet(name);
        user.setEmail(name.toLowerCase().replace(" ", ".") + "@example.com");
        user.setRoles(roles);
        return user;
    }
}
