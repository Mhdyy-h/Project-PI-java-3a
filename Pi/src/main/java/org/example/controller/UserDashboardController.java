package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import org.example.model.User;
import org.example.service.NavigationService;

public class UserDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label userNameLabel;

    private User currentUser;
    private final NavigationService navigationService = NavigationService.getInstance();

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            welcomeLabel.setText("Bienvenue, " + user.getNomComplet() + " !");
            userNameLabel.setText(user.getNomComplet());
        }
    }

    @FXML
    private void handleNavSports(MouseEvent event) {
        navigationService.navigateToSports(welcomeLabel, currentUser);
    }

    @FXML
    private void handleNavMental(MouseEvent event) {
        navigationService.navigateToMental(welcomeLabel, currentUser);
    }

    @FXML
    private void handleNavCommunity(MouseEvent event) {
        navigationService.navigateToCommunity(welcomeLabel, currentUser);
    }

    @FXML
    private void handleNavNutrition(MouseEvent event) {
        navigationService.navigateToNutrition(welcomeLabel, currentUser);
    }

    @FXML
    private void handleLogout(MouseEvent event) {
        navigationService.navigateToLogin(welcomeLabel);
    }
}
