package org.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Properties;

/**
 * Service de vérification reCAPTCHA v3 côté serveur.
 * Appelle l'API Google pour vérifier le token et retourner un score (0.0–1.0).
 */
public class RecaptchaService {
    private static RecaptchaService instance;
    private final String siteKey;
    private final String secretKey;
    private final double threshold;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    private RecaptchaService() {
        Properties props = loadConfig();
        this.siteKey = props.getProperty("recaptcha.site.key", "");
        this.secretKey = props.getProperty("recaptcha.secret.key", "");
        this.threshold = Double.parseDouble(props.getProperty("recaptcha.threshold", "0.5"));
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        this.mapper = new ObjectMapper();
    }

    public static synchronized RecaptchaService getInstance() {
        if (instance == null) instance = new RecaptchaService();
        return instance;
    }

    public String getSiteKey() { return siteKey; }
    public double getThreshold() { return threshold; }

    public boolean isConfigured() {
        return siteKey != null && !siteKey.isEmpty() && !siteKey.equals("YOUR_SITE_KEY")
            && secretKey != null && !secretKey.isEmpty() && !secretKey.equals("YOUR_SECRET_KEY");
    }

    /**
     * Vérifie un token reCAPTCHA et retourne le score.
     * @return score entre 0.0 et 1.0, ou -1 en cas d'erreur
     */
    public double verify(String token) {
        if (!isConfigured()) {
            System.out.println("[reCAPTCHA] Non configuré, vérification ignorée (score=1.0)");
            return 1.0;
        }
        try {
            String body = "secret=" + secretKey + "&response=" + token;
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VERIFY_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(10))
                .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = mapper.readTree(response.body());
            boolean success = json.path("success").asBoolean(false);
            double score = json.path("score").asDouble(0.0);
            System.out.println("[reCAPTCHA] success=" + success + " score=" + score);
            return success ? score : 0.0;
        } catch (Exception e) {
            System.err.println("[reCAPTCHA] Erreur: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Vérifie si le score dépasse le seuil configuré.
     */
    public boolean isHuman(String token) {
        double score = verify(token);
        return score >= threshold;
    }

    private Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/config.properties")) {
            if (is != null) props.load(is);
        } catch (IOException e) { e.printStackTrace(); }
        return props;
    }
}
