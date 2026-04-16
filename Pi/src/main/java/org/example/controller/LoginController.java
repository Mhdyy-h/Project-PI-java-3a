package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.dao.UserDAO;
import org.example.model.User;

import java.io.IOException;
import java.net.URL;

public class LoginController {

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private CheckBox termsCheckBox;
    @FXML private Button mainButton;
    @FXML private Label statusLabel;
    @FXML private Label titleLabel;

    @FXML private VBox fullNameBox;
    @FXML private VBox confirmPasswordBox;
    @FXML private VBox termsBox;

    private boolean isLoginMode = true;

    @FXML
    public void initialize() {
        handleLoginLink();
    }

    @FXML
    public void handleMainAction(ActionEvent event) {
        if (isLoginMode) processLogin(event);
        else processRegistration(event);
    }

    private void processLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String pass  = passwordField.getText();

        if (email.isEmpty() || pass.isEmpty()) {
            showStatus("Veuillez remplir tous les champs.", "#ef4444");
            return;
        }

        User user = UserDAO.login(email, pass);
        if (user != null) {
            navigateToDashboard(event, user);
        } else {
            showStatus("Email ou mot de passe incorrect.", "#ef4444");
        }
    }

    private void processRegistration(ActionEvent event) {
        String nom = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String pass = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (nom.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            showStatus("Tous les champs sont obligatoires.", "#ef4444");
            return;
        }
        if (!pass.equals(confirm)) {
            showStatus("Les mots de passe ne correspondent pas.", "#ef4444");
            return;
        }
        if (!termsCheckBox.isSelected()) {
            showStatus("Veuillez accepter les conditions.", "#ef4444");
            return;
        }

        User newUser = new User(0, nom, email, pass, "UTILISATEUR");
        if (UserDAO.insertUser(newUser)) {
            navigateToDashboard(event, newUser);
        } else {
            showStatus("Erreur : Email déjà utilisé.", "#ef4444");
        }
    }

    private void navigateToDashboard(ActionEvent event, User user) {
        try {
            // SAFE PATH CHECK: This tries to find the file in /view/ or in root
            String path = "/view/dashboard.fxml";
            URL fxmlLocation = getClass().getResource(path);

            if (fxmlLocation == null) {
                // Try without the /view/ folder if the first one fails
                path = "/dashboard.fxml";
                fxmlLocation = getClass().getResource(path);
            }

            if (fxmlLocation == null) {
                showStatus("Erreur : Fichier dashboard.fxml introuvable.", "#ef4444");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            AdminController controller = loader.getController();
            controller.setUser(user);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 700));
            stage.centerOnScreen();
            stage.setTitle("BioSync - Dashboard");

        } catch (IOException e) {
            e.printStackTrace();
            showStatus("Erreur de chargement : " + e.getMessage(), "#ef4444");
        }
    }

    @FXML
    public void handleLoginLink() {
        isLoginMode = true;
        titleLabel.setText("Connexion");
        mainButton.setText("Se connecter");
        toggleFields(false);
    }

    @FXML
    public void handleRegisterLink() {
        isLoginMode = false;
        titleLabel.setText("Inscription");
        mainButton.setText("Créer mon compte");
        toggleFields(true);
    }

    private void toggleFields(boolean isRegister) {
        fullNameBox.setVisible(isRegister);
        fullNameBox.setManaged(isRegister);
        confirmPasswordBox.setVisible(isRegister);
        confirmPasswordBox.setManaged(isRegister);
        termsBox.setVisible(isRegister);
        termsBox.setManaged(isRegister);
    }

    private void showStatus(String message, String color) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: " + color + ";");
    }

    @FXML
    public void handleHealthProLink(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/certification_request.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            showStatus("Erreur : Page certification introuvable.", "#ef4444");
        }
    }
}