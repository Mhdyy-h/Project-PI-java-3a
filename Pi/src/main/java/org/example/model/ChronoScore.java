package org.example.model;

public class ChronoScore {
    private int timingScore;
    private int nutritionScore;
    private int equilibreBonus;
    private int interactionScore;
    private int riskPenalty;
    private int totalScore;
    private String appreciation;
    private String couleur;
    private String messageRisque;

    public ChronoScore() {
        this.timingScore = 0;
        this.nutritionScore = 0;
        this.equilibreBonus = 0;
        this.interactionScore = 0;
        this.riskPenalty = 0;
        this.totalScore = 0;
        this.appreciation = "";
        this.couleur = "#6b7280";
        this.messageRisque = "";
    }

    // Getters et Setters
    public int getTimingScore() { return timingScore; }
    public void setTimingScore(int timingScore) { this.timingScore = timingScore; }
    public int getNutritionScore() { return nutritionScore; }
    public void setNutritionScore(int nutritionScore) { this.nutritionScore = nutritionScore; }
    public int getEquilibreBonus() { return equilibreBonus; }
    public void setEquilibreBonus(int equilibreBonus) { this.equilibreBonus = equilibreBonus; }
    public int getInteractionScore() { return interactionScore; }
    public void setInteractionScore(int interactionScore) { this.interactionScore = interactionScore; }
    public int getRiskPenalty() { return riskPenalty; }
    public void setRiskPenalty(int riskPenalty) { this.riskPenalty = riskPenalty; }
    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }
    public String getAppreciation() { return appreciation; }
    public void setAppreciation(String appreciation) { this.appreciation = appreciation; }
    public String getCouleur() { return couleur; }
    public void setCouleur(String couleur) { this.couleur = couleur; }
    public String getMessageRisque() { return messageRisque; }
    public void setMessageRisque(String messageRisque) { this.messageRisque = messageRisque; }

    public void calculerAppreciation() {
        if (totalScore >= 12) {
            appreciation = "Excellent ! 🎉";
            couleur = "#27ae60";
        } else if (totalScore >= 8) {
            appreciation = "Bon 👍";
            couleur = "#4C6FFF";
        } else if (totalScore >= 4) {
            appreciation = "Moyen ⚠️";
            couleur = "#f39c12";
        } else {
            appreciation = "À améliorer ❌";
            couleur = "#e74c3c";
        }
    }
}