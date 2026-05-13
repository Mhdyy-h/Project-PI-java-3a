package org.example.controller;

import org.example.model.User;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.util.Duration;

import javafx.application.Platform;
import org.example.DatabaseConnection;
import org.example.model.Question;
import org.example.model.Quiz;
import org.example.service.MentalHealthTipsService;
import org.example.service.NavigationService;
import org.example.service.QuestionService;
import org.example.service.QuizService;
import org.example.service.VoiceService;
import org.example.service.WellnessApiService;
import org.example.util.AlertHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.example.service.AnomalyDetectionService;

public class VueUtilisateurController {

    // ── Écran 1 ────────────────────────────────────────────────
    @FXML private AnchorPane screenSelection;
    @FXML private FlowPane   quizCardsContainer;
    @FXML private Label      emptyLabel;
    @FXML private HBox       wellnessBanner;
    @FXML private Label      wellnessEmojiLabel;
    @FXML private Label      wellnessTipLabel;
    @FXML private Label      apiSourceLabel;
    @FXML private TextField  searchField;
    @FXML private Label      countLabel;
    @FXML private Button     btnFilterAll;
    @FXML private Button     btnFilterFaible;
    @FXML private Button     btnFilterModere;
    @FXML private Button     btnFilterEleve;

    // ── Stats labels ───────────────────────────────────────────
    @FXML private Label      statEloLabel;
    @FXML private Label      statTauxLabel;
    @FXML private Label      statSessionsLabel;
    @FXML private Label      statNiveauLabel;

    // ── Écran 2 ────────────────────────────────────────────────
    @FXML private AnchorPane  screenIntro;
    @FXML private Label       introEmoji;
    @FXML private Label       introTitre;
    @FXML private Label       introDescription;
    @FXML private Label       introNbQuestions;
    @FXML private Label       introDifficulte;
    @FXML private Button      btnDemarrer;
    @FXML private ToggleButton btnVocal;

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

    // ── Écran 3 extras ─────────────────────────────────────────
    @FXML private Label      focusIndicatorLabel;

    // ── Écran 4 ────────────────────────────────────────────────
    @FXML private AnchorPane screenResultat;
    @FXML private Label      resultatEmoji;
    @FXML private Label      resultatTitre;
    @FXML private Label      resultatMessage;
    @FXML private StackPane  scoreCercle;
    @FXML private Label      labelScoreAnime;
    @FXML private Label      labelPourcentage;
    @FXML private VBox       recapContainer;

    // ── Anomaly panel (écran 4) ─────────────────────────────────
    @FXML private VBox   anomalyPanel;
    @FXML private Label  anomalyStatusLabel;
    @FXML private Label  focusScoreLabel;
    @FXML private Label  avgTimeLabel;
    @FXML private Label  anomalyCountLabel;
    @FXML private Label  anomalyRecommendationLabel;
    @FXML private VBox   anomalyListContainer;
    @FXML private Canvas confettiCanvas;

    // ── Services ───────────────────────────────────────────────
    private final QuizService              quizService     = new QuizService();
    private final QuestionService          questionService = new QuestionService();
    private final AnomalyDetectionService  anomalyService  = new AnomalyDetectionService();

    // ── État ───────────────────────────────────────────────────
    private User             currentUser;
    private List<Quiz>       allQuizActifs     = new ArrayList<>();
    private List<Quiz>       quizActifs        = new ArrayList<>();
    private Quiz             quizSelectionne;
    private List<Question>   questions         = new ArrayList<>();
    private int              indexQuestion     = 0;
    private int              score             = 0;
    private List<Boolean>    reponsesResultat  = new ArrayList<>();
    private boolean          reponseVerrouillee = false;
    private AnchorPane       screenActuel;

    // Search / filter state
    private String searchText   = "";
    private int    filterNiveau = -1;

    // Anomaly tracking
    private final List<AnomalyDetectionService.QuestionRecord> questionRecords = new ArrayList<>();
    private long questionStartTime = 0;

