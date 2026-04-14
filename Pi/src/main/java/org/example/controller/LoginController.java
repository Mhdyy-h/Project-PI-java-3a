package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.dao.UserDAO;
import org.example.model.User;

import java.io.IOException;

public class LoginController {
    
    @FXML
    private TextField fullNameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private CheckBox termsCheckBox;
    
    @FXML
    private Button createAccountButton;
    @FXML
    private Hyperlink loginLink;
    
    @FXML
    private Hyperlink healthProLink;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private VBox fullNameBox;
    
    @FXML
    private VBox confirmPasswordBox;
    
    @FXML
    private VBox termsBox;
    
    @FXML
    private Button registerLink;
    
    @FXML
    private HBox loginPromptBox;
    
    @FXML
    private HBox registerPromptBox;
    
    private boolean isLoginMode = false;
    
    @FXML
    public void handleCreateAccount(ActionEvent event) {
        if (isLoginMode) {
            handleLogin(event);
            return;
        }
        
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Validation
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            statusLabel.setText("Veuillez remplir tous les champs");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            statusLabel.setText("Les mots de passe ne correspondent pas");
            return;
        }
        
        if (!termsCheckBox.isSelected()) {
            statusLabel.setText("Veuillez accepter les conditions d'utilisation");
            return;
        }
        
        if (!email.contains("@") || !email.contains(".")) {
            statusLabel.setText("Veuillez entrer une adresse email valide");
            return;
        }
        
        // Create user with password
        User newUser = new User(0, fullName, email, password);
        newUser.setRoles("[\"ROLE_USER\"]");
        boolean success = UserDAO.insertUser(newUser);
        
        if (success) {
            statusLabel.setText("Compte créé avec succès!");
            statusLabel.setStyle("-fx-text-fill: green;");
            
            // Navigate to dashboard after successful registration
            navigateToDashboard(event, newUser);
        } else {
            statusLabel.setText("Erreur lors de la création du compte");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Veuillez remplir tous les champs");
            return;
        }
        
        User user = UserDAO.login(email, password);
        
        if (user != null) {
            statusLabel.setText("Connexion réussie!");
            statusLabel.setStyle("-fx-text-fill: green;");
            navigateToDashboard(event, user);
        } else {
            statusLabel.setText("Email ou mot de passe incorrect");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    @FXML
    public void handleLoginLink(ActionEvent event) {
        isLoginMode = true;
        fullNameBox.setVisible(false);
        fullNameBox.setManaged(false);
        confirmPasswordBox.setVisible(false);
        confirmPasswordBox.setManaged(false);
        termsBox.setVisible(false);
        termsBox.setManaged(false);
        healthProLink.setVisible(false);
        healthProLink.setManaged(false);
        loginPromptBox.setVisible(false);
        loginPromptBox.setManaged(false);
        registerPromptBox.setVisible(true);
        registerPromptBox.setManaged(true);
        createAccountButton.setText("Se connecter");
        statusLabel.setText("");
    }
    
    @FXML
    public void handleRegisterLink(ActionEvent event) {
        isLoginMode = false;
        fullNameBox.setVisible(true);
        fullNameBox.setManaged(true);
        confirmPasswordBox.setVisible(true);
        confirmPasswordBox.setManaged(true);
        termsBox.setVisible(true);
        termsBox.setManaged(true);
        healthProLink.setVisible(true);
        healthProLink.setManaged(true);
        loginPromptBox.setVisible(true);
        loginPromptBox.setManaged(true);
        registerPromptBox.setVisible(false);
        registerPromptBox.setManaged(false);
        createAccountButton.setText("Créer mon compte");
        statusLabel.setText("");
    }
    
    @FXML
    public void handleHealthProLink(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/certification_request.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 520, 750);
            stage.setScene(scene);
            stage.setTitle("BioSync Pro - Certification");
        } catch (IOException e) {
            statusLabel.setText("Erreur navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void navigateToDashboard(ActionEvent event, User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard.fxml"));
            Parent root = loader.load();
            
            AdminController controller = loader.getController();
            controller.setUser(user);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1100, 700);
            stage.setScene(scene);
            stage.setTitle("BioSync - Administration");
            stage.centerOnScreen();
            stage.show();
            
        } catch (IOException e) {
            statusLabel.setText("Erreur lors de la navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
