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

import java.io.File;
import java.io.IOException;
import javafx.stage.FileChooser;

public class CertificationRequestController {

    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> specialiteCombo;
    @FXML private TextArea motivationField;
    @FXML private Label statusLabel;
    @FXML private Label pdfNameLabel;

    private File selectedPdf;

    @FXML
    public void initialize() {
        specialiteCombo.setItems(FXCollections.observableArrayList(
                "Devenir Coach",
                "Devenir Spécialiste"
        ));
        specialiteCombo.getSelectionModel().select(0);
    }

    @FXML
    private void handleSelectPdf(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir le document justificatif (PDF)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (file != null) {
            selectedPdf = file;
            pdfNameLabel.setText(file.getName());
            pdfNameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #10b981; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void handleSubmit() {
        String nom = nomField.getText().trim();
        String email = emailField.getText().trim();
        String specialiteDisplay = specialiteCombo.getValue();
        String motivation = motivationField.getText().trim();

        if (nom.isEmpty() || email.isEmpty() || specialiteDisplay == null || selectedPdf == null) {
            statusLabel.setText("Veuillez remplir le nom, l'email, la spécialité et choisir un PDF.");
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

        String cheminPdf = null;
        try {
            File uploadDir = new File("uploads/certifications");
            if (!uploadDir.exists()) uploadDir.mkdirs();
            String newFileName = System.currentTimeMillis() + "_" + selectedPdf.getName();
            File dest = new File(uploadDir, newFileName);
            java.nio.file.Files.copy(selectedPdf.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            cheminPdf = dest.getAbsolutePath();
        } catch (IOException e) {
            statusLabel.setText("Erreur lors de l'enregistrement du PDF.");
            statusLabel.setStyle("-fx-text-fill: #ef4444;");
            return;
        }
        req.setCheminPdf(cheminPdf);

        boolean ok = CertificationDAO.insertRequest(req);
        if (ok) {
            statusLabel.setText("✓ Votre demande a été envoyée! Elle sera examinée sous 48-72h.");
            statusLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
            // Clear form
            nomField.clear();
            emailField.clear();
            motivationField.clear();
            specialiteCombo.getSelectionModel().select(0);
            selectedPdf = null;
            pdfNameLabel.setText("Aucun fichier sélectionné");
            pdfNameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af;");
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
