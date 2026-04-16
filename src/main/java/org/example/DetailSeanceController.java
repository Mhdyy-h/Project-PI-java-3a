package org.example;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;

public class DetailSeanceController {

    @FXML private Label labelTitre;
    @FXML private Label labelDuree;
    @FXML private Label labelDate;
    @FXML private Label labelHeure;
    @FXML private Label labelMedaille;
    @FXML private ListView<String> listExercices;

    private SeanceSport seanceActuelle;

    public void setSeance(SeanceSport s) {
        this.seanceActuelle = s;
        labelTitre.setText("🏃 " + s.getNomSeance());
        labelDuree.setText(s.getDureeMinutes() + " minutes");
        labelDate.setText(s.getDateSeance());
        labelHeure.setText(s.getHeureDebut());
        labelMedaille.setText(s.getMedailleObtenue() != null ? s.getMedailleObtenue() : "Aucune");

        // Charger les exercices liés via seance_exercice
        chargerExercices(s.getId());
    }

    private void chargerExercices(int seanceId) {
        try {
            SeanceExerciceDAO dao = new SeanceExerciceDAO(MyConnection.getConnection());
            List<SeanceExercice> liste = dao.getParSeance(seanceId);

            listExercices.getItems().clear();

            if (liste.isEmpty()) {
                listExercices.getItems().add("Aucun exercice lié à cette séance.");
            } else {
                for (SeanceExercice se : liste) {
                    // Récupérer le nom de l'exercice
                    ServiceExercice svc = new ServiceExercice();
                    Exercice ex = svc.getById(se.getExerciceId());
                    String ligne = se.getOrdre() + ". " +
                            (ex != null ? ex.getNomExercice() : "Exercice #" + se.getExerciceId()) +
                            "  —  " + se.getSeries() + " séries × " + se.getRepetitions() + " reps";
                    listExercices.getItems().add(ligne);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/AfficherSeance.fxml"));
        Stage stage = (Stage) labelTitre.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    @FXML
    private void goMenu() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/Menu.fxml"));
        Stage stage = (Stage) labelTitre.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    @FXML
    private void goSeances() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/AfficherSeance.fxml"));
        Stage stage = (Stage) labelTitre.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    @FXML
    private void goExercices() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/AfficherExercice.fxml"));
        Stage stage = (Stage) labelTitre.getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}