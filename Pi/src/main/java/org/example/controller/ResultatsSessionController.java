package org.example.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.model.QuestionAdaptive;
import org.example.model.QuizSession;
import org.example.model.ReponseUtilisateur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.model.FatigueLog;
import org.example.model.ProfilCognitif;
import org.example.service.AdaptiveService;
import org.example.service.AIAssistantService;
import org.example.service.ReportService;

import java.util.List;
import java.util.Map;

/**
 * Affiche les résultats d'une session de quiz terminée.
 * Reçoit ses données via {@link #chargerSession}.
 */
public class ResultatsSessionController {

    private static final Logger log = LoggerFactory.getLogger(ResultatsSessionController.class);

    // ── Score / ELO ─────────────────────────────────────────────
    @FXML private Label labelScoreEmoji;
    @FXML private Label labelScoreFinal;
    @FXML private Label labelNbBonnes;
    @FXML private Label labelEloAvant;
    @FXML private Label labelEloApres;
    @FXML private Label labelEloDelta;

    // ── Stats ────────────────────────────────────────────────────
    @FXML private Label labelFatigueFinal;
    @FXML private Label labelTempsMoyen;
    @FXML private Label labelDureeSession;
    @FXML private Label labelNbQuestions;

    // ── Coaching ─────────────────────────────────────────────────
    @FXML private Label labelCoaching;
    @FXML private VBox  conteneurRecommandations;

    // ── Chat ─────────────────────────────────────────────────────
    @FXML private VBox      conteneurChat;
    @FXML private TextField champChat;

    // ── Tableau détail ───────────────────────────────────────────
    @FXML private VBox conteneurDetailQuestions;

    // ── Données reçues ───────────────────────────────────────────
    private QuizSession                 session;
    private List<ReponseUtilisateur>    reponses;
    private Map<Integer, QuestionAdaptive>      questionsMap;
    private int                         eloAvant;

    private final AIAssistantService assistantSvc  = new AIAssistantService();
    private final AdaptiveService    adaptiveSvc   = new AdaptiveService();

    // ════════════════════════════════════════════════════════════════
    // Point d'entrée appelé par QuizViewController
    // ════════════════════════════════════════════════════════════════

    public void chargerSession(QuizSession session,
                               List<ReponseUtilisateur> reponses,
                               Map<Integer, QuestionAdaptive> questionsMap,
                               int eloAvant) {
        this.session      = session;
        this.reponses     = reponses;
        this.questionsMap = questionsMap;
        this.eloAvant     = eloAvant;

        afficherStats();
        afficherDetailQuestions();
        lancerCoachingIA();
    }

    // ════════════════════════════════════════════════════════════════
    // Affichage stats principales
    // ════════════════════════════════════════════════════════════════

    private void afficherStats() {
        double score = session.getScoreFinal();
        long bonnes  = reponses.stream().filter(ReponseUtilisateur::isEstCorrecte).count();
        int total    = reponses.size();

        // Score
        labelScoreFinal.setText(String.format("%.0f%%", score));
        labelNbBonnes.setText(bonnes + " / " + total + " bonnes réponses");

        if (score >= 80) {
            labelScoreEmoji.setText("🏆");
            labelScoreFinal.setStyle("-fx-font-size: 42px; -fx-font-weight: bold; -fx-text-fill: #16a34a;");
        } else if (score >= 60) {
            labelScoreEmoji.setText("🎉");
            labelScoreFinal.setStyle("-fx-font-size: 42px; -fx-font-weight: bold; -fx-text-fill: #d97706;");
        } else {
            labelScoreEmoji.setText("📚");
            labelScoreFinal.setStyle("-fx-font-size: 42px; -fx-font-weight: bold; -fx-text-fill: #dc2626;");
        }

        // ELO
        int eloApres = session.getScoreElo();
        int delta    = eloApres - eloAvant;

        labelEloAvant.setText(String.valueOf(eloAvant));
        labelEloApres.setText(String.valueOf(eloApres));
        if (delta >= 0) {
            labelEloDelta.setText("+" + delta + " pts");
            labelEloDelta.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #16a34a;");
        } else {
            labelEloDelta.setText(delta + " pts");
            labelEloDelta.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dc2626;");
        }

        // Fatigue
        double fatigue = session.getScoreFatigue();
        labelFatigueFinal.setText(String.format("%.0f%%", fatigue));
        String couleurFatigue = fatigue >= 70 ? "#dc2626" : fatigue >= 40 ? "#d97706" : "#16a34a";
        labelFatigueFinal.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + couleurFatigue + ";");

