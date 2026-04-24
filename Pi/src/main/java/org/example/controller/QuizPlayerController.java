package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.model.Question;
import org.example.model.Quiz;
import org.example.model.User;
import org.example.service.NavigationService;
import org.example.service.QuizService;
import org.example.service.QuestionService;
import org.example.util.AlertHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Contrôleur de quiz_player.fxml
 *
 * Trois vues dans un StackPane :
 *   VIEW 1 – quizSelectionView  : sélection du quiz
 *   VIEW 2 – quizPlayView       : quiz en cours
 *   VIEW 3 – resultView         : résultat final (utilisé si pas de navigation vers result.fxml)
 *
 * Flux :
 *   VIEW 1 → [clic Jouer] → VIEW 2 → [Terminer] → result.fxml (QuizResultController)
 *            ↑                         [Quitter]  → VIEW 1
 */
public class QuizPlayerController {

    // ── VIEW 1 ─────────────────────────────────────────────────
    @FXML private VBox  quizSelectionView;
    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private Label statusLabel;
    @FXML private VBox  quizListContainer;

    // ── VIEW 2 ─────────────────────────────────────────────────
    @FXML private VBox        quizPlayView;
    @FXML private Label       quizTitleLabel;
    @FXML private Label       questionNumberLabel;
    @FXML private Label       progressLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Label       questionTextLabel;
    @FXML private Button      optionAButton;
    @FXML private Button      optionBButton;
    @FXML private Button      optionCButton;
    @FXML private Button      optionDButton;
    @FXML private Button      previousButton;
    @FXML private Button      nextButton;
    @FXML private Button      submitButton;

    // ── VIEW 3 (résultat inline) ────────────────────────────────
    @FXML private VBox  resultView;
    @FXML private HBox  medalContainer;
    @FXML private Label medalLabel;
    @FXML private Label resultTitleLabel;
    @FXML private Label percentageLabel;
    @FXML private Label scoreLabel;
    @FXML private Label messageLabel;
    @FXML private Button retryButton;

    // ── État ───────────────────────────────────────────────────
    private final QuizService      quizService     = new QuizService();
    private final QuestionService  questionService = new QuestionService();

    private User             currentUser;
    private Quiz             currentQuiz;
    private List<Question>   questions        = new ArrayList<>();
    private int              currentIndex     = 0;
    private List<String>     userAnswers      = new ArrayList<>();   // "A"|"B"|"C"|"D"|null

    // Styles boutons options
    private static final String STYLE_NORMAL   =
            "-fx-background-color: #F9FAFB; -fx-border-color: #D1D5DB; "
          + "-fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 0 20 0 20; "
          + "-fx-cursor: hand; -fx-text-fill: #1A1A1A; -fx-font-size: 14px; "
          + "-fx-alignment: CENTER_LEFT; -fx-border-width: 2;";
    private static final String STYLE_SELECTED =
            "-fx-background-color: #DBEAFE; -fx-border-color: #4285F4; "
          + "-fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 0 20 0 20; "
          + "-fx-cursor: hand; -fx-text-fill: #1E40AF; -fx-font-size: 14px; "
          + "-fx-font-weight: bold; -fx-alignment: CENTER_LEFT; -fx-border-width: 2;";

    // ── Cycle de vie ───────────────────────────────────────────

    @FXML
    public void initialize() {
        afficherVue(1);
        chargerListeQuiz();
    }

