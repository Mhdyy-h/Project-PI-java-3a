package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.example.dao.UserDAO;
import org.example.model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EditUtilisateurController {

    @FXML private Label pageTitleLabel;
    @FXML private Label pageSubtitleLabel;
    @FXML private Label avatarLabel;
    @FXML private Circle avatarCircle;
    @FXML private Label userNameDisplay;
    @FXML private Label userIdDisplay;
    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox roleUtilisateur;
    @FXML private CheckBox roleCoach;
    @FXML private CheckBox roleSpecialiste;
    @FXML private CheckBox roleAdmin;
    @FXML private Label statusLabel;

    private User currentUser;   // logged-in admin
    private User targetUser;    // user being edited

    public void setData(User currentUser, User targetUser) {
        this.currentUser = currentUser;
        this.targetUser = targetUser;
        populateForm();
    }

    private void populateForm() {
        if (targetUser == null) return;

        pageSubtitleLabel.setText("Modifier les informations de " + targetUser.getNomComplet());
        userNameDisplay.setText(targetUser.getNomComplet());
        userIdDisplay.setText("ID: #" + targetUser.getId());
        nomField.setText(targetUser.getNomComplet());
        emailField.setText(targetUser.getEmail());

        // Avatar
        String initials = getInitials(targetUser.getNomComplet());
        avatarLabel.setText(initials);
        avatarCircle.setFill(Color.web(getAvatarColor(targetUser.getRoles())));

        // Roles
        String roles = targetUser.getRoles() != null ? targetUser.getRoles() : "";
        roleUtilisateur.setSelected(roles.contains("ROLE_USER") || roles.contains("UTILISATEUR") || (!roles.contains("COACH") && !roles.contains("ADMIN") && !roles.contains("SPECIALISTE")));
        roleCoach.setSelected(roles.contains("COACH"));
        roleSpecialiste.setSelected(roles.contains("SPECIALISTE"));
        roleAdmin.setSelected(roles.contains("ADMIN"));
    }

    @FXML
    private void handleSave() {
        String nom = nomField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (nom.isEmpty() || email.isEmpty()) {
            statusLabel.setText("Le nom et l'email sont obligatoires.");
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

        targetUser.setNomComplet(nom);
        targetUser.setEmail(email);
        targetUser.setRoles(roleStr);
        if (!password.isEmpty()) {
            targetUser.setMotDePasse(password);
        }

        boolean ok = UserDAO.updateUser(targetUser, password.isEmpty());
        if (ok) {
            statusLabel.setText("✓ Modifications enregistrées avec succès!");
            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #10b981;");
            // Navigate back after short delay
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.2));
            pause.setOnFinished(e -> navigateBackToUsers());
            pause.play();
        } else {
            statusLabel.setText("Erreur lors de la sauvegarde.");
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

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "??";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2)
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    private String getAvatarColor(String roles) {
        if (roles == null) return "#6b7280";
        if (roles.contains("ADMIN")) return "#ef4444";
        if (roles.contains("COACH")) return "#10b981";
        if (roles.contains("SPECIALISTE")) return "#f59e0b";
        return "#7c3aed";
    }
}
