package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.dao.UserDAO;
import org.example.model.User;

import java.io.IOException;
import java.util.List;

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
            welcomeLabel.setText("Bienvenue, " + currentUser.getUsername() + "!");
            userInfoLabel.setText("Email: " + currentUser.getEmail() + " | ID: " + currentUser.getId());
        }
    }
    
    @FXML
    public void handleProfile(ActionEvent event) {
        statusLabel.setText("Profil utilisateur - " + currentUser.getUsername());
        // You could create a profile page here
        showProfileDialog();
    }
    
    @FXML
    public void handleUsers(ActionEvent event) {
        statusLabel.setText("Chargement des utilisateurs...");
        showAllUsers();
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
        
        String content = "Nom: " + currentUser.getUsername() + "\n" +
                        "Email: " + currentUser.getEmail() + "\n" +
                        "ID Utilisateur: " + currentUser.getId() + "\n" +
                        "Statut: Actif";
        
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showAllUsers() {
        try {
            List<User> users = UserDAO.getAllUsers();
            
            if (users.isEmpty()) {
                statusLabel.setText("Aucun utilisateur trouvé");
                return;
            }
            
            // Create a dialog to show all users
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Tous les Utilisateurs");
            dialog.setHeaderText("Liste des utilisateurs enregistrés");
            
            TextArea textArea = new TextArea();
            StringBuilder sb = new StringBuilder();
            
            for (User user : users) {
                sb.append("ID: ").append(user.getId())
                  .append(" | Nom: ").append(user.getUsername())
                  .append(" | Email: ").append(user.getEmail())
                  .append("\n");
            }
            
            textArea.setText(sb.toString());
            textArea.setEditable(false);
            textArea.setPrefHeight(300);
            textArea.setPrefWidth(400);
            
            dialog.getDialogPane().setContent(textArea);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();
            
            statusLabel.setText(users.size() + " utilisateur(s) trouvé(s)");
            
        } catch (Exception e) {
            statusLabel.setText("Erreur: " + e.getMessage());
        }
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
