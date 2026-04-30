package org.example.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RendezVous {
    private Integer id;
    private LocalDateTime dateHeure;
    private String motif;
    private String statut;
    private String mode;
    private String lieu;
    private Integer niveauUrgence;
    private Integer patientId;
    private Integer specialisteId;
    private String patientNom;
    private String specialisteNom;
    
    // Constructors
    public RendezVous() {}
    
    public RendezVous(LocalDateTime dateHeure, String motif, String statut, String mode, 
                     String lieu, Integer niveauUrgence, Integer patientId, Integer specialisteId) {
        this.dateHeure = dateHeure;
        this.motif = motif;
        this.statut = statut;
        this.mode = mode;
        this.lieu = lieu;
        this.niveauUrgence = niveauUrgence;
        this.patientId = patientId;
        this.specialisteId = specialisteId;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public LocalDateTime getDateHeure() {
        return dateHeure;
    }
    
    public void setDateHeure(LocalDateTime dateHeure) {
        this.dateHeure = dateHeure;
    }
    
    public String getMotif() {
        return motif;
    }
    
    public void setMotif(String motif) {
        this.motif = motif;
    }
    
    public String getStatut() {
        return statut;
    }
    
    public void setStatut(String statut) {
        this.statut = statut;
    }
    
    public String getMode() {
        return mode;
    }
    
    public void setMode(String mode) {
        this.mode = mode;
    }
    
    public String getLieu() {
        return lieu;
    }
    
    public void setLieu(String lieu) {
        this.lieu = lieu;
    }
    
    public Integer getNiveauUrgence() {
        return niveauUrgence;
    }
    
    public void setNiveauUrgence(Integer niveauUrgence) {
        this.niveauUrgence = niveauUrgence;
    }
    
    public Integer getPatientId() {
        return patientId;
    }
    
    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }
    
    public Integer getSpecialisteId() {
        return specialisteId;
    }
    
    public void setSpecialisteId(Integer specialisteId) {
        this.specialisteId = specialisteId;
    }
    
    public String getPatientNom() {
        return patientNom;
    }
    
    public void setPatientNom(String patientNom) {
        this.patientNom = patientNom;
    }
    
    public String getSpecialisteNom() {
        return specialisteNom;
    }
    
    public void setSpecialisteNom(String specialisteNom) {
        this.specialisteNom = specialisteNom;
    }
    
    // Utility methods
    public String getFormattedDateHeure() {
        if (dateHeure != null) {
            return dateHeure.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
        return "";
    }
    
    public String getFormattedDate() {
        if (dateHeure != null) {
            return dateHeure.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        return "";
    }
    
    public String getFormattedTime() {
        if (dateHeure != null) {
            return dateHeure.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        return "";
    }
    
    public boolean isToday() {
        if (dateHeure == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return dateHeure.toLocalDate().isEqual(now.toLocalDate());
    }
    
    public boolean isThisWeek() {
        if (dateHeure == null) return false;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekStart = now.with(java.time.DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
        LocalDateTime weekEnd = now.with(java.time.DayOfWeek.SUNDAY).toLocalDate().atTime(23, 59, 59);
        return !dateHeure.isBefore(weekStart) && !dateHeure.isAfter(weekEnd);
    }
    
    // JSON representation for API
    public String toJson() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\":").append(id != null ? id : "null").append(",");
        json.append("\"dateHeure\":\"").append(dateHeure != null ? dateHeure.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")) : "").append("\",");
        json.append("\"motif\":\"").append(motif != null ? motif.replace("\"", "\\\"") : "").append("\",");
        json.append("\"statut\":\"").append(statut != null ? statut : "").append("\",");
        json.append("\"mode\":\"").append(mode != null ? mode : "").append("\",");
        json.append("\"patientId\":").append(patientId != null ? patientId : "null").append(",");
        json.append("\"specialisteId\":").append(specialisteId != null ? specialisteId : "null").append(",");
        json.append("\"lieu\":\"").append(lieu != null ? lieu.replace("\"", "\\\"") : "").append("\",");
        json.append("\"niveauUrgence\":").append(niveauUrgence != null ? niveauUrgence : "null").append(",");
        json.append("\"patientNom\":\"").append(patientNom != null ? patientNom.replace("\"", "\\\"") : "").append("\",");
        json.append("\"specialisteNom\":\"").append(specialisteNom != null ? specialisteNom.replace("\"", "\\\"") : "").append("\"");
        json.append("}");
        return json.toString();
    }
    
    @Override
    public String toString() {
        return "RendezVous{" +
                "id=" + id +
                ", dateHeure=" + dateHeure +
                ", motif='" + motif + '\'' +
                ", statut='" + statut + '\'' +
                ", mode='" + mode + '\'' +
                ", patient='" + patientNom + '\'' +
                ", specialiste='" + specialisteNom + '\'' +
                '}';
    }
}
