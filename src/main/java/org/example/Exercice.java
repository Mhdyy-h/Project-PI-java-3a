package org.example;

public class Exercice {

    public int id;
    public String nomExercice;
    public String intensite;
    public double caloriesParMinute;
    public int seanceId;

    // Constructeur complet — existant
    public Exercice(int id, String nomExercice, String intensite,
                    double caloriesParMinute, int seanceId) {
        this.id = id;
        this.nomExercice = nomExercice;
        this.intensite = intensite;
        this.caloriesParMinute = caloriesParMinute;
        this.seanceId = seanceId;
    }

    // ✅ Constructeur vide — AJOUTÉ pour les tests
    public Exercice() {}

    // ✅ Getters
    public int getId() { return id; }
    public String getNomExercice() { return nomExercice; }
    public String getIntensite() { return intensite; }
    public double getCaloriesParMinute() { return caloriesParMinute; }
    public int getSeanceId() { return seanceId; }

    // ✅ Setters — AJOUTÉS pour les tests
    public void setId(int id) { this.id = id; }
    public void setNomExercice(String nomExercice) { this.nomExercice = nomExercice; }
    public void setIntensite(String intensite) { this.intensite = intensite; }
    public void setCaloriesParMinute(double caloriesParMinute) { this.caloriesParMinute = caloriesParMinute; }
    public void setSeanceId(int seanceId) { this.seanceId = seanceId; }

    public String toString() {
        return "id=" + id + " | nom=" + nomExercice + " | intensite=" + intensite
                + " | calories/min=" + caloriesParMinute + " | seance_id=" + seanceId;
    }
}