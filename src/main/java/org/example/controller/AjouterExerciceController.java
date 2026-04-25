package org.example.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import org.example.model.Exercice;
import org.example.service.ServiceExercice;

import java.io.IOException;

public class AjouterExerciceController {

    @FXML private TextField        nomField;
    @FXML private ComboBox<String> intensiteBox;
    @FXML private TextField        caloriesField;
    @FXML private TextField        seanceIdField;
    @FXML private Label            messageLabel;
    @FXML private Label            errNom;
    @FXML private Label            errIntensite;
    @FXML private Label            errCalories;
    @FXML private Label            errSeance;
    @FXML private Button           btnVoirListe;
    @FXML private Button           btnMenu;

    private static final String STYLE_NORMAL =
            "-fx-background-color: #faf8ff; -fx-text-fill: #4a235a; " +
                    "-fx-border-color: #d4af37; -fx-border-radius: 12; " +
                    "-fx-background-radius: 12; -fx-pref-height: 46; " +
                    "-fx-font-size: 13px; -fx-padding: 0 16;";

    private static final String STYLE_ERREUR =
            "-fx-background-color: #fff5f5; -fx-text-fill: #4a235a; " +
                    "-fx-border-color: #e94560; -fx-border-width: 2; -fx-border-radius: 12; " +
                    "-fx-background-radius: 12; -fx-pref-height: 46; " +
                    "-fx-font-size: 13px; -fx-padding: 0 16;";

    private static final String STYLE_OK =
            "-fx-background-color: #f0fff6; -fx-text-fill: #4a235a; " +
                    "-fx-border-color: #27ae60; -fx-border-width: 2; -fx-border-radius: 12; " +
                    "-fx-background-radius: 12; -fx-pref-height: 46; " +
                    "-fx-font-size: 13px; -fx-padding: 0 16;";

    private static final String ERR_STYLE =
            "-fx-font-size: 11px; -fx-text-fill: #e94560; -fx-font-weight: bold;";

    private static final String OK_STYLE =
            "-fx-font-size: 11px; -fx-text-fill: #27ae60; -fx-font-weight: bold;";

    // ── Initialize ────────────────────────────────────────────

    @FXML
    public void initialize() {
        intensiteBox.setItems(FXCollections.observableArrayList(
                "Faible", "Moyenne", "Élevée"));

        // Validation temps réel
        nomField.textProperty().addListener((obs, old, val) -> validerNom());
        caloriesField.textProperty().addListener((obs, old, val) -> validerCalories());
        seanceIdField.textProperty().addListener((obs, old, val) -> validerSeanceId());
        intensiteBox.valueProperty().addListener((obs, old, val) -> validerIntensite());
    }

    // ── Validations ───────────────────────────────────────────

    private boolean validerNom() {
        String val = nomField.getText().trim();
        if (val.isEmpty()) {
            nomField.setStyle(STYLE_ERREUR);
            errNom.setText("❌ Le nom est obligatoire !");
            errNom.setStyle(ERR_STYLE);
            return false;
        }
        if (val.length() < 3) {
            nomField.setStyle(STYLE_ERREUR);
            errNom.setText("❌ Minimum 3 caractères !");
            errNom.setStyle(ERR_STYLE);
            return false;
        }
        if (val.length() > 50) {
            nomField.setStyle(STYLE_ERREUR);
            errNom.setText("❌ Maximum 50 caractères !");
            errNom.setStyle(ERR_STYLE);
            return false;
        }
        nomField.setStyle(STYLE_OK);
        errNom.setText("✅ Nom valide");
        errNom.setStyle(OK_STYLE);
        return true;
    }

    private boolean validerIntensite() {
        if (intensiteBox.getValue() == null) {
            errIntensite.setText("❌ Choisissez une intensité !");
            errIntensite.setStyle(ERR_STYLE);
            return false;
        }
        errIntensite.setText("✅ Intensité : " + intensiteBox.getValue());
        errIntensite.setStyle(OK_STYLE);
        return true;
    }

