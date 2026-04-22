package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.model.User;
import org.example.service.AuthService;
import org.example.service.NavigationService;

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

    private final AuthService authService = AuthService.getInstance();
    private final NavigationService navigationService = NavigationService.getInstance();
    private boolean isLoginMode = false;

    @FXML
    public void initialize() {
        // Face ID section only visible in login mode
        if (faceIdSection != null) {
            faceIdSection.setVisible(false);
            faceIdSection.setManaged(false);
        }
    }

    @FXML
    public void handleCreateAccount(ActionEvent event) {
        if (isLoginMode) {
            handleLogin(event);
            return;
        }

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
    }

    private void handleLogin(ActionEvent event) {
        AuthService.AuthResult result = authService.login(
            emailField.getText().trim(),
            passwordField.getText()
        );

        updateStatus(result.getMessage(), result.isSuccess());

        if (result.isSuccess()) {
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

    /**
     * Face ID button handler — simulates face recognition.
     * In a real app, this would open the camera, capture a frame and compare.
     * Here, we prompt for email and "authenticate" via photo_profil lookup.
     */
    @FXML
    public void handleFaceId(ActionEvent event) {
        // Show a dialog to enter the email associated with Face ID
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Connexion Face ID");
        dialog.setHeaderText("Face ID – BioSync");
        dialog.setContentText("Entrez votre adresse email liée à votre visage :");
        dialog.getEditor().setPromptText("votre@email.com");

        dialog.showAndWait().ifPresent(email -> {
            if (email.trim().isEmpty()) {
                updateStatus("Veuillez entrer votre email.", false);
                return;
            }
            AuthService.AuthResult result = authService.loginByFaceId(email.trim());
            updateStatus(result.getMessage(), result.isSuccess());
            if (result.isSuccess()) {
                navigationService.navigateToDashboard((Node) event.getSource(), result.getUser());
            }
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

        // Show Face ID only in login mode
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
