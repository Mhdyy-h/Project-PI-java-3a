package org.example.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.model.User;
import org.example.service.FaceRecognitionService;
import org.example.service.NavigationService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Controller for Face ID registration and recognition.
 * Handles camera capture, face registration (5-6 photos), and face recognition.
 */
public class FaceIDController {

    @FXML private ImageView cameraView;
    @FXML private Label instructionLabel;
    @FXML private Label statusLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Button captureButton;
    @FXML private Button cancelButton;
    @FXML private VBox registrationProgressBox;
    @FXML private Label photoCountLabel;

    private FaceRecognitionService faceService;
    private NavigationService navigationService;
    private User currentUser;
    private String userEmail;
    private boolean isRegistrationMode;
    private int capturedPhotosCount;
    private static final int REQUIRED_PHOTOS = 5;

    // Camera simulation (in real implementation, use OpenCV or Webcam Capture API)
    private ScheduledExecutorService cameraExecutor;
    private boolean isCameraRunning;

    @FXML
    public void initialize() {
        faceService = FaceRecognitionService.getInstance();
        navigationService = NavigationService.getInstance();
    }

    /**
     * Initialize the controller for Face ID flow
     * @param email The user's email
     * @param user The user object (can be null if not found yet)
     */
    public void initFaceIDFlow(String email, User user) {
        this.userEmail = email;
        this.currentUser = user;

        if (user == null) {
            // Try to get user by email
            this.currentUser = faceService.getUserByEmail(email);
        }

        if (currentUser == null) {
            showError("Utilisateur non trouvé pour l'email: " + email);
            return;
        }

        // Check if Face ID is already registered
        boolean isRegistered = faceService.isFaceIdRegistered(currentUser.getId());

        if (isRegistered) {
            // Recognition mode
            startRecognitionMode();
        } else {
            // Registration mode
            startRegistrationMode();
        }
    }

    private void startRegistrationMode() {
        this.isRegistrationMode = true;
        this.capturedPhotosCount = 0;

        instructionLabel.setText("Enregistrement Face ID");
        statusLabel.setText("Positionnez votre visage devant la caméra\nCliquez sur 'Capturer' pour prendre une photo");
        photoCountLabel.setText("0 / " + REQUIRED_PHOTOS + " photos");
        registrationProgressBox.setVisible(true);
        captureButton.setText("📷 Capturer");

        startCamera();
    }

    private void startRecognitionMode() {
        this.isRegistrationMode = false;

        instructionLabel.setText("Reconnaissance Face ID");
        statusLabel.setText("Positionnez votre visage devant la caméra\nCliquez sur 'Vérifier' pour vous connecter");
        registrationProgressBox.setVisible(false);
        captureButton.setText("🔍 Vérifier");

        startCamera();
    }

    private void startCamera() {
        isCameraRunning = true;

        // Simulate camera feed with a placeholder
        // In real implementation, this would capture from webcam
        cameraExecutor = Executors.newSingleThreadScheduledExecutor();
        cameraExecutor.scheduleAtFixedRate(() -> {
            if (isCameraRunning) {
                Platform.runLater(() -> {
                    // Generate a simulated camera frame
                    // In production: capture from actual webcam
                    updateCameraFrame();
                });
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    private void updateCameraFrame() {
        // Create a placeholder image showing camera area
        // In real implementation: capture from webcam
        WritableImage image = new WritableImage(640, 480);
        cameraView.setImage(image);
    }

    @FXML
    private void handleCapture() {
        if (isRegistrationMode) {
            captureRegistrationPhoto();
        } else {
            performRecognition();
        }
    }

    private void captureRegistrationPhoto() {
        // Simulate capturing a photo
        // In real implementation: capture actual frame from webcam
        BufferedImage capturedImage = captureFrameFromCamera();

        if (capturedImage == null) {
            statusLabel.setText("❌ Erreur de capture. Réessayez.");
            return;
        }

        try {
            String photoPath = faceService.saveFacePhoto(
                currentUser.getId(),
                capturedImage,
                capturedPhotosCount + 1
            );

            capturedPhotosCount++;
            photoCountLabel.setText(capturedPhotosCount + " / " + REQUIRED_PHOTOS + " photos");
            progressBar.setProgress((double) capturedPhotosCount / REQUIRED_PHOTOS);

            if (capturedPhotosCount >= REQUIRED_PHOTOS) {
                statusLabel.setText("✅ Enregistrement terminé!");
                finishRegistration();
            } else {
                statusLabel.setText("✓ Photo " + capturedPhotosCount + " capturée.\nContinuez avec la photo suivante...");
            }

        } catch (IOException e) {
            statusLabel.setText("❌ Erreur sauvegarde: " + e.getMessage());
        }
    }

    private void performRecognition() {
        statusLabel.setText("🔍 Analyse du visage en cours...");

        // Simulate face recognition
        // In real implementation: capture frame and compare with registered faces
        BufferedImage capturedImage = captureFrameFromCamera();

        if (capturedImage == null) {
            statusLabel.setText("❌ Erreur de capture. Réessayez.");
            return;
        }

        // Compare faces
        boolean match = faceService.compareFaces(currentUser.getId(), capturedImage);

        if (match) {
            statusLabel.setText("✅ Visage reconnu! Connexion en cours...");
            stopCamera();

            // Log successful Face ID login
            logFaceIDLogin();

            // Navigate to dashboard
            Platform.runLater(() -> {
                navigationService.navigateToDashboard(cancelButton.getScene().getRoot(), currentUser);
                closeWindow();
            });
        } else {
            statusLabel.setText("❌ Visage non reconnu.\nRéessayez ou utilisez le mot de passe.");
        }
    }

    private void finishRegistration() {
        captureButton.setDisable(true);

        // Wait 1.5 seconds then close
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1.5), e -> {
            stopCamera();

            // Now perform recognition (login)
            Platform.runLater(() -> {
                navigationService.navigateToDashboard(cancelButton.getScene().getRoot(), currentUser);
                closeWindow();
            });
        }));
        timeline.play();
    }

    private BufferedImage captureFrameFromCamera() {
        // In real implementation: capture from actual webcam
        // For now, create a placeholder image
        return new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);
    }

    private void logFaceIDLogin() {
        // Log to activity log
        org.example.dao.ActivityLogDAO.insertLog(new org.example.model.ActivityLog(
            currentUser.getId(),
            currentUser.getNomComplet(),
            currentUser.getEmail(),
            currentUser.getRoles() != null ? currentUser.getRoles() : "",
            "Connexion Face ID"
        ));
    }

    @FXML
    private void handleCancel() {
        stopCamera();
        closeWindow();
    }

    private void stopCamera() {
        isCameraRunning = false;
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        statusLabel.setText(message);
        captureButton.setDisable(true);
    }

    /**
     * Static method to open the Face ID dialog
     */
    public static void openFaceIDDialog(String email, User user, javafx.scene.Node source) {
        try {
            FXMLLoader loader = new FXMLLoader(
                FaceIDController.class.getResource("/view/faceid_dialog.fxml")
            );
            VBox root = loader.load();

            FaceIDController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Face ID - BioSync");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            if (source != null && source.getScene() != null) {
                dialogStage.initOwner(source.getScene().getWindow());
            }
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            // Initialize after scene is shown
            Platform.runLater(() -> controller.initFaceIDFlow(email, user));

            dialogStage.show();

        } catch (IOException e) {
            System.err.println("Error opening Face ID dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
