package org.example.service;

import org.example.dao.RateLimitingDAO;

import java.time.Instant;
import java.util.Map;

/**
 * Service de rate limiting pour l'application desktop.
 * Limite les tentatives de connexion/inscription par email.
 * Les données sont persistées en base de données.
 */
public class RateLimiterService {

    private static RateLimiterService instance;

    // Maximum de tentatives autorisées
    private static final int MAX_ATTEMPTS = 5;
    // Fenêtre de temps en minutes
    private static final int WINDOW_MINUTES = 15;

    private RateLimiterService() {
        // Créer la table si elle n'existe pas
        RateLimitingDAO.createTableIfNotExists();
    }

    public static synchronized RateLimiterService getInstance() {
        if (instance == null) {
            instance = new RateLimiterService();
        }
        return instance;
    }

    /**
     * Vérifie si une action est autorisée pour l'email donné.
     * @param email L'email de l'utilisateur
     * @return true si l'action est autorisée, false si la limite est dépassée
     */
    public boolean isAllowed(String email) {
        if (email == null || email.isEmpty()) {
            return true; // Pas de limitation si pas d'email
        }

        String key = email.toLowerCase().trim();
        Map<String, Object> data = RateLimitingDAO.getAttempts(key);

        if (data == null) {
            return true; // Première tentative
        }

        Instant lastAttempt = (Instant) data.get("lastAttempt");
        int count = (Integer) data.get("count");

        // Vérifier si la fenêtre de temps est expirée
        long minutesSinceLastAttempt = java.time.Duration.between(
            lastAttempt, Instant.now()).toMinutes();

        if (minutesSinceLastAttempt >= WINDOW_MINUTES) {
            // Réinitialiser après la fenêtre de temps
            RateLimitingDAO.deleteAttempts(key);
            return true;
        }

        // Vérifier le nombre de tentatives
        return count < MAX_ATTEMPTS;
    }

    /**
     * Enregistre une tentative pour l'email donné.
     * @param email L'email de l'utilisateur
     */
    public void recordAttempt(String email) {
        if (email == null || email.isEmpty()) {
            return;
        }

        String key = email.toLowerCase().trim();
        Map<String, Object> data = RateLimitingDAO.getAttempts(key);

        int newCount;
        if (data == null) {
            newCount = 1;
        } else {
            newCount = (Integer) data.get("count") + 1;
        }

        RateLimitingDAO.saveAttempts(key, newCount, Instant.now());
        System.out.println("[RateLimiter] Tentative enregistrée pour " + key + " (total: " + newCount + ")");
    }

    /**
     * Réinitialise les tentatives pour un email (après une connexion réussie).
     * @param email L'email de l'utilisateur
     */
    public void resetAttempts(String email) {
        if (email == null || email.isEmpty()) {
            return;
        }
        RateLimitingDAO.deleteAttempts(email.toLowerCase().trim());
        System.out.println("[RateLimiter] Tentatives réinitialisées pour " + email);
    }

    /**
     * Retourne le temps d'attente restant en minutes.
     * @param email L'email de l'utilisateur
     * @return Le temps d'attente restant, ou 0 si pas de limitation
     */
    public long getRemainingWaitTime(String email) {
        if (email == null || email.isEmpty()) {
            return 0;
        }

        String key = email.toLowerCase().trim();
        Map<String, Object> data = RateLimitingDAO.getAttempts(key);

        if (data == null) {
            return 0;
        }

        int count = (Integer) data.get("count");
        if (count < MAX_ATTEMPTS) {
            return 0;
        }

        Instant lastAttempt = (Instant) data.get("lastAttempt");
        long minutesSinceLastAttempt = java.time.Duration.between(
            lastAttempt, Instant.now()).toMinutes();

        long remaining = WINDOW_MINUTES - minutesSinceLastAttempt;
        return Math.max(0, remaining);
    }

    /**
     * Retourne un message d'erreur formaté avec le temps d'attente.
     * @param email L'email de l'utilisateur
     * @return Le message d'erreur, ou null si pas de limitation
     */
    public String getErrorMessage(String email) {
        long remaining = getRemainingWaitTime(email);
        if (remaining == 0) {
            return null;
        }
        return "Trop de tentatives. Veuillez réessayer dans " + remaining + " minute(s).";
    }

    /**
     * Retourne la liste des utilisateurs actuellement bloqués (pour l'admin).
     * @return Map avec email -> info de blocage
     */
    public Map<String, BlockedUserInfo> getBlockedUsers() {
        Map<String, BlockedUserInfo> blocked = new java.util.HashMap<>();
        Map<String, Map<String, Object>> allAttempts = RateLimitingDAO.getAllAttempts();

        for (Map.Entry<String, Map<String, Object>> entry : allAttempts.entrySet()) {
            String email = entry.getKey();
            Map<String, Object> data = entry.getValue();

            int count = (Integer) data.get("count");
            if (count >= MAX_ATTEMPTS) {
                long remaining = getRemainingWaitTime(email);
                if (remaining > 0) {
                    blocked.put(email, new BlockedUserInfo(
                        email,
                        count,
                        (Instant) data.get("lastAttempt"),
                        remaining
                    ));
                }
            }
        }
        return blocked;
    }

    /**
     * Débloque manuellement un utilisateur (pour l'admin).
     * @param email L'email de l'utilisateur à débloquer
     * @return true si l'utilisateur était bloqué et est maintenant débloqué
     */
    public boolean unblockUser(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        String key = email.toLowerCase().trim();
        boolean removed = RateLimitingDAO.deleteAttempts(key);
        if (removed) {
            System.out.println("[RateLimiter] Utilisateur débloqué par admin: " + email);
        }
        return removed;
    }

    /**
     * Retourne les informations d'un utilisateur spécifique.
     * @param email L'email de l'utilisateur
     * @return BlockedUserInfo ou null si pas bloqué
     */
    public BlockedUserInfo getUserInfo(String email) {
        if (email == null || email.isEmpty()) {
            return null;
        }
        String key = email.toLowerCase().trim();
        Map<String, Object> data = RateLimitingDAO.getAttempts(key);

        if (data == null) {
            return null;
        }

        long remaining = getRemainingWaitTime(key);
        return new BlockedUserInfo(
            email,
            (Integer) data.get("count"),
            (Instant) data.get("lastAttempt"),
            remaining
        );
    }

    public static class BlockedUserInfo {
        public final String email;
        public final int attemptCount;
        public final Instant lastAttempt;
        public final long remainingMinutes;

        public BlockedUserInfo(String email, int attemptCount, Instant lastAttempt, long remainingMinutes) {
            this.email = email;
            this.attemptCount = attemptCount;
            this.lastAttempt = lastAttempt;
            this.remainingMinutes = remainingMinutes;
        }

        public boolean isCurrentlyBlocked() {
            return remainingMinutes > 0 && attemptCount >= MAX_ATTEMPTS;
        }
    }
}
