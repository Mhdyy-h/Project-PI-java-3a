package org.example.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Profil de santé mentale complet pour un utilisateur
 * Agrège les données des quiz, sessions et analyses IA
 */
public class MentalHealthProfile {
    
    private int userId;
    private int stressLevel;           // 1-10
    private int anxietyLevel;          // 1-10
    private int depressionLevel;       // 1-10
    private int wellbeingScore;        // 0-100
    private int resilienceScore;       // 0-100
    private String emotionalState;     // "calm", "anxious", "stressed", "depressed", "happy"
    private String riskLevel;          // "low", "medium", "high", "critical"
    
    // Tendances (évolution sur 7/30 jours)
    private String stressTrend;        // "improving", "stable", "declining"
    private String anxietyTrend;
    private String wellbeingTrend;
    
    // Recommandations IA
    private String primaryRecommendation;
    private String secondaryRecommendation;
    private String urgentAction;       // Si risque élevé
    
    // Métadonnées
    private LocalDateTime lastAssessment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Scores détaillés par catégorie
    private Map<String, Integer> categoryScores;
    
    public MentalHealthProfile() {
        this.categoryScores = new HashMap<>();
    }
    
    public MentalHealthProfile(int userId) {
        this.userId = userId;
        this.categoryScores = new HashMap<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public int getStressLevel() {
        return stressLevel;
    }
    
    public void setStressLevel(int stressLevel) {
        this.stressLevel = Math.max(1, Math.min(10, stressLevel));
    }
    
    public int getAnxietyLevel() {
        return anxietyLevel;
    }
    
    public void setAnxietyLevel(int anxietyLevel) {
        this.anxietyLevel = Math.max(1, Math.min(10, anxietyLevel));
    }
    
    public int getDepressionLevel() {
        return depressionLevel;
    }
    
    public void setDepressionLevel(int depressionLevel) {
        this.depressionLevel = Math.max(1, Math.min(10, depressionLevel));
    }
    
    public int getWellbeingScore() {
        return wellbeingScore;
    }
    
    public void setWellbeingScore(int wellbeingScore) {
        this.wellbeingScore = Math.max(0, Math.min(100, wellbeingScore));
    }
    
    public int getResilienceScore() {
        return resilienceScore;
    }
    
    public void setResilienceScore(int resilienceScore) {
        this.resilienceScore = Math.max(0, Math.min(100, resilienceScore));
    }
    
    public String getEmotionalState() {
        return emotionalState;
    }
    
    public void setEmotionalState(String emotionalState) {
        this.emotionalState = emotionalState;
    }
    
    public String getRiskLevel() {
        return riskLevel;
    }
    
    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }
    
    public String getStressTrend() {
        return stressTrend;
    }
    
    public void setStressTrend(String stressTrend) {
        this.stressTrend = stressTrend;
    }
    
    public String getAnxietyTrend() {
        return anxietyTrend;
    }
    
    public void setAnxietyTrend(String anxietyTrend) {
        this.anxietyTrend = anxietyTrend;
    }
    
    public String getWellbeingTrend() {
        return wellbeingTrend;
    }
    
    public void setWellbeingTrend(String wellbeingTrend) {
        this.wellbeingTrend = wellbeingTrend;
    }
    
    public String getPrimaryRecommendation() {
        return primaryRecommendation;
    }
    
    public void setPrimaryRecommendation(String primaryRecommendation) {
        this.primaryRecommendation = primaryRecommendation;
    }
    
    public String getSecondaryRecommendation() {
        return secondaryRecommendation;
    }
    
    public void setSecondaryRecommendation(String secondaryRecommendation) {
        this.secondaryRecommendation = secondaryRecommendation;
    }
    
    public String getUrgentAction() {
        return urgentAction;
    }
    
    public void setUrgentAction(String urgentAction) {
        this.urgentAction = urgentAction;
    }
    
    public LocalDateTime getLastAssessment() {
        return lastAssessment;
    }
    
    public void setLastAssessment(LocalDateTime lastAssessment) {
        this.lastAssessment = lastAssessment;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Map<String, Integer> getCategoryScores() {
        return categoryScores;
    }
    
    public void setCategoryScores(Map<String, Integer> categoryScores) {
        this.categoryScores = categoryScores;
    }
    
    public void addCategoryScore(String category, int score) {
        this.categoryScores.put(category, score);
    }
    
    public Integer getCategoryScore(String category) {
        return this.categoryScores.getOrDefault(category, 0);
    }
    
    /**
     * Calcule le score global de santé mentale (0-100)
     */
    public int calculateOverallScore() {
        // Formule: 100 - (stress*3 + anxiety*3 + depression*4) / 10
        int negativeScore = (stressLevel * 3 + anxietyLevel * 3 + depressionLevel * 4);
        return Math.max(0, Math.min(100, 100 - negativeScore));
    }
    
    /**
     * Détermine le niveau de risque basé sur les scores
     */
    public String calculateRiskLevel() {
        int overallScore = calculateOverallScore();
        
        if (overallScore >= 75) return "low";
        if (overallScore >= 50) return "medium";
        if (overallScore >= 25) return "high";
        return "critical";
    }
    
    /**
     * Génère un résumé textuel du profil
     */
    public String getSummary() {
        return String.format(
            "Profil Mental - Score: %d/100 | Stress: %d/10 | Anxiété: %d/10 | État: %s | Risque: %s",
            calculateOverallScore(), stressLevel, anxietyLevel, emotionalState, riskLevel
        );
    }
    
    @Override
    public String toString() {
        return "MentalHealthProfile{" +
                "userId=" + userId +
                ", stressLevel=" + stressLevel +
                ", anxietyLevel=" + anxietyLevel +
                ", depressionLevel=" + depressionLevel +
                ", wellbeingScore=" + wellbeingScore +
                ", emotionalState='" + emotionalState + '\'' +
                ", riskLevel='" + riskLevel + '\'' +
                '}';
    }
}
