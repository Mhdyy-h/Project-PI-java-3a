package org.example;

import javafx.application.Application;

public class TestMain {
    public static void main(String[] args) {
        System.out.println("Testing JavaFX Application...");
        
        try {
            // Test if JavaFX is available
            Class.forName("javafx.application.Application");
            System.out.println("JavaFX is available!");
            
            // Launch the application
            Application.launch(MainApplication.class, args);
            
        } catch (ClassNotFoundException e) {
            System.err.println("JavaFX is not available: " + e.getMessage());
            System.err.println("Please ensure JavaFX is properly configured in your IDE");
        } catch (Exception e) {
            System.err.println("Error launching application: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
