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
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.dao.RendezVousDAO;
import org.example.dao.SpecialisteDAO;
import org.example.dao.UserDAO;
import org.example.model.RendezVous;
import org.example.model.Specialiste;
import org.example.model.User;

import java.util.List;
import java.util.Optional;

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
    
    private User currentUser;
    private ObservableList<RendezVous> rendezVousList = FXCollections.observableArrayList();
    private ObservableList<User> patientsList = FXCollections.observableArrayList();
    private ObservableList<Specialiste> specialistesList = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        setupButtonActions();
        loadRendezVous();
        setupTableSelection();
        setupButtonVisibility();
        
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
        
        // Initially disable all buttons
        editButton.setDisable(true);
        deleteButton.setDisable(true);
        confirmButton.setDisable(true);
        cancelButton.setDisable(true);
        
        // Always setup visibility based on user role
        setupButtonVisibility();
        
        // If nothing selected, keep all disabled
        if (!hasSelection || currentUser == null) {
            return;
        }
        
        // PATIENT permissions - can edit/delete their own RDVs
        if (currentUser.isPatient()) {
            boolean isOwnRDV = selected.getPatientId() == currentUser.getId();
            boolean canModify = isOwnRDV && 
                               !"confirmé".equalsIgnoreCase(selected.getStatut()) &&
                               !"annulé".equalsIgnoreCase(selected.getStatut());
            
            editButton.setDisable(!canModify);
            deleteButton.setDisable(!isOwnRDV); // Can delete even if confirmed
            
            // Patients cannot see confirm/cancel buttons
            confirmButton.setVisible(false);
            cancelButton.setVisible(false);
        }
        
        // SPECIALIST permissions - can confirm/cancel ANY pending RDV
        else if ("specialiste".equals(currentUser.getRoles()) || currentUser.getRoles().contains("specialiste")) {
            boolean canConfirmCancel = !"confirmé".equalsIgnoreCase(selected.getStatut()) &&
                                       !"annulé".equalsIgnoreCase(selected.getStatut());
            
            confirmButton.setDisable(false);
            cancelButton.setDisable(false);
        }
        
        // ADMIN permissions - can do everything
        else if (currentUser.isAdmin()) {
            editButton.setDisable(false);
            deleteButton.setDisable(false);
            confirmButton.setDisable(false);
            cancelButton.setDisable(false);
        }
    }
    
    private void setupButtonVisibility() {
        if (currentUser == null) {
            // Hide all buttons if no user
            editButton.setVisible(false);
            deleteButton.setVisible(false);
            confirmButton.setVisible(false);
            cancelButton.setVisible(false);
            return;
        }
        
        // For patients: hide confirm/cancel buttons
        if (currentUser.isPatient()) {
            confirmButton.setVisible(false);
            cancelButton.setVisible(false);
            editButton.setVisible(true);
            deleteButton.setVisible(true);
        }
        // For specialists: only show confirm/cancel buttons
        else if ("specialiste".equals(currentUser.getRoles()) || currentUser.getRoles().contains("specialiste")) {
            confirmButton.setVisible(true);
            cancelButton.setVisible(true);
            editButton.setVisible(false);
            deleteButton.setVisible(false);
            
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
        }
        
        // Force UI update
        if (editButton.getScene() != null) {
            editButton.getScene().getRoot().requestLayout();
        }
    }
    
    private void updateStatistics() {
        if (rendezVousList == null) return;
        
        int todayCount = 0;
        int weekCount = 0;
        int pendingCount = 0;
        int confirmedCount = 0;
        
        for (RendezVous rdv : rendezVousList) {
            // Count today's appointments
            if (rdv.isToday()) {
                todayCount++;
            }
            
            // Count this week's appointments
            if (rdv.isThisWeek()) {
                weekCount++;
            }
            
            // Count by status
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
        List<Specialiste> specialistes = SpecialisteDAO.getAllSpecialistes();
        specialistesList.addAll(specialistes);
        specialisteFilter.getItems().add(null); // "All" option
        specialisteFilter.getItems().addAll(specialistes);
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
            private final HBox buttonsBox = new HBox(5, confirmBtn, cancelBtn);
            
            {
                // Style buttons
                confirmBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 8 4 8;");
                cancelBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 4 8 4 8;");
                
                // Button actions
                confirmBtn.setOnAction(e -> {
                    RendezVous rdv = getTableView().getItems().get(getIndex());
                    confirmRendezVous(rdv);
                });
                
                cancelBtn.setOnAction(e -> {
                    RendezVous rdv = getTableView().getItems().get(getIndex());
                    cancelRendezVous(rdv);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setGraphic(null);
                } else {
                    int index = getIndex();
                    if (index >= 0 && index < getTableView().getItems().size()) {
                        RendezVous rdv = getTableView().getItems().get(index);
                        
                        // Debug output
                        boolean isSpecialist = currentUser != null && 
                                              ("specialiste".equals(currentUser.getRoles()) || 
                                               currentUser.getRoles().contains("specialiste"));
                        System.out.println("🔍 Row " + index + " - User: " + (currentUser != null ? currentUser.getRoles() : "null") + 
                                         ", RDV Status: " + rdv.getStatut() + 
                                         ", Is Specialist: " + isSpecialist);
                        
                        // Show buttons only for specialists and only for pending RDVs
                        if (currentUser != null && isSpecialist && 
                            !"confirmé".equalsIgnoreCase(rdv.getStatut()) && 
                            !"annulé".equalsIgnoreCase(rdv.getStatut()) &&
                            !"REALISE".equalsIgnoreCase(rdv.getStatut())) {
                            setGraphic(buttonsBox);
                            System.out.println("🔍 Showing buttons for row " + index);
                        } else {
                            setGraphic(null);
                            System.out.println("🔍 Hiding buttons for row " + index);
                        }
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
        
        // Add the column to the table
        System.out.println("🔍 Adding Actions column to table...");
        rendezVousTable.getColumns().add(actionColumn);
        System.out.println("🔍 Actions column added. Total columns: " + rendezVousTable.getColumns().size());
        
        // Force table refresh
        javafx.application.Platform.runLater(() -> {
            rendezVousTable.refresh();
            System.out.println("🔍 Table refreshed with Actions column");
        });
    }
    
    private void loadRendezVous() {
        try {
            List<RendezVous> rendezVous = RendezVousDAO.getAllRendezVous();
            rendezVousList.clear();
            rendezVousList.addAll(rendezVous);
            
            // Make sure the table is updated
            rendezVousTable.setItems(FXCollections.observableArrayList(rendezVousList));
            rendezVousTable.refresh();
            
            updateStatusLabel("Rendez-vous chargés");
            updateCountLabel(rendezVous.size());
            
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
                loadRendezVous();
            } else {
                showError("Erreur", "Impossible de confirmer le rendez-vous");
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
                loadRendezVous();
            } else {
                showError("Erreur", "Impossible d'annuler le rendez-vous");
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
        loadRendezVous();
    }
    
    // Retour action method
    @FXML
    private void handleRetour() {
        // Navigate back to dashboard
        try {
            Stage stage = (Stage) rendezVousTable.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            showError("Erreur", "Impossible de retourner: " + e.getMessage());
        }
    }
    
        
        
        
    // Set current user
    public void setCurrentUser(User user) {
        this.currentUser = user;
        setupButtonVisibility();
    }
}
