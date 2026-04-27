package org.example.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.model.User;
import org.example.service.*;

public class LoginController {

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private CheckBox termsCheckBox;
    @FXML private Button createAccountButton;
    @FXML private Hyperlink loginLink;
    @FXML private Hyperlink healthProLink;
    @FXML private Label statusLabel;
    @FXML private VBox fullNameBox;
    @FXML private VBox confirmPasswordBox;
    @FXML private VBox termsBox;
    @FXML private Hyperlink registerLink;
    @FXML private HBox registerBox;
    @FXML private Button backButton;
    @FXML private VBox faceIdSection;
    // New: Forgot password
    @FXML private HBox forgotPasswordBox;
    @FXML private Hyperlink forgotPasswordLink;
    private final AuthService authService = AuthService.getInstance();
    private final NavigationService navigationService = NavigationService.getInstance();
    private final FaceRecognitionService faceService = FaceRecognitionService.getInstance();
    private final RateLimiterService rateLimiter = RateLimiterService.getInstance();
    private boolean isLoginMode = false;

    @FXML
    public void initialize() {
        if (faceIdSection != null) {
            faceIdSection.setVisible(false);
            faceIdSection.setManaged(false);
        }
        if (forgotPasswordBox != null) {
            forgotPasswordBox.setVisible(false);
            forgotPasswordBox.setManaged(false);
        }
    }

    @FXML
    public void handleCreateAccount(ActionEvent event) {
        if (isLoginMode) {
            handleLogin(event);
            return;
        }

        String email = emailField.getText().trim();

        // Vérifier le rate limiting
        if (!rateLimiter.isAllowed(email)) {
            String errorMsg = rateLimiter.getErrorMessage(email);
            updateStatus(errorMsg != null ? errorMsg :
                "Trop de tentatives. Veuillez réessayer plus tard.", false);
            return;
        }

        AuthService.AuthResult result = authService.register(
            fullNameField.getText().trim(),
            email,
            passwordField.getText(),
            confirmPasswordField.getText(),
            termsCheckBox.isSelected()
        );

        // Enregistrer la tentative
        rateLimiter.recordAttempt(email);

        updateStatus(result.getMessage(), result.isSuccess());
        if (result.isSuccess()) {
            rateLimiter.resetAttempts(email); // Réinitialiser en cas de succès
            navigationService.navigateToDashboard((Node) event.getSource(), result.getUser());
        }
    }

    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();

        // Vérifier le rate limiting
        if (!rateLimiter.isAllowed(email)) {
            String errorMsg = rateLimiter.getErrorMessage(email);
            updateStatus(errorMsg != null ? errorMsg :
                "Trop de tentatives. Veuillez réessayer plus tard.", false);
            return;
        }

        AuthService.AuthResult result = authService.login(
            email,
            passwordField.getText()
        );

        // Enregistrer la tentative
        rateLimiter.recordAttempt(email);

        updateStatus(result.getMessage(), result.isSuccess());
        if (result.isSuccess()) {
            rateLimiter.resetAttempts(email); // Réinitialiser en cas de succès
            navigationService.navigateToDashboard((Node) event.getSource(), result.getUser());
        }
    }

    @FXML
    public void handleLoginLink(ActionEvent event) {
        isLoginMode = true;
        setLoginMode(true);
    }

    @FXML
    public void handleRegisterLink(ActionEvent event) {
        isLoginMode = false;
        setLoginMode(false);
    }

    @FXML
    public void handleBack(ActionEvent event) {
        handleRegisterLink(event);
    }

    @FXML
    public void handleHealthProLink(ActionEvent event) {
        navigationService.navigateToCertificationRequest((Node) event.getSource());
    }

    @FXML
    public void handleForgotPassword(ActionEvent event) {
        navigationService.navigateToForgotPassword((Node) event.getSource());
    }

    /**
     * Face ID button handler
     */
    @FXML
    public void handleFaceId(ActionEvent event) {
        TextInputDialog emailDialog = new TextInputDialog();
        emailDialog.setTitle("Connexion Face ID");
        emailDialog.setHeaderText("Face ID – BioSync");
        emailDialog.setContentText("Entrez votre adresse email :");
        emailDialog.getEditor().setPromptText("votre@email.com");

        emailDialog.showAndWait().ifPresent(email -> {
            if (email.trim().isEmpty()) {
                updateStatus("Veuillez entrer votre email.", false);
                return;
            }
            String trimmedEmail = email.trim();
            User user = faceService.getUserByEmail(trimmedEmail);
            if (user == null) {
                updateStatus("Aucun compte trouvé pour cet email.", false);
                return;
            }
            boolean isFaceIdRegistered = faceService.isFaceIdRegistered(user.getId());
            FaceIDController.openFaceIDDialog(trimmedEmail, user, (Node) event.getSource());
        });
    }

    private void setLoginMode(boolean login) {
        fullNameBox.setVisible(!login);
        fullNameBox.setManaged(!login);
        confirmPasswordBox.setVisible(!login);
        confirmPasswordBox.setManaged(!login);
        termsBox.setVisible(!login);
        termsBox.setManaged(!login);
        healthProLink.setVisible(!login);
        healthProLink.setManaged(!login);
        loginLink.setVisible(!login);
        loginLink.setManaged(!login);
        registerBox.setVisible(login);
        registerBox.setManaged(login);
        backButton.setVisible(login);
        backButton.setManaged(login);
        createAccountButton.setText(login ? "Se connecter" : "Créer mon compte");
        statusLabel.setText("");

        // Forgot password: visible only in login mode
        if (forgotPasswordBox != null) {
            forgotPasswordBox.setVisible(login);
            forgotPasswordBox.setManaged(login);
        }

        // Face ID: visible only in login mode
        if (faceIdSection != null) {
            faceIdSection.setVisible(login);
            faceIdSection.setManaged(login);
        }
    }

    private void updateStatus(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().clear();
        statusLabel.getStyleClass().add(success ? "status-success" : "status-error");
    }
}
