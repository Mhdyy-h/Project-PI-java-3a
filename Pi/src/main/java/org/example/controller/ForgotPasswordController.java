package org.example.controller;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.example.service.NavigationService;
import org.example.service.PasswordResetService;

/**
 * Contrôleur pour l'écran "Mot de passe oublié".
 * Étape 1: Saisir email → envoyer code
 * Étape 2: Saisir code + nouveau MDP → réinitialiser
 */
public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private Button sendCodeButton;
    @FXML private VBox codeSection;
    @FXML private TextField codeField;
    @FXML private PasswordField newPasswordField;
    @FXML private TextField newPasswordVisibleField;
    @FXML private Button eyeToggleBtn;
    @FXML private Button suggestPasswordBtn;
    @FXML private VBox passwordStrengthBox;
    @FXML private Region passwordStrengthTrack;
    @FXML private Region passwordStrengthFill;
    @FXML private Label passwordStrengthLabel;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField confirmPasswordVisibleField;
    @FXML private Button confirmEyeToggleBtn;
    @FXML private Button resetButton;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator progressIndicator;

    private final PasswordResetService resetService = PasswordResetService.getInstance();
    private final NavigationService navigationService = NavigationService.getInstance();
    private final org.example.service.PasswordStrengthService passwordStrengthService = org.example.service.PasswordStrengthService.getInstance();
    private final org.example.service.ThemeService themeService = org.example.service.ThemeService.getInstance();
    private String currentEmail = "";
    private boolean passwordVisible = false;
    private boolean confirmPasswordVisible = false;

    @FXML
    public void initialize() {
        codeSection.setVisible(false);
        codeSection.setManaged(false);
        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }

        // Password strength listener
        setupPasswordStrengthListener();

        // Register scene for theme
        Platform.runLater(() -> {
            if (newPasswordField != null && newPasswordField.getScene() != null) {
                themeService.registerScene(newPasswordField.getScene());
            }
        });
    }

    private void setupPasswordStrengthListener() {
        if (newPasswordField != null) {
            newPasswordField.textProperty().addListener((obs, oldVal, newVal) -> updatePasswordStrength(newVal));
        }
        if (newPasswordVisibleField != null) {
            newPasswordVisibleField.textProperty().addListener((obs, oldVal, newVal) -> updatePasswordStrength(newVal));
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
    public void handleEyeToggle() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            newPasswordVisibleField.setText(newPasswordField.getText());
            newPasswordField.setVisible(false);
            newPasswordField.setManaged(false);
            newPasswordVisibleField.setVisible(true);
            newPasswordVisibleField.setManaged(true);
            if (eyeToggleBtn != null) eyeToggleBtn.setText("🙈");
        } else {
            newPasswordField.setText(newPasswordVisibleField.getText());
            newPasswordVisibleField.setVisible(false);
            newPasswordVisibleField.setManaged(false);
            newPasswordField.setVisible(true);
            newPasswordField.setManaged(true);
            if (eyeToggleBtn != null) eyeToggleBtn.setText("👁");
        }
    }

    @FXML
    public void handleConfirmEyeToggle() {
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
    public void handleSuggestPassword() {
        String suggested = passwordStrengthService.generateStrongPassword();
        newPasswordField.setText(suggested);
        if (newPasswordVisibleField != null) newPasswordVisibleField.setText(suggested);
        if (confirmPasswordField != null) confirmPasswordField.setText(suggested);
        if (confirmPasswordVisibleField != null) confirmPasswordVisibleField.setText(suggested);
    }

    private String getNewPasswordText() {
        if (passwordVisible) {
            return newPasswordVisibleField != null ? newPasswordVisibleField.getText() : "";
        }
        return newPasswordField != null ? newPasswordField.getText() : "";
    }

    private String getConfirmPasswordText() {
        if (confirmPasswordVisible) {
            return confirmPasswordVisibleField != null ? confirmPasswordVisibleField.getText() : "";
        }
        return confirmPasswordField != null ? confirmPasswordField.getText() : "";
    }

    @FXML
    public void handleSendCode() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showStatus("Veuillez entrer votre adresse email.", false);
            return;
        }

        sendCodeButton.setDisable(true);
        if (progressIndicator != null) progressIndicator.setVisible(true);
        showStatus("Envoi du code en cours...", true);

        // Exécuter en arrière-plan pour ne pas bloquer l'UI
        new Thread(() -> {
            boolean sent = resetService.sendResetCode(email);
            Platform.runLater(() -> {
                sendCodeButton.setDisable(false);
                if (progressIndicator != null) progressIndicator.setVisible(false);

                if (sent) {
                    currentEmail = email;
                    codeSection.setVisible(true);
                    codeSection.setManaged(true);
                    emailField.setDisable(true);
                    showStatus("✓ Code envoyé à " + email + ". Vérifiez votre boîte mail.", true);
                } else {
                    showStatus("Aucun compte trouvé pour cet email, ou erreur d'envoi.", false);
                }
            });
        }).start();
    }

    @FXML
    public void handleResetPassword() {
        String code = codeField.getText().trim();
        String newPassword = getNewPasswordText();
        String confirmPassword = getConfirmPasswordText();

        if (code.isEmpty()) {
            showStatus("Veuillez entrer le code reçu par email.", false);
            return;
        }
        if (newPassword.isEmpty()) {
            showStatus("Veuillez entrer un nouveau mot de passe.", false);
            return;
        }
        if (newPassword.length() < 8) {
            showStatus("Le mot de passe doit contenir au moins 8 caractères.", false);
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            showStatus("Les mots de passe ne correspondent pas.", false);
            return;
        }

        resetButton.setDisable(true);
        boolean success = resetService.resetPassword(currentEmail, code, newPassword);
        resetButton.setDisable(false);

        if (success) {
            showStatus("✓ Mot de passe réinitialisé avec succès! Redirection...", true);
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> handleBackToLogin());
            pause.play();
        } else {
            showStatus("Code invalide ou expiré. Veuillez réessayer.", false);
        }
    }

    @FXML
    public void handleBackToLogin() {
        Node source = emailField;
        navigationService.navigateToLogin(source);
    }

    @FXML
    public void handleResendCode() {
        if (!currentEmail.isEmpty()) {
            emailField.setDisable(false);
            codeSection.setVisible(false);
            codeSection.setManaged(false);
            handleSendCode();
        }
    }

    private void showStatus(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.setStyle(success
            ? "-fx-text-fill: #10b981; -fx-font-size: 12px;"
            : "-fx-text-fill: #ef4444; -fx-font-size: 12px;");
    }
}
