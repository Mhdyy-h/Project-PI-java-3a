package org.example.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Consultation entity for BioSync
 * Transformed from PHP Consultation entity
 */
public class Consultation {
    private int id;
    private LocalDateTime dateConsultation;
    private String symptomes;
    private String diagnostic;
    private String recommandations;
    private RendezVous rendezVous;
    private List<Prescription> prescriptions;
    private String statut; // "en_cours", "terminee", "annulee"
    private LocalDateTime dateCreation;
    
    public Consultation() {
        this.prescriptions = new ArrayList<>();
        this.dateConsultation = LocalDateTime.now();
        this.dateCreation = LocalDateTime.now();
        this.statut = "en_cours";
    }
    
    public Consultation(RendezVous rendezVous) {
        this();
        this.rendezVous = rendezVous;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public LocalDateTime getDateConsultation() {
        return dateConsultation;
    }
    
    public void setDateConsultation(LocalDateTime dateConsultation) {
        this.dateConsultation = dateConsultation;
    }
    
    public String getSymptomes() {
        return symptomes;
    }
    
    public void setSymptomes(String symptomes) {
        this.symptomes = symptomes;
    }
    
    public String getDiagnostic() {
        return diagnostic;
    }
    
    public void setDiagnostic(String diagnostic) {
        this.diagnostic = diagnostic;
    }
    
    public String getRecommandations() {
        return recommandations;
    }
    
    public void setRecommandations(String recommandations) {
        this.recommandations = recommandations;
    }
    
    public RendezVous getRendezVous() {
        return rendezVous;
    }
    
    public void setRendezVous(RendezVous rendezVous) {
        this.rendezVous = rendezVous;
    }
    
    public List<Prescription> getPrescriptions() {
        return prescriptions;
    }
    
    public void setPrescriptions(List<Prescription> prescriptions) {
        this.prescriptions = prescriptions;
    }
    
    public String getStatut() {
        return statut;
    }
    
    public void setStatut(String statut) {
        this.statut = statut;
    }
    
    public LocalDateTime getDateCreation() {
        return dateCreation;
    }
    
    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
    
    // Prescription management methods
    public void addPrescription(Prescription prescription) {
        if (!prescriptions.contains(prescription)) {
            prescriptions.add(prescription);
            prescription.setConsultation(this);
        }
    }
    
    public void removePrescription(Prescription prescription) {
        if (prescriptions.remove(prescription)) {
            prescription.setConsultation(null);
        }
    }
    
    public boolean hasPrescriptions() {
        return !prescriptions.isEmpty();
    }
    
    public int getPrescriptionCount() {
        return prescriptions.size();
    }
    
    @Override
    public String toString() {
        return "Consultation{" +
                "id=" + id +
                ", dateConsultation=" + dateConsultation +
                ", symptomes='" + symptomes + '\'' +
                ", diagnostic='" + diagnostic + '\'' +
                ", rendezVous=" + (rendezVous != null ? rendezVous.getId() : "null") +
                ", prescriptionCount=" + prescriptions.size() +
                ", statut='" + statut + '\'' +
                '}';
    }
}
