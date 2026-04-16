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
import java.net.URL;
import java.util.List;

public class AdminController {

    @FXML private Label adminNameLabel;
    @FXML private Label adminInitialLabel;
    @FXML private Label userRoleLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label certCountLabel;
    @FXML private VBox recentUsersContainer;

    @FXML private HBox utilisateursNavItem;
    @FXML private HBox certificationsNavItem;

    private User currentUser;

    public void setUser(User user) {
        this.currentUser = user;
        updateUI();
        loadStats();
        loadRecentUsers();
    }

    private void updateUI() {
        if (currentUser == null) return;
        adminNameLabel.setText(currentUser.getNomComplet());
        String roles = (currentUser.getRoles() != null) ? currentUser.getRoles().toUpperCase() : "";
        boolean isAdmin = roles.contains("ADMIN");
        userRoleLabel.setText(isAdmin ? "Administrateur BioSync" : "Utilisateur BioSync");

        if (utilisateursNavItem != null) {
            utilisateursNavItem.setVisible(isAdmin);
            utilisateursNavItem.setManaged(isAdmin);
        }
        if (certificationsNavItem != null) {
            certificationsNavItem.setVisible(isAdmin);
            certificationsNavItem.setManaged(isAdmin);
        }

        if (currentUser.getNomComplet() != null && !currentUser.getNomComplet().isEmpty()) {
            adminInitialLabel.setText(currentUser.getNomComplet().substring(0, 1).toUpperCase());
        }
    }

    private void loadStats() {
        try {
            activeUsersLabel.setText(String.valueOf(UserDAO.getAllUsers().size()));
            certCountLabel.setText(String.valueOf(CertificationDAO.countPending()));
        } catch (Exception e) {
            if (activeUsersLabel != null) activeUsersLabel.setText("?");
        }
    }

    private void loadRecentUsers() {
        try {
            List<User> users = UserDAO.getAllUsers();
            if (recentUsersContainer == null) return;
            recentUsersContainer.getChildren().clear();
            int count = Math.min(users.size(), 5);
            for (int i = 0; i < count; i++) {
                recentUsersContainer.getChildren().add(buildRecentUserRow(users.get(i), i % 2 == 0));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private HBox buildRecentUserRow(User user, boolean alternate) {
        HBox row = new HBox();
        row.setStyle("-fx-padding: 10 16; -fx-background-color: " + (alternate ? "white" : "#fafbfd") + ";");
        Label name = new Label(user.getNomComplet());
        name.setPrefWidth(200);
        Label email = new Label(user.getEmail());
        email.setPrefWidth(300);
        row.getChildren().addAll(name, email);
        return row;
    }

    @FXML
    private void handleNavCommunity(MouseEvent event) {
        System.out.println("DEBUG: Community button clicked!");
        if (currentUser == null) {
            System.err.println("DEBUG: currentUser is null, cannot navigate!");
            return;
        }

        String roles = (currentUser.getRoles() != null) ? currentUser.getRoles().toUpperCase() : "";
        System.out.println("DEBUG: Current User Roles: " + roles);

        String fxmlPath;
        if (roles.contains("ADMIN")) {
            fxmlPath = "/view/community_home.fxml";
        } else {
            fxmlPath = "/view/user_community.fxml";
        }

        System.out.println("DEBUG: Attempting to navigate to: " + fxmlPath);
        navigateTo(fxmlPath, "Communauté");
    }

    @FXML private void handleNavUtilisateurs(MouseEvent event) { navigateTo("/view/utilisateurs.fxml", "Utilisateurs"); }
    @FXML private void handleNavCertifications(MouseEvent event) { navigateTo("/view/certifications_admin.fxml", "Certifications"); }
    @FXML private void handleGererMembres(MouseEvent event) { navigateTo("/view/utilisateurs.fxml", "Utilisateurs"); }

    private void navigateTo(String fxmlPath, String title) {
        try {
            URL location = getClass().getResource(fxmlPath);
            if (location == null) {
                // TRY FALLBACK (if not in /view/ folder)
                String altPath = fxmlPath.replace("/view", "");
                location = getClass().getResource(altPath);
                if (location == null) {
                    System.err.println("CRITICAL ERROR: Could not find FXML at " + fxmlPath + " or " + altPath);
                    return;
                }
            }

            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();

            Object controller = loader.getController();
            System.out.println("DEBUG: Controller loaded: " + controller.getClass().getSimpleName());

            if (controller instanceof CommunityController) ((CommunityController) controller).setCurrentUser(currentUser);
            if (controller instanceof UserCommunityController) ((UserCommunityController) controller).setCurrentUser(currentUser);

            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("BioSync - " + title);
            System.out.println("DEBUG: Navigation successful!");

        } catch (IOException e) {
            System.err.println("DEBUG: Failed to load FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(MouseEvent event) {
        try {
            URL loc = getClass().getResource("/view/login.fxml");
            if (loc == null) loc = getClass().getResource("/login.fxml");
            Parent root = FXMLLoader.load(loc);
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 400, 600));
            stage.centerOnScreen();
        } catch (IOException e) { e.printStackTrace(); }
    }
}