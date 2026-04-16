package org.example.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.dao.RendezVousDAO;
import org.example.dao.SpecialisteDAO;
import org.example.dao.UserDAO;
import org.example.model.RendezVous;
import org.example.model.Specialiste;
import org.example.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RendezVousDialogController {
    
    public enum Mode { ADD, EDIT }
    
    private Mode mode;
    private RendezVous rendezVous;
    private Stage dialogStage;
    
    @FXML private ComboBox<User> patientComboBox;
    @FXML private ComboBox<Specialiste> specialisteComboBox;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> timeComboBox;
    @FXML private TextArea motifTextArea;
    @FXML private ComboBox<String> statutComboBox;
    @FXML private ComboBox<String> modeComboBox;
    @FXML private TextField lieuTextField;
    @FXML private ComboBox<Integer> urgenceComboBox;
    
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label titleLabel;
    @FXML private Label errorLabel;
    
    @FXML
    public void initialize() {
        setupTimeOptions();
        setupStatusOptions();
        setupModeOptions();
        setupUrgencyOptions();
        setupEventHandlers();
        
        // Default selections
        statutComboBox.setValue("en attente");
        modeComboBox.setValue("présentiel");
        urgenceComboBox.setValue(1);
        
        loadComboBoxData();
    }
    
    private void setupTimeOptions() {
        for (int hour = 8; hour < 18; hour++) {
            timeComboBox.getItems().add(String.format("%02d:00", hour));
            timeComboBox.getItems().add(String.format("%02d:30", hour));
        }
    }
    
    private void setupStatusOptions() {
        statutComboBox.getItems().addAll("confirmé", "en attente", "annulé", "terminé");
    }
    
    private void setupModeOptions() {
        modeComboBox.getItems().addAll("présentiel", "vidéo", "téléconsultation");
    }
    
    private void setupUrgencyOptions() {
        for (int i = 1; i <= 5; i++) {
            urgenceComboBox.getItems().add(i);
        }
    }
    
    private void setupEventHandlers() {
        modeComboBox.setOnAction(e -> handleModeChange());
        
        // Add validation listeners
        motifTextArea.textProperty().addListener((obs, oldVal, newVal) -> validateMotif());
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateDateTime());
        timeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateDateTime());
        
        // Add focus listeners for real-time validation
        motifTextArea.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) validateMotif();
        });
        
        datePicker.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) validateDateTime();
        });
        
        timeComboBox.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) validateDateTime();
        });
    }
    
    private void loadComboBoxData() {
        try {
            // Load patients (users who are not specialists)
            List<User> patients = UserDAO.getAllPatients();
            patientComboBox.setItems(FXCollections.observableArrayList(patients));
            
            // Set custom cell factory for patient display
            patientComboBox.setCellFactory(lv -> new ListCell<User>() {
                @Override
                protected void updateItem(User item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getNomComplet());
                }
            });
            
            patientComboBox.setButtonCell(new ListCell<User>() {
                @Override
                protected void updateItem(User item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getNomComplet());
                }
            });
            
            // Load specialists
            List<Specialiste> specialistes = SpecialisteDAO.getAllSpecialistes();
            specialisteComboBox.setItems(FXCollections.observableArrayList(specialistes));
            
            // Set custom cell factory for specialist display
            specialisteComboBox.setCellFactory(lv -> new ListCell<Specialiste>() {
                @Override
                protected void updateItem(Specialiste item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getDisplayName());
                }
            });
            
            specialisteComboBox.setButtonCell(new ListCell<Specialiste>() {
                @Override
                protected void updateItem(Specialiste item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getDisplayName());
                }
            });
            
        } catch (Exception e) {
            showError("Erreur de chargement", "Impossible de charger les données: " + e.getMessage());
        }
    }
    
    public void setMode(Mode mode) {
        this.mode = mode;
        
        if (mode == Mode.ADD) {
            titleLabel.setText("Nouveau Rendez-vous");
        } else {
            titleLabel.setText("Modifier Rendez-vous");
        }
    }
    
    public void setRendezVous(RendezVous rendezVous) {
        this.rendezVous = rendezVous;
        
        if (rendezVous != null) {
            // Load data into form
            User patient = UserDAO.getUserById(rendezVous.getPatientId());
            if (patient != null) {
                patientComboBox.setValue(patient);
            }
            
            Specialiste specialiste = SpecialisteDAO.getSpecialisteById(rendezVous.getSpecialisteId());
            if (specialiste != null) {
                specialisteComboBox.setValue(specialiste);
            }
            
            // Set date and time
            LocalDateTime dateTime = rendezVous.getDateHeure();
            datePicker.setValue(dateTime.toLocalDate());
            timeComboBox.setValue(dateTime.format(DateTimeFormatter.ofPattern("HH:mm")));
            
            motifTextArea.setText(rendezVous.getMotif());
            statutComboBox.setValue(rendezVous.getStatut());
            modeComboBox.setValue(rendezVous.getMode());
            lieuTextField.setText(rendezVous.getLieu());
            
            if (rendezVous.getNiveauUrgence() != null) {
                urgenceComboBox.setValue(rendezVous.getNiveauUrgence());
            }
            
            handleModeChange();
        }
    }
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    @FXML
    private void handleModeChange() {
        String selectedMode = modeComboBox.getValue();
        boolean isPresentiel = "présentiel".equals(selectedMode);
        
        lieuTextField.setDisable(!isPresentiel);
        if (!isPresentiel) {
            lieuTextField.clear();
        }
    }
    
    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }
        
        try {
            // Create or update rendez-vous
            if (mode == Mode.ADD) {
                rendezVous = new RendezVous();
            }
            
            // Set data
            rendezVous.setPatientId(patientComboBox.getValue().getId());
            rendezVous.setSpecialisteId(specialisteComboBox.getValue().getId());
            rendezVous.setDateHeure(createDateTimeFromFields());
            rendezVous.setMotif(motifTextArea.getText().trim());
            rendezVous.setStatut(statutComboBox.getValue());
            rendezVous.setMode(modeComboBox.getValue());
            rendezVous.setLieu("présentiel".equals(modeComboBox.getValue()) ? lieuTextField.getText().trim() : null);
            rendezVous.setNiveauUrgence(urgenceComboBox.getValue());
            
            // Check for conflicts
            if (mode == Mode.ADD) {
                if (RendezVousDAO.hasConflict(rendezVous.getSpecialisteId(), rendezVous.getDateHeure(), null)) {
                    showError("Conflit d'horaire", "Ce créneau horaire est déjà réservé pour ce spécialiste");
                    return;
                }
                
                if (RendezVousDAO.createRendezVous(rendezVous)) {
                    showInfo("Succès", "Rendez-vous créé avec succès");
                    dialogStage.close();
                } else {
                    showError("Erreur", "Impossible de créer le rendez-vous");
                }
            } else {
                if (RendezVousDAO.hasConflict(rendezVous.getSpecialisteId(), rendezVous.getDateHeure(), rendezVous.getId())) {
                    showError("Conflit d'horaire", "Ce créneau horaire est déjà réservé pour ce spécialiste");
                    return;
                }
                
                if (RendezVousDAO.updateRendezVous(rendezVous)) {
                    showInfo("Succès", "Rendez-vous modifié avec succès");
                    dialogStage.close();
                } else {
                    showError("Erreur", "Impossible de modifier le rendez-vous");
                }
            }
            
        } catch (Exception e) {
            showError("Erreur", "Impossible de sauvegarder le rendez-vous: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
    
    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();
        
        // Patient validation
        if (patientComboBox.getValue() == null) {
            errors.append("- Le patient est obligatoire\n");
        }
        
        // Specialist validation
        if (specialisteComboBox.getValue() == null) {
            errors.append("- Le spécialiste est obligatoire\n");
        }
        
        // Date validation
        if (datePicker.getValue() == null) {
            errors.append("- La date est obligatoire\n");
        }
        
        // Time validation
        if (timeComboBox.getValue() == null) {
            errors.append("- L'heure est obligatoire\n");
        }
        
        // Motif validation
        String motif = motifTextArea.getText().trim();
        if (motif.isEmpty()) {
            errors.append("- Le motif est obligatoire\n");
        } else if (motif.length() < 3) {
            errors.append("- Le motif doit contenir au moins 3 caractères\n");
        } else if (motif.length() > 500) {
            errors.append("- Le motif ne peut pas dépasser 500 caractères\n");
        }
        
        // Status validation
        if (statutComboBox.getValue() == null) {
            errors.append("- Le statut est obligatoire\n");
        }
        
        // Mode validation
        if (modeComboBox.getValue() == null) {
            errors.append("- Le mode est obligatoire\n");
        }
        
        // Lieu validation for presentiel
        if ("présentiel".equals(modeComboBox.getValue())) {
            String lieu = lieuTextField.getText().trim();
            if (lieu.isEmpty()) {
                errors.append("- Le lieu est obligatoire pour une consultation en présentiel\n");
            } else if (lieu.length() > 200) {
                errors.append("- Le lieu ne peut pas dépasser 200 caractères\n");
            }
        }
        
        // Date/time validation
        if (datePicker.getValue() != null && timeComboBox.getValue() != null) {
            LocalDateTime dateTime = createDateTimeFromFields();
            LocalDateTime now = LocalDateTime.now();
            
            if (dateTime.isBefore(now.minusHours(1))) {
                errors.append("- La date du rendez-vous ne peut pas être dans le passé\n");
            }
            
            if (dateTime.isAfter(now.plusYears(1))) {
                errors.append("- La date du rendez-vous ne peut pas dépasser un an\n");
            }
            
            int hour = dateTime.getHour();
            if (hour < 8 || hour >= 18) {
                errors.append("- Les rendez-vous doivent être programmés entre 8h et 18h\n");
            }
        }
        
        if (errors.length() > 0) {
            showError("Erreurs de validation", errors.toString());
            return false;
        }
        
        return true;
    }
    
    private void validateMotif() {
        String motif = motifTextArea.getText().trim();
        if (motif.length() > 0 && motif.length() < 3) {
            errorLabel.setText("Le motif doit contenir au moins 3 caractères");
            motifTextArea.setStyle("-fx-border-color: red;");
        } else if (motif.length() > 500) {
            errorLabel.setText("Le motif ne peut pas dépasser 500 caractères");
            motifTextArea.setStyle("-fx-border-color: red;");
        } else {
            errorLabel.setText("");
            motifTextArea.setStyle("");
        }
    }
    
    private void validateDateTime() {
        if (datePicker.getValue() != null && timeComboBox.getValue() != null) {
            LocalDateTime dateTime = createDateTimeFromFields();
            LocalDateTime now = LocalDateTime.now();
            
            if (dateTime.isBefore(now.minusHours(1))) {
                errorLabel.setText("La date du rendez-vous ne peut pas être dans le passé");
                datePicker.setStyle("-fx-border-color: red;");
                timeComboBox.setStyle("-fx-border-color: red;");
            } else if (dateTime.isAfter(now.plusYears(1))) {
                errorLabel.setText("La date du rendez-vous ne peut pas dépasser un an");
                datePicker.setStyle("-fx-border-color: red;");
                timeComboBox.setStyle("-fx-border-color: red;");
            } else {
                int hour = dateTime.getHour();
                if (hour < 8 || hour >= 18) {
                    errorLabel.setText("Les rendez-vous doivent être programmés entre 8h et 18h");
                    datePicker.setStyle("-fx-border-color: red;");
                    timeComboBox.setStyle("-fx-border-color: red;");
                } else {
                    errorLabel.setText("");
                    datePicker.setStyle("");
                    timeComboBox.setStyle("");
                }
            }
        }
    }
    
    private LocalDateTime createDateTimeFromFields() {
        if (datePicker.getValue() == null || timeComboBox.getValue() == null) {
            return null;
        }
        
        String[] timeParts = timeComboBox.getValue().split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);
        
        return datePicker.getValue().atTime(hour, minute);
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
