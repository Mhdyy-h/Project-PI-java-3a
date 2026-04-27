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
    // New: reCAPTCHA
    @FXML private HBox captchaBox;
    @FXML private ProgressIndicator captchaProgress;
    @FXML private Label captchaLabel;

    private final AuthService authService = AuthService.getInstance();
    private final NavigationService navigationService = NavigationService.getInstance();
    private final FaceRecognitionService faceService = FaceRecognitionService.getInstance();
    private final RecaptchaService recaptchaService = RecaptchaService.getInstance();
    private final RecaptchaWebView recaptchaWebView = new RecaptchaWebView();
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
        if (captchaBox != null) {
            captchaBox.setVisible(false);
            captchaBox.setManaged(false);
        }
    }

    @FXML
    public void handleCreateAccount(ActionEvent event) {
        if (isLoginMode) {
            handleLogin(event);
            return;
        }

        // reCAPTCHA check before register
        executeWithRecaptcha("register", event, () -> {
            AuthService.AuthResult result = authService.register(
                fullNameField.getText().trim(),
                emailField.getText().trim(),
                passwordField.getText(),
                confirmPasswordField.getText(),
                termsCheckBox.isSelected()
            );
            updateStatus(result.getMessage(), result.isSuccess());
            if (result.isSuccess()) {
                navigationService.navigateToDashboard((Node) event.getSource(), result.getUser());
            }
        });
    }

    private void handleLogin(ActionEvent event) {
        // reCAPTCHA check before login
        executeWithRecaptcha("login", event, () -> {
            AuthService.AuthResult result = authService.login(
                emailField.getText().trim(),
                passwordField.getText()
            );
            updateStatus(result.getMessage(), result.isSuccess());
            if (result.isSuccess()) {
                navigationService.navigateToDashboard((Node) event.getSource(), result.getUser());
            }
        });
    }

    /**
     * Exécute reCAPTCHA v3 puis lance l'action si le score est suffisant.
     * Si reCAPTCHA n'est pas configuré, exécute directement.
     */
    private void executeWithRecaptcha(String action, ActionEvent event, Runnable onSuccess) {
        if (!recaptchaService.isConfigured()) {
            // reCAPTCHA non configuré, exécuter directement
            onSuccess.run();
            return;
        }

        // Afficher l'indicateur de chargement
        if (captchaBox != null) {
            captchaBox.setVisible(true);
            captchaBox.setManaged(true);
        }
        createAccountButton.setDisable(true);
        updateStatus("", true);

        recaptchaWebView.execute(action).thenAccept(token -> {
            Platform.runLater(() -> {
                if (captchaBox != null) {
                    captchaBox.setVisible(false);
                    captchaBox.setManaged(false);
                }
                createAccountButton.setDisable(false);

                if (token.startsWith("ERROR:") || token.equals("NOT_CONFIGURED")) {
                    // En cas d'erreur reCAPTCHA, laisser passer (fallback)
                    System.out.println("[reCAPTCHA] Erreur/non-configuré, fallback: " + token);
                    onSuccess.run();
                    return;
                }

                // Vérifier le token côté serveur
                double score = recaptchaService.verify(token);
                if (score >= recaptchaService.getThreshold()) {
                    System.out.println("[reCAPTCHA] Score OK: " + score);
                    onSuccess.run();
                } else {
                    updateStatus("⚠ Vérification de sécurité échouée (score: " +
                        String.format("%.1f", score) + "). Veuillez réessayer.", false);
                }
            });
        });
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
