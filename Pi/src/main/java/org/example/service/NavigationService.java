package org.example.service;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * NavigationService - Centralized navigation management for JavaFX application
 * Provides singleton-based and static utility navigation methods
 */
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

    /**
     * Static utility method to load a page from an ActionEvent
     * @param fxml Path to FXML file (e.g., "/view/dashboard.fxml")
     * @param event ActionEvent to get source stage
     * @return true if navigation succeeded
     */
    public static boolean loadPage(String fxml, ActionEvent event) {
        try {
            Node source = (Node) event.getSource();
            Stage currentStage = (Stage) source.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(NavigationService.class.getResource(
                Objects.requireNonNull(fxml, "FXML path cannot be null")));
            Parent root = loader.load();

            currentStage.setScene(new Scene(root));
            currentStage.show();
            return true;
        } catch (IOException | NullPointerException e) {
            System.err.println("Navigation error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Static utility method to load a page with specific dimensions
     * @param fxml Path to FXML file
     * @param title Window title
     * @param width Window width
     * @param height Window height
     * @param event ActionEvent to get source stage
     * @return true if navigation succeeded
     */
    public static boolean loadPage(String fxml, String title, double width, double height, ActionEvent event) {
        try {
            Node source = (Node) event.getSource();
            Stage currentStage = (Stage) source.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(NavigationService.class.getResource(
                Objects.requireNonNull(fxml, "FXML path cannot be null")));
            Parent root = loader.load();

            Scene scene = new Scene(root, width, height);
            currentStage.setScene(scene);
            currentStage.setTitle(title);
            currentStage.centerOnScreen();
            currentStage.show();
            return true;
        } catch (IOException | NullPointerException e) {
            System.err.println("Navigation error: " + e.getMessage());
            return false;
        }
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
                ((org.example.controller.AdminController) ctrl).initializeWithUser(currentUser);
            }
        });
    }

    public void navigateToLogin(Node sourceNode) {
        navigateFrom(sourceNode, "/view/login.fxml", "BioSync - Inscription", 400, 550);
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

    // ── AI ─────────────────────────────────────────────────────

    public void navigateToAiChat(Node sourceNode, User currentUser) {
        navigateFrom(sourceNode, "/view/ai_chat.fxml", "BioSync - Assistant IA", 1000, 720, ctrl -> {
            if (ctrl instanceof org.example.controller.AiChatController chatCtrl) {
                chatCtrl.setCurrentUser(currentUser);
            }
        });
    }

    public void navigateToDashboardCognitif(Node sourceNode) {
        navigateFrom(sourceNode, "/DashboardCognitif.fxml", "BioSync - Dashboard Cognitif", 1000, 720, ctrl -> {
            // DashboardCognitifController uses SessionContext, no user injection needed
        });
    }

    // ── Mental Wellness ────────────────────────────────────────

    /** Ouvre la liste des quiz (admin) - quiz_manager.fxml. */
    public void navigateToMental(Node sourceNode, User currentUser) {
        navigateToQuizManager(sourceNode, currentUser);
    }

    /** Quiz Manager – liste et gestion des quiz. */
    public void navigateToQuizManager(Node sourceNode, User currentUser) {
        navigateFrom(sourceNode, "/view/quiz_manager.fxml", "BioSync - Quiz Mentaux", 1200, 720, ctrl -> {
            if (ctrl instanceof org.example.controller.QuizController) {
                ((org.example.controller.QuizController) ctrl).setCurrentUser(currentUser);
            }
        });
    }

    /**
     * Quiz Form – création (quiz == null) ou édition (quiz != null).
     */
    public void navigateToQuizForm(Node sourceNode, User currentUser,
                                   org.example.model.Quiz quiz) {
        navigateFrom(sourceNode, "/view/quiz_form.fxml", "BioSync - Formulaire Quiz", 760, 680, ctrl -> {
            if (ctrl instanceof org.example.controller.QuizFormController qfc) {
                qfc.setCurrentUser(currentUser);
                if (quiz != null) qfc.setQuiz(quiz);
            }
        });
    }

    /**
     * Question Manager – gestion des questions d'un quiz.
     */
    public void navigateToQuestionManager(Node sourceNode, User currentUser,
                                          org.example.model.Quiz quiz) {
        navigateFrom(sourceNode, "/view/question_manager.fxml", "BioSync - Questions", 1200, 720, ctrl -> {
            if (ctrl instanceof org.example.controller.QuestionManagerController qmc) {
                qmc.setCurrentUser(currentUser);
                qmc.setQuiz(quiz);
            }
        });
    }

    /**
     * Quiz Player – VIEW 1 (sélection) si quiz == null,
     *               VIEW 2 (jeu direct) si quiz != null.
     */
    public void navigateToQuizPlayer(Node sourceNode, User currentUser,
                                     org.example.model.Quiz quiz) {
        navigateFrom(sourceNode, "/view/quiz_player.fxml", "BioSync - Jouer", 1000, 720, ctrl -> {
            if (ctrl instanceof org.example.controller.QuizPlayerController qpc) {
                qpc.setCurrentUser(currentUser);
                if (quiz != null) qpc.setQuiz(quiz);
            }
        });
    }

    /** Vue Utilisateur – interface patient pour passer les quiz. */
    public void navigateToVueUtilisateur(Node sourceNode, User currentUser) {
        navigateFrom(sourceNode, "/view/vue_utilisateur.fxml", "BioSync – Quiz Patient", 940, 720, ctrl -> {
            if (ctrl instanceof org.example.controller.VueUtilisateurController vuc) {
                vuc.setCurrentUser(currentUser);
            }
        });
    }

    public static class NavigationException extends RuntimeException {
        public NavigationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
