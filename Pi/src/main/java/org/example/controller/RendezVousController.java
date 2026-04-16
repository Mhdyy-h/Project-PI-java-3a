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
    
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statutFilter;
    @FXML private ComboBox<Specialiste> specialisteFilter;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;
    @FXML private Button searchButton;
    @FXML private Button clearSearchButton;
    
    @FXML private Label statusLabel;
    @FXML private Label countLabel;
    
    private User currentUser;
    private ObservableList<RendezVous> rendezVousList = FXCollections.observableArrayList();
    private ObservableList<User> patientsList = FXCollections.observableArrayList();
    private ObservableList<Specialiste> specialistesList = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        setupButtonActions();
        setupTableSelection();
        loadRendezVous();
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
                    switch (item.getStatut()) {
                        case "confirmé":
                            setStyle("-fx-background-color: #d4edda;"); // Green
                            break;
                        case "en attente":
                            setStyle("-fx-background-color: #fff3cd;"); // Yellow
                            break;
                        case "annulé":
                            setStyle("-fx-background-color: #f8d7da;"); // Red
                            break;
                        case "terminé":
                            setStyle("-fx-background-color: #e2e3e5;"); // Gray
                            break;
                        default:
                            setStyle("");
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
                setText(empty ? "Tous" : item.getDisplayName());
            }
        });
        
        specialisteFilter.setButtonCell(new ListCell<Specialiste>() {
            @Override
            protected void updateItem(Specialiste item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "Tous" : item.getDisplayName());
            }
        });
    }
    
    private void setupButtonActions() {
        addButton.setOnAction(e -> showAddDialog());
        editButton.setOnAction(e -> showEditDialog());
        deleteButton.setOnAction(e -> deleteSelectedRendezVous());
        refreshButton.setOnAction(e -> loadRendezVous());
        searchButton.setOnAction(e -> searchRendezVous());
        clearSearchButton.setOnAction(e -> clearSearch());
        
        // Add hover effects
        addButton.setOnMouseEntered(e -> addButton.setStyle("-fx-background-color: #0056b3;"));
        addButton.setOnMouseExited(e -> addButton.setStyle("-fx-background-color: #007bff;"));
        
        editButton.setOnMouseEntered(e -> editButton.setStyle("-fx-background-color: #0056b3;"));
        editButton.setOnMouseExited(e -> editButton.setStyle("-fx-background-color: #007bff;"));
        
        deleteButton.setOnMouseEntered(e -> deleteButton.setStyle("-fx-background-color: #c82333;"));
        deleteButton.setOnMouseExited(e -> deleteButton.setStyle("-fx-background-color: #dc3545;"));
    }
    
    private void setupTableSelection() {
        rendezVousTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                boolean selected = newSelection != null;
                editButton.setDisable(!selected);
                deleteButton.setDisable(!selected);
            }
        );
        
        editButton.setDisable(true);
        deleteButton.setDisable(true);
    }
    
    private void loadRendezVous() {
        try {
            List<RendezVous> rendezVous = RendezVousDAO.getAllRendezVous();
            rendezVousList.clear();
            rendezVousList.addAll(rendezVous);
            
            updateStatusLabel("Données chargées avec succès");
            updateCountLabel(rendezVous.size());
            
        } catch (Exception e) {
            showError("Erreur de chargement", "Impossible de charger les rendez-vous: " + e.getMessage());
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
            
            updateStatusLabel("Recherche terminée");
            updateCountLabel(results.size());
            
        } catch (Exception e) {
            showError("Erreur de recherche", "Impossible d'effectuer la recherche: " + e.getMessage());
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
    
    // Set current user
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            statusLabel.setText("Connecté en tant que: " + user.getNomComplet());
        }
    }
}
