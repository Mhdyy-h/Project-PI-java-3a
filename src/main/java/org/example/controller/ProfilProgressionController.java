package org.example.controller;

import org.example.model.Badge;
import org.example.model.NiveauAthlete;
import org.example.model.ProfilProgression;
import org.example.model.User;
import org.example.service.ProgressionService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;        // ← AJOUTER
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.io.IOException;           // ← AJOUTER

public class ProfilProgressionController {

    @FXML private Label       lblNomUser;
    @FXML private Label       lblNiveau;
    @FXML private Label       lblXP;
    @FXML private Label       lblTotalSeances;
    @FXML private Label       lblSerie;
    @FXML private Label       lblMessage;
    @FXML private ProgressBar progressionBar;
    @FXML private Label       lblPourcentage;
    @FXML private FlowPane    badgesContainer;

    private ProgressionService progressionService;

    @FXML
    public void initialize() {
        progressionService = new ProgressionService();
    }

    public void chargerProfil(User user) {
        ProfilProgression profil = progressionService.calculerProfil(user);
        afficherProfil(profil);
    }

    private void afficherProfil(ProfilProgression profil) {
        NiveauAthlete niveau = profil.getNiveau();

        lblNomUser.setText(profil.getUser().getNomComplet());
        lblNiveau.setText(niveau.getEmoji() + " " + niveau.getLibelle());
        lblXP.setText("✨ " + profil.getPointsXP() + " XP");
        lblTotalSeances.setText(
                "🏃 " + profil.getTotalSeances() + " séances complétées");
        lblSerie.setText(
                "🔥 Série actuelle : " + profil.getSerieActuelle() + " jours");

        double pct = profil.getPourcentageProgression() / 100.0;
        progressionBar.setProgress(pct);
        lblPourcentage.setText(
                String.format("%.0f%%", profil.getPourcentageProgression()));

        lblMessage.setText(progressionService.getMessageMotivant(profil));

        badgesContainer.getChildren().clear();
        for (Badge badge : profil.getBadges()) {
            badgesContainer.getChildren().add(creerCarteBadge(badge));
        }
    }

    private VBox creerCarteBadge(Badge badge) {
        VBox carte = new VBox(5);
        carte.setAlignment(javafx.geometry.Pos.CENTER);
        carte.setPrefWidth(100);
        carte.setPrefHeight(100);

        Label emoji = new Label(badge.getType().getEmoji());
        emoji.setStyle("-fx-font-size: 28px;");

        Label nom = new Label(badge.getType().getNom());
        nom.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; "
                + "-fx-wrap-text: true; -fx-text-alignment: center;");

        if (badge.isDebloque()) {
            carte.setStyle(
                    "-fx-background-color: #1a1a2e;"
                            + "-fx-border-color: #f0c040;"
                            + "-fx-border-width: 2;"
                            + "-fx-border-radius: 10;"
                            + "-fx-background-radius: 10;"
                            + "-fx-padding: 10;");
            nom.setStyle(nom.getStyle() + "-fx-text-fill: #f0c040;");
            Label date = new Label(badge.getDateObtention().toString());
            date.setStyle("-fx-font-size: 9px; -fx-text-fill: #aaaaaa;");
            carte.getChildren().addAll(emoji, nom, date);

        } else {
            emoji.setStyle("-fx-font-size: 28px; -fx-opacity: 0.3;");
            carte.setStyle(
                    "-fx-background-color: #2a2a2a;"
                            + "-fx-border-color: #555555;"
                            + "-fx-border-width: 1;"
                            + "-fx-border-radius: 10;"
                            + "-fx-background-radius: 10;"
                            + "-fx-padding: 10;"
                            + "-fx-opacity: 0.6;");
            nom.setStyle(nom.getStyle() + "-fx-text-fill: #888888;");
            Label verrou = new Label("🔒");
            verrou.setStyle("-fx-font-size: 14px;");
            carte.getChildren().addAll(emoji, nom, verrou);
        }

        Tooltip.install(carte, new Tooltip(badge.getType().getDescription()));
        return carte;
    }
    @FXML
    private void retourDashboard() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/view/MenuUser.fxml"));
            lblNomUser.getScene().setRoot(root);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}