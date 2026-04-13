package org.example.model;

public class CertificationRequest {
    private int id;
    private String nomComplet;
    private String email;
    private String specialite;   // "COACH" or "SPECIALISTE"
    private String motivation;
    private String statut;       // "EN_ATTENTE", "ACCEPTE", "REFUSE"
    private String dateEnvoi;

    public CertificationRequest() {}

    public CertificationRequest(int id, String nomComplet, String email,
                                 String specialite, String motivation,
                                 String statut, String dateEnvoi) {
        this.id = id;
        this.nomComplet = nomComplet;
        this.email = email;
        this.specialite = specialite;
        this.motivation = motivation;
        this.statut = statut;
        this.dateEnvoi = dateEnvoi;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNomComplet() { return nomComplet; }
    public void setNomComplet(String nomComplet) { this.nomComplet = nomComplet; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    public String getMotivation() { return motivation; }
    public void setMotivation(String motivation) { this.motivation = motivation; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(String dateEnvoi) { this.dateEnvoi = dateEnvoi; }

    @Override
    public String toString() {
        return "CertificationRequest{id=" + id + ", email='" + email + "', specialite='" + specialite + "', statut='" + statut + "'}";
    }
}
