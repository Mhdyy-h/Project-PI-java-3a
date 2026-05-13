package org.example.service;

import org.example.model.*;
import org.example.dao.QuizDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service d'Intelligence Artificielle pour l'analyse de la santé mentale
 * Analyse les résultats des quiz et génère des recommandations personnalisées
 */
public class MentalHealthAIService {
    
    private static final Logger log = LoggerFactory.getLogger(MentalHealthAIService.class);
    private static MentalHealthAIService instance;
    
    private MentalHealthAIService() {}
    
    public static synchronized MentalHealthAIService getInstance() {
        if (instance == null) {
            instance = new MentalHealthAIService();
        }
        return instance;
    }
    
    /**
     * Analyse complète du profil mental d'un utilisateur
     * basée sur l'historique des quiz
     */
    public MentalHealthProfile analyzeUserProfile(int userId, List<QuizSession> recentSessions) {
        log.info("Analyzing mental health profile for user {}", userId);
        
        MentalHealthProfile profile = new MentalHealthProfile(userId);
        
        if (recentSessions == null || recentSessions.isEmpty()) {
            // Profil par défaut si pas de données
            profile.setStressLevel(5);
            profile.setAnxietyLevel(5);
            profile.setDepressionLevel(5);
            profile.setWellbeingScore(50);
            profile.setResilienceScore(50);
            profile.setEmotionalState("unknown");
            profile.setRiskLevel("medium");
            return profile;
        }
        
        // Calculer les moyennes des dernières sessions
        double avgStress = calculateAverageStress(recentSessions);
        double avgAnxiety = calculateAverageAnxiety(recentSessions);
        double avgDepression = calculateAverageDepression(recentSessions);
        
        profile.setStressLevel((int) Math.round(avgStress));
        profile.setAnxietyLevel((int) Math.round(avgAnxiety));
        profile.setDepressionLevel((int) Math.round(avgDepression));
        
        // Calculer le score de bien-être
        int wellbeingScore = calculateWellbeingScore(avgStress, avgAnxiety, avgDepression);
        profile.setWellbeingScore(wellbeingScore);
        
        // Calculer la résilience (basée sur la progression)
        int resilienceScore = calculateResilienceScore(recentSessions);
        profile.setResilienceScore(resilienceScore);
        
        // Déterminer l'état émotionnel
        String emotionalState = determineEmotionalState(avgStress, avgAnxiety, avgDepression);
        profile.setEmotionalState(emotionalState);
        
        // Calculer les tendances
        profile.setStressTrend(calculateTrend(recentSessions, "stress"));
        profile.setAnxietyTrend(calculateTrend(recentSessions, "anxiety"));
        profile.setWellbeingTrend(calculateTrend(recentSessions, "wellbeing"));
        
        // Déterminer le niveau de risque
        String riskLevel = profile.calculateRiskLevel();
        profile.setRiskLevel(riskLevel);
        
        // Dernière évaluation
        if (!recentSessions.isEmpty()) {
            profile.setLastAssessment(recentSessions.get(0).getDateDebut());
        }
        
        profile.setUpdatedAt(LocalDateTime.now());
        
        log.info("Profile analysis complete: {}", profile.getSummary());
        return profile;
    }
    
