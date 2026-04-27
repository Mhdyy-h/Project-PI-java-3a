package org.example.service;

import org.example.dao.UserDAO;
import org.example.model.User;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service de réinitialisation de mot de passe.
 * Génère un code à 6 chiffres, le stocke en mémoire avec expiration,
 * et envoie l'email via MailjetEmailService.
 */
public class PasswordResetService {

    private static PasswordResetService instance;

    private final MailjetEmailService emailService = MailjetEmailService.getInstance();
    private final SecureRandom random = new SecureRandom();

    // Stockage des codes en mémoire : email -> ResetEntry
    private final Map<String, ResetEntry> pendingResets = new ConcurrentHashMap<>();

    // Durée de validité du code (5 minutes)
    private static final long CODE_EXPIRY_MS = 5 * 60 * 1000;

    private PasswordResetService() {}

    public static PasswordResetService getInstance() {
        if (instance == null) {
            instance = new PasswordResetService();
        }
        return instance;
    }

    /**
     * Envoie un code de réinitialisation par email.
     * @return true si l'email a été envoyé avec succès
     */
    public boolean sendResetCode(String email) {
        // Vérifier que l'utilisateur existe
        User user = UserDAO.getUserByEmail(email);
        if (user == null) {
            return false;
        }

        // Générer un code à 6 chiffres
        String code = generateCode();

        // Stocker le code avec timestamp
        pendingResets.put(email.toLowerCase(), new ResetEntry(code, System.currentTimeMillis()));

        // Envoyer l'email avec le nom de l'utilisateur
        boolean sent = emailService.sendPasswordResetEmail(email, code, user.getNomComplet());
        if (!sent) {
            pendingResets.remove(email.toLowerCase());
        }

        System.out.println("[PasswordReset] Code envoyé à " + email + ": " + code);
        return sent;
    }

    /**
     * Vérifie un code de réinitialisation.
     * @return true si le code est valide et non expiré
     */
    public boolean verifyCode(String email, String code) {
        ResetEntry entry = pendingResets.get(email.toLowerCase());
        if (entry == null) {
            return false;
        }

        // Vérifier l'expiration
        if (System.currentTimeMillis() - entry.timestamp > CODE_EXPIRY_MS) {
            pendingResets.remove(email.toLowerCase());
            return false;
        }

        // Vérifier le code
        return entry.code.equals(code.trim());
    }

    /**
     * Réinitialise le mot de passe après vérification du code.
     * @return true si le mot de passe a été changé avec succès
     */
    public boolean resetPassword(String email, String code, String newPassword) {
        if (!verifyCode(email, code)) {
            return false;
        }

        // Mettre à jour le mot de passe en base
        boolean success = UserDAO.updatePasswordByEmail(email, newPassword);
        if (success) {
            pendingResets.remove(email.toLowerCase());
        }
        return success;
    }

    /**
     * Génère un code numérique à 6 chiffres.
     */
    private String generateCode() {
        int code = 100000 + random.nextInt(900000); // 100000–999999
        return String.valueOf(code);
    }

    /**
     * Entrée interne pour stocker un code et son timestamp.
     */
    private static class ResetEntry {
        final String code;
        final long timestamp;

        ResetEntry(String code, long timestamp) {
            this.code = code;
            this.timestamp = timestamp;
        }
    }
}
