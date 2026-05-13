package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaView;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.example.model.RendezVous;
import org.example.model.User;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for Teleconsultation Interface
 */
public class TeleconsultationController implements Initializable {
    
    @FXML private VBox mainContainer;
    @FXML private HBox videoContainer;
    @FXML private MediaView localVideoView;
    @FXML private MediaView remoteVideoView;
    @FXML private Label titleLabel;
    @FXML private Label statusLabel;
    @FXML private Label participantLabel;
    @FXML private Button toggleAudioButton;
    @FXML private Button toggleVideoButton;
    @FXML private Button shareScreenButton;
    @FXML private Button endCallButton;
    @FXML private Button chatButton;
    @FXML private HBox controlsContainer;
    
    private RendezVous rendezVous;
    private User currentUser;
    private boolean isSpecialist;
    private Stage currentStage;
    
    private boolean isAudioEnabled = true;
    private boolean isVideoEnabled = true;
    private boolean isScreenSharing = false;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupUI();
        setupEventHandlers();
    }
    
    private void setupUI() {
        // Set initial UI state
        updateStatusLabel("Connexion en cours...");
        updateButtonStates();
    }
    
    private void setupEventHandlers() {
        toggleAudioButton.setOnAction(e -> toggleAudio());
        toggleVideoButton.setOnAction(e -> toggleVideo());
        shareScreenButton.setOnAction(e -> toggleScreenShare());
        endCallButton.setOnAction(e -> endCall());
        chatButton.setOnAction(e -> openChat());
    }
    
    public void setRendezVous(RendezVous rendezVous) {
        this.rendezVous = rendezVous;
        updateUI();
    }
    
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
    
    public void setIsSpecialist(boolean isSpecialist) {
        this.isSpecialist = isSpecialist;
        updateUI();
    }
    
    public void setStage(Stage stage) {
        this.currentStage = stage;
    }
    
    private void updateUI() {
        if (rendezVous != null && currentUser != null) {
            String participantName = isSpecialist ? rendezVous.getPatientNom() : rendezVous.getSpecialisteNom();
            titleLabel.setText("Téléconsultation - " + participantName);
            participantLabel.setText("Avec: " + participantName);
            
            // Adjust UI based on role
            if (isSpecialist) {
                endCallButton.setText("Terminer la consultation");
            } else {
                endCallButton.setText("Quitter l'appel");
            }
        }
    }
    
    private void toggleAudio() {
        isAudioEnabled = !isAudioEnabled;
        updateAudioButton();
        
        // TODO: Implement actual audio toggle with WebRTC
        System.out.println("Audio " + (isAudioEnabled ? "activé" : "désactivé"));
    }
    
    private void toggleVideo() {
        isVideoEnabled = !isVideoEnabled;
        updateVideoButton();
        
        // TODO: Implement actual video toggle with WebRTC
        System.out.println("Vidéo " + (isVideoEnabled ? "activée" : "désactivée"));
    }
    
    private void toggleScreenShare() {
        isScreenSharing = !isScreenSharing;
        updateScreenShareButton();
        
        // TODO: Implement actual screen sharing with WebRTC
        System.out.println("Partage d'écran " + (isScreenSharing ? "activé" : "désactivé"));
    }
    
    private void openChat() {
        // TODO: Implement chat interface
        System.out.println("Ouverture du chat...");
    }
    
    private void endCall() {
        if (isSpecialist) {
            // Specialist ends the consultation
            rendezVous.endSession();
            updateStatusLabel("Consultation terminée");
            
            // TODO: Update database
            
            // Show confirmation
            showAlert("Consultation terminée", "La téléconsultation a été terminée avec succès.");
        } else {
            // Patient just leaves the call
            updateStatusLabel("Vous avez quitté l'appel");
        }
        
        // Close the window after a short delay
        closeWindow();
    }
    
    private void updateStatusLabel(String status) {
        statusLabel.setText(status);
    }
    
    private void updateButtonStates() {
        updateAudioButton();
        updateVideoButton();
        updateScreenShareButton();
    }
    
    private void updateAudioButton() {
        if (isAudioEnabled) {
            toggleAudioButton.setText("🎤 Audio");
            toggleAudioButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
        } else {
            toggleAudioButton.setText("🔇 Audio");
            toggleAudioButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        }
    }
    
    private void updateVideoButton() {
        if (isVideoEnabled) {
            toggleVideoButton.setText("📹 Vidéo");
            toggleVideoButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
        } else {
            toggleVideoButton.setText("📹 Vidéo (désactivée)");
            toggleVideoButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        }
    }
    
    private void updateScreenShareButton() {
        if (isScreenSharing) {
            shareScreenButton.setText("🖥️ Arrêter partage");
            shareScreenButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        } else {
            shareScreenButton.setText("🖥️ Partager écran");
            shareScreenButton.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black;");
        }
    }
    
    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void closeWindow() {
        // Close window after 2 seconds
        javafx.application.Platform.runLater(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (currentStage != null) {
                currentStage.close();
            }
        });
    }
    
    // Simulate connection established
    public void simulateConnectionEstablished() {
        updateStatusLabel("Connecté");
        // TODO: Start WebRTC connection
    }
    
    // Simulate participant joined
    public void simulateParticipantJoined() {
        updateStatusLabel("Participant connecté");
        // TODO: Handle remote stream
    }
    
    // Simulate participant left
    public void simulateParticipantLeft() {
        updateStatusLabel("Participant déconnecté");
        if (!isSpecialist) {
            // If patient disconnects, specialist might want to wait or end call
            showAlert("Patient déconnecté", "Le patient a quitté la téléconsultation.");
        }
    }
}
