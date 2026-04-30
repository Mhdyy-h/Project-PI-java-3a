package org.example.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Dialog;
import javafx.util.Callback;
import org.example.dao.RendezVousDAO;
import org.example.dao.SpecialisteDAO;
import org.example.dao.UserDAO;
import org.example.model.RendezVous;
import org.example.model.Specialiste;
import org.example.model.User;
import org.example.service.IntelligentScheduler;
import org.example.service.AbsencePredictor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class RendezVousDialogController {
    
    public enum Mode { ADD, EDIT }
    
    private Mode mode;
    private RendezVous rendezVous;
    private Stage dialogStage;
    private User currentUser;
    
    @FXML private ComboBox<User> patientComboBox;
    @FXML private ComboBox<Specialiste> specialisteComboBox;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> timeComboBox;
    @FXML private TextArea motifTextArea;
    @FXML private ComboBox<String> modeComboBox;
    @FXML private TextField lieuTextField;
    @FXML private VBox urgencyDisplaySection;
    @FXML private Label urgencyLevelLabel;
    @FXML private Label urgencyReasonLabel;
    
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button suggestButton;
    @FXML private Label titleLabel;
    @FXML private Label errorLabel;
    @FXML private Label currentDateLabel;
    @FXML private Label currentTimeLabel;
    @FXML private Label currentPatientLabel;
    @FXML private Label currentSpecialisteLabel;
    @FXML private Label readOnlyPatientLabel;
    @FXML private Label readOnlySpecialisteLabel;
    @FXML private VBox currentInfoSection;
    @FXML private VBox locationSection;
    @FXML private VBox patientSection;
    
    @FXML
    public void initialize() {
        // Set French locale for date picker
        Locale.setDefault(Locale.FRENCH);
        
        setupTimeOptions();
        setupModeOptions();
        setupEventHandlers();
        
        // Default selections
        modeComboBox.setValue("présentiel");
        
        // Setup urgency calculation
        setupUrgencyCalculation();
        
        // Trigger mode change handler to set initial state
        handleModeChange();
        
        // Will be called after currentUser is set
    }
    
    private void setupForUserRole() {
        if (currentUser != null) {
            if (currentUser.isPatient()) {
                // Hide patient section for patients
                patientSection.setVisible(false);
                patientSection.setManaged(false);
                // Auto-select current patient
                patientComboBox.setValue(currentUser);
            }
            // Admin and others see all fields
            
            // Load combo box data now that we have the user
            loadComboBoxData();
        }
    }
    
    private void setupTimeOptions() {
        for (int hour = 8; hour < 18; hour++) {
            timeComboBox.getItems().add(String.format("%02d:00", hour));
            timeComboBox.getItems().add(String.format("%02d:30", hour));
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
    
    private void setupModeOptions() {
        modeComboBox.getItems().addAll("présentiel", "téléconsultation");
    }
    
    private void setupUrgencyCalculation() {
        // Add listener to motif text area for real-time urgency calculation
        motifTextArea.textProperty().addListener((obs, oldText, newText) -> {
            calculateUrgency(newText);
        });
        
        // Initialize with default urgency
        calculateUrgency("");
    }
    
    private void calculateUrgency(String motif) {
        int urgencyLevel = 1; // Default: Faible
        String urgencyText = "Faible";
        String urgencyColor = "#22c55e"; // Green
        String reason = "Consultation routine";
        
        if (motif == null || motif.trim().isEmpty()) {
            urgencyText = "Faible";
            urgencyColor = "#22c55e";
            reason = "Consultation routine";
        } else {
            String lowerMotif = motif.toLowerCase();
            
            // High urgency keywords
            if (containsAny(lowerMotif, "urgence", "urgent", "douleur forte", "accident", "saignement", "difficulté respirer", "poitrine", "malaise")) {
                urgencyLevel = 5;
                urgencyText = "Urgence";
                urgencyColor = "#ef4444"; // Red
                reason = "Symptômes d'urgence détectés";
            }
            // Medium-high urgency keywords
            else if (containsAny(lowerMotif, "fièvre", "vomissement", "diarrhée", "infection", "blessure", "chute", "brûlure")) {
                urgencyLevel = 4;
                urgencyText = "Élevée";
                urgencyColor = "#f97316"; // Orange
                reason = "Symptômes modérés à sévères";
            }
            // Medium urgency keywords
            else if (containsAny(lowerMotif, "mal", "douleur", "fatigue", "perte", "gonflement", "rougeur")) {
                urgencyLevel = 3;
                urgencyText = "Moyenne";
                urgencyColor = "#eab308"; // Yellow
                reason = "Symptômes modérés";
            }
            // Low-medium urgency keywords
            else if (containsAny(lowerMotif, "contrôle", "suivi", "vaccin", "ordonnance", "renouvellement")) {
                urgencyLevel = 2;
                urgencyText = "Faible";
                urgencyColor = "#22c55e"; // Green
                reason = "Consultation de suivi";
            }
        }
        
        // Update UI
        urgencyLevelLabel.setText(urgencyText + " (Niveau " + urgencyLevel + ")");
        urgencyLevelLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + urgencyColor + ";");
        urgencyReasonLabel.setText(reason);
        
        // Store the calculated urgency for later use
        calculatedUrgency = urgencyLevel;
    }
    
    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    private int calculatedUrgency = 1;
    
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
            // Load patients based on user role
            List<User> patients = UserDAO.getAccessiblePatients(currentUser);
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
            
            // Load specialists based on user role
            List<Specialiste> specialistes = loadSpecialistesForUser();
            specialisteComboBox.setItems(FXCollections.observableArrayList(specialistes));
            
            // Set custom cell factory for specialist display
            specialisteComboBox.setCellFactory(lv -> new ListCell<Specialiste>() {
                @Override
                protected void updateItem(Specialiste item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getNomDocteur());
                }
            });
            
            specialisteComboBox.setButtonCell(new ListCell<Specialiste>() {
                @Override
                protected void updateItem(Specialiste item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getNomDocteur());
                }
            });
            
            // Auto-select for single options
            if (patients.size() == 1) {
                patientComboBox.setValue(patients.get(0));
            }
            if (specialistes.size() == 1) {
                specialisteComboBox.setValue(specialistes.get(0));
            }
            
        } catch (Exception e) {
            showError("Erreur de chargement", "Impossible de charger les données: " + e.getMessage());
        }
    }
    
    private List<Specialiste> loadSpecialistesForUser() {
        if (currentUser == null) {
            return SpecialisteDAO.getAllSpecialistes();
        }
        
        // Admin can see all specialists
        if (currentUser.isAdmin()) {
            return SpecialisteDAO.getAllSpecialistes();
        }
        
        // Specialist can only see themselves
        if (currentUser.isSpecialiste()) {
            Specialiste self = SpecialisteDAO.getSpecialisteById(currentUser.getId());
            if (self != null) {
                List<Specialiste> selfList = FXCollections.observableArrayList();
                selfList.add(self);
                return selfList;
            }
        }
        
        // Patient can see all specialists (for booking)
        if (currentUser.isPatient()) {
            return SpecialisteDAO.getAllSpecialistes();
        }
        
        // Default: all specialists
        return SpecialisteDAO.getAllSpecialistes();
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
            // Show current appointment info
            showCurrentAppointmentInfo(rendezVous);
            
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
            modeComboBox.setValue(rendezVous.getMode());
            lieuTextField.setText(rendezVous.getLieu());
            
            // Calculate urgency from existing motif
            calculateUrgency(rendezVous.getMotif());
            
            handleModeChange();
        }
    }
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        setupForUserRole();
    }
    
    @FXML
    private void handleModeChange() {
        String selectedMode = modeComboBox.getValue();
        boolean isPresentiel = "présentiel".equals(selectedMode);
        
        System.out.println("Mode changed to: " + selectedMode + ", isPresentiel: " + isPresentiel);
        
        // Show/hide the entire location section
        if (locationSection != null) {
            locationSection.setVisible(isPresentiel);
            locationSection.setManaged(isPresentiel);
            System.out.println("Location section visibility set to: " + isPresentiel);
        }
        
        // Also disable/enable the text field
        if (lieuTextField != null) {
            lieuTextField.setDisable(!isPresentiel);
            if (!isPresentiel) {
                lieuTextField.clear();
                lieuTextField.setPromptText("Non applicable pour rendez-vous à distance");
            } else {
                lieuTextField.setPromptText("Entrez le lieu du rendez-vous");
            }
            System.out.println("Lieu field disabled: " + !isPresentiel);
        }
    }
    
    @FXML
    private void handleSuggest() {
        User selectedPatient = patientComboBox.getValue();
        Specialiste selectedSpecialist = specialisteComboBox.getValue();
        String motif = motifTextArea.getText().trim();
        
        if (selectedPatient == null || selectedSpecialist == null || motif.isEmpty()) {
            showError("Information requise", "Veuillez sélectionner un patient, un spécialiste et entrer un motif avant de demander des suggestions.");
            return;
        }
        
        try {
            // Obtenir les suggestions intelligentes
            List<IntelligentScheduler.TimeSlot> suggestions = IntelligentScheduler.suggestOptimalSlots(
                selectedPatient, 
                motif, 
                selectedSpecialist.getSpecialite()
            );
            
            if (suggestions.isEmpty()) {
                showInfo("Aucune suggestion", "Aucun créneau disponible n'a été trouvé pour les prochains jours.");
                return;
            }
            
            // Afficher les suggestions dans une boîte de dialogue
            showSuggestionsDialog(suggestions);
            
        } catch (Exception e) {
            showError("Erreur", "Impossible de générer des suggestions: " + e.getMessage());
        }
    }
    
    private void showSuggestionsDialog(List<IntelligentScheduler.TimeSlot> suggestions) {
        // Create choice dialog for clickable selection
        ChoiceDialog<String> dialog = new ChoiceDialog<>();
        dialog.setTitle("Suggestions intelligentes");
        dialog.setHeaderText("Sélectionnez un créneau suggéré:");
        
        // Create choice items with scores
        java.util.List<String> choices = new java.util.ArrayList<>();
        for (int i = 0; i < suggestions.size(); i++) {
            IntelligentScheduler.TimeSlot slot = suggestions.get(i);
            String choiceText = String.format("%d. %s (Score: %.0f)", 
                i + 1, 
                slot.getDateTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + 
                " - " + slot.getSpecialist().getNomDocteur(),
                slot.getScore()
            );
            choices.add(choiceText);
        }
        
        dialog.getItems().addAll(choices);
        if (!choices.isEmpty()) {
            dialog.setSelectedItem(choices.get(0));
        }
        
        // Show dialog and handle selection
        java.util.Optional<String> result = dialog.showAndWait();
        result.ifPresent(selected -> {
            // Extract index from selection (first number before the dot)
            String indexStr = selected.substring(0, selected.indexOf('.'));
            int index = Integer.parseInt(indexStr.trim()) - 1;
            
            if (index >= 0 && index < suggestions.size()) {
                applySuggestion(suggestions.get(index));
                showInfo("Suggestion appliquée", 
                    "✅ Créneau sélectionné: " + suggestions.get(index).getDisplayText());
            }
        });
    }
    
    private void applySuggestion(IntelligentScheduler.TimeSlot slot) {
        // Set the date
        datePicker.setValue(slot.getDateTime().toLocalDate());
        
        // Set the time
        String timeString = slot.getDateTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        if (!timeComboBox.getItems().contains(timeString)) {
            timeComboBox.getItems().add(timeString);
        }
        timeComboBox.setValue(timeString);
        
        // Show confirmation
        showInfo("Suggestion appliquée", "Créneau sélectionné: " + slot.getDisplayText());
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
            rendezVous.setStatut("en attente"); // Auto-set to en attente
            rendezVous.setMode(modeComboBox.getValue());
            rendezVous.setLieu("présentiel".equals(modeComboBox.getValue()) ? lieuTextField.getText().trim() : null);
            rendezVous.setNiveauUrgence(calculatedUrgency);
            
            // Check for conflicts
            if (mode == Mode.ADD) {
                if (RendezVousDAO.hasConflict(rendezVous.getSpecialisteId(), rendezVous.getDateHeure(), null)) {
                    showError("Conflit d'horaire", "Ce créneau horaire est déjà réservé pour ce spécialiste");
                    return;
                }
                
                try {
                    if (RendezVousDAO.createRendezVous(rendezVous)) {
                        // RDV créé avec succès - analyser le risque
                        try {
                            User patient = patientComboBox.getValue();
                            double riskScore = AbsencePredictor.predictAbsence(patient, rendezVous);
                            
                            String successMessage = "✅ Rendez-vous créé avec succès!\n\n";
                            successMessage += "📊 Analyse du risque d'absence: ";
                            
                            if (riskScore >= 0.7) {
                                successMessage += "ÉLEVÉ (" + String.format("%.0f", riskScore * 100) + "%)\n";
                                successMessage += "📱 Rappel SMS sera envoyé 48h avant";
                            } else if (riskScore >= 0.4) {
                                successMessage += "MOYEN (" + String.format("%.0f", riskScore * 100) + "%)\n";
                                successMessage += "📧 Rappel email sera envoyé 24h avant";
                            } else {
                                successMessage += "FAIBLE (" + String.format("%.0f", riskScore * 100) + "%)\n";
                                successMessage += "✅ Patient considéré comme fiable";
                            }
                            
                            showInfo("Succès intelligent", successMessage);
                        } catch (Exception e) {
                            showInfo("Succès", "✅ Rendez-vous créé avec succès!");
                        }
                        
                        dialogStage.close();
                        return; // Sortir sans erreur
                    }
                } catch (Exception e) {
                    // Si création échoue, montrer l'erreur
                    showError("Erreur", "Impossible de créer le rendez-vous: " + e.getMessage());
                    return;
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
    
    private void showCurrentAppointmentInfo(RendezVous rdv) {
        if (rdv != null) {
            currentInfoSection.setVisible(true);
            currentDateLabel.setText(rdv.getFormattedDateHeure());
            currentTimeLabel.setText(rdv.getDateHeure().format(DateTimeFormatter.ofPattern("HH:mm")));
            readOnlyPatientLabel.setText(rdv.getPatientNom());
            readOnlySpecialisteLabel.setText(rdv.getSpecialisteNom());
        } else {
            currentInfoSection.setVisible(false);
        }
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
