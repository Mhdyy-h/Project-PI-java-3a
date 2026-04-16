package org.example.controller;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.util.Duration;
import javafx.util.Interpolator;
import org.example.model.Question;
import org.example.model.Quiz;
import org.example.model.User;
import org.example.service.NavigationService;
import org.example.service.QuestionService;
import org.example.service.QuizService;
import org.example.util.AlertHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur de vue_utilisateur.fxml
 *
 * Gère 4 écrans pour le passage d'un quiz par un patient :
 *   Écran 1 – Sélection du quiz
 *   Écran 2 – Introduction / présentation du quiz
 *   Écran 3 – Question courante + timer circulaire
 *   Écran 4 – Résultat final animé
 */
public class VueUtilisateurController {

    // ── Écran 1 ────────────────────────────────────────────────
    @FXML private AnchorPane screenSelection;
    @FXML private FlowPane   quizCardsContainer;
    @FXML private Label      emptyLabel;

    // ── Écran 2 ────────────────────────────────────────────────
    @FXML private AnchorPane screenIntro;
    @FXML private Label      introEmoji;
    @FXML private Label      introTitre;
    @FXML private Label      introDescription;
    @FXML private Label      introNbQuestions;
    @FXML private Label      introDifficulte;
    @FXML private Button     btnDemarrer;

    // ── Écran 3 ────────────────────────────────────────────────
    @FXML private AnchorPane  screenQuestion;
    @FXML private Label       labelProgression;
    @FXML private Label       labelScore;
    @FXML private ProgressBar progressQuiz;
    @FXML private Canvas      timerCanvas;
    @FXML private Label       timerLabel;
    @FXML private Label       labelNumero;
    @FXML private Label       labelQuestion;
    @FXML private Button      btnA;
    @FXML private Button      btnB;
    @FXML private Button      btnC;
    @FXML private Button      btnD;

    // ── Écran 4 ────────────────────────────────────────────────
    @FXML private AnchorPane screenResultat;
    @FXML private Label      resultatEmoji;
    @FXML private Label      resultatTitre;
    @FXML private Label      resultatMessage;
    @FXML private StackPane  scoreCercle;
    @FXML private Label      labelScoreAnime;
    @FXML private Label      labelPourcentage;
    @FXML private VBox       recapContainer;

    // ── Services ───────────────────────────────────────────────
    private final QuizService     quizService     = new QuizService();
    private final QuestionService questionService = new QuestionService();

    // ── État ───────────────────────────────────────────────────
    private User             currentUser;
    private List<Quiz>       quizActifs        = new ArrayList<>();
    private Quiz             quizSelectionne;
    private List<Question>   questions         = new ArrayList<>();
    private int              indexQuestion     = 0;
    private int              score             = 0;
    private List<Boolean>    reponsesResultat  = new ArrayList<>();
    private boolean          reponseVerrouillee = false;
    private AnchorPane       screenActuel;

    // Timer
    private Timeline timerTimeline;
    private int      tempsRestant;
    private static final int TEMPS_PAR_QUESTION = 30;

    // Boutons réponses (tableau pour boucler facilement)
    private Button[] boutonsReponse;

    // ── Cycle de vie ───────────────────────────────────────────

    @FXML
    public void initialize() {
        boutonsReponse = new Button[]{btnA, btnB, btnC, btnD};
        screenActuel   = screenSelection;
        chargerQuizActifs();
    }

    /** Injecté par NavigationService. */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    // ══════════════════════════════════════════════════════════
    // ÉCRAN 1 — Sélection du Quiz
    // ══════════════════════════════════════════════════════════

    private void chargerQuizActifs() {
        quizActifs = quizService.getAllQuiz().stream()
                .filter(Quiz::isActif)
                .collect(Collectors.toList());

        if (quizActifs.isEmpty()) {
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
        } else {
            emptyLabel.setVisible(false);
            emptyLabel.setManaged(false);
            afficherCardsAvecAnimation();
        }
    }

