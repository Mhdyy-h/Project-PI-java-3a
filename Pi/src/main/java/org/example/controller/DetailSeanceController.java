package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.dao.SeanceExerciceDAO;
import org.example.model.SeanceExercice;
import org.example.model.SeanceSport;
import org.example.model.Session;
import org.example.service.ServiceSeanceSport;

import java.io.IOException;
import java.util.List;

public class DetailSeanceController {

    @FXML private Label  labelTitre;
    @FXML private Label  labelDuree;
    @FXML private Label  labelDate;
    @FXML private Label  labelHeure;
    @FXML private Label  labelMedaille;
    @FXML private Label  labelCalories;
    @FXML private VBox   exercicesContainer;
    @FXML private Label  labelVide;
    @FXML private Button btnAssigner;
    @FXML private HBox   boxAssigner;

    private SeanceSport seance;

    public void setSeance(SeanceSport seance) {
        this.seance = seance;

        // ✅ DEBUG — à supprimer après test
        System.out.println(">>> Session.role = '" + Session.role + "'");
        System.out.println(">>> isCoach = " + Session.isCoach());

        afficherInfos();
        afficherExercices();

        boolean isCoach = Session.isCoach();
        btnAssigner.setVisible(isCoach);
        btnAssigner.setManaged(isCoach);
        boxAssigner.setVisible(isCoach);
        boxAssigner.setManaged(isCoach);
    }

    private void afficherInfos() {
        labelTitre.setText("🏃 " + seance.getNomSeance());
        labelDuree.setText(seance.getDureeMinutes() + " minutes");
        labelDate.setText(seance.getDateSeance() != null ? seance.getDateSeance() : "—");
        labelHeure.setText(seance.getHeureDebut() != null ? seance.getHeureDebut() : "—");

        String med = seance.getMedailleObtenue() != null
                ? seance.getMedailleObtenue() : "Aucune";
        String[] medInfo = switch (med) {
            case "Or"     -> new String[]{"🥇 Or",     "#c9a227"};
            case "Argent" -> new String[]{"🥈 Argent", "#6b7280"};
            case "Bronze" -> new String[]{"🥉 Bronze", "#cd7f32"};
            default       -> new String[]{"— Aucune",  "#9ca3af"};
        };
        labelMedaille.setText(medInfo[0]);
        labelMedaille.setStyle("-fx-font-weight: bold; -fx-text-fill: " + medInfo[1] + ";");
    }

    private void afficherExercices() {
        try {
            SeanceExerciceDAO dao = new SeanceExerciceDAO(
                    new ServiceSeanceSport().getConnection()
            );
            List<SeanceExercice> liste = dao.getParSeance(seance.getId());

            if (liste.isEmpty()) {
                labelVide.setVisible(true);
                labelVide.setManaged(true);
                labelCalories.setText("🔥 0 cal estimées");
                return;
            }

            double totalCal = 0;
            for (SeanceExercice se : liste) {
                double cal = se.getExercice().getCaloriesParMinute()
                        * se.getSeries()
                        * se.getRepetitions() / 10.0;
                totalCal += cal;

                HBox ligne = new HBox(12);
                ligne.setStyle(
                        "-fx-background-color: #f0f2f8;" +
                                "-fx-background-radius: 8;" +
                                "-fx-padding: 10 14;"
                );

                Label nom = new Label("💪 " + se.getExercice().getNomExercice());
                nom.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;" +
                        "-fx-text-fill: #1a0f4f; -fx-pref-width: 150;");

                Label intensite = new Label(se.getExercice().getIntensite());
                intensite.setStyle("-fx-font-size: 12px; -fx-text-fill: #7d3c98;" +
                        "-fx-pref-width: 80;");

                Label details = new Label(
                        se.getSeries() + " séries × " + se.getRepetitions() + " rép"
                );
                details.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");

                Label calLabel = new Label(
                        "🔥 " + String.format("%.1f", cal) + " cal"
                );
                calLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #10b981;" +
                        "-fx-font-weight: bold;");

                ligne.getChildren().addAll(nom, intensite, details, calLabel);
                exercicesContainer.getChildren().add(ligne);
            }

            labelCalories.setText("🔥 Total : " +
                    String.format("%.0f", totalCal) + " cal estimées");

        } catch (Exception e) {
            labelVide.setText("❌ Erreur : " + e.getMessage());
            labelVide.setVisible(true);
            labelVide.setManaged(true);
        }
    }

    @FXML
    public void ouvrirAssigner() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/AssignerExercice.fxml"));
            Parent root = loader.load();
            AssignerExerciceController ctrl = loader.getController();
            ctrl.setSeance(seance);
            labelTitre.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @FXML
    public void goBack() {
        System.out.println(">>> goBack — role = " + Session.role); // debug
        naviguer(Session.isCoach()
                ? "/view/AfficherSeance.fxml"
                : "/view/VoirSeances.fxml");
    }

    @FXML
    public void goMenu() {
        System.out.println(">>> goMenu — role = " + Session.role); // debug
        naviguer(Session.isCoach()
                ? "/view/MenuCoach.fxml"
                : "/view/MenuUser.fxml");
    }

    @FXML
    public void goSeances() {
        System.out.println(">>> goSeances — role = " + Session.role); // debug
        naviguer(Session.isCoach()
                ? "/view/AfficherSeance.fxml"
                : "/view/VoirSeances.fxml");
    }

    private void naviguer(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            labelTitre.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}