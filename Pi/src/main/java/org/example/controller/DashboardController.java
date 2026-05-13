package org.example.controller;

import org.example.model.User;
import org.example.service.NavigationService;
import org.example.service.ThemeService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
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
        showProfileDialog();
    }

    @FXML
    public void handleSettings(ActionEvent event) {
        statusLabel.setText("Paramètres - En construction");
        showSettingsDialog();
    }

    @FXML
    public void handleNavMental() {
        NavigationService.getInstance().navigateToQuizManager(welcomeLabel, currentUser);
    }

    @FXML
    public void handleNavAi() {
        statusLabel.setText("IA Mentale - En construction");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("IA Mentale");

        alert.setHeaderText("Assistant IA de Santé Mentale");

        alert.setContentText(
                "Cette fonctionnalité sera bientôt disponible:\n\n" +
                "• Analyse de l'humeur\n" +
                "• Recommandations personnalisées\n" +
                "• Suivi du bien-être mental\n" +
                "• Chat avec l'assistant IA"
        );

        alert.showAndWait();
    }

    @FXML
    public void handleLogout(ActionEvent event) {

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

        String content =
                "Nom: " + currentUser.getNomComplet() + "\n" +
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

        alert.setContentText(
                "Les paramètres suivants seront bientôt disponibles:\n\n" +
                "1. Personnalisation du profil\n" +
                "2. Préférences de notification\n" +
                "3. Sécurité et confidentialité\n" +
                "4. Thème et apparence"
        );

        alert.showAndWait();
    }

    private void navigateToLogin(ActionEvent event) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/view/login.fxml"));

            Parent root = loader.load();

            Stage stage =
                    (Stage) ((Node) event.getSource()).getScene().getWindow();

            Scene scene = new Scene(root);

            stage.setScene(scene);

            stage.setTitle("BioSync - Inscription");

            stage.show();

        } catch (IOException e) {

            statusLabel.setText(
                    "Erreur lors de la déconnexion: " + e.getMessage()
            );
        }
    }

    // =========================
    // MISSING METHODS
    // =========================

    @FXML
    private void handleNavUtilisateurs() {
        NavigationService.getInstance().navigateToUtilisateurs(welcomeLabel, currentUser);
    }

    @FXML
    private void handleNavCertifications() {
        NavigationService.getInstance().navigateToQuizManager(welcomeLabel, currentUser);
    }

    @FXML
    private void handleHistoriqueLogs() {
        // For now, show placeholder - implement logs view later
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Historique des Logs");
        alert.setHeaderText("Historique des Logs");
        alert.setContentText("Cette fonctionnalité sera bientôt disponible.");
        alert.showAndWait();
    }

    @FXML
    private void handleRateLimiter() {
        // For now, show placeholder - implement rate limiter view later
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Rate Limiter");
        alert.setHeaderText("Gestion du Rate Limiter");
        alert.setContentText("Cette fonctionnalité sera bientôt disponible.");
        alert.showAndWait();
    }

    @FXML
    private void handleNavNutrition() {
        if (welcomeLabel != null && currentUser != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/nutrition/repas_index.fxml"));
                Parent root = loader.load();
                
                // Get the nutrition controller and set the current user
                Object controller = loader.getController();
                if (controller instanceof org.example.controller.Nutritioncontroller) {
                    org.example.controller.Nutritioncontroller nutritionCtrl = (org.example.controller.Nutritioncontroller) controller;
                    nutritionCtrl.setCurrentUser(currentUser);
                    nutritionCtrl.setUtilisateurId(currentUser.getId());
                }

                Stage stage = new Stage();
                stage.setTitle("BioSync - Nutrition");
                stage.setScene(new Scene(root, 1100, 700));
                stage.show();

                // Close current dashboard window
                Stage dashboardStage = (Stage) welcomeLabel.getScene().getWindow();
                dashboardStage.close();

            } catch (IOException e) {
                statusLabel.setText("Erreur lors de l'ouverture: " + e.getMessage());
            }
        } else {
            statusLabel.setText("Erreur: Écran d'accueil non disponible ou utilisateur non connecté");
        }
    }

    @FXML
    private void handleNavSports() {
        if (currentUser != null) {
            NavigationService.getInstance().navigateToSports(welcomeLabel, currentUser);
        } else {
            statusLabel.setText("Erreur: Utilisateur non connecté");
        }
    }

    @FXML
    private void handleNavCommunity() {
        if (welcomeLabel != null && currentUser != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/user_community.fxml"));
                Parent root = loader.load();
                
                // Get the community controller and set the current user
                Object controller = loader.getController();
                if (controller instanceof org.example.controller.UserCommunityController) {
                    org.example.controller.UserCommunityController communityCtrl = (org.example.controller.UserCommunityController) controller;
                    communityCtrl.setCurrentUser(currentUser);
                }

                Stage stage = new Stage();
                stage.setTitle("BioSync - Communauté");
                stage.setScene(new Scene(root, 1100, 700));
                stage.show();

                // Close current dashboard window
                Stage dashboardStage = (Stage) welcomeLabel.getScene().getWindow();
                dashboardStage.close();

            } catch (IOException e) {
                statusLabel.setText("Erreur lors de l'ouverture: " + e.getMessage());
            }
        } else {
            statusLabel.setText("Erreur: Écran d'accueil non disponible ou utilisateur non connecté");
        }
    }

    @FXML
    private void handleNavConsultation(ActionEvent event) {
        try {
            // Load the original dashboard to get the sidebar
            FXMLLoader dashboardLoader = new FXMLLoader(getClass().getResource("/view/dashboard.fxml"));
            BorderPane dashboardRoot = (BorderPane) dashboardLoader.load();
            
            // Set current user in the new dashboard controller
            DashboardController newDashboardController = dashboardLoader.getController();
            newDashboardController.setUser(currentUser);
            
            // Load the consultation content
            FXMLLoader consultationLoader = new FXMLLoader(getClass().getResource("/view/consultation_interface.fxml"));
            Parent consultationContent = consultationLoader.load();
            
            // Get the consultation controller and set the current user
            Object controller = consultationLoader.getController();
            if (controller instanceof org.example.controller.ConsultationController) {
                org.example.controller.ConsultationController consultationCtrl = (org.example.controller.ConsultationController) controller;
                consultationCtrl.setCurrentUser(currentUser);
            }
            
            // Set consultation content as the center of the dashboard (keeping sidebar)
            dashboardRoot.setCenter(consultationContent);
            
            // Replace content in current stage using event source
            Stage currentStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            currentStage.setTitle("BioSync - Gestion des Consultations");
            currentStage.setScene(new Scene(dashboardRoot, 1400.0, 700.0));
            
        } catch (IOException e) {
            statusLabel.setText("Erreur lors de l'ouverture: " + e.getMessage());
        }
    }

    @FXML
    private void handleNavPrescription(ActionEvent event) {
        try {
            // Load the original dashboard to get the sidebar
            FXMLLoader dashboardLoader = new FXMLLoader(getClass().getResource("/view/dashboard.fxml"));
            BorderPane dashboardRoot = (BorderPane) dashboardLoader.load();
            
            // Set current user in the new dashboard controller
            DashboardController newDashboardController = dashboardLoader.getController();
            newDashboardController.setUser(currentUser);
            
            // Load the prescription content
            FXMLLoader prescriptionLoader = new FXMLLoader(getClass().getResource("/view/prescription_interface.fxml"));
            Parent prescriptionContent = prescriptionLoader.load();
            
            // Get the prescription controller and set the current user
            Object controller = prescriptionLoader.getController();
            if (controller instanceof org.example.controller.PrescriptionController) {
                org.example.controller.PrescriptionController prescriptionCtrl = (org.example.controller.PrescriptionController) controller;
                prescriptionCtrl.setCurrentUser(currentUser);
            }
            
            // Set prescription content as the center of the dashboard (keeping sidebar)
            dashboardRoot.setCenter(prescriptionContent);
            
            // Replace content in current stage using event source
            Stage currentStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            currentStage.setTitle("BioSync - Gestion des Prescriptions");
            currentStage.setScene(new Scene(dashboardRoot, 1400.0, 700.0));
            
        } catch (IOException e) {
            statusLabel.setText("Erreur lors de l'ouverture: " + e.getMessage());
        }
    }

    @FXML
    private void handleThemeToggle() {
        ThemeService themeService = ThemeService.getInstance();
        themeService.toggleDarkMode();
    }

    @FXML
    private void handleGererMembres() {
        NavigationService.getInstance().navigateToUtilisateurs(welcomeLabel, currentUser);
    }

    @FXML
    private void handleValidationsPro() {
        NavigationService.getInstance().navigateToQuizManager(welcomeLabel, currentUser);
    }
    @FXML
