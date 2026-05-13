package org.example.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Recommandation personnalisée générée par l'IA
 * pour améliorer la santé mentale de l'utilisateur
 */
public class MentalHealthRecommendation {
    
    private int id;
    private int userId;
    private String type;              // "exercise", "meditation", "breathing", "therapy", "lifestyle"
    private String priority;          // "urgent", "high", "medium", "low"
    private String title;
    private String description;
    private String actionSteps;       // JSON ou texte avec étapes
    private int estimatedDuration;    // en minutes
    private String expectedBenefit;
    private double confidenceScore;   // 0.0 - 1.0 (confiance de l'IA)
    
    // Contexte
    private String basedOn;           // "stress_level", "anxiety_pattern", "quiz_results"
    private String targetArea;        // "stress", "anxiety", "depression", "sleep", "mood"
    
    // Suivi
    private boolean completed;
    private LocalDateTime completedAt;
    private Integer userRating;       // 1-5 étoiles
    private String userFeedback;
    
    // Métadonnées
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    
    // Ressources additionnelles
    private List<String> resources;   // URLs, vidéos, articles
    
    public MentalHealthRecommendation() {
        this.resources = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.completed = false;
    }
    
    public MentalHealthRecommendation(int userId, String type, String title, String description) {
        this();
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.description = description;
    }
    
    // Getters and Setters
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getActionSteps() {
        return actionSteps;
    }
    
    public void setActionSteps(String actionSteps) {
        this.actionSteps = actionSteps;
    }
    
    public int getEstimatedDuration() {
        return estimatedDuration;
    }
    
    public void setEstimatedDuration(int estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }
    
    public String getExpectedBenefit() {
        return expectedBenefit;
    }
    
    public void setExpectedBenefit(String expectedBenefit) {
        this.expectedBenefit = expectedBenefit;
    }
    
    public double getConfidenceScore() {
        return confidenceScore;
    }
    
    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = Math.max(0.0, Math.min(1.0, confidenceScore));
    }
    
    public String getBasedOn() {
        return basedOn;
    }
    
    public void setBasedOn(String basedOn) {
        this.basedOn = basedOn;
    }
    
    public String getTargetArea() {
        return targetArea;
    }
    
    public void setTargetArea(String targetArea) {
        this.targetArea = targetArea;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed && completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public Integer getUserRating() {
        return userRating;
    }
    
    public void setUserRating(Integer userRating) {
        if (userRating != null) {
            this.userRating = Math.max(1, Math.min(5, userRating));
        } else {
            this.userRating = null;
        }
    }
    
    public String getUserFeedback() {
        return userFeedback;
    }
    
    public void setUserFeedback(String userFeedback) {
        this.userFeedback = userFeedback;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public List<String> getResources() {
        return resources;
    }
    
    public void setResources(List<String> resources) {
        this.resources = resources;
    }
    
    public void addResource(String resource) {
        if (this.resources == null) {
            this.resources = new ArrayList<>();
        }
        this.resources.add(resource);
    }
    
    /**
     * Vérifie si la recommandation est encore valide
     */
    public boolean isValid() {
        if (completed) return false;
        if (expiresAt == null) return true;
        return LocalDateTime.now().isBefore(expiresAt);
    }
    
    /**
     * Génère un badge de priorité coloré
     */
    public String getPriorityBadge() {
        switch (priority != null ? priority.toLowerCase() : "low") {
            case "urgent": return "🔴 URGENT";
            case "high": return "🟠 HAUTE";
            case "medium": return "🟡 MOYENNE";
            default: return "🟢 BASSE";
        }
    }
    
    @Override
    public String toString() {
        return "MentalHealthRecommendation{" +
                "id=" + id +
                ", userId=" + userId +
                ", type='" + type + '\'' +
                ", priority='" + priority + '\'' +
                ", title='" + title + '\'' +
                ", completed=" + completed +
                '}';
    }
}
