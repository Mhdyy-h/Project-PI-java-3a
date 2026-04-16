package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.example.dao.UserDAO;
import org.example.model.User;

public class UserController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private Label statusLabel;

    @FXML
    public void testDatabaseConnection() {
        if (UserDAO.testConnection()) {
            statusLabel.setText("✅ Database connection successful!");
            statusLabel.setStyle("-fx-text-fill: #10b981;");
        } else {
            statusLabel.setText("❌ Database connection failed!");
            statusLabel.setStyle("-fx-text-fill: #ef4444;");
        }
    }

    @FXML
    public void saveUser() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();

        if (username.isEmpty() || email.isEmpty()) {
            statusLabel.setText("Veuillez remplir tous les champs!");
            statusLabel.setStyle("-fx-text-fill: #f59e0b;");
            return;
        }

        User user = new User(0, username, email, "default123");
        user.setRoles("UTILISATEUR");

        if (UserDAO.insertUser(user)) {
            statusLabel.setText("Utilisateur enregistré !");
            statusLabel.setStyle("-fx-text-fill: #10b981;");
            usernameField.clear();
            emailField.clear();
        } else {
            statusLabel.setText("Erreur lors de la sauvegarde.");
            statusLabel.setStyle("-fx-text-fill: #ef4444;");
        }
    }
}