package org.example.controller;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.example.model.Repas;
import org.example.model.Aliment;
import org.example.dao.RepasDAO;
import org.example.dao.AlimentDAO;
import org.example.service.RepasService;
import org.example.service.AlertService;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RepasFormController {

    @FXML private TextField titreField;
    @FXML private ComboBox<String> momentCombo;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> heureCombo;
    @FXML private ComboBox<String> minuteCombo;
    @FXML private ListView<Aliment> alimentsDisponiblesList;
    @FXML private ListView<Aliment> alimentsChoisisList;
    @FXML private TextField quantiteField;
    @FXML private Label totalCaloriesLabel;
    @FXML private Label scorePreviewLabel;

    private ObservableList<Aliment> alimentsDisponibles = FXCollections.observableArrayList();
    private ObservableList<Aliment> alimentsChoisis     = FXCollections.observableArrayList();
    private List<Integer> quantites = new ArrayList<>();
    private int utilisateurId;
    private Repas repasExistant;
    private Runnable onSaveCallback;

    // ─────────────────────────────────────────────────
    //  INITIALISATION
    // ─────────────────────────────────────────────────

    @FXML
    public void initialize() {
        momentCombo.setItems(FXCollections.observableArrayList(
                "MATIN", "MIDI", "COLLATION", "SOIR"));
        datePicker.setValue(LocalDate.now());

        List<String> heures = new ArrayList<>();
        for (int i = 0; i < 24; i++) heures.add(String.format("%02d", i));
        heureCombo.setItems(FXCollections.observableArrayList(heures));
        heureCombo.setValue(String.format("%02d", LocalTime.now().getHour()));

        List<String> minutes = new ArrayList<>();
        for (int i = 0; i < 60; i += 15) minutes.add(String.format("%02d", i));
        minuteCombo.setItems(FXCollections.observableArrayList(minutes));
        minuteCombo.setValue(String.format("%02d",
                LocalTime.now().getMinute() / 15 * 15));

        chargerAliments();
        alimentsChoisisList.setItems(alimentsChoisis);
        alimentsDisponiblesList.setItems(alimentsDisponibles);

        alimentsChoisisList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) retirerAliment();
        });

        quantiteField.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("\\d*")) quantiteField.setText(n.replaceAll("[^\\d]", ""));
        });
    }

    private void chargerAliments() {
        alimentsDisponibles.clear();
        alimentsDisponibles.addAll(AlimentDAO.getAll());
    }

    public void setUtilisateurId(int id) {
        this.utilisateurId = id;
        this.repasExistant = null;
    }

    public void setRepas(Repas repas) {
        this.repasExistant = repas;
        this.utilisateurId = repas.getUtilisateurId();
        titreField.setText(repas.getTitreRepas());
        momentCombo.setValue(repas.getTypeMoment());
        datePicker.setValue(repas.getDateConsommation().toLocalDate());
        heureCombo.setValue(String.format("%02d", repas.getDateConsommation().getHour()));
        minuteCombo.setValue(String.format("%02d", repas.getDateConsommation().getMinute()));
        alimentsChoisis.addAll(repas.getAliments());
        quantites.addAll(repas.getQuantites());
        alimentsDisponibles.removeAll(repas.getAliments());
        mettreAJourCalories();
    }

    public void setOnSave(Runnable callback) {
        this.onSaveCallback = callback;
    }

    // ─────────────────────────────────────────────────
    //  ACTIONS ALIMENTS
    // ─────────────────────────────────────────────────

    @FXML
    private void ajouterAliment() {
        Aliment selected = alimentsDisponiblesList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlertErreur("Selection manquante", "Selectionnez un aliment.");
            return;
        }
        int quantite = 1;
        try {
            quantite = Integer.parseInt(quantiteField.getText().trim());
            if (quantite < 1 || quantite > 50) {
                showAlertErreur("Quantite invalide", "Entre 1 et 50.");
                return;
            }
        } catch (NumberFormatException e) { quantite = 1; }
        alimentsChoisis.add(selected);
        quantites.add(quantite);
        alimentsDisponibles.remove(selected);
        quantiteField.clear();
        mettreAJourCalories();
    }

    @FXML
    private void retirerAliment() {
        int index = alimentsChoisisList.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            Aliment a = alimentsChoisis.remove(index);
            quantites.remove(index);
            alimentsDisponibles.add(a);
            mettreAJourCalories();
        }
    }

    private void mettreAJourCalories() {
        int total = 0;
        for (int i = 0; i < alimentsChoisis.size(); i++)
            total += alimentsChoisis.get(i).getCalories() * quantites.get(i);
        totalCaloriesLabel.setText(total + " calories");

        if (!alimentsChoisis.isEmpty() && momentCombo.getValue() != null
                && datePicker.getValue() != null) {
            try {
                Repas preview = new Repas();
                preview.setAliments(new ArrayList<>(alimentsChoisis));
                preview.setQuantites(new ArrayList<>(quantites));
                preview.setTypeMoment(momentCombo.getValue());
                preview.setDateConsommation(LocalDateTime.of(
                        datePicker.getValue(),
                        LocalTime.of(Integer.parseInt(heureCombo.getValue()),
                                Integer.parseInt(minuteCombo.getValue()))));
                int score = RepasService.calculerScoreIntelligent(preview);
                scorePreviewLabel.setText("Score estimé: " + score + "/14");
                if      (score >= 10) scorePreviewLabel.setStyle("-fx-text-fill: #27ae60;");
                else if (score >= 7)  scorePreviewLabel.setStyle("-fx-text-fill: #4C6FFF;");
                else if (score >= 4)  scorePreviewLabel.setStyle("-fx-text-fill: #f39c12;");
                else                  scorePreviewLabel.setStyle("-fx-text-fill: #e74c3c;");
            } catch (Exception ignored) {}
        }
    }

    // ─────────────────────────────────────────────────
    //  SAUVEGARDE
    // ─────────────────────────────────────────────────

    @FXML
    private void sauvegarder() {
        // ── Validations ──
        String titre = titreField.getText() != null ? titreField.getText().trim() : "";
        if (titre.isEmpty()) {
            marquerErreur(titreField);
            showAlertErreur("Obligatoire", "Le titre est obligatoire.");
            return;
        }
        if (titre.length() < 3) {
            marquerErreur(titreField);
            showAlertErreur("Titre trop court", "Minimum 3 caracteres.");
            return;
        }
        if (momentCombo.getValue() == null) {
            marquerErreur(momentCombo);
            showAlertErreur("Obligatoire", "Choisissez un moment.");
            return;
        }
        if (datePicker.getValue() == null
                || datePicker.getValue().isAfter(LocalDate.now())) {
            marquerErreur(datePicker);
            showAlertErreur("Date invalide", "Date obligatoire, pas dans le futur.");
            return;
        }
        if (alimentsChoisis.isEmpty()) {
            showAlertErreur("Aliments manquants", "Ajoutez au moins un aliment.");
            return;
        }
        Integer idExistant = repasExistant != null ? repasExistant.getId() : null;
        if (repasDejaExistant(utilisateurId, titre, momentCombo.getValue(),
                datePicker.getValue(), idExistant)) {
            marquerErreur(titreField);
            showAlertErreur("Doublon", "Ce repas existe deja pour cette date.");
            return;
        }
        reinitialiserTousLesStyles();

        // ── Construction du repas ──
        LocalDateTime dateConsommation = LocalDateTime.of(
                datePicker.getValue(),
                LocalTime.of(Integer.parseInt(heureCombo.getValue()),
                        Integer.parseInt(minuteCombo.getValue())));

        Repas repas;
        if (repasExistant != null) {
            repas = repasExistant;
            repas.setTitreRepas(titre);
            repas.setTypeMoment(momentCombo.getValue());
            repas.setDateConsommation(dateConsommation);
            repas.setAliments(new ArrayList<>(alimentsChoisis));
            repas.setQuantites(new ArrayList<>(quantites));
        } else {
            repas = new Repas(titre, momentCombo.getValue(),
                    dateConsommation, utilisateurId);
            repas.setAliments(new ArrayList<>(alimentsChoisis));
            repas.setQuantites(new ArrayList<>(quantites));
        }

        int score = RepasService.calculerScoreIntelligent(repas);
        repas.setPointsGagnes(score);

        // ── DÉTECTER les alertes AVANT sauvegarde ──
        List<String[]> alertesAfficher = detecterAlertesPourAffichage(repas);

        // ── Sauvegarder ──
        boolean success = repasExistant != null
                ? RepasDAO.update(repas)
                : RepasDAO.insert(repas);

        if (success) {
            // Sauvegarder les alertes en base
            AlertService.verifierRepas(repas);

            // Fermer le formulaire
            if (onSaveCallback != null) onSaveCallback.run();
            fermer();

            // Afficher popup si alertes détectées
            if (!alertesAfficher.isEmpty()) {
                afficherPopupAlertesClignotantes(alertesAfficher, score);
            }
        } else {
            showAlertErreur("Erreur", "Impossible de sauvegarder le repas.");
        }
    }

    // ─────────────────────────────────────────────────
    //  DÉTECTION ALERTES POUR AFFICHAGE
    //  Analyse le repas AVANT sauvegarde pour savoir
    //  quelles alertes vont apparaître
    // ─────────────────────────────────────────────────

    /**
     * Retourne une liste de [message, criticite] à afficher.
     * Appelé avant la sauvegarde pour ne pas rater l'affichage.
     */
    private List<String[]> detecterAlertesPourAffichage(Repas repas) {
        List<String[]> alertes = new ArrayList<>();
        int heure = repas.getDateConsommation().getHour();

        // ── Alerte 1 : Excitants tardifs (après 16h) ──
        if (heure >= 16) {
            List<Aliment> excitants = repas.getAliments().stream()
                    .filter(Aliment::isEstExcitant)
                    .toList();
            if (!excitants.isEmpty()) {
                String noms = excitants.stream()
                        .map(Aliment::getNomAliment)
                        .reduce((a, b) -> a + ", " + b).orElse("");
                String criticite = "JAUNE";
                alertes.add(new String[]{
                        "Aliments excitants apres 16h detectes : " + noms
                                + "\nRisque : perturbation du sommeil.",
                        criticite
                });
            }
        }

        // ── Alerte 2 : Calories excessives (> 1200) ──
        if (repas.getTotalCalories() > 1200) {
            alertes.add(new String[]{
                    "Repas tres calorique : " + repas.getTotalCalories()
                            + " cal (seuil : 1200 cal).\nRisque metabolique eleve.",
                    "ROUGE"
            });
        }

        // ── Alerte 3 : Dîner très tardif (après 22h) ──
        if (heure >= 22 && "SOIR".equals(repas.getTypeMoment())) {
            alertes.add(new String[]{
                    "Diner tres tardif detecte (" + String.format("%02d:xx", heure)
                            + ").\nManger apres 22h perturbe le sommeil.",
                    "ROUGE"
            });
        }

        // ── Alerte 4 : Score très bas (< 4) ──
        int score = RepasService.calculerScoreIntelligent(repas);
        if (score < 4) {
            alertes.add(new String[]{
                    "Score nutritionnel tres bas : " + score + "/14.\n"
                            + "Ce repas est desequilibre ou mal timé.",
                    "JAUNE"
            });
        }

        return alertes;
    }

    // ─────────────────────────────────────────────────
    //  POPUP CLIGNOTANTE
    // ─────────────────────────────────────────────────

    private void afficherPopupAlertesClignotantes(
            List<String[]> alertes, int score) {

        boolean hasRouge = alertes.stream()
                .anyMatch(a -> "ROUGE".equals(a[1]));

        String couleur1 = hasRouge ? "#c0392b" : "#e67e22";
        String couleur2 = hasRouge ? "#e74c3c" : "#f39c12";
        String emoji    = hasRouge ? "🚨" : "⚠️";
        String titreStr = hasRouge ? "ALERTE CRITIQUE !" : "ATTENTION !";

        // ── Stage ──
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initStyle(StageStyle.UNDECORATED);
        popup.setAlwaysOnTop(true);

        // ── Root VBox ──
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setMinWidth(430);
        root.setMaxWidth(480);
        appliquerStyle(root, couleur2);

        // ── Titre ──
        Label lblTitre = new Label(emoji + "  " + titreStr + "  " + emoji);
        lblTitre.setStyle(
                "-fx-font-size: 22px; -fx-font-weight: bold;"
                        + "-fx-text-fill: white;");

        // ── Zone messages ──
        VBox zoneMessages = new VBox(10);
        zoneMessages.setStyle(
                "-fx-background-color: rgba(0,0,0,0.3);"
                        + "-fx-background-radius: 10; -fx-padding: 15;");

        for (String[] alerte : alertes) {
            String icone = "ROUGE".equals(alerte[1]) ? "🔴 " : "🟡 ";
            Label lbl = new Label(icone + alerte[0]);
            lbl.setWrapText(true);
            lbl.setMaxWidth(420);
            lbl.setStyle(
                    "-fx-font-size: 13px; -fx-text-fill: white;"
                            + "-fx-font-weight: bold;");
            zoneMessages.getChildren().add(lbl);
        }

        // ── Score ──
        Label lblScore = new Label("Score de ce repas : " + score + " / 14");
        lblScore.setStyle(
                "-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.9);"
                        + "-fx-font-style: italic;");

        // ── Conseil ──
        Label lblConseil = new Label(
                "Conseil : Remplacez excitants du soir par une tisane.");
        lblConseil.setWrapText(true);
        lblConseil.setStyle(
                "-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.8);"
                        + "-fx-font-style: italic;");

        // ── Bouton ──
        Button btnOk = new Button("Compris !");
        btnOk.setStyle(
                "-fx-background-color: white;"
                        + "-fx-text-fill: " + couleur2 + ";"
                        + "-fx-font-weight: bold; -fx-font-size: 14px;"
                        + "-fx-background-radius: 20; -fx-padding: 10 30;"
                        + "-fx-cursor: hand;");
        btnOk.setOnAction(e -> popup.close());

        root.getChildren().addAll(lblTitre, zoneMessages, lblScore, lblConseil, btnOk);

        // ── Scene ──
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);

        // ── Fade in ──
        root.setOpacity(0);
        popup.show();
        popup.centerOnScreen();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // ── Clignotement après fade ──
        fadeIn.setOnFinished(ev -> {
            Timeline cligno = new Timeline();
            for (int i = 0; i < 6; i++) {
                final String c = (i % 2 == 0) ? couleur1 : couleur2;
                cligno.getKeyFrames().add(new KeyFrame(
                        Duration.millis(i * 350),
                        e -> appliquerStyle(root, c)
                ));
            }
            cligno.play();
            // Stabiliser après clignotement
            cligno.setOnFinished(e -> appliquerStyle(root, couleur2));
        });
    }

    private void appliquerStyle(VBox root, String couleur) {
        root.setStyle(
                "-fx-background-color: " + couleur + ";"
                        + "-fx-background-radius: 15;"
                        + "-fx-border-color: white;"
                        + "-fx-border-width: 3;"
                        + "-fx-border-radius: 15;"
                        + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.6),25,0,0,8);");
    }

    // ─────────────────────────────────────────────────
    //  TEST D'UNICITÉ
    // ─────────────────────────────────────────────────

    private boolean repasDejaExistant(int utilisateurId, String titre,
                                      String typeMoment, LocalDate date,
                                      Integer idExistant) {
        LocalDateTime debut = date.atStartOfDay();
        LocalDateTime fin   = date.atTime(23, 59, 59);
        List<Repas> repasJour = RepasDAO.getByUtilisateurAndDate(
                utilisateurId, debut, fin);
        for (Repas r : repasJour) {
            if (idExistant != null && r.getId() == idExistant) continue;
            if (r.getTitreRepas().equalsIgnoreCase(titre)
                    && r.getTypeMoment().equals(typeMoment)) return true;
        }
        return false;
    }

    // ─────────────────────────────────────────────────
    //  HELPERS UI
    // ─────────────────────────────────────────────────

    private void marquerErreur(Control c) {
        if (c != null) c.setStyle(
                "-fx-border-color: #e74c3c; -fx-border-width: 2px;"
                        + "-fx-border-radius: 4px;");
    }

    private void reinitialiserStyle(Control c) {
        if (c != null) c.setStyle("");
    }

    private void reinitialiserTousLesStyles() {
        reinitialiserStyle(titreField);
        reinitialiserStyle(momentCombo);
        reinitialiserStyle(datePicker);
    }

    @FXML
    private void fermer() {
        ((Stage) titreField.getScene().getWindow()).close();
    }

    private void showAlertErreur(String titre, String contenu) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titre);
        a.setHeaderText(null);
        a.setContentText(contenu);
        a.showAndWait();
    }
}