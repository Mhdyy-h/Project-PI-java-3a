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

    public void navigateTo(String fxmlPath, String title, double width, double height) {
        navigateTo(fxmlPath, title, width, height, null);
    }

    // Helper for direct navigation if no source node is available (via mainStage)
    public void navigateTo(String fxmlPath, String title, double width, double height, Consumer<Object> setup) {
        if (mainStage == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            if (setup != null) setup.accept(loader.getController());
            Stage stage = mainStage != null ? mainStage : new Stage();
            Scene scene = new Scene(root, width, height);
            themeService.registerScene(scene);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            throw new NavigationException("Failed to navigate to " + fxmlPath, e);
        }
    }

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

    // ✅ Après login → redirection selon le rôle
    public void navigateToDashboard(Node sourceNode, User currentUser) {
        if (currentUser == null) {
            System.err.println("NavigationService: currentUser is null, cannot navigate to dashboard.");
            return;
        }
        boolean isAdmin = currentUser.getRoles() != null
                && currentUser.getRoles().contains("ROLE_ADMIN");
        boolean isCoach = currentUser.getRoles() != null
                && currentUser.getRoles().contains("ROLE_COACH");

        if (isAdmin) {
            org.example.model.Session.role = "ADMIN";
            navigateFrom(sourceNode, "/view/dashboard.fxml", "BioSync - Dashboard Admin", 1100, 700, ctrl -> {
                if (ctrl instanceof AdminController c)
                    c.setUser(currentUser);
            });
        } else if (isCoach) {
            org.example.model.Session.role = "COACH";
            navigateFrom(sourceNode, "/view/MenuCoach.fxml", "BioSync - Espace Coach", 1100, 700, ctrl -> {
                if (ctrl instanceof org.example.controller.MenuCoachController c)
                    c.setCurrentUser(currentUser);
            });
        } else {
            // Regular USER → goes to user dashboard
            org.example.model.Session.role = "USER";
            navigateFrom(sourceNode, "/view/dashboard_user.fxml", "BioSync - Dashboard", 1100, 700, ctrl -> {
                if (ctrl instanceof org.example.controller.UserDashboardController c)
                    c.setCurrentUser(currentUser);
            });
        }
    }

    // ✅ Depuis bouton Sports → selon le rôle
    public void navigateToSports(Node sourceNode, User currentUser) {
        boolean isAdmin = currentUser.getRoles() != null
                && currentUser.getRoles().contains("ROLE_ADMIN");
        boolean isCoach = currentUser.getRoles() != null
                && currentUser.getRoles().contains("ROLE_COACH");

        if (isAdmin || isCoach) {
            // Admin/Coach → MenuCoach
            navigateFrom(sourceNode, "/view/MenuCoach.fxml", "BioSync - Espace Coach", 1100, 700, ctrl -> {
                if (ctrl instanceof org.example.controller.MenuCoachController c)
                    c.setCurrentUser(currentUser);
            });
        } else {
            // User → MenuUser
            navigateFrom(sourceNode, "/view/MenuUser.fxml", "BioSync - Espace Sport", 1100, 700, ctrl -> {
                if (ctrl instanceof org.example.controller.MenuUserController c)
                    c.setCurrentUser(currentUser);
            });
        }
    }

    // ✅ INTACT — travail de tes amis
    public void navigateToLogin(Node sourceNode) {
        navigateFrom(sourceNode, "/view/login.fxml", "BioSync - Connexion", 480, 680);
    }

    public void navigateToForgotPassword(Node sourceNode) {
        navigateFrom(sourceNode, "/view/forgot_password.fxml", "BioSync - Mot de passe oublié", 480, 680);
    }

    public void navigateToUtilisateurs(Node sourceNode, User currentUser) {
        navigateFrom(sourceNode, "/view/utilisateurs.fxml", "BioSync - Gestion Utilisateurs", 1100, 700, ctrl -> {
            if (ctrl instanceof UtilisateursController) {
                ((UtilisateursController) ctrl).setCurrentUser(currentUser);
            }
        });
    }

    public void navigateToCommunity(Node sourceNode, User currentUser) {
        boolean isAdmin = currentUser != null && currentUser.getRoles() != null
                && currentUser.getRoles().contains("ROLE_ADMIN");

        if (isAdmin) {
            // Admin → community_home.fxml (CommunityController)
            navigateFrom(sourceNode, "/view/community_home.fxml", "BioSync - Communauté Admin", 1100, 700, ctrl -> {
                if (ctrl instanceof CommunityController) {
                    ((CommunityController) ctrl).setCurrentUser(currentUser);
                }
            });
        } else {
            // User/Coach → user_community.fxml
            navigateFrom(sourceNode, "/view/user_community.fxml", "BioSync - Communauté", 1100, 700, ctrl -> {
                if (ctrl instanceof UserCommunityController) {
                    ((UserCommunityController) ctrl).setCurrentUser(currentUser);
                }
            });
        }
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

    public void navigateToMental(Node sourceNode, User currentUser) {
        boolean isAdmin = currentUser != null && currentUser.getRoles() != null
                && currentUser.getRoles().contains("ROLE_ADMIN");

        if (isAdmin) {
            // Admin → quiz_manager.fxml
            navigateToQuizManager(sourceNode, currentUser);
        } else {
            // User → vue_utilisateur.fxml
            navigateToVueUtilisateur(sourceNode, currentUser);
        }
    }

    public void navigateToMentalHealthDashboard(Node sourceNode, User currentUser) {
        navigateFrom(sourceNode, "/view/mental_health_dashboard.fxml", "BioSync - Santé Mentale", 1200, 800, ctrl -> {
            if (ctrl instanceof MentalHealthDashboardController) {
                ((MentalHealthDashboardController) ctrl).setCurrentUser(currentUser);
            }
        });
    }

    public void navigateToQuizManager(Node sourceNode, User currentUser) {
        navigateFrom(sourceNode, "/view/quiz_manager.fxml", "BioSync - Gestion Quiz", 1200, 750, ctrl -> {
            if (ctrl instanceof QuizController) {
                ((QuizController) ctrl).setCurrentUser(currentUser);
            }
        });
    }

    public void navigateToQuizForm(Node sourceNode, User currentUser, org.example.model.Quiz quiz) {
        navigateFrom(sourceNode, "/view/quiz_form.fxml", "BioSync - " + (quiz == null ? "Nouveau Quiz" : "Modifier Quiz"), 700, 800, ctrl -> {
            if (ctrl instanceof QuizFormController) {
                ((QuizFormController) ctrl).setCurrentUser(currentUser);
                if (quiz != null) {
                    ((QuizFormController) ctrl).setQuiz(quiz);
                }
            }
        });
    }

    public void navigateToQuestionManager(Node sourceNode, User currentUser, org.example.model.Quiz quiz) {
        navigateFrom(sourceNode, "/view/question_manager.fxml", "BioSync - Gestion Questions", 1200, 750, ctrl -> {
            if (ctrl instanceof QuestionManagerController) {
                ((QuestionManagerController) ctrl).setCurrentUser(currentUser);
                ((QuestionManagerController) ctrl).setQuiz(quiz);
            }
        });
    }

    public void navigateToQuizPlayer(Node sourceNode, User currentUser, org.example.model.Quiz quiz) {
        navigateFrom(sourceNode, "/view/quiz_player.fxml", "BioSync - Quiz", 900, 700, ctrl -> {
            if (ctrl instanceof QuizPlayerController) {
                ((QuizPlayerController) ctrl).setCurrentUser(currentUser);
                if (quiz != null) {
                    ((QuizPlayerController) ctrl).setQuiz(quiz);
                }
            }
        });
    }

    public void navigateToVueUtilisateur(Node sourceNode, User currentUser) {
        navigateFrom(sourceNode, "/view/vue_utilisateur.fxml", "BioSync - Quiz Patient", 1100, 750, ctrl -> {
            if (ctrl instanceof VueUtilisateurController) {
                ((VueUtilisateurController) ctrl).setCurrentUser(currentUser);
            }
        });
    }

    public void navigateToAiChat(Node sourceNode, User currentUser) {
        navigateFrom(sourceNode, "/view/ai_chat.fxml", "BioSync - Assistant IA", 800, 650, ctrl -> {
            if (ctrl instanceof AiChatController) {
                ((AiChatController) ctrl).setCurrentUser(currentUser);
            }
        });
    }

    public void navigateToDashboardCognitif(Node sourceNode) {
        navigateFrom(sourceNode, "/view/DashboardCognitif.fxml", "BioSync - Dashboard Cognitif", 1200, 750);
    }

    public void navigateToDashboardCognitif(Node sourceNode, User user) {
        if (user != null) org.example.util.SessionContext.connecter(user);
        navigateFrom(sourceNode, "/view/DashboardCognitif.fxml", "BioSync - Dashboard Cognitif", 1200, 750);
    }

    // ============================================================
    // ADDITIONAL NAVIGATION METHODS FOR DASHBOARD
    // ============================================================

    public void navigateToCoachRepas(Node sourceNode, User currentUser) {
        navigateFrom(sourceNode, "/view/nutrition/coach_repas.fxml", "BioSync - Coach Nutrition", 1100, 700, ctrl -> {
            if (ctrl instanceof CoachRepasController) {
                ((CoachRepasController) ctrl).setCoachUser(currentUser);
                // Note: setSelectedUser doit être appelé après avoir sélectionné un utilisateur
            }
        });
    }

    public void navigateToAfficherSeance(Node sourceNode, User currentUser) {
        navigateFrom(sourceNode, "/view/AfficherSeance.fxml", "BioSync - Séances Sport", 1100, 700);
    }

    public void navigateToGroupManager(Node sourceNode, User currentUser) {
        navigateFrom(sourceNode, "/view/user_community.fxml", "BioSync - Gestion Groupes", 1100, 700, ctrl -> {
            if (ctrl instanceof UserCommunityController) {
                ((UserCommunityController) ctrl).setCurrentUser(currentUser);
            }
        });
    }

    public void navigateToUserDashboard(Node sourceNode, User currentUser) {
        navigateFrom(sourceNode, "/view/dashboard_user.fxml", "BioSync - Dashboard", 1100, 700, ctrl -> {
            if (ctrl instanceof UserDashboardController) {
                ((UserDashboardController) ctrl).setCurrentUser(currentUser);
            }
        });
    }
    public void navigateToCertificationsAdmin(Node sourceNode, User currentUser) {
        navigateFrom(sourceNode, "/view/certifications_admin.fxml", "BioSync - Certifications", 1100, 700, ctrl -> {
            if (ctrl instanceof CertificationsAdminController) {
                ((CertificationsAdminController) ctrl).setCurrentUser(currentUser);
            }
        });
    }

    public static class NavigationException extends RuntimeException {
        public NavigationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}