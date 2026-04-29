package org.example.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.example.dao.MembreDAO;
import org.example.dao.UserDAO;
import org.example.model.Groupe;
import org.example.model.User;
import org.example.service.WeatherService;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

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
    }

    private void refreshUI() {
        if (currentGroupe == null) return;

        groupNameLabel.setText(currentGroupe.getNomGroupe());
        themeLabel.setText(currentGroupe.getThematique().toUpperCase());
        descriptionLabel.setText(currentGroupe.getDescription());
        capacityLabel.setText(String.valueOf(currentGroupe.getCapaciteMax()));

        // Image Handling
        try {
            if (currentGroupe.getImage() != null && !currentGroupe.getImage().equals("default.png")) {
                groupImageView.setImage(new Image(new File(currentGroupe.getImage()).toURI().toString()));
            } else {
                groupImageView.setImage(new Image("https://via.placeholder.com/220x150.png?text=BioSync"));
            }
        } catch (Exception e) {
            System.err.println("Error loading group image: " + e.getMessage());
        }

        updateMembershipStatus();
    }

    private void updateMembershipStatus() {
        try {
            int count = MembreDAO.getMemberCount(currentGroupe.getId());
            memberCountLabel.setText(String.valueOf(count));

            boolean isMember = MembreDAO.isMember(currentUser.getId(), currentGroupe.getId());
            if (isMember) {
                actionBtn.setText("Quitter le groupe");
                actionBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-background-radius: 12; -fx-font-weight: bold;");
            } else {
                actionBtn.setText("Rejoindre");
                actionBtn.setStyle("-fx-background-color: #4C6FFF; -fx-text-fill: white; -fx-background-radius: 12; -fx-font-weight: bold;");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleBackToDashboard() { /* Navigate to dashboard logic */ }
}