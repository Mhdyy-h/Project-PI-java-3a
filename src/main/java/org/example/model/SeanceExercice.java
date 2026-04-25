package org.example.model;

public class SeanceExercice {
    private int id;
    private int seanceId;
    private int exerciceId;
    private Exercice exercice;
    private int ordre;
    private int series;
    private int repetitions;

    public SeanceExercice() {}

    public SeanceExercice(int seanceId, int exerciceId, int ordre, int series, int repetitions) {
        this.seanceId = seanceId;
        this.exerciceId = exerciceId;
        this.ordre = ordre;
        this.series = series;
        this.repetitions = repetitions;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSeanceId() { return seanceId; }
    public void setSeanceId(int seanceId) { this.seanceId = seanceId; }
    public int getExerciceId() { return exerciceId; }
    public void setExerciceId(int exerciceId) { this.exerciceId = exerciceId; }
    public Exercice getExercice() { return exercice; }
    public void setExercice(Exercice exercice) { this.exercice = exercice; }
    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }
    public int getSeries() { return series; }
    public void setSeries(int series) { this.series = series; }
    public int getRepetitions() { return repetitions; }
    public void setRepetitions(int repetitions) { this.repetitions = repetitions; }
}