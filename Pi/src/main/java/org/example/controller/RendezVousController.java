package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.Modality;
import org.example.dao.RendezVousDAO;
import org.example.dao.SpecialisteDAO;
import org.example.dao.UserDAO;
import org.example.model.RendezVous;
import org.example.model.Specialiste;
import org.example.model.User;
import org.example.model.Notification;
import org.example.controller.ConsultationController;
import org.example.controller.PrescriptionController;
import org.example.service.NavigationService;

import java.io.IOException;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

public class RendezVousController {
    
    @FXML private TableView<RendezVous> rendezVousTable;
    @FXML private TableColumn<RendezVous, String> dateColumn;
    @FXML private TableColumn<RendezVous, String> motifColumn;
    @FXML private TableColumn<RendezVous, String> statutColumn;
    @FXML private TableColumn<RendezVous, String> modeColumn;
    @FXML private TableColumn<RendezVous, String> patientColumn;
    @FXML private TableColumn<RendezVous, String> specialisteColumn;
    @FXML private TableColumn<RendezVous, String> lieuColumn;
    @FXML private TableColumn<RendezVous, Integer> urgenceColumn;
    
    @FXML private Button addButton;
    @FXML private Button confirmButton;
    @FXML private Button cancelButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;
    @FXML private Button completeConsultationButton;
    @FXML private Button exportPdfButton;
    @FXML private Button newRdvButton;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statutFilter;
    @FXML private ComboBox<Specialiste> specialisteFilter;
    @FXML private Button searchButton;
    @FXML private Button clearButton;
    @FXML private Label countLabel;
    
        @FXML private Label selectedRdvLabel;
    @FXML private Label todayCountLabel;
    @FXML private Label weekCountLabel;
    @FXML private Label pendingCountLabel;
    @FXML private Label confirmedCountLabel;
    
    @FXML private Label statusLabel;
    
    // Notification components
    @FXML private VBox notificationsContainer;
    @FXML private ScrollPane notificationsScrollPane;
    @FXML private Button clearNotificationsButton;
    
    private User currentUser;
    private ObservableList<RendezVous> rendezVousList = FXCollections.observableArrayList();
    private ObservableList<User> patientsList = FXCollections.observableArrayList();
    private ObservableList<Specialiste> specialistesList = FXCollections.observableArrayList();
    private ObservableList<Notification> notificationsList = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        setupButtonActions();
        loadRendezVous();
        setupTableSelection();
        setupButtonVisibility();
        setupNotifications();
        
