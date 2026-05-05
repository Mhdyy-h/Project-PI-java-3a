package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import org.example.model.AlerteSurcharge;
import org.example.model.User;
import org.example.service.SurchargeService;

import java.io.IOException;

public class SurchargeController {

    @FXML private Label lblEmoji;
    @FXML private Label lblTitre;
    @FXML private Label lblMessage;
    @FXML private Label lblSeances7j;
    @FXML private Label lblSeances30j;
    @FXML private Label lblDureeMoy;
    @FXML private Label lblJoursRepos;
    @FXML private Label lblConseil;
    @FXML private VBox  carteAlerte;

    private SurchargeService surchargeService;
    private User             currentUser;

    @FXML
    public void initialize() {
        surchargeService = new SurchargeService();
    }

    public void chargerAnalyse(User user) {
        this.currentUser = user;
        if (user == null) return;

        AlerteSurcharge alerte = surchargeService.analyser(user.getId());
        afficherAlerte(alerte);
    }

    private void afficherAlerte(AlerteSurcharge alerte) {
        AlerteSurcharge.TypeAlerte type = alerte.getType();

        // ── Couleur selon le type ─────────────────────────────────
        String couleur = switch (type) {
            case CRITIQUE   -> "#ff4444";
            case ATTENTION  -> "#ff8800";
            case CONSEILLE  -> "#f0c040";
            case INSUFFISANT-> "#4488ff";
            default         -> "#44cc88";  // OPTIMAL
        };

        // ── Afficher les infos ────────────────────────────────────
        lblEmoji.setText(type.getEmoji());
        lblTitre.setText(type.getTitre());
        lblTitre.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;"
                + "-fx-text-fill: " + couleur + ";");
        lblMessage.setText(type.getMessage());

        carteAlerte.setStyle(
                "-fx-background-color: #1a1a2e;"
                        + "-fx-border-color: " + couleur + ";"
                        + "-fx-border-width: 2;"
                        + "-fx-border-radius: 12;"
                        + "-fx-background-radius: 12;"
                        + "-fx-padding: 20;");

        // ── Stats ─────────────────────────────────────────────────
        lblSeances7j.setText( "📅 Séances cette semaine : "
                + alerte.getSeances7Jours());
        lblSeances30j.setText("📊 Séances ce mois : "
                + alerte.getSeances30Jours());
        lblDureeMoy.setText(  "⏱ Durée moyenne : "
                + String.format("%.0f", alerte.getDureeMoyenne())
                + " min");
        lblJoursRepos.setText("😴 Jours de repos : "
                + alerte.getJoursRepos() + " jour(s)");

        // ── Conseil personnalisé ──────────────────────────────────
        lblConseil.setText(alerte.getConseil());
    }

    @FXML
    private void retourDashboard() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/view/MenuUser.fxml"));
            lblEmoji.getScene().setRoot(root);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}