    private void afficherCardsAvecAnimation() {
        quizCardsContainer.getChildren().clear();

        for (int i = 0; i < quizActifs.size(); i++) {
            Quiz   quiz = quizActifs.get(i);
            VBox   card = creerQuizCard(quiz);
            card.setOpacity(0);
            card.setTranslateY(30);
            quizCardsContainer.getChildren().add(card);

            // Animation staggerée : chaque carte apparaît 100ms après la précédente
            final int delay = i * 100;
            PauseTransition pause = new PauseTransition(Duration.millis(delay));
            pause.setOnFinished(e -> {
                FadeTransition fade  = new FadeTransition(Duration.millis(400), card);
                fade.setToValue(1);
                TranslateTransition slide = new TranslateTransition(Duration.millis(400), card);
                slide.setToY(0);
                slide.setInterpolator(Interpolator.EASE_OUT);
                new ParallelTransition(fade, slide).play();
            });
            pause.play();
        }
    }

    private VBox creerQuizCard(Quiz quiz) {
        VBox card = new VBox(10);
        card.getStyleClass().add("quiz-card");
        card.setPrefWidth(260);
        card.setCursor(Cursor.HAND);

        Label emoji = new Label("🧠");
        emoji.getStyleClass().add("card-emoji");

        Label titre = new Label(quiz.getTitre() != null ? quiz.getTitre() : "Sans titre");
        titre.getStyleClass().add("card-titre");
        titre.setWrapText(true);
        titre.setMaxWidth(220);

        // Afficher le statut + niveau stress
        String meta = "Niveau : " + quiz.getNiveauStressCible();
        Label metaLbl = new Label(meta);
        metaLbl.getStyleClass().add("card-meta");

        int nbQ = questionService.countQuestionsByQuiz(quiz.getId());
        Label nbQuestionsLbl = new Label(nbQ + " question" + (nbQ > 1 ? "s" : ""));
        nbQuestionsLbl.getStyleClass().add("card-questions");

        Button btnCommencer = new Button("Commencer ▶");
        btnCommencer.getStyleClass().add("card-btn");
        btnCommencer.setOnAction(e -> ouvrirIntro(quiz));
        btnCommencer.setMaxWidth(Double.MAX_VALUE);

        // Hover scale
        card.setOnMouseEntered(e -> {
            ScaleTransition sc = new ScaleTransition(Duration.millis(150), card);
            sc.setToX(1.03); sc.setToY(1.03);
            sc.play();
        });
        card.setOnMouseExited(e -> {
            ScaleTransition sc = new ScaleTransition(Duration.millis(150), card);
            sc.setToX(1.0); sc.setToY(1.0);
            sc.play();
        });

        card.getChildren().addAll(emoji, titre, metaLbl, nbQuestionsLbl, btnCommencer);
        return card;
    }

    // ══════════════════════════════════════════════════════════
    // ÉCRAN 2 — Introduction
    // ══════════════════════════════════════════════════════════

    private void ouvrirIntro(Quiz quiz) {
        this.quizSelectionne = quiz;

        introTitre.setText(quiz.getTitre() != null ? quiz.getTitre() : "—");
        introDescription.setText(quiz.getDescription() != null && !quiz.getDescription().isBlank()
                ? quiz.getDescription() : "Évaluez vos capacités cognitives.");
        introDifficulte.setText(String.valueOf(quiz.getNiveauStressCible()));

        int nbQ = questionService.countQuestionsByQuiz(quiz.getId());
        introNbQuestions.setText(String.valueOf(nbQ));

        transitionVers(screenSelection, screenIntro);
    }

    @FXML
    private void retourSelection() {
        arreterTimer();
        resetEtat();
        AnchorPane depuis = screenActuel != null ? screenActuel : screenIntro;
        transitionVers(depuis, screenSelection);
        // Recharger les cartes pour qu'elles soient fraîches
        PauseTransition p = new PauseTransition(Duration.millis(260));
        p.setOnFinished(e -> {
            quizCardsContainer.getChildren().clear();
            afficherCardsAvecAnimation();
        });
        p.play();
    }

