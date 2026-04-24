package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.Exercice;
import model.SeanceSport;
import service.ServiceExercice;
import service.ServiceSeanceSport;

import java.io.IOException;
import java.util.List;

public class MenuController {

    @FXML private Label totalSeancesLabel;
    @FXML private Label totalExercicesLabel;
    @FXML private Label moyenneDureeLabel;
    @FXML private Label totalMedaillesLabel;

    // 🔥 FIX IMPORTANT : exécution en thread pour éviter freeze JavaFX
    @FXML
    public void initialize() {
        new Thread(this::chargerStatistiques).start();
    }

    private void chargerStatistiques() {
        try {
            List<SeanceSport> seances = new ServiceSeanceSport().afficherAll();
            totalSeancesLabel.setText(String.valueOf(seances.size()));

            double moyenne = seances.stream()
                    .mapToInt(SeanceSport::getDureeMinutes)
                    .average().orElse(0);

            moyenneDureeLabel.setText(String.format("%.0f", moyenne));

            long medaillesOr = seances.stream()
                    .filter(s -> "Or".equalsIgnoreCase(s.getMedailleObtenue()))
                    .count();

            totalMedaillesLabel.setText(String.valueOf(medaillesOr));

        } catch (Exception e) {
            totalSeancesLabel.setText("0");
            moyenneDureeLabel.setText("0");
            totalMedaillesLabel.setText("0");
        }

        try {
            List<Exercice> exercices = new ServiceExercice().afficherAll();
            totalExercicesLabel.setText(String.valueOf(exercices.size()));
        } catch (Exception e) {
            totalExercicesLabel.setText("0");
        }
    }

    // ── CARTES ──────────────────────────────────────

    @FXML
    public void ouvrirSeances(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherSeance.fxml"));
            totalSeancesLabel.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @FXML
    public void ouvrirExercices(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherExercice.fxml"));
            totalExercicesLabel.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @FXML
    public void ouvrirAdmin(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/admin.fxml"));
            Stage adminStage = new Stage();
            adminStage.setTitle("⚙️ Back Office Admin");
            adminStage.setScene(new Scene(root));
            adminStage.setWidth(1100);
            adminStage.setHeight(750);
            adminStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── NAVBAR ───────────────────────────────────────

    @FXML
    public void ouvrirSeancesNav(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherSeance.fxml"));
            totalSeancesLabel.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @FXML
    public void ouvrirExercicesNav(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherExercice.fxml"));
            totalExercicesLabel.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @FXML
    public void ouvrirAdminNav(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/admin.fxml"));
            Stage adminStage = new Stage();
            adminStage.setTitle("⚙️ Back Office Admin");
            adminStage.setScene(new Scene(root));
            adminStage.setWidth(1200);
            adminStage.setHeight(780);
            adminStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}