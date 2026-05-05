package org.example.service;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.model.User;

import java.io.IOException;
import java.util.function.Consumer;

public class NavigationService {

    private static NavigationService instance;
    private Stage mainStage;

    private NavigationService() {}

    public static NavigationService getInstance() {
        if (instance == null) {
            instance = new NavigationService();
        }
        return instance;
    }

    public void setMainStage(Stage stage) {
        this.mainStage = stage;
    }

    public void navigateTo(String fxmlPath, String title, double width, double height) {
        navigateTo(fxmlPath, title, width, height, null);
    }

    public void navigateTo(String fxmlPath, String title, double width, double height, Consumer<Object> controllerSetup) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            if (controllerSetup != null) controllerSetup.accept(loader.getController());
            Stage stage = mainStage != null ? mainStage : new Stage();
            stage.setScene(new Scene(root, width, height));
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            throw new NavigationException("Failed to navigate to " + fxmlPath, e);
        }
    }

    public void navigateFrom(Node sourceNode, String fxmlPath, String title, double width, double height) {
        navigateFrom(sourceNode, fxmlPath, title, width, height, null);
    }

    public void navigateFrom(Node sourceNode, String fxmlPath, String title, double width, double height, Consumer<Object> controllerSetup) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            if (controllerSetup != null) controllerSetup.accept(loader.getController());
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            stage.setScene(new Scene(root, width, height));
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            throw new NavigationException("Failed to navigate to " + fxmlPath, e);
        }
    }

    // ✅ Redirige selon le rôle Coach ou User
    public void navigateToDashboard(Node sourceNode, User currentUser) {
        boolean isCoach = currentUser.getRoles() != null
                && currentUser.getRoles().contains("ROLE_COACH");

        if (isCoach) {
            org.example.model.Session.role = "COACH";
            navigateFrom(sourceNode, "/view/MenuCoach.fxml", "BioSync - Espace Coach", 1100, 700, ctrl -> {
                if (ctrl instanceof org.example.controller.MenuCoachController c)
                    c.setCurrentUser(currentUser);
            });
        } else {
            org.example.model.Session.role = "USER";
            navigateFrom(sourceNode, "/view/MenuUser.fxml", "BioSync - Espace Utilisateur", 1100, 700, ctrl -> {
                if (ctrl instanceof org.example.controller.MenuUserController c)
                    c.setCurrentUser(currentUser);
            });
        }
    }

    public void navigateToLogin(Node sourceNode) {
        navigateFrom(sourceNode, "/view/login.fxml", "BioSync - Inscription", 400, 550);
    }

    // ✅ Gardé car AdminController l'utilise — vide temporairement si UtilisateurController manque
    public void navigateToUtilisateurs(Node sourceNode, User currentUser) {
        navigateFrom(sourceNode, "/view/utilisateurs.fxml", "BioSync - Utilisateurs", 1100, 700, ctrl -> {
            // sera complété quand UtilisateurController sera intégré
        });
    }

    // ✅ Gardé car AdminController l'utilise — vide temporairement
    public void navigateToCertifications(Node sourceNode, User currentUser) {
        navigateFrom(sourceNode, "/view/certifications_admin.fxml", "BioSync - Certifications", 1100, 700, ctrl -> {
            // sera complété quand CertificationsAdminController sera intégré
        });
    }

    // ✅ Gardé car AdminController l'utilise — vide temporairement
    public void navigateToMental(Node sourceNode, User currentUser) {
        // sera complété quand QuizController sera intégré
    }

    public static class NavigationException extends RuntimeException {
        public NavigationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}