package org.example;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import java.io.IOException;

public class ModifierSeanceController {

    @FXML private TextField        nomField;
    @FXML private TextField        heureDebutField;
    @FXML private TextField        dureeField;
    @FXML private TextField        dateField;
    @FXML private ComboBox<String> medailleBox;
    @FXML private TextField        utilisateurIdField;
    @FXML private TextField        heureDebutReelleField;
    @FXML private ComboBox<String> alerteBox;
    @FXML private Label            messageLabel;
    @FXML private Button           btnSauvegarder;
    @FXML private Button           btnRetour;
    @FXML private Button           btnMenu;

    private SeanceSport seanceAModifier;

    private final String STYLE_NORMAL =
            "-fx-background-color: white; -fx-text-fill: #4a235a; " +
                    "-fx-border-color: #d4af37; -fx-border-radius: 12; " +
                    "-fx-background-radius: 12; -fx-pref-height: 46; " +
                    "-fx-font-size: 13px; -fx-padding: 0 16;";

    private final String STYLE_ERREUR =
            "-fx-background-color: #fff5f5; -fx-text-fill: #4a235a; " +
                    "-fx-border-color: #e94560; -fx-border-width: 2; -fx-border-radius: 12; " +
                    "-fx-background-radius: 12; -fx-pref-height: 46; " +
                    "-fx-font-size: 13px; -fx-padding: 0 16;";

    @FXML
    public void initialize() {
        medailleBox.setItems(FXCollections.observableArrayList(
                "Or", "Argent", "Bronze", "Aucune"
        ));
        alerteBox.setItems(FXCollections.observableArrayList(
                "0 - Non envoyée", "1 - Envoyée"
        ));

        seanceAModifier = SeanceSelection.seance;
        if (seanceAModifier != null) {
            nomField.setText(seanceAModifier.getNomSeance());
            heureDebutField.setText(seanceAModifier.getHeureDebut());
            dureeField.setText(String.valueOf(seanceAModifier.getDureeMinutes()));
            dateField.setText(seanceAModifier.getDateSeance());
            medailleBox.setValue(seanceAModifier.getMedailleObtenue() != null
                    ? seanceAModifier.getMedailleObtenue() : "Aucune");
            utilisateurIdField.setText(String.valueOf(seanceAModifier.getUtilisateurId()));
            heureDebutReelleField.setText(seanceAModifier.getHeureDebutReelle() != null
                    ? seanceAModifier.getHeureDebutReelle() : "");
            alerteBox.setValue(seanceAModifier.getAlerteEnvoyee() == 1
                    ? "1 - Envoyée" : "0 - Non envoyée");
        }
    }

    @FXML
    public void sauvegarder(ActionEvent event) {
        String nom             = nomField.getText().trim();
        String heureDebut      = heureDebutField.getText().trim();
        String dureeStr        = dureeField.getText().trim();
        String date            = dateField.getText().trim();
        String medaille        = medailleBox.getValue();
        String utilisateurStr  = utilisateurIdField.getText().trim();
        String heureReelle     = heureDebutReelleField.getText().trim();
        String alerteStr       = alerteBox.getValue();

        resetStyles();

        if (nom.isEmpty() || nom.length() < 3) {
            nomField.setStyle(STYLE_ERREUR);
            afficherErreur("❌ Le nom doit contenir au moins 3 caractères !");
            return;
        }
        if (!heureDebut.matches("([01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d")) {
            heureDebutField.setStyle(STYLE_ERREUR);
            afficherErreur("❌ Format heure invalide ! Utilisez HH:MM:SS");
            return;
        }
        if (dureeStr.isEmpty() || !dureeStr.matches("\\d+")) {
            dureeField.setStyle(STYLE_ERREUR);
            afficherErreur("❌ La durée doit être un nombre entier !");
            return;
        }
        int duree = Integer.parseInt(dureeStr);
        if (duree < 5 || duree > 480) {
            dureeField.setStyle(STYLE_ERREUR);
            afficherErreur("❌ La durée doit être entre 5 et 480 minutes !");
            return;
        }
        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            dateField.setStyle(STYLE_ERREUR);
            afficherErreur("❌ Format date invalide ! Utilisez YYYY-MM-DD");
            return;
        }
        if (medaille == null) {
            afficherErreur("❌ Choisissez une médaille !");
            return;
        }
        if (utilisateurStr.isEmpty() || !utilisateurStr.matches("\\d+")) {
            utilisateurIdField.setStyle(STYLE_ERREUR);
            afficherErreur("❌ L'ID utilisateur doit être un nombre entier !");
            return;
        }
        if (!heureReelle.isEmpty() && !heureReelle.matches("([01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d")) {
            heureDebutReelleField.setStyle(STYLE_ERREUR);
            afficherErreur("❌ Format heure réelle invalide ! Utilisez HH:MM:SS");
            return;
        }
        if (alerteStr == null) {
            afficherErreur("❌ Choisissez le statut de l'alerte !");
            return;
        }

        try {
            seanceAModifier.nomSeance          = nom;
            seanceAModifier.heureDebut         = heureDebut;
            seanceAModifier.dureeMinutes       = Integer.parseInt(dureeStr);
            seanceAModifier.dateSeance         = date;
            seanceAModifier.medailleObtenue    = medaille;
            seanceAModifier.utilisateurId      = Integer.parseInt(utilisateurStr);
            seanceAModifier.heureDebutReelle   = heureReelle.isEmpty() ? null : heureReelle;
            seanceAModifier.alerteEnvoyee      = alerteStr.startsWith("1") ? 1 : 0;

            new ServiceSeanceSport().update(seanceAModifier);
            resetStyles();
            messageLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 13px; -fx-font-weight: bold;");
            messageLabel.setText("✅ Séance modifiée avec succès !");

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
        heureDebutField.setStyle(STYLE_NORMAL);
        dureeField.setStyle(STYLE_NORMAL);
        dateField.setStyle(STYLE_NORMAL);
        utilisateurIdField.setStyle(STYLE_NORMAL);
        heureDebutReelleField.setStyle(STYLE_NORMAL);
    }

    @FXML
    public void retour(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherSeance.fxml"));
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