    /**
     * Génère des recommandations personnalisées basées sur le profil
     */
    public List<MentalHealthRecommendation> generateRecommendations(MentalHealthProfile profile) {
        log.info("Generating recommendations for user {}", profile.getUserId());
        
        List<MentalHealthRecommendation> recommendations = new ArrayList<>();
        
        // Recommandations basées sur le niveau de stress
        if (profile.getStressLevel() >= 7) {
            recommendations.add(createStressRecommendation(profile, "high"));
        } else if (profile.getStressLevel() >= 5) {
            recommendations.add(createStressRecommendation(profile, "medium"));
        }
        
        // Recommandations basées sur l'anxiété
        if (profile.getAnxietyLevel() >= 7) {
            recommendations.add(createAnxietyRecommendation(profile, "high"));
        } else if (profile.getAnxietyLevel() >= 5) {
            recommendations.add(createAnxietyRecommendation(profile, "medium"));
        }
        
        // Recommandations basées sur la dépression
        if (profile.getDepressionLevel() >= 7) {
            recommendations.add(createDepressionRecommendation(profile, "high"));
        }
        
        // Recommandations générales de bien-être
        if (profile.getWellbeingScore() < 50) {
            recommendations.add(createWellbeingRecommendation(profile));
        }
        
        // Recommandations d'urgence si risque critique
        if ("critical".equals(profile.getRiskLevel())) {
            recommendations.add(createUrgentRecommendation(profile));
        }
        
        // Recommandations positives si tout va bien
        if (profile.getWellbeingScore() >= 75) {
            recommendations.add(createMaintenanceRecommendation(profile));
        }
        
        // Trier par priorité
        recommendations.sort((r1, r2) -> {
            Map<String, Integer> priorityMap = Map.of(
                "urgent", 4, "high", 3, "medium", 2, "low", 1
            );
            int p1 = priorityMap.getOrDefault(r1.getPriority(), 0);
            int p2 = priorityMap.getOrDefault(r2.getPriority(), 0);
            return Integer.compare(p2, p1);
        });
        
        log.info("Generated {} recommendations", recommendations.size());
        return recommendations;
    }
    
    /**
     * Prédit le niveau de stress futur basé sur les tendances
     */
    public Map<String, Object> predictFutureStress(int userId, List<QuizSession> historicalSessions) {
        Map<String, Object> prediction = new HashMap<>();
        
        if (historicalSessions == null || historicalSessions.size() < 3) {
            prediction.put("prediction", "insufficient_data");
            prediction.put("confidence", 0.0);
            return prediction;
        }
        
        // Régression linéaire simple
        List<Double> stressLevels = historicalSessions.stream()
            .map(s -> (double) extractStressFromSession(s))
            .collect(Collectors.toList());
        
        double trend = calculateLinearTrend(stressLevels);
        double currentStress = stressLevels.get(stressLevels.size() - 1);
        double predictedStress = currentStress + (trend * 7); // Prédiction à 7 jours
        
        prediction.put("current_stress", currentStress);
        prediction.put("predicted_stress_7days", Math.max(1, Math.min(10, predictedStress)));
        prediction.put("trend", trend > 0 ? "increasing" : trend < 0 ? "decreasing" : "stable");
        prediction.put("confidence", calculatePredictionConfidence(stressLevels));
        prediction.put("recommendation", trend > 0.5 ? "intervention_needed" : "continue_monitoring");
        
        return prediction;
    }
    
    /**
     * Analyse les patterns de réponses pour détecter des anomalies
     */
    public Map<String, Object> detectAnomalies(QuizSession session, List<QuizSession> historicalSessions) {
        Map<String, Object> analysis = new HashMap<>();
        
        if (historicalSessions == null || historicalSessions.isEmpty()) {
            analysis.put("anomaly_detected", false);
            return analysis;
        }
        
        // Calculer les moyennes historiques
        double avgScore = historicalSessions.stream()
            .mapToDouble(QuizSession::getScoreFinal)
            .average()
            .orElse(0.0);
        
        double stdDev = calculateStandardDeviation(
            historicalSessions.stream()
                .map(s -> (int) s.getScoreFinal())
                .collect(Collectors.toList())
        );
        
        // Détecter si le score actuel est anormal (> 2 écarts-types)
        double currentScore = session.getScoreFinal();
        double zScore = Math.abs((currentScore - avgScore) / (stdDev + 0.001));
        
        boolean anomalyDetected = zScore > 2.0;
        
        analysis.put("anomaly_detected", anomalyDetected);
        analysis.put("z_score", zScore);
        analysis.put("current_score", currentScore);
        analysis.put("average_score", avgScore);
        analysis.put("severity", zScore > 3.0 ? "high" : zScore > 2.0 ? "medium" : "low");
        
        if (anomalyDetected) {
            analysis.put("alert_message", "Changement significatif détecté dans les résultats");
            analysis.put("suggested_action", "Consultation recommandée");
        }
        
        return analysis;
    }
    
