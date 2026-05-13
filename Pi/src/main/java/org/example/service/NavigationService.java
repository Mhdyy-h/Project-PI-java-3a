package org.example.service;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.controller.*;
import org.example.model.User;

import java.io.IOException;
import java.util.function.Consumer;

public class NavigationService {

    private static NavigationService instance;
    private Stage mainStage;
    private final ThemeService themeService = ThemeService.getInstance();

    private NavigationService() {
    }

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
    // CORE NAVIGATION ENGINE
    // ============================================================

    public void navigateTo(String fxmlPath,
                           String title,
                           double width,
                           double height) {

        navigateTo(fxmlPath, title, width, height, null);
    }

    public void navigateTo(String fxmlPath,
                           String title,
                           double width,
                           double height,
                           Consumer<Object> setup) {

        if (mainStage == null) {
            return;
        }

        try {

            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource(fxmlPath));

            Parent root = loader.load();

            if (setup != null) {
                setup.accept(loader.getController());
            }

            Scene scene = new Scene(root, width, height);

            themeService.registerScene(scene);

            mainStage.setScene(scene);
            mainStage.setTitle(title);
            mainStage.centerOnScreen();
            mainStage.show();

        } catch (IOException e) {

            throw new NavigationException(
                    "Failed to navigate to " + fxmlPath,
                    e
            );
        }
    }

    // ============================================================
    // NAVIGATION FROM NODE
    // ============================================================

    public void navigateFrom(Node sourceNode,
                             String fxmlPath,
                             String title,
                             double width,
                             double height) {

        navigateFrom(
                sourceNode,
                fxmlPath,
                title,
                width,
                height,
                null
        );
    }

    public void navigateFrom(Node sourceNode,
                             String fxmlPath,
                             String title,
                             double width,
                             double height,
                             Consumer<Object> setup) {

        System.out.println(" NAVIGATION DEBUG: navigateFrom called with fxmlPath: " + fxmlPath);
        System.out.println(" NAVIGATION DEBUG: sourceNode: " + (sourceNode != null ? sourceNode.getClass().getSimpleName() : "null"));
        
        if (sourceNode == null) {
            System.out.println(" NAVIGATION ERROR: sourceNode is null!");
            return;
        }
        
        if (sourceNode.getScene() == null) {
            System.out.println(" NAVIGATION ERROR: sourceNode.getScene() is null!");
            return;
        }

        try {

            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource(fxmlPath));

            Parent root = loader.load();
            System.out.println(" NAVIGATION DEBUG: FXML loaded successfully");

            if (setup != null) {
                setup.accept(loader.getController());
            }

            Stage stage =
                    (Stage) sourceNode.getScene().getWindow();
            System.out.println(" NAVIGATION DEBUG: Stage obtained successfully");

            Scene scene = new Scene(root, width, height);

            themeService.registerScene(scene);

            stage.setScene(scene);
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();
            System.out.println(" NAVIGATION DEBUG: Navigation completed successfully");

        } catch (IOException e) {
            System.out.println(" NAVIGATION ERROR: IOException: " + e.getMessage());
            throw new NavigationException(
                    "Failed to navigate to " + fxmlPath,
                    e
            );
        } catch (Exception e) {
            System.out.println(" NAVIGATION ERROR: Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ============================================================
    // ROUTES
    // ============================================================

    public void navigateToDashboard(Node sourceNode,
                                    User currentUser) {

        final User finalUser = currentUser;

        if (currentUser == null) {
            return;
        }

        boolean isAdmin =
                currentUser.getRoles() != null
                        &&
                        currentUser.getRoles()
                                .contains("ROLE_ADMIN");

        boolean isCoach =
                currentUser.getRoles() != null
                        &&
                        currentUser.getRoles()
                                .contains("ROLE_COACH");

        boolean isSpecialiste =
                currentUser.getRoles() != null
                        &&
                        (
                                currentUser.getRoles()
                                        .contains("ROLE_SPECIALIST")
                                        ||
                                        currentUser.getRoles()
                                                .contains("SPECIALISTE")
                        );

        if (isAdmin) {

            org.example.model.Session.role = "ADMIN";

        } else if (isCoach) {

            org.example.model.Session.role = "COACH";

        } else if (isSpecialiste) {

            org.example.model.Session.role = "SPECIALIST";

        } else {

            org.example.model.Session.role = "USER";
        }

        navigateFrom(
                sourceNode,
                "/view/dashboard.fxml",
                "BioSync - Dashboard",
                1100,
                700,
                ctrl -> {

                    if (ctrl instanceof DashboardController) {

                        DashboardController c =
                                (DashboardController) ctrl;

                        c.setUser(finalUser);
                    }
                }
        );
    }

    public void navigateToSports(Node sourceNode,
                                 User currentUser) {

        final User finalUser = currentUser;

        if (currentUser == null) {

            System.err.println(
                    "currentUser is null in navigateToSports"
            );

            return;
        }

        boolean isCoach =
                currentUser.getRoles() != null
                        &&
                        currentUser.getRoles()
                                .contains("ROLE_COACH");

        if (isCoach) {

            navigateFrom(
                    sourceNode,
                    "/view/MenuCoach.fxml",
                    "BioSync - Espace Coach",
                    1100,
                    700,
                    ctrl -> {

                        if (ctrl instanceof MenuCoachController) {

                            MenuCoachController c =
                                    (MenuCoachController) ctrl;

                            c.setCurrentUser(finalUser);
                        }
                    }
            );

        } else {

            System.err.println(
                    "User is not a coach in navigateToSports"
            );
        }
    }

    public void navigateToLogin(Node sourceNode) {

        navigateFrom(
                sourceNode,
                "/view/login.fxml",
                "BioSync - Connexion",
                480,
                680
        );
    }

    public void navigateToForgotPassword(Node sourceNode) {

        navigateFrom(
                sourceNode,
                "/view/forgot_password.fxml",
                "BioSync - Mot de passe oublié",
                480,
                680
        );
    }

    public void navigateToUtilisateurs(Node sourceNode,
                                       User currentUser) {

        final User finalUser = currentUser;

        navigateFrom(
                sourceNode,
                "/view/utilisateurs.fxml",
                "BioSync - Gestion Utilisateurs",
                1100,
                700,
                ctrl -> {

                    if (ctrl instanceof UtilisateursController) {

                        ((UtilisateursController) ctrl)
                                .setCurrentUser(finalUser);
                    }
                }
        );
    }

    public void navigateToCertifications(Node sourceNode,
                                         User currentUser) {

        final User finalUser = currentUser;

        navigateFrom(
                sourceNode,
                "/view/certifications_admin.fxml",
                "BioSync - Certifications",
                1100,
                700,
                ctrl -> {

                    if (ctrl instanceof CertificationsAdminController) {

                        ((CertificationsAdminController) ctrl)
                                .setCurrentUser(finalUser);
                    }
                }
        );
    }

    public void navigateToCertificationRequest(Node sourceNode) {

        navigateFrom(
                sourceNode,
                "/view/certification_request.fxml",
                "BioSync - Demande Pro",
                520,
                750
        );
    }

    public void navigateToLogs(Node sourceNode,
                               User currentUser) {

        final User finalUser = currentUser;

        navigateFrom(
                sourceNode,
                "/view/logs_view.fxml",
                "BioSync - Logs",
                1200,
                750,
                ctrl -> {

                    if (ctrl instanceof LogsController) {

                        ((LogsController) ctrl)
                                .setCurrentUser(finalUser);
                    }
                }
        );
    }

    public void navigateToLogStats(Node sourceNode,
                                   User currentUser) {

        final User finalUser = currentUser;

        navigateFrom(
                sourceNode,
                "/view/logs_stats.fxml",
                "BioSync - Statistiques Logs",
                1200,
                750,
                ctrl -> {

                    if (ctrl instanceof LogsStatsController) {

                        ((LogsStatsController) ctrl)
                                .setCurrentUser(finalUser);
                    }
                }
        );
    }

    public void navigateToNutrition(Node sourceNode,
                                    User currentUser) {

        final User finalUser = currentUser;

        String roles =
                currentUser != null
                        ? currentUser.getRoles()
                        : "";

        boolean isCoachOrAdmin =
                roles != null
                        &&
                        (
                                roles.contains("ADMIN")
                                        ||
                                        roles.contains("COACH")
                        );

        if (isCoachOrAdmin) {

            navigateFrom(
                    sourceNode,
                    "/view/nutrition/coach_users.fxml",
                    "BioSync - Espace Coach",
                    1100,
                    700,
                    ctrl -> {

                        if (ctrl instanceof CoachUsersController) {

                            CoachUsersController c =
                                    (CoachUsersController) ctrl;

                            c.setCoachUser(finalUser);
                        }
                    }
            );

        } else {

            System.err.println(
                    "User is not a coach or admin"
            );
        }
    }

    public void navigateToMental(Node sourceNode,
                                 User currentUser) {

        navigateToQuizManager(sourceNode, currentUser);
    }

    public void navigateToQuizManager(Node sourceNode,
                                      User currentUser) {

        final User finalUser = currentUser;

        navigateFrom(
                sourceNode,
                "/view/quiz_manager.fxml",
                "BioSync - Gestion Quiz",
                1200,
                750,
                ctrl -> {

                    if (ctrl instanceof QuizController) {

                        QuizController c =
                                (QuizController) ctrl;

                        c.setCurrentUser(finalUser);
                    }
                }
        );
    }

    public void navigateToQuizForm(Node sourceNode,
                                   User currentUser,
                                   org.example.model.Quiz quiz) {

        final User finalUser = currentUser;

        navigateFrom(
                sourceNode,
                "/view/quiz_form.fxml",
                "BioSync - "
                        + (quiz == null
                        ? "Nouveau Quiz"
                        : "Modifier Quiz"),
                700,
                800,
                ctrl -> {

                    if (ctrl instanceof QuizFormController) {

                        QuizFormController c =
                                (QuizFormController) ctrl;

                        c.setCurrentUser(finalUser);
                    }
                }
        );
    }

    public void navigateToQuestionManager(
            Node sourceNode,
            User currentUser,
            org.example.model.Quiz quiz
    ) {

        final User finalUser = currentUser;

        navigateFrom(
                sourceNode,
                "/view/question_manager.fxml",
                "BioSync - Gestion Questions",
                1200,
                750,
                ctrl -> {

                    if (ctrl instanceof QuestionManagerController) {

                        QuestionManagerController c =
                                (QuestionManagerController) ctrl;

                        c.setCurrentUser(finalUser);
                        c.setQuiz(quiz);
                    }
                }
        );
    }

    public void navigateToQuizPlayer(
            Node sourceNode,
            User currentUser,
            org.example.model.Quiz quiz
    ) {

        final User finalUser = currentUser;

        navigateFrom(
                sourceNode,
                "/view/quiz_player.fxml",
                "BioSync - Quiz",
                900,
                700,
                ctrl -> {

                    if (ctrl instanceof QuizPlayerController) {

                        QuizPlayerController c =
                                (QuizPlayerController) ctrl;

                        c.setCurrentUser(finalUser);

                        if (quiz != null) {
                            c.setQuiz(quiz);
                        }
                    }
                }
        );
    }

    public void navigateToVueUtilisateur(Node sourceNode,
                                         User currentUser) {

        final User finalUser = currentUser;

        navigateFrom(
                sourceNode,
                "/view/vue_utilisateur.fxml",
                "BioSync - Quiz Patient",
                1100,
                750,
                ctrl -> {

                    if (ctrl instanceof VueUtilisateurController) {

                        ((VueUtilisateurController) ctrl)
                                .setCurrentUser(finalUser);
                    }
                }
        );
    }

    public void navigateToAiChat(Node sourceNode,
                                 User currentUser) {

        final User finalUser = currentUser;

        navigateFrom(
                sourceNode,
                "/view/ai_chat.fxml",
                "BioSync - Assistant IA",
                800,
                650,
                ctrl -> {

                    if (ctrl instanceof AiChatController) {

                        ((AiChatController) ctrl)
                                .setCurrentUser(finalUser);
                    }
                }
        );
    }

    public void navigateToCommunity(Node sourceNode,
                                    User currentUser) {

        navigateFrom(
                sourceNode,
                "/view/community.fxml",
                "BioSync - Communauté",
                1200,
                750
        );
    }

    public void navigateToDashboardCognitif(Node sourceNode) {

        navigateFrom(
                sourceNode,
                "/view/dashboard_cognitif.fxml",
                "BioSync - Dashboard Cognitif",
                1200,
                750
        );
    }

    public static class NavigationException
            extends RuntimeException {

        public NavigationException(String message,
                                   Throwable cause) {

            super(message, cause);
        }
    }
}