        // Temps moyen
        if (!reponses.isEmpty()) {
            double tempsMoyenSec = reponses.stream()
                    .mapToLong(ReponseUtilisateur::getTempsReponseMs)
                    .average().orElse(0) / 1000.0;
            labelTempsMoyen.setText(String.format("%.1f s", tempsMoyenSec));
        } else {
            labelTempsMoyen.setText("—");
        }

        // Durée session
        if (session.getDureeSecondes() > 0) {
            int min = session.getDureeSecondes() / 60;
            int sec = session.getDureeSecondes() % 60;
            labelDureeSession.setText(String.format("%d min %02d s", min, sec));
        } else {
            labelDureeSession.setText("—");
        }

        labelNbQuestions.setText(total + " questions");
    }

    // ════════════════════════════════════════════════════════════════
    // Tableau détail des réponses
    // ════════════════════════════════════════════════════════════════

    private void afficherDetailQuestions() {
        conteneurDetailQuestions.getChildren().clear();

        for (int i = 0; i < reponses.size(); i++) {
            ReponseUtilisateur rep = reponses.get(i);
            QuestionAdaptive q = questionsMap.get(rep.getQuestionId());

            HBox row = new HBox();
            row.setStyle("-fx-padding: 8 16; -fx-background-radius: 6;");
            row.setOnMouseEntered(e -> row.setStyle("-fx-padding: 8 16; -fx-background-color: #f9fafb; -fx-background-radius: 6;"));
            row.setOnMouseExited(e  -> row.setStyle("-fx-padding: 8 16; -fx-background-radius: 6;"));

            Label lNum = new Label(String.valueOf(i + 1));
            lNum.setMinWidth(30);
            lNum.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af;");

            String enonce = q != null ? truncate(q.getEnonce(), 60) : "Question inconnue";
            Label lEnonce = new Label(enonce);
            lEnonce.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");
            HBox.setHgrow(lEnonce, Priority.ALWAYS);

            String resultStr = rep.isEstCorrecte() ? "✓ Correct" : "✗ Faux";
            String couleur   = rep.isEstCorrecte() ? "#16a34a" : "#dc2626";
            Label lResult = new Label(resultStr);
            lResult.setMinWidth(80);
            lResult.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + couleur + ";");

            Label lTemps = new Label(String.format("%.1f s", rep.getTempsReponseMs() / 1000.0));
            lTemps.setMinWidth(70);
            lTemps.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

            String diff = q != null ? "Niv. " + q.getNiveauDifficulte() : "—";
            Label lDiff = new Label(diff);
            lDiff.setMinWidth(80);
            lDiff.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

            row.getChildren().addAll(lNum, lEnonce, lResult, lTemps, lDiff);
            conteneurDetailQuestions.getChildren().add(row);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // Coaching IA (background)
    // ════════════════════════════════════════════════════════════════

    private void lancerCoachingIA() {
        labelCoaching.setText("Analyse de votre session en cours...");

        Thread t = new Thread(() -> {
            try {
                ProfilCognitif profil = adaptiveSvc.getOuCreerProfil(session.getUtilisateurId());
                AIAssistantService.CoachingResult coaching =
                        assistantSvc.coacher(profil, reponses, questionsMap);
                Platform.runLater(() -> afficherCoaching(coaching));
            } catch (Exception e) {
                log.warn("Erreur coaching IA : {}", e.getMessage());
                Platform.runLater(() -> labelCoaching.setText(
                        "Continuez à pratiquer régulièrement pour progresser. " +
                        "Chaque session vous rapproche de vos objectifs."));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void afficherCoaching(AIAssistantService.CoachingResult coaching) {
        if (coaching == null) {
            labelCoaching.setText("Bien joué ! Continuez à vous entraîner.");
            return;
        }

        labelCoaching.setText(coaching.getTexteCoaching());
        conteneurRecommandations.getChildren().clear();

        for (String reco : coaching.getRecommandations()) {
            Label l = new Label("→  " + reco);
            l.setWrapText(true);
            l.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.75);");
            conteneurRecommandations.getChildren().add(l);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // Chat
    // ════════════════════════════════════════════════════════════════

    @FXML
    public void envoyerMessage() {
        String msg = champChat.getText().trim();
        if (msg.isEmpty()) return;

        champChat.clear();
        ajouterBulleChat(msg, true);

        Thread t = new Thread(() -> {
            String reponse = assistantSvc.chat(msg);
            Platform.runLater(() -> ajouterBulleChat(reponse, false));
        });
        t.setDaemon(true);
        t.start();
    }

    private void ajouterBulleChat(String texte, boolean estUtilisateur) {
        Label bulle = new Label(texte);
        bulle.setWrapText(true);
        bulle.setMaxWidth(280);

        if (estUtilisateur) {
            bulle.setStyle("-fx-background-color: #1a0f4f; -fx-text-fill: white; " +
                    "-fx-background-radius: 12 12 2 12; -fx-padding: 8 12; -fx-font-size: 12px;");
        } else {
            bulle.setStyle("-fx-background-color: #e5e7eb; -fx-text-fill: #111827; " +
                    "-fx-background-radius: 12 12 12 2; -fx-padding: 8 12; -fx-font-size: 12px;");
        }

        HBox ligne = new HBox(bulle);
        ligne.setAlignment(estUtilisateur
                ? javafx.geometry.Pos.CENTER_RIGHT
                : javafx.geometry.Pos.CENTER_LEFT);
        VBox.setMargin(ligne, new Insets(2, 0, 2, 0));

        conteneurChat.getChildren().add(ligne);
    }

    // ════════════════════════════════════════════════════════════════
    // Export PDF
    // ════════════════════════════════════════════════════════════════

    @FXML
    public void exporterPDF() {
        Thread t = new Thread(() -> {
            try {
                ProfilCognitif profil = adaptiveSvc.getOuCreerProfil(session.getUtilisateurId());
                String chemin = new ReportService().generer(
                        session, profil, reponses, questionsMap,
                        List.of(), List.of());
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Export réussi");
                    alert.setHeaderText(null);
                    alert.setContentText("Rapport PDF généré :\n" + chemin);
                    alert.showAndWait();
                });
            } catch (Exception e) {
                log.error("Erreur export PDF : {}", e.getMessage());
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText(null);
                    alert.setContentText("Impossible d'exporter le rapport : " + e.getMessage());
                    alert.showAndWait();
                });
            }
        });
        t.setDaemon(true);
        t.start();
    }

    // ════════════════════════════════════════════════════════════════
    // Navigation
    // ════════════════════════════════════════════════════════════════

    @FXML
    public void retourDashboard() {
        naviguerVers("/DashboardCognitif.fxml");
    }

    @FXML
    public void nouveauQuiz() {
        naviguerVers("/QuizView.fxml");
    }

    private void naviguerVers(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) labelScoreFinal.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            log.error("Erreur navigation vers {} : {}", fxml, e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // Utilitaire
    // ════════════════════════════════════════════════════════════════

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}
