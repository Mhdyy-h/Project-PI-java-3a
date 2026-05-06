package org.example.controller;

import org.example.model.User;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.example.dao.UserDAO;

public class UserController {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    public void testDatabaseConnection() {
        try {
            boolean isConnected = UserDAO.testConnection();
            if (isConnected) {
                statusLabel.setText("✅ Database connection successful!");
                statusLabel.setStyle("-fx-text-fill: green;");
            } else {
                statusLabel.setText("❌ Database connection failed!");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    @FXML
    public void saveUser() {
        String username = usernameField.getText();
        String email = emailField.getText();
        
        if (username.isEmpty() || email.isEmpty()) {
            statusLabel.setText("Please fill all fields!");
            statusLabel.setStyle("-fx-text-fill: orange;");
            return;
        }
        
        // TODO: Implement save user logic
        statusLabel.setText("User saved: " + username + " (" + email + ")");
        statusLabel.setStyle("-fx-text-fill: green;");
        
        // Clear fields
        usernameField.clear();
        emailField.clear();
    }
}

