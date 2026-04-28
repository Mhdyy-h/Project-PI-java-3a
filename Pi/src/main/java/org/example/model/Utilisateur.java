package org.example.model;

import java.time.LocalDateTime;

/**
 * Représente un utilisateur de la plateforme.
 * Correspond à la table {@code utilisateur} dans biosync.
 *
 * <p>Exemple d'utilisation :</p>
 * <pre>
 *   Utilisateur u = new Utilisateur("Dupont", "Alice", "alice@example.com", "hashBcrypt");
 *   serviceUtilisateur.ajouter(u);
 * </pre>
 */
public class Utilisateur {

    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String role;
    private LocalDateTime dateCreation;

    public Utilisateur() {
        this.role = "ETUDIANT";
    }

    public Utilisateur(String nom, String prenom, String email, String motDePasse) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = "ETUDIANT";
        this.dateCreation = LocalDateTime.now();
    }

    public Utilisateur(int id, String nom, String prenom, String email,
                       String motDePasse, String role, LocalDateTime dateCreation) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
        this.dateCreation = dateCreation;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public String getNomComplet() { return prenom + " " + nom; }

    public boolean isAdmin() { return "ADMIN".equals(role); }

    @Override
    public String toString() {
        return "Utilisateur{id=" + id + ", email='" + email + "', role='" + role + "'}";
    }
}