public void handleNavRendezvous(javafx.scene.input.MouseEvent event) {
    try {
        // Load the original dashboard to get the sidebar
        FXMLLoader dashboardLoader = new FXMLLoader(getClass().getResource("/view/dashboard.fxml"));
        BorderPane dashboardRoot = (BorderPane) dashboardLoader.load();
        
        // Set current user in the new dashboard controller
        DashboardController newDashboardController = dashboardLoader.getController();
        newDashboardController.setUser(currentUser);
        
        // Load the rendezvous content
        FXMLLoader rendezvousLoader = new FXMLLoader(getClass().getResource("/view/rendezvous.fxml"));
        Parent rendezvousContent = rendezvousLoader.load();
        
        RendezVousController controller = rendezvousLoader.getController();
        controller.setCurrentUser(currentUser);
        
        // Set rendezvous content as the center of the dashboard (keeping sidebar)
        dashboardRoot.setCenter(rendezvousContent);
        
        // Replace content in current stage using event source
        Stage currentStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        currentStage.setTitle("BioSync - Gestion des Rendez-vous");
        currentStage.setScene(new Scene(dashboardRoot, 1400.0, 700.0));
        
    } catch (IOException e) {
        statusLabel.setText("Erreur lors de l'ouverture: " + e.getMessage());
    }
}
}