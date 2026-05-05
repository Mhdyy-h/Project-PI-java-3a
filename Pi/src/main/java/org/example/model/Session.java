package org.example.model;

public class Session {
    public static String role = "";

    public static boolean isCoach() {
        return "COACH".equalsIgnoreCase(role);
    }

    public static boolean isUser() {
        return "USER".equalsIgnoreCase(role);
    }
}