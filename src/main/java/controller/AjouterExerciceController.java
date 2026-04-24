package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import model.Exercice;
import service.ServiceExercice;

import java.io.IOException;

public class AjouterExerciceController {

    @FXML private TextField        nomField;
    @FXML private ComboBox<String> intensiteBox;
    @FXML private TextField        caloriesField;
    @FXML private TextField        seanceIdField;
    @FXML private Label            messageLabel;
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

    @FXML
    public void initialize() {
        intensiteBox.setItems(FXCollections.observableArrayList(
                "Faible", "Moyenne", "Élevée"));

        // Validation temps réel — uniquement si le champ n'est pas vide
        nomField.textProperty().addListener((obs, old, val) -> {
            if (val != null && !val.isEmpty()) validerNom(val);
            else nomField.setStyle(STYLE_NORMAL);
        });
        caloriesField.textProperty().addListener((obs, old, val) -> {
            if (val != null && !val.isEmpty()) validerCalories(val);
            else caloriesField.setStyle(STYLE_NORMAL);
        });
        seanceIdField.textProperty().addListener((obs, old, val) -> {
            if (val != null && !val.isEmpty()) validerSeanceId(val);
            else seanceIdField.setStyle(STYLE_NORMAL);
        });
    }

    // ── Validations ───────────────────────────────────────────

    private boolean validerNom(String val) {
        if (val == null || val.trim().isEmpty()) {
            nomField.setStyle(STYLE_ERREUR);
            setMsg("⚠ Le nom est obligatoire", false);
            return false;
        }
        if (val.trim().length() < 3) {
            nomField.setStyle(STYLE_ERREUR);
            setMsg("⚠ Le nom doit contenir au moins 3 caractères", false);
            return false;
        }
        if (val.trim().length() > 50) {
            nomField.setStyle(STYLE_ERREUR);
            setMsg("⚠ Le nom ne doit pas dépasser 50 caractères", false);
            return false;
        }
        nomField.setStyle(STYLE_OK);
        messageLabel.setText("");
        return true;
    }

    private boolean validerCalories(String val) {
        if (val == null || val.trim().isEmpty()) {
            caloriesField.setStyle(STYLE_ERREUR);
            setMsg("⚠ Les calories sont obligatoires", false);
            return false;
        }
        try {
            double cal = Double.parseDouble(val.trim().replace(",", "."));
            if (cal <= 0) {
                caloriesField.setStyle(STYLE_ERREUR);
                setMsg("⚠ Les calories doivent être > 0", false);
                return false;
            }
            if (cal > 100) {
                caloriesField.setStyle(STYLE_ERREUR);
                setMsg("⚠ Les calories doivent être ≤ 100 cal/min", false);
                return false;
            }
            caloriesField.setStyle(STYLE_OK);
            messageLabel.setText("");
            return true;
        } catch (NumberFormatException e) {
            caloriesField.setStyle(STYLE_ERREUR);
            setMsg("⚠ Nombre décimal requis (ex: 8.5)", false);
            return false;
        }
    }

    private boolean validerSeanceId(String val) {
        if (val == null || val.trim().isEmpty()) {
            seanceIdField.setStyle(STYLE_ERREUR);
            setMsg("⚠ L'ID séance est obligatoire", false);
            return false;
        }
        if (!val.trim().matches("\\d+")) {
            seanceIdField.setStyle(STYLE_ERREUR);
            setMsg("⚠ L'ID séance doit être un entier positif", false);
            return false;
        }
        if (Integer.parseInt(val.trim()) <= 0) {
            seanceIdField.setStyle(STYLE_ERREUR);
            setMsg("⚠ L'ID séance doit être > 0", false);
            return false;
        }
        seanceIdField.setStyle(STYLE_OK);
        messageLabel.setText("");
        return true;
    }

    // ── Soumission ────────────────────────────────────────────

    @FXML
    public void ajouterExercice(ActionEvent event) {

        // Valider chaque champ indépendamment
        boolean nomOk      = validerNom(nomField.getText());
        boolean calOk      = validerCalories(caloriesField.getText());
        boolean seanceOk   = validerSeanceId(seanceIdField.getText());
        boolean intensiteOk = intensiteBox.getValue() != null;

        if (!intensiteOk) {
            setMsg("⚠ Choisissez une intensité !", false);
        }

        // Si un champ est invalide, on s'arrête sans écraser les messages
        if (!nomOk || !calOk || !seanceOk || !intensiteOk) {
            return;
        }

        try {
            double cal     = Double.parseDouble(caloriesField.getText().trim().replace(",", "."));
            int    idSeance = Integer.parseInt(seanceIdField.getText().trim());

            Exercice ex = new Exercice(
                    0,
                    nomField.getText().trim(),
                    intensiteBox.getValue(),
                    cal,
                    idSeance
            );
            new ServiceExercice().ajouter(ex);
            setMsg("✅ Exercice ajouté avec succès !", true);
            viderFormulaire();

        } catch (Exception e) {
            setMsg("❌ Erreur : " + e.getMessage(), false);
            e.printStackTrace(); // voir console pour détails
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
        messageLabel.setText("");
    }

    // ── Navigation ────────────────────────────────────────────

    @FXML
    public void ouvrirAfficher(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherExercice.fxml"));
            btnVoirListe.getScene().setRoot(root);
        } catch (IOException e) { System.err.println(e.getMessage()); }
    }

    @FXML
    public void ouvrirMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Menu.fxml"));
            btnMenu.getScene().setRoot(root);
        } catch (IOException e) { System.err.println(e.getMessage()); }
    }

    // ── Helper ────────────────────────────────────────────────

    private void setMsg(String msg, boolean succes) {
        messageLabel.setText(msg);
        messageLabel.setStyle(
                "-fx-font-size: 13px; -fx-font-weight: bold; " +
                        "-fx-text-fill: " + (succes ? "#27ae60" : "#e94560") + ";"
        );
    }
}