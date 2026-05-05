package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import org.example.model.SeanceSelection;
import org.example.model.SeanceSport;
import org.example.service.ServiceSeanceSport;

import java.io.IOException;

public class SupprimerSeanceController {

    @FXML private Label nomLabel;
    @FXML private Label dureeLabel;
    @FXML private Label dateLabel;
    @FXML private Label messageLabel;
    @FXML private Button btnConfirmer;
    @FXML private Button btnAnnuler;
    @FXML private Button btnMenu;

    private SeanceSport seanceASupprimer;

    @FXML
    public void initialize() {
        seanceASupprimer = SeanceSelection.seance;
        if (seanceASupprimer != null) {
            nomLabel.setText(seanceASupprimer.getNomSeance());
            dureeLabel.setText("⏱️ " + seanceASupprimer.getDureeMinutes() + " min");
            dateLabel.setText("📅 " + seanceASupprimer.getDateSeance());
        }
    }

    @FXML
    public void confirmerSuppression(ActionEvent event) {
        try {
            new ServiceSeanceSport().delete(seanceASupprimer.getId());
            messageLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 13px; -fx-font-weight: bold;");
            messageLabel.setText("✅ Séance supprimée avec succès !");
            btnConfirmer.setDisable(true);

            // Retour automatique après 1.5s
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(() -> {
                        try {
                            Parent root = FXMLLoader.load(getClass().getResource("/view/AfficherSeance.fxml"));
                            btnAnnuler.getScene().setRoot(root);
                        } catch (IOException e) {
                            System.err.println(e.getMessage());
                        }
                    });
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                }
            }).start();

        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: #e94560; -fx-font-size: 13px;");
            messageLabel.setText("❌ Erreur : " + e.getMessage());
        }
    }

    @FXML
    public void annuler(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/AfficherSeance.fxml"));
            btnAnnuler.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @FXML
    public void ouvrirMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/Menu.fxml"));
            btnMenu.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}