        // Add Actions column after initial setup
        addActionsColumn();
    }
    
    private void setupTableSelection() {
        rendezVousTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateSelectedRdvLabel(newSelection);
            updateButtonStates(newSelection);
        });
    }
    
    private void updateSelectedRdvLabel(RendezVous selected) {
        if (selected != null) {
            selectedRdvLabel.setText("Sélectionné: " + selected.getMotif() + " - " + selected.getFormattedDateHeure());
        } else {
            selectedRdvLabel.setText("Aucun rendez-vous sélectionné");
        }
    }
    
    private void updateButtonStates(RendezVous selected) {
        boolean hasSelection = selected != null;
        
        // Only allow confirm/cancel if current user is the assigned specialiste or admin
        boolean canManageRdv = false;
        if (hasSelection && currentUser != null) {
            if (currentUser.isAdmin()) {
                // Admin can manage all RDVs
                canManageRdv = true;
            } else if (currentUser.isSpecialiste() && 
                       selected != null && selected.getSpecialisteId() == currentUser.getId()) {
                // Specialiste (including hybrid) can only manage RDVs assigned to them
                canManageRdv = true;
            }
        }
        
        // PATIENT permissions - can edit/delete their own RDVs
        System.out.println("🔍 DEBUG: isPatient(): " + currentUser.isPatient());
        if (currentUser.isPatient()) {
            if (selected != null) {
                boolean isOwnRDV = selected.getPatientId() == currentUser.getId();
                boolean isConfirmed = "confirmé".equalsIgnoreCase(selected.getStatut());
                boolean canModify = isOwnRDV && !isConfirmed && !"annulé".equalsIgnoreCase(selected.getStatut());
                
                editButton.setDisable(!canModify);
                deleteButton.setDisable(!isOwnRDV); // Can delete even if confirmed
                
                editButton.setVisible(true);
                editButton.setManaged(true);
                deleteButton.setVisible(true);
                deleteButton.setManaged(true);
                
                // Patients cannot see confirm/cancel buttons
                confirmButton.setVisible(false);
                confirmButton.setManaged(false);
                cancelButton.setVisible(false);
                cancelButton.setManaged(false);
            } else {
                // No selection - disable all buttons
                editButton.setDisable(true);
                deleteButton.setDisable(true);
                confirmButton.setDisable(true);
                cancelButton.setDisable(true);
            }
        }
        
        // SPECIALIST permissions - can confirm/cancel their own pending RDVs
        else if (currentUser.isSpecialiste()) {
            if (selected != null) {
                boolean isOwnRDV = selected.getSpecialisteId() == currentUser.getId();
                boolean isConfirmed = "confirmé".equalsIgnoreCase(selected.getStatut());
                boolean canConfirmCancel = isOwnRDV && !isConfirmed && !"annulé".equalsIgnoreCase(selected.getStatut());
                
                confirmButton.setDisable(!canConfirmCancel);
                cancelButton.setDisable(!canConfirmCancel);
            } else {
                confirmButton.setDisable(true);
                cancelButton.setDisable(true);
            }
            
            confirmButton.setVisible(true);
            confirmButton.setManaged(true);
            cancelButton.setVisible(true);
            cancelButton.setManaged(true);
            
            // Specialists cannot edit/delete patient RDVs
            editButton.setVisible(false);
            editButton.setManaged(false);
            deleteButton.setVisible(false);
            deleteButton.setManaged(false);
        }
        
        // ADMIN permissions - can do everything
        else if (currentUser.isAdmin()) {
            editButton.setDisable(false);
            deleteButton.setDisable(false);
            confirmButton.setDisable(false);
            cancelButton.setDisable(false);
            editButton.setVisible(true);
            editButton.setManaged(true);
            deleteButton.setVisible(true);
            deleteButton.setManaged(true);
            confirmButton.setVisible(true);
            confirmButton.setManaged(true);
            cancelButton.setVisible(true);
            cancelButton.setManaged(true);
        }
    }
    
    private void setupButtonVisibility() {
        if (currentUser == null) {
            // Hide all buttons if no user
            editButton.setVisible(false);
            deleteButton.setVisible(false);
            confirmButton.setVisible(false);
            cancelButton.setVisible(false);
            addButton.setVisible(false);
            return;
        }
        
        // For patients: show add button, hide confirm/cancel buttons
        if (currentUser.isPatient()) {
            confirmButton.setVisible(false);
            cancelButton.setVisible(false);
            editButton.setVisible(true);
            deleteButton.setVisible(true);
            addButton.setVisible(true); // Allow patients to create RDVs
            addButton.setManaged(true);
        }
        // For specialists: only show confirm/cancel buttons
        else if (currentUser.isSpecialiste()) {
            confirmButton.setVisible(true);
            cancelButton.setVisible(true);
            editButton.setVisible(false);
            deleteButton.setVisible(false);
            addButton.setVisible(true); // Allow specialists to create RDVs
            addButton.setManaged(true);
            
            confirmButton.setManaged(true);
            cancelButton.setManaged(true);
            editButton.setManaged(false);
            deleteButton.setManaged(false);
        }
        // For admins: show all buttons
        else if (currentUser.isAdmin()) {
            confirmButton.setVisible(true);
            cancelButton.setVisible(true);
            editButton.setVisible(true);
            deleteButton.setVisible(true);
            addButton.setVisible(true); // Allow admins to create RDVs
            addButton.setManaged(true);
        }
        
        // Force UI update
        if (editButton.getScene() != null) {
            editButton.getScene().getRoot().requestLayout();
        }
    }
    
    private void updateStatistics() {
        if (rendezVousList == null) return;
        
        int todayCount = 0;
        int confirmedCount = 0;
        int pendingCount = 0;
        int weekCount = 0;
        
        for (RendezVous rdv : rendezVousList) {
            if (rdv.isThisWeek()) {
                weekCount++;
            }
            if ("en attente".equals(rdv.getStatut())) {
                pendingCount++;
            } else if ("confirmé".equals(rdv.getStatut())) {
                confirmedCount++;
            }
        }
        
        todayCountLabel.setText(String.valueOf(todayCount));
        weekCountLabel.setText(String.valueOf(weekCount));
        pendingCountLabel.setText(String.valueOf(pendingCount));
        confirmedCountLabel.setText(String.valueOf(confirmedCount));
    }
    
    private void setupTableColumns() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedDateHeure"));
        motifColumn.setCellValueFactory(new PropertyValueFactory<>("motif"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        modeColumn.setCellValueFactory(new PropertyValueFactory<>("mode"));
        patientColumn.setCellValueFactory(new PropertyValueFactory<>("patientNom"));
        specialisteColumn.setCellValueFactory(new PropertyValueFactory<>("specialisteNom"));
        lieuColumn.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        urgenceColumn.setCellValueFactory(new PropertyValueFactory<>("niveauUrgence"));
        
        rendezVousTable.setItems(rendezVousList);
        
        // Set row factory for custom styling
        rendezVousTable.setRowFactory(tv -> new TableRow<RendezVous>() {
            @Override
            protected void updateItem(RendezVous item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    // Color code by status
                    String baseStyle = "";
                    switch (item.getStatut()) {
                        case "confirmé":
                            baseStyle = "-fx-background-color: #d1fae5; -fx-border-color: #10b981;";
                            break;
                        case "en attente":
                            baseStyle = "-fx-background-color: #fef3c7; -fx-border-color: #f59e0b;";
                            break;
                        case "annulé":
                            baseStyle = "-fx-background-color: #fee2e2; -fx-border-color: #ef4444;";
                            break;
                        default:
                            baseStyle = "-fx-background-color: white; -fx-border-color: #e5e7eb;";
                    }
                    
                    // Apply selection style if selected
                    if (isSelected()) {
                        setStyle(baseStyle + " -fx-background-color: #dbeafe; -fx-border-color: #3b82f6; -fx-border-width: 2px;");
                    } else {
                        setStyle(baseStyle);
                    }
                }
            }
        });
    }
    
    private void setupFilters() {
        // Status filter
        statutFilter.getItems().addAll("Tous", "confirmé", "en attente", "annulé", "terminé");
        statutFilter.setValue("Tous");
        
        // Specialist filter
        try {
            List<Specialiste> specialistes = SpecialisteDAO.getAllSpecialistes();
            specialistesList.addAll(specialistes);
            specialisteFilter.getItems().add(null); // "All" option
            specialisteFilter.getItems().addAll(specialistes);
        } catch (Exception e) {
            System.err.println("Error loading specialists: " + e.getMessage());
            e.printStackTrace();
        }
        specialisteFilter.setValue(null);
        
        // Set custom cell factory for specialist filter
        specialisteFilter.setCellFactory(lv -> new ListCell<Specialiste>() {
            @Override
            protected void updateItem(Specialiste item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Tous");
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        
        specialisteFilter.setButtonCell(new ListCell<Specialiste>() {
            @Override
            protected void updateItem(Specialiste item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Tous");
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
    }
    
    private void setupButtonActions() {
        addButton.setOnAction(e -> showAddDialog());
        
        // FXML buttons will use their onAction methods directly
        // No programmatic event handlers needed to avoid conflicts
    }
    
    private void addActionsColumn() {
        // Add action column with buttons for each row
        TableColumn<RendezVous, Void> actionColumn = new TableColumn<>("Actions");
        actionColumn.setPrefWidth(120);
        
        actionColumn.setCellFactory(param -> new TableCell<RendezVous, Void>() {
            private final Button confirmBtn = new Button("✅");
            private final Button cancelBtn = new Button("❌");
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final Button startConsultBtn = new Button("🩺");
            
            {
                // Style buttons
                confirmBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 8 4 8;");
                cancelBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 8 4 8;");
                editBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 8 4 8;");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 8 4 8;");
                startConsultBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 8 4 8;");
                
                // Button actions
                confirmBtn.setOnAction(e -> {
                    RendezVous rdv = getTableView().getItems().get(getIndex());
                    confirmRendezVous(rdv);
                });
                
                cancelBtn.setOnAction(e -> {
                    RendezVous rdv = getTableView().getItems().get(getIndex());
                    cancelRendezVous(rdv);
                });
                
                editBtn.setOnAction(e -> {
                    RendezVous rdv = getTableView().getItems().get(getIndex());
                    showEditDialog(rdv);
                });
                
                deleteBtn.setOnAction(e -> {
                    RendezVous rdv = getTableView().getItems().get(getIndex());
                    deleteRendezVous(rdv);
                });
                
                startConsultBtn.setOnAction(e -> {
                    RendezVous rdv = getTableView().getItems().get(getIndex());
                    startConsultation(rdv);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }
                
                RendezVous rdv = getTableView().getItems().get(getIndex());
                boolean isSpecialist = currentUser != null && currentUser.isSpecialiste();
                boolean isPatient = currentUser != null && currentUser.isPatient();
                boolean isConfirmed = "confirmé".equalsIgnoreCase(rdv.getStatut());
                boolean isCancelled = "annulé".equalsIgnoreCase(rdv.getStatut());
                boolean isTeleconsultationActive = rdv.isSessionActive();
                boolean isOwnRDVSpecialist = isSpecialist && rdv.getSpecialisteId() != null && rdv.getSpecialisteId() == currentUser.getId();
                boolean isOwnRDVPatient = isPatient && rdv.getPatientId() == currentUser.getId();
                
                // Debug output for action column
                System.out.println("🔍 ACTION DEBUG: RDV #" + rdv.getId() + 
                                 " | isSpecialist=" + isSpecialist + 
                                 " | isOwnRDVSpecialist=" + isOwnRDVSpecialist +
                                 " | specialiste_id=" + rdv.getSpecialisteId() + 
                                 " | currentUser.getId()=" + currentUser.getId() +
                                 " | status=" + rdv.getStatut());
                
                HBox currentButtonsBox = new HBox(3);
                
                // PATIENT: Show edit/delete for their own RDVs (only if not confirmed)
                if (isPatient && isOwnRDVPatient && !isConfirmed) {
                    currentButtonsBox.getChildren().addAll(editBtn, deleteBtn);
                } else if (isPatient && isOwnRDVPatient && isConfirmed) {
                    // Patient can view their confirmed RDV but no edit/delete
                    Button viewBtn = new Button("👁 Voir");
                    viewBtn.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 8 4 8;");
                    viewBtn.setOnAction(e -> {
                        // Show RDV details dialog
                        showAlert("Détails RDV", "RDV #" + rdv.getId() + "\nPatient: " + rdv.getPatientNom() + "\nDate: " + rdv.getDateHeure() + "\nStatut: " + rdv.getStatut(), Alert.AlertType.INFORMATION);
                    });
                    currentButtonsBox.getChildren().add(viewBtn);
                }
                
                // PATIENT: Show join call button for teleconsultation when active
                if (isPatient && isOwnRDVPatient && isConfirmed && isTeleconsultationActive) {
                    Button joinCallBtn = new Button("📞 Rejoindre");
                    joinCallBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 8 4 8;");
                    joinCallBtn.setOnAction(e -> {
                        joinTeleconsultation(rdv);
                    });
                    currentButtonsBox.getChildren().add(joinCallBtn);
                }
                
                // SPECIALIST: Show confirm/cancel for their own pending RDVs
                if (isSpecialist && isOwnRDVSpecialist && !isConfirmed && !isCancelled) {
                    System.out.println("🔍 Adding Confirm/Cancel buttons for RDV #" + rdv.getId());
                    currentButtonsBox.getChildren().addAll(confirmBtn, cancelBtn);
                } else if (isSpecialist && isOwnRDVSpecialist && isConfirmed && !isTeleconsultationActive) {
                    System.out.println("🔍 Adding Start Consultation button for RDV #" + rdv.getId());
                    currentButtonsBox.getChildren().add(startConsultBtn);
                } else if (isSpecialist) {
                    System.out.println("🔍 Specialist RDV #" + rdv.getId() + " - no action buttons (status: " + rdv.getStatut() + ")");
                }
                
                if (currentButtonsBox.getChildren().isEmpty()) {
                    setGraphic(null);
                } else {
                    setGraphic(currentButtonsBox);
                }
            }
        });
        
        rendezVousTable.getColumns().add(actionColumn);
    }
    
    public void loadRendezVous() {
        try {
            List<RendezVous> rendezVous;
            
            // Filter RDVs based on current user role
            if (currentUser != null && currentUser.isSpecialiste()) {
                // If logged in as specialiste (including hybrid), only show RDVs assigned to this specialiste
                rendezVous = RendezVousDAO.getRendezVousBySpecialiste(currentUser.getId());
                System.out.println("🔍 Specialist " + currentUser.getNomComplet() + " viewing " + rendezVous.size() + " RDVs assigned to them");
                System.out.println("🔍 Specialist must use action buttons to accept/cancel their assigned RDVs");
                
                // If specialist has no RDVs, show guidance message
                if (rendezVous.isEmpty()) {
                    System.out.println("🔍 Specialist has no RDVs assigned!");
                    System.out.println("🔍 Tip: Click 'Actualiser' to assign some RDVs for testing, or ask admin to assign RDVs to you.");
                    updateStatusLabel("Aucun RDV assigné - Cliquez sur 'Actualiser' pour en assigner");
                }
                
                System.out.println("🔍 DEBUG: First 3 RDV IDs:");
                for (int i = 0; i < Math.min(3, rendezVous.size()); i++) {
                    System.out.println("🔍 RDV #" + (i+1) + ": ID=" + rendezVous.get(i).getId() + ", specialiste_id=" + rendezVous.get(i).getSpecialisteId());
                }
            } else if (currentUser != null && currentUser.isAdmin()) {
                // Admin can see all RDVs
                rendezVous = RendezVousDAO.getAllRendezVous();
            } else if (currentUser != null && currentUser.isPatient()) {
                // Regular users (patients) can only see their own RDVs
                rendezVous = RendezVousDAO.getRendezVousByPatient(currentUser.getId());
                System.out.println("🔍 Patient " + currentUser.getNomComplet() + " viewing " + rendezVous.size() + " RDVs assigned to them");
            } else {
                // Default - show all RDVs
                rendezVous = RendezVousDAO.getAllRendezVous();
            }
            
            // Safety check: don't clear if new list is empty and current list has data
            if (rendezVous.isEmpty() && !rendezVousList.isEmpty()) {
                System.out.println("🔍 WARNING: New list is empty but current list has " + rendezVousList.size() + " items. Preserving current data.");
                return; // Don't clear the table
            }
            
            rendezVousList.clear();
            rendezVousList.addAll(rendezVous);
            
            // Make sure the table is updated
            rendezVousTable.setItems(FXCollections.observableArrayList(rendezVousList));
            rendezVousTable.refresh();
            
            updateStatusLabel("Rendez-vous chargés");
            updateCountLabel(rendezVous.size());
            updateStatistics(); // Update real-time statistics for current user
            
        } catch (Exception e) {
            System.out.println("Erreur chargement: " + e.getMessage());
        }
    }
    
    private void searchRendezVous() {
        try {
            String keyword = searchField.getText().trim();
            String statut = "Tous".equals(statutFilter.getValue()) ? null : statutFilter.getValue();
            Integer specialisteId = specialisteFilter.getValue() != null ? specialisteFilter.getValue().getId() : null;
            
            List<RendezVous> results = RendezVousDAO.searchRendezVous(keyword, statut, specialisteId);
            rendezVousList.clear();
            rendezVousList.addAll(results);
            
            // Make sure the table is updated
            rendezVousTable.setItems(FXCollections.observableArrayList(rendezVousList));
            rendezVousTable.refresh();
            
            updateStatusLabel("Recherche terminée");
            updateCountLabel(results.size());
            
        } catch (Exception e) {
            System.out.println("Erreur recherche: " + e.getMessage());
        }
    }
    
    private void clearSearch() {
        searchField.clear();
        statutFilter.setValue("Tous");
        specialisteFilter.setValue(null);
        loadRendezVous();
    }
    
    private void showAddDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/rendezvous_dialog.fxml"));
            Parent root = loader.load();
            
            RendezVousDialogController controller = loader.getController();
            controller.setMode(RendezVousDialogController.Mode.ADD);
            controller.setCurrentUser(currentUser);
            
            Stage dialog = new Stage();
            dialog.setTitle("Nouveau Rendez-vous");
            dialog.setScene(new Scene(root));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            
            // Store controller reference in dialog for notifications
            dialog.getScene().getRoot().setUserData(this);
            
            dialog.showAndWait();
            
            // Refresh data after dialog closes
            loadRendezVous();
            
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir la boîte de dialogue: " + e.getMessage());
        }
    }
    
    private void showEditDialog() {
        try {
            RendezVous selected = rendezVousTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            
            // Use simplified edit interface for regular users
            if (currentUser != null && currentUser.isPatient()) {
                showSimpleEditDialog(selected);
            } else {
                showFullEditDialog(selected);
            }
            
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir la boîte de dialogue: " + e.getMessage());
        }
    }
    
    private void showSimpleEditDialog(RendezVous selected) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/edit_rdv.fxml"));
            Parent root = loader.load();
            
            EditRDVController controller = loader.getController();
            controller.setRendezVous(selected);
            controller.setCurrentUser(currentUser);
            controller.setParentController(this);
            
            Stage dialog = new Stage();
            dialog.setTitle("Modifier Rendez-vous");
            dialog.setScene(new Scene(root));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            
            dialog.showAndWait();
            
            // Refresh data after dialog closes
            loadRendezVous();
            
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir la boîte de dialogue: " + e.getMessage());
        }
    }
    
    private void showFullEditDialog(RendezVous selected) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/rendezvous_dialog.fxml"));
            Parent root = loader.load();
            
            RendezVousDialogController controller = loader.getController();
            controller.setMode(RendezVousDialogController.Mode.EDIT);
            controller.setRendezVous(selected);
            controller.setCurrentUser(currentUser);
            
            Stage dialog = new Stage();
            dialog.setTitle("Modifier Rendez-vous");
            dialog.setScene(new Scene(root));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            
            // Store controller reference in dialog for notifications
            dialog.getScene().getRoot().setUserData(this);
            
            dialog.showAndWait();
            
            // Refresh data after dialog closes
            loadRendezVous();
            
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir la boîte de dialogue: " + e.getMessage());
        }
    }
    
    private void deleteSelectedRendezVous() {
        try {
            RendezVous selected = rendezVousTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            
            // Show confirmation dialog
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Supprimer ce rendez-vous ?");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer le rendez-vous du " + 
                                selected.getFormattedDateHeure() + " avec " + 
                                selected.getSpecialisteNom() + " ?");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                
                boolean deleted = RendezVousDAO.deleteRendezVous(selected.getId());
                
                if (deleted) {
                    showInfo("Succès", "Rendez-vous supprimé avec succès");
                    loadRendezVous();
                } else {
                    showError("Erreur", "Impossible de supprimer le rendez-vous");
                }
            }
            
        } catch (Exception e) {
            showError("Erreur", "Impossible de supprimer le rendez-vous: " + e.getMessage());
        }
    }
    
    // Overloaded method for row button action
    private void showEditDialog(RendezVous rdv) {
        try {
            if (rdv == null) {
                return;
            }
            
            // Use simplified edit interface for regular users
            if (currentUser != null && currentUser.isPatient()) {
                showSimpleEditDialog(rdv);
            } else {
                showFullEditDialog(rdv);
            }
            
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir la boîte de dialogue: " + e.getMessage());
        }
    }
    
    // Overloaded method for row button action
    private void deleteRendezVous(RendezVous rdv) {
        try {
            if (rdv == null) {
                return;
            }
            
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Supprimer ce rendez-vous ?");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer le rendez-vous du " + 
                                rdv.getFormattedDateHeure() + " ?");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                
                boolean deleted = RendezVousDAO.deleteRendezVous(rdv.getId());
                
                if (deleted) {
                    showInfo("Succès", "Rendez-vous supprimé avec succès");
                    loadRendezVous();
                } else {
                    showError("Erreur", "Impossible de supprimer le rendez-vous");
                }
            }
            
        } catch (Exception e) {
            showError("Erreur", "Impossible de supprimer le rendez-vous: " + e.getMessage());
        }
    }
    
    private void confirmSelectedRendezVous() {
        try {
            RendezVous selected = rendezVousTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            
            // Check if already confirmed
            if ("confirmé".equalsIgnoreCase(selected.getStatut())) {
                return;
            }
            
            // Update status to confirmed
            selected.setStatut("confirmé");
            boolean updated = RendezVousDAO.updateRendezVous(selected);
            
            if (updated) {
                showInfo("Succès", "Rendez-vous confirmé avec succès!");
                addAppointmentNotification(selected, "confirmed");
                loadRendezVous();
            } else {
                showError("Erreur", "Impossible de confirmer le rendez-vous");
            }
            
        } catch (Exception e) {
            showError("Erreur", "Impossible de confirmer le rendez-vous: " + e.getMessage());
        }
    }
    
    private void cancelSelectedRendezVous() {
        try {
            RendezVous selected = rendezVousTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            
            // Check if already cancelled
            if ("annulé".equalsIgnoreCase(selected.getStatut())) {
                return;
            }
            
            // Update status to cancelled
            selected.setStatut("annulé");
            boolean updated = RendezVousDAO.updateRendezVous(selected);
            
            if (updated) {
                showInfo("Succès", "Rendez-vous annulé avec succès!");
                addAppointmentNotification(selected, "cancelled");
                loadRendezVous();
            } else {
                showError("Erreur", "Impossible d'annuler le rendez-vous");
            }
            
        } catch (Exception e) {
            showError("Erreur", "Impossible d'annuler le rendez-vous: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Helper methods for row-specific actions
    @FXML
    private void handleAssignRDVs() {
        assignSomeRDVsToSpecialist();
    }
    private void assignSomeRDVsToSpecialist() {
        try {
            if (currentUser == null || !currentUser.isSpecialiste()) {
                return;
            }
            
            // Get all RDVs and assign first 3 unassigned ones to current specialist
            List<RendezVous> allRDVs = RendezVousDAO.getAllRendezVous();
            int assignedCount = 0;
            
            for (RendezVous rdv : allRDVs) {
                if (rdv.getSpecialisteId() == null && assignedCount < 3) {
                    rdv.setSpecialisteId(currentUser.getId());
                    if (RendezVousDAO.updateRendezVous(rdv)) {
                        assignedCount++;
                        System.out.println("🔍 Assigned RDV #" + rdv.getId() + " to " + currentUser.getNomComplet());
                    }
                }
            }
            
            if (assignedCount > 0) {
                showInfo("Succès", assignedCount + " RDVs assignés à " + currentUser.getNomComplet() + "!\nVous pouvez maintenant les confirmer ou les annuler.");
                System.out.println("🔍 Assignment successful: " + assignedCount + " RDVs assigned");
                // Reload the RDVs to show the newly assigned ones
                loadRendezVous();
                updateStatistics();
            } else {
                showInfo("Info", "Aucun RDV disponible à assigner");
            }
            
        } catch (Exception e) {
            showError("Erreur", "Erreur lors de l'assignation: " + e.getMessage());
        }
    }
    
    private void confirmRendezVous(RendezVous rdv) {
        try {
            if (rdv == null) {
                showError("Erreur", "Rendez-vous invalide");
                return;
            }
            
            // Check if already confirmed
            if ("confirmé".equalsIgnoreCase(rdv.getStatut())) {
                showInfo("Info", "Rendez-vous déjà confirmé");
                return;
            }
            
            // Update status to confirmed
            rdv.setStatut("confirmé");
            boolean updated = RendezVousDAO.updateRendezVous(rdv);
            
            if (updated) {
                showInfo("Succès", "Rendez-vous #" + rdv.getId() + " confirmé avec succès!");
                addAppointmentNotification(rdv, "confirmed");
                loadRendezVous();
                updateStatistics();
            } else {
                showError("Erreur", "Échec de la confirmation");
            }
            
        } catch (Exception e) {
            showError("Erreur", "Impossible de confirmer le rendez-vous: " + e.getMessage());
        }
    }
    
    private void cancelRendezVous(RendezVous rdv) {
        try {
            if (rdv == null) {
                showError("Erreur", "Rendez-vous invalide");
                return;
            }
            
            // Check if already cancelled
            if ("annulé".equalsIgnoreCase(rdv.getStatut())) {
                showInfo("Info", "Rendez-vous déjà annulé");
                return;
            }
            
            // Update status to cancelled
            rdv.setStatut("annulé");
            boolean updated = RendezVousDAO.updateRendezVous(rdv);
            
            if (updated) {
                showInfo("Succès", "Rendez-vous #" + rdv.getId() + " annulé avec succès!");
                addAppointmentNotification(rdv, "cancelled");
                loadRendezVous();
                updateStatistics();
            } else {
                showError("Erreur", "Échec de l'annulation");
            }
            
        } catch (Exception e) {
            showError("Erreur", "Impossible d'annuler le rendez-vous: " + e.getMessage());
        }
    }
    
    private void updateStatusLabel(String message) {
        statusLabel.setText(message);
    }
    
    private void updateCountLabel(int count) {
        countLabel.setText("Total: " + count + " rendez-vous");
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
    
    // Handle table double-click for quick edit
    @FXML
    private void handleTableDoubleClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            RendezVous selected = rendezVousTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showEditDialog();
            }
        }
    }
    
    // Refresh button action
    @FXML
    private void handleRefreshAction() {
        loadRendezVous();
    }
    
    // Export to PDF functionality
    @FXML
    private void handleExportAction() {
        try {
            // This would integrate with your existing PDF functionality
            showInfo("Export", "Fonctionnalité d'export PDF à implémenter");
        } catch (Exception e) {
            showError("Erreur d'export", "Impossible d'exporter les données: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleExportPdf() {
        try {
            // This would integrate with your existing PDF functionality
            showInfo("Export", "Fonctionnalité d'export PDF à implémenter");
        } catch (Exception e) {
            showError("Erreur d'export", "Impossible d'exporter les données: " + e.getMessage());
        }
    }
    
    // Add action method
    @FXML
    private void handleAdd() {
        showAddDialog();
    }
    
    @FXML
    private void handleAddAction() {
        showAddDialog();
    }
    
    // Edit action method
    @FXML
    private void handleEdit() {
        showEditDialog();
    }
    
    // Confirm action method
    @FXML
    private void handleConfirm() {
        confirmSelectedRendezVous();
    }
    
    // Cancel action method
    @FXML
    private void handleCancel() {
        cancelSelectedRendezVous();
    }
    
    // Delete action method
    @FXML
    private void handleDelete() {
        deleteSelectedRendezVous();
    }
    
    // Search action method
    @FXML
    private void handleSearch() {
        searchRendezVous();
    }
    
    @FXML
    private void handleSearchAction() {
        searchRendezVous();
    }
    
    // Clear search action method
    @FXML
    private void handleClear() {
        clearSearch();
    }
    
    @FXML
    private void handleClearSearchAction() {
        clearSearch();
    }
    
    // Refresh action method
    @FXML
    private void handleRefresh() {
        try {
            System.out.println("🔍 Refresh clicked for user: " + (currentUser != null ? currentUser.getNomComplet() : "null"));
            
            // If specialist has no RDVs, assign some for testing
            if (currentUser != null && currentUser.isSpecialiste()) {
                List<RendezVous> currentRDVs = RendezVousDAO.getRendezVousBySpecialiste(currentUser.getId());
                System.out.println("🔍 Specialist currently has " + currentRDVs.size() + " RDVs");
                
                if (currentRDVs.isEmpty()) {
                    System.out.println("🔍 Specialist has no RDVs, assigning some for testing...");
                    assignSomeRDVsToSpecialist();
                } else {
                    System.out.println("🔍 Specialist has RDVs, normal refresh...");
                    loadRendezVous();
                    updateStatistics();
                    updateStatusLabel("Rendez-vous actualisés");
                }
            } else {
                System.out.println("🔍 Non-specialist user, normal refresh...");
                loadRendezVous();
                updateStatistics();
                updateStatusLabel("Rendez-vous actualisés");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error during refresh: " + e.getMessage());
            e.printStackTrace();
            updateStatusLabel("Erreur lors de l'actualisation");
        }
    }
    
    @FXML
    private void handleRetour() {
        // Navigate back to dashboard
        try {
            NavigationService.getInstance().navigateToDashboard(rendezVousTable, currentUser);
        } catch (Exception e) {
            showError("Erreur", "Impossible de retourner: " + e.getMessage());
        }
    }
    
        
        
        
    // Set current user
    public void setCurrentUser(User user) {
        this.currentUser = user;
        setupButtonVisibility();
        loadNotificationsForUser();
    }
    
    // ===== NOTIFICATION METHODS =====
    
    private void setupNotifications() {
        // Initialize notifications container
        if (notificationsContainer != null) {
            refreshNotificationsDisplay();
        }
    }
    
    private void loadNotificationsForUser() {
        if (currentUser == null) return;
        
        try {
            // Clear existing notifications
            notificationsList.clear();
            
            // Load notifications for current user
            // For now, we'll create some sample notifications
            // In a real implementation, you would load from a database
            
            // Add welcome notification for new users
            if (currentUser.isPatient()) {
                addNotification(new Notification(
                    "Bienvenue sur BioSync", 
                    "Votre espace patient est prêt. Vous pouvez consulter vos rendez-vous ici.", 
                    "info", 
                    currentUser.getId()
                ));
            }
            
            // Check for upcoming appointments and add reminders
            checkUpcomingAppointments();
            
            refreshNotificationsDisplay();
            
        } catch (Exception e) {
            System.err.println("Error loading notifications: " + e.getMessage());
        }
    }
    
    private void checkUpcomingAppointments() {
        if (currentUser == null || !currentUser.isPatient()) return;
        
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        
        for (RendezVous rdv : rendezVousList) {
            if (rdv.getPatientId() == currentUser.getId()) {
                java.time.LocalDateTime rdvTime = rdv.getDateHeure();
                java.time.Duration duration = java.time.Duration.between(now, rdvTime);
                
                // Add notifications based on appointment timing
                if (duration.toHours() <= 24 && duration.toHours() > 0) {
                    addNotification(new Notification(
                        "Rappel de rendez-vous",
                        "Votre rendez-vous \"" + rdv.getMotif() + "\" est prévu pour demain à " + 
                        rdvTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) + 
                        " avec " + rdv.getSpecialisteNom() + ".",
                        "warning",
                        currentUser.getId(),
                        rdv.getId()
                    ));
                } else if (duration.toHours() <= 2 && duration.toHours() > 0) {
                    addNotification(new Notification(
                        "Rendez-vous imminent",
                        "Votre rendez-vous \"" + rdv.getMotif() + "\" est dans " + 
                        duration.toMinutes() + " minutes!",
                        "error",
                        currentUser.getId(),
                        rdv.getId()
                    ));
                } else if ("confirmé".equals(rdv.getStatut()) && duration.toDays() <= 7 && duration.toDays() >= 0) {
                    addNotification(new Notification(
                        "Rendez-vous confirmé",
                        "Votre rendez-vous du " + rdv.getFormattedDateHeure() + 
                        " a été confirmé avec " + rdv.getSpecialisteNom() + ".",
                        "success",
                        currentUser.getId(),
                        rdv.getId()
                    ));
                }
            }
        }
    }
    
    private void addNotification(Notification notification) {
        if (notification != null && !notificationsList.contains(notification)) {
            notificationsList.add(0, notification); // Add to beginning
        }
    }
    
    private void refreshNotificationsDisplay() {
        if (notificationsContainer == null) return;
        
        javafx.application.Platform.runLater(() -> {
            notificationsContainer.getChildren().clear();
            
            if (notificationsList.isEmpty()) {
                notificationsContainer.getChildren().add(
                    new Label("Aucune notification pour le moment")
                );
                return;
            }
            
            for (Notification notification : notificationsList) {
                HBox notificationBox = createNotificationBox(notification);
                notificationsContainer.getChildren().add(notificationBox);
            }
        });
    }
    
    private HBox createNotificationBox(Notification notification) {
        HBox box = new HBox(15);
        box.setStyle(notification.getColorStyle() + 
                    "-fx-background-radius: 12; -fx-border-radius: 12; -fx-padding: 18 20 18 20; -fx-border-width: 1.5;");
        box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        box.setPrefWidth(Double.MAX_VALUE);
        
        // Icon
        Label iconLabel = new Label(notification.getIcon());
        iconLabel.setStyle("-fx-font-size: 20px; -fx-min-width: 25; -fx-alignment: center;");
        
        // Content
        VBox content = new VBox(6);
        content.setPrefWidth(Double.MAX_VALUE);
        HBox.setHgrow(content, javafx.scene.layout.Priority.ALWAYS);
        
        Label titleLabel = new Label(notification.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-wrap-text: true;");
        
        Label messageLabel = new Label(notification.getMessage());
        messageLabel.setStyle("-fx-font-size: 13px; -fx-wrap-text: true; -fx-line-spacing: 2;");
        messageLabel.setMaxWidth(450);
        
        Label timeLabel = new Label(notification.getFormattedTimestamp());
        timeLabel.setStyle("-fx-font-size: 12px; -fx-opacity: 0.8; -fx-font-style: italic;");
        
        content.getChildren().addAll(titleLabel, messageLabel, timeLabel);
        
        // Delete button
        Button deleteBtn = new Button("✕");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6b7280; -fx-font-size: 14px; -fx-cursor: hand; -fx-min-width: 30; -fx-min-height: 30; -fx-background-radius: 15;");
        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("-fx-background-color: rgba(107,114,128,0.1); -fx-text-fill: #374151; -fx-font-size: 14px; -fx-cursor: hand; -fx-min-width: 30; -fx-min-height: 30; -fx-background-radius: 15;"));
        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6b7280; -fx-font-size: 14px; -fx-cursor: hand; -fx-min-width: 30; -fx-min-height: 30; -fx-background-radius: 15;"));
        deleteBtn.setOnAction(e -> removeNotification(notification));
        
        box.getChildren().addAll(iconLabel, content, deleteBtn);
        
        return box;
    }
    
    private void removeNotification(Notification notification) {
        notificationsList.remove(notification);
        refreshNotificationsDisplay();
    }
    
    @FXML
    private void handleClearNotifications() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Effacer les notifications");
        alert.setHeaderText("Voulez-vous effacer toutes les notifications?");
        alert.setContentText("Cette action ne peut pas être annulée.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            notificationsList.clear();
            refreshNotificationsDisplay();
        }
    }
    
    public void addAppointmentNotification(RendezVous rdv, String action) {
        if (currentUser == null || !currentUser.isPatient() || rdv.getPatientId() != currentUser.getId()) {
            return;
        }
        
        String title = "";
        String message = "";
        String type = "info";
        
        switch (action) {
            case "created":
                title = "Nouveau rendez-vous créé";
                message = "Votre rendez-vous \"" + rdv.getMotif() + "\" a été créé pour le " + 
                         rdv.getFormattedDateHeure() + " avec " + rdv.getSpecialisteNom() + ".";
                type = "success";
                break;
            case "confirmed":
                title = "Rendez-vous confirmé";
                message = "Votre rendez-vous du " + rdv.getFormattedDateHeure() + 
                         " a été confirmé par " + rdv.getSpecialisteNom() + ".";
                type = "success";
                break;
            case "cancelled":
                title = "Rendez-vous annulé";
                message = "Votre rendez-vous du " + rdv.getFormattedDateHeure() + 
                         " a été annulé. Contactez-nous pour un nouveau rendez-vous.";
                type = "error";
                break;
            case "modified":
                title = "Rendez-vous modifié";
                message = "Votre rendez-vous a été modifié. Nouvelle date: " + 
                         rdv.getFormattedDateHeure() + " avec " + rdv.getSpecialisteNom() + ".";
                type = "warning";
                break;
            case "teleconsultation_started":
                title = "📞 Téléconsultation démarrée";
                message = "Dr. " + rdv.getSpecialisteNom() + " a démarré votre téléconsultation. Cliquez pour rejoindre l'appel.";
                type = "success";
                break;
        }
        
        addNotification(new Notification(title, message, type, currentUser.getId(), rdv.getId()));
        refreshNotificationsDisplay();
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Start consultation method - opens consultation interface
    private void startConsultation(RendezVous rdv) {
        if (currentUser == null || !currentUser.isSpecialiste()) {
            showAlert("Erreur", "Seul un spécialiste peut démarrer une consultation.", Alert.AlertType.ERROR);
            return;
        }
        
        if (!"confirmé".equalsIgnoreCase(rdv.getStatut())) {
            showAlert("Erreur", "Le rendez-vous doit être confirmé avant de démarrer la consultation.", Alert.AlertType.ERROR);
            return;
        }
        
        try {
            // Check if it's a teleconsultation
            if ("téléconsultation".equalsIgnoreCase(rdv.getMode())) {
                // Start teleconsultation session
                rdv.startSession();
                RendezVousDAO.updateRendezVous(rdv);
                
                // Open teleconsultation interface for specialist
                openTeleconsultationInterface(rdv, true);
                
                // Notify patient
                addAppointmentNotification(rdv, "teleconsultation_started");
            } else {
                // Regular consultation
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/consultation_interface.fxml"));
                Parent root = loader.load();
                
                // Get controller and pass RDV data
                ConsultationController controller = loader.getController();
                controller.setRendezVous(rdv);
                controller.setCurrentUser(currentUser);
                controller.setRendezVousController(this); // Pass reference back to this controller
                
                // Create and show stage
                Stage stage = new Stage();
                stage.setTitle("🩺 Consultation - " + rdv.getPatientNom());
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setResizable(true);
                stage.showAndWait();
            }
            
            // Refresh RDV list after consultation closes
            loadRendezVous();
            updateStatistics();
            
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir l'interface de consultation: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    
    // Join teleconsultation method for patients
    private void joinTeleconsultation(RendezVous rdv) {
        if (currentUser == null || !currentUser.isPatient()) {
            showAlert("Erreur", "Seul un patient peut rejoindre une téléconsultation.", Alert.AlertType.ERROR);
            return;
        }
        
        if (!rdv.isSessionActive()) {
            showAlert("Erreur", "La téléconsultation n'a pas encore commencé.", Alert.AlertType.ERROR);
            return;
        }
        
        try {
            // Open teleconsultation interface for patient
            openTeleconsultationInterface(rdv, false);
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de rejoindre la téléconsultation: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    
    // Open teleconsultation interface
    private void openTeleconsultationInterface(RendezVous rdv, boolean isSpecialist) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/teleconsultation_interface.fxml"));
            Parent root = loader.load();
            
            // Get controller and pass RDV data
            TeleconsultationController controller = loader.getController();
            controller.setRendezVous(rdv);
            controller.setCurrentUser(currentUser);
            controller.setIsSpecialist(isSpecialist);
            
            // Create and show stage
            Stage stage = new Stage();
            stage.setTitle("📞 Téléconsultation - " + (isSpecialist ? rdv.getPatientNom() : rdv.getSpecialisteNom()));
            stage.setScene(new Scene(root));
            stage.initModality(Modality.NONE); // Non-modal for real-time communication
            stage.setResizable(true);
            stage.show();
            
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir l'interface de téléconsultation: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    
    // Method to open prescription interface (called from ConsultationController)
    public void openPrescriptionInterface(RendezVous rdv) {
        // Only specialists can access prescription interface
        if (currentUser == null || !currentUser.isSpecialiste()) {
            showAlert("Accès refusé", "Seul un spécialiste peut accéder à l'interface de prescription.", Alert.AlertType.ERROR);
            return;
        }
        
        try {
            // Load prescription interface
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/prescription_interface.fxml"));
            Parent root = loader.load();
            
            // Get controller and pass data
            PrescriptionController controller = loader.getController();
            controller.setRendezVous(rdv);
            controller.setCurrentUser(currentUser);
            controller.setRendezVousController(this);
            
            // Create and show stage
            Stage stage = new Stage();
            stage.setTitle("💊 Prescription - " + rdv.getPatientNom());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);
            stage.showAndWait();
            
            // Refresh after prescription closes
            loadRendezVous();
            updateStatistics();
            
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir l'interface de prescription: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
}
