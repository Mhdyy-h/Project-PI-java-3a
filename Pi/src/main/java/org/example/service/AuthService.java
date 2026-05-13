package org.example.service;

import org.example.dao.ActivityLogDAO;
import org.example.dao.UserDAO;
import org.example.model.User;

public class AuthService {

    private static AuthService instance;
    private final ValidationService validationService = ValidationService.getInstance();

    private AuthService() {}

    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    public AuthResult register(String fullName, String email, String password, String confirmPassword, boolean termsAccepted) {
        // Validation du nom
        ValidationResult nameValid = validationService.validateName(fullName, "Le nom complet");
        if (nameValid.hasError()) {
            return AuthResult.failure(nameValid.getMessage());
        }

        // Validation de l'email
        ValidationResult emailValid = validationService.validateEmail(email);
        if (emailValid.hasError()) {
            return AuthResult.failure(emailValid.getMessage());
        }

        // Validation du mot de passe
        ValidationResult passwordValid = validationService.validatePassword(password, true);
        if (passwordValid.hasError()) {
            return AuthResult.failure(passwordValid.getMessage());
        }

        // Validation de la confirmation
        ValidationResult confirmValid = validationService.validatePasswordMatch(password, confirmPassword);
        if (confirmValid.hasError()) {
            return AuthResult.failure(confirmValid.getMessage());
        }

        if (!termsAccepted) {
            return AuthResult.failure("Veuillez accepter les conditions d'utilisation");
        }

        // Create user
        User newUser = new User(0, fullName, email, password);
        newUser.setRoles("[\"ROLE_USER\"]");

        boolean success = UserDAO.insertUser(newUser);

        if (success) {
            // Log the registration
            ActivityLogDAO.insertLog(new org.example.model.ActivityLog(
                newUser.getId(), fullName, email, "[\"ROLE_USER\"]", "Inscription"
            ));
            return AuthResult.success(newUser, "Compte créé avec succès!");
        } else {
            return AuthResult.failure("Erreur lors de la création du compte");
        }
    }

    public AuthResult login(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            return AuthResult.failure("Veuillez remplir tous les champs");
        }

        System.out.println("🔍 AUTH DEBUG: Attempting login for email: " + email);
        System.out.println("🔍 AUTH DEBUG: Password provided: " + password);

        User user = UserDAO.login(email, password);

        if (user != null) {
            System.out.println("✅ AUTH DEBUG: Login successful for user: " + user.getNomComplet());
            System.out.println("🔍 AUTH DEBUG: User roles: " + user.getRoles());
            System.out.println("🔍 AUTH DEBUG: isSpecialiste(): " + user.isSpecialiste());
            
            // Log successful login
            ActivityLogDAO.insertLog(new org.example.model.ActivityLog(
                user.getId(), user.getNomComplet(), user.getEmail(),
                user.getRoles() != null ? user.getRoles() : "",
                "Connexion réussie"
            ));
            return AuthResult.success(user, "Connexion réussie!");
        } else {
            System.out.println("❌ AUTH DEBUG: Login failed - UserDAO returned null");
            
            // Log failed login — try to find user by email to get the name
            User found = UserDAO.getUserByEmail(email);
            if (found != null) {
                System.out.println("🔍 AUTH DEBUG: Found user in database: " + found.getNomComplet());
                System.out.println("🔍 AUTH DEBUG: User roles from DB: " + found.getRoles());
                System.out.println("🔍 AUTH DEBUG: isSpecialiste() from DB: " + found.isSpecialiste());
            } else {
                System.out.println("❌ AUTH DEBUG: User not found in database for email: " + email);
            }
            
            String nom = found != null ? found.getNomComplet() : "Inconnu";
            String roles = found != null && found.getRoles() != null ? found.getRoles() : "";
            ActivityLogDAO.insertLog(new org.example.model.ActivityLog(
                found != null ? found.getId() : 0, nom, email, roles, "Connexion échouée"
            ));
            return AuthResult.failure("Email ou mot de passe incorrect");
        }
    }

    public AuthResult loginByFaceId(String email) {
        User user = UserDAO.getUserByEmail(email);
        if (user != null) {
            ActivityLogDAO.insertLog(new org.example.model.ActivityLog(
                user.getId(), user.getNomComplet(), user.getEmail(),
                user.getRoles() != null ? user.getRoles() : "",
                "Connexion Face ID"
            ));
            return AuthResult.success(user, "Connexion Face ID réussie!");
        }
        return AuthResult.failure("Utilisateur non trouvé pour ce Face ID");
    }

    public static class AuthResult {
        private final boolean success;
        private final User user;
        private final String message;

        private AuthResult(boolean success, User user, String message) {
            this.success = success;
            this.user = user;
            this.message = message;
        }

        public static AuthResult success(User user, String message) {
            return new AuthResult(true, user, message);
        }

        public static AuthResult failure(String message) {
            return new AuthResult(false, null, message);
        }

        public boolean isSuccess() { return success; }
        public User getUser() { return user; }
        public String getMessage() { return message; }
    }
}
