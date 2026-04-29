package org.example.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.model.User;
import org.example.service.*;

public class LoginController {

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private Button eyeToggleBtn;
    @FXML private Button suggestPasswordBtn;
    @FXML private VBox passwordStrengthBox;
    @FXML private Region passwordStrengthTrack;
    @FXML private Region passwordStrengthFill;
    @FXML private Label passwordStrengthLabel;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField confirmPasswordVisibleField;
    @FXML private Button confirmEyeToggleBtn;
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
    @FXML private HBox forgotPasswordBox;
    @FXML private Hyperlink forgotPasswordLink;
    private final AuthService authService = AuthService.getInstance();
    private final NavigationService navigationService = NavigationService.getInstance();
    private final FaceRecognitionService faceService = FaceRecognitionService.getInstance();
    private final RateLimiterService rateLimiter = RateLimiterService.getInstance();
    private final PasswordStrengthService passwordStrengthService = PasswordStrengthService.getInstance();
    private final ThemeService themeService = ThemeService.getInstance();
    private boolean isLoginMode = false;
    private boolean passwordVisible = false;
    private boolean confirmPasswordVisible = false;

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

        // Password strength listener
        setupPasswordStrengthListener();

        // Register scene for theme
        Platform.runLater(() -> {
            if (passwordField != null && passwordField.getScene() != null) {
                themeService.registerScene(passwordField.getScene());
            }
        });
    }

    private void setupPasswordStrengthListener() {
        if (passwordField != null) {
            passwordField.textProperty().addListener((obs, oldVal, newVal) -> updatePasswordStrength(newVal));
        }
        if (passwordVisibleField != null) {
            passwordVisibleField.textProperty().addListener((obs, oldVal, newVal) -> updatePasswordStrength(newVal));
        }
    }

    private void updatePasswordStrength(String password) {
        if (passwordStrengthBox == null) return;

        boolean show = password != null && !password.isEmpty();
        passwordStrengthBox.setVisible(show);
        passwordStrengthBox.setManaged(show);

        if (suggestPasswordBtn != null) {
            suggestPasswordBtn.setVisible(show);
            suggestPasswordBtn.setManaged(show);
        }

        if (!show) return;

        double strength = passwordStrengthService.calculateStrength(password);
        String color = passwordStrengthService.getStrengthColor(strength);
        String label = passwordStrengthService.getStrengthLabel(strength);

        // Update fill bar width
        double trackWidth = passwordStrengthTrack != null ? passwordStrengthTrack.getWidth() : 280;
        double fillWidth = trackWidth * strength;
        if (passwordStrengthFill != null) {
            passwordStrengthFill.setPrefWidth(Math.max(0, fillWidth));
            passwordStrengthFill.setStyle("-fx-background-color: " + color + ";");
        }
        if (passwordStrengthLabel != null) {
            passwordStrengthLabel.setText(label);
            passwordStrengthLabel.setStyle("-fx-text-fill: " + color + ";");
        }
    }

    @FXML
    public void handleEyeToggle(ActionEvent event) {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            passwordVisibleField.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordVisibleField.setVisible(true);
            passwordVisibleField.setManaged(true);
            if (eyeToggleBtn != null) eyeToggleBtn.setText("🙈");
        } else {
            passwordField.setText(passwordVisibleField.getText());
            passwordVisibleField.setVisible(false);
            passwordVisibleField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            if (eyeToggleBtn != null) eyeToggleBtn.setText("👁");
        }
    }

    @FXML
    public void handleConfirmEyeToggle(ActionEvent event) {
        confirmPasswordVisible = !confirmPasswordVisible;
        if (confirmPasswordVisible) {
            confirmPasswordVisibleField.setText(confirmPasswordField.getText());
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
            confirmPasswordVisibleField.setVisible(true);
            confirmPasswordVisibleField.setManaged(true);
            if (confirmEyeToggleBtn != null) confirmEyeToggleBtn.setText("🙈");
        } else {
            confirmPasswordField.setText(confirmPasswordVisibleField.getText());
            confirmPasswordVisibleField.setVisible(false);
            confirmPasswordVisibleField.setManaged(false);
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            if (confirmEyeToggleBtn != null) confirmEyeToggleBtn.setText("👁");
        }
    }

    @FXML
    public void handleSuggestPassword(ActionEvent event) {
        String suggested = passwordStrengthService.generateStrongPassword();
        passwordField.setText(suggested);
        if (passwordVisibleField != null) passwordVisibleField.setText(suggested);
        if (confirmPasswordField != null) confirmPasswordField.setText(suggested);
        if (confirmPasswordVisibleField != null) confirmPasswordVisibleField.setText(suggested);
    }

    /**
     * Helper to get password text from either visible or hidden field.
     */
    private String getPasswordText() {
        if (passwordVisible) {
            return passwordVisibleField != null ? passwordVisibleField.getText() : "";
        }
        return passwordField != null ? passwordField.getText() : "";
    }

    private String getConfirmPasswordText() {
        if (confirmPasswordVisible) {
            return confirmPasswordVisibleField != null ? confirmPasswordVisibleField.getText() : "";
        }
        return confirmPasswordField != null ? confirmPasswordField.getText() : "";
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
            getPasswordText(),
            getConfirmPasswordText(),
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
            getPasswordText()
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