    // ═══════════════════════════════════════════════════════════
    // MÉTHODES PRIVÉES - CALCULS
    // ═══════════════════════════════════════════════════════════
    
    private double calculateAverageStress(List<QuizSession> sessions) {
        return sessions.stream()
            .mapToDouble(this::extractStressFromSession)
            .average()
            .orElse(5.0);
    }
    
    private double calculateAverageAnxiety(List<QuizSession> sessions) {
        return sessions.stream()
            .mapToDouble(this::extractAnxietyFromSession)
            .average()
            .orElse(5.0);
    }
    
    private double calculateAverageDepression(List<QuizSession> sessions) {
        return sessions.stream()
            .mapToDouble(this::extractDepressionFromSession)
            .average()
            .orElse(5.0);
    }
    
    private int extractStressFromSession(QuizSession session) {
        // Logique pour extraire le niveau de stress du score
        // Score bas = stress élevé, score haut = stress faible
        double scoreFinal = session.getScoreFinal(); // 0-100 percentage
        double percentage = scoreFinal / 100.0;
        return (int) Math.round(10 - (percentage * 9)); // Inverse: 100% = stress 1, 0% = stress 10
    }
    
    private int extractAnxietyFromSession(QuizSession session) {
        // Similaire au stress mais avec une pondération différente
        double scoreFinal = session.getScoreFinal();
        double percentage = scoreFinal / 100.0;
        return (int) Math.round(10 - (percentage * 8));
    }
    
    private int extractDepressionFromSession(QuizSession session) {
        double scoreFinal = session.getScoreFinal();
        double percentage = scoreFinal / 100.0;
        return (int) Math.round(10 - (percentage * 7));
    }
    
    private int calculateWellbeingScore(double stress, double anxiety, double depression) {
        // Formule: 100 - (stress*3 + anxiety*3 + depression*4) / 10
        double negativeScore = (stress * 3 + anxiety * 3 + depression * 4);
        return (int) Math.max(0, Math.min(100, 100 - negativeScore));
    }
    
    private int calculateResilienceScore(List<QuizSession> sessions) {
        if (sessions.size() < 2) return 50;
        
        // Calculer l'amélioration au fil du temps
        double firstScore = sessions.get(sessions.size() - 1).getScoreFinal();
        double lastScore = sessions.get(0).getScoreFinal();
        double improvement = lastScore - firstScore;
        
        // Score de résilience basé sur l'amélioration
        return (int) Math.max(0, Math.min(100, 50 + (improvement * 2)));
    }
    
    private String determineEmotionalState(double stress, double anxiety, double depression) {
        if (depression >= 7) return "depressed";
        if (anxiety >= 7) return "anxious";
        if (stress >= 7) return "stressed";
        if (stress <= 3 && anxiety <= 3) return "calm";
        if (stress <= 4 && anxiety <= 4 && depression <= 3) return "happy";
        return "neutral";
    }
    
    private String calculateTrend(List<QuizSession> sessions, String metric) {
        if (sessions.size() < 2) return "stable";
        
        List<Double> values = new ArrayList<>();
        for (QuizSession session : sessions) {
            switch (metric) {
                case "stress":
                    values.add((double) extractStressFromSession(session));
                    break;
                case "anxiety":
                    values.add((double) extractAnxietyFromSession(session));
                    break;
                case "wellbeing":
                    values.add(session.getScoreFinal());
                    break;
            }
        }
        
        double trend = calculateLinearTrend(values);
        
        if (metric.equals("wellbeing")) {
            return trend > 0.5 ? "improving" : trend < -0.5 ? "declining" : "stable";
        } else {
            return trend > 0.5 ? "declining" : trend < -0.5 ? "improving" : "stable";
        }
    }
    
