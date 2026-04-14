package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.dao.CertificationDAO;
import org.example.dao.UserDAO;
import org.example.model.User;

import java.io.IOException;
import java.util.List;

public class AdminController {

    @FXML private Label adminNameLabel;
    @FXML private Label adminInitialLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label certCountLabel;
    @FXML private VBox recentUsersContainer;
    @FXML private Label statusLabel;

    private User currentUser;

    public void setUser(User user) {
        this.currentUser = user;
        updateHeader();
        loadStats();
        loadRecentUsers();
    }

    private void updateHeader() {
        if (currentUser != null) {
            adminNameLabel.setText(currentUser.getNomComplet());
            String name = currentUser.getNomComplet();
            String initials = name.length() >= 2
                    ? (name.substring(0, 1) + name.charAt(name.indexOf(' ') > 0 ? name.indexOf(' ') + 1 : 1)).toUpperCase()
                    : name.substring(0, Math.min(2, name.length())).toUpperCase();
            adminInitialLabel.setText(initials);
        }
    }

    private void loadStats() {
        try {
            List<User> users = UserDAO.getAllUsers();
            activeUsersLabel.setText(String.valueOf(users.size()));
            int pending = CertificationDAO.countPending();
            certCountLabel.setText(String.valueOf(pending));
        } catch (Exception e) {
            activeUsersLabel.setText("?");
        }
    }

    private void loadRecentUsers() {
        try {
            List<User> users = UserDAO.getAllUsers();
            recentUsersContainer.getChildren().clear();

            int count = Math.min(users.size(), 5);
            for (int i = 0; i < count; i++) {
                User u = users.get(i);
                HBox row = buildRecentUserRow(u, i % 2 == 0);
                recentUsersContainer.getChildren().add(row);
            }

            if (users.isEmpty()) {
                statusLabel.setText("Aucun utilisateur trouvé");
            } else {
                statusLabel.setText("");
            }
        } catch (Exception e) {
            statusLabel.setText("Erreur: " + e.getMessage());
        }
    }

    private HBox buildRecentUserRow(User user, boolean alternate) {
        HBox row = new HBox();
        row.setStyle("-fx-padding: 14 16 14 16; -fx-border-color: transparent transparent #f8fafc transparent; -fx-border-width: 0 0 1 0;"
                + (alternate ? "-fx-background-color: white;" : "-fx-background-color: #fafbfd;"));

        // Name
        Label nameLbl = new Label(user.getNomComplet());
        nameLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #1a1a2e; -fx-font-weight: bold;");
        nameLbl.setPrefWidth(200);

        // Email
        Label emailLbl = new Label(user.getEmail());
        emailLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #4C6FFF;");
        emailLbl.setPrefWidth(300);

        // Role badge
        String role = extractDisplayRole(user.getRoles());
        Label roleLbl = new Label(role);
        roleLbl.setStyle(getRoleBadgeStyle(role));
        roleLbl.setPrefWidth(120);

        row.getChildren().addAll(nameLbl, emailLbl, roleLbl);
        return row;
    }

    private String extractDisplayRole(String roles) {
        if (roles == null) return "UTILISATEUR";
        if (roles.contains("ADMIN")) return "ADMIN";
        if (roles.contains("COACH")) return "COACH";
        if (roles.contains("SPECIALISTE")) return "SPÉCIALISTE";
        return "UTILISATEUR";
    }

    private String getRoleBadgeStyle(String role) {
        String base = "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 4 12 4 12; -fx-text-fill: white;";
        return switch (role) {
            case "ADMIN" -> base + " -fx-background-color: #ef4444;";
            case "COACH" -> base + " -fx-background-color: #10b981;";
            case "SPÉCIALISTE" -> base + " -fx-background-color: #f59e0b;";
            default -> base + " -fx-background-color: #6b7280;";
        };
    }

    @FXML
    private void handleNavUtilisateurs(MouseEvent event) {
        navigateToUtilisateurs();
    }

    @FXML
    private void handleNavCertifications(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/certifications_admin.fxml"));
            Parent root = loader.load();
            CertificationsAdminController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            stage.setScene(scene);
            stage.setTitle("BioSync - Certifications");
        } catch (IOException e) {
            statusLabel.setText("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGererMembres(MouseEvent event) {
        navigateToUtilisateurs();
    }

    @FXML
    private void handleValidationsPro(MouseEvent event) {
        // Navigate to certifications admin page - same as sidebar Certifications
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/certifications_admin.fxml"));
            Parent root = loader.load();
            CertificationsAdminController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            stage.setScene(scene);
            stage.setTitle("BioSync - Certifications");
        } catch (IOException e) {
            statusLabel.setText("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToUtilisateurs() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/utilisateurs.fxml"));
            Parent root = loader.load();

            UtilisateursController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);

            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            stage.setScene(scene);
            stage.setTitle("BioSync - Utilisateurs");
        } catch (IOException e) {
            statusLabel.setText("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            Scene scene = new Scene(root, 400, 550);
            stage.setScene(scene);
            stage.setTitle("BioSync - Inscription");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
