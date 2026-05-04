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

    public void navigateTo(String fxmlPath, String title, double width, double height) {
        navigateTo(fxmlPath, title, width, height, null);
    }

    public void navigateTo(String fxmlPath, String title, double width, double height, Consumer<Object> controllerSetup) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            if (controllerSetup != null) {
                controllerSetup.accept(loader.getController());
            }

            Stage stage = mainStage != null ? mainStage : new Stage();
            Scene scene = new Scene(root, width, height);

            // Apply current theme
            themeService.registerScene(scene);

            stage.setScene(scene);
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

            if (controllerSetup != null) {
                controllerSetup.accept(loader.getController());
            }

            Stage stage = (Stage) sourceNode.getScene().getWindow();
            Scene scene = new Scene(root, width, height);

            // Apply current theme
            themeService.registerScene(scene);

            stage.setScene(scene);
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            throw new NavigationException("Failed to navigate to " + fxmlPath, e);
        }
    }

    public void navigateToDashboard(Node sourceNode, User currentUser) {
        navigateFrom(sourceNode, "/view/dashboard.fxml", "BioSync - Administration", 1100, 700, ctrl -> {
            if (ctrl instanceof org.example.controller.AdminController) {
                ((org.example.controller.AdminController) ctrl).setUser(currentUser);
            }
        });
    }

    public void navigateToLogin(Node sourceNode) {
        navigateFrom(sourceNode, "/view/login.fxml", "BioSync - Connexion", 480, 680);
    }

    public void navigateToUtilisateurs(Node sourceNode, User currentUser) {
        navigateFrom(sourceNode, "/view/utilisateurs.fxml", "BioSync - Utilisateurs", 1100, 700, ctrl -> {
            if (ctrl instanceof org.example.controller.UtilisateurController) {
                ((org.example.controller.UtilisateurController) ctrl).setCurrentUser(currentUser);
            }
        });
    }

    public void navigateToCertifications(Node sourceNode, User currentUser) {
        navigateFrom(sourceNode, "/view/certifications_admin.fxml", "BioSync - Certifications", 1100, 700, ctrl -> {
            if (ctrl instanceof org.example.controller.CertificationsAdminController) {
                ((org.example.controller.CertificationsAdminController) ctrl).setCurrentUser(currentUser);
            }
        });
    }

    public void navigateToCertificationRequest(Node sourceNode) {
        navigateFrom(sourceNode, "/view/certification_request.fxml", "BioSync - Demande de Certification", 520, 750);
    }

    public void navigateToLogs(Node sourceNode, User currentUser) {
        navigateFrom(sourceNode, "/view/logs_view.fxml", "BioSync - Historique des Logs", 1200, 750, ctrl -> {
            if (ctrl instanceof org.example.controller.LogsController) {
                ((org.example.controller.LogsController) ctrl).setCurrentUser(currentUser);
            }
        });
    }

    public void navigateToLogStats(Node sourceNode, User currentUser) {
        navigateFrom(sourceNode, "/view/logs_stats.fxml", "BioSync - Statistiques des Logs", 1200, 750, ctrl -> {
            if (ctrl instanceof org.example.controller.LogsStatsController) {
                ((org.example.controller.LogsStatsController) ctrl).setCurrentUser(currentUser);
            }
        });
    }

    public void navigateToForgotPassword(Node sourceNode) {
        navigateFrom(sourceNode, "/view/forgot_password.fxml", "BioSync - Mot de passe oublié", 480, 680);
    }

    public void navigateToNutrition(Node sourceNode, User currentUser) {
        String roles = currentUser != null ? currentUser.getRoles() : "";
        boolean isAdminOrCoach = roles != null && (roles.contains("ADMIN") || roles.contains("COACH"));

        if (isAdminOrCoach) {
            navigateFrom(sourceNode, "/view/nutrition/coach_users.fxml", "BioSync - Nutrition", 1100, 700, ctrl -> {
                if (ctrl instanceof org.example.controller.CoachUsersController) {
                    ((org.example.controller.CoachUsersController) ctrl).setCoachUser(currentUser);
                }
            });
        } else {
            navigateFrom(sourceNode, "/view/nutrition/repas_index.fxml", "BioSync - Mes Repas", 1100, 700, ctrl -> {
                if (ctrl instanceof org.example.controller.Nutritioncontroller) {
                    ((org.example.controller.Nutritioncontroller) ctrl).setCurrentUser(currentUser);
                    if (currentUser != null) {
                        ((org.example.controller.Nutritioncontroller) ctrl).setUtilisateurId(currentUser.getId());
                    }
                }
            });
        }
    }

    public static class NavigationException extends RuntimeException {
        public NavigationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
