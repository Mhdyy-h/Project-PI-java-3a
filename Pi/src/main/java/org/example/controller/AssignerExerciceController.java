package org.example.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import org.example.dao.SeanceExerciceDAO;
import org.example.model.Exercice;
import org.example.model.SeanceExercice;
import org.example.model.SeanceSport;
import org.example.service.ServiceExercice;
import org.example.dao.DatabaseConnection;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AssignerExerciceController {

    @FXML private Label labelSeance;
    @FXML private ListView<String> listExercicesAssignes;
    @FXML private ListView<String> listExercicesDisponibles;
    @FXML private TextField seriesField;
    @FXML private TextField repetitionsField;
    @FXML private Label messageLabel;

    private SeanceSport seance;
    private List<Exercice> tousLesExercices;
    private SeanceExerciceDAO seanceExerciceDAO;
    private ServiceExercice serviceExercice;

    @FXML
    public void initialize() {
        seanceExerciceDAO = new SeanceExerciceDAO(
                DatabaseConnection.getConnection()
        );
        serviceExercice = new ServiceExercice();
    }

    // Appelé depuis AfficherSeanceController
    public void setSeance(SeanceSport seance) {
        this.seance = seance;
        labelSeance.setText("Séance : " + seance.getNomSeance()
                + " — " + seance.getDureeMinutes() + " min");
        chargerExercices();
    }

    private void chargerExercices() {
        try {
            // Exercices déjà assignés
            List<SeanceExercice> assignes =
                    seanceExerciceDAO.getParSeance(seance.getId());
            listExercicesAssignes.setItems(
                    FXCollections.observableArrayList(
                            assignes.stream()
                                    .map(se -> se.getExercice().getNomExercice()
                                            + " — " + se.getSeries() + " séries x "
                                            + se.getRepetitions() + " rép")
                                    .toList()
                    )
            );

            // Tous les exercices disponibles
            tousLesExercices = serviceExercice.afficherAll();
            listExercicesDisponibles.setItems(
                    FXCollections.observableArrayList(
                            tousLesExercices.stream()
                                    .map(e -> e.getNomExercice()
                                            + " — " + e.getIntensite()
                                            + " — " + e.getCaloriesParMinute() + " cal/min")
                                    .toList()
                    )
            );

        } catch (Exception e) {
            messageLabel.setText("❌ Erreur : " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: #e94560;");
        }
    }

    @FXML
    public void assignerExercice() {
        int index = listExercicesDisponibles.getSelectionModel()
                .getSelectedIndex();
        if (index < 0) {
            setMessage("⚠️ Sélectionnez un exercice !", false);
            return;
        }
        if (seriesField.getText().isEmpty()
                || repetitionsField.getText().isEmpty()) {
            setMessage("⚠️ Remplissez séries et répétitions !", false);
            return;
        }

        try {
            Exercice ex = tousLesExercices.get(index);
            SeanceExercice se = new SeanceExercice();
            se.setSeanceId(seance.getId());
            se.setExerciceId(ex.getId());
            se.setOrdre(listExercicesAssignes.getItems().size() + 1);
            se.setSeries(Integer.parseInt(seriesField.getText().trim()));
            se.setRepetitions(
                    Integer.parseInt(repetitionsField.getText().trim())
            );
            seanceExerciceDAO.ajouter(se);
            setMessage("✅ Exercice assigné avec succès !", true);
            chargerExercices();
            seriesField.clear();
            repetitionsField.clear();

        } catch (SQLException e) {
            setMessage("❌ Erreur BD : " + e.getMessage(), false);
        }
    }

    @FXML
    public void retirerExercice() {
        int index = listExercicesAssignes.getSelectionModel()
                .getSelectedIndex();
        if (index < 0) {
            setMessage("⚠️ Sélectionnez un exercice assigné !", false);
            return;
        }
        try {
            List<SeanceExercice> assignes =
                    seanceExerciceDAO.getParSeance(seance.getId());
            SeanceExercice se = assignes.get(index);
            seanceExerciceDAO.supprimer(se.getSeanceId(), se.getExerciceId());
            setMessage("✅ Exercice retiré !", true);
            chargerExercices();
        } catch (SQLException e) {
            setMessage("❌ Erreur BD : " + e.getMessage(), false);
        }
    }

    @FXML
    public void retour() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/view/AfficherSeance.fxml")
            );
            messageLabel.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void setMessage(String msg, boolean success) {
        messageLabel.setText(msg);
        messageLabel.setStyle(success
                ? "-fx-text-fill: #27ae60; -fx-font-weight: bold;"
                : "-fx-text-fill: #e94560; -fx-font-weight: bold;");
    }
}