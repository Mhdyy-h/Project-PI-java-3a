package org.example.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import org.example.dao.GroupeDAO;
import org.example.dao.MembreDAO;
import org.example.dao.UserDAO;
import org.example.model.Groupe;
import org.example.model.User;
import org.example.service.WeatherService;
import org.example.service.QuoteService;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class UserCommunityController {

    @FXML private VBox groupsGrid;
    @FXML private Label userNameLabel, userInitialLabel, userScoreLabel, roleLabel;
    @FXML private Label tempLabel, cityLabel, weatherIcon;
    @FXML private Label quoteLabel, authorLabel;
    @FXML private TextField searchField;

    private User currentUser;

    public void setCurrentUser(User user) {
        if (user == null) return;
        this.currentUser = user;
        Platform.runLater(() -> {
            updateUI();
            refreshGroups();
            loadWeather();
            loadQuote();
        });
    }

    private void loadQuote() {
        new Thread(() -> {
            JSONObject quoteObj = QuoteService.getQuote();
            if (quoteObj != null) {
                Platform.runLater(() -> {
                    quoteLabel.setText("\"" + quoteObj.getString("q") + "\"");
                    authorLabel.setText("- " + quoteObj.getString("a"));
                });
            }
        }).start();
    }

    private void loadWeather() {
        new Thread(() -> {
            JSONObject weather = WeatherService.getWeather();
            if (weather != null) {
                Platform.runLater(() -> {
                    tempLabel.setText(Math.round(weather.getJSONObject("main").getDouble("temp")) + "°C");
                    cityLabel.setText(weather.getString("name") + ", " + weather.getJSONArray("weather").getJSONObject(0).getString("main"));
                });
            }
        }).start();
    }

    private void updateUI() {
        if (currentUser != null) {
            userNameLabel.setText(currentUser.getNomComplet());
            userScoreLabel.setText(String.valueOf(currentUser.getScoreGlobal()));
            userInitialLabel.setText(currentUser.getNomComplet().substring(0, 1).toUpperCase());
        }
    }

    public void refreshGroups() {
        groupsGrid.getChildren().clear();
        try {
            List<Groupe> groups = GroupeDAO.getAllGroups();
            String query = searchField.getText().toLowerCase().trim();
            for (Groupe g : groups) {
                if (g.getNomGroupe().toLowerCase().contains(query) || g.getThematique().toLowerCase().contains(query)) {
                    groupsGrid.getChildren().add(buildCard(g));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private HBox buildCard(Groupe g) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 18; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4); -fx-cursor: hand;");
        card.setOnMouseClicked(event -> showGroupDetails(g));

        // Group Image with Rounded Corners
        ImageView imgView = new ImageView();
        imgView.setFitWidth(100);
        imgView.setFitHeight(80);

        try {
            if (g.getImage() != null && !g.getImage().equals("default.png")) {
                imgView.setImage(new Image(new File(g.getImage()).toURI().toString()));
            } else {
                imgView.setImage(new Image("https://via.placeholder.com/100x80.png?text=BioSync"));
            }
        } catch (Exception e) {
            imgView.setImage(new Image("https://via.placeholder.com/100x80.png?text=Error"));
        }

        // Clip the image for rounded corners
        Rectangle clip = new Rectangle(100, 80);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        imgView.setClip(clip);

        VBox info = new VBox(5);
        Label name = new Label(g.getNomGroupe());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1a1a2e;");
        Label theme = new Label("🏷 " + g.getThematique());
        theme.setStyle("-fx-text-fill: #4C6FFF; -fx-font-size: 12px; -fx-font-weight: bold;");

        int count = 0;
        try { count = MembreDAO.getMemberCount(g.getId()); } catch (SQLException e) {}
        Label stats = new Label("👥 " + count + " / " + g.getCapaciteMax() + " membres");
        stats.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

        info.getChildren().addAll(name, theme, stats);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btn = new Button("Voir");
        btn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #4C6FFF; -fx-background-radius: 10; -fx-font-weight: bold;");

        card.getChildren().addAll(imgView, info, spacer, btn);
        return card;
    }

    private void showGroupDetails(Groupe g) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/group_details_user.fxml"));
            Parent root = loader.load();
            GroupDetailsController ctrl = loader.getController();
            ctrl.setData(g, currentUser);
            groupsGrid.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void handleViewLeaderboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/leaderboard.fxml"));
            Parent root = loader.load();
            LeaderboardController ctrl = loader.getController();
            ctrl.setUser(currentUser);
            groupsGrid.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void handleSearch() { refreshGroups(); }
    @FXML private void handleLogout(MouseEvent event) {
        org.example.service.NavigationService.getInstance().navigateToLogin(groupsGrid);
    }
    @FXML private void handleBackToDashboard(MouseEvent event) {
        org.example.service.NavigationService.getInstance().navigateToDashboard(groupsGrid, currentUser);
    }
}