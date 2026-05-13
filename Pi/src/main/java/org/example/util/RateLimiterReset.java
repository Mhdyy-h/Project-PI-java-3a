package org.example.util;

import org.example.dao.RateLimitingDAO;
import org.example.service.RateLimiterService;

/**
 * Utility class to reset rate limiter
 * Run this main method to clear ALL rate limiter data
 */
public class RateLimiterReset {
    
    public static void main(String[] args) {
        RateLimiterService rateLimiter = RateLimiterService.getInstance();
        
        System.out.println("🔄 Clearing ALL rate limiter data...");
        
        try {
            // Clear ALL rate limiter data from database
            boolean success = RateLimitingDAO.clearAllAttempts();
            
            if (success) {
                System.out.println("✅ ALL rate limiter data cleared successfully!");
                System.out.println("🎉 All users can now try logging in again!");
                
                // Show blocked users before and after
                var blockedUsers = rateLimiter.getBlockedUsers();
                System.out.println("📊 Currently blocked users: " + blockedUsers.size());
                
                if (blockedUsers.isEmpty()) {
                    System.out.println("✨ No users are currently blocked!");
                } else {
                    System.out.println("� Blocked users list:");
                    blockedUsers.forEach((email, info) -> {
                        System.out.println("   - " + email + " (" + info.attemptCount + " attempts, " + info.remainingMinutes + " min remaining)");
                    });
                }
                
            } else {
                System.out.println("❌ Failed to clear rate limiter data");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error clearing rate limiter: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n� You can now restart your application and try logging in with any account!");
    }
}
