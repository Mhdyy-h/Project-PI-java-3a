package org.example.service;

import org.example.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service de génération de données de démonstration pour le module de santé mentale
 * Génère des profils, sessions et recommandations réalistes pour les tests
 */
public class MentalHealthDemoDataService {
    
    private static final Logger log = LoggerFactory.getLogger(MentalHealthDemoDataService.class);
    private static MentalHealthDemoDataService instance;
    private final Random random = new Random();
    
    private MentalHealthDemoDataService() {}
    
    public static MentalHealthDemoDataService getInstance() {
        if (instance == null) {
            instance = new MentalHealthDemoDataService();
        }
        return instance;
    }
    
    /**
     * Génère un profil de santé mentale basé sur un scénario
     */
    public MentalHealthProfile generateDemoProfile(int userId, String scenario) {
        MentalHealthProfile profile = new MentalHealthProfile();
        profile.setUserId(userId);
        profile.setLastAssessment(LocalDateTime.now().minusHours(random.nextInt(24)));
        
        switch (scenario.toLowerCase()) {
            case "excellent":
                profile.setWellbeingScore(85 + random.nextInt(15));
                profile.setStressLevel(1 + random.nextInt(2));
                profile.setAnxietyLevel(1 + random.nextInt(2));
                profile.setRiskLevel("low");
                profile.setWellbeingTrend("improving");
                profile.setStressTrend("improving");
                profile.setAnxietyTrend("improving");
                break;
                
            case "good":
                profile.setWellbeingScore(70 + random.nextInt(15));
                profile.setStressLevel(3 + random.nextInt(2));
                profile.setAnxietyLevel(3 + random.nextInt(2));
                profile.setRiskLevel("low");
                profile.setWellbeingTrend("stable");
                profile.setStressTrend("stable");
                profile.setAnxietyTrend("stable");
                break;
                
            case "moderate":
                profile.setWellbeingScore(50 + random.nextInt(15));
                profile.setStressLevel(5 + random.nextInt(2));
                profile.setAnxietyLevel(5 + random.nextInt(2));
                profile.setRiskLevel("medium");
                profile.setWellbeingTrend("stable");
                profile.setStressTrend("stable");
                profile.setAnxietyTrend("stable");
                break;
                
            case "concerning":
                profile.setWellbeingScore(35 + random.nextInt(10));
                profile.setStressLevel(7 + random.nextInt(2));
                profile.setAnxietyLevel(7 + random.nextInt(2));
                profile.setRiskLevel("high");
                profile.setWellbeingTrend("declining");
                profile.setStressTrend("declining");
                profile.setAnxietyTrend("declining");
                break;
                
            case "critical":
                profile.setWellbeingScore(15 + random.nextInt(15));
                profile.setStressLevel(8 + random.nextInt(2));
                profile.setAnxietyLevel(8 + random.nextInt(2));
                profile.setRiskLevel("critical");
                profile.setWellbeingTrend("declining");
                profile.setStressTrend("declining");
                profile.setAnxietyTrend("declining");
                break;
                
            default:
                profile.setWellbeingScore(60);
                profile.setStressLevel(5);
                profile.setAnxietyLevel(5);
                profile.setRiskLevel("medium");
                profile.setWellbeingTrend("stable");
                profile.setStressTrend("stable");
                profile.setAnxietyTrend("stable");
        }
        
        return profile;
    }
    
    /**
     * Génère des sessions de quiz de démonstration avec une tendance
     */
    public List<QuizSession> generateDemoSessions(int userId, int count, String trend) {
        List<QuizSession> sessions = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            QuizSession session = new QuizSession();
            session.setUtilisateurId(userId);
            session.setTheme("Santé Mentale");
            session.setNiveauDifficulte(3 + random.nextInt(5)); // Difficulté 3-7
            session.setDateDebut(LocalDateTime.now().minusDays(count - i));
            session.setDateFin(LocalDateTime.now().minusDays(count - i).plusMinutes(10 + random.nextInt(20)));
            session.setStatut("TERMINEE");
            session.setNombreQuestions(10 + random.nextInt(10));
            
            // Calculer le score basé sur la tendance
            double baseScore = 50.0;
            if ("improving".equals(trend)) {
                baseScore = 40.0 + (i * 5.0) + random.nextInt(10);
            } else if ("declining".equals(trend)) {
                baseScore = 80.0 - (i * 5.0) + random.nextInt(10);
            } else {
                baseScore = 50.0 + random.nextInt(20);
            }
            
            session.setScoreFinal(Math.max(0.0, Math.min(100.0, baseScore)));
            
            // Score ELO basé sur la performance
            int eloBase = 1000;
            if (baseScore >= 80) {
                eloBase = 1200 + random.nextInt(200);
            } else if (baseScore >= 60) {
                eloBase = 1000 + random.nextInt(200);
            } else {
                eloBase = 800 + random.nextInt(200);
            }
            session.setScoreElo(eloBase);
            
            // Score de fatigue (inversement proportionnel au score)
            double fatigue = 100.0 - baseScore + random.nextInt(20) - 10;
            session.setScoreFatigue(Math.max(0.0, Math.min(100.0, fatigue)));
            
            // Durée en secondes
            int duree = (10 + random.nextInt(20)) * 60; // 10-30 minutes en secondes
            session.setDureeSecondes(duree);
            
            sessions.add(session);
        }
        
