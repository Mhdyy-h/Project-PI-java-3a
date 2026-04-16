package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.model.User;
import org.example.service.DashboardService;
import org.example.service.NavigationService;
import org.example.service.UserService;

import java.util.List;

public class AdminController {

    @FXML private Label adminNameLabel;
    @FXML private Label adminInitialLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label certCountLabel;
    @FXML private VBox recentUsersContainer;
    @FXML private Label statusLabel;

    private final DashboardService dashboardService = DashboardService.getInstance();
    private final NavigationService navigationService = NavigationService.getInstance();
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
            adminInitialLabel.setText(dashboardService.getUserInitials(currentUser.getNomComplet()));
        }
    }

    private void loadStats() {
        DashboardService.DashboardStats stats = dashboardService.loadStats();
        activeUsersLabel.setText(String.valueOf(stats.getTotalUsers()));
        certCountLabel.setText(String.valueOf(stats.getPendingCertifications()));
    }

    private void loadRecentUsers() {
        DashboardService.DashboardStats stats = dashboardService.loadStats();
        List<User> users = stats.getRecentUsers();
        
        recentUsersContainer.getChildren().clear();
        
        int count = Math.min(users.size(), 5);
        for (int i = 0; i < count; i++) {
            User u = users.get(i);
            HBox row = buildRecentUserRow(u, i % 2 == 0);
            recentUsersContainer.getChildren().add(row);
        }
        
        statusLabel.setText(users.isEmpty() ? "Aucun utilisateur trouvé" : "");
    }

    private HBox buildRecentUserRow(User user, boolean alternate) {
        HBox row = new HBox();
        row.getStyleClass().addAll("recent-user-row", alternate ? "recent-user-row-alt" : "recent-user-row-default");

        // Name
        Label nameLbl = new Label(user.getNomComplet());
        nameLbl.getStyleClass().add("recent-user-name");
        nameLbl.setPrefWidth(200);

        // Email
        Label emailLbl = new Label(user.getEmail());
        emailLbl.getStyleClass().add("recent-user-email");
        emailLbl.setPrefWidth(300);

        // Role badge
        String role = UserService.extractDisplayRole(user.getRoles());
        Label roleLbl = new Label(role);
        roleLbl.getStyleClass().addAll("role-badge-small", "role-badge-" + role.toLowerCase().replace("é", "e"));
        roleLbl.setPrefWidth(120);

        row.getChildren().addAll(nameLbl, emailLbl, roleLbl);
        return row;
    }

    @FXML
    private void handleNavUtilisateurs(MouseEvent event) {
        navigationService.navigateToUtilisateurs(adminNameLabel, currentUser);
    }

    @FXML
    private void handleNavCertifications(MouseEvent event) {
        navigationService.navigateToCertifications(adminNameLabel, currentUser);
    }

    @FXML
    private void handleNavRendezVous(MouseEvent event) {
        navigationService.navigateToRendezVous(adminNameLabel, currentUser);
    }

    @FXML
    private void handleGererMembres(MouseEvent event) {
        navigationService.navigateToUtilisateurs(adminNameLabel, currentUser);
    }

    @FXML
    private void handleValidationsPro(MouseEvent event) {
        navigationService.navigateToCertifications(adminNameLabel, currentUser);
    }

    @FXML
    private void handleLogout(MouseEvent event) {
        navigationService.navigateToLogin(adminNameLabel);
    }
}
