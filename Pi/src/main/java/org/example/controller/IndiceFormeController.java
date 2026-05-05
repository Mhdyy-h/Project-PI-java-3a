package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import org.example.model.IndiceFormePhysique;
import org.example.model.IndiceFormePhysique.NiveauForme;
import org.example.model.User;
import org.example.service.IndiceFormeService;

import java.io.IOException;

public class IndiceFormeController {

    @FXML private Label lblScore;
    @FXML private Label lblNiveau;
    @FXML private Label lblEmoji;
    @FXML private Label lblConseil;
    @FXML private Label lblScoreReg;
    @FXML private Label lblScoreInt;
    @FXML private Label lblScoreCons;
    @FXML private Label lblScoreRec;
    @FXML private Label lblBarreReg;
    @FXML private Label lblBarreInt;
    @FXML private Label lblBarreCons;
    @FXML private Label lblBarreRec;
    @FXML private Label lblTotalSeances;
    @FXML private Label lblSeances7j;
    @FXML private Label lblDureeMoy;
    @FXML private Label lblSerie;
    @FXML private VBox  carteScore;

    private IndiceFormeService indiceService;

    @FXML
    public void initialize() {
        indiceService = new IndiceFormeService();
    }

    public void chargerIndice(User user) {
        if (user == null) return;
        IndiceFormePhysique indice = indiceService.calculerIndice(user.getId());
        afficherIndice(indice);
    }

    private void afficherIndice(IndiceFormePhysique indice) {
        NiveauForme niveau = indice.getNiveau();
        String couleur     = niveau.getCouleur();

        // ── Score principal ───────────────────────────────────────
        lblScore.setText(String.valueOf(indice.getScoreGlobal()));
        lblScore.setStyle("-fx-font-size: 72px; -fx-font-weight: bold;"
                + "-fx-text-fill: " + couleur + ";");
        lblEmoji.setText(niveau.getEmoji());
        lblNiveau.setText(niveau.getLibelle());
        lblNiveau.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;"
                + "-fx-text-fill: " + couleur + ";");

        carteScore.setStyle(
                "-fx-background-color: #1a1a2e;"
                        + "-fx-border-color: " + couleur + ";"
                        + "-fx-border-width: 3;"
                        + "-fx-border-radius: 16;"
                        + "-fx-background-radius: 16;"
                        + "-fx-padding: 30;");

        // ── Scores détaillés ──────────────────────────────────────
        lblScoreReg.setText(indice.getScoreRegularite()   + " / 25");
        lblScoreInt.setText(indice.getScoreIntensity()    + " / 25");
        lblScoreCons.setText(indice.getScoreConsistance() + " / 25");
        lblScoreRec.setText(indice.getScoreRecuperation() + " / 25");

        // ── Barres visuelles ──────────────────────────────────────
        lblBarreReg.setText(genererBarre(indice.getScoreRegularite(),  25));
        lblBarreInt.setText(genererBarre(indice.getScoreIntensity(),   25));
        lblBarreCons.setText(genererBarre(indice.getScoreConsistance(),25));
        lblBarreRec.setText(genererBarre(indice.getScoreRecuperation(),25));

        // ── Stats brutes ──────────────────────────────────────────
        lblTotalSeances.setText(indice.getTotalSeances() + " séances totales");
        lblSeances7j.setText(indice.getSeances7Jours()  + " séances cette semaine");
        lblDureeMoy.setText(String.format("%.0f min", indice.getDureeMoyenne())
                + " durée moyenne");
        lblSerie.setText(indice.getSerie() + " jours consécutifs");

        // ── Conseil ───────────────────────────────────────────────
        lblConseil.setText(indice.getConseil());
    }

    // Génère une barre visuelle type "████████░░░░"
    private String genererBarre(int score, int max) {
        int filled = (int) Math.round((score * 10.0) / max);
        int empty  = 10 - filled;
        return "█".repeat(filled) + "░".repeat(empty);
    }

    @FXML
    private void retourDashboard() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/view/MenuUser.fxml"));
            lblScore.getScene().setRoot(root);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}