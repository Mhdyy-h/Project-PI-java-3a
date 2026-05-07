package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.dao.ConsultationDAO;
import org.example.dao.PrescriptionDAO;
import org.example.model.Consultation;
import org.example.model.Prescription;
import org.example.model.RendezVous;
import org.example.model.User;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for Prescription Management Interface
 */
public class PrescriptionController implements Initializable {
    
    @FXML private TableView<Prescription> prescriptionsTable;
    @FXML private TableColumn<Prescription, String> medicamentColumn;
    @FXML private TableColumn<Prescription, String> doseColumn;
    @FXML private TableColumn<Prescription, String> frequenceColumn;
    @FXML private TableColumn<Prescription, Integer> dureeColumn;
    @FXML private TableColumn<Prescription, String> instructionsColumn;
    @FXML private TableColumn<Prescription, String> dateColumn;
    @FXML private TableColumn<Prescription, Void> actionsColumn;
    
    @FXML private TextField medicamentField;
    @FXML private TextField doseField;
    @FXML private TextField frequenceField;
    @FXML private TextField dureeField;
    @FXML private TextArea instructionsArea;
    @FXML private Button addButton;
    @FXML private Button saveButton;
    @FXML private Button finishButton;
    @FXML private Label consultationInfoLabel;
    @FXML private Label statusLabel;
    @FXML private Label countLabel;
    
