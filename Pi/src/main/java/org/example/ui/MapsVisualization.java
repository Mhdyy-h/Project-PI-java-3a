package org.example.ui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
import java.util.Map;
import java.util.HashMap;

/**
 * JavaFX Maps Visualization for BioSync
 * Desktop application to visualize appointments on a map
 */
public class MapsVisualization extends Application {
    
    private Canvas mapCanvas;
    private TableView<LocationItem> locationsTable;
    private ObservableList<LocationItem> locationsList = FXCollections.observableArrayList();
    private ComboBox<String> filterComboBox;
    private ComboBox<Specialiste> specialisteComboBox;
    private Label statusLabel;
    private Label countLabel;
    private TextArea mapInfoTextArea;
    private ScrollPane mapScrollPane;
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("🗺️ BioSync Maps Visualization");
        primaryStage.setWidth(1400);
        primaryStage.setHeight(900);
        
        // Create main layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8f9fa;");
        
        // Header
        root.setTop(createHeader());
        
        // Center content
        root.setCenter(createMainContent());
        
        // Status bar
        root.setBottom(createStatusBar());
        
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Load initial data
        loadLocations();
        loadSpecialistes();
        initializeMap();
    }
    
    private VBox createHeader() {
        VBox header = new VBox();
        header.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        header.setPadding(new Insets(20));
        
        Label title = new Label("🗺️ BioSync Maps Visualization");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.valueOf("#2c3e50"));
        
        Label subtitle = new Label("Visualisation géographique des rendez-vous et spécialistes");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setTextFill(Color.valueOf("#7f8c8d"));
        
        header.getChildren().addAll(title, subtitle);
        return header;
    }
    
    private HBox createMainContent() {
        HBox mainContent = new HBox(20);
        mainContent.setPadding(new Insets(20));
        
        // Left panel - Map
        VBox leftPanel = createMapPanel();
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        
        // Right panel - Locations data
        VBox rightPanel = createLocationsPanel();
        rightPanel.setPrefWidth(450);
        
        mainContent.getChildren().addAll(leftPanel, rightPanel);
        return mainContent;
    }
    
    private VBox createMapPanel() {
        VBox panel = new VBox(15);
        panel.setStyle("-fx-background-color: white; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        panel.setPadding(new Insets(20));
        
        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(10);
        
        Label title = new Label("📍 Carte Interactive");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.valueOf("#2c3e50"));
        
        Button refreshButton = new Button("🔄 Actualiser");
        refreshButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16 8 16;");
        refreshButton.setOnAction(e -> {
            loadLocations();
            refreshMap();
        });
        
        header.getChildren().addAll(title, refreshButton);
        
        // Map canvas
        mapCanvas = new Canvas(800, 600);
        mapCanvas.setStyle("-fx-border-color: #ddd; -fx-border-radius: 8;");
        
        // Wrap canvas in scroll pane for better handling
        mapScrollPane = new ScrollPane();
        mapScrollPane.setContent(mapCanvas);
        mapScrollPane.setFitToWidth(true);
        mapScrollPane.setFitToHeight(true);
        mapScrollPane.setPrefHeight(600);
        mapScrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: #ddd; -fx-border-radius: 8;");
        
        // Map info
        mapInfoTextArea = new TextArea();
        mapInfoTextArea.setPromptText("Informations sur la carte...");
        mapInfoTextArea.setPrefHeight(100);
        mapInfoTextArea.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #ddd; -fx-border-radius: 8;");
        mapInfoTextArea.setEditable(false);
        
        // Add components to panel
        panel.getChildren().addAll(header);
        panel.getChildren().add(mapScrollPane);
        panel.getChildren().add(mapInfoTextArea);
        
        return panel;
    }
    
    private VBox createLocationsPanel() {
        VBox panel = new VBox(15);
        panel.setStyle("-fx-background-color: white; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        panel.setPadding(new Insets(20));
        
        // Header
        Label title = new Label("📍 Lieux et Rendez-vous");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.valueOf("#2c3e50"));
        
        // Filters
        VBox filters = new VBox(10);
        
        HBox filterRow1 = new HBox(10);
        filterRow1.setAlignment(Pos.CENTER_LEFT);
        
        Label filterLabel = new Label("Filtre:");
        filterLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        filterComboBox = new ComboBox<>();
        filterComboBox.getItems().addAll("Tous", "Aujourd'hui", "Cette semaine", "Ce mois");
        filterComboBox.setValue("Tous");
        filterComboBox.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #ddd; -fx-border-radius: 8;");
        filterComboBox.setOnAction(e -> applyFilters());
        
        filterRow1.getChildren().addAll(filterLabel, filterComboBox);
        
        HBox filterRow2 = new HBox(10);
        filterRow2.setAlignment(Pos.CENTER_LEFT);
        
        Label specialisteLabel = new Label("Spécialiste:");
        specialisteLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        specialisteComboBox = new ComboBox<>();
        specialisteComboBox.setPromptText("Tous les spécialistes");
        specialisteComboBox.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #ddd; -fx-border-radius: 8;");
        specialisteComboBox.setOnAction(e -> applyFilters());
        
        filterRow2.getChildren().addAll(specialisteLabel, specialisteComboBox);
        
        filters.getChildren().addAll(filterRow1, filterRow2);
        
        // Table
        locationsTable = new TableView<>();
        locationsTable.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        locationsTable.setPrefHeight(400);
        
        // Columns
        TableColumn<LocationItem, String> lieuColumn = new TableColumn<>("Lieu");
        lieuColumn.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        lieuColumn.setPrefWidth(120);
        
        TableColumn<LocationItem, String> specialisteColumn = new TableColumn<>("Spécialiste");
        specialisteColumn.setCellValueFactory(new PropertyValueFactory<>("specialiste"));
        specialisteColumn.setPrefWidth(120);
        
        TableColumn<LocationItem, String> patientColumn = new TableColumn<>("Patient");
        patientColumn.setCellValueFactory(new PropertyValueFactory<>("patient"));
        patientColumn.setPrefWidth(100);
        
        TableColumn<LocationItem, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setPrefWidth(100);
        
        TableColumn<LocationItem, String> statutColumn = new TableColumn<>("Statut");
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        statutColumn.setPrefWidth(80);
        
        locationsTable.getColumns().addAll(lieuColumn, specialisteColumn, patientColumn, dateColumn, statutColumn);
        locationsTable.setItems(locationsList);
        
        // Statistics
        VBox statsBox = new VBox(10);
        statsBox.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 8; -fx-padding: 15;");
        
        Label statsTitle = new Label("📊 Statistiques");
        statsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        Label totalLabel = new Label("Total: 0 rendez-vous");
        totalLabel.setFont(Font.font("Arial", 12));
        
        Label confirmedLabel = new Label("Confirmés: 0");
        confirmedLabel.setFont(Font.font("Arial", 12));
        confirmedLabel.setTextFill(Color.valueOf("#27ae60"));
        
        Label pendingLabel = new Label("En attente: 0");
        pendingLabel.setFont(Font.font("Arial", 12));
        pendingLabel.setTextFill(Color.valueOf("#f39c12"));
        
        statsBox.getChildren().addAll(statsTitle, totalLabel, confirmedLabel, pendingLabel);
        
        panel.getChildren().addAll(title, filters, locationsTable, statsBox);
        return panel;
    }
    
    private HBox createStatusBar() {
        HBox statusBar = new HBox();
        statusBar.setStyle("-fx-background-color: #34495e; -fx-padding: 10 20 10 20;");
        statusBar.setAlignment(Pos.CENTER_LEFT);
        
        countLabel = new Label("Total: 0 localités");
        countLabel.setTextFill(Color.WHITE);
        countLabel.setFont(Font.font("Arial", 12));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        statusLabel = new Label("Prêt");
        statusLabel.setTextFill(Color.WHITE);
        statusLabel.setFont(Font.font("Arial", 12));
        
        statusBar.getChildren().addAll(countLabel, spacer, statusLabel);
        return statusBar;
    }
    
    private void initializeMap() {
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        
        // Clear canvas
        gc.clearRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());
        
        // Draw background
        gc.setFill(Color.valueOf("#e8f4f8"));
        gc.fillRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());
        
        // Draw grid/map background
        drawMapBackground(gc);
        
        // Draw location markers
        drawLocationMarkers(gc);
        
        mapInfoTextArea.setText("📍 Carte initialisée avec succès!\n" +
                              "🔍 Les marqueurs montrent les lieux des rendez-vous\n" +
                              "📊 Vert = Confirmé, Orange = En attente\n" +
                              "🎯 Utilisez les filtres pour affiner l'affichage\n" +
                              "💡 Cliquez sur les marqueurs pour voir les détails");
    }
    
    private void drawMapBackground(GraphicsContext gc) {
        // Draw simple map representation
        gc.setStroke(Color.valueOf("#d0d0d0"));
        gc.setLineWidth(1);
        
        // Draw grid
        for (int x = 0; x < mapCanvas.getWidth(); x += 50) {
            gc.strokeLine(x, 0, x, mapCanvas.getHeight());
        }
        for (int y = 0; y < mapCanvas.getHeight(); y += 50) {
            gc.strokeLine(0, y, mapCanvas.getWidth(), y);
        }
        
        // Draw main roads (simplified)
        gc.setStroke(Color.valueOf("#a0a0a0"));
        gc.setLineWidth(2);
        
        // Horizontal main road
        gc.strokeLine(0, mapCanvas.getHeight() / 2, mapCanvas.getWidth(), mapCanvas.getHeight() / 2);
        
        // Vertical main road
        gc.strokeLine(mapCanvas.getWidth() / 2, 0, mapCanvas.getWidth() / 2, mapCanvas.getHeight());
        
        // Add map title
        gc.setFill(Color.valueOf("#2c3e50"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gc.fillText("Carte de Tunis - Zones de Rendez-vous", 20, 30);
    }
    
    private void drawLocationMarkers(GraphicsContext gc) {
        // Sample locations with coordinates
        Map<String, double[]> locations = new HashMap<>();
        locations.put("Hôpital La Rabta", new double[]{200, 150});
        locations.put("Clinique El Manar", new double[]{400, 200});
        locations.put("Polyclinique Tunis", new double[]{300, 350});
        locations.put("Cabinet Médical Centre", new double[]{500, 300});
        locations.put("Hôpital Charles Nicolle", new double[]{150, 400});
        locations.put("Clinique Ariana", new double[]{600, 150});
        locations.put("Hôpital Mongi Slim", new double[]{650, 400});
        
        // Draw markers for each location in table
        for (LocationItem item : locationsList) {
            double[] coords = locations.get(item.getLieu());
            if (coords != null) {
                // Choose color based on status
                Color markerColor = "confirmé".equals(item.getStatut()) ? 
                    Color.valueOf("#27ae60") : Color.valueOf("#f39c12");
                
                // Draw marker
                gc.setFill(markerColor);
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(2);
                
                // Draw circle marker
                gc.fillOval(coords[0] - 8, coords[1] - 8, 16, 16);
                gc.strokeOval(coords[0] - 8, coords[1] - 8, 16, 16);
                
                // Draw location name
                gc.setFill(Color.BLACK);
                gc.setFont(Font.font("Arial", 10));
                gc.fillText(item.getLieu(), coords[0] + 12, coords[1] + 5);
            }
        }
        
        // Draw legend
        drawLegend(gc);
    }
    
    private void drawLegend(GraphicsContext gc) {
        int legendX = (int) mapCanvas.getWidth() - 150;
        int legendY = 50;
        
        // Legend background
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.fillRect(legendX - 10, legendY - 10, 140, 80);
        gc.strokeRect(legendX - 10, legendY - 10, 140, 80);
        
        // Legend title
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.fillText("Légende", legendX, legendY + 10);
        
        // Confirmed marker
        gc.setFill(Color.valueOf("#27ae60"));
        gc.fillOval(legendX, legendY + 20, 10, 10);
        gc.setStroke(Color.BLACK);
        gc.strokeOval(legendX, legendY + 20, 10, 10);
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 10));
        gc.fillText("Confirmé", legendX + 15, legendY + 29);
        
        // Pending marker
        gc.setFill(Color.valueOf("#f39c12"));
        gc.fillOval(legendX, legendY + 40, 10, 10);
        gc.setStroke(Color.BLACK);
        gc.strokeOval(legendX, legendY + 40, 10, 10);
        gc.setFill(Color.BLACK);
        gc.fillText("En attente", legendX + 15, legendY + 49);
    }
    
    private void loadLocations() {
        try {
            locationsList.clear();
            
            // Load rendez-vous from database
            List<RendezVous> rendezVousList = RendezVousDAO.getAllRendezVous();
            
            for (RendezVous rdv : rendezVousList) {
                LocationItem location = new LocationItem(
                    rdv.getLieu(),
                    rdv.getSpecialisteNom(),
                    rdv.getPatientNom(),
                    rdv.getFormattedDateHeure(),
                    rdv.getStatut()
                );
                locationsList.add(location);
            }
            
            // Add some sample locations if database is empty
            if (locationsList.isEmpty()) {
                locationsList.add(new LocationItem("Hôpital La Rabta", "Dr. Ben Ali", "Patient A", "30/04/2026 14:00", "confirmé"));
                locationsList.add(new LocationItem("Clinique El Manar", "Dr. Mohamed", "Patient B", "30/04/2026 16:00", "en attente"));
                locationsList.add(new LocationItem("Polyclinique Tunis", "Dr. Salah", "Patient C", "01/05/2026 09:00", "confirmé"));
                locationsList.add(new LocationItem("Cabinet Médical Centre", "Dr. Karim", "Patient D", "01/05/2026 11:00", "confirmé"));
                locationsList.add(new LocationItem("Hôpital Charles Nicolle", "Dr. Fatma", "Patient E", "02/05/2026 10:00", "en attente"));
            }
            
            countLabel.setText("Total: " + locationsList.size() + " localités");
            statusLabel.setText("Chargé avec succès");
            
        } catch (Exception e) {
            statusLabel.setText("Erreur: " + e.getMessage());
        }
    }
    
    private void loadSpecialistes() {
        try {
            List<Specialiste> specialistes = SpecialisteDAO.getAllSpecialistes();
            specialisteComboBox.getItems().clear();
            
            for (Specialiste specialiste : specialistes) {
                specialisteComboBox.getItems().add(specialiste);
            }
            
        } catch (Exception e) {
            System.err.println("Error loading specialistes: " + e.getMessage());
        }
    }
    
    private void applyFilters() {
        // Apply filters to locations table
        String filter = filterComboBox.getValue();
        Specialiste selectedSpecialiste = specialisteComboBox.getValue();
        
        // This is a simplified filter - in a real app, you'd filter database query
        String filterText = "Filtres appliqués: " + filter;
        if (selectedSpecialiste != null) {
            filterText += ", " + selectedSpecialiste.getNomDocteur();
        }
        statusLabel.setText(filterText);
    }
    
    private void refreshMap() {
        initializeMap();
        statusLabel.setText("Carte actualisée");
    }
    
    // Location item model
    public static class LocationItem {
        private String lieu;
        private String specialiste;
        private String patient;
        private String date;
        private String statut;
        
        public LocationItem(String lieu, String specialiste, String patient, String date, String statut) {
            this.lieu = lieu;
            this.specialiste = specialiste;
            this.patient = patient;
            this.date = date;
            this.statut = statut;
        }
        
        // Getters
        public String getLieu() { return lieu; }
        public String getSpecialiste() { return specialiste; }
        public String getPatient() { return patient; }
        public String getDate() { return date; }
        public String getStatut() { return statut; }
    }
}