    // ══════════════════════════════════════════════════════════
    // ÉCRAN 3 — Passage du Quiz
    // ══════════════════════════════════════════════════════════

    @FXML
    private void demarrerQuiz() {
        questions = questionService.getQuestionsByQuizId(quizSelectionne.getId());

        if (questions == null || questions.isEmpty()) {
            AlertHelper.showWarning("Quiz vide",
                    "Ce quiz ne contient aucune question. Ajoutez des questions avant de jouer.");
            return;
        }

        indexQuestion = 0;
        score = 0;
        reponsesResultat.clear();

        transitionVers(screenIntro, screenQuestion);
        afficherQuestion();
    }

    private void afficherQuestion() {
        if (indexQuestion >= questions.size()) {
            afficherResultat();
            return;
        }

        Question q = questions.get(indexQuestion);
        reponseVerrouillee = false;

        // Progression
        int total = questions.size();
        labelProgression.setText("Question " + (indexQuestion + 1) + " / " + total);
        labelScore.setText("Score : " + score + " / " + indexQuestion);
        progressQuiz.setProgress((double) indexQuestion / total);

        // Contenu question
        labelNumero.setText("Question " + (indexQuestion + 1));
        labelQuestion.setText(q.getEnonce() != null ? q.getEnonce() : "");

        // Options (A/B/C/D depuis optionsFausses pipe-separated)
        String[] textes = {q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD()};
        for (int i = 0; i < boutonsReponse.length; i++) {
            Button btn = boutonsReponse[i];
            String txt  = textes[i] != null ? textes[i] : "";
            btn.setText(txt);

            boolean visible = !txt.isBlank();
            btn.setVisible(visible);
            btn.setManaged(visible);
            btn.setDisable(false);

            // Reset styles
            btn.getStyleClass().removeAll("btn-correct", "btn-incorrect", "btn-reponse-disabled");
            if (!btn.getStyleClass().contains("btn-reponse")) {
                btn.getStyleClass().add("btn-reponse");
            }
        }

        // Animation slide-in depuis la droite
        screenQuestion.setTranslateX(80);
        TranslateTransition slide = new TranslateTransition(Duration.millis(280), screenQuestion);
        slide.setToX(0);
        slide.setInterpolator(Interpolator.EASE_OUT);
        slide.play();

        demarrerTimer();
    }

    @FXML
    private void choisirReponse(ActionEvent event) {
        if (reponseVerrouillee) return;
        reponseVerrouillee = true;
        arreterTimer();

        Button btnClique = (Button) event.getSource();
        String reponseChoisie = btnClique.getUserData() != null
                ? btnClique.getUserData().toString() : "";
        String bonneReponse = questions.get(indexQuestion).getReponseCorrecte();
        if (bonneReponse == null) bonneReponse = "";

        boolean correct = reponseChoisie.equalsIgnoreCase(bonneReponse);
        reponsesResultat.add(correct);
        if (correct) score++;

        // Désactiver tous les boutons + colorier
        for (Button btn : boutonsReponse) {
            btn.setDisable(true);
            String userData = btn.getUserData() != null ? btn.getUserData().toString() : "";
            if (userData.equalsIgnoreCase(bonneReponse)) {
                btn.getStyleClass().removeAll("btn-reponse");
                btn.getStyleClass().add("btn-correct");
            } else if (btn == btnClique && !correct) {
                btn.getStyleClass().removeAll("btn-reponse");
                btn.getStyleClass().add("btn-incorrect");
            }
        }

        // Animation shake sur mauvaise réponse
        if (!correct) {
            TranslateTransition shake = new TranslateTransition(Duration.millis(55), btnClique);
            shake.setFromX(-8);
            shake.setToX(8);
            shake.setCycleCount(4);
            shake.setAutoReverse(true);
            shake.play();
        }

        // Passer à la question suivante après 1.2s
        PauseTransition pause = new PauseTransition(Duration.millis(1200));
        pause.setOnFinished(e -> {
            indexQuestion++;
            afficherQuestion();
        });
        pause.play();
    }

