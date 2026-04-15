package org.example.service;

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
            return AuthResult.success(newUser, "Compte créé avec succès!");
        } else {
            return AuthResult.failure("Erreur lors de la création du compte");
        }
    }

    public AuthResult login(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            return AuthResult.failure("Veuillez remplir tous les champs");
        }

        User user = UserDAO.login(email, password);

        if (user != null) {
            return AuthResult.success(user, "Connexion réussie!");
        } else {
            return AuthResult.failure("Email ou mot de passe incorrect");
        }
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

        public boolean isSuccess() {
            return success;
        }

        public User getUser() {
            return user;
        }

        public String getMessage() {
            return message;
        }
    }
}
