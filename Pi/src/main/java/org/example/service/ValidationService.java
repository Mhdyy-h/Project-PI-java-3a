package org.example.service;

import java.util.regex.Pattern;

public class ValidationService {
    
    private static ValidationService instance;
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-zÀ-ÖØ-öø-ÿ\\s'-]{2,100}$");
    private static final Pattern LICENSE_PATTERN = Pattern.compile("^[0-9]{6,15}$");
    
    private ValidationService() {}
    
    public static ValidationService getInstance() {
        if (instance == null) {
            instance = new ValidationService();
        }
        return instance;
    }
    
    public ValidationResult validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return ValidationResult.failure("L'adresse email est obligatoire");
        }
        
        email = email.trim();
        
        if (email.length() > 100) {
            return ValidationResult.failure("L'email ne doit pas dépasser 100 caractères");
        }
        
        // Vérification explicite des guillemets
        if (email.contains("\"") || email.contains("'")) {
            return ValidationResult.failure("L'email ne doit pas contenir de guillemets");
        }
        
        // Vérification des points consécutifs
        if (email.contains("..") || email.contains(".@") || email.startsWith(".") || email.endsWith(".")) {
            return ValidationResult.failure("L'email contient des points consécutifs ou mal placés");
        }
        
        // Vérification du domaine - extraire la partie après @
        String[] parts = email.split("@");
        if (parts.length == 2) {
            String domain = parts[1];
            // Compter les points dans le domaine
            int dotCount = domain.length() - domain.replace(".", "").length();
            // Domaines comme gmail.com.fr ont trop de niveaux (3 parties = 2 points après @)
            if (dotCount > 1) {
                return ValidationResult.failure("Le domaine de l'email n'est pas valide (ex: nom@gmail.com)");
            }
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ValidationResult.failure("Format d'email invalide (ex: nom@domaine.com)");
        }
        
        return ValidationResult.success();
    }
    
    public ValidationResult validatePassword(String password, boolean isRequired) {
        if (password == null || password.isEmpty()) {
            if (isRequired) {
                return ValidationResult.failure("Le mot de passe est obligatoire");
            }
            return ValidationResult.success();
        }
        
        if (password.length() < 8) {
            return ValidationResult.failure("Le mot de passe doit contenir au moins 8 caractères");
        }
        
        if (password.length() > 50) {
            return ValidationResult.failure("Le mot de passe ne doit pas dépasser 50 caractères");
        }
        
        boolean hasUppercase = !password.equals(password.toLowerCase());
        boolean hasDigit = password.matches(".*\\d.*");
        
        if (!hasUppercase) {
            return ValidationResult.failure("Le mot de passe doit contenir au moins une majuscule");
        }
        
        if (!hasDigit) {
            return ValidationResult.failure("Le mot de passe doit contenir au moins un chiffre");
        }
        
        return ValidationResult.success();
    }
    
    public ValidationResult validatePasswordMatch(String password, String confirmPassword) {
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            return ValidationResult.failure("La confirmation du mot de passe est obligatoire");
        }
        
        if (!password.equals(confirmPassword)) {
            return ValidationResult.failure("Les mots de passe ne correspondent pas");
        }
        
        return ValidationResult.success();
    }
    
    public ValidationResult validateName(String name, String fieldName) {
        if (name == null || name.trim().isEmpty()) {
            return ValidationResult.failure(fieldName + " est obligatoire");
        }
        
        name = name.trim();
        
        if (name.length() < 2) {
            return ValidationResult.failure(fieldName + " doit contenir au moins 2 caractères");
        }
        
        if (name.length() > 100) {
            return ValidationResult.failure(fieldName + " ne doit pas dépasser 100 caractères");
        }
        
        // Vérification explicite des chiffres
        if (name.matches(".*[0-9].*")) {
            return ValidationResult.failure(fieldName + " ne doit pas contenir de chiffres");
        }
        
        if (!NAME_PATTERN.matcher(name).matches()) {
            return ValidationResult.failure(fieldName + " contient des caractères invalides (lettres, espaces, tirets et apostrophes uniquement)");
        }
        
        return ValidationResult.success();
    }
    
    public ValidationResult validateLicenseNumber(String license) {
        if (license == null || license.trim().isEmpty()) {
            return ValidationResult.failure("Le numéro de licence est obligatoire");
        }
        
        license = license.trim();
        
        if (!LICENSE_PATTERN.matcher(license).matches()) {
            return ValidationResult.failure("Le numéro de licence doit contenir entre 6 et 15 chiffres");
        }
        
        return ValidationResult.success();
    }
    
    public ValidationResult validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return ValidationResult.failure(fieldName + " est obligatoire");
        }
        
        return ValidationResult.success();
    }
    
    public ValidationResult validateComboBoxSelection(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return ValidationResult.failure("Veuillez sélectionner " + fieldName);
        }
        
        return ValidationResult.success();
    }
}
