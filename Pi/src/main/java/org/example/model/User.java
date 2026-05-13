package org.example.model;

public class User {

    private int id;
    private String nomComplet;
    private String email;
    private String motDePasse;
    private String roles;
    private int scoreGlobal;
    private String dateInscription;
    private String photoProfil;

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

    public User(int id, String nomComplet, String email, String motDePasse,
                String roles, int scoreGlobal, String dateInscription) {

        this.id = id;
        this.nomComplet = nomComplet;
        this.email = email;
        this.motDePasse = motDePasse;
        this.roles = roles;
        this.scoreGlobal = scoreGlobal;
        this.dateInscription = dateInscription;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomComplet() {
        return nomComplet;
    }

    public void setNomComplet(String nomComplet) {
        this.nomComplet = nomComplet;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public int getScoreGlobal() {
        return scoreGlobal;
    }

    public void setScoreGlobal(int scoreGlobal) {
        this.scoreGlobal = scoreGlobal;
    }

    public String getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(String dateInscription) {
        this.dateInscription = dateInscription;
    }

    public String getPhotoProfil() {
        return photoProfil;
    }

    public void setPhotoProfil(String photoProfil) {
        this.photoProfil = photoProfil;
    }

    // Role checking methods

    public boolean isAdmin() {
        return roles != null && roles.contains("ROLE_ADMIN");
    }

    public boolean isSpecialiste() {

        if (roles == null) {
            System.out.println(" DEBUG: roles is null, returning false");
            return false;
        }

        System.out.println(" DEBUG: Checking roles: " + roles);

        // Handle both JSON array format and comma-separated format
        String[] rolesArray;
        if (roles.startsWith("[") && roles.endsWith("]")) {
            // JSON format: ["ROLE_SPECIALISTE","ROLE_USER"]
            String content = roles.substring(1, roles.length() - 1); // Remove [ and ]
            rolesArray = content.split(",");
            System.out.println(" DEBUG: JSON format detected, content: " + content);
        } else {
            // Comma-separated format: ROLE_SPECIALISTE,ROLE_USER
            rolesArray = roles.split(",");
            System.out.println(" DEBUG: Comma format detected");
        }

        for (String role : rolesArray) {
            // Clean up quotes and spaces
            String cleanRole = role.replace("[", "").replace("]", "").replace("\"", "").trim();
            System.out.println(" DEBUG: Checking role: '" + cleanRole + "'");

            if (cleanRole.contains("SPECIALISTE")
                    || cleanRole.contains("ROLE_SPECIALISTE")
                    || cleanRole.contains("dr.SPECIALISTE")) {

                System.out.println(" DEBUG: SPECIALISTE found! Returning true");
                return true;
            }
        }

        System.out.println(" DEBUG: No specialist role found, returning false");
        return false;
    }

    public boolean isPatient() {
        return roles != null && roles.contains("ROLE_USER");
    }

    @Override
    public String toString() {
        return "User{id=" + id +
                ", nomComplet='" + nomComplet + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}