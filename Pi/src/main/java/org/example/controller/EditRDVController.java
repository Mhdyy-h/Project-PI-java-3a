package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.dao.RendezVousDAO;
import org.example.model.RendezVous;
import org.example.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EditRDVController {
    
    private RendezVous rendezVous;
    private Stage dialogStage;
    private User currentUser;
    private RendezVousController parentController;
    
    @FXML private Label titleLabel;
    @FXML private Label currentDateLabel;
    @FXML private Label currentTimeLabel;
    @FXML private Label currentMotifLabel;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> timeComboBox;
    @FXML private TextArea motifTextArea;
    
    @FXML
    public void initialize() {
        setupTimeOptions();
    }
    
    private void setupTimeOptions() {
        for (int hour = 8; hour < 18; hour++) {
            timeComboBox.getItems().add(String.format("%02d:00", hour));
            timeComboBox.getItems().add(String.format("%02d:30", hour));
        }
    }
    
    public void setRendezVous(RendezVous rdv) {
        this.rendezVous = rdv;
        loadCurrentInfo();
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }
    
    public void setParentController(RendezVousController controller) {
        this.parentController = controller;
    }
    
    private void loadCurrentInfo() {
        if (rendezVous != null) {
            // Display current information
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            
            currentDateLabel.setText(rendezVous.getDateHeure().format(dateFormatter));
            currentTimeLabel.setText(rendezVous.getDateHeure().format(timeFormatter));
            currentMotifLabel.setText(rendezVous.getMotif() != null ? rendezVous.getMotif() : "Non spécifié");
            
            // Pre-fill edit fields with current values
            datePicker.setValue(rendezVous.getDateHeure().toLocalDate());
            timeComboBox.setValue(rendezVous.getDateHeure().format(timeFormatter));
            motifTextArea.setText(rendezVous.getMotif() != null ? rendezVous.getMotif() : "");
        }
    }
    
    @FXML
    private void handleClose() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
    
    @FXML
    private void handleCancel() {
        handleClose();
    }
    
    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }
        
        try {
            // Update the rendez-vous with new values
            LocalDate selectedDate = datePicker.getValue();
            String selectedTime = timeComboBox.getValue();
            String newMotif = motifTextArea.getText().trim();
            
            if (selectedDate == null || selectedTime == null || newMotif.isEmpty()) {
                showAlert("Erreur", "Veuillez remplir tous les champs obligatoires.", Alert.AlertType.ERROR);
                return;
            }
            
            // Parse time and create new LocalDateTime
            String[] timeParts = selectedTime.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);
            
            LocalDateTime newDateTime = selectedDate.atTime(hour, minute);
            
            // Update the rendez-vous object
            rendezVous.setDateHeure(newDateTime);
            rendezVous.setMotif(newMotif);
            
            // Save to database
            if (RendezVousDAO.updateRendezVous(rendezVous)) {
                showAlert("Succès", "Rendez-vous modifié avec succès!", Alert.AlertType.INFORMATION);
                
                // Refresh parent controller
                if (parentController != null) {
                    parentController.loadRendezVous();
                }
                
                handleClose();
            } else {
                showAlert("Erreur", "Impossible de modifier le rendez-vous.", Alert.AlertType.ERROR);
            }
            
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la modification: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    
    private boolean validateInput() {
        if (datePicker.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner une date.", Alert.AlertType.ERROR);
            return false;
        }
        
        if (timeComboBox.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner une heure.", Alert.AlertType.ERROR);
            return false;
        }
        
        if (motifTextArea.getText().trim().isEmpty()) {
            showAlert("Erreur", "Veuillez entrer un motif.", Alert.AlertType.ERROR);
            return false;
        }
        
        return true;
    }
    
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
