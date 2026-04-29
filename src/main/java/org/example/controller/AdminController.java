package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.model.User;
import org.example.service.ServiceExercice;
import org.example.service.ServiceSeanceSport;
import javafx.scene.control.ScrollPane;
import java.io.IOException;

public class AdminController {

    @FXML private Label topBarTitle;
    @FXML private Button btnDashboard;
    @FXML private Button btnSeances;
    @FXML private Button btnExercices;

    @FXML private VBox paneDashboard;
    @FXML private VBox paneSeances;
    @FXML private VBox paneExercices;
    @FXML private VBox tableSeances;
    @FXML private VBox tableExercices;
    @FXML private VBox contenuAjouterSeance;
    @FXML private VBox contenuModifierSeance;
    @FXML private VBox contenuAjouterExercice;
    @FXML private VBox contenuModifierExercice;
    @FXML private VBox paneConfirmSuppr;
    @FXML private VBox detailsSuppr;
    @FXML private VBox contentArea;

    @FXML private Label statSeances;
    @FXML private Label statExercices;
    @FXML private Label statMedailles;
    @FXML private Label statCalories;
    @FXML private Label supprType;
    @FXML private Label supprNom;
    @FXML private Label supprDetail;

    @FXML private VBox paneAjouterSeance;
    @FXML private VBox paneModifierSeance;
    @FXML private VBox paneAjouterExercice;
    @FXML private VBox paneModifierExercice;
    @FXML private VBox cardSeances;

    private User currentUser;

    public void setUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void initialize() {
        chargerStats();
    }

    private void chargerStats() {
        new Thread(() -> {
            try {
                var seances = new ServiceSeanceSport().afficherAll();
                var exercices = new ServiceExercice().afficherAll();

                double moyCalories = exercices.stream()
                        .mapToDouble(e -> e.getCaloriesParMinute())
                        .average().orElse(0);

                long medailles = seances.stream()
                        .filter(s -> s.getMedailleObtenue() != null
                                && !s.getMedailleObtenue().equals("Aucune"))
                        .count();

                javafx.application.Platform.runLater(() -> {
                    statSeances.setText(String.valueOf(seances.size()));
                    statExercices.setText(String.valueOf(exercices.size()));
                    statMedailles.setText(String.valueOf(medailles));
                    statCalories.setText(
                            String.format("%.1f", moyCalories)
                    );
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    statSeances.setText("0");
                    statExercices.setText("0");
                    statMedailles.setText("0");
                    statCalories.setText("0");
                });
            }
        }).start();
    }

    @FXML
    public void showDashboard() {
        cacherTout();
        paneDashboard.setVisible(true);
        paneDashboard.setManaged(true);
        topBarTitle.setText("📊 Dashboard");
    }

    @FXML
    public void showSeances() {
        try {
            cacherTout();
            paneSeances.setVisible(true);
            paneSeances.setManaged(true);
            topBarTitle.setText("🏃 Séances");

            tableSeances.getChildren().clear();
            var seances = new ServiceSeanceSport().afficherAll();
            for (var seance : seances) {
                javafx.scene.layout.HBox row =
                        new javafx.scene.layout.HBox(12);
                row.setStyle(
                        "-fx-padding: 12 16; -fx-background-color: white;" +
                                "-fx-border-color: transparent transparent #f0f2f8 transparent;"
                );
                Label nom = new Label(seance.getNomSeance());
                nom.setStyle("-fx-font-weight: bold; -fx-pref-width: 200;");
                Label duree = new Label(seance.getDureeMinutes() + " min");
                duree.setStyle("-fx-text-fill: #27ae60; -fx-pref-width: 100;");
                Label date = new Label(seance.getDateSeance());
                date.setStyle("-fx-text-fill: #555; -fx-pref-width: 120;");
                row.getChildren().addAll(nom, duree, date);
                tableSeances.getChildren().add(row);
            }
        } catch (Exception e) {
            System.err.println("Erreur séances : " + e.getMessage());
        }
    }

    @FXML
    public void showExercices() {
        try {
            cacherTout();
            paneExercices.setVisible(true);
            paneExercices.setManaged(true);
            topBarTitle.setText("💪 Exercices");

            tableExercices.getChildren().clear();
            var exercices = new ServiceExercice().afficherAll();
            for (var ex : exercices) {
                javafx.scene.layout.HBox row =
                        new javafx.scene.layout.HBox(12);
                row.setStyle(
                        "-fx-padding: 12 16; -fx-background-color: white;" +
                                "-fx-border-color: transparent transparent #f0f2f8 transparent;"
                );
                Label nom = new Label(ex.getNomExercice());
                nom.setStyle("-fx-font-weight: bold; -fx-pref-width: 200;");
                Label intensite = new Label(ex.getIntensite());
                intensite.setStyle("-fx-text-fill: #7d3c98; -fx-pref-width: 100;");
                Label cal = new Label(ex.getCaloriesParMinute() + " cal/min");
                cal.setStyle("-fx-text-fill: #e94560; -fx-pref-width: 120;");
                row.getChildren().addAll(nom, intensite, cal);
                tableExercices.getChildren().add(row);
            }
        } catch (Exception e) {
            System.err.println("Erreur exercices : " + e.getMessage());
        }
    }

    @FXML
    public void ajouterSeance() {
        try {
            cacherTout();
            paneAjouterSeance.setVisible(true);
            paneAjouterSeance.setManaged(true);
            topBarTitle.setText("➕ Ajouter Séance");
            Parent form = FXMLLoader.load(
                    getClass().getResource("/view/AjouterSeance.fxml")
            );
            contenuAjouterSeance.getChildren().setAll(form);
        } catch (IOException e) {
            System.err.println("Erreur : " + e.getMessage());
        }
    }

    @FXML
    public void ajouterExercice() {
        try {
            cacherTout();
            paneAjouterExercice.setVisible(true);
            paneAjouterExercice.setManaged(true);
            topBarTitle.setText("➕ Ajouter Exercice");
            Parent form = FXMLLoader.load(
                    getClass().getResource("/view/AjouterExercice.fxml")
            );
            contenuAjouterExercice.getChildren().setAll(form);
        } catch (IOException e) {
            System.err.println("Erreur : " + e.getMessage());
        }
    }

    @FXML
    public void annulerSuppression() {
        cacherTout();
        showDashboard();
    }

    @FXML
    public void confirmerSuppression() {
        cacherTout();
        showDashboard();
    }

    @FXML
    public void fermerFenetre() {
        Stage stage = (Stage) btnDashboard.getScene().getWindow();
        stage.close();
    }

    private void cacherTout() {
        paneDashboard.setVisible(false);
        paneDashboard.setManaged(false);
        paneSeances.setVisible(false);
        paneSeances.setManaged(false);
        paneExercices.setVisible(false);
        paneExercices.setManaged(false);
        paneAjouterSeance.setVisible(false);
        paneAjouterSeance.setManaged(false);
        paneModifierSeance.setVisible(false);
        paneModifierSeance.setManaged(false);
        paneAjouterExercice.setVisible(false);
        paneAjouterExercice.setManaged(false);
        paneModifierExercice.setVisible(false);
        paneModifierExercice.setManaged(false);
        paneConfirmSuppr.setVisible(false);
        paneConfirmSuppr.setManaged(false);
    }
}