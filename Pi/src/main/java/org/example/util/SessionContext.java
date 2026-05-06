package org.example.util;

import org.example.model.User;

/**
 * Holder singleton de l'utilisateur actuellement connecté.
 *
 * <p>Remplace l'absence de système d'authentification : une fois l'utilisateur
 * identifié (via {@code ServiceUtilisateur.connexion()}), il est stocké ici
 * et accessible depuis n'importe quel contrôleur sans passer par les FXML.</p>
 *
 * <p>Exemple d'utilisation dans un contrôleur :</p>
 * <pre>
 *   // Après connexion
 *   SessionContext.connecter(utilisateur);
 *
 *   // Dans n'importe quel service ou contrôleur
 *   Utilisateur moi = SessionContext.getUtilisateur();
 *   int monId      = SessionContext.getUtilisateurId();
 *
 *   // À la déconnexion
 *   SessionContext.deconnecter();
 * </pre>
 */
public final class SessionContext {

    private static User utilisateurConnecte;

    private SessionContext() {}

    /** Enregistre l'utilisateur après une connexion réussie. */
    public static void connecter(User utilisateur) {
        utilisateurConnecte = utilisateur;
    }

    /** Efface la session (déconnexion). */
    public static void deconnecter() {
        utilisateurConnecte = null;
    }

    /**
     * Retourne l'utilisateur connecté.
     *
     * @throws IllegalStateException si aucun utilisateur n'est connecté
     */
    public static User getUtilisateur() {
        if (utilisateurConnecte == null) {
            throw new IllegalStateException(
                "Aucun utilisateur connecté. Appeler SessionContext.connecter() d'abord.");
        }
        return utilisateurConnecte;
    }

    /**
     * Retourne l'ID de l'utilisateur connecté.
     *
     * @throws IllegalStateException si aucun utilisateur n'est connecté
     */
    public static int getUtilisateurId() {
        return getUtilisateur().getId();
    }

    /** Retourne {@code true} si un utilisateur est actuellement connecté. */
    public static boolean estConnecte() {
        return utilisateurConnecte != null;
    }

    /** Retourne {@code true} si l'utilisateur connecté est admin. */
   // public static boolean estAdmin() {return estConnecte() && utilisateurConnecte.isAdmin();
    }

    /**
     * Retourne l'utilisateur connecté, ou {@code null} si personne n'est connecté.
     * À utiliser quand on veut éviter l'exception (ex: vérification de garde).
     */
  //  public static User getUtilisateurOuNull() {return utilisateurConnecte;
  //  }}
