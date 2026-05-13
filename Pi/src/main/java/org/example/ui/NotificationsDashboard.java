package org.example.ui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.example.dao.RendezVousDAO;
import org.example.dao.UserDAO;
import org.example.model.RendezVous;
import org.example.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * JavaFX Notifications Dashboard for BioSync
 * Desktop application to manage and send notifications
 */
public class NotificationsDashboard extends Application {
    
    private TableView<NotificationItem> notificationsTable;
    private ObservableList<NotificationItem> notificationsList = FXCollections.observableArrayList();
    private ComboBox<String> notificationTypeComboBox;
    private TextArea messageTextArea;
    private ComboBox<User> patientComboBox;
    private Label statusLabel;
    private Label countLabel;
    private TitledPane sendNotificationPane;
    private TitledPane historyPane;
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("🔔 BioSync Notifications Dashboard");
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
        loadNotifications();
        loadPatients();
    }
    
    private VBox createHeader() {
        VBox header = new VBox();
        header.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        header.setPadding(new Insets(20));
        
        Label title = new Label("🔔 BioSync Notifications Dashboard");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.valueOf("#2c3e50"));
        
        Label subtitle = new Label("Gérez les notifications pour les patients et spécialistes");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setTextFill(Color.valueOf("#7f8c8d"));
        
        header.getChildren().addAll(title, subtitle);
        return header;
    }
    
    private VBox createMainContent() {
        VBox mainContent = new VBox(15);
        mainContent.setPadding(new Insets(20));
        mainContent.setStyle("-fx-background-color: #f5f7fa;");
        
        // Create scrollable content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        VBox scrollableContent = new VBox(20);
        
        // Top section - Send notification (collapsible)
        TitledPane sendNotificationPane = new TitledPane();
        sendNotificationPane.setText("📤 Envoyer une Nouvelle Notification");
        sendNotificationPane.setExpanded(true);
        sendNotificationPane.setAnimated(true);
        sendNotificationPane.setStyle("-fx-background-color: white; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 8); -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        sendNotificationPane.setContent(createEnhancedSendNotificationPanel());
        
        // Bottom section - Notifications history (collapsible)
        TitledPane historyPane = new TitledPane();
        historyPane.setText("📋 Historique des Notifications");
        historyPane.setExpanded(true);
        historyPane.setAnimated(true);
        historyPane.setStyle("-fx-background-color: white; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 8); -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        historyPane.setContent(createEnhancedNotificationsPanel());
        
        scrollableContent.getChildren().addAll(sendNotificationPane, historyPane);
        scrollPane.setContent(scrollableContent);
        
        // Make scroll pane grow
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        mainContent.getChildren().add(scrollPane);
        return mainContent;
    }
    
    private VBox createEnhancedNotificationsPanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(20));
        
        // Statistics bar at top
        HBox statsBar = new HBox(30);
        statsBar.setAlignment(Pos.CENTER);
        statsBar.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2); -fx-background-radius: 12; -fx-padding: 20 30 20 30; -fx-effect: dropshadow(gaussian, rgba(102,126,234,0.3), 8, 0, 0, 4);");
        
        Label totalLabel = new Label("📊 Total: 0");
        totalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        totalLabel.setTextFill(Color.WHITE);
        
        Label sentLabel = new Label("✅ Envoyées: 0");
        sentLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        sentLabel.setTextFill(Color.valueOf("#a8ff78"));
        
        Label pendingLabel = new Label("⏳ En attente: 0");
        pendingLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        pendingLabel.setTextFill(Color.valueOf("#ffeb3b"));
        
        Button refreshButton = new Button("🔄 Actualiser");
        refreshButton.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 20 10 20; -fx-cursor: hand; -fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 1px;");
        refreshButton.setOnAction(e -> loadNotifications());
        
        statsBar.getChildren().addAll(totalLabel, sentLabel, pendingLabel, refreshButton);
        
        // Enhanced table with better sizing
        notificationsTable = new TableView<>();
        notificationsTable.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-font-size: 14px; -fx-cell-size: 40;");
        notificationsTable.setPrefHeight(500);
        notificationsTable.setMinHeight(400);
        notificationsTable.setMaxHeight(Double.MAX_VALUE);
        notificationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Enhanced columns with better sizing
        TableColumn<NotificationItem, String> typeColumn = new TableColumn<>("📝 Type");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeColumn.setStyle("-fx-font-weight: bold; -fx-background-color: #f8f9fa; -fx-alignment: CENTER;");
        typeColumn.setPrefWidth(150);
        typeColumn.setMinWidth(120);
        
        TableColumn<NotificationItem, String> recipientColumn = new TableColumn<>("👤 Destinataire");
        recipientColumn.setCellValueFactory(new PropertyValueFactory<>("recipient"));
        recipientColumn.setStyle("-fx-font-weight: bold; -fx-background-color: #f8f9fa;");
        recipientColumn.setPrefWidth(250);
        recipientColumn.setMinWidth(200);
        
        TableColumn<NotificationItem, String> messageColumn = new TableColumn<>("💬 Message");
        messageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        messageColumn.setStyle("-fx-font-weight: bold; -fx-background-color: #f8f9fa;");
        messageColumn.setPrefWidth(400);
        messageColumn.setMinWidth(300);
        
        TableColumn<NotificationItem, String> statusColumn = new TableColumn<>("📊 Statut");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setStyle("-fx-font-weight: bold; -fx-background-color: #f8f9fa; -fx-alignment: CENTER;");
        statusColumn.setPrefWidth(150);
        statusColumn.setMinWidth(120);
        
        TableColumn<NotificationItem, String> timestampColumn = new TableColumn<>("📅 Date");
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        timestampColumn.setStyle("-fx-font-weight: bold; -fx-background-color: #f8f9fa; -fx-alignment: CENTER;");
        timestampColumn.setPrefWidth(180);
        timestampColumn.setMinWidth(150);
        
        notificationsTable.getColumns().addAll(typeColumn, recipientColumn, messageColumn, statusColumn, timestampColumn);
        notificationsTable.setItems(notificationsList);
        
        panel.getChildren().addAll(statsBar, notificationsTable);
        return panel;
    }
    
    private VBox createEnhancedSendNotificationPanel() {
        VBox panel = new VBox(25);
        panel.setPadding(new Insets(25));
        
        // Header section with better styling
        HBox headerSection = new HBox(20);
        headerSection.setAlignment(Pos.CENTER_LEFT);
        headerSection.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2); -fx-background-radius: 12; -fx-padding: 25 30 25 30; -fx-effect: dropshadow(gaussian, rgba(102,126,234,0.3), 8, 0, 0, 4);");
        
        VBox titleBox = new VBox(5);
        Label title = new Label("📤 Nouvelle Notification");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.WHITE);
        
        Label subtitle = new Label("Envoyez des SMS ou Emails personnalisés");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setTextFill(Color.valueOf("#e8eaf6"));
        
        titleBox.getChildren().addAll(title, subtitle);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label quickTip = new Label("💡 Conseil: Personnalisez vos messages pour un meilleur impact");
        quickTip.setFont(Font.font("Arial", 12));
        quickTip.setTextFill(Color.valueOf("#ffd54f"));
        quickTip.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 8; -fx-padding: 8 15 8 15;");
        
        headerSection.getChildren().addAll(titleBox, spacer, quickTip);
        
        // Form section with better layout
        GridPane formGrid = new GridPane();
        formGrid.setHgap(20);
        formGrid.setVgap(20);
        formGrid.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 12; -fx-padding: 25;");
        
        // Notification type
        VBox typeBox = new VBox(8);
        Label typeLabel = new Label("📝 Type de notification");
        typeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        typeLabel.setTextFill(Color.valueOf("#2c3e50"));
        
        notificationTypeComboBox = new ComboBox<>();
        notificationTypeComboBox.getItems().addAll(
            "📅 Rappel RDV", "✅ Confirmation", "❌ Annulation", "ℹ️ Information", "🚨 Urgence"
        );
        notificationTypeComboBox.setPromptText("👆 Sélectionner le type");
        notificationTypeComboBox.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e0; -fx-border-radius: 8; -fx-font-size: 14px; -fx-padding: 12 15 12 15; -fx-border-width: 2px;");
        notificationTypeComboBox.setPrefWidth(300);
        
        typeBox.getChildren().addAll(typeLabel, notificationTypeComboBox);
        
        // Patient selection
        VBox patientBox = new VBox(8);
        Label patientLabel = new Label("👤 Patient destinataire");
        patientLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        patientLabel.setTextFill(Color.valueOf("#2c3e50"));
        
        patientComboBox = new ComboBox<>();
        patientComboBox.setPromptText("👆 Sélectionner un patient");
        patientComboBox.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e0; -fx-border-radius: 8; -fx-font-size: 14px; -fx-padding: 12 15 12 15; -fx-border-width: 2px;");
        patientComboBox.setPrefWidth(300);
        
        patientBox.getChildren().addAll(patientLabel, patientComboBox);
        
        // Message area (spans full width)
        VBox messageBox = new VBox(8);
        Label messageLabel = new Label("💬 Message personnalisé");
        messageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        messageLabel.setTextFill(Color.valueOf("#2c3e50"));
        
        messageTextArea = new TextArea();
        messageTextArea.setPromptText("✍️ Rédigez votre message ici...\n\nExemples:\n• Bonjour [NomPatient], votre rendez-vous est confirmé pour le [Date] à [Heure].\n• Rappel: Vous avez un rendez-vous demain à [Heure] avec [NomMédecin].\n• Votre rendez-vous du [Date] a été annulé. Merci de contacter le secrétariat.");
        messageTextArea.setPrefHeight(150);
        messageTextArea.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e0; -fx-border-radius: 8; -fx-font-size: 14px; -fx-padding: 15 18 15 18; -fx-wrap-text: true; -fx-border-width: 2px;");
        
        messageBox.getChildren().addAll(messageLabel, messageTextArea);
        
        // Add to grid
        formGrid.add(typeBox, 0, 0);
        formGrid.add(patientBox, 1, 0);
        formGrid.add(messageBox, 0, 1, 2, 1);
        
        // Enhanced action buttons
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 25; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0, 0, 3);");
        
        Button sendSmsButton = new Button("📱 Envoyer SMS");
        sendSmsButton.setStyle("-fx-background-color: linear-gradient(to right, #27ae60, #2ecc71); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 15 30 15 30; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(39,174,96,0.4), 8, 0, 0, 4); -fx-font-size: 16px; -fx-border-width: 0;");
        sendSmsButton.setOnAction(e -> sendNotification("SMS"));
        sendSmsButton.setOnMouseEntered(e -> sendSmsButton.setStyle("-fx-background-color: linear-gradient(to right, #229954, #27ae60); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 15 30 15 30; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(39,174,96,0.6), 10, 0, 0, 5); -fx-font-size: 16px; -fx-border-width: 0; -fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
        sendSmsButton.setOnMouseExited(e -> sendSmsButton.setStyle("-fx-background-color: linear-gradient(to right, #27ae60, #2ecc71); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 15 30 15 30; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(39,174,96,0.4), 8, 0, 0, 4); -fx-font-size: 16px; -fx-border-width: 0; -fx-scale-x: 1.0; -fx-scale-y: 1.0;"));
        
        Button sendEmailButton = new Button("📧 Envoyer Email");
        sendEmailButton.setStyle("-fx-background-color: linear-gradient(to right, #3498db, #5dade2); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 15 30 15 30; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(52,152,219,0.4), 8, 0, 0, 4); -fx-font-size: 16px; -fx-border-width: 0;");
        sendEmailButton.setOnAction(e -> sendNotification("Email"));
        sendEmailButton.setOnMouseEntered(e -> sendEmailButton.setStyle("-fx-background-color: linear-gradient(to right, #2980b9, #3498db); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 15 30 15 30; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(52,152,219,0.6), 10, 0, 0, 5); -fx-font-size: 16px; -fx-border-width: 0; -fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
        sendEmailButton.setOnMouseExited(e -> sendEmailButton.setStyle("-fx-background-color: linear-gradient(to right, #3498db, #5dade2); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 15 30 15 30; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(52,152,219,0.4), 8, 0, 0, 4); -fx-font-size: 16px; -fx-border-width: 0; -fx-scale-x: 1.0; -fx-scale-y: 1.0;"));
        
        Button clearButton = new Button("🗑️ Effacer");
        clearButton.setStyle("-fx-background-color: linear-gradient(to right, #e74c3c, #ec7063); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 15 30 15 30; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(231,76,60,0.4), 8, 0, 0, 4); -fx-font-size: 16px; -fx-border-width: 0;");
        clearButton.setOnAction(e -> clearForm());
        clearButton.setOnMouseEntered(e -> clearButton.setStyle("-fx-background-color: linear-gradient(to right, #c0392b, #e74c3c); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 15 30 15 30; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(231,76,60,0.6), 10, 0, 0, 5); -fx-font-size: 16px; -fx-border-width: 0; -fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
        clearButton.setOnMouseExited(e -> clearButton.setStyle("-fx-background-color: linear-gradient(to right, #e74c3c, #ec7063); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 15 30 15 30; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(231,76,60,0.4), 8, 0, 0, 4); -fx-font-size: 16px; -fx-border-width: 0; -fx-scale-x: 1.0; -fx-scale-y: 1.0;"));
        
        buttonBox.getChildren().addAll(sendSmsButton, sendEmailButton, clearButton);
        
        panel.getChildren().addAll(headerSection, formGrid, buttonBox);
        
        return panel;
    }
    
    private HBox createStatusBar() {
        HBox statusBar = new HBox();
        statusBar.setStyle("-fx-background-color: #34495e; -fx-padding: 10 20 10 20;");
        statusBar.setAlignment(Pos.CENTER_LEFT);
        
        countLabel = new Label("Total: 0 notifications");
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
    
    private void loadNotifications() {
        try {
            notificationsList.clear();
            
            // Load sample notifications
            List<RendezVous> rendezVousList = RendezVousDAO.getAllRendezVous();
            
            for (RendezVous rdv : rendezVousList) {
                if ("confirmé".equals(rdv.getStatut())) {
                    NotificationItem notification = new NotificationItem(
                        "Confirmation",
                        rdv.getPatientNom(),
                        "Votre rendez-vous du " + rdv.getFormattedDateHeure() + " est confirmé",
                        "Envoyé",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    );
                    notificationsList.add(notification);
                }
            }
            
            // Add some sample notifications
            notificationsList.add(new NotificationItem(
                "Rappel RDV", "Patient Test", "Rappel: Votre rendez-vous est demain à 14:00", "Envoyé", 
                LocalDateTime.now().minusHours(2).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            ));
            
            notificationsList.add(new NotificationItem(
                "Information", "Tous les patients", "Nouveaux horaires disponibles pour les consultations", "Envoyé",
                LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            ));
            
            countLabel.setText("Total: " + notificationsList.size() + " notifications");
            statusLabel.setText("Chargé avec succès");
            
        } catch (Exception e) {
            statusLabel.setText("Erreur: " + e.getMessage());
        }
    }
    
    private void loadPatients() {
        try {
            List<User> patients = UserDAO.getAllUsers();
            patientComboBox.getItems().clear();
            
            for (User patient : patients) {
                if (patient.isPatient()) {
                    patientComboBox.getItems().add(patient);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error loading patients: " + e.getMessage());
        }
    }
    
    private void sendNotification(String type) {
        try {
            String notificationType = notificationTypeComboBox.getValue();
            User selectedPatient = patientComboBox.getValue();
            String message = messageTextArea.getText();
            
            if (notificationType == null || selectedPatient == null || message.trim().isEmpty()) {
                showAlert("Erreur", "Veuillez remplir tous les champs", Alert.AlertType.ERROR);
                return;
            }
            
            // Simulate sending notification
            statusLabel.setText("Envoi en cours...");
            
            // Add to notifications list
            NotificationItem notification = new NotificationItem(
                notificationType,
                selectedPatient.getNomComplet(),
                message,
                "Envoyé",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );
            
            notificationsList.add(0, notification);
            
            // Clear form
            notificationTypeComboBox.getSelectionModel().clearSelection();
            patientComboBox.getSelectionModel().clearSelection();
            messageTextArea.clear();
            
            countLabel.setText("Total: " + notificationsList.size() + " notifications");
            statusLabel.setText("Notification " + type + " envoyée avec succès!");
            
            showAlert("Succès", "Notification " + type + " envoyée à " + selectedPatient.getNomComplet(), Alert.AlertType.INFORMATION);
            
        } catch (Exception e) {
            statusLabel.setText("Erreur: " + e.getMessage());
            showAlert("Erreur", "Impossible d'envoyer la notification: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void clearForm() {
        notificationTypeComboBox.getSelectionModel().clearSelection();
        patientComboBox.getSelectionModel().clearSelection();
        messageTextArea.clear();
        statusLabel.setText("Formulaire effacé");
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Notification item model
    public static class NotificationItem {
        private String type;
        private String recipient;
        private String message;
        private String status;
        private String timestamp;
        
        public NotificationItem(String type, String recipient, String message, String status, String timestamp) {
            this.type = type;
            this.recipient = recipient;
            this.message = message;
            this.status = status;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getType() { return type; }
        public String getRecipient() { return recipient; }
        public String getMessage() { return message; }
        public String getStatus() { return status; }
        public String getTimestamp() { return timestamp; }
    }
}