    // ── Timer circulaire ────────────────────────────────────────

    private void demarrerTimer() {
        tempsRestant = TEMPS_PAR_QUESTION;
        dessinerTimer(tempsRestant);

        timerTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            tempsRestant--;
            dessinerTimer(tempsRestant);

            if (tempsRestant <= 0) {
                timerTimeline.stop();
                // Temps écoulé → réponse incorrecte automatique
                reponseVerrouillee = true;
                reponsesResultat.add(false);

                for (Button btn : boutonsReponse) btn.setDisable(true);

                // Flash rouge sur le timer
                timerLabel.getStyleClass().add("timer-expire");
                String correcte = questions.get(indexQuestion).getReponseCorrecte();
                if (correcte != null) {
                    for (Button btn : boutonsReponse) {
                        if (btn.getUserData() != null
                                && btn.getUserData().toString().equalsIgnoreCase(correcte)) {
                            btn.getStyleClass().removeAll("btn-reponse");
                            btn.getStyleClass().add("btn-correct");
                        }
                    }
                }

                PauseTransition p = new PauseTransition(Duration.millis(900));
                p.setOnFinished(ev -> {
                    timerLabel.getStyleClass().remove("timer-expire");
                    indexQuestion++;
                    afficherQuestion();
                });
                p.play();
            }
        }));
        timerTimeline.setCycleCount(Timeline.INDEFINITE);
        timerTimeline.play();
    }

    private void dessinerTimer(int secondesRestantes) {
        GraphicsContext gc = timerCanvas.getGraphicsContext2D();
        double w = timerCanvas.getWidth();
        double h = timerCanvas.getHeight();
        gc.clearRect(0, 0, w, h);

        // Cercle de fond gris
        gc.setStroke(Color.web("#E5E7EB"));
        gc.setLineWidth(6);
        gc.strokeOval(6, 6, w - 12, h - 12);

        // Arc de progression
        double progress = (double) Math.max(secondesRestantes, 0) / TEMPS_PAR_QUESTION;
        gc.setStroke(secondesRestantes <= 10 ? Color.web("#E24B4A") : Color.web("#1D9E75"));
        gc.setLineWidth(6);
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.strokeArc(6, 6, w - 12, h - 12, 90, -360.0 * progress, ArcType.OPEN);

        // Label texte
        timerLabel.setText(String.valueOf(Math.max(secondesRestantes, 0)));
        String couleurTexte = secondesRestantes <= 10 ? "#E24B4A" : "#1D9E75";
        timerLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + couleurTexte + ";");
    }

    private void arreterTimer() {
        if (timerTimeline != null) {
            timerTimeline.stop();
            timerTimeline = null;
        }
    }

    // ══════════════════════════════════════════════════════════
    // ÉCRAN 4 — Résultat Final
    // ══════════════════════════════════════════════════════════

    private void afficherResultat() {
        arreterTimer();
        int    total     = questions.size();
        double pct       = total > 0 ? (double) score / total * 100 : 0;
        int    pctInt    = (int) Math.round(pct);

        // Emoji et message
        if (pctInt >= 80) {
            resultatEmoji.setText("🏆");
            resultatTitre.setText("Excellent !");
            resultatMessage.setText("Félicitations ! Vous maîtrisez parfaitement ce sujet.");
        } else if (pctInt >= 60) {
            resultatEmoji.setText("👍");
            resultatTitre.setText("Bien joué !");
            resultatMessage.setText("Bon résultat ! Encore un effort pour atteindre l'excellence.");
        } else if (pctInt >= 40) {
            resultatEmoji.setText("💪");
            resultatTitre.setText("Continuez !");
            resultatMessage.setText("Vous progressez. Révisez les questions manquées et réessayez.");
        } else {
            resultatEmoji.setText("📚");
            resultatTitre.setText("Réessayez !");
            resultatMessage.setText("Ne vous découragez pas. Chaque essai est une occasion d'apprendre.");
        }

        labelPourcentage.setText(pctInt + "%");

        // Récapitulatif
        recapContainer.getChildren().clear();
        for (int i = 0; i < questions.size(); i++) {
            boolean correct = i < reponsesResultat.size() && reponsesResultat.get(i);
            HBox ligne = new HBox(10);
            ligne.setAlignment(Pos.CENTER_LEFT);
            ligne.getStyleClass().add(correct ? "recap-correct" : "recap-incorrect");

            Label icone = new Label(correct ? "✓" : "✗");
            icone.getStyleClass().add(correct ? "recap-icone-ok" : "recap-icone-ko");

            String contenuQ = questions.get(i).getEnonce();
            String extrait  = contenuQ != null && contenuQ.length() > 50
                    ? contenuQ.substring(0, 50) + "…" : contenuQ;
            Label texte = new Label("Q" + (i + 1) + " — " + extrait + "  → " + (correct ? "Correct" : "Incorrect"));
            texte.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (correct ? "#065F46" : "#991B1B") + ";");

            ligne.getChildren().addAll(icone, texte);
            recapContainer.getChildren().add(ligne);
        }

        transitionVers(screenQuestion, screenResultat);

        // Count-up animé : 0 → score
        final int finalScore = score;
        final int[] compteur  = {0};
        Timeline countUp = new Timeline(new KeyFrame(Duration.millis(40), e -> {
            labelScoreAnime.setText(compteur[0] + " / " + total);
            if (compteur[0] < finalScore) compteur[0]++;
        }));
        countUp.setCycleCount(finalScore + 1);

        // ZoomIn du cercle score
        ScaleTransition zoom = new ScaleTransition(Duration.millis(600), scoreCercle);
        zoom.setFromX(0.2);
        zoom.setFromY(0.2);
        zoom.setToX(1.0);
        zoom.setToY(1.0);
        zoom.setInterpolator(Interpolator.SPLINE(0.34, 1.56, 0.64, 1.0));

        // Démarrer après la transition de l'écran
        PauseTransition delai = new PauseTransition(Duration.millis(400));
        delai.setOnFinished(e -> {
            zoom.play();
            countUp.play();
        });
        delai.play();
    }

    @FXML
    private void rejouer() {
        resetEtat();
        transitionVers(screenResultat, screenIntro);
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION
    // ══════════════════════════════════════════════════════════

    @FXML
    private void handleRetourManager() {
        arreterTimer();
        NavigationService.getInstance().navigateToQuizManager(btnA, currentUser);
    }

    // ── Transition entre écrans ─────────────────────────────────

    /**
     * Transition FadeOut de {@code depuis} puis FadeIn + SlideIn de {@code vers}.
     * L'écran actuel est mis à jour automatiquement.
     */
    private void transitionVers(AnchorPane depuis, AnchorPane vers) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(220), depuis);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            depuis.setVisible(false);

            vers.setVisible(true);
            vers.setOpacity(0);
            vers.setTranslateX(40);

            FadeTransition     fadeIn = new FadeTransition(Duration.millis(280), vers);
            TranslateTransition slide = new TranslateTransition(Duration.millis(280), vers);
            fadeIn.setToValue(1);
            slide.setToX(0);
            slide.setInterpolator(Interpolator.EASE_OUT);

            new ParallelTransition(fadeIn, slide).play();
            screenActuel = vers;
        });
        fadeOut.play();
    }

    // ── Réinitialisation ────────────────────────────────────────

    private void resetEtat() {
        indexQuestion      = 0;
        score              = 0;
        reponsesResultat.clear();
        reponseVerrouillee = false;
        arreterTimer();
    }
}
