package org.example.controller;

import org.example.model.User;
import org.example.service.NavigationService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
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
    private final NavigationService navigationService = NavigationService.getInstance();
    
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
    
    // ==================== NAVIGATION HANDLERS ====================
    
    @FXML
    public void handleNavUtilisateurs(MouseEvent event) {
        try {
            Node sourceNode = (Node) event.getSource();
            navigationService.navigateToVueUtilisateur(sourceNode, currentUser);
        } catch (Exception e) {
            showError("Erreur de navigation", "Impossible d'accéder à la gestion des utilisateurs: " + e.getMessage());
        }
    }
    
    @FXML
    public void handleNavCertifications(MouseEvent event) {
        try {
            Node sourceNode = (Node) event.getSource();
            navigationService.navigateToCertificationsAdmin(sourceNode, currentUser);
        } catch (Exception e) {
            showError("Erreur de navigation", "Impossible d'accéder aux certifications: " + e.getMessage());
        }
    }
    
    @FXML
    public void handleNavNutrition(MouseEvent event) {
        try {
            Node sourceNode = (Node) event.getSource();
            navigationService.navigateToCoachRepas(sourceNode, currentUser);
        } catch (Exception e) {
            showError("Erreur de navigation", "Impossible d'accéder à la nutrition: " + e.getMessage());
        }
    }
    
    @FXML
    public void handleNavSports(MouseEvent event) {
        try {
            Node sourceNode = (Node) event.getSource();
            navigationService.navigateToAfficherSeance(sourceNode, currentUser);
        } catch (Exception e) {
            showError("Erreur de navigation", "Impossible d'accéder aux sports: " + e.getMessage());
        }
    }
    
    @FXML
    public void handleNavAi(MouseEvent event) {
        try {
            Node sourceNode = (Node) event.getSource();
            navigationService.navigateToAiChat(sourceNode, currentUser);
        } catch (Exception e) {
            showError("Erreur de navigation", "Impossible d'accéder à l'IA: " + e.getMessage());
        }
    }
    
    @FXML
    public void handleNavMentalHealthDashboard(MouseEvent event) {
        try {
            Node sourceNode = (Node) event.getSource();
            navigationService.navigateToMentalHealthDashboard(sourceNode, currentUser);
        } catch (Exception e) {
            showError("Erreur de navigation", "Impossible d'accéder au tableau de bord santé mentale: " + e.getMessage());
        }
    }
    
    @FXML
    public void handleNavCommunity(MouseEvent event) {
        try {
            Node sourceNode = (Node) event.getSource();
            navigationService.navigateToGroupManager(sourceNode, currentUser);
        } catch (Exception e) {
            showError("Erreur de navigation", "Impossible d'accéder à la communauté: " + e.getMessage());
        }
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

