package org.example.service;

import org.example.dao.UserDAO;
import org.example.model.User;

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
 * FaceRecognitionService — Stub version (OpenCV désactivé)
 * La reconnaissance faciale est désactivée temporairement.
 * Pour la réactiver : ajouter org.openpnp:opencv dans pom.xml
 * et restaurer l'implémentation OpenCV.
 */
public class FaceRecognitionService {

    private static final String FACE_DATA_DIR = "user_data/faces";
    private static final int REGISTRATION_PHOTO_COUNT = 5;
    private static FaceRecognitionService instance;

    private FaceRecognitionService() {
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

    public boolean isFaceIdRegistered(int userId) {
        Path userFaceDir = Paths.get(FACE_DATA_DIR, String.valueOf(userId));
        if (!Files.exists(userFaceDir)) return false;
        File[] photos = userFaceDir.toFile().listFiles(
                (d, name) -> name.endsWith(".png") || name.endsWith(".jpg")
        );
        return photos != null && photos.length >= 3;
    }

    public boolean isFaceIdRegistered(String email) {
        User user = UserDAO.getUserByEmail(email);
        return user != null && isFaceIdRegistered(user.getId());
    }

    public User getUserByEmail(String email) {
        return UserDAO.getUserByEmail(email);
    }

    public String saveFacePhoto(int userId, int photoIndex) throws IOException {
        Path userFaceDir = Paths.get(FACE_DATA_DIR, String.valueOf(userId));
        Files.createDirectories(userFaceDir);
        String filename = "face_" + photoIndex + "_" + UUID.randomUUID().toString().substring(0, 8) + ".png";
        Path photoPath = userFaceDir.resolve(filename);
        Files.createFile(photoPath);
        return photoPath.toString();
    }

    public String saveFacePhoto(int userId, int photoIndex, BufferedImage image) throws IOException {
        // OpenCV désactivé — sauvegarde placeholder
        return saveFacePhoto(userId, photoIndex);
    }

    public boolean detectFace(BufferedImage image) {
        // OpenCV désactivé — retourne true pour permettre la capture
        return image != null;
    }

    public boolean compareFaces(int userId, BufferedImage capturedImage) {
        // OpenCV désactivé — retourne false (pas de reconnaissance)
        System.out.println("Face recognition désactivée (OpenCV non configuré)");
        return false;
    }

    public boolean compareFaces(int userId) {
        return false;
    }

    public List<File> getFacePhotos(int userId) {
        List<File> photos = new ArrayList<>();
        Path userFaceDir = Paths.get(FACE_DATA_DIR, String.valueOf(userId));
        if (!Files.exists(userFaceDir)) return photos;
        File[] files = userFaceDir.toFile().listFiles(
                (d, name) -> name.endsWith(".png") || name.endsWith(".jpg")
        );
        if (files != null) {
            for (File file : files) photos.add(file);
        }
        return photos;
    }

    public int getRequiredPhotoCount() {
        return REGISTRATION_PHOTO_COUNT;
    }

    public int getRegisteredPhotoCount(int userId) {
        return getFacePhotos(userId).size();
    }

    public boolean clearFacePhotos(int userId) {
        Path userFaceDir = Paths.get(FACE_DATA_DIR, String.valueOf(userId));
        if (!Files.exists(userFaceDir)) return true;
        try {
            File[] files = userFaceDir.toFile().listFiles();
            if (files != null) {
                for (File file : files) file.delete();
            }
            Files.deleteIfExists(userFaceDir);
            return true;
        } catch (IOException e) {
            System.err.println("Error clearing face photos: " + e.getMessage());
            return false;
        }
    }
}