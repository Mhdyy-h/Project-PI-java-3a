package org.example;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import java.io.IOException;

public class ModifierExerciceController {

    @FXML private TextField nomField;
    @FXML private ComboBox<String> intensiteBox;
    @FXML private TextField caloriesField;
    @FXML private TextField seanceIdField;
    @FXML private Label messageLabel;
    @FXML private Button btnSauvegarder;
    @FXML private Button btnRetour;
    @FXML private Button btnMenu;

    private Exercice exerciceAModifier;

    private final String STYLE_NORMAL =
            "-fx-background-color: white; -fx-text-fill: #4a235a; " +
                    "-fx-border-color: #d4af37; -fx-border-radius: 8; " +
                    "-fx-background-radius: 8; -fx-pref-height: 42; " +
                    "-fx-font-size: 13px; -fx-padding: 0 12;";

    private final String STYLE_ERREUR =
            "-fx-background-color: #fff5f5; -fx-text-fill: #4a235a; " +
                    "-fx-border-color: #e94560; -fx-border-width: 2; -fx-border-radius: 8; " +
                    "-fx-background-radius: 8; -fx-pref-height: 42; " +
                    "-fx-font-size: 13px; -fx-padding: 0 12;";

    @FXML
    public void initialize() {
        intensiteBox.setItems(FXCollections.observableArrayList(
                "Faible", "Moyenne", "Élevée"
        ));
        exerciceAModifier = ExerciceSelection.exercice;
        if (exerciceAModifier != null) {
            nomField.setText(exerciceAModifier.getNomExercice());
            intensiteBox.setValue(exerciceAModifier.getIntensite());
            caloriesField.setText(String.valueOf(exerciceAModifier.getCaloriesParMinute()));
            seanceIdField.setText(String.valueOf(exerciceAModifier.getSeanceId()));
        }
    }

    @FXML
    public void sauvegarder(ActionEvent event) {
        String nom = nomField.getText().trim();
        String intensite = intensiteBox.getValue();
        String caloriesStr = caloriesField.getText().trim();
        String seanceIdStr = seanceIdField.getText().trim();

        resetStyles();

        if (nom.isEmpty() || nom.length() < 3) {
            nomField.setStyle(STYLE_ERREUR);
            afficherErreur("❌ Le nom doit contenir au moins 3 caractères !");
            return;
        }
        if (intensite == null) {
            afficherErreur("❌ Choisissez une intensité !");
            return;
        }
        if (caloriesStr.isEmpty()) {
            caloriesField.setStyle(STYLE_ERREUR);
            afficherErreur("❌ Les calories sont obligatoires !");
            return;
        }
        try {
            double calories = Double.parseDouble(caloriesStr);
            if (calories <= 0 || calories > 100) {
                caloriesField.setStyle(STYLE_ERREUR);
                afficherErreur("❌ Les calories doivent être entre 0.1 et 100 !");
                return;
            }
        } catch (NumberFormatException e) {
            caloriesField.setStyle(STYLE_ERREUR);
            afficherErreur("❌ Les calories doivent être un nombre décimal !");
            return;
        }
        if (seanceIdStr.isEmpty() || !seanceIdStr.matches("\\d+")) {
            seanceIdField.setStyle(STYLE_ERREUR);
            afficherErreur("❌ L'ID séance doit être un nombre entier !");
            return;
        }

        try {
            exerciceAModifier.nomExercice = nom;
            exerciceAModifier.intensite = intensite;
            exerciceAModifier.caloriesParMinute = Double.parseDouble(caloriesStr);
            exerciceAModifier.seanceId = Integer.parseInt(seanceIdStr);

            new ServiceExercice().update(exerciceAModifier);
            resetStyles();
            messageLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 13px; -fx-font-weight: bold;");
            messageLabel.setText("✅ Exercice modifié avec succès !");
        } catch (Exception e) {
            afficherErreur("❌ Erreur : " + e.getMessage());
        }
    }

    private void afficherErreur(String msg) {
        messageLabel.setStyle("-fx-text-fill: #e94560; -fx-font-size: 13px; -fx-font-weight: bold;");
        messageLabel.setText(msg);
    }

    private void resetStyles() {
        nomField.setStyle(STYLE_NORMAL);
        caloriesField.setStyle(STYLE_NORMAL);
        seanceIdField.setStyle(STYLE_NORMAL);
    }

    @FXML
    public void retour(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherExercice.fxml"));
            btnRetour.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @FXML
    public void ouvrirMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Menu.fxml"));
            btnMenu.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}