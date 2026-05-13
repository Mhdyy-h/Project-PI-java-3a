package org.example.service;

import org.example.model.MentalHealthProfile;
import org.example.model.MentalHealthRecommendation;
import org.example.model.QuizSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Service utilisant Ollama (IA locale) pour générer des insights
 * personnalisés sur la santé mentale
 */
public class MentalHealthOllamaService {
    
    private static final Logger log = LoggerFactory.getLogger(MentalHealthOllamaService.class);
    private static MentalHealthOllamaService instance;
    
    private final OllamaClient ollamaClient;
    
    private MentalHealthOllamaService() {
        this.ollamaClient = new OllamaClient();
    }
    
    public static synchronized MentalHealthOllamaService getInstance() {
        if (instance == null) {
            instance = new MentalHealthOllamaService();
        }
        return instance;
    }
    
    /**
     * Génère une analyse personnalisée du profil mental avec Ollama
     */
    public String generatePersonalizedInsight(MentalHealthProfile profile) {
        log.info("Generating personalized insight for user {}", profile.getUserId());
        
        try {
            String prompt = buildInsightPrompt(profile);
            List<Map<String, String>> messages = Arrays.asList(
                Map.of("role", "system", "content", 
                    "Tu es un psychologue expert en santé mentale. " +
                    "Analyse les données et fournis des insights personnalisés en français. " +
                    "Sois empathique, professionnel et constructif. " +
                    "Limite ta réponse à 200 mots maximum."),
                Map.of("role", "user", "content", prompt)
            );
            
            String response = ollamaClient.chat("llama3", messages);
            
            if (response == null || response.isBlank()) {
                return generateFallbackInsight(profile);
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("Error generating insight with Ollama", e);
            return generateFallbackInsight(profile);
        }
    }
    
    /**
     * Génère des conseils personnalisés basés sur les résultats du quiz
     */
    public String generateQuizFeedback(QuizSession session, MentalHealthProfile profile) {
        log.info("Generating quiz feedback for session {}", session.getId());
        
        try {
            String prompt = buildQuizFeedbackPrompt(session, profile);
            List<Map<String, String>> messages = Arrays.asList(
                Map.of("role", "system", "content",
                    "Tu es un coach en bien-être mental. " +
                    "Fournis un feedback encourageant et constructif sur les résultats du quiz. " +
                    "Sois positif, empathique et propose des actions concrètes. " +
                    "Réponds en français, maximum 150 mots."),
                Map.of("role", "user", "content", prompt)
            );
            
            String response = ollamaClient.chat("llama3", messages);
            
            if (response == null || response.isBlank()) {
                return generateFallbackQuizFeedback(session);
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("Error generating quiz feedback with Ollama", e);
            return generateFallbackQuizFeedback(session);
        }
    }
    
    /**
     * Génère des exercices de pleine conscience personnalisés
     */
    public String generateMindfulnessExercise(MentalHealthProfile profile) {
        log.info("Generating mindfulness exercise for user {}", profile.getUserId());
        
        try {
            String prompt = String.format(
                "Crée un exercice de pleine conscience personnalisé pour quelqu'un avec:\n" +
                "- Niveau de stress: %d/10\n" +
                "- Niveau d'anxiété: %d/10\n" +
                "- État émotionnel: %s\n\n" +
                "L'exercice doit durer 5-10 minutes et être facile à suivre.",
                profile.getStressLevel(),
                profile.getAnxietyLevel(),
                profile.getEmotionalState()
            );
            
            List<Map<String, String>> messages = Arrays.asList(
                Map.of("role", "system", "content",
                    "Tu es un instructeur de méditation et pleine conscience. " +
                    "Crée des exercices simples, guidés étape par étape. " +
                    "Utilise un langage apaisant et encourageant. " +
                    "Réponds en français."),
                Map.of("role", "user", "content", prompt)
            );
            
            String response = ollamaClient.chat("llama3", messages);
            
            if (response == null || response.isBlank()) {
                return generateFallbackMindfulnessExercise(profile);
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("Error generating mindfulness exercise with Ollama", e);
            return generateFallbackMindfulnessExercise(profile);
        }
    }
    
    /**
     * Génère un plan d'action personnalisé pour améliorer la santé mentale
     */
    public String generateActionPlan(MentalHealthProfile profile, List<MentalHealthRecommendation> recommendations) {
        log.info("Generating action plan for user {}", profile.getUserId());
        
        try {
            String prompt = buildActionPlanPrompt(profile, recommendations);
            List<Map<String, String>> messages = Arrays.asList(
                Map.of("role", "system", "content",
                    "Tu es un coach en santé mentale. " +
                    "Crée un plan d'action personnalisé, réaliste et motivant. " +
                    "Structure le plan sur 7 jours avec des objectifs quotidiens. " +
                    "Réponds en français, sois concis et actionnable."),
                Map.of("role", "user", "content", prompt)
            );
            
            String response = ollamaClient.chat("llama3", messages);
            
            if (response == null || response.isBlank()) {
                return generateFallbackActionPlan(profile);
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("Error generating action plan with Ollama", e);
            return generateFallbackActionPlan(profile);
        }
    }
    
    /**
     * Analyse les patterns de comportement et génère des insights
     */
    public Map<String, String> analyzePatterns(List<QuizSession> sessions) {
        Map<String, String> analysis = new HashMap<>();
        
        if (sessions == null || sessions.size() < 3) {
            analysis.put("pattern", "insufficient_data");
            analysis.put("insight", "Continuez à passer des quiz pour obtenir une analyse détaillée.");
            return analysis;
        }
        
        try {
            String prompt = buildPatternAnalysisPrompt(sessions);
            List<Map<String, String>> messages = Arrays.asList(
                Map.of("role", "system", "content",
                    "Tu es un analyste en psychologie comportementale. " +
                    "Identifie les patterns et tendances dans les données. " +
                    "Fournis des insights actionnables. " +
                    "Réponds en français, maximum 100 mots."),
                Map.of("role", "user", "content", prompt)
            );
            
            String response = ollamaClient.chat("llama3", messages);
            
            if (response != null && !response.isBlank()) {
                analysis.put("pattern", "detected");
                analysis.put("insight", response);
            } else {
                analysis.put("pattern", "none");
                analysis.put("insight", "Aucun pattern significatif détecté pour le moment.");
            }
            
        } catch (Exception e) {
            log.error("Error analyzing patterns with Ollama", e);
            analysis.put("pattern", "error");
            analysis.put("insight", "Impossible d'analyser les patterns actuellement.");
        }
        
        return analysis;
    }
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTION DES PROMPTS
    // ═══════════════════════════════════════════════════════════
    
    private String buildInsightPrompt(MentalHealthProfile profile) {
        return String.format(
            "Analyse ce profil de santé mentale et fournis un insight personnalisé:\n\n" +
            "Score de bien-être: %d/100\n" +
            "Niveau de stress: %d/10\n" +
            "Niveau d'anxiété: %d/10\n" +
            "Niveau de dépression: %d/10\n" +
            "Score de résilience: %d/100\n" +
            "État émotionnel: %s\n" +
            "Niveau de risque: %s\n" +
            "Tendance stress: %s\n" +
            "Tendance anxiété: %s\n" +
            "Tendance bien-être: %s\n\n" +
            "Fournis une analyse empathique et des conseils constructifs.",
            profile.getWellbeingScore(),
            profile.getStressLevel(),
            profile.getAnxietyLevel(),
            profile.getDepressionLevel(),
            profile.getResilienceScore(),
            profile.getEmotionalState(),
            profile.getRiskLevel(),
            profile.getStressTrend(),
            profile.getAnxietyTrend(),
            profile.getWellbeingTrend()
        );
    }
    
    private String buildQuizFeedbackPrompt(QuizSession session, MentalHealthProfile profile) {
        int dureeMinutes = session.getDureeSecondes() / 60;
        
        return String.format(
            "L'utilisateur vient de terminer un quiz:\n\n" +
            "Score obtenu: %.1f%%\n" +
            "Durée: %d minutes\n" +
            "Profil actuel:\n" +
            "- Stress: %d/10\n" +
            "- Anxiété: %d/10\n" +
            "- Bien-être: %d/100\n\n" +
            "Fournis un feedback encourageant et des conseils pratiques.",
            session.getScoreFinal(),
            dureeMinutes,
            profile.getStressLevel(),
            profile.getAnxietyLevel(),
            profile.getWellbeingScore()
        );
    }
    
    private String buildActionPlanPrompt(MentalHealthProfile profile, List<MentalHealthRecommendation> recommendations) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Crée un plan d'action sur 7 jours pour améliorer la santé mentale:\n\n");
        prompt.append(String.format("Profil actuel:\n"));
        prompt.append(String.format("- Bien-être: %d/100\n", profile.getWellbeingScore()));
        prompt.append(String.format("- Stress: %d/10\n", profile.getStressLevel()));
        prompt.append(String.format("- Anxiété: %d/10\n\n", profile.getAnxietyLevel()));
        
        if (recommendations != null && !recommendations.isEmpty()) {
            prompt.append("Recommandations prioritaires:\n");
            for (int i = 0; i < Math.min(3, recommendations.size()); i++) {
                MentalHealthRecommendation rec = recommendations.get(i);
                prompt.append(String.format("- %s (%s)\n", rec.getTitle(), rec.getType()));
            }
        }
        
        prompt.append("\nCrée un plan progressif avec des objectifs quotidiens réalistes.");
        return prompt.toString();
    }
    
    private String buildPatternAnalysisPrompt(List<QuizSession> sessions) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyse ces résultats de quiz sur les dernières semaines:\n\n");
        
        for (int i = 0; i < Math.min(10, sessions.size()); i++) {
            QuizSession session = sessions.get(i);
            int dureeMinutes = session.getDureeSecondes() / 60;
            
            prompt.append(String.format("Session %d: Score %.1f%%, Durée: %d min\n",
                i + 1,
                session.getScoreFinal(),
                dureeMinutes
            ));
        }
        
        prompt.append("\nIdentifie les patterns, tendances et fournis des insights actionnables.");
        return prompt.toString();
    }
    
    // ═══════════════════════════════════════════════════════════
    // FALLBACKS (si Ollama n'est pas disponible)
    // ═══════════════════════════════════════════════════════════
    
    private String generateFallbackInsight(MentalHealthProfile profile) {
        if (profile.getWellbeingScore() >= 75) {
            return "Votre santé mentale est excellente! Continuez vos bonnes habitudes et prenez soin de vous.";
        } else if (profile.getWellbeingScore() >= 50) {
            return "Votre bien-être est correct, mais il y a des opportunités d'amélioration. " +
                   "Concentrez-vous sur la gestion du stress et maintenez une routine saine.";
        } else {
            return "Vos résultats indiquent un besoin d'attention. " +
                   "Considérez parler à un professionnel et suivez les recommandations personnalisées.";
        }
    }
    
    private String generateFallbackQuizFeedback(QuizSession session) {
        double percentage = session.getScoreFinal(); // Déjà un pourcentage 0-100
        
        if (percentage >= 80) {
            return "Excellent travail! Vos réponses montrent une bonne conscience de votre santé mentale. " +
                   "Continuez à pratiquer l'auto-réflexion régulièrement.";
        } else if (percentage >= 60) {
            return "Bon résultat! Vous êtes sur la bonne voie. " +
                   "Continuez à travailler sur les domaines identifiés et suivez les recommandations.";
        } else {
            return "Merci d'avoir complété ce quiz. Vos réponses nous aident à mieux comprendre vos besoins. " +
                   "Consultez les recommandations personnalisées pour améliorer votre bien-être.";
        }
    }
    
    private String generateFallbackMindfulnessExercise(MentalHealthProfile profile) {
        return "Exercice de Respiration 4-7-8:\n\n" +
               "1. Asseyez-vous confortablement, dos droit\n" +
               "2. Placez votre langue contre votre palais\n" +
               "3. Inspirez par le nez pendant 4 secondes\n" +
               "4. Retenez votre souffle pendant 7 secondes\n" +
               "5. Expirez complètement par la bouche pendant 8 secondes\n" +
               "6. Répétez ce cycle 4 fois\n\n" +
               "Cet exercice aide à réduire le stress et l'anxiété rapidement.";
    }
    
    private String generateFallbackActionPlan(MentalHealthProfile profile) {
        return "Plan d'Action 7 Jours:\n\n" +
               "Jour 1-2: Établir une routine de sommeil (7-8h)\n" +
               "Jour 3-4: Pratiquer 10 min de méditation quotidienne\n" +
               "Jour 5-6: Faire 30 min d'exercice physique\n" +
               "Jour 7: Évaluer vos progrès et ajuster\n\n" +
               "Conseil: Commencez petit et soyez régulier!";
    }
}
