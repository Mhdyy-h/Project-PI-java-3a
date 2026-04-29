package org.example.controller;

import org.example.model.Recommandation;
import org.example.model.Repas;
import org.example.model.User;
import org.example.dao.RepasDAO;
import org.example.service.RepasService;
import org.example.service.Chronoscoreservice;
import org.example.service.AlertService;
import org.example.service.ExportPdfService;
import org.example.service.RecommandationService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class Nutritioncontroller {

    // ── TableView ──
    @FXML private TableView<Repas> repasTable;
    @FXML private TableColumn<Repas, String>  colTitre;
    @FXML private TableColumn<Repas, String>  colMoment;
    @FXML private TableColumn<Repas, String>  colDate;
    @FXML private TableColumn<Repas, Integer> colPoints;

    // ── Filtres & Recherche ──
    @FXML private TextField    rechercheField;       // recherche par titre
    @FXML private ComboBox<String> filtresMomentCombo; // filtre par moment
    @FXML private DatePicker   filtreDateDebut;       // filtre période début
    @FXML private DatePicker   filtreDateFin;         // filtre période fin

    // ── Labels stats ──
    @FXML private Label caloriesJourLabel;
    @FXML private Label caloriesPeriodeLabel;
    @FXML private Label moyenneScoreLabel;
    @FXML private Label totalRepasLabel;

    // ── Alertes ──
    @FXML private VBox alertesContainer;

    // ── Recommandations intelligentes ──
    @FXML private VBox  recommandationsContainer;
    @FXML private Label recosBadgeLabel;
    @FXML private Label noRecosLabel;

    private int utilisateurId;
    private User currentUser;
    private ObservableList<Repas> repasList    = FXCollections.observableArrayList();
    private FilteredList<Repas>   repasFiltres;

    // ─────────────────────────────────────────────────
    //  INITIALISATION
    // ─────────────────────────────────────────────────

    @FXML
    public void initialize() {
        // Colonnes
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titreRepas"));
        colMoment.setCellValueFactory(new PropertyValueFactory<>("typeMoment"));
        colDate.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDateFormatee()
                                + " " + cellData.getValue().getHeureFormatee()));
        colPoints.setCellValueFactory(new PropertyValueFactory<>("pointsGagnes"));

        // ── Filtrage dynamique ──────────────────────────
        repasFiltres = new FilteredList<>(repasList, p -> true);
        SortedList<Repas> repasTries = new SortedList<>(repasFiltres);
        repasTries.comparatorProperty().bind(repasTable.comparatorProperty());
        repasTable.setItems(repasTries);

        // ── Tri : activer le clic sur les en-têtes ──────
        repasTable.setSortPolicy(table -> true); // géré par SortedList

        // ── ComboBox filtre moment ──────────────────────
        filtresMomentCombo.setItems(FXCollections.observableArrayList(
                "Tous", "MATIN", "MIDI", "COLLATION", "SOIR"));
        filtresMomentCombo.setValue("Tous");

        // ── Listeners pour mise à jour automatique du filtre ──
        rechercheField.textProperty().addListener((obs, o, n) -> appliquerFiltres());
        filtresMomentCombo.valueProperty().addListener((obs, o, n) -> appliquerFiltres());
        filtreDateDebut.valueProperty().addListener((obs, o, n) -> appliquerFiltres());
        filtreDateFin.valueProperty().addListener((obs, o, n) -> appliquerFiltres());

        // Double-clic pour détails
        repasTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) voirDetailsRepas();
        });
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void setUtilisateurId(int id) {
        this.utilisateurId = id;
        chargerRepas();
        chargerStatsJour();
        chargerAlertes();
        chargerRecommandations();
    }

    // ─────────────────────────────────────────────────
    //  CHARGEMENT
    // ─────────────────────────────────────────────────

    private void chargerRepas() {
        List<Repas> repas = RepasDAO.getByUtilisateurId(utilisateurId);
        repasList.clear();
        repasList.addAll(repas);
        appliquerFiltres();
        mettreAJourCompteur();
    }

    private void chargerStatsJour() {
        // ── Calories : repas du jour en priorité, sinon total de tous les repas ──
        List<Repas> repasJour = RepasDAO.getTodayRepas(utilisateurId);
        if (!repasJour.isEmpty()) {
            int calJour = repasJour.stream().mapToInt(Repas::getTotalCalories).sum();
            caloriesJourLabel.setText(calJour + " cal");
            if (caloriesPeriodeLabel != null)
                caloriesPeriodeLabel.setText("Aujourd'hui ("
                        + repasJour.size() + " repas)");
        } else {
            // Aucun repas aujourd'hui : afficher le total de tous les repas
            int calTotal = repasList.stream().mapToInt(Repas::getTotalCalories).sum();
            caloriesJourLabel.setText(calTotal + " cal");
            if (caloriesPeriodeLabel != null)
                caloriesPeriodeLabel.setText("Total histor. ("
                        + repasList.size() + " repas)");
        }

        // ── Score moyen : sur tous les repas de l'utilisateur ──
        if (!repasList.isEmpty()) {
            double moyenne = repasList.stream()
                    .mapToInt(r -> Chronoscoreservice.calculerChronoScore(r).getTotalScore())
                    .average().orElse(0);
            String couleur;
            if      (moyenne >= 10) couleur = "#10b981";
            else if (moyenne >= 7)  couleur = "#4C6FFF";
            else if (moyenne >= 4)  couleur = "#f39c12";
            else                    couleur = "#e74c3c";
            moyenneScoreLabel.setText(String.format("%.1f/14", moyenne));
            moyenneScoreLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: "
                    + couleur + ";");
        } else {
            moyenneScoreLabel.setText("—");
            moyenneScoreLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #9ca3af;");
        }
    }

    private void chargerAlertes() {
        alertesContainer.getChildren().clear();
        var alertes = AlertService.getAlertesActives(utilisateurId);
        for (var alerte : alertes) {
            Label alerteLabel = new Label("⚠️ " + alerte.getMessage());
            alerteLabel.setStyle("-fx-text-fill: " + alerte.getCouleurCriticite()
                    + "; -fx-padding: 8; -fx-background-color: #fff3e0; -fx-background-radius: 8;");
            alertesContainer.getChildren().add(alerteLabel);
        }
    }

    // ─────────────────────────────────────────────────
    //  RECOMMANDATIONS INTELLIGENTES
    // ─────────────────────────────────────────────────

    private void chargerRecommandations() {
        if (recommandationsContainer == null) return;
        recommandationsContainer.getChildren().clear();

        List<Recommandation> recos = RecommandationService.analyser(
                new java.util.ArrayList<>(repasList));

        // Mise à jour du badge
        int nb = recos.size();
        if (recosBadgeLabel != null) {
            recosBadgeLabel.setText(nb + (nb <= 1 ? " conseil" : " conseils"));
            recosBadgeLabel.setStyle(
                    nb == 0 ? "-fx-background-color: #d1fae5; -fx-text-fill: #10b981; -fx-background-radius: 20; -fx-padding: 4 12; -fx-font-size: 11px; -fx-font-weight: bold;"
                            : "-fx-background-color: #e8edff; -fx-text-fill: #4C6FFF; -fx-background-radius: 20; -fx-padding: 4 12; -fx-font-size: 11px; -fx-font-weight: bold;");
        }

        // Afficher ou cacher le message "Aucun conseil"
        if (noRecosLabel != null) {
            noRecosLabel.setVisible(nb == 0);
            noRecosLabel.setManaged(nb == 0);
        }

        // Construire les cartes
        for (Recommandation reco : recos) {
            recommandationsContainer.getChildren().add(buildRecoCard(reco));
        }
    }

    /**
     * Construit une carte visuelle pour une recommandation.
     * Barre colorée gauche + icône + message + conseil.
     */
    private javafx.scene.layout.HBox buildRecoCard(Recommandation reco) {
        javafx.scene.layout.HBox card = new javafx.scene.layout.HBox(12);
        card.setStyle(
                "-fx-background-color: " + reco.getCouleurFond() + ";"
                + "-fx-background-radius: 10;"
                + "-fx-padding: 12 16;"
                + "-fx-border-color: " + reco.getCouleurBord() + ";"
                + "-fx-border-width: 0 0 0 4;"
                + "-fx-border-radius: 0 8 8 0;"
                + "-fx-alignment: CENTER_LEFT;");

        // Icône
        Label icone = new Label(reco.getIcone());
        icone.setStyle("-fx-font-size: 20px;");

        // Textes
        javafx.scene.layout.VBox textes = new javafx.scene.layout.VBox(4);
        textes.setStyle("-fx-pref-width: 10000;");

        // Badge type
        Label badge = new Label(reco.getLibelleType());
        badge.setStyle("-fx-background-color: " + reco.getCouleurType() + ";"
                + "-fx-text-fill: white; -fx-font-size: 9px; -fx-font-weight: bold;"
                + "-fx-background-radius: 20; -fx-padding: 2 8;");

        Label message = new Label(reco.getMessage());
        message.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
        message.setWrapText(true);

        Label conseil = new Label(reco.getConseil());
        conseil.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");
        conseil.setWrapText(true);

        textes.getChildren().addAll(badge, message, conseil);
        card.getChildren().addAll(icone, textes);
        return card;
    }

    // ─────────────────────────────────────────────────
    //  FILTRAGE (recherche + moment + période)
    // ─────────────────────────────────────────────────

    /**
     * Applique simultanément les 3 filtres :
     * - recherche par titre (insensible à la casse)
     * - filtre par moment (MATIN / MIDI / COLLATION / SOIR / Tous)
     * - filtre par période (date début → date fin)
     */
    private void appliquerFiltres() {
        String recherche = rechercheField.getText() != null
                ? rechercheField.getText().toLowerCase().trim() : "";
        String moment    = filtresMomentCombo.getValue();
        LocalDate debut  = filtreDateDebut.getValue();
        LocalDate fin    = filtreDateFin.getValue();

        repasFiltres.setPredicate(repas -> {

            // ── Filtre recherche par titre ──
            if (!recherche.isEmpty()
                    && !repas.getTitreRepas().toLowerCase().contains(recherche)) {
                return false;
            }

            // ── Filtre moment ──
            if (moment != null && !moment.equals("Tous")
                    && !repas.getTypeMoment().equals(moment)) {
                return false;
            }

            // ── Filtre date début ──
            if (debut != null
                    && repas.getDateConsommation().toLocalDate().isBefore(debut)) {
                return false;
            }

            // ── Filtre date fin ──
            if (fin != null
                    && repas.getDateConsommation().toLocalDate().isAfter(fin)) {
                return false;
            }

            return true;
        });

        mettreAJourCompteur();
    }

    /** Réinitialise tous les filtres */
    @FXML
    private void reinitialiserFiltres() {
        rechercheField.clear();
        filtresMomentCombo.setValue("Tous");
        filtreDateDebut.setValue(null);
        filtreDateFin.setValue(null);
        appliquerFiltres();
    }

    private void mettreAJourCompteur() {
        if (totalRepasLabel != null) {
            long visible = repasFiltres.size();
            long total   = repasList.size();
            totalRepasLabel.setText(visible + " / " + total + " repas");
        }
        // Rafraîchir aussi les stats à chaque changement de filtre
        if (utilisateurId > 0 && !repasList.isEmpty()) {
            chargerStatsJour();
        }
    }

    // ─────────────────────────────────────────────────
    //  EXPORT PDF
    // ─────────────────────────────────────────────────

    /**
     * Exporte la liste des repas actuellement affichée (après filtrage) en PDF.
     * Ouvre un FileChooser pour choisir l'emplacement de sauvegarde.
     */
    @FXML
    private void exporterPdf() {
        List<Repas> repasVisibles = repasFiltres.stream().collect(Collectors.toList());

        if (repasVisibles.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Export PDF",
                    "Aucun repas à exporter. Modifiez les filtres et réessayez.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport PDF");
        fileChooser.setInitialFileName("rapport_repas_"
                + LocalDate.now().toString().replace("-", "") + ".pdf");
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("Fichiers PDF (*.pdf)", "*.pdf"));

        File fichier = fileChooser.showSaveDialog(repasTable.getScene().getWindow());
        if (fichier == null) return;

        try {
            ExportPdfService.exporterListeRepas(repasVisibles, fichier.getAbsolutePath());
            showAlert(Alert.AlertType.INFORMATION, "Export réussi",
                    "Le rapport PDF a été généré avec succès :\n" + fichier.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur d'export",
                    "Impossible de générer le PDF.\nDétail : " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────
    //  ACTIONS CRUD
    // ─────────────────────────────────────────────────

    @FXML
    private void nouveauRepas() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/nutrition/repas_form.fxml"));
            Parent root = loader.load();
            RepasFormController controller = loader.getController();
            controller.setUtilisateurId(utilisateurId);
            controller.setOnSave(() -> {
                chargerRepas();
                chargerStatsJour();
                chargerAlertes();
                chargerRecommandations();
            });
            Stage stage = new Stage();
            stage.setTitle("Nouveau repas");
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

    @FXML
    private void voirDetailsRepas() {
        Repas selected = repasTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/nutrition/repas_show.fxml"));
            Parent root = loader.load();
            RepasShowController controller = loader.getController();
            controller.setRepas(selected);
            Stage stage = new Stage();
            stage.setTitle("Détails du repas");
            stage.setScene(new Scene(root, 700, 500));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void modifierRepas() {
        Repas selected = repasTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/nutrition/repas_form.fxml"));
            Parent root = loader.load();
            RepasFormController controller = loader.getController();
            controller.setRepas(selected);
            controller.setOnSave(() -> {
                chargerRepas();
                chargerStatsJour();
                chargerAlertes();
                chargerRecommandations();
            });
            Stage stage = new Stage();
            stage.setTitle("Modifier le repas");
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void supprimerRepas() {
        Repas selected = repasTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le repas");
        confirm.setContentText("Voulez-vous vraiment supprimer \""
                + selected.getTitreRepas() + "\" ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (RepasDAO.delete(selected.getId())) {
                chargerRepas();
                chargerStatsJour();
                chargerAlertes();
                chargerRecommandations();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Repas supprimé !");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Impossible de supprimer le repas.");
            }
        }
    }

    @FXML
    private void ouvrirDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/nutrition/nutrition_dashboard.fxml"));
            Parent root = loader.load();
            Nutritiondashboardcontroller controller = loader.getController();
            controller.setUtilisateurId(utilisateurId);
            Stage stage = new Stage();
            stage.setTitle("Dashboard Nutritionnel");
            stage.setScene(new Scene(root, 900, 700));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void gererAliments() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/nutrition/aliments.fxml"));
            Parent root = loader.load();
            AlimentController controller = loader.getController();
            controller.setOnDataChanged(this::chargerRepas);
            Stage stage = new Stage();
            stage.setTitle("Gestion des aliments");
            stage.setScene(new Scene(root, 900, 600));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────
    //  RETOUR VERS LE DASHBOARD
    // ─────────────────────────────────────────────────

    @FXML
    private void retournerVersDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/dashboard.fxml"));
            Parent root = loader.load();
            AdminController adminController = loader.getController();
            if (currentUser != null) {
                adminController.setUser(currentUser);
            }
            Stage stage = (Stage) repasTable.getScene().getWindow();
            stage.setScene(new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight()));
            stage.setTitle("BioSync - Dashboard");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de retourner au dashboard : " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────
    //  HELPER UI
    // ─────────────────────────────────────────────────

    private void showAlert(Alert.AlertType type, String titre, String contenu) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(contenu);
        alert.showAndWait();
    }
}