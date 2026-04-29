package org.example.model;

import java.math.BigDecimal;

public class Aliment {
    private int id;
    private String nomAliment;
    private int calories;
    private double proteines;
    private double glucides;
    private double lipides;
    private int indexGlycemique;
    private boolean estExcitant;
    private String typeAliment;
    private String multiScore;
    private String nutriScore;

    // Constructeurs
    public Aliment() {}

    public Aliment(String nomAliment, int calories, double proteines, double glucides, double lipides) {
        this.nomAliment = nomAliment;
        this.calories = calories;
        this.proteines = proteines;
        this.glucides = glucides;
        this.lipides = lipides;
        this.indexGlycemique = 0;
        this.estExcitant = false;
    }

    public Aliment(int id, String nomAliment, int calories, double proteines, double glucides,
                   double lipides, int indexGlycemique, boolean estExcitant, String typeAliment,
                   String multiScore, String nutriScore) {
        this.id = id;
        this.nomAliment = nomAliment;
        this.calories = calories;
        this.proteines = proteines;
        this.glucides = glucides;
        this.lipides = lipides;
        this.indexGlycemique = indexGlycemique;
        this.estExcitant = estExcitant;
        this.typeAliment = typeAliment;
        this.multiScore = multiScore;
        this.nutriScore = nutriScore;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNomAliment() { return nomAliment; }
    public void setNomAliment(String nomAliment) { this.nomAliment = nomAliment; }
    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }
    public double getProteines() { return proteines; }
    public void setProteines(double proteines) { this.proteines = proteines; }
    public double getGlucides() { return glucides; }
    public void setGlucides(double glucides) { this.glucides = glucides; }
    public double getLipides() { return lipides; }
    public void setLipides(double lipides) { this.lipides = lipides; }
    public int getIndexGlycemique() { return indexGlycemique; }
    public void setIndexGlycemique(int indexGlycemique) { this.indexGlycemique = indexGlycemique; }
    public boolean isEstExcitant() { return estExcitant; }
    public void setEstExcitant(boolean estExcitant) { this.estExcitant = estExcitant; }
    public String getTypeAliment() { return typeAliment; }
    public void setTypeAliment(String typeAliment) { this.typeAliment = typeAliment; }
    public String getMultiScore() { return multiScore; }
    public void setMultiScore(String multiScore) { this.multiScore = multiScore; }
    public String getNutriScore() { return nutriScore; }
    public void setNutriScore(String nutriScore) { this.nutriScore = nutriScore; }

    // Calcul des macros en pourcentage
    public double getPourcentageProteines() {
        if (calories == 0) return 0;
        return (proteines * 4) / calories * 100;
    }

    public double getPourcentageGlucides() {
        if (calories == 0) return 0;
        return (glucides * 4) / calories * 100;
    }

    public double getPourcentageLipides() {
        if (calories == 0) return 0;
        return (lipides * 9) / calories * 100;
    }

    @Override
    public String toString() {
        return nomAliment + " (" + calories + " cal)";
    }
}