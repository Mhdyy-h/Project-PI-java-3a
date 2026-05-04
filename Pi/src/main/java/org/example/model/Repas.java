package org.example.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Repas {
    private int id;
    private String titreRepas;
    private String typeMoment; // MATIN, MIDI, COLLATION, SOIR
    private LocalDateTime dateConsommation;
    private int pointsGagnes;
    private int utilisateurId;
    private List<Aliment> aliments;
    private List<Integer> quantites;

    // Constructeurs
    public Repas() {
        this.aliments = new ArrayList<>();
        this.quantites = new ArrayList<>();
    }

    public Repas(String titreRepas, String typeMoment, LocalDateTime dateConsommation, int utilisateurId) {
        this();
        this.titreRepas = titreRepas;
        this.typeMoment = typeMoment;
        this.dateConsommation = dateConsommation;
        this.utilisateurId = utilisateurId;
        this.pointsGagnes = 0;
    }

    public Repas(int id, String titreRepas, String typeMoment, LocalDateTime dateConsommation,
                 int pointsGagnes, int utilisateurId) {
        this();
        this.id = id;
        this.titreRepas = titreRepas;
        this.typeMoment = typeMoment;
        this.dateConsommation = dateConsommation;
        this.pointsGagnes = pointsGagnes;
        this.utilisateurId = utilisateurId;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitreRepas() { return titreRepas; }
    public void setTitreRepas(String titreRepas) { this.titreRepas = titreRepas; }
    public String getTypeMoment() { return typeMoment; }
    public void setTypeMoment(String typeMoment) { this.typeMoment = typeMoment; }
    public LocalDateTime getDateConsommation() { return dateConsommation; }
    public void setDateConsommation(LocalDateTime dateConsommation) { this.dateConsommation = dateConsommation; }
    public int getPointsGagnes() { return pointsGagnes; }
    public void setPointsGagnes(int pointsGagnes) { this.pointsGagnes = pointsGagnes; }
    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }
    public List<Aliment> getAliments() { return aliments; }
    public void setAliments(List<Aliment> aliments) { this.aliments = aliments; }
    public List<Integer> getQuantites() { return quantites; }
    public void setQuantites(List<Integer> quantites) { this.quantites = quantites; }

    public void addAliment(Aliment aliment, int quantite) {
        this.aliments.add(aliment);
        this.quantites.add(quantite);
    }

    // Calcul des totaux nutritionnels
    public int getTotalCalories() {
        int total = 0;
        for (int i = 0; i < aliments.size(); i++) {
            total += aliments.get(i).getCalories() * quantites.get(i);
        }
        return total;
    }

    public double getTotalProteines() {
        double total = 0;
        for (int i = 0; i < aliments.size(); i++) {
            total += aliments.get(i).getProteines() * quantites.get(i);
        }
        return total;
    }

    public double getTotalGlucides() {
        double total = 0;
        for (int i = 0; i < aliments.size(); i++) {
            total += aliments.get(i).getGlucides() * quantites.get(i);
        }
        return total;
    }

    public double getTotalLipides() {
        double total = 0;
        for (int i = 0; i < aliments.size(); i++) {
            total += aliments.get(i).getLipides() * quantites.get(i);
        }
        return total;
    }

    public boolean contientExcitant() {
        return aliments.stream().anyMatch(Aliment::isEstExcitant);
    }

    public String getHeureFormatee() {
        return dateConsommation.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String getDateFormatee() {
        return dateConsommation.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    @Override
    public String toString() {
        return titreRepas + " - " + typeMoment + " (" + getDateFormatee() + " " + getHeureFormatee() + ")";
    }
}