package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import org.example.model.Exercice;
import org.example.model.ExerciceSelection;
import org.example.service.ServiceExercice;

import java.io.IOException;

public class SupprimerExerciceController {

    @FXML private Label nomLabel;
    @FXML private Label intensiteLabel;
    @FXML private Label caloriesLabel;
    @FXML private Label messageLabel;
    @FXML private Button btnConfirmer;
    @FXML private Button btnAnnuler;
    @FXML private Button btnMenu;

    private Exercice exerciceASupprimer;

    @FXML
    public void initialize() {
        exerciceASupprimer = ExerciceSelection.exercice;
        if (exerciceASupprimer != null) {
            nomLabel.setText(exerciceASupprimer.getNomExercice());
            intensiteLabel.setText("💪 " + exerciceASupprimer.getIntensite());
            caloriesLabel.setText("🔥 " + exerciceASupprimer.getCaloriesParMinute() + " cal/min");
        }
    }

    @FXML
    public void confirmerSuppression(ActionEvent event) {
        try {
            new ServiceExercice().delete(exerciceASupprimer.getId());
            messageLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 13px; -fx-font-weight: bold;");
            messageLabel.setText("✅ Exercice supprimé avec succès !");
            btnConfirmer.setDisable(true);

            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(() -> {
                        try {
                            Parent root = FXMLLoader.load(getClass().getResource("/view/AfficherExercice.fxml"));
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
            Parent root = FXMLLoader.load(getClass().getResource("/view/AfficherExercice.fxml"));
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