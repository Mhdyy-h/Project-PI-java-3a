package org.example.service;

import org.example.dao.UserDAO;
import org.example.model.User;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for Face ID recognition and registration.
 * Stores face photos in the filesystem under user_data/faces/{userId}/
 */
public class FaceRecognitionService {

    private static final String FACE_DATA_DIR = "user_data/faces";
    private static final int REGISTRATION_PHOTO_COUNT = 5;
    private static FaceRecognitionService instance;

    private FaceRecognitionService() {
        // Ensure directory exists
        createDirectoryIfNotExists();
    }

    public static synchronized FaceRecognitionService getInstance() {
        if (instance == null) {
            instance = new FaceRecognitionService();
        }
        return instance;
    }

    private void createDirectoryIfNotExists() {
        try {
            Files.createDirectories(Paths.get(FACE_DATA_DIR));
        } catch (IOException e) {
            System.err.println("Error creating face data directory: " + e.getMessage());
        }
    }

    /**
     * Check if a user has Face ID registered (has photos)
     */
    public boolean isFaceIdRegistered(int userId) {
        Path userFaceDir = Paths.get(FACE_DATA_DIR, String.valueOf(userId));
        if (!Files.exists(userFaceDir)) {
            return false;
        }

        File dir = userFaceDir.toFile();
        File[] photos = dir.listFiles((d, name) -> name.endsWith(".png") || name.endsWith(".jpg"));
        return photos != null && photos.length >= 3; // At least 3 photos for registration
    }

    /**
     * Check if a user has Face ID registered by email
     */
    public boolean isFaceIdRegistered(String email) {
        User user = UserDAO.getUserByEmail(email);
        if (user == null) {
            return false;
        }
        return isFaceIdRegistered(user.getId());
    }

    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        return UserDAO.getUserByEmail(email);
    }

    /**
     * Save a captured face photo for registration
     * @return the file path of the saved photo
     */
    public String saveFacePhoto(int userId, BufferedImage image, int photoIndex) throws IOException {
        Path userFaceDir = Paths.get(FACE_DATA_DIR, String.valueOf(userId));
        Files.createDirectories(userFaceDir);

        String filename = "face_" + photoIndex + "_" + UUID.randomUUID().toString().substring(0, 8) + ".png";
        Path photoPath = userFaceDir.resolve(filename);

        ImageIO.write(image, "PNG", photoPath.toFile());
        return photoPath.toString();
    }

    /**
     * Get all registered face photos for a user
     */
    public List<File> getFacePhotos(int userId) {
        List<File> photos = new ArrayList<>();
        Path userFaceDir = Paths.get(FACE_DATA_DIR, String.valueOf(userId));

        if (!Files.exists(userFaceDir)) {
            return photos;
        }

        File dir = userFaceDir.toFile();
        File[] files = dir.listFiles((d, name) -> name.endsWith(".png") || name.endsWith(".jpg"));

        if (files != null) {
            for (File file : files) {
                photos.add(file);
            }
        }

        return photos;
    }

    /**
     * Get the number of photos needed for complete registration
     */
    public int getRequiredPhotoCount() {
        return REGISTRATION_PHOTO_COUNT;
    }

    /**
     * Get the current number of registered photos for a user
     */
    public int getRegisteredPhotoCount(int userId) {
        return getFacePhotos(userId).size();
    }

    /**
     * Clear all face photos for a user (for re-registration)
     */
    public boolean clearFacePhotos(int userId) {
        Path userFaceDir = Paths.get(FACE_DATA_DIR, String.valueOf(userId));
        if (!Files.exists(userFaceDir)) {
            return true;
        }

        try {
            File dir = userFaceDir.toFile();
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
            Files.deleteIfExists(userFaceDir);
            return true;
        } catch (IOException e) {
            System.err.println("Error clearing face photos: " + e.getMessage());
            return false;
        }
    }

    /**
     * Compare a captured face with registered faces.
     * This is a simplified implementation - in production, use proper face recognition
     * like OpenCV with face embeddings or a service like AWS Rekognition.
     *
     * For now, we accept the capture if:
     * - The user has registered photos
     * - The capture is successful (proper image format)
     *
     * In a real implementation, you would compare face descriptors/embeddings.
     */
    public boolean compareFaces(int userId, BufferedImage capturedImage) {
        // Check if user has registered faces
        List<File> registeredPhotos = getFacePhotos(userId);
        if (registeredPhotos.isEmpty()) {
            return false;
        }

        // Simplified: Just check image properties
        // In production: Use OpenCV face recognition or external API
        if (capturedImage == null || capturedImage.getWidth() < 50 || capturedImage.getHeight() < 50) {
            return false;
        }

        // For now, we assume success if we have valid images
        // TODO: Implement proper face comparison using OpenCV or similar
        return true;
    }
}