    private Consultation consultation;
    private User currentUser;
    private ObservableList<Prescription> prescriptionsList = FXCollections.observableArrayList();
    private Stage currentStage;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupButtonActions();
        loadPrescriptions();
        clearForm();
    }
    
    public void setConsultation(Consultation consultation) {
        this.consultation = consultation;
        updateConsultationInfo();
        loadPrescriptions();
    }
    
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
    
    public void setStage(Stage stage) {
        this.currentStage = stage;
    }
    
    // New method to set RDV directly (when no consultation yet)
    private RendezVous rendezVous;
    
    public void setRendezVous(RendezVous rendezVous) {
        this.rendezVous = rendezVous;
        updateRDVInfo();
    }
    
    private void updateRDVInfo() {
        if (rendezVous != null) {
            String info = String.format("📋 RDV #%d | Patient: %s | Date: %s | Motif: %s",
                rendezVous.getId(),
                rendezVous.getPatientNom(),
                rendezVous.getFormattedDateHeure(),
                rendezVous.getMotif());
            consultationInfoLabel.setText(info);
        }
    }
    
    // Reference to main controller
    private RendezVousController rendezVousController;
    
    public void setRendezVousController(RendezVousController controller) {
        this.rendezVousController = controller;
    }
    
    private void updateConsultationInfo() {
        if (consultation != null && consultation.getRendezVous() != null) {
            String info = String.format("📋 Consultation: %s | Patient: %s | Date: %s",
                consultation.getId(),
                consultation.getRendezVous().getPatientNom(),
                consultation.getRendezVous().getFormattedDateHeure()
            );
            consultationInfoLabel.setText(info);
        }
    }
    
    private void setupTableColumns() {
        medicamentColumn.setCellValueFactory(new PropertyValueFactory<>("nomMedicament"));
        doseColumn.setCellValueFactory(new PropertyValueFactory<>("dose"));
        frequenceColumn.setCellValueFactory(new PropertyValueFactory<>("frequence"));
        dureeColumn.setCellValueFactory(new PropertyValueFactory<>("duree"));
        instructionsColumn.setCellValueFactory(new PropertyValueFactory<>("instructions"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        
        // Format date column
        dateColumn.setCellFactory(column -> new TableCell<Prescription, String>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(LocalDateTime.parse(item).format(formatter));
                }
            }
        });
        
        // Add actions column
        addActionsColumn();
        
        prescriptionsTable.setItems(prescriptionsList);
    }
    
    private void addActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<Prescription, Void>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox buttonsBox = new HBox(5, editBtn, deleteBtn);
            
            {
                // Style buttons
                editBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 8 4 8;");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 8 4 8;");
                
                // Button actions
                editBtn.setOnAction(e -> {
                    Prescription prescription = getTableView().getItems().get(getIndex());
                    editPrescription(prescription);
                });
                
                deleteBtn.setOnAction(e -> {
                    Prescription prescription = getTableView().getItems().get(getIndex());
                    deletePrescription(prescription);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonsBox);
                }
            }
        });
    }
    
    private void setupButtonActions() {
        addButton.setOnAction(e -> addPrescription());
        saveButton.setOnAction(e -> saveCurrentPrescription());
        finishButton.setOnAction(e -> finishAndReturn());
    }
    
    private void loadPrescriptions() {
        if (consultation != null) {
            prescriptionsList.clear();
            prescriptionsList.addAll(PrescriptionDAO.getPrescriptionsByConsultationId(consultation.getId()));
            updateCountLabel();
        }
    }
    
    private void addPrescription() {
        if (!validateForm()) {
            return;
        }
        
        Prescription prescription = new Prescription();
        prescription.setNomMedicament(medicamentField.getText().trim());
        prescription.setDose(doseField.getText().trim());
        prescription.setFrequence(frequenceField.getText().trim());
        prescription.setDuree(Integer.parseInt(dureeField.getText().trim()));
        prescription.setInstructions(instructionsArea.getText().trim());
        prescription.setConsultation(consultation);
        
        if (PrescriptionDAO.createPrescription(prescription)) {
            prescriptionsList.add(0, prescription);
            clearForm();
            statusLabel.setText("💊 Prescription ajoutée avec succès!");
            updateCountLabel();
        } else {
            showAlert("Erreur", "Impossible d'ajouter la prescription.", Alert.AlertType.ERROR);
        }
    }
    
    private void saveCurrentPrescription() {
        // This would be used if we implement editing
        showAlert("Info", "Fonction de modification à implémenter.", Alert.AlertType.INFORMATION);
    }
    
    private void editPrescription(Prescription prescription) {
        // Load prescription data into form for editing
        medicamentField.setText(prescription.getNomMedicament());
        doseField.setText(prescription.getDose());
        frequenceField.setText(prescription.getFrequence());
        dureeField.setText(String.valueOf(prescription.getDuree()));
        instructionsArea.setText(prescription.getInstructions());
        
        statusLabel.setText("✏️ Modification de la prescription en cours...");
    }
    
    private void deletePrescription(Prescription prescription) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Supprimer Prescription");
        alert.setHeaderText("Voulez-vous supprimer cette prescription?");
        alert.setContentText("Médicament: " + prescription.getNomMedicament());
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (PrescriptionDAO.deletePrescription(prescription.getId())) {
                prescriptionsList.remove(prescription);
                statusLabel.setText("🗑️ Prescription supprimée avec succès!");
                updateCountLabel();
            } else {
                showAlert("Erreur", "Impossible de supprimer la prescription.", Alert.AlertType.ERROR);
            }
        }
    }
    
    private void finishAndReturn() {
        // Mark consultation as fully completed
        if (consultation != null) {
            consultation.setStatut("completee");
            if (ConsultationDAO.updateConsultation(consultation)) {
                showAlert("Succès", "Consultation et prescriptions terminées avec succès!\n\nRetour à l'interface principale...", Alert.AlertType.INFORMATION);
                
                // Close the prescription interface window
                if (currentStage != null) {
                    currentStage.close();
                }
            } else {
                showAlert("Erreur", "Impossible de finaliser la consultation.", Alert.AlertType.ERROR);
            }
        }
    }
    
    private boolean validateForm() {
        String medicament = medicamentField.getText().trim();
        String dose = doseField.getText().trim();
        String frequence = frequenceField.getText().trim();
        String dureeStr = dureeField.getText().trim();
        
        if (medicament.isEmpty()) {
            showAlert("Erreur", "Le nom du médicament est obligatoire.", Alert.AlertType.ERROR);
            medicamentField.requestFocus();
            return false;
        }
        
        if (dose.isEmpty()) {
            showAlert("Erreur", "La dose est obligatoire.", Alert.AlertType.ERROR);
            doseField.requestFocus();
            return false;
        }
        
        if (frequence.isEmpty()) {
            showAlert("Erreur", "La fréquence est obligatoire.", Alert.AlertType.ERROR);
            frequenceField.requestFocus();
            return false;
        }
        
        if (dureeStr.isEmpty()) {
            showAlert("Erreur", "La durée est obligatoire.", Alert.AlertType.ERROR);
            dureeField.requestFocus();
            return false;
        }
        
        try {
            int duree = Integer.parseInt(dureeStr);
            if (duree <= 0) {
                showAlert("Erreur", "La durée doit être un nombre positif.", Alert.AlertType.ERROR);
                dureeField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "La durée doit être un nombre valide.", Alert.AlertType.ERROR);
            dureeField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void clearForm() {
        medicamentField.clear();
        doseField.clear();
        frequenceField.clear();
        dureeField.clear();
        instructionsArea.clear();
        medicamentField.requestFocus();
    }
    
    private void updateCountLabel() {
        countLabel.setText("Total: " + prescriptionsList.size() + " prescription(s)");
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