        return sessions;
    }
    
    /**
     * Génère un insight de démonstration basé sur le scénario
     */
    public String generateDemoInsight(String scenario) {
        switch (scenario.toLowerCase()) {
            case "excellent":
                return "🌟 Excellent travail ! Votre santé mentale est dans un état optimal. " +
                       "Vous montrez une grande résilience émotionnelle et un bien-être général élevé. " +
                       "Continuez vos bonnes habitudes de vie et vos pratiques de bien-être.";
                
            case "good":
                return "✅ Votre santé mentale est globalement bonne. Vous gérez bien le stress quotidien " +
                       "et maintenez un équilibre émotionnel stable. Quelques ajustements mineurs pourraient " +
                       "encore améliorer votre bien-être général.";
                
            case "moderate":
                return "⚠️ Votre santé mentale nécessite une attention modérée. Vous ressentez un niveau " +
                       "de stress et d'anxiété qui pourrait bénéficier d'interventions ciblées. " +
                       "Considérez l'adoption de techniques de relaxation et de gestion du stress.";
                
            case "concerning":
                return "🔔 Attention : Votre profil indique des signes préoccupants de stress et d'anxiété élevés. " +
                       "Il est recommandé de consulter un professionnel de la santé mentale et d'adopter " +
                       "des stratégies d'intervention immédiates pour améliorer votre bien-être.";
                
            case "critical":
                return "🚨 ALERTE : Votre santé mentale nécessite une attention urgente. Les niveaux de stress " +
                       "et d'anxiété sont critiques. Veuillez contacter immédiatement un professionnel de santé " +
                       "ou appeler une ligne d'aide en cas de crise (3114 - Numéro national de prévention du suicide).";
                
            default:
                return "Votre profil mental montre un état général stable avec quelques domaines à surveiller.";
        }
    }
    
    /**
     * Génère des recommandations de démonstration basées sur le scénario
     */
    public List<MentalHealthRecommendation> generateDemoRecommendations(int userId, String scenario) {
        List<MentalHealthRecommendation> recommendations = new ArrayList<>();
        
        switch (scenario.toLowerCase()) {
            case "excellent":
                recommendations.add(createRecommendation(
                    userId, "Maintien", "Continuez vos pratiques de bien-être",
                    "Maintenez vos habitudes actuelles qui contribuent à votre excellent état mental.",
                    "low", "wellbeing", 15,
                    "1. Continuez vos activités physiques régulières\n2. Maintenez vos routines de sommeil\n3. Gardez vos connexions sociales actives",
                    "Maintien d'un état mental optimal et prévention de la détérioration"
                ));
                break;
                
            case "good":
                recommendations.add(createRecommendation(
                    userId, "Optimisation", "Techniques de méditation avancées",
                    "Approfondissez votre pratique de la pleine conscience pour renforcer votre résilience.",
                    "low", "wellbeing", 20,
                    "1. Pratiquez 15 minutes de méditation quotidienne\n2. Essayez la méditation guidée\n3. Tenez un journal de gratitude",
                    "Amélioration de la conscience émotionnelle et réduction du stress résiduel"
                ));
                break;
                
            case "moderate":
                recommendations.add(createRecommendation(
                    userId, "Important", "Exercices de respiration profonde",
                    "Pratiquez des techniques de respiration pour réduire le stress et l'anxiété.",
                    "medium", "stress", 10,
                    "1. Respiration 4-7-8 (inspirez 4s, retenez 7s, expirez 8s)\n2. Pratiquez 3 fois par jour\n3. Utilisez lors de moments stressants",
                    "Réduction immédiate du stress et amélioration de la régulation émotionnelle"
                ));
                
                recommendations.add(createRecommendation(
                    userId, "Important", "Activité physique régulière",
                    "L'exercice physique est un puissant antidote au stress et à l'anxiété.",
                    "medium", "wellbeing", 30,
                    "1. 30 minutes d'activité modérée par jour\n2. Marche, yoga, natation ou vélo\n3. Privilégiez les activités en plein air",
                    "Amélioration de l'humeur, réduction du stress et meilleur sommeil"
                ));
                break;
                
            case "concerning":
                recommendations.add(createRecommendation(
                    userId, "Urgent", "Consultation professionnelle recommandée",
                    "Votre niveau de stress nécessite l'accompagnement d'un professionnel de santé mentale.",
                    "high", "crisis", 60,
                    "1. Prenez rendez-vous avec un psychologue ou psychiatre\n2. Contactez votre médecin traitant\n3. Envisagez une thérapie cognitivo-comportementale",
                    "Accompagnement professionnel pour gérer efficacement le stress et l'anxiété"
                ));
                
                recommendations.add(createRecommendation(
                    userId, "Urgent", "Techniques de gestion de crise",
                    "Apprenez des stratégies immédiates pour gérer les moments de forte anxiété.",
                    "high", "anxiety", 15,
                    "1. Technique du 5-4-3-2-1 (5 choses que vous voyez, 4 que vous touchez, etc.)\n2. Respiration carrée\n3. Contactez un proche de confiance",
                    "Outils immédiats pour gérer les crises d'anxiété"
                ));
                break;
                
            case "critical":
                recommendations.add(createRecommendation(
                    userId, "Critique", "AIDE IMMÉDIATE NÉCESSAIRE",
                    "Votre état nécessite une intervention urgente. Contactez immédiatement un professionnel.",
                    "urgent", "crisis", 0,
                    "1. Appelez le 3114 (Numéro national de prévention du suicide)\n2. Contactez le 15 (SAMU) en cas d'urgence vitale\n3. Rendez-vous aux urgences psychiatriques les plus proches\n4. Contactez immédiatement un proche",
                    "Intervention d'urgence pour assurer votre sécurité et votre bien-être"
                ));
                
                recommendations.add(createRecommendation(
                    userId, "Critique", "Suivi psychiatrique urgent",
                    "Un suivi médical immédiat est nécessaire pour évaluer votre état.",
                    "urgent", "crisis", 60,
                    "1. Consultation psychiatrique en urgence\n2. Évaluation médicale complète\n3. Mise en place d'un plan de traitement\n4. Suivi rapproché",
                    "Stabilisation de votre état mental et mise en place d'un traitement adapté"
                ));
                break;
        }
        
        return recommendations;
    }
    
    /**
     * Génère des données de progression pour le graphique
     */
    public Map<String, List<Integer>> generateProgressionData(String trend) {
        Map<String, List<Integer>> data = new HashMap<>();
        List<Integer> stressData = new ArrayList<>();
        List<Integer> anxietyData = new ArrayList<>();
        List<Integer> wellbeingData = new ArrayList<>();
        
        int days = 10;
        
        for (int i = 0; i < days; i++) {
            int stress, anxiety, wellbeing;
            
            if ("improving".equals(trend)) {
                stress = Math.max(1, 8 - i + random.nextInt(2));
                anxiety = Math.max(1, 7 - i + random.nextInt(2));
                wellbeing = Math.min(100, 40 + (i * 6) + random.nextInt(10));
            } else if ("declining".equals(trend)) {
                stress = Math.min(10, 3 + i + random.nextInt(2));
                anxiety = Math.min(10, 3 + i + random.nextInt(2));
                wellbeing = Math.max(10, 80 - (i * 6) + random.nextInt(10));
            } else {
                stress = 5 + random.nextInt(3) - 1;
                anxiety = 5 + random.nextInt(3) - 1;
                wellbeing = 50 + random.nextInt(20) - 10;
            }
            
            stressData.add(stress);
            anxietyData.add(anxiety);
            wellbeingData.add(wellbeing);
        }
        
        data.put("stress", stressData);
        data.put("anxiety", anxietyData);
        data.put("wellbeing", wellbeingData);
        
        return data;
    }
    
    /**
     * Méthode utilitaire pour créer une recommandation
     */
    private MentalHealthRecommendation createRecommendation(
            int userId, String priorityBadgeText, String title, String description,
            String priority, String targetArea, int duration,
            String actionSteps, String expectedBenefit) {
        
        MentalHealthRecommendation rec = new MentalHealthRecommendation();
        rec.setUserId(userId);
        // Note: priorityBadge est calculé automatiquement via getPriorityBadge() basé sur priority
        rec.setTitle(title);
        rec.setDescription(description);
        rec.setPriority(priority);
        rec.setTargetArea(targetArea);
        rec.setEstimatedDuration(duration);
        rec.setActionSteps(actionSteps);
        rec.setExpectedBenefit(expectedBenefit);
        rec.setConfidenceScore(0.85 + (random.nextDouble() * 0.15));
        rec.setCreatedAt(LocalDateTime.now());
        
        // Ajouter quelques ressources
        List<String> resources = new ArrayList<>();
        resources.add("Guide de pratique disponible dans l'application");
        resources.add("Vidéos tutorielles sur notre chaîne YouTube");
        rec.setResources(resources);
        
        return rec;
    }
}
