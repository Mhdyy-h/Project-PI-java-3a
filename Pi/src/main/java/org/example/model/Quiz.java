package org.example.model;

import java.sql.Timestamp;

public class Quiz {

    private int id;
    private String titre;
    private int niveauStressCible;
    private int scoreResultat;
    private String medailleQuiz;
    private Timestamp dateQuiz;
    private int utilisateurId;
    private String statut;
    private Double tempsMoyenReponse;
    private String agiliteCognitive;

    public Quiz() {}

    public Quiz(String titre, int niveauStressCible, String statut) {
        this.titre = titre;
        this.niveauStressCible = niveauStressCible;
        this.statut = statut;
    }

    // ── Getters / Setters ──────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public int getNiveauStressCible() { return niveauStressCible; }
    public void setNiveauStressCible(int niveauStressCible) { this.niveauStressCible = niveauStressCible; }

    public int getScoreResultat() { return scoreResultat; }
    public void setScoreResultat(int scoreResultat) { this.scoreResultat = scoreResultat; }

    public String getMedailleQuiz() { return medailleQuiz; }
    public void setMedailleQuiz(String medailleQuiz) { this.medailleQuiz = medailleQuiz; }

    public Timestamp getDateQuiz() { return dateQuiz; }
    public void setDateQuiz(Timestamp dateQuiz) { this.dateQuiz = dateQuiz; }

    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Double getTempsMoyenReponse() { return tempsMoyenReponse; }
    public void setTempsMoyenReponse(Double tempsMoyenReponse) { this.tempsMoyenReponse = tempsMoyenReponse; }

    public String getAgiliteCognitive() { return agiliteCognitive; }
    public void setAgiliteCognitive(String agiliteCognitive) { this.agiliteCognitive = agiliteCognitive; }

    // ── Méthodes de compatibilité (alias pour ancien code) ──────────────────────────────────────

    public String getCategorie() { return getNiveauStressLabel(); }
    public void setCategorie(String categorie) {
        this.niveauStressCible = parseNiveauStressLabel(categorie);
    }

    public String getDifficulte() { return getNiveauStressLabel(); }
    public void setDifficulte(String difficulte) {
        this.niveauStressCible = parseNiveauStressLabel(difficulte);
    }

    private String getNiveauStressLabel() {
        switch (niveauStressCible) {
            case 0: return "Faible";
            case 1: case 2: return "Léger";
            case 3: case 4: case 5: return "Modéré";
            case 6: case 7: return "Élevé";
            case 8: case 9: case 10: return "Sévère";
            default: return "Modéré";
        }
    }

    private int parseNiveauStressLabel(String label) {
        if (label == null) return 0;
        switch (label.toLowerCase()) {
            case "faible": return 0;
            case "léger": case "leger": return 2;
            case "modéré": case "modere": return 5;
            case "élevé": case "eleve": return 7;
            case "sévère": case "severe": return 10;
            default: 
                try { return Integer.parseInt(label); } catch (Exception e) { return 0; }
        }
    }

    public int getPassingScore() { return scoreResultat; }
    public void setPassingScore(int passingScore) { this.scoreResultat = passingScore; }

    public String getDescription() { return agiliteCognitive; }
    public void setDescription(String description) { this.agiliteCognitive = description; }

    public boolean isActif() { return "disponible".equalsIgnoreCase(statut); }
    public void setActif(boolean actif) { this.statut = actif ? "disponible" : "complete"; }

    @Override
    public String toString() { return titre != null ? titre : "—"; }
}
