package org.example.service;

import java.security.SecureRandom;

/**
 * Service pour évaluer la force d'un mot de passe et générer des mots de passe forts.
 */
public class PasswordStrengthService {

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%&*?+-=";
    private static final String ALL_CHARS = UPPER + LOWER + DIGITS + SPECIAL;
    private static final SecureRandom random = new SecureRandom();

    private static PasswordStrengthService instance;

    public static PasswordStrengthService getInstance() {
        if (instance == null) {
            instance = new PasswordStrengthService();
        }
        return instance;
    }

    /**
     * Calcule la force d'un mot de passe (0.0 à 1.0).
     */
    public double calculateStrength(String password) {
        if (password == null || password.isEmpty()) return 0.0;

        double score = 0.0;

        // Longueur
        int len = password.length();
        if (len >= 8) score += 0.15;
        if (len >= 12) score += 0.15;
        if (len >= 16) score += 0.10;

        // Majuscules
        if (password.chars().anyMatch(Character::isUpperCase)) score += 0.15;

        // Minuscules
        if (password.chars().anyMatch(Character::isLowerCase)) score += 0.10;

        // Chiffres
        if (password.chars().anyMatch(Character::isDigit)) score += 0.15;

        // Caractères spéciaux
        if (password.chars().anyMatch(c -> SPECIAL.indexOf(c) >= 0)) score += 0.20;

        // Bonus diversité
        long uniqueChars = password.chars().distinct().count();
        if (uniqueChars >= len * 0.7) score += 0.05;

        return Math.min(1.0, score);
    }

    /**
     * Retourne le label de force en français.
     */
    public String getStrengthLabel(double strength) {
        if (strength < 0.25) return "Très faible";
        if (strength < 0.45) return "Faible";
        if (strength < 0.65) return "Moyen";
        if (strength < 0.85) return "Fort";
        return "Très fort";
    }

    /**
     * Retourne la couleur CSS pour la force.
     */
    public String getStrengthColor(double strength) {
        if (strength < 0.25) return "#ef4444";
        if (strength < 0.45) return "#f97316";
        if (strength < 0.65) return "#eab308";
        if (strength < 0.85) return "#22c55e";
        return "#10b981";
    }

    /**
     * Génère un mot de passe fort de longueur donnée.
     */
    public String generateStrongPassword(int length) {
        if (length < 8) length = 8;
        if (length > 50) length = 50;

        StringBuilder password = new StringBuilder(length);

        // Garantir au moins un de chaque type
        password.append(UPPER.charAt(random.nextInt(UPPER.length())));
        password.append(LOWER.charAt(random.nextInt(LOWER.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SPECIAL.charAt(random.nextInt(SPECIAL.length())));

        // Remplir le reste aléatoirement
        for (int i = 4; i < length; i++) {
            password.append(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }

        // Mélanger les caractères
        char[] chars = password.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }

        return new String(chars);
    }

    /**
     * Génère un mot de passe fort de 14 caractères par défaut.
     */
    public String generateStrongPassword() {
        return generateStrongPassword(14);
    }
}
