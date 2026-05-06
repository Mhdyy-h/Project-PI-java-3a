package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.dao.UserDAO;
import org.example.model.User;

import java.io.IOException;
import java.util.List;

public class LeaderboardController {

    @FXML private VBox leaderboardContainer;

    private User currentUser;

    // FIX: Method to receive the user from the previous screen
    public void setUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void initialize() {
        loadLeaderboard();
    }

    private void loadLeaderboard() {
        leaderboardContainer.getChildren().clear();
        List<User> topUsers = UserDAO.getTopUsers(10); // Fetch top 10

        int rank = 1;
        for (User user : topUsers) {
            leaderboardContainer.getChildren().add(createRankCard(user, rank));
            rank++;
        }
    }

    private HBox createRankCard(User user, int rank) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-padding: 15 25; -fx-background-radius: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4);");

        // Rank Number / Medal
        Label rankLabel = new Label(String.valueOf(rank));
        String rankColor = switch (rank) {
            case 1 -> "#F59E0B"; // Gold
            case 2 -> "#94A3B8"; // Silver
            case 3 -> "#B45309"; // Bronze
            default -> "#4C6FFF"; // Standard Blue
        };
        rankLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: " + rankColor + "; -fx-min-width: 30;");

        // User Avatar Circle (Initials)
        Label avatar = new Label(user.getNomComplet().substring(0, 1).toUpperCase());
        avatar.setAlignment(Pos.CENTER);
        avatar.setPrefSize(45, 45);
        avatar.setStyle("-fx-background-color: " + rankColor + "22; -fx-text-fill: " + rankColor + "; " +
                "-fx-font-weight: bold; -fx-background-radius: 50;");

        // Name and BioPoints
        VBox userInfo = new VBox(2);
        Label name = new Label(user.getNomComplet());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1a1a2e;");
        Label score = new Label(user.getScoreGlobal() + " BioPoints");
        score.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
        userInfo.getChildren().addAll(name, score);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Badge for Top 3
        if (rank <= 3) {
            Label trophy = new Label(rank == 1 ? "🏆" : (rank == 2 ? "🥈" : "🥉"));
            trophy.setStyle("-fx-font-size: 20px;");
            card.getChildren().addAll(rankLabel, avatar, userInfo, spacer, trophy);
        } else {
            card.getChildren().addAll(rankLabel, avatar, userInfo);
        }

        return card;
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/user_community.fxml"));
            Parent root = loader.load();

            // FIX: Pass the user back to the community controller so it can refresh
            UserCommunityController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);

            leaderboardContainer.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}