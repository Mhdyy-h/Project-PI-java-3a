package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import org.example.model.User;
import org.example.service.AvatarService;
import org.example.service.DashboardService;
import org.example.service.NavigationService;
import org.example.service.RateLimiterService;
import org.example.service.UserService;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.format.DateTimeFormatter;

import java.io.File;
import java.util.List;

public class AdminController {

    @FXML private Label adminNameLabel;
    @FXML private Label adminInitialLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label certCountLabel;
    @FXML private VBox recentUsersContainer;
    @FXML private Label statusLabel;
    @FXML private StackPane adminAvatarPane;
    @FXML private Circle adminAvatarCircle;
    @FXML private ImageView adminAvatarImage;

    private final DashboardService dashboardService = DashboardService.getInstance();
    private final NavigationService navigationService = NavigationService.getInstance();
    private final RateLimiterService rateLimiter = RateLimiterService.getInstance();
    private User currentUser;
    private static boolean photoUpdatedFlag = false;

    public static void setPhotoUpdatedFlag(boolean flag) {
        photoUpdatedFlag = flag;
    }

    public void setUser(User user) {
        this.currentUser = user;

        // If photo was updated elsewhere, reload user data from DB
        if (photoUpdatedFlag && user != null) {
            try {
                org.example.model.User refreshed = org.example.dao.UserDAO.getUserById(user.getId());
                if (refreshed != null) {
                    this.currentUser = refreshed;
                }
            } catch (Exception e) {
                System.err.println("[Admin] Error refreshing user: " + e.getMessage());
            }
            photoUpdatedFlag = false;
        }

        updateHeader();
        loadStats();
        loadRecentUsers();
    }

    private void updateHeader() {
        if (currentUser != null) {
            adminNameLabel.setText(currentUser.getNomComplet());
            adminInitialLabel.setText(dashboardService.getUserInitials(currentUser.getNomComplet()));

            // Display user's photo if available, otherwise generate avatar
            if (adminAvatarImage != null) {
                Image avatarImage = null;

                // Try user photo first
                if (currentUser.getPhotoProfil() != null && !currentUser.getPhotoProfil().isEmpty()) {
                    try {
                        java.io.File photoFile = new java.io.File(currentUser.getPhotoProfil());
                        if (photoFile.exists()) {
                            avatarImage = new Image(photoFile.toURI().toString());
                        }
                    } catch (Exception e) {
                        System.err.println("[Admin] Error loading photo: " + e.getMessage());
                    }
                }

                // Fall back to generated avatar if no photo
                if (avatarImage == null && currentUser.getEmail() != null) {
                    AvatarService avatarService = AvatarService.getInstance();
                    avatarImage = avatarService.generateAvatar(currentUser.getEmail());
                }

                if (avatarImage != null) {
                    adminAvatarImage.setImage(avatarImage);
                    adminAvatarImage.setVisible(true);
                    if (adminInitialLabel != null) adminInitialLabel.setVisible(false);
                    if (adminAvatarCircle != null) adminAvatarCircle.setVisible(false);
                }
            }
        }
    }

