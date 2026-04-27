package org.example.controller;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
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
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button resetButton;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator progressIndicator;

    private final PasswordResetService resetService = PasswordResetService.getInstance();
    private final NavigationService navigationService = NavigationService.getInstance();
    private String currentEmail = "";

    @FXML
    public void initialize() {
        codeSection.setVisible(false);
        codeSection.setManaged(false);
        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }
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
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

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
