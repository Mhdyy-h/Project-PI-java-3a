package org.example.service;

/**
 * Service reCAPTCHA - DÉSACTIVÉ pour application desktop.
 * Retourne toujours succès (score=1.0).
 */
public class RecaptchaService {
    private static RecaptchaService instance;

    private RecaptchaService() {}

    public static synchronized RecaptchaService getInstance() {
        if (instance == null) instance = new RecaptchaService();
        return instance;
    }

    public String getSiteKey() { return ""; }
    public double getThreshold() { return 0.5; }

    public boolean isConfigured() {
        return false;
    }

    /**
     * Vérification désactivée - retourne toujours succès.
     */
    public double verify(String token) {
        return 1.0;
    }

    public boolean isHuman(String token) {
        return true;
    }
}
