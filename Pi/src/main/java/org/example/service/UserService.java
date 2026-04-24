package org.example.service;

import org.example.model.User;

public class UserService {

    private static UserService instance;

    private UserService() {}

    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    public static String extractDisplayRole(String roles) {
        if (roles == null) return "UTILISATEUR";
        if (roles.contains("ADMIN")) return "ADMIN";
        if (roles.contains("COACH")) return "COACH";
        if (roles.contains("SPECIALISTE")) return "SPÉCIALISTE";
        return "UTILISATEUR";
    }

    public static String getAvatarRoleClass(String roles) {
        if (roles == null) return "avatar-default";
        if (roles.contains("ADMIN")) return "avatar-admin";
        if (roles.contains("COACH")) return "avatar-coach";
        if (roles.contains("SPECIALISTE")) return "avatar-specialiste";
        return "avatar-default";
    }

    public static String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }
}
