package org.example.controller;

import org.example.model.User;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private Label userInfoLabel;
    
    @FXML
    private Button profileButton;
    
    @FXML
    private Button usersButton;
    
    @FXML
    private Button settingsButton;
    
    @FXML
    private Button logoutButton;
    
    @FXML
    private Label statusLabel;
    
    private User currentUser;
    
    public void setUser(User user) {
        this.currentUser = user;
        updateUserInfo();
    }
    
    private void updateUserInfo() {
        if (currentUser != null) {
            welcomeLabel.setText("Bienvenue, " + currentUser.getNomComplet() + "!");
            userInfoLabel.setText("Email: " + currentUser.getEmail() + " | ID: " + currentUser.getId());
        }
    }
    
    @FXML
    public void handleProfile(ActionEvent event) {
        statusLabel.setText("Profil utilisateur - " + currentUser.getNomComplet());
        // You could create a profile page here
        showProfileDialog();
    }
    

    @FXML
    public void handleSettings(ActionEvent event) {
        statusLabel.setText("Paramètres - En construction");
        showSettingsDialog();
    }
    
    @FXML
    public void handleLogout(ActionEvent event) {
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Voulez-vous vraiment vous déconnecter?");
        alert.setContentText("Vous serez redirigé vers la page d'inscription.");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            navigateToLogin(event);
        }
    }
    
    private void showProfileDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mon Profil");
        alert.setHeaderText("Informations du Profil");
        
        String content = "Nom: " + currentUser.getNomComplet() + "\n" +
                        "Email: " + currentUser.getEmail() + "\n" +
                        "ID Utilisateur: " + currentUser.getId() + "\n" +
                        "Statut: Actif";
        
        alert.setContentText(content);
        alert.showAndWait();
    }
    

    
    private void showSettingsDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Paramètres");
        alert.setHeaderText("Paramètres de l'Application");
        alert.setContentText("Les paramètres suivants seront bientôt disponibles:\n\n" +
                           "1. Personnalisation du profil\n" +
                           "2. Préférences de notification\n" +
                           "3. Sécurité et confidentialité\n" +
                           "4. Thème et apparence");
        alert.showAndWait();
    }
    
    private void navigateToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("BioSync - Inscription");
            stage.show();
            
        } catch (IOException e) {
            statusLabel.setText("Erreur lors de la déconnexion: " + e.getMessage());
        }
    }
}

