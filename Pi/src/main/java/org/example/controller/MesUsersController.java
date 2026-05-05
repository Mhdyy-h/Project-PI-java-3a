package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.model.User;
import org.example.service.CoachUserService;

import java.io.IOException;
import java.util.List;

public class MesUsersController {

    @FXML private VBox      listeUsersContainer;
    @FXML private VBox      listeSansCoachContainer;
    @FXML private Label     lblNombreUsers;

    private CoachUserService coachUserService;
    private User             currentCoach;

    @FXML
    public void initialize() {
        coachUserService = new CoachUserService();
    }

    public void setCoach(User coach) {
        this.currentCoach = coach;
        chargerMesUsers();
        chargerUsersSansCoach();
    }

    // ── Mes Users assignés ────────────────────
    private void chargerMesUsers() {
        listeUsersContainer.getChildren().clear();
        List<User> users = coachUserService.getUsersDuCoach(currentCoach.getId());

        lblNombreUsers.setText("Mes Athletes (" + users.size() + ")");

        if (users.isEmpty()) {
            Label vide = new Label("Aucun athlète assigné");
            vide.setStyle("-fx-text-fill: #888888; -fx-font-size: 13px;");
            listeUsersContainer.getChildren().add(vide);
            return;
        }

        for (User user : users) {
            listeUsersContainer.getChildren().add(creerCarteUser(user));
        }
    }

    // ── Users disponibles sans coach ──────────
    private void chargerUsersSansCoach() {
        listeSansCoachContainer.getChildren().clear();
        List<User> users = coachUserService.getUsersSansCoach();

        for (User user : users) {
            listeSansCoachContainer.getChildren()
                    .add(creerCarteUserDisponible(user));
        }
    }

    // ── Carte User assigné ────────────────────
    private HBox creerCarteUser(User user) {
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-background-color: #1a1a2e;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 14 18;" +
                        "-fx-border-color: #44cc88;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;");

        Label avatar = new Label("👤");
        avatar.setStyle("-fx-font-size: 22px;");

        VBox info = new VBox(4);
        Label nom = new Label(user.getNomComplet());
        nom.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        Label email = new Label(user.getEmail());
        email.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");
        info.getChildren().addAll(nom, email);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRetirer = new Button("Retirer");
        btnRetirer.setStyle(
                "-fx-background-color: #c0392b;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 6 12;" +
                        "-fx-cursor: hand;");
        btnRetirer.setOnAction(e -> {
            coachUserService.retirerUser(currentCoach.getId(), user.getId());
            chargerMesUsers();
            chargerUsersSansCoach();
        });

        card.getChildren().addAll(avatar, info, spacer, btnRetirer);
        return card;
    }

    // ── Carte User disponible ─────────────────
    private HBox creerCarteUserDisponible(User user) {
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-background-color: #1a1a2e;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 14 18;" +
                        "-fx-border-color: #2a2a3e;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;");

        Label avatar = new Label("👤");
        avatar.setStyle("-fx-font-size: 22px;");

        VBox info = new VBox(4);
        Label nom = new Label(user.getNomComplet());
        nom.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        Label email = new Label(user.getEmail());
        email.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");
        info.getChildren().addAll(nom, email);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnAssigner = new Button("+ Assigner");
        btnAssigner.setStyle(
                "-fx-background-color: #7d3c98;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 6 12;" +
                        "-fx-cursor: hand;");
        btnAssigner.setOnAction(e -> {
            coachUserService.assignerUser(currentCoach.getId(), user.getId());
            chargerMesUsers();
            chargerUsersSansCoach();
        });

        card.getChildren().addAll(avatar, info, spacer, btnAssigner);
        return card;
    }

    @FXML
    private void retourMenu() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/view/MenuCoach.fxml"));
            listeUsersContainer.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}