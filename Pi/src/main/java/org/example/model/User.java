package org.example.model;

public class User {
    private int id;
    private String nomComplet;
    private String email;
    private String motDePasse;
    private String roles;
    private int scoreGlobal;
    private String dateInscription;
    private String photoProfil;   // chemin vers la photo de profil

    public User() {}

    public User(int id, String nomComplet, String email) {
        this.id = id;
        this.nomComplet = nomComplet;
        this.email = email;
    }

    public User(int id, String nomComplet, String email, String motDePasse) {
        this.id = id;
        this.nomComplet = nomComplet;
        this.email = email;
        this.motDePasse = motDePasse;
    }

    public User(int id, String nomComplet, String email, String motDePasse, String roles) {
        this.id = id;
        this.nomComplet = nomComplet;
        this.email = email;
        this.motDePasse = motDePasse;
        this.roles = roles;
    }

    public User(int id, String nomComplet, String email, String motDePasse, String roles, int scoreGlobal, String dateInscription) {
        this.id = id;
        this.nomComplet = nomComplet;
        this.email = email;
        this.motDePasse = motDePasse;
        this.roles = roles;
        this.scoreGlobal = scoreGlobal;
        this.dateInscription = dateInscription;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNomComplet() { return nomComplet; }
    public void setNomComplet(String nomComplet) { this.nomComplet = nomComplet; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }

    public int getScoreGlobal() { return scoreGlobal; }
    public void setScoreGlobal(int scoreGlobal) { this.scoreGlobal = scoreGlobal; }

    public String getDateInscription() { return dateInscription; }
    public void setDateInscription(String dateInscription) { this.dateInscription = dateInscription; }

    public String getPhotoProfil() { return photoProfil; }
    public void setPhotoProfil(String photoProfil) { this.photoProfil = photoProfil; }

    @Override
    public String toString() {
        return "User{id=" + id + ", nomComplet='" + nomComplet + "', email='" + email + "'}";
    }
}