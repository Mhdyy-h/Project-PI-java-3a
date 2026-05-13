package org.example.model;

import java.time.LocalDateTime;

/**
 * Prescription entity for BioSync
 * Transformed from PHP Prescription entity
 */
public class Prescription {
    private int id;
    private String nomMedicament;
    private String dose;
    private String frequence;
    private int duree;
    private String instructions;
    private Consultation consultation;
    private LocalDateTime dateCreation;
    private Integer patientId; // Direct link to patient for role-based filtering
    private Integer specialisteId; // Direct link to specialist for role-based filtering
    
    public Prescription() {
        this.dateCreation = LocalDateTime.now();
    }
    
    public Prescription(String nomMedicament, String dose, String frequence, int duree, String instructions) {
        this();
        this.nomMedicament = nomMedicament;
        this.dose = dose;
        this.frequence = frequence;
        this.duree = duree;
        this.instructions = instructions;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getNomMedicament() {
        return nomMedicament;
    }
    
    public void setNomMedicament(String nomMedicament) {
        this.nomMedicament = nomMedicament;
    }
    
    public String getDose() {
        return dose;
    }
    
    public void setDose(String dose) {
        this.dose = dose;
    }
    
    public String getFrequence() {
        return frequence;
    }
    
    public void setFrequence(String frequence) {
        this.frequence = frequence;
    }
    
    public int getDuree() {
        return duree;
    }
    
    public void setDuree(int duree) {
        this.duree = duree;
    }
    
    public String getInstructions() {
        return instructions;
    }
    
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
    
    public Consultation getConsultation() {
        return consultation;
    }
    
    public void setConsultation(Consultation consultation) {
        this.consultation = consultation;
    }
    
    public LocalDateTime getDateCreation() {
        return dateCreation;
    }
    
    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
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
    
    @Override
    public String toString() {
        return "Prescription{" +
                "id=" + id +
                ", nomMedicament='" + nomMedicament + '\'' +
                ", dose='" + dose + '\'' +
                ", frequence='" + frequence + '\'' +
                ", duree=" + duree +
                ", consultation=" + (consultation != null ? consultation.getId() : "null") +
                '}';
    }
}
