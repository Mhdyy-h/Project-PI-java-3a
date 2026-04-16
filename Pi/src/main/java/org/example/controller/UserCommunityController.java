package org.example.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import org.example.dao.GroupeDAO;
import org.example.dao.MembreDAO;
import org.example.dao.UserDAO;
import org.example.model.Groupe;
import org.example.model.User;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class UserCommunityController {

    @FXML private VBox groupsGrid;
    @FXML private Label userNameLabel;
    @FXML private Label userInitialLabel;
    @FXML private Label userScoreLabel;
    @FXML private Label roleLabel;
    @FXML private TextField searchField;

    private User currentUser;

    public void setCurrentUser(User user) {
        if (user == null) return;
        this.currentUser = user;

        Platform.runLater(() -> {
            updateUI();
            refreshGroups();
        });
    }

    private void updateUI() {
        if (currentUser != null) {
            userNameLabel.setText(currentUser.getNomComplet());
            userScoreLabel.setText(String.valueOf(currentUser.getScoreGlobal()));
            roleLabel.setText("Utilisateur BioSync");
            if (currentUser.getNomComplet() != null && !currentUser.getNomComplet().isEmpty()) {
                userInitialLabel.setText(currentUser.getNomComplet().substring(0, 1).toUpperCase());
            }
        }
    }

    public void refreshGroups() {
        if (groupsGrid == null) return;
        groupsGrid.getChildren().clear();

        try {
            List<Groupe> groups = GroupeDAO.getAllGroups();
            String query = searchField.getText().toLowerCase().trim();

            List<Groupe> filteredGroups = groups.stream()
                    .filter(g -> g.getNomGroupe().toLowerCase().contains(query) ||
                            g.getThematique().toLowerCase().contains(query))
                    .collect(Collectors.toList());

            if (filteredGroups.isEmpty()) {
                Label info = new Label(query.isEmpty() ? "Aucun groupe disponible." : "Aucun résultat pour '" + query + "'");
                info.setStyle("-fx-text-fill: #9ca3af; -fx-font-style: italic; -fx-padding: 20;");
                groupsGrid.getChildren().add(info);
            } else {
                for (Groupe g : filteredGroups) {
                    groupsGrid.getChildren().add(buildCard(g));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox buildCard(Groupe g) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4); -fx-cursor: hand;");

        // Navigate to Details when the card (the background) is clicked
        card.setOnMouseClicked(event -> showGroupDetails(g));

        VBox info = new VBox(5);
        Label name = new Label(g.getNomGroupe());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1a1a2e;");

        Label theme = new Label("🏷 " + g.getThematique());
        theme.setStyle("-fx-text-fill: #4C6FFF; -fx-font-size: 12px; -fx-font-weight: bold;");

        int count = 0;
        boolean isMember = false;
        try {
            count = MembreDAO.getMemberCount(g.getId());
            if (currentUser != null) {
                isMember = MembreDAO.isMember(currentUser.getId(), g.getId());
            }
        } catch (SQLException e) { e.printStackTrace(); }

        Label stats = new Label("👥 " + count + " / " + g.getCapaciteMax() + " membres");
        stats.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");

        info.getChildren().addAll(name, theme, stats);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Join/Leave Button
        Button btn = new Button(isMember ? "Quitter" : "Rejoindre");
        if (isMember) {
            btn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 8 18;");
        } else {
            btn.setStyle("-fx-background-color: #4C6FFF; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 8 18;");
        }

        btn.setOnAction(e -> {
            // STOP the click from triggering the Card's "showGroupDetails"
            e.consume();
            try {
                if (MembreDAO.isMember(currentUser.getId(), g.getId())) {
                    MembreDAO.leave(currentUser.getId(), g.getId());
                } else {
                    MembreDAO.join(currentUser.getId(), g.getId());
                    UserDAO.updateScore(currentUser.getId(), 50);
                    currentUser.setScoreGlobal(currentUser.getScoreGlobal() + 50);
                }
                refreshGroups();
                updateUI();
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        // Ensure the button itself doesn't trigger the card click when clicked
        btn.setOnMouseClicked(MouseEvent::consume);

        card.getChildren().addAll(info, spacer, btn);
        return card;
    }

    private void showGroupDetails(Groupe g) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/group_details_user.fxml"));
            Parent root = loader.load();

            GroupDetailsController ctrl = loader.getController();
            ctrl.setData(g, currentUser);

            groupsGrid.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleSearch() { refreshGroups(); }

    @FXML private void handleLogout(MouseEvent event) {
        try {
            URL loc = getClass().getResource("/view/login.fxml");
            Parent root = FXMLLoader.load(loc);
            userNameLabel.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void handleBackToDashboard(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dashboard.fxml"));
            Parent root = loader.load();
            AdminController ctrl = loader.getController();
            ctrl.setUser(currentUser);
            userNameLabel.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }
}