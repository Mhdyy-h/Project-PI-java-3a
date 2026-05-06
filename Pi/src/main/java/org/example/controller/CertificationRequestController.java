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
import org.example.service.ValidationService;
import org.example.service.ValidationResult;
import org.example.service.NavigationService;
import org.example.service.ThemeService;

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
    private final ValidationService validationService = ValidationService.getInstance();
    private final NavigationService navigationService = NavigationService.getInstance();
    private final ThemeService themeService = ThemeService.getInstance();

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
        clearFieldErrors();
        
        String nom = nomField.getText().trim();
        String email = emailField.getText().trim();
        String specialiteDisplay = specialiteCombo.getValue();
        String motivation = motivationField.getText().trim();

        // Validation du nom
        ValidationResult nomValid = validationService.validateName(nom, "Le nom complet");
        if (nomValid.hasError()) {
            showFieldError(nomField, nomValid.getMessage());
            return;
        }

        // Validation de l'email
        ValidationResult emailValid = validationService.validateEmail(email);
        if (emailValid.hasError()) {
            showFieldError(emailField, emailValid.getMessage());
            return;
        }

        // Validation de la spécialité
        if (specialiteDisplay == null || specialiteDisplay.isEmpty()) {
            statusLabel.setText("Veuillez sélectionner une spécialité.");
            statusLabel.setStyle("-fx-text-fill: #ef4444;");
            return;
        }

        // Validation du PDF
        if (selectedPdf == null) {
            statusLabel.setText("Veuillez choisir un document PDF justificatif.");
            statusLabel.setStyle("-fx-text-fill: #ef4444;");
            pdfNameLabel.setText("⚠ Document obligatoire");
            pdfNameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ef4444;");
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
        navigationService.navigateToLogin((Node) event.getSource());
    }

    // ==================== VALIDATION HELPERS ====================
    private void showFieldError(TextField field, String message) {
        if (field != null) {
            field.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2; -fx-border-radius: 8;");
            field.setTooltip(new Tooltip(message));
            field.requestFocus();
        }
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #ef4444;");
    }

    private void clearFieldErrors() {
        clearFieldStyle(nomField);
        clearFieldStyle(emailField);
        statusLabel.setText("");
        if (selectedPdf != null) {
            pdfNameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #10b981; -fx-font-weight: bold;");
        } else {
            pdfNameLabel.setText("Aucun fichier sélectionné");
            pdfNameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af;");
        }
    }

    private void clearFieldStyle(TextField field) {
        if (field != null) {
            if (themeService.isDarkMode()) {
                field.setStyle("-fx-background-color: #1e293b; -fx-border-color: #475569; -fx-border-radius: 8;");
            } else {
                field.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e5e7eb; -fx-border-radius: 8;");
            }
            field.setTooltip(null);
        }
    }
}
