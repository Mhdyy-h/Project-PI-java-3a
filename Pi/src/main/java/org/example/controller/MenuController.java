package org.example.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.example.model.Exercice;
import org.example.model.SeanceSport;
import org.example.service.ServiceExercice;
import org.example.service.ServiceSeanceSport;

import java.io.IOException;
import java.util.List;

public class MenuController {

    @FXML private Label totalSeancesLabel;
    @FXML private Label totalExercicesLabel;
    @FXML private Label moyenneDureeLabel;
    @FXML private Label totalMedaillesLabel;

    @FXML
    public void initialize() {
        new Thread(() -> {
            try {
                List<SeanceSport> seances = new ServiceSeanceSport().afficherAll();
                double moyenne = seances.stream()
                        .mapToInt(SeanceSport::getDureeMinutes)
                        .average().orElse(0);
                long medaillesOr = seances.stream()
                        .filter(s -> "Or".equalsIgnoreCase(s.getMedailleObtenue()))
                        .count();

                Platform.runLater(() -> {
                    totalSeancesLabel.setText(String.valueOf(seances.size()));
                    moyenneDureeLabel.setText(String.format("%.0f", moyenne));
                    totalMedaillesLabel.setText(String.valueOf(medaillesOr));
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    totalSeancesLabel.setText("0");
                    moyenneDureeLabel.setText("0");
                    totalMedaillesLabel.setText("0");
                });
            }

            try {
                List<Exercice> exercices = new ServiceExercice().afficherAll();
                Platform.runLater(() ->
                        totalExercicesLabel.setText(String.valueOf(exercices.size()))
                );
            } catch (Exception e) {
                Platform.runLater(() -> totalExercicesLabel.setText("0"));
            }
        }).start();
    }

    // ── Séances → MenuCoach ──────────────────────────────────
    @FXML
    public void ouvrirSeances(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/MenuCoach.fxml"));
            ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    // ── Exercices → MenuUser ─────────────────────────────────
    @FXML
    public void ouvrirExercices(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/MenuUser.fxml"));
            ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    // ── Admin — ouvre dans une nouvelle fenêtre ──────────────
    @FXML
    public void ouvrirAdmin(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/admin.fxml"));
            Stage adminStage = new Stage();
            adminStage.setTitle("⚙️ Back Office Admin");
            adminStage.setScene(new Scene(root));
            adminStage.setWidth(1200);
            adminStage.setHeight(780);
            adminStage.show();
        } catch (IOException e) {
            System.err.println("Erreur admin : " + e.getMessage());
        }
    }

    // ── Boutons Nav (ActionEvent) ────────────────────────────
    @FXML
    public void ouvrirSeancesNav(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/MenuCoach.fxml"));
            ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @FXML
    public void ouvrirExercicesNav(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/MenuUser.fxml"));
            ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @FXML
    public void ouvrirAdminNav(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/admin.fxml"));
            Stage adminStage = new Stage();
            adminStage.setTitle("⚙️ Back Office Admin");
            adminStage.setScene(new Scene(root));
            adminStage.setWidth(1200);
            adminStage.setHeight(780);
            adminStage.show();
        } catch (IOException e) {
            System.err.println("Erreur admin : " + e.getMessage());
        }
    }
}