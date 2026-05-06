package org.example.service;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.model.User;
import org.example.controller.*;

import java.io.IOException;
import java.util.function.Consumer;

public class NavigationService {

    private static NavigationService instance;
    private Stage mainStage;
    private final ThemeService themeService = ThemeService.getInstance();

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

    // ============================================================
    // CORE NAVIGATION ENGINE (Overloaded to handle 5 or 6 args)
    // ============================================================

    /**
     * Standard navigation with 5 arguments (No controller setup)
     */
    public void navigateFrom(Node sourceNode, String fxmlPath, String title, double width, double height) {
        navigateFrom(sourceNode, fxmlPath, title, width, height, null);
    }

    /**
     * Advanced navigation with 6 arguments (With controller setup)
     */
    public void navigateFrom(Node sourceNode, String fxmlPath, String title, double width, double height, Consumer<Object> controllerSetup) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Set up the controller if a consumer is provided
            if (controllerSetup != null && loader.getController() != null) {
                controllerSetup.accept(loader.getController());
            }

            // Get the current stage from the source node
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            Scene scene = new Scene(root, width, height);

            // Apply theme management
            themeService.registerScene(scene);
            themeService.reapplyCurrentScene(scene);

            stage.setScene(scene);
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            System.err.println("❌ Navigation Error [" + fxmlPath + "]: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ============================================================
    // SPECIFIC ROUTES
    // ============================================================

    public void navigateToLogin(Node sourceNode) {
        navigateFrom(sourceNode, "/view/login.fxml", "BioSync - Connexion", 480, 680);
    }

    public void navigateToForgotPassword(Node sourceNode) {
        navigateFrom(sourceNode, "/view/forgot_password.fxml", "BioSync - Mot de passe oublié", 480, 680);
    }

    public void navigateToDashboard(Node sourceNode, User currentUser) {
        navigateFrom(sourceNode, "/view/dashboard.fxml", "BioSync - Administration", 1100, 700, ctrl -> {
            if (ctrl instanceof AdminController) {
                ((AdminController) ctrl).setUser(currentUser);
            }
        });
    }

    public void navigateToUtilisateurs(Node sourceNode, User currentUser) {
        navigateFrom(sourceNode, "/view/utilisateurs.fxml", "BioSync - Gestion Utilisateurs", 1100, 700, ctrl -> {
            if (ctrl instanceof UtilisateursController) {
                ((UtilisateursController) ctrl).setCurrentUser(currentUser);
            }
        });
    }

    public void navigateToCommunity(Node sourceNode, User currentUser) {
        navigateFrom(sourceNode, "/view/user_community.fxml", "BioSync - Communauté", 1100, 700, ctrl -> {
            if (ctrl instanceof UserCommunityController) {
                ((UserCommunityController) ctrl).setCurrentUser(currentUser);
            }
        });
    }

    public void navigateToCertifications(Node sourceNode, User currentUser) {
        navigateFrom(sourceNode, "/view/certifications_admin.fxml", "BioSync - Certifications", 1100, 700, ctrl -> {
            if (ctrl instanceof CertificationsAdminController) {
                ((CertificationsAdminController) ctrl).setCurrentUser(currentUser);
            }
        });
    }

    public void navigateToCertificationRequest(Node sourceNode) {
        navigateFrom(sourceNode, "/view/certification_request.fxml", "BioSync - Demande Pro", 520, 750);
    }

    public void navigateToLogs(Node sourceNode, User currentUser) {
        navigateFrom(sourceNode, "/view/logs_view.fxml", "BioSync - Logs", 1200, 750, ctrl -> {
            if (ctrl instanceof LogsController) {
                ((LogsController) ctrl).setCurrentUser(currentUser);
            }
        });
    }

    // FIXED: Added missing method requested by LogsController
    public void navigateToLogStats(Node sourceNode, User currentUser) {
        navigateFrom(sourceNode, "/view/logs_stats.fxml", "BioSync - Statistiques Logs", 1200, 750, ctrl -> {
            if (ctrl instanceof LogsStatsController) {
                ((LogsStatsController) ctrl).setCurrentUser(currentUser);
            }
        });
    }

    public void navigateToNutrition(Node sourceNode, User currentUser) {
        String roles = currentUser != null ? currentUser.getRoles() : "";
        boolean isCoachOrAdmin = roles != null && (roles.contains("ADMIN") || roles.contains("COACH"));

        if (isCoachOrAdmin) {
            navigateFrom(sourceNode, "/view/nutrition/coach_users.fxml", "BioSync - Espace Coach", 1100, 700, ctrl -> {
                if (ctrl instanceof CoachUsersController) {
                    ((CoachUsersController) ctrl).setCoachUser(currentUser);
                }
            });
        } else {
            navigateFrom(sourceNode, "/view/nutrition/repas_index.fxml", "BioSync - Ma Nutrition", 1100, 700, ctrl -> {
                if (ctrl instanceof Nutritioncontroller) {
                    ((Nutritioncontroller) ctrl).setCurrentUser(currentUser);
                    ((Nutritioncontroller) ctrl).setUtilisateurId(currentUser.getId());
                }
            });
        }
    }

    // Helper for direct navigation if no source node is available (via mainStage)
    public void navigateTo(String fxmlPath, String title, double width, double height, Consumer<Object> setup) {
        if (mainStage == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            if (setup != null) setup.accept(loader.getController());
            Scene scene = new Scene(root, width, height);
            themeService.registerScene(scene);
            mainStage.setScene(scene);
            mainStage.setTitle(title);
            mainStage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}