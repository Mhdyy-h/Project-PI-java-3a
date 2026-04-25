package org.example.controller;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
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
import javafx.stage.Window;
import javafx.util.Duration;
import org.example.model.User;
import org.example.service.FaceRecognitionService;
import org.example.service.NavigationService;

import java.awt.image.BufferedImage;
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
    private Stage dialogStage; // Store reference to stage

    // Webcam
    private Webcam webcam;
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

        try {
            // Initialize webcam
            webcam = Webcam.getDefault();
            if (webcam == null) {
                statusLabel.setText("❌ Aucune caméra détectée");
                return;
            }

            webcam.setViewSize(WebcamResolution.VGA.getSize());
            webcam.open();

            // Start camera feed
            cameraExecutor = Executors.newSingleThreadScheduledExecutor();
            cameraExecutor.scheduleAtFixedRate(() -> {
                if (isCameraRunning && webcam.isOpen()) {
                    BufferedImage frame = webcam.getImage();
                    if (frame != null) {
                        Platform.runLater(() -> {
                            Image image = SwingFXUtils.toFXImage(frame, null);
                            cameraView.setImage(image);
                        });
                    }
                }
            }, 0, 33, TimeUnit.MILLISECONDS); // ~30 FPS

            statusLabel.setText("✅ Caméra active");

        } catch (Exception e) {
            statusLabel.setText("❌ Erreur caméra: " + e.getMessage());
            System.err.println("Camera error: " + e.getMessage());
        }
    }

    private void updateCameraFrame() {
        // Not used with real webcam
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
        // Capture real photo from webcam
        if (webcam == null || !webcam.isOpen()) {
            statusLabel.setText("❌ Caméra non disponible");
            return;
        }

        BufferedImage frame = webcam.getImage();
        if (frame == null) {
            statusLabel.setText("❌ Erreur de capture");
            return;
        }

        // Check if face is detected
        if (!faceService.detectFace(frame)) {
            statusLabel.setText("❌ Aucun visage détecté.\nPositionnez votre visage devant la caméra.");
            return;
        }

        try {
            String photoPath = faceService.saveFacePhoto(currentUser.getId(), capturedPhotosCount + 1, frame);
            capturedPhotosCount++;
            photoCountLabel.setText(capturedPhotosCount + " / " + REQUIRED_PHOTOS + " photos");
            progressBar.setProgress((double) capturedPhotosCount / REQUIRED_PHOTOS);

            if (capturedPhotosCount >= REQUIRED_PHOTOS) {
                statusLabel.setText("✅ Enregistrement terminé!");
                finishRegistration();
            } else {
                statusLabel.setText("✓ Photo " + capturedPhotosCount + " capturée (visage détecté).\nContinuez avec la photo suivante...");
            }
        } catch (Exception e) {
            statusLabel.setText("❌ Erreur sauvegarde: " + e.getMessage());
        }
    }

    private void performRecognition() {
        statusLabel.setText("🔍 Analyse du visage en cours...");

        // Capture frame from webcam
        if (webcam == null || !webcam.isOpen()) {
            statusLabel.setText("❌ Caméra non disponible");
            return;
        }

        final BufferedImage capturedFrame = webcam.getImage();
        if (capturedFrame == null) {
            statusLabel.setText("❌ Erreur de capture");
            return;
        }

        // Simulate processing delay and face comparison
        Timeline processingDelay = new Timeline(new KeyFrame(Duration.seconds(1.5), e -> {
            // Check if there's a face in the captured image and compare with registered faces
            boolean match = faceService.compareFaces(currentUser.getId(), capturedFrame);

            if (match) {
                statusLabel.setText("✅ Visage reconnu! Connexion en cours...");
                stopCamera();
                logFaceIDLogin();

                Platform.runLater(() -> {
                    // Navigate using the owner stage (main window), not the dialog
                    if (dialogStage != null && dialogStage.getOwner() != null) {
                        Window ownerWindow = dialogStage.getOwner();
                        if (ownerWindow instanceof Stage) {
                            Stage ownerStage = (Stage) ownerWindow;
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dashboard.fxml"));
                                javafx.scene.Parent root = loader.load();
                                Object ctrl = loader.getController();
                                if (ctrl instanceof org.example.controller.AdminController) {
                                    ((org.example.controller.AdminController) ctrl).setUser(currentUser);
                                }
                                ownerStage.setScene(new javafx.scene.Scene(root, 1100, 700));
                                ownerStage.setTitle("BioSync - Administration");
                                ownerStage.centerOnScreen();
                            } catch (Exception ex) {
                                System.err.println("Navigation error: " + ex.getMessage());
                            }
                        }
                    }
                    closeWindow();
                });
            } else {
                statusLabel.setText("❌ Visage non reconnu ou non enregistré.\nRéessayez ou utilisez le mot de passe.");
            }
        }));
        processingDelay.play();
    }

    private void finishRegistration() {
        captureButton.setDisable(true);

        // Wait 1.5 seconds then close and navigate
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1.5), e -> {
            stopCamera();
            logFaceIDLogin();

            // Navigate to dashboard using owner stage
            if (dialogStage != null && dialogStage.getOwner() != null) {
                Window ownerWindow = dialogStage.getOwner();
                if (ownerWindow instanceof Stage) {
                    Stage ownerStage = (Stage) ownerWindow;
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dashboard.fxml"));
                        javafx.scene.Parent root = loader.load();
                        Object ctrl = loader.getController();
                        if (ctrl instanceof org.example.controller.AdminController) {
                            ((org.example.controller.AdminController) ctrl).setUser(currentUser);
                        }
                        ownerStage.setScene(new javafx.scene.Scene(root, 1100, 700));
                        ownerStage.setTitle("BioSync - Administration");
                        ownerStage.centerOnScreen();
                    } catch (Exception ex) {
                        System.err.println("Navigation error: " + ex.getMessage());
                    }
                }
            }

            closeWindow();
        }));
        timeline.play();
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
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
    }

    private void closeWindow() {
        if (dialogStage != null) {
            dialogStage.close();
        } else if (cancelButton != null && cancelButton.getScene() != null) {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        }
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
            controller.dialogStage = dialogStage;

        } catch (IOException e) {
            System.err.println("Error opening Face ID dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
