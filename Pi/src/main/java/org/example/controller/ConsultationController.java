package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.dao.ConsultationDAO;
import org.example.dao.PrescriptionDAO;
import org.example.model.Consultation;
import org.example.model.Prescription;
import org.example.model.RendezVous;
import org.example.model.User;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Optional;

/**
 * Controller for Consultation Interface
 */
public class ConsultationController implements Initializable {
    
    @FXML private Label rendezVousInfoLabel;
    @FXML private Label patientNomLabel;
    @FXML private Label dateRdvLabel;
    @FXML private Label motifLabel;
    @FXML private Label statusLabel;
    
    @FXML private TextArea symptomesArea;
    @FXML private TextArea diagnosticArea;
    @FXML private TextArea recommandationsArea;
    
    @FXML private Button saveButton;
    @FXML private Button prescriptionButton;
    @FXML private Button finishButton;
    
    private Consultation consultation;
    private RendezVous rendezVous;
    private User currentUser;
    private Stage currentStage;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupButtonActions();
        clearForm();
    }
    
    public void setConsultation(Consultation consultation) {
        this.consultation = consultation;
        this.rendezVous = consultation.getRendezVous();
        loadRendezVousInfo();
        loadConsultationData();
    }
    
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
    
    public void setStage(Stage stage) {
        this.currentStage = stage;
    }
    
    // New method to set RDV directly
    public void setRendezVous(RendezVous rendezVous) {
        this.rendezVous = rendezVous;
        loadRendezVousInfo();
    }
    
    // Reference to main controller for opening prescription
    private RendezVousController rendezVousController;
    
    public void setRendezVousController(RendezVousController controller) {
        this.rendezVousController = controller;
    }
    
    private void loadRendezVousInfo() {
        if (rendezVous != null) {
            patientNomLabel.setText(rendezVous.getPatientNom());
            dateRdvLabel.setText(rendezVous.getFormattedDateHeure());
            motifLabel.setText(rendezVous.getMotif());
            
            String info = String.format("RDV #%d - %s", rendezVous.getId(), rendezVous.getFormattedDateHeure());
            rendezVousInfoLabel.setText(info);
        }
    }
    
    private void loadConsultationData() {
        if (consultation != null) {
            symptomesArea.setText(consultation.getSymptomes() != null ? consultation.getSymptomes() : "");
            diagnosticArea.setText(consultation.getDiagnostic() != null ? consultation.getDiagnostic() : "");
            recommandationsArea.setText(consultation.getRecommandations() != null ? consultation.getRecommandations() : "");
            
            // Update status based on consultation status
            updateStatusLabel();
        }
    }
    
    private void setupButtonActions() {
        saveButton.setOnAction(e -> saveConsultation());
        prescriptionButton.setOnAction(e -> openPrescriptionInterface());
        finishButton.setOnAction(e -> finishAndReturn());
    }
    
    private void saveConsultation() {
        if (!validateForm()) {
            return;
        }
        
        if (consultation == null) {
            consultation = new Consultation(rendezVous);
        }
        
        consultation.setSymptomes(symptomesArea.getText().trim());
        consultation.setDiagnostic(diagnosticArea.getText().trim());
        consultation.setRecommandations(recommandationsArea.getText().trim());
        consultation.setStatut("en_cours");
        
        if (consultation.getId() == 0) {
            // Create new consultation
            if (ConsultationDAO.createConsultation(consultation)) {
                showAlert("Succès", "Consultation sauvegardée avec succès!", Alert.AlertType.INFORMATION);
                updateStatusLabel();
                prescriptionButton.setDisable(false);
            } else {
                showAlert("Erreur", "Impossible de sauvegarder la consultation.", Alert.AlertType.ERROR);
            }
        } else {
            // Update existing consultation
            if (ConsultationDAO.updateConsultation(consultation)) {
                showAlert("Succès", "Consultation mise à jour avec succès!", Alert.AlertType.INFORMATION);
                updateStatusLabel();
            } else {
                showAlert("Erreur", "Impossible de mettre à jour la consultation.", Alert.AlertType.ERROR);
            }
        }
    }
    
    private void openPrescriptionInterface() {
        if (consultation == null || consultation.getId() == 0) {
            showAlert("Erreur", "Veuillez d'abord sauvegarder la consultation.", Alert.AlertType.ERROR);
            return;
        }
        
        try {
            // Load prescription interface
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/prescription_interface.fxml"));
            javafx.scene.Parent root = loader.load();
            
            // Get controller and pass consultation data
            PrescriptionController controller = loader.getController();
            controller.setConsultation(consultation);
            controller.setCurrentUser(currentUser);
            controller.setStage(new Stage());
            
            // Create and show stage
            Stage stage = new Stage();
            stage.setTitle("💊 Gestion des Prescriptions - " + rendezVous.getPatientNom());
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            // Refresh consultation data after closing prescription interface
            loadConsultationData();
            
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir l'interface des prescriptions: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void finishAndReturn() {
        if (consultation == null || consultation.getId() == 0) {
            showAlert("Erreur", "Veuillez d'abord sauvegarder la consultation.", Alert.AlertType.ERROR);
            return;
        }
        
        // Check if consultation has required fields
        if (!validateForm()) {
            return;
        }
        
        // Mark consultation as completed
        consultation.setStatut("terminee");
        
        if (ConsultationDAO.updateConsultation(consultation)) {
            showAlert("Succès", "Consultation terminée avec succès!\n\nRetour à l'interface principale...", Alert.AlertType.INFORMATION);
            
            // Close the consultation window
            if (currentStage != null) {
                currentStage.close();
            }
        } else {
            showAlert("Erreur", "Impossible de terminer la consultation.", Alert.AlertType.ERROR);
        }
    }
    
    private boolean validateForm() {
        String symptomes = symptomesArea.getText().trim();
        String diagnostic = diagnosticArea.getText().trim();
        
        if (symptomes.isEmpty()) {
            showAlert("Erreur", "Les symptômes sont obligatoires.", Alert.AlertType.ERROR);
            symptomesArea.requestFocus();
            return false;
        }
        
        if (diagnostic.isEmpty()) {
            showAlert("Erreur", "Le diagnostic est obligatoire.", Alert.AlertType.ERROR);
            diagnosticArea.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void updateStatusLabel() {
        if (consultation != null) {
            switch (consultation.getStatut()) {
                case "en_cours":
                    statusLabel.setText("📝 En cours");
                    statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: rgba(255,193,7,0.2); -fx-background-radius: 8; -fx-padding: 8 15 8 15;");
                    break;
                case "terminee":
                    statusLabel.setText("✅ Terminée");
                    statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: rgba(39,174,96,0.2); -fx-background-radius: 8; -fx-padding: 8 15 8 15;");
                    break;
                default:
                    statusLabel.setText("🔄 Nouvelle");
                    statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: rgba(52,152,219,0.2); -fx-background-radius: 8; -fx-padding: 8 15 8 15;");
                    break;
            }
        }
    }
    
    private void clearForm() {
        symptomesArea.clear();
        diagnosticArea.clear();
        recommandationsArea.clear();
        prescriptionButton.setDisable(true);
        statusLabel.setText("🔄 Nouvelle");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: rgba(52,152,219,0.2); -fx-background-radius: 8; -fx-padding: 8 15 8 15;");
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