    // Voice mode
    private boolean voiceMode = false;

    // Timer
    private Timeline timerTimeline;
    private int      tempsRestant;
    private static final int TEMPS_PAR_QUESTION = 30;

    // Boutons réponses
    private Button[] boutonsReponse;

    // ── Cycle de vie ───────────────────────────────────────────

    @FXML
    public void initialize() {
        boutonsReponse = new Button[]{btnA, btnB, btnC, btnD};
        screenActuel   = screenSelection;
        chargerQuizActifs();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        chargerStatsEtApi();
    }

    // ══════════════════════════════════════════════════════════
    // ÉCRAN 1 — Sélection du Quiz
    // ══════════════════════════════════════════════════════════

    private void chargerQuizActifs() {
        allQuizActifs = quizService.getAllQuiz().stream()
                .filter(Quiz::isActif)
                .collect(Collectors.toList());
        quizActifs = new ArrayList<>(allQuizActifs);

        afficherCardsFiltered();
        chargerStatsEtApi();
    }

    private void chargerStatsEtApi() {
        String localTip = MentalHealthTipsService.getTipOfDay();

        Thread t = new Thread(() -> {
            // ── Appel API (gratuit, sans clé) ───────────────────
            WellnessApiService.ApiResult result =
                    WellnessApiService.fetchWithFallback(localTip);

            // ── Lecture des stats profil cognitif ───────────────
            int    elo      = 0;
            double taux     = 0.0;
            int    sessions = 0;
            String niveau   = null;

            int userId = currentUser != null ? currentUser.getId() : -1;
            if (userId > 0) {
                try (Connection cn = DatabaseConnection.getConnection();
                     PreparedStatement ps = cn.prepareStatement(
                         "SELECT score_elo, taux_reussite, sessions_totales, niveau_actuel " +
                         "FROM profil_cognitif WHERE utilisateur_id = ? LIMIT 1")) {
                    ps.setInt(1, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            elo      = rs.getInt("score_elo");
                            taux     = rs.getDouble("taux_reussite");
                            sessions = rs.getInt("sessions_totales");
                            niveau   = rs.getString("niveau_actuel");
                        }
                    }
                } catch (Exception ignored) {}
            }

            final WellnessApiService.ApiResult apiResult = result;
            final int    fElo      = elo;
            final double fTaux     = taux;
            final int    fSessions = sessions;
            final String fNiveau   = niveau;

            Platform.runLater(() -> {
                // Mettre à jour le bandeau wellness
                if (wellnessTipLabel != null && apiResult != null) {
                    wellnessTipLabel.setText(apiResult.text);
                    if (wellnessEmojiLabel != null) {
                        wellnessEmojiLabel.setText(apiResult.emoji);
                    }
                    if (apiSourceLabel != null) {
                        apiSourceLabel.setText(apiResult.source);
                        apiSourceLabel.setVisible(true);
                        apiSourceLabel.setManaged(true);
                    }
                }

                // Mettre à jour les stats
                if (fElo > 0) {
                    if (statEloLabel != null)      statEloLabel.setText(String.valueOf(fElo));
                    if (statTauxLabel != null)     statTauxLabel.setText(String.format("%.0f%%", fTaux));
                    if (statSessionsLabel != null) statSessionsLabel.setText(String.valueOf(fSessions));
                    if (statNiveauLabel != null && fNiveau != null) {
                        statNiveauLabel.setText(fNiveau);
                        String couleur = switch (fNiveau) {
                            case "AVANCE"        -> "#7C6FCD";
                            case "INTERMEDIAIRE" -> "#1D9E75";
                            default              -> "#34D399";
                        };
                        statNiveauLabel.setStyle(
                            "-fx-font-size: 15px; -fx-font-weight: 900; -fx-text-fill: " + couleur + ";");
                    }
                }
            });
        });
        t.setDaemon(true);
        t.start();
    }

    private void afficherCardsFiltered() {
        quizCardsContainer.getChildren().clear();

        if (countLabel != null) {
            countLabel.setText(quizActifs.size() + " quiz disponible" + (quizActifs.size() > 1 ? "s" : ""));
        }

        if (quizActifs.isEmpty()) {
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
            return;
        }

        emptyLabel.setVisible(false);
        emptyLabel.setManaged(false);
        afficherCardsAvecAnimation();
    }

    private void afficherCardsAvecAnimation() {
        quizCardsContainer.getChildren().clear();

        for (int i = 0; i < quizActifs.size(); i++) {
            VBox card = creerQuizCard(quizActifs.get(i));
            card.setOpacity(0);
            card.setTranslateY(24);
            quizCardsContainer.getChildren().add(card);

            final int delay = i * 80;
            PauseTransition pause = new PauseTransition(Duration.millis(delay));
            pause.setOnFinished(e -> {
                FadeTransition fade = new FadeTransition(Duration.millis(350), card);
                fade.setToValue(1);
                TranslateTransition slide = new TranslateTransition(Duration.millis(350), card);
                slide.setToY(0);
                slide.setInterpolator(Interpolator.EASE_OUT);
                new ParallelTransition(fade, slide).play();
            });
            pause.play();
        }
    }

    private VBox creerQuizCard(Quiz quiz) {
        int    niveau       = quiz.getNiveauStressCible();
        String accentColor  = MentalHealthTipsService.getColorForLevel(niveau);
        String lightBg      = MentalHealthTipsService.getLightBgForLevel(niveau);
        String levelLabel   = MentalHealthTipsService.getLabelForLevel(niveau);
        String levelEmoji   = MentalHealthTipsService.getEmojiForLevel(niveau);
        String medaille     = quiz.getMedailleQuiz();
        String medalEmoji   = MentalHealthTipsService.getEmojiForMedal(medaille);
        String medalBg      = MentalHealthTipsService.getBgColorForMedal(medaille);
        String medalText    = MentalHealthTipsService.getTextColorForMedal(medaille);

        // ── Card container ──────────────────────────────────────
        VBox card = new VBox(0);
        card.setPrefWidth(270);
        card.setMaxWidth(270);
        card.setCursor(Cursor.HAND);
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #E5E7EB;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 16;" +
            "-fx-background-radius: 16;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 8, 0, 0, 3);"
        );

        // ── Accent strip (colored top bar) ──────────────────────
        HBox strip = new HBox();
        strip.setPrefHeight(6);
        strip.setStyle(
            "-fx-background-color: " + accentColor + ";" +
            "-fx-background-radius: 15 15 0 0;"
        );

        // ── Card body ───────────────────────────────────────────
        VBox body = new VBox(10);
        body.setStyle("-fx-padding: 16 18 14 18;");

        // Top row: emoji + medal badge
        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label emoji = new Label(levelEmoji);
        emoji.setStyle("-fx-font-size: 28px;");

        Region spacerTop = new Region();
        HBox.setHgrow(spacerTop, Priority.ALWAYS);
        topRow.getChildren().addAll(emoji, spacerTop);

        if (medaille != null && !medaille.isBlank()) {
            HBox medalBox = new HBox(3);
            medalBox.setAlignment(Pos.CENTER);
            medalBox.setStyle(
                "-fx-background-color: " + medalBg + ";" +
                "-fx-background-radius: 20;" +
                "-fx-padding: 3 10 3 10;"
            );
            Label medalLbl = new Label(medalEmoji + " " + medaille);
            medalLbl.setStyle(
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + medalText + ";"
            );
            medalBox.getChildren().add(medalLbl);
            topRow.getChildren().add(medalBox);
        }

        // Title
        Label titre = new Label(quiz.getTitre() != null ? quiz.getTitre() : "Sans titre");
        titre.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #085041;");
        titre.setWrapText(true);
        titre.setMaxWidth(234);

        body.getChildren().addAll(topRow, titre);

        // Description
        String desc = quiz.getDescription();
        if (desc != null && !desc.isBlank()) {
            String short_ = desc.length() > 72 ? desc.substring(0, 72) + "…" : desc;
            Label descLbl = new Label(short_);
            descLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
            descLbl.setWrapText(true);
            descLbl.setMaxWidth(234);
            body.getChildren().add(descLbl);
        }

        // Stats row: level badge + question count
        int nbQ = questionService.countQuestionsByQuiz(quiz.getId());
        HBox statsRow = new HBox(10);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        statsRow.setStyle(
            "-fx-padding: 8 0 4 0;" +
            "-fx-border-color: #F0F0F0 transparent transparent transparent;" +
            "-fx-border-width: 1 0 0 0;"
        );

        Label levelBadge = new Label(levelLabel);
        levelBadge.setStyle(
            "-fx-background-color: " + lightBg + ";" +
            "-fx-text-fill: " + accentColor + ";" +
            "-fx-font-size: 11px; -fx-font-weight: bold;" +
            "-fx-background-radius: 20; -fx-padding: 3 10 3 10;"
        );

        Region statSpacer = new Region();
        HBox.setHgrow(statSpacer, Priority.ALWAYS);

        Label nbQLabel = new Label("📝 " + nbQ + " question" + (nbQ > 1 ? "s" : ""));
        nbQLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #1D9E75;");

        statsRow.getChildren().addAll(levelBadge, statSpacer, nbQLabel);
        body.getChildren().add(statsRow);

        // Start button
        Button btnCommencer = new Button("▶  Commencer");
        btnCommencer.setMaxWidth(Double.MAX_VALUE);
        btnCommencer.setStyle(
            "-fx-background-color: " + accentColor + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px; -fx-font-weight: bold;" +
            "-fx-background-radius: 10; -fx-padding: 10 18 10 18;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 4, 0, 0, 2);"
        );
        btnCommencer.setOnAction(e -> ouvrirIntro(quiz));
        VBox.setMargin(btnCommencer, new Insets(4, 0, 0, 0));
        body.getChildren().add(btnCommencer);

        card.getChildren().addAll(strip, body);

        // ── Hover effect ────────────────────────────────────────
        String baseStyle =
            "-fx-background-color: white;" +
            "-fx-border-color: #E5E7EB;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 16;" +
            "-fx-background-radius: 16;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 8, 0, 0, 3);";
        String hoverStyle =
            "-fx-background-color: white;" +
            "-fx-border-color: " + accentColor + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 16;" +
            "-fx-background-radius: 16;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.14), 14, 0, 0, 5);";

        card.setOnMouseEntered(e -> {
            card.setStyle(hoverStyle);
            ScaleTransition sc = new ScaleTransition(Duration.millis(140), card);
            sc.setToX(1.025); sc.setToY(1.025);
            sc.play();
        });
        card.setOnMouseExited(e -> {
            card.setStyle(baseStyle);
            ScaleTransition sc = new ScaleTransition(Duration.millis(140), card);
            sc.setToX(1.0); sc.setToY(1.0);
            sc.play();
        });

        return card;
    }

    // ── Search & Filter ─────────────────────────────────────────

    @FXML
    private void onSearchChanged() {
        searchText = searchField != null && searchField.getText() != null
                ? searchField.getText().toLowerCase().trim() : "";
        appliquerFiltres();
    }

    @FXML private void onFilterAll()    { filterNiveau = -1; updateFilterChips(btnFilterAll);    appliquerFiltres(); }
    @FXML private void onFilterFaible() { filterNiveau = 1;  updateFilterChips(btnFilterFaible); appliquerFiltres(); }
    @FXML private void onFilterModere() { filterNiveau = 2;  updateFilterChips(btnFilterModere); appliquerFiltres(); }
    @FXML private void onFilterEleve()  { filterNiveau = 3;  updateFilterChips(btnFilterEleve);  appliquerFiltres(); }

    private void appliquerFiltres() {
        quizActifs = allQuizActifs.stream()
                .filter(q -> {
                    boolean matchSearch = searchText.isEmpty()
                            || (q.getTitre() != null && q.getTitre().toLowerCase().contains(searchText))
                            || (q.getDescription() != null && q.getDescription().toLowerCase().contains(searchText));
                    boolean matchNiveau = filterNiveau == -1 || niveauMatchesFilter(q.getNiveauStressCible(), filterNiveau);
                    return matchSearch && matchNiveau;
                })
                .collect(Collectors.toList());
        afficherCardsFiltered();
    }

    private boolean niveauMatchesFilter(int niveau, int filter) {
        return switch (filter) {
            case 1 -> niveau <= 3;
            case 2 -> niveau >= 4 && niveau <= 5;
            case 3 -> niveau >= 6;
            default -> true;
        };
    }

    private void updateFilterChips(Button active) {
        String on  = "-fx-background-color: #1D9E75; -fx-text-fill: white; " +
                     "-fx-font-size: 12px; -fx-font-weight: bold; " +
                     "-fx-background-radius: 20; -fx-padding: 7 16 7 16; -fx-cursor: hand;";
        String off = "-fx-background-color: white; -fx-text-fill: #6B7280; " +
                     "-fx-border-color: #D1D5DB; -fx-border-width: 1; " +
                     "-fx-font-size: 12px; -fx-font-weight: 600; " +
                     "-fx-background-radius: 20; -fx-padding: 7 16 7 16; -fx-cursor: hand;";
        for (Button btn : new Button[]{btnFilterAll, btnFilterFaible, btnFilterModere, btnFilterEleve}) {
            if (btn != null) btn.setStyle(btn == active ? on : off);
        }
    }

    // ══════════════════════════════════════════════════════════
    // VOICE MODE
    // ══════════════════════════════════════════════════════════

    @FXML
    private void toggleVoiceMode() {
        voiceMode = btnVocal.isSelected();
        if (voiceMode) {
            btnVocal.setText("🎤 Mode Vocal : Activé");
            btnVocal.setStyle("-fx-background-color: #1D9E75; -fx-text-fill: white; "
                    + "-fx-font-size: 13px; -fx-font-weight: 700; "
                    + "-fx-background-radius: 10; -fx-padding: 10 28 10 28; "
                    + "-fx-cursor: hand; -fx-border-color: #0F6E56; "
                    + "-fx-border-width: 1.5; -fx-border-radius: 10;");
            VoiceService.speakAsync("Mode vocal activé. Cliquez sur Démarrer le Quiz.");
        } else {
            btnVocal.setText("🎤 Mode Vocal : Désactivé");
            btnVocal.setStyle("-fx-background-color: #F0FDF4; -fx-text-fill: #166534; "
                    + "-fx-font-size: 13px; -fx-font-weight: 600; "
                    + "-fx-background-radius: 10; -fx-padding: 10 28 10 28; "
                    + "-fx-cursor: hand; -fx-border-color: #86EFAC; "
                    + "-fx-border-width: 1.5; -fx-border-radius: 10;");
        }
    }

    // ══════════════════════════════════════════════════════════
    // ÉCRAN 2 — Introduction
    // ══════════════════════════════════════════════════════════

    private void ouvrirIntro(Quiz quiz) {
        this.quizSelectionne = quiz;

        introTitre.setText(quiz.getTitre() != null ? quiz.getTitre() : "—");
        String desc = quiz.getDescription();
        introDescription.setText(desc != null && !desc.trim().isEmpty()
                ? desc : "Évaluez vos capacités cognitives.");
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

        if (voiceMode) {
            VoiceService.speakAsync("Quiz démarré : " + quizSelectionne.getTitre()
                    + ". Il y a " + questions.size() + " questions. Vous avez "
                    + TEMPS_PAR_QUESTION + " secondes par question.");
        }

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
        questionStartTime  = System.currentTimeMillis();

        int total = questions.size();
        labelProgression.setText("Question " + (indexQuestion + 1) + " / " + total);
        labelScore.setText("Score : " + score + " / " + indexQuestion);
        progressQuiz.setProgress((double) indexQuestion / total);

        labelNumero.setText("Question " + (indexQuestion + 1));
        labelQuestion.setText(q.getEnonce() != null ? q.getEnonce() : "");

        String[] textes = {q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD()};
        for (int i = 0; i < boutonsReponse.length; i++) {
            Button btn = boutonsReponse[i];
            String txt  = textes[i] != null ? textes[i] : "";
            btn.setText(txt);

            boolean visible = !txt.trim().isEmpty();
            btn.setVisible(visible);
            btn.setManaged(visible);
            btn.setDisable(false);

            btn.getStyleClass().removeAll("btn-correct", "btn-incorrect", "btn-reponse-disabled");
            if (!btn.getStyleClass().contains("btn-reponse")) {
                btn.getStyleClass().add("btn-reponse");
            }
        }

        screenQuestion.setTranslateX(80);
        TranslateTransition slide = new TranslateTransition(Duration.millis(280), screenQuestion);
        slide.setToX(0);
        slide.setInterpolator(Interpolator.EASE_OUT);
        slide.play();

        if (voiceMode) {
            String toSpeak = "Question " + (indexQuestion + 1) + " sur " + questions.size()
                    + ". " + (q.getEnonce() != null ? q.getEnonce() : "")
                    + ". A : " + nvl(q.getOptionA())
                    + ". B : " + nvl(q.getOptionB())
                    + ". C : " + nvl(q.getOptionC())
                    + ". D : " + nvl(q.getOptionD()) + ".";
            VoiceService.speakAsync(toSpeak);
        }

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

        long elapsed = System.currentTimeMillis() - questionStartTime;
        questionRecords.add(new AnomalyDetectionService.QuestionRecord(
                indexQuestion, elapsed, correct, reponseChoisie));
        updateFocusIndicator();

        if (voiceMode) {
            VoiceService.speakAsync(correct ? "Bonne réponse !" : "Mauvaise réponse.");
        }

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

        if (!correct) {
            TranslateTransition shake = new TranslateTransition(Duration.millis(55), btnClique);
            shake.setFromX(-8);
            shake.setToX(8);
            shake.setCycleCount(4);
            shake.setAutoReverse(true);
            shake.play();
        }

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
                reponseVerrouillee = true;
                reponsesResultat.add(false);

                long elapsed = System.currentTimeMillis() - questionStartTime;
                questionRecords.add(new AnomalyDetectionService.QuestionRecord(
                        indexQuestion, elapsed, false, ""));
                updateFocusIndicator();

                if (voiceMode) VoiceService.speakAsync("Temps écoulé !");

                for (Button btn : boutonsReponse) btn.setDisable(true);

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

        gc.setStroke(Color.web("#E5E7EB"));
        gc.setLineWidth(6);
        gc.strokeOval(6, 6, w - 12, h - 12);

        double progress = (double) Math.max(secondesRestantes, 0) / TEMPS_PAR_QUESTION;
        gc.setStroke(secondesRestantes <= 10 ? Color.web("#E24B4A") : Color.web("#1D9E75"));
        gc.setLineWidth(6);
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.strokeArc(6, 6, w - 12, h - 12, 90, -360.0 * progress, ArcType.OPEN);

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
        int    total  = questions.size();
        double pct    = total > 0 ? (double) score / total * 100 : 0;
        int    pctInt = (int) Math.round(pct);

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

        if (voiceMode) {
            VoiceService.speakAsync(resultatTitre.getText() + ". "
                    + "Vous avez obtenu " + score + " sur " + total + " questions. "
                    + pctInt + " pourcent.");
        }

        transitionVers(screenQuestion, screenResultat);
        afficherRapportAnomalies();
        lancerConfetti(pct);
        animerEmoji();

        final int finalScore = score;
        final int[] compteur = {0};
        Timeline countUp = new Timeline(new KeyFrame(Duration.millis(40), e -> {
            labelScoreAnime.setText(compteur[0] + " / " + total);
            if (compteur[0] < finalScore) compteur[0]++;
        }));
        countUp.setCycleCount(finalScore + 1);

        ScaleTransition zoom = new ScaleTransition(Duration.millis(600), scoreCercle);
        zoom.setFromX(0.2);
        zoom.setFromY(0.2);
        zoom.setToX(1.0);
        zoom.setToY(1.0);
        zoom.setInterpolator(Interpolator.SPLINE(0.34, 0.97, 0.64, 1.0));

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
        boolean isAdmin = currentUser != null && currentUser.getRoles() != null
                && currentUser.getRoles().contains("ROLE_ADMIN");
        if (isAdmin) {
            NavigationService.getInstance().navigateToQuizManager(btnA, currentUser);
        } else {
            NavigationService.getInstance().navigateToDashboard(btnA, currentUser);
        }
    }

    // ══════════════════════════════════════════════════════════
    // ANOMALY + ANIMATIONS
    // ══════════════════════════════════════════════════════════

    /** Met à jour l'indicateur de focus (🟢/🟡/🔴) pendant le quiz. */
    private void updateFocusIndicator() {
        if (focusIndicatorLabel == null) return;
        AnomalyDetectionService.AnomalyReport r = anomalyService.analyze(questionRecords);
        String dot = switch (r.overallStatus) {
            case "ALERTE"    -> "🔴";
            case "ATTENTION" -> "🟡";
            default          -> "🟢";
        };
        focusIndicatorLabel.setText(dot);
    }

    /** Remplit le panneau d'analyse à la fin du quiz. */
    private void afficherRapportAnomalies() {
        if (anomalyPanel == null) return;
        AnomalyDetectionService.AnomalyReport r = anomalyService.analyze(questionRecords);

        anomalyStatusLabel.setText(r.statusEmoji + "  " + r.overallStatus);
        String statusColor = switch (r.overallStatus) {
            case "ALERTE"    -> "#DC2626";
            case "ATTENTION" -> "#D97706";
            default          -> "#065F46";
        };
        anomalyStatusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + statusColor + ";");

        double fs = r.focusScore;
        String fsColor = fs >= 80 ? "#34D399" : fs >= 60 ? "#D97706" : "#E24B4A";
        focusScoreLabel.setText(String.format("%.0f", fs));
        focusScoreLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: 900; -fx-text-fill: " + fsColor + ";");

        avgTimeLabel.setText(String.format("%.1fs", r.avgResponseTimeSec));
        anomalyCountLabel.setText(String.valueOf(r.anomalies.size()));
        anomalyRecommendationLabel.setText(r.recommendation);

        anomalyListContainer.getChildren().clear();
        for (String anomaly : r.anomalies) {
            Label lbl = new Label(anomaly);
            lbl.setWrapText(true);
            lbl.setMaxWidth(Double.MAX_VALUE);
            lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #991B1B;" +
                    "-fx-background-color: #FEF2F2; -fx-background-radius: 6;" +
                    "-fx-padding: 4 10 4 10;");
            anomalyListContainer.getChildren().add(lbl);
        }

        // Slide-in du panneau avec délai
        anomalyPanel.setOpacity(0);
        anomalyPanel.setTranslateY(20);
        FadeTransition fade = new FadeTransition(Duration.millis(500), anomalyPanel);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(500), anomalyPanel);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);
        PauseTransition wait = new PauseTransition(Duration.millis(900));
        wait.setOnFinished(e -> new ParallelTransition(fade, slide).play());
        wait.play();
    }

    /** Emoji bounce au démarrage de l'écran résultat. */
    private void animerEmoji() {
        if (resultatEmoji == null) return;
        resultatEmoji.setScaleX(0.3);
        resultatEmoji.setScaleY(0.3);
        ScaleTransition bounce = new ScaleTransition(Duration.millis(500), resultatEmoji);
        bounce.setToX(1.2); bounce.setToY(1.2);
        bounce.setInterpolator(Interpolator.SPLINE(0.34, 0.97, 0.64, 1.0));
        ScaleTransition settle = new ScaleTransition(Duration.millis(200), resultatEmoji);
        settle.setToX(1.0); settle.setToY(1.0);
        PauseTransition gap = new PauseTransition(Duration.millis(300));
        gap.setOnFinished(e -> {
            bounce.setOnFinished(ev -> settle.play());
            bounce.play();
        });
        gap.play();
    }

    /** Pluie de confettis sur le Canvas de résultat. */
    private void lancerConfetti(double pct) {
        if (confettiCanvas == null) return;
        Platform.runLater(() -> {
            double W = Math.max(940, screenResultat.getWidth());
            double H = Math.max(720, screenResultat.getHeight());
            confettiCanvas.setWidth(W);
            confettiCanvas.setHeight(H);

            int n = pct >= 70 ? 130 : pct >= 50 ? 70 : 30;
            Random rand = new Random();
            String[] COLORS = {
                "#34D399","#7C6FCD","#6B5EBC","#1D9E75",
                "#F59E0B","#EC4899","#60A5FA","#A78BFA"
            };

            double[] px = new double[n], py = new double[n];
            double[] pvx = new double[n], pvy = new double[n];
            double[] prot = new double[n], prs = new double[n];
            double[] pw = new double[n], ph = new double[n];
            double[] palpha = new double[n];
            int[]    pci   = new int[n];

            for (int i = 0; i < n; i++) {
                px[i]    = rand.nextDouble() * W;
                py[i]    = -20 - rand.nextDouble() * 500;
                pvx[i]   = (rand.nextDouble() - 0.5) * 5;
                pvy[i]   = 2.5 + rand.nextDouble() * 4.5;
                prot[i]  = rand.nextDouble() * 360;
                prs[i]   = (rand.nextDouble() - 0.5) * 14;
                pw[i]    = 7  + rand.nextDouble() * 11;
                ph[i]    = 4  + rand.nextDouble() * 6;
                palpha[i] = 0.85 + rand.nextDouble() * 0.15;
                pci[i]   = rand.nextInt(COLORS.length);
            }

            long[] startNano = {0};
            new AnimationTimer() {
                @Override public void handle(long now) {
                    if (startNano[0] == 0) startNano[0] = now;
                    double elapsed = (now - startNano[0]) / 1_000_000_000.0;

                    GraphicsContext gc = confettiCanvas.getGraphicsContext2D();
                    gc.clearRect(0, 0, W, H);

                    boolean any = false;
                    for (int i = 0; i < n; i++) {
                        px[i] += pvx[i];
                        py[i] += pvy[i];
                        prot[i] += prs[i];
                        pvy[i]  += 0.11;  // gravité
                        if (elapsed > 2.5) palpha[i] = Math.max(0, palpha[i] - 0.016);

                        if (palpha[i] > 0.01 && py[i] < H + 30) {
                            any = true;
                            gc.save();
                            gc.setGlobalAlpha(palpha[i]);
                            gc.setFill(Color.web(COLORS[pci[i]]));
                            gc.translate(px[i], py[i]);
                            gc.rotate(prot[i]);
                            gc.fillRect(-pw[i] / 2, -ph[i] / 2, pw[i], ph[i]);
                            gc.restore();
                        }
                    }
                    if (!any || elapsed > 7) {
                        gc.clearRect(0, 0, W, H);
                        stop();
                    }
                }
            }.start();
        });
    }

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

    private String nvl(String s) { return s != null ? s : ""; }

    private void resetEtat() {
        indexQuestion      = 0;
        score              = 0;
        reponsesResultat.clear();
        questionRecords.clear();
        questionStartTime  = 0;
        reponseVerrouillee = false;
        arreterTimer();
        // Clear confetti canvas
        if (confettiCanvas != null) {
            confettiCanvas.getGraphicsContext2D()
                    .clearRect(0, 0, confettiCanvas.getWidth(), confettiCanvas.getHeight());
        }
    }
}
