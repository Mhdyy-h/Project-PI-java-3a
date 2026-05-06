package org.example.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.service.*;
import org.example.util.ConfigLoader;
import org.example.util.SessionContext;

import java.util.*;

/**
 * Contrôleur de l'écran de quiz ({@code QuizView.fxml}).
 *
 * <h3>Cycle de vie d'une question</h3>
 * <pre>
 *   chargerQuestion() → afficherQuestion()
 *         ↓ (utilisateur clique sur une option ou timer expire)
 *   traiterReponse(texte, tempsMs)
 *         ↓
 *   FatigueService.evaluer()  +  AnomalyService.analyser()
 *   AdaptiveService.mettreAJourElo()
 *   AIAssistantService.expliquer()
 *         ↓
 *   afficherFeedback()
 *         ↓ (bouton "Suivant" ou timer feedback)
 *   questionSuivante() → (si fin) terminerSession()
 * </pre>
 */
public class QuizViewController {

    private static final Logger log = LoggerFactory.getLogger(QuizViewController.class);

    // ── FXML — top bar ───────────────────────────────────────────
    @FXML private Label       labelProgression;
    @FXML private ProgressBar barreProgression;
    @FXML private Label       labelTheme;
    @FXML private Label       labelFatigue;
    @FXML private Label       labelTimer;
    @FXML private ProgressBar barreTimer;

    // ── FXML — vue question ──────────────────────────────────────
    @FXML private VBox        vueQuestion;
    @FXML private Label       labelDifficulte;
    @FXML private Label       labelTypeQuestion;
    @FXML private Label       labelScoreCourant;
    @FXML private Label       labelEnonce;
    @FXML private VBox        conteneurOptions;

    // ── FXML — vue feedback ──────────────────────────────────────
    @FXML private VBox        vueFeedback;
    @FXML private Label       iconeFeedback;
    @FXML private Label       labelFeedbackTitre;
    @FXML private Label       labelReponseCorrecte;
    @FXML private Label       labelExplication;
    @FXML private VBox        alerteFatigue;
    @FXML private Label       labelAlerteFatigue;
    @FXML private Button      btnSuivant;

    // ── FXML — vue chargement ────────────────────────────────────
    @FXML private VBox        vueChargement;

    // ── FXML — footer ────────────────────────────────────────────
    @FXML private Label       labelEloCourant;
    @FXML private Label       labelNbCorrectes;
    @FXML private Label       labelTempsMoyen;

    // ── Services ─────────────────────────────────────────────────
    private final QuestionGenService  questionGenSvc  = new QuestionGenService();
    private final AdaptiveService     adaptiveSvc     = new AdaptiveService();
    private final FatigueService      fatigueSvc      = new FatigueService();
    private final AnomalyService      anomalySvc      = new AnomalyService();
    private final AIAssistantService  assistantSvc    = new AIAssistantService();
    private final ServiceQuizSession  sessionSvc      = new ServiceQuizSession();

    // ── État de la session ────────────────────────────────────────
    private QuizSession         session;
    private ProfilCognitif      profil;
    private QuestionAdaptive            questionCourante;
    private final List<ReponseUtilisateur>  reponses     = new ArrayList<>();
    private final Map<Integer, QuestionAdaptive>    questionsMap = new HashMap<>();

    // ── Timer ─────────────────────────────────────────────────────
    private Timeline      timerTimeline;
    private int           tempsRestant;
    private long          tempsAffichageMs;

    // ── Config ────────────────────────────────────────────────────
    private String  theme          = "Informatique";
    private int     nombreQuestions = 10;
    private int     tempsParQuestion = 30;

    private int     nbCorrectes   = 0;
    private int     eloAvantSession;

