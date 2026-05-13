package org.example.controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.example.model.*;
import org.example.service.*;
import org.example.util.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour le tableau de bord de santé mentale avec IA
 * Affiche les analyses, recommandations et insights personnalisés
 */
public class MentalHealthDashboardController {
    
    private static final Logger log = LoggerFactory.getLogger(MentalHealthDashboardController.class);
    
    @FXML private BorderPane mainPane;
    @FXML private Label userNameLabel;
    @FXML private Label userInitialsLabel;
    @FXML private Circle userAvatar;
    
    // Scores principaux
    @FXML private Label wellbeingScoreLabel;
    @FXML private Label wellbeingTrendLabel;
    @FXML private Label stressLevelLabel;
    @FXML private Label stressTrendLabel;
    @FXML private Label anxietyLevelLabel;
    @FXML private Label anxietyTrendLabel;
    @FXML private Label riskLevelLabel;
    @FXML private Label riskBadge;
    
    // Insight IA
    @FXML private TextArea insightArea;
    @FXML private Label lastAssessmentLabel;
    
    // Recommandations
    @FXML private VBox recommendationsContainer;
    
    // Graphique
    @FXML private LineChart<Number, Number> progressChart;
    @FXML private NumberAxis xAxis;
    @FXML private NumberAxis yAxis;
    
    // Sélecteur de scénario (pour démo)
    @FXML private ComboBox<String> scenarioSelector;
    @FXML private Button loadDemoButton;
    
    private User currentUser;
    private MentalHealthAIService aiService;
    private MentalHealthOllamaService ollamaService;
    private MentalHealthDemoDataService demoService;
    private MentalHealthProfile currentProfile;
    
    @FXML
    public void initialize() {
        log.info("Initializing Mental Health Dashboard");
        
        aiService = MentalHealthAIService.getInstance();
        ollamaService = MentalHealthOllamaService.getInstance();
        demoService = MentalHealthDemoDataService.getInstance();
        
        // NE PAS charger automatiquement - attendre setCurrentUser()
    }
    
