package controller.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.example.model.Quiz;
import org.example.model.User;
import org.example.service.NavigationService;

/**
 * Contrôleur de result.fxml
 * Reçoit les données de résultat via setResultat().
 */
public class QuizResultController {

    // ── Header ─────────────────────────────────────────────────
    @FXML private Label medalIconLabel;
    @FXML private Label medalLabel;
    @FXML private Label quizTitleLabel;

    // ── Score ──────────────────────────────────────────────────
    @FXML private Label scoreLabel;
    @FXML private Label percentageLabel;
    @FXML private Label messageLabel;

    // ── Statistiques ───────────────────────────────────────────
    @FXML private Label totalQuestionsLabel;
    @FXML private Label correctAnswersLabel;
    @FXML private Label incorrectAnswersLabel;

    // ── Boutons ────────────────────────────────────────────────
    @FXML private Button retryButton;
    @FXML private Button backButton;

    // ── Contexte (pour navigation) ─────────────────────────────
    private Quiz lastQuiz;
    private User lastUser;

    // ── API publique ────────────────────────────────────────────

    /**
     * Initialise l'affichage du résultat.
     *
     * @param earnedPoints  points obtenus par le joueur
     * @param totalPoints   points maximum possibles
     * @param correctCount  nombre de bonnes réponses
     * @param totalQuestions nombre total de questions
     * @param quiz           quiz joué (pour le titre et le replay)
     * @param user           joueur courant (pour navigation retour)
     */
    public void setResultat(int earnedPoints, int totalPoints,
                            int correctCount, int totalQuestions,
                            Quiz quiz, User user) {
        this.lastQuiz = quiz;
        this.lastUser = user;

        double pct    = totalPoints > 0 ? (earnedPoints * 100.0 / totalPoints) : 0;
        int    pctInt = (int) Math.round(pct);
        int    incorrectCount = totalQuestions - correctCount;

        // Titre du quiz
        quizTitleLabel.setText(quiz != null ? quiz.getTitre() : "—");

        // Score
        scoreLabel.setText(earnedPoints + " / " + totalPoints);
        percentageLabel.setText(pctInt + "%");

        // Message encourageant
        messageLabel.setText(genererMessage(pctInt));

        // Médaille
        String[] medal = calculerMedaille(pctInt);
        medalIconLabel.setText(medal[0]);
        medalLabel.setText(medal[1]);
        appliquerStyleHeader(pctInt);

        // Stats
        totalQuestionsLabel.setText(String.valueOf(totalQuestions));
        correctAnswersLabel.setText(String.valueOf(correctCount));
        incorrectAnswersLabel.setText(String.valueOf(incorrectCount));
    }

    // ── Actions ─────────────────────────────────────────────────

    @FXML
    private void handleRetry() {
        if (lastQuiz != null) {
            NavigationService.getInstance().navigateToQuizPlayer(retryButton, lastUser, lastQuiz);
        }
    }

    @FXML
    private void handleBack() {
        NavigationService.getInstance().navigateToQuizManager(backButton, lastUser);
    }

    // ── Helpers ─────────────────────────────────────────────────

    private String genererMessage(int pct) {
        if (pct >= 80) return "🎉 Félicitations ! Vous maîtrisez excellemment ce sujet !";
        if (pct >= 60) return "👍 Bien joué ! Bonne maîtrise du sujet.";
        if (pct >= 40) return "💪 Pas mal ! Continuez à vous améliorer.";
        return "📖 Courage ! Révisez et réessayez, vous progresserez !";
    }

    private String[] calculerMedaille(int pct) {
        if (pct >= 80) return new String[]{"🏆", "Médaille d'Or"};
        if (pct >= 60) return new String[]{"🥈", "Médaille d'Argent"};
        if (pct >= 40) return new String[]{"🥉", "Médaille de Bronze"};
        return new String[]{"🎯", "Participation"};
    }

    private void appliquerStyleHeader(int pct) {
        String color;
        if (pct >= 80)      color = "#4285F4, #F59E0B";
        else if (pct >= 60) color = "#4285F4, #9CA3AF";
        else if (pct >= 40) color = "#4285F4, #B45309";
        else                color = "#4285F4, #6EC5A6";
        // Le gradient header est défini inline dans le FXML — la couleur du score est ajustée ici
        percentageLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: 800; "
                + "-fx-text-fill: " + (pct >= 60 ? "#4285F4" : (pct >= 40 ? "#F59E0B" : "#EF4444")) + ";");
    }
}