    /**
     * Refresh the header - called when user data changes (e.g., photo upload)
     */
    public void refreshHeader() {
        updateHeader();
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

        Label nameLbl = new Label(user.getNomComplet());
        nameLbl.getStyleClass().add("recent-user-name");
        nameLbl.setPrefWidth(200);

        Label emailLbl = new Label(user.getEmail());
        emailLbl.getStyleClass().add("recent-user-email");
        emailLbl.setPrefWidth(300);

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
    private void handleGererMembres(MouseEvent event) {
        navigationService.navigateToUtilisateurs(adminNameLabel, currentUser);
    }

    @FXML
    private void handleValidationsPro(MouseEvent event) {
        navigationService.navigateToCertifications(adminNameLabel, currentUser);
    }

    @FXML
    private void handleHistoriqueLogs(MouseEvent event) {
        navigationService.navigateToLogs(adminNameLabel, currentUser);
    }

    @FXML
    private void handleLogout(MouseEvent event) {
        navigationService.navigateToLogin(adminNameLabel);
    }

    @FXML
    private void handleRateLimiter(MouseEvent event) {
        showRateLimiterDialog();
    }

    /**
     * Affiche une fenêtre de gestion des utilisateurs bloqués par rate limiting.
     */
    private void showRateLimiterDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Gestion du Rate Limiting");
        dialog.setHeaderText("Utilisateurs bloqués (trop de tentatives de connexion)");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

        // TableView pour afficher les utilisateurs bloqués
        TableView<RateLimiterService.BlockedUserInfo> table = new TableView<>();
        table.setPrefSize(600, 300);

        TableColumn<RateLimiterService.BlockedUserInfo, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().email));
        emailCol.setPrefWidth(250);

        TableColumn<RateLimiterService.BlockedUserInfo, Integer> attemptsCol = new TableColumn<>("Tentatives");
        attemptsCol.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().attemptCount));
        attemptsCol.setPrefWidth(100);

        TableColumn<RateLimiterService.BlockedUserInfo, String> remainingCol = new TableColumn<>("Temps restant");
        remainingCol.setCellValueFactory(cellData -> {
            long minutes = cellData.getValue().remainingMinutes;
            return new javafx.beans.property.SimpleStringProperty(minutes + " min");
        });
        remainingCol.setPrefWidth(100);

        TableColumn<RateLimiterService.BlockedUserInfo, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(120);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button unblockBtn = new Button("Débloquer");
            {
                unblockBtn.setOnAction(e -> {
                    RateLimiterService.BlockedUserInfo user = getTableView().getItems().get(getIndex());
                    if (rateLimiter.unblockUser(user.email)) {
                        getTableView().getItems().remove(user);
                        showInfo("Utilisateur débloqué", user.email + " a été débloqué avec succès.");
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : unblockBtn);
            }
        });

        table.getColumns().addAll(emailCol, attemptsCol, remainingCol, actionCol);

        // Charger les utilisateurs bloqués
        ObservableList<RateLimiterService.BlockedUserInfo> blockedUsers = FXCollections.observableArrayList(
            rateLimiter.getBlockedUsers().values()
        );
        table.setItems(blockedUsers);

        if (blockedUsers.isEmpty()) {
            Label noDataLabel = new Label("Aucun utilisateur bloqué actuellement.");
            noDataLabel.setStyle("-fx-padding: 20; -fx-font-size: 14;");
            dialog.getDialogPane().setContent(noDataLabel);
        } else {
            dialog.getDialogPane().setContent(table);
        }

        // Bouton de rafraîchissement
        ButtonType refreshType = new ButtonType("Rafraîchir", ButtonBar.ButtonData.LEFT);
        dialog.getDialogPane().getButtonTypes().add(0, refreshType);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == refreshType) {
                // Rafraîchir la liste
                ObservableList<RateLimiterService.BlockedUserInfo> refreshed = FXCollections.observableArrayList(
                    rateLimiter.getBlockedUsers().values()
                );
                table.setItems(refreshed);
            }
            return null;
        });

        dialog.showAndWait();
    }

    @FXML
    private void handleGenerateAvatar() {
        if (currentUser != null && currentUser.getEmail() != null) {
            AvatarService avatarService = AvatarService.getInstance();
            // Generate avatar with random style
            Image newAvatar = avatarService.regenerateWithRandomStyle(currentUser.getEmail());

            if (newAvatar != null && !newAvatar.isError()) {
                adminAvatarImage.setImage(newAvatar);
                adminAvatarImage.setVisible(true);
                if (adminInitialLabel != null) adminInitialLabel.setVisible(false);
                if (adminAvatarCircle != null) adminAvatarCircle.setVisible(false);

                showInfo("Avatar généré", "Votre nouvel avatar cartoon a été généré avec succès !");
            } else {
                showInfo("Erreur", "Impossible de générer l'avatar. Vérifiez votre connexion internet.");
            }
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
