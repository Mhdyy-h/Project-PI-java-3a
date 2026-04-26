package org.example.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.model.User;
import org.example.util.UserSession;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * BaseController - Abstract base class for all controllers
 * Provides standardized navigation, user session management, and common utilities
 */
public abstract class BaseController {
    
    private static final Logger LOGGER = Logger.getLogger(BaseController.class.getName());
    
    // Standard view paths
    public static final String VIEW_PATH = "/view/";
    public static final String STYLES_PATH = "/styles/";
    
    // View FXML files
    public static final String VIEW_LOGIN = VIEW_PATH + "login.fxml";
    public static final String VIEW_DASHBOARD = VIEW_PATH + "dashboard.fxml";
    public static final String VIEW_QUIZ_MANAGER = VIEW_PATH + "quiz_manager.fxml";
    public static final String VIEW_QUIZ_FORM = VIEW_PATH + "quiz_form.fxml";
    public static final String VIEW_QUIZ_PLAYER = VIEW_PATH + "quiz_player.fxml";
    public static final String VIEW_RESULT = VIEW_PATH + "result.fxml";
    public static final String VIEW_QUESTION_MANAGER = VIEW_PATH + "question_manager.fxml";
    public static final String VIEW_USERS = VIEW_PATH + "utilisateurs.fxml";
    public static final String VIEW_CERTIFICATIONS = VIEW_PATH + "certifications_admin.fxml";
    public static final String VIEW_CERTIFICATION_REQUEST = VIEW_PATH + "certification_request.fxml";
    
    /**
     * Load a new scene/window
     * @param fxmlPath Path to FXML file
     * @param title Window title
     * @param width Window width
     * @param height Window height
     * @return true if loaded successfully
     */
    protected boolean loadPage(String fxmlPath, String title, double width, double height) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                LOGGER.log(Level.SEVERE, "FXML file not found: " + fxmlPath);
                showErrorAlert("Navigation Error", "Could not find view: " + fxmlPath);
                return false;
            }
            
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            Object rawController = loader.getController();
            if (rawController instanceof BaseController) {
                ((BaseController) rawController).initializeWithUser(getCurrentUser());
            }

            Stage stage = new Stage();
            stage.setScene(new Scene(root, width, height));
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();
            
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load view: " + fxmlPath, e);
            showErrorAlert("Navigation Error", "Could not load view: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Load a modal dialog
     * @param fxmlPath Path to FXML file
     * @param title Dialog title
     * @param owner Owner stage
     * @return Optional containing the controller if loaded successfully
     */
    protected <T extends BaseController> Optional<T> loadModal(String fxmlPath, String title, Stage owner) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                LOGGER.log(Level.SEVERE, "FXML file not found: " + fxmlPath);
                return Optional.empty();
            }
            
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(owner);
            dialogStage.initStyle(StageStyle.DECORATED);
            
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            Object rawController = loader.getController();
            if (!(rawController instanceof BaseController)) {
                LOGGER.log(Level.WARNING, "Modal controller does not extend BaseController: " + fxmlPath);
                dialogStage.showAndWait();
                return Optional.empty();
            }
            @SuppressWarnings("unchecked")
            T controller = (T) rawController;
            controller.initializeWithUser(getCurrentUser());

            dialogStage.showAndWait();
            return Optional.of(controller);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load modal: " + fxmlPath, e);
            showErrorAlert("Dialog Error", "Could not open dialog: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Navigate to login screen and close current window
     */
    protected void navigateToLogin(Stage currentStage) {
        loadPage(VIEW_LOGIN, "BioSync - Login", 900, 600);
        if (currentStage != null) {
            currentStage.close();
        }
        UserSession.getInstance().clearSession();
    }
    
    /**
     * Navigate to dashboard
     */
    protected void navigateToDashboard() {
        loadPage(VIEW_DASHBOARD, "BioSync - Dashboard", 1200, 800);
    }
    
    /**
     * Navigate to quiz management
     */
    protected void navigateToQuizManager() {
        loadPage(VIEW_QUIZ_MANAGER, "BioSync - Quiz Management", 1200, 800);
    }
    
    /**
     * Navigate to quiz player
     */
    protected void navigateToQuizPlayer(int quizId) {
        loadPage(VIEW_QUIZ_PLAYER, "BioSync - Quiz", 1000, 700);
    }
    
    /**
     * Show confirmation dialog
     * @param title Dialog title
     * @param message Confirmation message
     * @return true if user confirmed
     */
    protected boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Show error alert
     * @param title Alert title
     * @param message Error message
     */
    protected void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show success alert
     * @param title Alert title
     * @param message Success message
     */
    protected void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Get current logged-in user
     * @return Current user or null if not logged in
     */
    protected User getCurrentUser() {
        return UserSession.getInstance().getCurrentUser();
    }
    
    /**
     * Check if current user has specific role
     * @param role Role to check
     * @return true if user has the role
     */
    protected boolean hasRole(String role) {
        User user = getCurrentUser();
        return user != null && UserSession.getInstance().hasRole(role);
    }
    
    /**
     * Check if current user is admin
     * @return true if admin
     */
    protected boolean isAdmin() {
        return hasRole("ADMIN") || hasRole("ROLE_ADMIN");
    }
    
    /**
     * Check if current user is coach
     * @return true if coach
     */
    protected boolean isCoach() {
        return hasRole("COACH") || hasRole("ROLE_COACH");
    }
    
    /**
     * Check if user is logged in
     * @return true if authenticated
     */
    protected boolean isAuthenticated() {
        return UserSession.getInstance().isAuthenticated();
    }
    
    /**
     * Initialize controller with user data
     * Override this method in subclasses to perform initialization
     * @param user The current user
     */
    public void initializeWithUser(User user) {
        // Override in subclasses
    }
    
    /**
     * Log message for debugging
     * @param message Message to log
     */
    protected void log(String message) {
        LOGGER.log(Level.INFO, message);
    }
    
    /**
     * Log error with exception
     * @param message Error message
     * @param e Exception
     */
    protected void logError(String message, Exception e) {
        LOGGER.log(Level.SEVERE, message, e);
    }
}