    private boolean validerCalories() {
        String val = caloriesField.getText().trim();
        if (val.isEmpty()) {
            caloriesField.setStyle(STYLE_ERREUR);
            errCalories.setText("❌ Les calories sont obligatoires !");
            errCalories.setStyle(ERR_STYLE);
            return false;
        }
        try {
            double cal = Double.parseDouble(val.replace(",", "."));
            if (cal <= 0) {
                caloriesField.setStyle(STYLE_ERREUR);
                errCalories.setText("❌ Les calories doivent être > 0 !");
                errCalories.setStyle(ERR_STYLE);
                return false;
            }
            if (cal > 100) {
                caloriesField.setStyle(STYLE_ERREUR);
                errCalories.setText("❌ Maximum 100 cal/min !");
                errCalories.setStyle(ERR_STYLE);
                return false;
            }
            caloriesField.setStyle(STYLE_OK);
            errCalories.setText("✅ " + cal + " cal/min");
            errCalories.setStyle(OK_STYLE);
            return true;
        } catch (NumberFormatException e) {
            caloriesField.setStyle(STYLE_ERREUR);
            errCalories.setText("❌ Nombre décimal requis (ex: 8.5) !");
            errCalories.setStyle(ERR_STYLE);
            return false;
        }
    }

    private boolean validerSeanceId() {
        String val = seanceIdField.getText().trim();
        if (val.isEmpty()) {
            seanceIdField.setStyle(STYLE_ERREUR);
            errSeance.setText("❌ L'ID séance est obligatoire !");
            errSeance.setStyle(ERR_STYLE);
            return false;
        }
        if (!val.matches("\\d+") || Integer.parseInt(val) <= 0) {
            seanceIdField.setStyle(STYLE_ERREUR);
            errSeance.setText("❌ Entier positif requis (ex: 1, 2, 3) !");
            errSeance.setStyle(ERR_STYLE);
            return false;
        }
        seanceIdField.setStyle(STYLE_OK);
        errSeance.setText("✅ ID valide");
        errSeance.setStyle(OK_STYLE);
        return true;
    }

    // ── Soumission ────────────────────────────────────────────

    @FXML
    public void ajouterExercice(ActionEvent event) {
        boolean ok =
                validerNom() &
                        validerIntensite() &
                        validerCalories() &
                        validerSeanceId();

        if (!ok) {
            messageLabel.setText("⚠️ Corrigez les erreurs avant de continuer !");
            messageLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #e94560;");
            return;
        }

        try {
            double cal      = Double.parseDouble(caloriesField.getText().trim().replace(",", "."));
            int    idSeance = Integer.parseInt(seanceIdField.getText().trim());

            Exercice ex = new Exercice(
                    0,
                    nomField.getText().trim(),
                    intensiteBox.getValue(),
                    cal,
                    idSeance
            );
            new ServiceExercice().ajouter(ex);

            messageLabel.setText("✅ Exercice ajouté avec succès !");
            messageLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
            viderFormulaire();

        } catch (Exception e) {
            messageLabel.setText("❌ Erreur BD : " + e.getMessage());
            messageLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #e94560;");
            e.printStackTrace();
        }
    }

    private void viderFormulaire() {
        nomField.clear();
        intensiteBox.setValue(null);
        caloriesField.clear();
        seanceIdField.clear();
        nomField.setStyle(STYLE_NORMAL);
        caloriesField.setStyle(STYLE_NORMAL);
        seanceIdField.setStyle(STYLE_NORMAL);
        errNom.setText("");
        errIntensite.setText("");
        errCalories.setText("");
        errSeance.setText("");
    }

    // ── Navigation ────────────────────────────────────────────

    @FXML
    public void ouvrirAfficher(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/AfficherExercice.fxml"));
            btnVoirListe.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Erreur AfficherExercice : " + e.getMessage());
        }
    }

    @FXML
    public void ouvrirMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/Menu.fxml"));
            btnMenu.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Erreur Menu : " + e.getMessage());
        }
    }
}