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
                ((org.example.controller.AdminController) ctrl).setUser(currentUser);
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

    // ── Mental Wellness ────────────────────────────────────────

    /** Ouvre la liste des quiz (admin). */
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
