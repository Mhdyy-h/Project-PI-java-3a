package org.example.util;

import java.util.HashSet;
import java.util.Set;

/**
 * UserSession - Singleton class for managing user session state
 * Provides authentication status, role checking, and user data access
 */
public class UserSession {
    
    private static UserSession instance;
    private User currentUser;
    private Set<String> roles;
    private long loginTime;
    
    private UserSession() {
        this.roles = new HashSet<>();
    }
    
    /**
     * Get singleton instance
     * @return UserSession instance
     */
    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }
    
    /**
     * Start new session with user
     * @param user The authenticated user
     */
    public void startSession(User user) {
        this.currentUser = user;
        this.loginTime = System.currentTimeMillis();
        this.roles.clear();
        
        if (user != null && user.getRoles() != null) {
            parseRoles(user.getRoles());
        }
    }
    
    /**
     * Clear current session (logout)
     */
    public void clearSession() {
        this.currentUser = null;
        this.roles.clear();
        this.loginTime = 0;
    }
    
    /**
     * Check if user is authenticated
     * @return true if session is active
     */
    public boolean isAuthenticated() {
        return currentUser != null;
    }
    
    /**
     * Get current logged-in user
     * @return Current user or null
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Get current user ID
     * @return User ID or -1 if not logged in
     */
    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }
    
    /**
     * Get current user name
     * @return User name or "Guest"
     */
    public String getCurrentUserName() {
        return currentUser != null ? currentUser.getNomComplet() : "Guest";
    }
    
    /**
     * Check if current user has specific role
     * @param role Role to check
     * @return true if user has the role
     */
    public boolean hasRole(String role) {
        if (role == null || roles.isEmpty()) {
            return false;
        }
        return roles.contains(role.toUpperCase()) || roles.contains("ROLE_" + role.toUpperCase());
    }
    
    /**
     * Check if user is admin
     * @return true if admin role
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }
    
    /**
     * Check if user is coach
     * @return true if coach role
     */
    public boolean isCoach() {
        return hasRole("COACH") || hasRole("TRAINER");
    }
    
    /**
     * Check if user is regular user
     * @return true if user role
     */
    public boolean isUser() {
        return hasRole("USER") || (!isAdmin() && !isCoach());
    }
    
    /**
     * Get session duration in minutes
     * @return Session duration or 0 if not logged in
     */
    public long getSessionDurationMinutes() {
        if (loginTime == 0) {
            return 0;
        }
        return (System.currentTimeMillis() - loginTime) / (1000 * 60);
    }
    
    /**
     * Get all roles
     * @return Set of roles
     */
    public Set<String> getRoles() {
        return new HashSet<>(roles);
    }
    
    /**
     * Parse roles from JSON string
     * @param rolesJson JSON roles string
     */
    private void parseRoles(String rolesJson) {
        if (rolesJson == null || rolesJson.isEmpty()) {
            return;
        }
        
        // Remove brackets and quotes
        String cleaned = rolesJson.replace("[", "")
                                  .replace("]", "")
                                  .replace("\"", "")
                                  .replace("'", "");
        
        String[] roleArray = cleaned.split(",");
        for (String role : roleArray) {
            String trimmed = role.trim().toUpperCase();
            if (!trimmed.isEmpty()) {
                roles.add(trimmed);
            }
        }
    }
}
