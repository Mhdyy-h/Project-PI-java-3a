package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.example.dao.EvenementDAO;
import org.example.dao.MembreDAO;
import org.example.dao.UserDAO;
import org.example.model.Evenement;
import org.example.model.Groupe;
import org.example.model.User;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class GroupDetailsController {

    @FXML private Label groupNameLabel, themeLabel, descriptionLabel, memberCountLabel, capacityLabel;
    @FXML private VBox eventsContainer;
    @FXML private Button actionBtn;

    private User currentUser;
    private Groupe currentGroup;

    public void setData(Groupe g, User u) {
        this.currentGroup = g;
        this.currentUser = u;
        refreshUI();
    }

    private void refreshUI() {
        if (currentGroup == null || currentUser == null) return;

        groupNameLabel.setText(currentGroup.getNomGroupe());
        themeLabel.setText("🏷 " + currentGroup.getThematique());
        descriptionLabel.setText(currentGroup.getDescription());
        capacityLabel.setText(String.valueOf(currentGroup.getCapaciteMax()));

        try {
            // Update Membership Status
            int members = MembreDAO.getMemberCount(currentGroup.getId());
            memberCountLabel.setText(String.valueOf(members));

            boolean joined = MembreDAO.isMember(currentUser.getId(), currentGroup.getId());
            updateActionButton(joined);

            // Load Events for this specific group
            loadEvents();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateActionButton(boolean joined) {
        if (joined) {
            actionBtn.setText("Quitter le Groupe");
            actionBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-background-radius: 12; -fx-font-weight: bold;");
        } else {
            actionBtn.setText("Rejoindre le Groupe");
            actionBtn.setStyle("-fx-background-color: #4C6FFF; -fx-text-fill: white; -fx-background-radius: 12; -fx-font-weight: bold;");
        }
    }

    private void loadEvents() throws SQLException {
        eventsContainer.getChildren().clear();

        // Corrected: Fetch events specific to this group only
        List<Evenement> groupEvents = EvenementDAO.getEventsByGroupId(currentGroup.getId());

        if (groupEvents.isEmpty()) {
            Label noEvent = new Label("Aucun événement prévu pour ce groupe.");
            noEvent.setStyle("-fx-text-fill: #9ca3af; -fx-font-style: italic; -fx-padding: 10;");
            eventsContainer.getChildren().add(noEvent);
        } else {
            for (Evenement ev : groupEvents) {
                VBox card = new VBox(8);
                card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 8, 0, 0, 2);");

                Label title = new Label("📅 " + ev.getTitreEvent());
                title.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1e293b;");

                Label date = new Label("🕒 " + ev.getDateEvent().toString());
                date.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

                Label loc = new Label("📍 " + ev.getLocationName() + " (" + ev.getAddress() + ")");
                loc.setStyle("-fx-font-size: 12px; -fx-text-fill: #4C6FFF; -fx-font-weight: bold;");

                card.getChildren().addAll(title, date, loc);
                eventsContainer.getChildren().add(card);
            }
        }
    }

    @FXML
    private void handleJoinLeave() {
        try {
            if (MembreDAO.isMember(currentUser.getId(), currentGroup.getId())) {
                MembreDAO.leave(currentUser.getId(), currentGroup.getId());
                // Logic for leaving (optional: deduct points?)
            } else {
                MembreDAO.join(currentUser.getId(), currentGroup.getId());
                UserDAO.updateScore(currentUser.getId(), 50);
                currentUser.setScoreGlobal(currentUser.getScoreGlobal() + 50);

                // Show a small confirmation
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Félicitations ! Vous avez rejoint le groupe et gagné 50 BioPoints.");
                alert.setHeaderText(null);
                alert.show();
            }
            refreshUI();
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de l'opération : " + e.getMessage());
            alert.show();
        }
    }

    @FXML private void handleBackToExplorer() {
        navigateTo("/view/user_community.fxml");
    }

    @FXML private void handleBackToDashboard() {
        navigateTo("/view/dashboard.fxml");
    }

    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object ctrl = loader.getController();
            if (ctrl instanceof UserCommunityController) ((UserCommunityController) ctrl).setCurrentUser(currentUser);
            if (ctrl instanceof AdminController) ((AdminController) ctrl).setUser(currentUser);

            groupNameLabel.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}