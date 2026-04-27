package org.example.service;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Service de génération d'avatars cartoon style Instagram.
 * Utilise l'API HTTP de DiceBear avec cache local.
 */
public class AvatarService {

    private static final String API_BASE_URL = "https://api.dicebear.com/9.x";
    private static final String DEFAULT_STYLE = "lorelei"; // Style cartoon mignon
    private static final int AVATAR_SIZE = 200;
    private static final int CACHE_SIZE = 100;

    private static AvatarService instance;
    private final HttpClient httpClient;
    private final Path cacheDirectory;

    // Styles disponibles
    public enum Style {
        LORELEI("lorelei", "Cartoon mignon"),
        AVATAAARS("avataaars", "Avatar cartoon"),
        NOTIONISTS("notionists", "Style Notion"),
        FUN_EMOJI("fun-emoji", "Emoji personnalisé"),
        ADVENTURER("adventurer", "Aventurier RPG"),
        BOTTOX("bottts", "Robot cartoon");

        private final String apiName;
        private final String displayName;

        Style(String apiName, String displayName) {
            this.apiName = apiName;
            this.displayName = displayName;
        }

        public String getApiName() { return apiName; }
        public String getDisplayName() { return displayName; }
    }

    private AvatarService() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .build();

        // Créer le répertoire de cache
        this.cacheDirectory = Paths.get(System.getProperty("user.home"), ".biosync", "avatars");
        try {
            Files.createDirectories(cacheDirectory);
        } catch (IOException e) {
            System.err.println("[AvatarService] Erreur création cache: " + e.getMessage());
        }
    }

    public static synchronized AvatarService getInstance() {
        if (instance == null) {
            instance = new AvatarService();
        }
        return instance;
    }

    /**
     * Génère un avatar pour un utilisateur donné.
     * @param seed Identifiant unique (email, nom d'utilisateur, etc.)
     * @return Image JavaFX de l'avatar
     */
    public Image generateAvatar(String seed) {
        return generateAvatar(seed, DEFAULT_STYLE, AVATAR_SIZE);
    }

    /**
     * Génère un avatar avec un style spécifique.
     * @param seed Identifiant unique
     * @param style Style d'avatar (voir enum Style)
     * @return Image JavaFX de l'avatar
     */
    public Image generateAvatar(String seed, String style) {
        return generateAvatar(seed, style, AVATAR_SIZE);
    }

    /**
     * Génère un avatar avec un style et une taille spécifiques.
     * @param seed Identifiant unique
     * @param style Style d'avatar
     * @param size Taille en pixels
     * @return Image JavaFX de l'avatar
     */
    public Image generateAvatar(String seed, String style, int size) {
        String normalizedSeed = seed.toLowerCase().trim();
        String cacheKey = generateCacheKey(normalizedSeed, style, size);

        // Essayer de charger depuis le cache
        Path cachedFile = cacheDirectory.resolve(cacheKey + ".png");
        if (Files.exists(cachedFile)) {
            try {
                return new Image(cachedFile.toUri().toString());
            } catch (Exception e) {
                System.err.println("[AvatarService] Erreur chargement cache: " + e.getMessage());
            }
        }

        // Télécharger depuis l'API
        Image avatar = downloadAvatar(normalizedSeed, style, size);

        // Sauvegarder dans le cache si téléchargement réussi
        if (avatar != null && !avatar.isError()) {
            saveToCache(avatar, cachedFile);
        } else if (avatar == null || avatar.isError()) {
            // Fallback: générer un avatar local si l'API échoue
            return generateFallbackAvatar(normalizedSeed, size);
        }

        return avatar;
    }

    /**
     * Régénère l'avatar de l'utilisateur avec un style aléatoire.
     * @param seed Identifiant unique
     * @return Image JavaFX du nouvel avatar
     */
    public Image regenerateWithRandomStyle(String seed) {
        Style[] styles = Style.values();
        Style randomStyle = styles[(int) (Math.random() * styles.length)];
        return generateAvatar(seed, randomStyle.getApiName());
    }

    private Image downloadAvatar(String seed, String style, int size) {
        try {
            String encodedSeed = java.net.URLEncoder.encode(seed, "UTF-8");
            String url = String.format("%s/%s/png?seed=%s&size=%d&backgroundColor=b6e3f4",
                API_BASE_URL, style, encodedSeed, size);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "image/png")
                .GET()
                .build();

            HttpResponse<byte[]> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() == 200) {
                byte[] imageData = response.body();
                return new Image(new ByteArrayInputStream(imageData));
            } else {
                System.err.println("[AvatarService] Erreur API: " + response.statusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("[AvatarService] Erreur téléchargement: " + e.getMessage());
            return null;
        }
    }

    private void saveToCache(Image image, Path cacheFile) {
        try {
            // Convertir Image en bytes PNG
            javafx.scene.image.PixelReader reader = image.getPixelReader();
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();

            WritableImage writableImage = new WritableImage(reader, width, height);

            // Utiliser JavaFX pour sauvegarder
            javafx.embed.swing.SwingFXUtils.fromFXImage(writableImage, null);

            // Alternative: sauvegarder via Java2D
            java.awt.image.BufferedImage bufferedImage =
                javafx.embed.swing.SwingFXUtils.fromFXImage(writableImage, null);
            javax.imageio.ImageIO.write(bufferedImage, "PNG", cacheFile.toFile());

            // Nettoyer le cache si trop grand
            cleanupCache();
        } catch (Exception e) {
            System.err.println("[AvatarService] Erreur sauvegarde cache: " + e.getMessage());
        }
    }

    private void cleanupCache() {
        try {
            java.util.List<Path> files = Files.list(cacheDirectory)
                .sorted((a, b) -> {
                    try {
                        return Files.getLastModifiedTime(a).compareTo(Files.getLastModifiedTime(b));
                    } catch (IOException e) {
                        return 0;
                    }
                })
                .toList();

            if (files.size() > CACHE_SIZE) {
                // Supprimer les fichiers les plus anciens
                for (int i = 0; i < files.size() - CACHE_SIZE; i++) {
                    Files.deleteIfExists(files.get(i));
                }
            }
        } catch (IOException e) {
            System.err.println("[AvatarService] Erreur nettoyage cache: " + e.getMessage());
        }
    }

    private String generateCacheKey(String seed, String style, int size) {
        String input = seed + "_" + style + "_" + size;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.substring(0, 16); // Tronquer pour la lisibilité
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(input.hashCode());
        }
    }

    /**
     * Génère un avatar de fallback local (initiales colorées).
     * @param seed Identifiant
     * @param size Taille
     * @return Image JavaFX
     */
    public Image generateFallbackAvatar(String seed, int size) {
        Canvas canvas = new Canvas(size, size);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Couleur de fond basée sur le seed
        Color bgColor = generateColorFromSeed(seed);

        // Fond
        gc.setFill(bgColor);
        gc.fillRect(0, 0, size, size);

        // Initiales
        String initials = extractInitials(seed);
        gc.setFill(Color.WHITE);
        gc.setFont(new javafx.scene.text.Font("System Bold", size * 0.5));

        javafx.scene.text.Text text = new javafx.scene.text.Text(initials);
        text.setFont(gc.getFont());
        double textWidth = text.getBoundsInLocal().getWidth();
        double textHeight = text.getBoundsInLocal().getHeight();

        gc.fillText(initials,
            (size - textWidth) / 2,
            (size + textHeight / 2) / 2);

        // Convertir en Image
        WritableImage image = new WritableImage(size, size);
        canvas.snapshot(null, image);
        return image;
    }

    private Color generateColorFromSeed(String seed) {
        int hash = seed.hashCode();
        double hue = Math.abs(hash % 360);
        double saturation = 0.6 + (Math.abs((hash >> 8) % 20) / 100.0);
        double brightness = 0.7 + (Math.abs((hash >> 16) % 20) / 100.0);
        return Color.hsb(hue, saturation, brightness);
    }

    private String extractInitials(String text) {
        if (text == null || text.isEmpty()) {
            return "?";
        }

        String[] parts = text.split("[@._\\s]+");
        StringBuilder initials = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty() && initials.length() < 2) {
                initials.append(Character.toUpperCase(part.charAt(0)));
            }
        }

        return initials.length() > 0 ? initials.toString() : "?";
    }

    /**
     * Efface le cache des avatars.
     */
    public void clearCache() {
        try {
            Files.list(cacheDirectory).forEach(file -> {
                try {
                    Files.deleteIfExists(file);
                } catch (IOException e) {
                    System.err.println("[AvatarService] Erreur suppression: " + e.getMessage());
                }
            });
            System.out.println("[AvatarService] Cache effacé");
        } catch (IOException e) {
            System.err.println("[AvatarService] Erreur nettoyage cache: " + e.getMessage());
        }
    }
}
