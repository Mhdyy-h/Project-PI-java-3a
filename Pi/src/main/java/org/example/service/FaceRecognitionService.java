package org.example.service;

import nu.pattern.OpenCV;
import org.example.dao.UserDAO;
import org.example.model.User;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
    private CascadeClassifier faceDetector;

    private FaceRecognitionService() {
        // Initialize OpenCV
        try {
            OpenCV.loadLocally();
            // Load Haar cascade for face detection
            faceDetector = new CascadeClassifier();

            // Try multiple methods to load the cascade file
            boolean loaded = false;

            // Method 1: Try loading from file system (project root or resources)
            String[] possiblePaths = {
                "src/main/resources/haarcascade_frontalface_default.xml",
                "target/classes/haarcascade_frontalface_default.xml",
                "haarcascade_frontalface_default.xml"
            };

            for (String path : possiblePaths) {
                java.io.File file = new java.io.File(path);
                if (file.exists()) {
                    if (faceDetector.load(file.getAbsolutePath())) {
                        System.out.println("Face cascade loaded from: " + file.getAbsolutePath());
                        loaded = true;
                        break;
                    }
                }
            }

            // Method 2: Try from classpath (extract to temp file if needed)
            if (!loaded) {
                try (java.io.InputStream is = getClass().getResourceAsStream("/haarcascade_frontalface_default.xml")) {
                    if (is != null) {
                        java.io.File tempFile = java.io.File.createTempFile("haarcascade", ".xml");
                        tempFile.deleteOnExit();
                        java.nio.file.Files.copy(is, tempFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        if (faceDetector.load(tempFile.getAbsolutePath())) {
                            System.out.println("Face cascade loaded from temp file: " + tempFile.getAbsolutePath());
                            loaded = true;
                        }
                    }
                }
            }

            if (!loaded) {
                System.err.println("Could not load face cascade, face detection disabled");
                faceDetector = null;
            }
        } catch (Exception e) {
            System.err.println("OpenCV initialization failed: " + e.getMessage());
            e.printStackTrace();
            faceDetector = null;
        }
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
    public String saveFacePhoto(int userId, int photoIndex) throws IOException {
        Path userFaceDir = Paths.get(FACE_DATA_DIR, String.valueOf(userId));
        Files.createDirectories(userFaceDir);

        String filename = "face_" + photoIndex + "_" + UUID.randomUUID().toString().substring(0, 8) + ".png";
        Path photoPath = userFaceDir.resolve(filename);

        // Create placeholder file (in production: save actual image)
        Files.createFile(photoPath);
        return photoPath.toString();
    }

    /**
     * Save a captured BufferedImage as face photo
     */
    public String saveFacePhoto(int userId, int photoIndex, BufferedImage image) throws IOException {
        Path userFaceDir = Paths.get(FACE_DATA_DIR, String.valueOf(userId));
        Files.createDirectories(userFaceDir);

        String filename = "face_" + photoIndex + "_" + UUID.randomUUID().toString().substring(0, 8) + ".png";
        Path photoPath = userFaceDir.resolve(filename);

        // Convert BufferedImage to Mat and save using OpenCV
        Mat mat = bufferedImageToMat(image);
        Imgcodecs.imwrite(photoPath.toString(), mat);
        return photoPath.toString();
    }

    /**
     * Detect if a face is present in the given image
     */
    public boolean detectFace(BufferedImage image) {
        if (faceDetector == null || image == null) {
            return false;
        }

        try {
            Mat mat = bufferedImageToMat(image);
            Mat gray = new Mat();
            Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);
            Imgproc.equalizeHist(gray, gray);

            MatOfRect faces = new MatOfRect();
            faceDetector.detectMultiScale(gray, faces, 1.1, 3, 0, new Size(30, 30), new Size());

            boolean faceFound = !faces.empty();
            gray.release();
            mat.release();

            return faceFound;
        } catch (Exception e) {
            System.err.println("Face detection error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Convert BufferedImage to OpenCV Mat
     */
    private Mat bufferedImageToMat(BufferedImage image) {
        if (image.getType() != BufferedImage.TYPE_3BYTE_BGR) {
            BufferedImage converted = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            converted.getGraphics().drawImage(image, 0, 0, null);
            image = converted;
        }

        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, pixels);
        return mat;
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
     * Compare a captured face with registered faces using histogram comparison.
     * Extracts face region and compares color/texture features.
     */
    public boolean compareFaces(int userId, BufferedImage capturedImage) {
        // Check if user has registered faces
        List<File> registeredPhotos = getFacePhotos(userId);
        if (registeredPhotos.isEmpty()) {
            System.out.println("No registered photos found for user " + userId);
            return false;
        }

        // First, check if there's a face in the captured image
        if (!detectFace(capturedImage)) {
            System.out.println("No face detected in captured image");
            return false;
        }

        try {
            // Convert captured image to Mat and extract face region
            Mat capturedMat = bufferedImageToMat(capturedImage);
            Mat capturedFace = extractFaceRegion(capturedMat);
            if (capturedFace == null || capturedFace.empty()) {
                System.out.println("Could not extract face region from captured image");
                capturedMat.release();
                return false;
            }

            // Calculate histogram for captured face
            Mat capturedHist = calculateFaceHistogram(capturedFace);

            // Compare with each registered photo
            double bestMatch = Double.MAX_VALUE;
            double threshold = 0.35; // Similarity threshold (lower is more similar)

            for (File registeredPhoto : registeredPhotos) {
                try {
                    Mat registeredMat = Imgcodecs.imread(registeredPhoto.getAbsolutePath());
                    if (registeredMat == null || registeredMat.empty()) {
                        continue;
                    }

                    Mat registeredFace = extractFaceRegion(registeredMat);
                    if (registeredFace == null || registeredFace.empty()) {
                        registeredMat.release();
                        continue;
                    }

                    Mat registeredHist = calculateFaceHistogram(registeredFace);

                    // Compare histograms using correlation
                    double correlation = Imgproc.compareHist(capturedHist, registeredHist, Imgproc.HISTCMP_CORREL);
                    double bhattacharyya = Imgproc.compareHist(capturedHist, registeredHist, Imgproc.HISTCMP_BHATTACHARYYA);

                    // Convert correlation to distance (1 - correlation is distance)
                    double distance = (1 - correlation) * 0.5 + bhattacharyya * 0.5;

                    if (distance < bestMatch) {
                        bestMatch = distance;
                    }

                    registeredHist.release();
                    registeredFace.release();
                    registeredMat.release();

                } catch (Exception e) {
                    System.err.println("Error comparing with registered photo: " + e.getMessage());
                }
            }

            capturedHist.release();
            capturedFace.release();
            capturedMat.release();

            System.out.println("Best face match distance: " + bestMatch + " (threshold: " + threshold + ")");

            // Accept if best match is below threshold
            return bestMatch < threshold;

        } catch (Exception e) {
            System.err.println("Face comparison error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Extract face region from image using face detection
     */
    private Mat extractFaceRegion(Mat image) {
        if (faceDetector == null) {
            return image.clone();
        }

        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(gray, gray);

        MatOfRect faces = new MatOfRect();
        faceDetector.detectMultiScale(gray, faces, 1.1, 3, 0, new Size(100, 100), new Size());

        Rect[] faceArray = faces.toArray();
        if (faceArray.length == 0) {
            gray.release();
            faces.release();
            return image.clone();
        }

        // Take the largest face
        Rect largestFace = faceArray[0];
        for (Rect face : faceArray) {
            if (face.area() > largestFace.area()) {
                largestFace = face;
            }
        }

        // Extract face region with some margin
        int margin = 10;
        int x = Math.max(0, largestFace.x - margin);
        int y = Math.max(0, largestFace.y - margin);
        int width = Math.min(image.width() - x, largestFace.width + 2 * margin);
        int height = Math.min(image.height() - y, largestFace.height + 2 * margin);

        Rect faceRect = new Rect(x, y, width, height);
        Mat faceRegion = new Mat(image, faceRect);

        // Resize to standard size
        Mat resizedFace = new Mat();
        Imgproc.resize(faceRegion, resizedFace, new Size(128, 128));

        gray.release();
        faces.release();
        faceRegion.release();

        return resizedFace;
    }

    /**
     * Calculate color histogram for face comparison
     */
    private Mat calculateFaceHistogram(Mat face) {
        Mat hsv = new Mat();
        Imgproc.cvtColor(face, hsv, Imgproc.COLOR_BGR2HSV);

        // Calculate histogram for H and S channels
        List<Mat> channels = new ArrayList<>();
        Core.split(hsv, channels);

        MatOfInt histSize = new MatOfInt(30, 32);
        MatOfFloat ranges = new MatOfFloat(0, 180, 0, 256);
        MatOfInt channelsToUse = new MatOfInt(0, 1);

        Mat hist = new Mat();
        Imgproc.calcHist(Arrays.asList(hsv), channelsToUse, new Mat(), hist, histSize, ranges);
        Core.normalize(hist, hist, 0, 1, Core.NORM_MINMAX, -1, new Mat());

        hsv.release();
        for (Mat channel : channels) {
            channel.release();
        }

        return hist;
    }

    /**
     * Legacy method for backward compatibility - requires face detection
     */
    public boolean compareFaces(int userId) {
        // This method requires an image parameter for proper face detection
        // Returns false to prevent false positives
        return false;
    }
}