    // ── API publique ────────────────────────────────────────────

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            welcomeLabel.setText("Bonjour " + user.getNomComplet() + " !");
            roleLabel.setText("Joueur");
        }
    }

    /** Pré-sélectionne un quiz et démarre VIEW 2 directement. */
    public void setQuiz(Quiz quiz) {
        this.currentQuiz = quiz;
        demarrerQuiz(quiz);
    }

    // ── VIEW 1 – Sélection ──────────────────────────────────────

    private void chargerListeQuiz() {
        quizListContainer.getChildren().clear();
        List<Quiz> quizActifs = quizService.getQuizActifs();

        if (quizActifs.isEmpty()) {
            statusLabel.setText("Aucun quiz disponible pour le moment.");
            return;
        }
        statusLabel.setText("");

        for (Quiz quiz : quizActifs) {
            quizListContainer.getChildren().add(creerCarteQuiz(quiz));
        }
    }

    private HBox creerCarteQuiz(Quiz quiz) {
        HBox card = new HBox(20);
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14; "
                + "-fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 14; "
                + "-fx-padding: 18 24 18 24; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");

        // Info bloc
        VBox info = new VBox(4);
        info.getChildren().addAll(
            labelStyle(quiz.getTitre(), "-fx-font-size: 15px; -fx-font-weight: 700; -fx-text-fill: #1A1A1A;"),
            labelStyle(quiz.getCategorie() + " · " + quiz.getDifficulte(),
                       "-fx-font-size: 12px; -fx-text-fill: #6B7280;"),
            labelStyle("Score de passage : " + quiz.getPassingScore() + "%",
                       "-fx-font-size: 12px; -fx-text-fill: #9CA3AF;")
        );

        // Spacer
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Bouton Jouer
        Button btnJouer = new Button("▶  Jouer");
        btnJouer.setStyle("-fx-background-color: #4285F4; -fx-text-fill: white; "
                + "-fx-font-size: 13px; -fx-font-weight: 700; -fx-background-radius: 8; "
                + "-fx-padding: 10 22 10 22; -fx-cursor: hand;");
        btnJouer.setOnAction(e -> demarrerQuiz(quiz));

        card.getChildren().addAll(info, spacer, btnJouer);
        return card;
    }

    // ── VIEW 2 – Jeu ────────────────────────────────────────────

    private void demarrerQuiz(Quiz quiz) {
        currentQuiz = quiz;
        questions   = questionService.getQuestionsByQuizId(quiz.getId());

        if (questions.isEmpty()) {
            AlertHelper.showWarning("Aucune question",
                    "Ce quiz ne contient aucune question pour l'instant.");
            return;
        }

        currentIndex = 0;
        userAnswers  = new ArrayList<>(java.util.Collections.nCopies(questions.size(), null));

        quizTitleLabel.setText(quiz.getTitre());
        afficherVue(2);
        afficherQuestion(currentIndex);
    }

    private void afficherQuestion(int index) {
        if (index < 0 || index >= questions.size()) return;

        Question q = questions.get(index);
        int total  = questions.size();

        questionNumberLabel.setText("Question " + (index + 1) + " / " + total);
        progressLabel.setText((index + 1) + " / " + total);
        progressBar.setProgress((double)(index + 1) / total);

        questionTextLabel.setText(q.getContenu());

        optionAButton.setText("A.  " + nvl(q.getOptionA()));
        optionBButton.setText("B.  " + nvl(q.getOptionB()));
        optionCButton.setText("C.  " + nvl(q.getOptionC()));
        optionDButton.setText("D.  " + nvl(q.getOptionD()));

        // Restaurer la sélection précédente
        String savedAnswer = userAnswers.get(index);
        resetOptionStyles();
        if (savedAnswer != null) selectionnerOption(savedAnswer, false);

        // Boutons navigation
        previousButton.setDisable(index == 0);

        boolean derniere = (index == total - 1);
        nextButton.setVisible(!derniere);
        nextButton.setManaged(!derniere);
        submitButton.setVisible(derniere);
        submitButton.setManaged(derniere);

        // Activer Suivant uniquement si réponse déjà donnée
        nextButton.setDisable(savedAnswer == null);
    }

    @FXML private void handleOptionA() { selectionnerOption("A", true); }
    @FXML private void handleOptionB() { selectionnerOption("B", true); }
    @FXML private void handleOptionC() { selectionnerOption("C", true); }
    @FXML private void handleOptionD() { selectionnerOption("D", true); }

    private void selectionnerOption(String lettre, boolean save) {
        if (save) userAnswers.set(currentIndex, lettre);

        resetOptionStyles();
        switch (lettre) {
            case "A" -> optionAButton.setStyle(STYLE_SELECTED);
            case "B" -> optionBButton.setStyle(STYLE_SELECTED);
            case "C" -> optionCButton.setStyle(STYLE_SELECTED);
            case "D" -> optionDButton.setStyle(STYLE_SELECTED);
        }
        nextButton.setDisable(false);
    }

    private void resetOptionStyles() {
        optionAButton.setStyle(STYLE_NORMAL);
        optionBButton.setStyle(STYLE_NORMAL);
        optionCButton.setStyle(STYLE_NORMAL);
        optionDButton.setStyle(STYLE_NORMAL);
    }

    @FXML
    private void handlePreviousQuestion() {
        if (currentIndex > 0) afficherQuestion(--currentIndex);
    }

    @FXML
    private void handleNextQuestion() {
        if (userAnswers.get(currentIndex) == null) {
            AlertHelper.showWarning("Réponse requise", "Veuillez sélectionner une réponse avant de continuer.");
            return;
        }
        if (currentIndex < questions.size() - 1) afficherQuestion(++currentIndex);
    }

    @FXML
    private void handleSubmitQuiz() {
        if (userAnswers.get(currentIndex) == null) {
            AlertHelper.showWarning("Réponse requise", "Veuillez répondre à la dernière question.");
            return;
        }

        // Calcul du score
        int earnedPoints = 0;
        int totalPoints  = 0;
        int correctCount = 0;

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            totalPoints += q.getPoints();
            if (q.getBonneReponse() != null && q.getBonneReponse().equals(userAnswers.get(i))) {
                earnedPoints += q.getPoints();
                correctCount++;
            }
        }

        // Naviguer vers result.fxml
        final int ep = earnedPoints, tp = totalPoints, cc = correctCount;
        NavigationService.getInstance().navigateFrom(
                submitButton,
                "/view/result.fxml",
                "BioSync - Résultat",
                540, 640,
                ctrl -> {
                    if (ctrl instanceof QuizResultController) {
                        ((QuizResultController) ctrl).setResultat(ep, tp, cc, questions.size(),
                                currentQuiz, currentUser);
                    }
                });
    }

    // ── VIEW 3 – Résultat inline (si result.fxml non utilisé) ──

    private void afficherResultat(int earnedPoints, int totalPoints, int correctCount) {
        double pct = totalPoints > 0 ? (earnedPoints * 100.0 / totalPoints) : 0;
        int    pctInt = (int) Math.round(pct);

        resultTitleLabel.setText(currentQuiz.getTitre());
        percentageLabel.setText(pctInt + "%");
        scoreLabel.setText(earnedPoints + " / " + totalPoints + " pts");
        messageLabel.setText(genererMessage(pctInt));

        // Médaille
        String[] medal = calculerMedaille(pctInt);
        medalLabel.setText(medal[0]);
        medalContainer.setStyle(medalContainer.getStyle()
                .replace("linear-gradient(to right, #FCD34D, #F59E0B)",
                         "linear-gradient(to right, " + medal[1] + ")"));

        afficherVue(3);
    }

    @FXML
    private void handleRetryQuiz() {
        if (currentQuiz != null) demarrerQuiz(currentQuiz);
    }

    // ── Navigation ──────────────────────────────────────────────

    @FXML
    private void handleRetour() {
        NavigationService.getInstance().navigateToQuizManager(welcomeLabel, currentUser);
    }

    @FXML
    private void handleBackToQuizzes() {
        afficherVue(1);
        chargerListeQuiz();
    }

    // ── Utilitaires ─────────────────────────────────────────────

    private void afficherVue(int vue) {
        quizSelectionView.setVisible(vue == 1);
        quizSelectionView.setManaged(vue == 1);
        quizPlayView.setVisible(vue == 2);
        quizPlayView.setManaged(vue == 2);
        resultView.setVisible(vue == 3);
        resultView.setManaged(vue == 3);
    }

    private String genererMessage(int pct) {
        if (pct >= 80) return "🎉 Excellent ! Vous maîtrisez très bien ce sujet !";
        if (pct >= 60) return "👍 Bien joué ! Continuez sur votre lancée !";
        if (pct >= 40) return "💪 Pas mal ! Un peu de pratique et vous progresserez.";
        return "📖 Courage ! Révisez et réessayez, vous pouvez faire mieux !";
    }

    private String[] calculerMedaille(int pct) {
        if (pct >= 80) return new String[]{"🥇 Médaille d'Or",    "#FCD34D, #F59E0B"};
        if (pct >= 60) return new String[]{"🥈 Médaille d'Argent","#D1D5DB, #9CA3AF"};
        if (pct >= 40) return new String[]{"🥉 Médaille de Bronze","#D97706, #B45309"};
        return new String[]{"🎯 Participation",                     "#6EC5A6, #059669"};
    }

    private String nvl(String s) { return s != null ? s : ""; }

    private Label labelStyle(String text, String style) {
        Label lbl = new Label(text);
        lbl.setStyle(style);
        return lbl;
    }
}
