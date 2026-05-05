package org.example.model;

public class Groupe {
    private int id;
    private String nomGroupe;
    private String thematique;
    private String description;
    private int capaciteMax;
    private String image;

    public Groupe() {}

    public Groupe(int id, String nomGroupe, String thematique, String description, int capaciteMax, String image) {
        this.id = id;
        this.nomGroupe = nomGroupe;
        this.thematique = thematique;
        this.description = description;
        this.capaciteMax = capaciteMax;
        this.image = image;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNomGroupe() { return nomGroupe; }
    public void setNomGroupe(String nomGroupe) { this.nomGroupe = nomGroupe; }

    public String getThematique() { return thematique; }
    public void setThematique(String thematique) { this.thematique = thematique; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCapaciteMax() { return capaciteMax; }
    public void setCapaciteMax(int capaciteMax) { this.capaciteMax = capaciteMax; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}