    // ════════════════════════════════════════════════════════════════
    // Initialisation
    // ════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        if (!SessionContext.estConnecte()) {
            naviguerVers("/Connexion.fxml");
            return;
        }

        int userId = SessionContext.getUtilisateurId();
        profil = adaptiveSvc.getOuCreerProfil(userId);
        eloAvantSession = profil.getScoreElo();

        session = new QuizSession(userId, theme, adaptiveSvc.elToNiveau(profil.getScoreElo()));
        session.setNombreQuestions(nombreQuestions);
        sessionSvc.demarrer(session);

        labelTheme.setText(theme);
        labelEloCourant.setText(String.valueOf(profil.getScoreElo()));
        mettreAJourFooter();

        chargerProchainQuestion();
    }

    /**
     * Appelé depuis {@code DashboardCognitifController} pour paramétrer le quiz.
     *
     * @param theme          thème du quiz
     * @param nombre         nombre de questions
     * @param tempsSec       temps par question en secondes
     */
    public void configurerSession(String theme, int nombre, int tempsSec) {
        this.theme          = theme;
        this.nombreQuestions = nombre;
        this.tempsParQuestion = tempsSec;
    }

    // ════════════════════════════════════════════════════════════════
    // Cycle de vie des questions
    // ════════════════════════════════════════════════════════════════

    private void chargerProchainQuestion() {
        if (reponses.size() >= nombreQuestions) {
            terminerSession();
            return;
        }

        afficherVue(vueChargement);

        Thread t = new Thread(() -> {
            QuestionAdaptive q = adaptiveSvc.prochaineQuestion(profil, theme, session.getId());
            if (q == null) q = questionGenSvc.generer(theme, adaptiveSvc.elToNiveau(profil.getScoreElo()), "QCM");

            final QuestionAdaptive questionFinale = q;
            Platform.runLater(() -> {
                questionCourante = questionFinale;
                questionsMap.put(questionFinale.getId(), questionFinale);
                afficherQuestion(questionFinale);
                demarrerTimer();
            });
        });
        t.setDaemon(true);
        t.start();
    }

    private void afficherQuestion(QuestionAdaptive q) {
        int numero = reponses.size() + 1;
        labelProgression.setText("Question " + numero + " / " + nombreQuestions);
        barreProgression.setProgress((double)(numero - 1) / nombreQuestions);
        labelDifficulte.setText("Niveau " + q.getNiveauDifficulte());
        labelTypeQuestion.setText(q.getType());
        labelEnonce.setText(q.getEnonce());
        labelScoreCourant.setText("Score : " + String.format("%.0f%%",
                reponses.isEmpty() ? 0 : nbCorrectes * 100.0 / reponses.size()));

        conteneurOptions.getChildren().clear();

        if ("VRAI_FAUX".equals(q.getType())) {
            creerBoutonOption("Vrai", q);
            creerBoutonOption("Faux", q);
        } else if (q.getOptions() != null && !q.getOptions().isEmpty()) {
            q.getOptions().forEach(opt -> creerBoutonOption(opt, q));
        } else {
            // QuestionAdaptive ouverte : champ texte
            TextField saisie = new TextField();
            saisie.setPromptText("Votre réponse...");
            saisie.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; "
                    + "-fx-border-color: #d1d5db; -fx-padding: 12 16; -fx-font-size: 14px;");
            Button valider = new Button("Valider →");
            valider.setStyle("-fx-background-color: #1a0f4f; -fx-text-fill: white; "
                    + "-fx-background-radius: 10; -fx-padding: 10 24; -fx-cursor: hand;");
            valider.setOnAction(e -> traiterReponse(saisie.getText().trim()));
            saisie.setOnAction(e -> traiterReponse(saisie.getText().trim()));
            conteneurOptions.getChildren().addAll(saisie, valider);
        }

        tempsAffichageMs = System.currentTimeMillis();
        afficherVue(vueQuestion);
    }

    private void creerBoutonOption(String texte, QuestionAdaptive question) {
        Button btn = new Button(texte);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: white; "
                + "-fx-border-color: #d1d5db; -fx-border-radius: 10; -fx-background-radius: 10; "
                + "-fx-padding: 14 20; -fx-font-size: 14px; -fx-text-fill: #111827; "
                + "-fx-cursor: hand; -fx-alignment: CENTER_LEFT;");
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle().replace("#d1d5db", "#1a0f4f")));
        btn.setOnMouseExited(e  -> btn.setStyle(btn.getStyle().replace("#1a0f4f", "#d1d5db")));
        btn.setOnAction(e -> {
            conteneurOptions.getChildren().forEach(node -> node.setDisable(true));
            traiterReponse(texte);
        });
        conteneurOptions.getChildren().add(btn);
    }

    private void traiterReponse(String reponseDonnee) {
        arreterTimer();

        int tempsMs = (int)(System.currentTimeMillis() - tempsAffichageMs);
        boolean estCorrecte = questionCourante.estReponseCorrecte(reponseDonnee);

        if (estCorrecte) nbCorrectes++;

        ReponseUtilisateur reponse = new ReponseUtilisateur(
                session.getId(), questionCourante.getId(),
                reponseDonnee, estCorrecte, tempsMs, reponses.size() + 1);
        reponses.add(reponse);
        sessionSvc.enregistrerReponse(reponse);

        // Mise à jour ELO
        adaptiveSvc.mettreAJourElo(profil, questionCourante, estCorrecte);
        adaptiveSvc.sauvegarderProfil(profil);

        // Évaluation fatigue
        FatigueLog fatigueLog = fatigueSvc.evaluer(session.getId(),
                SessionContext.getUtilisateurId(), reponses.size(), reponses);
        fatigueSvc.enregistrer(fatigueLog);

        // Détection anomalies
        List<AnomalieEvent> anomalies = anomalySvc.analyser(
                session.getId(), SessionContext.getUtilisateurId(), reponses);
        anomalies.forEach(anomalySvc::enregistrer);

        // Explication IA (non bloquant)
        Thread t = new Thread(() -> {
            String explication = assistantSvc.expliquer(questionCourante, reponseDonnee, estCorrecte);
            Platform.runLater(() -> afficherFeedback(estCorrecte, reponseDonnee, explication, fatigueLog));
        });
        t.setDaemon(true);
        t.start();

        mettreAJourFooter();
    }

    private void afficherFeedback(boolean correct, String reponseDonnee,
                                   String explication, FatigueLog fatigueLog) {
        if (correct) {
            iconeFeedback.setText("✓");
            iconeFeedback.setStyle("-fx-font-size: 32px; -fx-text-fill: #16a34a;");
            labelFeedbackTitre.setText("Bonne réponse !");
            labelFeedbackTitre.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #16a34a;");
        } else {
            iconeFeedback.setText("✗");
            iconeFeedback.setStyle("-fx-font-size: 32px; -fx-text-fill: #dc2626;");
            labelFeedbackTitre.setText("Pas tout à fait...");
            labelFeedbackTitre.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #dc2626;");
        }

        labelReponseCorrecte.setText("Réponse correcte : " + questionCourante.getReponseCorrecte()
                + (correct ? "" : " (vous avez répondu : " + reponseDonnee + ")"));
        labelExplication.setText(explication != null ? explication : "");

        // Alerte fatigue
        if (fatigueLog.necessiteArret()) {
            afficherAlerteFatigue("⚠️ Votre niveau de fatigue est élevé. "
                    + "Il est recommandé de faire une pause.", "#dc2626");
        } else if (fatigueLog.necessitePause()) {
            afficherAlerteFatigue("💤 Fatigue détectée. "
                    + "Prenez une courte pause avant de continuer.", "#ea580c");
        } else {
            alerteFatigue.setVisible(false);
            alerteFatigue.setManaged(false);
        }

        // Indicateur fatigue dans la barre du haut
        double scoreFat = fatigueLog.getScoreFatigue();
        labelFatigue.setText(String.format("Fatigue : %.0f%%", scoreFat));
        String couleurFat = scoreFat >= 85 ? "#dc2626" : scoreFat >= 65 ? "#ea580c" : "#16a34a";
        labelFatigue.setStyle("-fx-font-size: 12px; -fx-text-fill: " + couleurFat + ";");

        afficherVue(vueFeedback);

        // Auto-avancement après 4 secondes si pas d'alerte
        if (!fatigueLog.necessiteArret() && !fatigueLog.necessitePause()) {
            Timeline auto = new Timeline(new KeyFrame(Duration.seconds(4.5),
                    e -> questionSuivante()));
            auto.play();
        }
    }

    private void afficherAlerteFatigue(String msg, String couleur) {
        labelAlerteFatigue.setText(msg);
        labelAlerteFatigue.setStyle("-fx-font-size: 13px; -fx-text-fill: " + couleur + ";");
        alerteFatigue.setVisible(true);
        alerteFatigue.setManaged(true);
    }

    @FXML
    public void questionSuivante() {
        chargerProchainQuestion();
    }

    // ════════════════════════════════════════════════════════════════
    // Timer
    // ════════════════════════════════════════════════════════════════

    private void demarrerTimer() {
        arreterTimer();
        tempsRestant = tempsParQuestion;
        labelTimer.setText(String.valueOf(tempsRestant));
        barreTimer.setProgress(1.0);

        timerTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            tempsRestant--;
            labelTimer.setText(String.valueOf(Math.max(0, tempsRestant)));
            barreTimer.setProgress((double) tempsRestant / tempsParQuestion);

            // Couleur rouge dans les 5 dernières secondes
            if (tempsRestant <= 5) {
                labelTimer.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
            }
            if (tempsRestant <= 0) {
                arreterTimer();
                traiterReponse(""); // réponse vide = temps écoulé
            }
        }));
        timerTimeline.setCycleCount(tempsParQuestion);
        timerTimeline.play();
    }

    private void arreterTimer() {
        if (timerTimeline != null) {
            timerTimeline.stop();
            timerTimeline = null;
        }
        labelTimer.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
    }

    // ════════════════════════════════════════════════════════════════
    // Fin de session
    // ════════════════════════════════════════════════════════════════

    private void terminerSession() {
        double scoreFinal = reponses.isEmpty() ? 0.0 : nbCorrectes * 100.0 / reponses.size();
        double scoreFatigue = fatigueSvc.scoreMoyenSession(session.getId());

        sessionSvc.terminer(session, scoreFinal, profil.getScoreElo(), scoreFatigue);
        adaptiveSvc.mettreAJourStats(profil, reponses, scoreFinal);
        adaptiveSvc.sauvegarderProfil(profil);
        predictionSvc.invaliderCache(SessionContext.getUtilisateurId());

        naviguerVersResultats();
    }

    private void naviguerVersResultats() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ResultatsSession.fxml"));
            Parent root = loader.load();
            ResultatsSessionController ctrl = loader.getController();
            ctrl.chargerSession(session, reponses, questionsMap, eloAvantSession);

            Stage stage = (Stage) labelProgression.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            log.error("Erreur navigation résultats : {}", e.getMessage());
        }
    }

    @FXML
    public void quitterSession() {
        arreterTimer();
        sessionSvc.abandonner(session.getId());
        naviguerVers("/DashboardCognitif.fxml");
    }

    // ════════════════════════════════════════════════════════════════
    // Helpers UI
    // ════════════════════════════════════════════════════════════════

    private void afficherVue(VBox vue) {
        for (VBox v : new VBox[]{vueQuestion, vueFeedback, vueChargement}) {
            v.setVisible(v == vue);
            v.setManaged(v == vue);
        }
    }

    private void mettreAJourFooter() {
        labelEloCourant.setText(String.valueOf(profil != null ? profil.getScoreElo() : 0));
        labelNbCorrectes.setText(String.valueOf(nbCorrectes));
        if (!reponses.isEmpty()) {
            int tempsMoy = (int) reponses.stream()
                    .mapToInt(ReponseUtilisateur::getTempsReponseMs).average().orElse(0);
            labelTempsMoyen.setText(String.format("%.1f s", tempsMoy / 1000.0));
        }
    }

    private void naviguerVers(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) labelProgression.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            log.error("Erreur navigation vers {} : {}", fxml, e.getMessage());
        }
    }

    // Référence lazy pour invalider le cache prédiction
    private final PredictionService predictionSvc = new PredictionService();
}
