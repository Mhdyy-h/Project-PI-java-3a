package org.example.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.example.dao.EvenementDAO;
import org.example.dao.MembreDAO;
import org.example.dao.UserDAO;
import org.example.model.Evenement;
import org.example.model.Groupe;
import org.example.model.User;
import org.example.service.WeatherService;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class GroupDetailsController {

    @FXML private Label groupNameLabel, themeLabel, descriptionLabel, memberCountLabel, capacityLabel;
    @FXML private Label tempLabel, cityLabel, weatherIcon;
    @FXML private Button actionBtn;
    @FXML private ImageView groupImageView;
    @FXML private VBox eventsContainer;

    private Groupe currentGroupe;
    private User currentUser;

    public void setData(Groupe g, User u) {
        this.currentGroupe = g;
        this.currentUser = u;

        refreshUI();
        loadWeather();
        loadEvents(); // CRITICAL: Added this call
    }

    private void refreshUI() {
        if (currentGroupe == null) return;

        groupNameLabel.setText(currentGroupe.getNomGroupe());
        themeLabel.setText(currentGroupe.getThematique().toUpperCase());
        descriptionLabel.setText(currentGroupe.getDescription());
        capacityLabel.setText(String.valueOf(currentGroupe.getCapaciteMax()));

        try {
            if (currentGroupe.getImage() != null && !currentGroupe.getImage().equals("default.png")) {
                groupImageView.setImage(new Image(new File(currentGroupe.getImage()).toURI().toString()));
            } else {
                groupImageView.setImage(new Image("https://via.placeholder.com/220x150.png?text=BioSync"));
            }
        } catch (Exception e) {
            groupImageView.setImage(new Image("https://via.placeholder.com/220x150.png?text=Error"));
        }

        updateMembershipStatus();
    }

    private void loadEvents() {
        eventsContainer.getChildren().clear();
        try {
            List<Evenement> events = EvenementDAO.getEventsByGroupId(currentGroupe.getId());
            if (events.isEmpty()) {
                Label noEvents = new Label("Aucun événement prévu pour ce groupe.");
                noEvents.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic;");
                eventsContainer.getChildren().add(noEvents);
            } else {
                for (Evenement e : events) {
                    eventsContainer.getChildren().add(buildEventCard(e));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private HBox buildEventCard(Evenement e) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 8, 0, 0, 2);");

        VBox dateBox = new VBox(2);
        dateBox.setAlignment(Pos.CENTER);
        dateBox.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 10; -fx-background-radius: 10; -fx-min-width: 60;");

        String dateStr = e.getDateEvent().toLocalDateTime().getDayOfMonth() + " " +
                e.getDateEvent().toLocalDateTime().getMonth().name().substring(0,3);
        Label dateLbl = new Label(dateStr);
        dateLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #4C6FFF;");
        dateBox.getChildren().add(dateLbl);

        VBox info = new VBox(3);
        Label title = new Label(e.getTitreEvent());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label loc = new Label("📍 " + e.getLocationName());
        loc.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
        info.getChildren().addAll(title, loc);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label points = new Label("+" + e.getPointsParticipation() + " pts");
        points.setStyle("-fx-background-color: #ecfdf5; -fx-text-fill: #10b981; -fx-padding: 5 10; -fx-background-radius: 20; -fx-font-weight: bold; -fx-font-size: 11px;");

        card.getChildren().addAll(dateBox, info, spacer, points);
        return card;
    }

    private void updateMembershipStatus() {
        try {
            int count = MembreDAO.getMemberCount(currentGroupe.getId());
            memberCountLabel.setText(String.valueOf(count));
            boolean isMember = MembreDAO.isMember(currentUser.getId(), currentGroupe.getId());
            actionBtn.setText(isMember ? "Quitter le groupe" : "Rejoindre");
            actionBtn.setStyle(isMember ?
                    "-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-background-radius: 12; -fx-font-weight: bold;" :
                    "-fx-background-color: #4C6FFF; -fx-text-fill: white; -fx-background-radius: 12; -fx-font-weight: bold;");
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    @FXML
    private void handleJoinLeave() {
        try {
            if (MembreDAO.isMember(currentUser.getId(), currentGroupe.getId())) {
                MembreDAO.leave(currentUser.getId(), currentGroupe.getId());
            } else {
                MembreDAO.join(currentUser.getId(), currentGroupe.getId());
                UserDAO.updateScore(currentUser.getId(), 50);
                currentUser.setScoreGlobal(currentUser.getScoreGlobal() + 50);
            }
            updateMembershipStatus();
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void loadWeather() {
        new Thread(() -> {
            JSONObject weather = WeatherService.getWeather();
            if (weather != null) {
                Platform.runLater(() -> {
                    tempLabel.setText(Math.round(weather.getJSONObject("main").getDouble("temp")) + "°C");
                    cityLabel.setText(weather.getString("name"));
                });
            }
        }).start();
    }

    @FXML
    private void handleBackToExplorer() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/user_community.fxml"));
            Parent root = loader.load();
            UserCommunityController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            groupNameLabel.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void handleBackToDashboard() {}
}