    /**
     * Définit l'utilisateur courant (appelé par NavigationService)
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            setupUserInfo();
            setupScenarioSelector();
            loadDashboard("good");
        } else {
            log.error("User is null in setCurrentUser");
            showError("Erreur: Utilisateur non connecté");
        }
    }
    
    private void setupUserInfo() {
        String fullName = currentUser.getNomComplet();
        userNameLabel.setText(fullName);
        
        // Créer les initiales
        String[] parts = fullName.split(" ");
        String initials = "";
        if (parts.length >= 2) {
            initials = parts[0].substring(0, 1).toUpperCase() + 
                      parts[1].substring(0, 1).toUpperCase();
        } else if (parts.length == 1) {
            initials = parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        userInitialsLabel.setText(initials);
    }
    
    private void setupScenarioSelector() {
        scenarioSelector.getItems().addAll(
            "excellent", "good", "moderate", "concerning", "critical"
        );
        scenarioSelector.setValue("good");
        
        loadDemoButton.setOnAction(e -> {
            String scenario = scenarioSelector.getValue();
            if (scenario != null) {
                loadDashboard(scenario);
            }
        });
    }
    
    private void loadDashboard(String scenario) {
        log.info("Loading dashboard with scenario: {}", scenario);
        
        try {
            // Générer le profil de démonstration
            currentProfile = demoService.generateDemoProfile(currentUser.getId(), scenario);
            
            // Générer les sessions de démonstration
            String trend = determineTrendFromScenario(scenario);
            List<QuizSession> demoSessions = demoService.generateDemoSessions(
                currentUser.getId(), 10, trend
            );
            
            // Afficher les scores
            displayScores(currentProfile);
            
            // Générer et afficher l'insight
            displayInsight(currentProfile, scenario);
            
            // Charger les recommandations
            loadRecommendations(currentProfile, scenario);
            
            // Afficher le graphique de progression
            displayProgressChart(trend);
            
            log.info("Dashboard loaded successfully");
            
        } catch (Exception e) {
            log.error("Error loading dashboard", e);
            showError("Erreur lors du chargement du tableau de bord: " + e.getMessage());
        }
    }
    
    private String determineTrendFromScenario(String scenario) {
        switch (scenario.toLowerCase()) {
            case "excellent":
            case "good":
                return "improving";
            case "concerning":
            case "critical":
                return "declining";
            default:
                return "stable";
        }
    }
    
    private void displayScores(MentalHealthProfile profile) {
        // Score de bien-être
        wellbeingScoreLabel.setText(profile.getWellbeingScore() + "/100");
        wellbeingTrendLabel.setText(getTrendEmoji(profile.getWellbeingTrend()));
        
        // Niveau de stress
        stressLevelLabel.setText(profile.getStressLevel() + "/10");
        stressTrendLabel.setText(getTrendEmoji(profile.getStressTrend()));
        
        // Niveau d'anxiété
        anxietyLevelLabel.setText(profile.getAnxietyLevel() + "/10");
        anxietyTrendLabel.setText(getTrendEmoji(profile.getAnxietyTrend()));
        
        // Niveau de risque
        riskLevelLabel.setText(getRiskLevelText(profile.getRiskLevel()));
        riskBadge.setText(getRiskBadge(profile.getRiskLevel()));
        riskBadge.setStyle(getRiskBadgeStyle(profile.getRiskLevel()));
        
        // Dernière évaluation
        if (profile.getLastAssessment() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");
            lastAssessmentLabel.setText("Dernière évaluation: " + 
                profile.getLastAssessment().format(formatter));
        }
    }
    
    private void displayInsight(MentalHealthProfile profile, String scenario) {
        // Générer l'insight avec le service de démo
        String demoInsight = demoService.generateDemoInsight(scenario);
        
        // Essayer d'obtenir un insight personnalisé avec Ollama (avec fallback)
        String ollamaInsight = ollamaService.generatePersonalizedInsight(profile);
        
        // Utiliser l'insight Ollama s'il est différent du fallback, sinon utiliser le démo
        if (ollamaInsight != null && !ollamaInsight.contains("Votre profil mental montre")) {
            insightArea.setText(ollamaInsight);
        } else {
            insightArea.setText(demoInsight);
        }
    }
    
    private void loadRecommendations(MentalHealthProfile profile, String scenario) {
        recommendationsContainer.getChildren().clear();
        
        // Générer les recommandations de démonstration
        List<MentalHealthRecommendation> recommendations = 
            demoService.generateDemoRecommendations(currentUser.getId(), scenario);
        
        if (recommendations.isEmpty()) {
            Label noRec = new Label("Aucune recommandation pour le moment");
            noRec.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");
            recommendationsContainer.getChildren().add(noRec);
            return;
        }
        
        for (MentalHealthRecommendation rec : recommendations) {
            VBox recCard = createRecommendationCard(rec);
            recommendationsContainer.getChildren().add(recCard);
        }
    }
    
    private VBox createRecommendationCard(MentalHealthRecommendation rec) {
        VBox card = new VBox(12);
        card.getStyleClass().add("mw-card");
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-padding: 20; " +
            "-fx-background-radius: 12; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2); " +
            "-fx-cursor: hand;"
        );
        
        // Header avec priorité
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label priorityBadge = new Label(rec.getPriorityBadge());
        priorityBadge.setStyle(
            "-fx-background-color: " + getPriorityColor(rec.getPriority()) + "; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 4 12; " +
            "-fx-background-radius: 12; " +
            "-fx-font-size: 11px; " +
            "-fx-font-weight: bold;"
        );
        
        Label title = new Label(rec.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1a1a1a;");
        title.setWrapText(true);
        
        header.getChildren().addAll(priorityBadge, title);
        HBox.setHgrow(title, Priority.ALWAYS);
        
        // Description
        Label description = new Label(rec.getDescription());
        description.setWrapText(true);
        description.setStyle("-fx-text-fill: #666; -fx-font-size: 13px;");
        
        // Métadonnées
        HBox metadata = new HBox(20);
        metadata.setAlignment(Pos.CENTER_LEFT);
        
        if (rec.getEstimatedDuration() > 0) {
            Label duration = new Label("⏱ " + rec.getEstimatedDuration() + " min");
            duration.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
            metadata.getChildren().add(duration);
        }
        
        Label targetArea = new Label("🎯 " + getTargetAreaText(rec.getTargetArea()));
        targetArea.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        metadata.getChildren().add(targetArea);
        
        // Bouton d'action
        Button actionBtn = new Button("Voir les détails");
        actionBtn.getStyleClass().add("mw-button-primary");
        actionBtn.setStyle(
            "-fx-background-color: #7C6FCD; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 8; " +
            "-fx-cursor: hand; " +
            "-fx-font-weight: bold;"
        );
        actionBtn.setOnAction(e -> showRecommendationDetails(rec));
        
        // Effet hover sur la carte
        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: white; " +
                "-fx-padding: 20; " +
                "-fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 4); " +
                "-fx-cursor: hand; " +
                "-fx-scale-x: 1.02; " +
                "-fx-scale-y: 1.02;"
            );
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: white; " +
                "-fx-padding: 20; " +
                "-fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2); " +
                "-fx-cursor: hand;"
            );
        });
        
        card.getChildren().addAll(header, description, metadata, actionBtn);
        return card;
    }
    
    private void showRecommendationDetails(MentalHealthRecommendation rec) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(rec.getTitle());
        alert.setHeaderText(rec.getDescription());
        
        // Contenu détaillé
        StringBuilder content = new StringBuilder();
        
        if (rec.getActionSteps() != null && !rec.getActionSteps().isEmpty()) {
            content.append("📋 ÉTAPES À SUIVRE:\n\n");
            content.append(rec.getActionSteps());
            content.append("\n\n");
        }
        
        if (rec.getExpectedBenefit() != null) {
            content.append("✨ BÉNÉFICES ATTENDUS:\n");
            content.append(rec.getExpectedBenefit());
            content.append("\n\n");
        }
        
        if (rec.getEstimatedDuration() > 0) {
            content.append("⏱ DURÉE: " + rec.getEstimatedDuration() + " minutes\n");
        }
        
        if (rec.getConfidenceScore() > 0) {
            int confidence = (int) (rec.getConfidenceScore() * 100);
            content.append("🎯 CONFIANCE IA: " + confidence + "%\n");
        }
        
        if (rec.getResources() != null && !rec.getResources().isEmpty()) {
            content.append("\n📚 RESSOURCES:\n");
            for (String resource : rec.getResources()) {
                content.append("• " + resource + "\n");
            }
        }
        
        alert.setContentText(content.toString());
        
        // Styliser l'alerte
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-font-family: 'Segoe UI', Arial, sans-serif;");
        
        alert.showAndWait();
    }
    
    private void displayProgressChart(String trend) {
        progressChart.getData().clear();
        
        // Générer les données de progression
        Map<String, List<Integer>> progressionData = demoService.generateProgressionData(trend);
        
        // Série pour le stress
        XYChart.Series<Number, Number> stressSeries = new XYChart.Series<>();
        stressSeries.setName("Stress");
        List<Integer> stressData = progressionData.get("stress");
        for (int i = 0; i < stressData.size(); i++) {
            stressSeries.getData().add(new XYChart.Data<>(i + 1, stressData.get(i)));
        }
        
        // Série pour l'anxiété
        XYChart.Series<Number, Number> anxietySeries = new XYChart.Series<>();
        anxietySeries.setName("Anxiété");
        List<Integer> anxietyData = progressionData.get("anxiety");
        for (int i = 0; i < anxietyData.size(); i++) {
            anxietySeries.getData().add(new XYChart.Data<>(i + 1, anxietyData.get(i)));
        }
        
        // Série pour le bien-être (échelle inversée pour le graphique)
        XYChart.Series<Number, Number> wellbeingSeries = new XYChart.Series<>();
        wellbeingSeries.setName("Bien-être");
        List<Integer> wellbeingData = progressionData.get("wellbeing");
        for (int i = 0; i < wellbeingData.size(); i++) {
            // Convertir 0-100 en 0-10 pour l'affichage
            wellbeingSeries.getData().add(new XYChart.Data<>(i + 1, wellbeingData.get(i) / 10.0));
        }
        
        progressChart.getData().addAll(stressSeries, anxietySeries, wellbeingSeries);
        
        // Configurer les axes
        xAxis.setLabel("Jours");
        yAxis.setLabel("Niveau (1-10)");
    }
    
    // ═══════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES
    // ═══════════════════════════════════════════════════════════
    
    private String getTrendEmoji(String trend) {
        if (trend == null) return "➡️";
        switch (trend.toLowerCase()) {
            case "improving": return "📈";
            case "declining": return "📉";
            default: return "➡️";
        }
    }
    
    private String getRiskLevelText(String riskLevel) {
        if (riskLevel == null) return "Inconnu";
        switch (riskLevel.toLowerCase()) {
            case "low": return "Faible";
            case "medium": return "Modéré";
            case "high": return "Élevé";
            case "critical": return "Critique";
            default: return riskLevel;
        }
    }
    
    private String getRiskBadge(String riskLevel) {
        if (riskLevel == null) return "⚪ INCONNU";
        switch (riskLevel.toLowerCase()) {
            case "low": return "🟢 FAIBLE";
            case "medium": return "🟡 MODÉRÉ";
            case "high": return "🟠 ÉLEVÉ";
            case "critical": return "🔴 CRITIQUE";
            default: return "⚪ " + riskLevel.toUpperCase();
        }
    }
    
    private String getRiskBadgeStyle(String riskLevel) {
        String baseStyle = "-fx-padding: 8 16; -fx-background-radius: 20; " +
                          "-fx-font-weight: bold; -fx-font-size: 13px; ";
        
        if (riskLevel == null) return baseStyle + "-fx-background-color: #e0e0e0; -fx-text-fill: #666;";
        
        switch (riskLevel.toLowerCase()) {
            case "low":
                return baseStyle + "-fx-background-color: #d4edda; -fx-text-fill: #155724;";
            case "medium":
                return baseStyle + "-fx-background-color: #fff3cd; -fx-text-fill: #856404;";
            case "high":
                return baseStyle + "-fx-background-color: #f8d7da; -fx-text-fill: #721c24;";
            case "critical":
                return baseStyle + "-fx-background-color: #f5c6cb; -fx-text-fill: #721c24; " +
                       "-fx-effect: dropshadow(gaussian, rgba(220,53,69,0.3), 8, 0, 0, 2);";
            default:
                return baseStyle + "-fx-background-color: #e0e0e0; -fx-text-fill: #666;";
        }
    }
    
    private String getPriorityColor(String priority) {
        if (priority == null) return "#6c757d";
        switch (priority.toLowerCase()) {
            case "urgent": return "#dc3545";
            case "high": return "#fd7e14";
            case "medium": return "#ffc107";
            case "low": return "#28a745";
            default: return "#6c757d";
        }
    }
    
    private String getTargetAreaText(String targetArea) {
        if (targetArea == null) return "Général";
        switch (targetArea.toLowerCase()) {
            case "stress": return "Gestion du stress";
            case "anxiety": return "Réduction de l'anxiété";
            case "depression": return "Soutien dépression";
            case "sleep": return "Amélioration du sommeil";
            case "wellbeing": return "Bien-être général";
            case "social": return "Connexions sociales";
            case "crisis": return "Aide d'urgence";
            case "maintenance": return "Maintien";
            default: return targetArea;
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // ═══════════════════════════════════════════════════════════
    // NAVIGATION
    // ═══════════════════════════════════════════════════════════
    
    @FXML
    private void handleBackToDashboard(MouseEvent event) {
        NavigationService.getInstance().navigateToDashboard(mainPane, currentUser);
    }
    
    @FXML
    private void handleLogout(MouseEvent event) {
        UserSession.getInstance().clearSession();
        NavigationService.getInstance().navigateToLogin(mainPane);
    }
}
