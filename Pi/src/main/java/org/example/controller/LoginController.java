package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.model.User;
import org.example.service.AuthService;
import org.example.util.UserSession;

/**
 * LoginController - Handles user authentication (login and registration)
 * Extends BaseController for standardized navigation
 */
public class LoginController extends BaseController {

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

    private final AuthService authService = AuthService.getInstance();
    private boolean isLoginMode = false;

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
            User user = result.getUser();
            UserSession.getInstance().startSession(user);
            log("User registered: " + user.getEmail());
            navigateToDashboard();
            closeCurrentWindow(event);
        }
    }

    private void handleLogin(ActionEvent event) {
        AuthService.AuthResult result = authService.login(
            emailField.getText().trim(),
            passwordField.getText()
        );

        updateStatus(result.getMessage(), result.isSuccess());

        if (result.isSuccess()) {
            User user = result.getUser();
            UserSession.getInstance().startSession(user);
            log("User registered and logged in: " + user.getEmail());
            navigateToDashboard();
            closeCurrentWindow(event);
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
        showInfoAlert("Certification Pro", 
            "Veuillez vous connecter ou créer un compte pour demander une certification professionnelle.");
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Closes the current window after navigation
     */
    private void closeCurrentWindow(ActionEvent event) {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
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
    }

    private void updateStatus(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().clear();
        statusLabel.getStyleClass().add(success ? "status-success" : "status-error");
    }
}
