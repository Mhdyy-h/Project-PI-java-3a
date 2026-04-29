package org.example.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.dao.CertificationDAO;
import org.example.model.CertificationRequest;

import java.io.IOException;

public class CertificationRequestController {

    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> specialiteCombo;
    @FXML private TextArea motivationField;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        specialiteCombo.setItems(FXCollections.observableArrayList(
                "Devenir Coach",
                "Devenir Spécialiste"
        ));
        specialiteCombo.getSelectionModel().select(0);
    }

    @FXML
    private void handleSubmit() {
        String nom = nomField.getText().trim();
        String email = emailField.getText().trim();
        String specialiteDisplay = specialiteCombo.getValue();
        String motivation = motivationField.getText().trim();

        if (nom.isEmpty() || email.isEmpty() || specialiteDisplay == null) {
            statusLabel.setText("Veuillez remplir le nom, l'email et choisir une spécialité.");
            statusLabel.setStyle("-fx-text-fill: #ef4444;");
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            statusLabel.setText("Adresse email invalide.");
            statusLabel.setStyle("-fx-text-fill: #ef4444;");
            return;
        }

        // Map display value to DB value
        String specialiteDB = specialiteDisplay.equals("Devenir Coach") ? "COACH" : "SPECIALISTE";

        CertificationRequest req = new CertificationRequest();
        req.setNomComplet(nom);
        req.setEmail(email);
        req.setSpecialite(specialiteDB);
        req.setMotivation(motivation.isEmpty() ? null : motivation);

        boolean ok = CertificationDAO.insertRequest(req);
        if (ok) {
            statusLabel.setText("✓ Votre demande a été envoyée! Elle sera examinée sous 48-72h.");
            statusLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
            // Clear form
            nomField.clear();
            emailField.clear();
            motivationField.clear();
            specialiteCombo.getSelectionModel().select(0);
        } else {
            statusLabel.setText("Erreur lors de l'envoi. Veuillez réessayer.");
            statusLabel.setStyle("-fx-text-fill: #ef4444;");
        }
    }

    @FXML
    private void handleGoToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 400, 550);
            stage.setScene(scene);
            stage.setTitle("BioSync - Connexion");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
