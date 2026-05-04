package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.dao.UserDAO;
import org.example.model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NouvelUtilisateurController {

    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox roleUtilisateur;
    @FXML private CheckBox roleCoach;
    @FXML private CheckBox roleSpecialiste;
    @FXML private CheckBox roleAdmin;
    @FXML private Label statusLabel;

    private User currentUser; // logged-in admin

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    private void handleCreate() {
        String nom = nomField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (nom.isEmpty() || email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Tous les champs sont obligatoires.");
            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ef4444;");
            return;
        }
        if (password.length() < 8) {
            statusLabel.setText("Le mot de passe doit contenir au moins 8 caractères.");
            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ef4444;");
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            statusLabel.setText("Adresse email invalide.");
            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ef4444;");
            return;
        }

        // Build role string
        List<String> roles = new ArrayList<>();
        if (roleAdmin.isSelected()) roles.add("ROLE_ADMIN");
        else if (roleCoach.isSelected()) roles.add("ROLE_COACH");
        else if (roleSpecialiste.isSelected()) roles.add("ROLE_SPECIALISTE");
        else roles.add("ROLE_USER");

        String roleStr = "[\"" + String.join("\",\"", roles) + "\"]";

        User newUser = new User(0, nom, email, password);
        newUser.setRoles(roleStr);

        boolean ok = UserDAO.insertUser(newUser);
        if (ok) {
            statusLabel.setText("✓ Utilisateur créé avec succès!");
            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #10b981;");
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.2));
            pause.setOnFinished(e -> navigateBackToUsers());
            pause.play();
        } else {
            statusLabel.setText("Erreur lors de la création (email déjà utilisé?).");
            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ef4444;");
        }
    }

    @FXML
    private void handleRetour() {
        navigateBackToUsers();
    }

    private void navigateBackToUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/utilisateurs.fxml"));
            Parent root = loader.load();
            UtilisateursController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            Stage stage = (Stage) nomField.getScene().getWindow();
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            stage.setScene(scene);
            stage.setTitle("BioSync - Utilisateurs");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
