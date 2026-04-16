package org.example.model;

public class Specialiste {
    private Integer id;
    private String nomDocteur;
    private String specialite;
    private String telephone;
    private String disponibilite;
    private String adresse;
    private String ville;
    private Integer utilisateurId;
    private String email;
    private Integer note;
    
    // Constructors
    public Specialiste() {}
    
    public Specialiste(String nomDocteur, String specialite, String telephone, 
                      String disponibilite, String adresse, String ville, Integer utilisateurId) {
        this.nomDocteur = nomDocteur;
        this.specialite = specialite;
        this.telephone = telephone;
        this.disponibilite = disponibilite;
        this.adresse = adresse;
        this.ville = ville;
        this.utilisateurId = utilisateurId;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getNomDocteur() {
        return nomDocteur;
    }
    
    public void setNomDocteur(String nomDocteur) {
        this.nomDocteur = nomDocteur;
    }
    
    public String getSpecialite() {
        return specialite;
    }
    
    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }
    
    public String getTelephone() {
        return telephone;
    }
    
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
    
    public String getDisponibilite() {
        return disponibilite;
    }
    
    public void setDisponibilite(String disponibilite) {
        this.disponibilite = disponibilite;
    }
    
    public String getAdresse() {
        return adresse;
    }
    
    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
    
    public String getVille() {
        return ville;
    }
    
    public void setVille(String ville) {
        this.ville = ville;
    }
    
    public Integer getUtilisateurId() {
        return utilisateurId;
    }
    
    public void setUtilisateurId(Integer utilisateurId) {
        this.utilisateurId = utilisateurId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Integer getNote() {
        return note;
    }
    
    public void setNote(Integer note) {
        this.note = note;
    }
    
    // Utility methods
    public String getDisplayName() {
        return nomDocteur + " - " + specialite;
    }
    
    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        if (adresse != null && !adresse.trim().isEmpty()) {
            address.append(adresse);
        }
        if (ville != null && !ville.trim().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(ville);
        }
        return address.toString();
    }
    
    @Override
    public String toString() {
        return "Specialiste{" +
                "id=" + id +
                ", nomDocteur='" + nomDocteur + '\'' +
                ", specialite='" + specialite + '\'' +
                ", telephone='" + telephone + '\'' +
                ", ville='" + ville + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
