package org.example.model;

public class Quiz {

    private int id;
    private String titre;
    private String description;
    private String categorie;
    private String difficulte;
    private int passingScore;
    private boolean actif;

    public Quiz() {}

    public Quiz(String titre, String description, String categorie,
                String difficulte, int passingScore, boolean actif) {
        this.titre        = titre;
        this.description  = description;
        this.categorie    = categorie;
        this.difficulte   = difficulte;
        this.passingScore = passingScore;
        this.actif        = actif;
    }

    // ── Getters / Setters ──────────────────────────────────────

    public int getId()                    { return id; }
    public void setId(int id)             { this.id = id; }

    public String getTitre()              { return titre; }
    public void setTitre(String titre)    { this.titre = titre; }

    public String getDescription()                    { return description; }
    public void setDescription(String description)    { this.description = description; }

    public String getCategorie()                  { return categorie; }
    public void setCategorie(String categorie)    { this.categorie = categorie; }

    public String getDifficulte()                   { return difficulte; }
    public void setDifficulte(String difficulte)    { this.difficulte = difficulte; }

    public int getPassingScore()                    { return passingScore; }
    public void setPassingScore(int passingScore)   { this.passingScore = passingScore; }

    public boolean isActif()              { return actif; }
    public void setActif(boolean actif)   { this.actif = actif; }

    @Override
    public String toString() { return titre != null ? titre : "—"; }
}
