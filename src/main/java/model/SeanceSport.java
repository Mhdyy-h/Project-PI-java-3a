package model;

public class SeanceSport {

    public int id;
    public String nomSeance;
    public String heureDebut;
    public int dureeMinutes;
    public String medailleObtenue;
    public String dateSeance;
    public int utilisateurId;
    public String heureDebutReelle;
    public int alerteEnvoyee;

    public SeanceSport() {}

    public SeanceSport(int id, String nomSeance, String heureDebut,
                       int dureeMinutes, String medailleObtenue,
                       String dateSeance, int utilisateurId,
                       String heureDebutReelle, int alerteEnvoyee) {
        this.id = id;
        this.nomSeance = nomSeance;
        this.heureDebut = heureDebut;
        this.dureeMinutes = dureeMinutes;
        this.medailleObtenue = medailleObtenue;
        this.dateSeance = dateSeance;
        this.utilisateurId = utilisateurId;
        this.heureDebutReelle = heureDebutReelle;
        this.alerteEnvoyee = alerteEnvoyee;
    }

    // ── Getters ───────────────────────────────────────────────

    public int getId()                  { return id; }
    public String getNomSeance()        { return nomSeance; }
    public String getHeureDebut()       { return heureDebut; }
    public int getDureeMinutes()        { return dureeMinutes; }
    public String getMedailleObtenue()  { return medailleObtenue; }
    public String getDateSeance()       { return dateSeance; }
    public int getUtilisateurId()       { return utilisateurId; }
    public String getHeureDebutReelle() { return heureDebutReelle; }
    public int getAlerteEnvoyee()       { return alerteEnvoyee; }

    // ── Setters ───────────────────────────────────────────────

    public void setId(int id)                           { this.id = id; }
    public void setNomSeance(String nomSeance)          { this.nomSeance = nomSeance; }
    public void setHeureDebut(String heureDebut)        { this.heureDebut = heureDebut; }
    public void setDureeMinutes(int dureeMinutes)       { this.dureeMinutes = dureeMinutes; }
    public void setMedailleObtenue(String med)          { this.medailleObtenue = med; }
    public void setDateSeance(String dateSeance)        { this.dateSeance = dateSeance; }
    public void setUtilisateurId(int utilisateurId)     { this.utilisateurId = utilisateurId; }
    public void setHeureDebutReelle(String h)           { this.heureDebutReelle = h; }
    public void setAlerteEnvoyee(int alerteEnvoyee)    { this.alerteEnvoyee = alerteEnvoyee; }

    // ── toString ──────────────────────────────────────────────

    public String toString() {
        return "id=" + id + " | nom=" + nomSeance + " | date=" + dateSeance
                + " | duree=" + dureeMinutes + "min";
    }
}