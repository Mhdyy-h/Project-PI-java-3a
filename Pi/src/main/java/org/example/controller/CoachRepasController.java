package org.example.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.dao.AlerteDAO;
import org.example.dao.RepasDAO;
import org.example.model.Aliment;
import org.example.model.Repas;
import org.example.model.User;
import org.example.service.Chronoscoreservice;
import org.example.service.RepasService;

import java.io.IOException;
import java.util.List;

/**
 * Controller de la liste des repas d'un utilisateur – Vue Coach.
 */
public class CoachRepasController {

    @FXML private Label userInitialeLabel;
    @FXML private Label userNomLabel;
    @FXML private Label userEmailLabel;
    @FXML private Label totalRepasLabel;
    @FXML private Label totalCaloriesLabel;
    @FXML private Label scoreMoyenLabel;

    @FXML private TableView<Repas>             repasTable;
    @FXML private TableColumn<Repas, String>   colTitre;
    @FXML private TableColumn<Repas, String>   colMoment;
    @FXML private TableColumn<Repas, String>   colDate;
    @FXML private TableColumn<Repas, Integer>  colPoints;
    @FXML private TableColumn<Repas, Integer>  colCalories;

    private User coachUser;
    private User selectedUser;
    private ObservableList<Repas> repasList = FXCollections.observableArrayList();

    // ─────────────────────────────────────────────────
    //  INITIALISATION
    // ─────────────────────────────────────────────────

    @FXML
    public void initialize() {
        colTitre.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getTitreRepas()));
        colMoment.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getTypeMoment()));
        colDate.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getDateFormatee() + " " + d.getValue().getHeureFormatee()));
        colPoints.setCellValueFactory(d ->
                new SimpleIntegerProperty(d.getValue().getPointsGagnes()).asObject());
        colCalories.setCellValueFactory(d ->
                new SimpleIntegerProperty(d.getValue().getTotalCalories()).asObject());

        repasTable.setItems(repasList);
    }

    public void setCoachUser(User coach) {
        this.coachUser = coach;
    }

    public void setSelectedUser(User user) {
        this.selectedUser = user;
        afficherInfosUser();
        chargerRepas();
    }

    // ─────────────────────────────────────────────────
    //  CHARGEMENT
    // ─────────────────────────────────────────────────

    private void afficherInfosUser() {
        if (selectedUser == null) return;
        String nom = selectedUser.getNomComplet();
        userNomLabel.setText(nom);
        userEmailLabel.setText(selectedUser.getEmail());
        // Initiale
        String initiale = nom != null && !nom.isEmpty()
                ? String.valueOf(nom.charAt(0)).toUpperCase() : "U";
        userInitialeLabel.setText(initiale);
    }

    private void chargerRepas() {
        if (selectedUser == null) return;
        List<Repas> repas = RepasDAO.getByUtilisateurId(selectedUser.getId());
        repasList.setAll(repas);

        totalRepasLabel.setText(String.valueOf(repas.size()));

        // Calories du jour
        int cal = RepasService.getCaloriesTotalesJour(selectedUser.getId());
        totalCaloriesLabel.setText(String.valueOf(cal));

        // Score moyen
        double moy = repas.stream()
                .mapToInt(r -> Chronoscoreservice.calculerChronoScore(r).getTotalScore())
                .average().orElse(0);
        scoreMoyenLabel.setText(String.format("%.1f/14", moy));
    }

    // ─────────────────────────────────────────────────
    //  ACTIONS
    // ─────────────────────────────────────────────────

    @FXML
    private void voirAliments() {
        Repas selected = repasTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise",
                    "Veuillez sélectionner un repas dans la liste.");
            return;
        }

        // Fenêtre popup avec les aliments
        Stage popup = new Stage();
        popup.setTitle("Aliments – " + selected.getTitreRepas());

        VBox content = new VBox(16);
        content.setStyle("-fx-padding: 24; -fx-background-color: #f0f2f8;");

        Label titre = new Label("🍽️  Aliments du repas : " + selected.getTitreRepas());
        titre.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
        content.getChildren().add(titre);

        List<Aliment> aliments = selected.getAliments();
        List<Integer> quantites = selected.getQuantites();

        if (aliments.isEmpty()) {
            Label vide = new Label("Aucun aliment enregistré pour ce repas.");
            vide.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 13px;");
            content.getChildren().add(vide);
        } else {
            // En-tête tableau
            javafx.scene.layout.HBox header = new javafx.scene.layout.HBox(0);
            header.setStyle("-fx-background-color: #e8edff; -fx-background-radius: 8; -fx-padding: 10 16;");
            String[] cols = {"Aliment", "Qté", "Calories", "Protéines", "Glucides", "Lipides"};
            double[] widths = {180, 60, 90, 90, 90, 90};
            for (int i = 0; i < cols.length; i++) {
                Label lbl = new Label(cols[i]);
                lbl.setPrefWidth(widths[i]);
                lbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #4C6FFF;");
                header.getChildren().add(lbl);
            }
            content.getChildren().add(header);

            for (int i = 0; i < aliments.size(); i++) {
                Aliment a = aliments.get(i);
                int qte = i < quantites.size() ? quantites.get(i) : 1;
                javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(0);
                row.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 10 16;");

                String[] vals = {
                        a.getNomAliment(),
                        String.valueOf(qte),
                        (a.getCalories() * qte) + " cal",
                        String.format("%.1fg", a.getProteines() * qte),
                        String.format("%.1fg", a.getGlucides() * qte),
                        String.format("%.1fg", a.getLipides() * qte)
                };
                for (int j = 0; j < vals.length; j++) {
                    Label lbl = new Label(vals[j]);
                    lbl.setPrefWidth(widths[j]);
                    lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #1a1a2e;");
                    row.getChildren().add(lbl);
                }
                content.getChildren().add(row);
            }

            // Total calories
            Label total = new Label("Total : " + selected.getTotalCalories() + " cal | "
                    + String.format("%.1fg prot. | %.1fg gluc. | %.1fg lip.",
                    selected.getTotalProteines(), selected.getTotalGlucides(), selected.getTotalLipides()));
            total.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #4C6FFF; -fx-padding: 8 0 0 0;");
            content.getChildren().add(total);
        }

        javafx.scene.control.ScrollPane scroll = new javafx.scene.control.ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        popup.setScene(new Scene(scroll, 650, 450));
        popup.show();
    }

    @FXML
    private void ouvrirDashboard() {
        if (selectedUser == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/nutrition/coach_dashboard.fxml"));
            Parent root = loader.load();
            CoachDashboardController ctrl = loader.getController();
            ctrl.setUtilisateur(selectedUser);

            Stage stage = new Stage();
            stage.setTitle("Dashboard – " + selectedUser.getNomComplet());
            stage.setScene(new Scene(root, 900, 750));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le dashboard : " + e.getMessage());
        }
    }

    @FXML
    private void retournerVersListeUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/nutrition/coach_users.fxml"));
            Parent root = loader.load();
            CoachUsersController ctrl = loader.getController();
            ctrl.setCoachUser(coachUser);

            Stage stage = (Stage) repasTable.getScene().getWindow();
            stage.setScene(new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight()));
            stage.setTitle("BioSync – Suivi Nutritionnel");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String titre, String msg) {
        Alert a = new Alert(type);
        a.setTitle(titre);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