    private double calculateLinearTrend(List<Double> values) {
        if (values.size() < 2) return 0.0;
        
        int n = values.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        
        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += values.get(i);
            sumXY += i * values.get(i);
            sumX2 += i * i;
        }
        
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        return slope;
    }
    
    private double calculateStandardDeviation(List<Integer> values) {
        if (values.isEmpty()) return 0.0;
        
        double mean = values.stream().mapToDouble(Integer::doubleValue).average().orElse(0.0);
        double variance = values.stream()
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .average()
            .orElse(0.0);
        
        return Math.sqrt(variance);
    }
    
    private double calculatePredictionConfidence(List<Double> values) {
        if (values.size() < 3) return 0.3;
        if (values.size() < 5) return 0.5;
        if (values.size() < 10) return 0.7;
        return 0.9;
    }
    
    // ═══════════════════════════════════════════════════════════
    // CRÉATION DES RECOMMANDATIONS
    // ═══════════════════════════════════════════════════════════
    
    private MentalHealthRecommendation createStressRecommendation(MentalHealthProfile profile, String level) {
        MentalHealthRecommendation rec = new MentalHealthRecommendation(
            profile.getUserId(),
            "breathing",
            "Exercice de Respiration Anti-Stress",
            "Pratiquez la respiration profonde pour réduire votre niveau de stress élevé."
        );
        
        rec.setPriority(level.equals("high") ? "high" : "medium");
        rec.setTargetArea("stress");
        rec.setBasedOn("stress_level");
        rec.setEstimatedDuration(10);
        rec.setExpectedBenefit("Réduction du stress de 30-40% en 10 minutes");
        rec.setConfidenceScore(0.85);
        rec.setActionSteps(
            "1. Trouvez un endroit calme\n" +
            "2. Inspirez profondément pendant 4 secondes\n" +
            "3. Retenez votre souffle pendant 4 secondes\n" +
            "4. Expirez lentement pendant 6 secondes\n" +
            "5. Répétez 10 fois"
        );
        rec.addResource("https://www.youtube.com/watch?v=breathing-exercise");
        rec.setExpiresAt(LocalDateTime.now().plusDays(7));
        
        return rec;
    }
    
    private MentalHealthRecommendation createAnxietyRecommendation(MentalHealthProfile profile, String level) {
        MentalHealthRecommendation rec = new MentalHealthRecommendation(
            profile.getUserId(),
            "meditation",
            "Méditation Guidée pour l'Anxiété",
            "Une séance de méditation pour calmer votre anxiété et retrouver la sérénité."
        );
        
        rec.setPriority(level.equals("high") ? "high" : "medium");
        rec.setTargetArea("anxiety");
        rec.setBasedOn("anxiety_level");
        rec.setEstimatedDuration(15);
        rec.setExpectedBenefit("Réduction de l'anxiété et amélioration du calme mental");
        rec.setConfidenceScore(0.80);
        rec.setActionSteps(
            "1. Asseyez-vous confortablement\n" +
            "2. Fermez les yeux\n" +
            "3. Concentrez-vous sur votre respiration\n" +
            "4. Laissez passer les pensées sans jugement\n" +
            "5. Pratiquez pendant 15 minutes"
        );
        rec.addResource("https://www.headspace.com/meditation/anxiety");
        rec.setExpiresAt(LocalDateTime.now().plusDays(7));
        
        return rec;
    }
    
    private MentalHealthRecommendation createDepressionRecommendation(MentalHealthProfile profile, String level) {
        MentalHealthRecommendation rec = new MentalHealthRecommendation(
            profile.getUserId(),
            "therapy",
            "Consultation Professionnelle Recommandée",
            "Vos résultats indiquent un niveau élevé de symptômes dépressifs. Une consultation avec un professionnel est recommandée."
        );
        
        rec.setPriority("urgent");
        rec.setTargetArea("depression");
        rec.setBasedOn("depression_level");
        rec.setEstimatedDuration(60);
        rec.setExpectedBenefit("Accompagnement professionnel et plan de traitement personnalisé");
        rec.setConfidenceScore(0.95);
        rec.setActionSteps(
            "1. Contactez votre médecin traitant\n" +
            "2. Demandez une référence à un psychologue/psychiatre\n" +
            "3. Prenez rendez-vous dans les 7 jours\n" +
            "4. Préparez vos questions pour la consultation\n" +
            "5. Parlez-en à un proche de confiance"
        );
        rec.addResource("https://www.psycom.org/find-help");
        rec.setExpiresAt(LocalDateTime.now().plusDays(3));
        
        return rec;
    }
    
    private MentalHealthRecommendation createWellbeingRecommendation(MentalHealthProfile profile) {
        MentalHealthRecommendation rec = new MentalHealthRecommendation(
            profile.getUserId(),
            "lifestyle",
            "Routine de Bien-être Quotidienne",
            "Établissez une routine quotidienne pour améliorer votre bien-être général."
        );
        
        rec.setPriority("medium");
        rec.setTargetArea("wellbeing");
        rec.setBasedOn("wellbeing_score");
        rec.setEstimatedDuration(30);
        rec.setExpectedBenefit("Amélioration progressive du bien-être sur 2-4 semaines");
        rec.setConfidenceScore(0.75);
        rec.setActionSteps(
            "1. Dormez 7-8 heures par nuit\n" +
            "2. Faites 30 minutes d'exercice par jour\n" +
            "3. Mangez équilibré (fruits, légumes, protéines)\n" +
            "4. Limitez les écrans avant le coucher\n" +
            "5. Pratiquez la gratitude (3 choses positives/jour)"
        );
        rec.setExpiresAt(LocalDateTime.now().plusDays(30));
        
        return rec;
    }
    
    private MentalHealthRecommendation createUrgentRecommendation(MentalHealthProfile profile) {
        MentalHealthRecommendation rec = new MentalHealthRecommendation(
            profile.getUserId(),
            "urgent",
            "⚠️ Aide Immédiate Nécessaire",
            "Vos résultats indiquent un niveau de détresse critique. Une aide professionnelle immédiate est fortement recommandée."
        );
        
        rec.setPriority("urgent");
        rec.setTargetArea("crisis");
        rec.setBasedOn("risk_level");
        rec.setEstimatedDuration(0);
        rec.setExpectedBenefit("Soutien immédiat et sécurité");
        rec.setConfidenceScore(1.0);
        rec.setActionSteps(
            "1. Appelez le 3114 (numéro national de prévention du suicide)\n" +
            "2. Contactez votre médecin immédiatement\n" +
            "3. Rendez-vous aux urgences si nécessaire\n" +
            "4. Parlez à un proche de confiance MAINTENANT\n" +
            "5. Ne restez pas seul(e)"
        );
        rec.addResource("tel:3114");
        rec.addResource("https://www.suicide-ecoute.fr");
        rec.setExpiresAt(LocalDateTime.now().plusHours(24));
        
        return rec;
    }
    
    private MentalHealthRecommendation createMaintenanceRecommendation(MentalHealthProfile profile) {
        MentalHealthRecommendation rec = new MentalHealthRecommendation(
            profile.getUserId(),
            "maintenance",
            "✨ Continuez sur cette Voie!",
            "Votre santé mentale est excellente! Voici comment maintenir cet équilibre."
        );
        
        rec.setPriority("low");
        rec.setTargetArea("maintenance");
        rec.setBasedOn("wellbeing_score");
        rec.setEstimatedDuration(20);
        rec.setExpectedBenefit("Maintien d'un excellent équilibre mental");
        rec.setConfidenceScore(0.90);
        rec.setActionSteps(
            "1. Continuez vos habitudes actuelles\n" +
            "2. Pratiquez la pleine conscience 10 min/jour\n" +
            "3. Maintenez vos connexions sociales\n" +
            "4. Célébrez vos réussites\n" +
            "5. Restez actif physiquement"
        );
        rec.setExpiresAt(LocalDateTime.now().plusDays(30));
        
        return rec;
    }
}
