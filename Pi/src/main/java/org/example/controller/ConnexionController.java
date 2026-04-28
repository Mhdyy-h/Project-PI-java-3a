package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.model.Utilisateur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.service.ServiceUtilisateur;
import org.example.util.SessionContext;

/**
 * Contrôleur de l'écran de connexion / inscription ({@code Connexion.fxml}).
 *
 * <p>Après une connexion réussie, l'utilisateur est stocké dans {@link SessionContext}
 * et redirigé vers le menu principal.</p>
 */
public class ConnexionController {

    private static final Logger log = LoggerFactory.getLogger(ConnexionController.class);

    // ── Connexion ────────────────────────────────────────────────
    @FXML private VBox       panneauConnexion;
    @FXML private TextField  champEmail;
    @FXML private PasswordField champMotDePasse;
    @FXML private Label      msgErreurConnexion;

    // ── Inscription ──────────────────────────────────────────────
    @FXML private VBox       panneauInscription;
    @FXML private TextField  champNom;
    @FXML private TextField  champPrenom;
    @FXML private TextField  champEmailInscription;
    @FXML private PasswordField champMdpInscription;
    @FXML private Label      msgErreurInscription;

    // ── Onglets ──────────────────────────────────────────────────
    @FXML private Button     btnOngletConnexion;
    @FXML private Button     btnOngletInscription;

    private final ServiceUtilisateur serviceUtilisateur = new ServiceUtilisateur();

    @FXML
    public void initialize() {
        creerCompteDemo();
        // Soumission au Enter sur le champ mot de passe
        champMotDePasse.setOnAction(e -> seConnecter());
    }

    // ════════════════════════════════════════════════════════════════
    // Connexion
    // ════════════════════════════════════════════════════════════════

    @FXML
    public void seConnecter() {
        String email = champEmail.getText().trim();
        String mdp   = champMotDePasse.getText();

        if (email.isEmpty() || mdp.isEmpty()) {
            afficherErreurConnexion("Veuillez remplir tous les champs.");
            return;
        }

        Utilisateur utilisateur = serviceUtilisateur.connexion(email, mdp);
        if (utilisateur == null) {
            afficherErreurConnexion("Email ou mot de passe incorrect.");
            champMotDePasse.clear();
            return;
        }

        SessionContext.connecter(utilisateur);
        log.info("Utilisateur connecté : {}", utilisateur.getEmail());
        naviguerVersMenu();
    }

    // ════════════════════════════════════════════════════════════════
    // Inscription
    // ════════════════════════════════════════════════════════════════

    @FXML
    public void sInscrire() {
        String nom    = champNom.getText().trim();
        String prenom = champPrenom.getText().trim();
        String email  = champEmailInscription.getText().trim();
        String mdp    = champMdpInscription.getText();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || mdp.isEmpty()) {
            afficherErreurInscription("Veuillez remplir tous les champs.", false);
            return;
        }
        if (!email.contains("@")) {
            afficherErreurInscription("Adresse email invalide.", false);
            return;
        }
        if (mdp.length() < 6) {
            afficherErreurInscription("Le mot de passe doit contenir au moins 6 caractères.", false);
            return;
        }

        Utilisateur u = serviceUtilisateur.inscrire(nom, prenom, email, mdp);
        if (u == null) {
            afficherErreurInscription("Cet email est déjà utilisé.", false);
            return;
        }

        afficherErreurInscription("Compte créé ! Vous pouvez vous connecter.", true);
        afficherConnexion();
        champEmail.setText(email);
    }

    // ════════════════════════════════════════════════════════════════
    // Onglets
    // ════════════════════════════════════════════════════════════════

    @FXML
    public void afficherConnexion() {
        panneauConnexion.setVisible(true);
        panneauConnexion.setManaged(true);
        panneauInscription.setVisible(false);
        panneauInscription.setManaged(false);
        styleOngletActif(btnOngletConnexion, btnOngletInscription);
    }

    @FXML
    public void afficherInscription() {
        panneauInscription.setVisible(true);
        panneauInscription.setManaged(true);
        panneauConnexion.setVisible(false);
        panneauConnexion.setManaged(false);
        styleOngletActif(btnOngletInscription, btnOngletConnexion);
    }

    // ════════════════════════════════════════════════════════════════
    // Utilitaires
    // ════════════════════════════════════════════════════════════════

    private void afficherErreurConnexion(String msg) {
        msgErreurConnexion.setText(msg);
        msgErreurConnexion.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px;");
    }

    private void afficherErreurInscription(String msg, boolean succes) {
        msgErreurInscription.setText(msg);
        String couleur = succes ? "#16a34a" : "#dc2626";
        msgErreurInscription.setStyle("-fx-text-fill: " + couleur + "; -fx-font-size: 12px;");
    }

    private void styleOngletActif(Button actif, Button inactif) {
        actif.setStyle("-fx-background-color: #1a0f4f; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-background-radius: 8; "
                + "-fx-padding: 8 24; -fx-cursor: hand;");
        inactif.setStyle("-fx-background-color: transparent; -fx-text-fill: #6b7280; "
                + "-fx-background-radius: 8; -fx-padding: 8 24; -fx-cursor: hand;");
    }

    /** Crée le compte de démonstration s'il n'existe pas encore. */
    private void creerCompteDemo() {
        try {
            if (serviceUtilisateur.getByEmail("demo@biosync.fr") == null) {
                serviceUtilisateur.inscrire("Demo", "Utilisateur", "demo@biosync.fr", "demo123");
            }
        } catch (Exception e) {
            log.debug("Compte démo déjà existant ou table absente : {}", e.getMessage());
        }
    }

    private void naviguerVersMenu() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Menu.fxml"));
            Stage stage = (Stage) champEmail.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            log.error("Erreur navigation vers Menu : {}", e.getMessage());
        }
    }
}
