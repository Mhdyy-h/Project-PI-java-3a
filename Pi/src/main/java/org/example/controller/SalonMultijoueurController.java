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
import org.example.model.SessionMultijoueur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.service.MultiplayerService;
import org.example.service.QuestionGenService;
import org.example.util.ConfigLoader;
import org.example.util.SessionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Contrôleur du salon multijoueur.
 * Implémente {@link MultiplayerService.MultiplayerListener} pour les callbacks WebSocket.
 * Gère 4 vues empilées : Lobby → Salle d'attente → Jeu → Fin.
 */
public class SalonMultijoueurController implements MultiplayerService.MultiplayerListener {

    private static final Logger log = LoggerFactory.getLogger(SalonMultijoueurController.class);

    private static final int    NB_QUESTIONS   = 10;
    private static final int    TEMPS_PAR_Q    = 30;
    private static final String THEME_DEFAUT   = "Informatique";

    // ── Vues ────────────────────────────────────────────────────
    @FXML private VBox vueLobby;
    @FXML private VBox vueSalle;
    @FXML private VBox vueJeu;
    @FXML private VBox vueFin;

    // ── Lobby ────────────────────────────────────────────────────
    @FXML private TextField champCode;
    @FXML private Label     labelErreurLobby;
    @FXML private Label     labelStatutConnexion;

    // ── Salle d'attente ──────────────────────────────────────────
    @FXML private Label  labelCode;
    @FXML private Label  labelNbJoueurs;
    @FXML private VBox   listeJoueurs;
    @FXML private Button btnDemarrer;

    // ── Jeu ──────────────────────────────────────────────────────
    @FXML private Label labelThemeJeu;
    @FXML private Label labelTimerJeu;
    @FXML private Label labelNumeroQuestion;
    @FXML private Label labelEnonceJeu;
    @FXML private VBox  conteneurOptionsJeu;
    @FXML private Label labelFeedbackJeu;
    @FXML private VBox  tableauScores;

    // ── Fin ──────────────────────────────────────────────────────
    @FXML private VBox podium;

    // ── État ─────────────────────────────────────────────────────
    private MultiplayerService mpService;
    private SessionMultijoueur salon;
    private boolean            estHote       = false;
    private int                questionIndex = 0;
    private QuestionAdaptive           questionCourante;
    private long               debutQuestion;

    private final QuestionGenService questionSvc = new QuestionGenService();
    private final List<String>       joueursConnectes = new ArrayList<>();

    // ════════════════════════════════════════════════════════════════
    // MultiplayerListener — callbacks appelés depuis le service
    // ════════════════════════════════════════════════════════════════

    @Override
    public void onJoueurRejoint(String nom, int total) {
        if (!joueursConnectes.contains(nom)) joueursConnectes.add(nom);
        mettreAJourListeJoueurs(joueursConnectes);
    }

    @Override
    public void onQuestionRecue(QuestionAdaptive q, int tempsRestantSec) {
        questionCourante = q;
        debutQuestion = System.currentTimeMillis();
        questionIndex++;
        afficherQuestionJeu(q, tempsRestantSec);
    }

    @Override
    public void onScoresMAJ(List<MultiplayerService.ScoreEntry> classement) {
        mettreAJourScores(classement);
    }

    @Override
    public void onPartieTerminee(List<MultiplayerService.ScoreEntry> classementFinal) {
        afficherFin(classementFinal);
    }

    @Override
    public void onErreur(String message) {
        afficherErreurLobby(message);
        log.error("Erreur multijoueur : {}", message);
    }

    // ════════════════════════════════════════════════════════════════
    // Actions Lobby
    // ════════════════════════════════════════════════════════════════

    @FXML
    public void creerPartie() {
        int userId = SessionContext.getUtilisateurId();

        mpService = new MultiplayerService();
        mpService.setListener(this);
        estHote   = true;

        salon = mpService.creerSalon(userId, THEME_DEFAUT, NB_QUESTIONS, TEMPS_PAR_Q);
        joueursConnectes.clear();
        joueursConnectes.add(SessionContext.getUtilisateur().getNomComplet());

        try {
            mpService.demarrerServeur();
            labelCode.setText(salon.getCodeSalon());
            labelStatutConnexion.setText("En ligne • Hôte");
            labelStatutConnexion.setStyle("-fx-font-size: 12px; -fx-text-fill: #16a34a;");
            btnDemarrer.setVisible(true);
            btnDemarrer.setManaged(true);
            mettreAJourListeJoueurs(joueursConnectes);
            afficherVue(vueSalle);
        } catch (Exception e) {
            log.error("Impossible de créer la partie : {}", e.getMessage());
            afficherErreurLobby("Impossible de créer la partie. Réessayez.");
        }
    }

    @FXML
    public void rejoindrePartie() {
        String code = champCode.getText().trim().toUpperCase();
        if (code.length() < 4) {
            afficherErreurLobby("Code invalide (minimum 4 caractères).");
            return;
        }

        int    userId    = SessionContext.getUtilisateurId();
        String nomJoueur = SessionContext.getUtilisateur().getNomComplet();

        mpService = new MultiplayerService();
        mpService.setListener(this);
        estHote   = false;

        int port = ConfigLoader.websocketPort();
        String url = "ws://localhost:" + port;

        Thread t = new Thread(() -> {
            try {
                mpService.rejoindre(url, nomJoueur, userId);
                Platform.runLater(() -> {
                    labelCode.setText(code);
                    labelStatutConnexion.setText("En ligne • Joueur");
                    labelStatutConnexion.setStyle("-fx-font-size: 12px; -fx-text-fill: #2563eb;");
                    btnDemarrer.setVisible(false);
                    btnDemarrer.setManaged(false);
                    afficherVue(vueSalle);
                });
            } catch (Exception e) {
                log.error("Impossible de rejoindre : {}", e.getMessage());
                Platform.runLater(() -> afficherErreurLobby("Impossible de rejoindre. Vérifiez le code."));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    // ════════════════════════════════════════════════════════════════
    // Actions Salle d'attente
    // ════════════════════════════════════════════════════════════════

    @FXML
    public void demarrerPartie() {
        if (!estHote) return;
        btnDemarrer.setDisable(true);
        btnDemarrer.setText("Préparation...");

        Thread t = new Thread(() -> {
            List<QuestionAdaptive> questions = new ArrayList<>();
            for (int i = 0; i < NB_QUESTIONS; i++) {
                questions.add(questionSvc.generer(THEME_DEFAUT, 5, "QCM"));
            }
            Platform.runLater(() -> {
                afficherVue(vueJeu);
                labelThemeJeu.setText(THEME_DEFAUT);
                mpService.demarrerPartie(questions);
            });
        });
        t.setDaemon(true);
        t.start();
    }

    @FXML
    public void quitterSalle() {
        arreterService();
        joueursConnectes.clear();
        questionIndex = 0;
        afficherVue(vueLobby);
        labelStatutConnexion.setText("Hors ligne");
        labelStatutConnexion.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af;");
    }

    // ════════════════════════════════════════════════════════════════
    // Jeu
    // ════════════════════════════════════════════════════════════════

    private void afficherQuestionJeu(QuestionAdaptive q, int tempsSec) {
        labelNumeroQuestion.setText("Question " + questionIndex + " / " + NB_QUESTIONS);
        labelEnonceJeu.setText(q.getEnonce());
        labelFeedbackJeu.setText("");
        labelTimerJeu.setText(String.valueOf(tempsSec));
        conteneurOptionsJeu.getChildren().clear();

        List<String> options = q.getOptions();
        for (String opt : options) {
            Button btn = new Button(opt);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 10; " +
                    "-fx-padding: 12 20; -fx-font-size: 13px; -fx-cursor: hand; " +
                    "-fx-border-color: #e5e7eb; -fx-border-radius: 10;");
            btn.setOnAction(e -> soumettreReponse(opt, q, btn));
            conteneurOptionsJeu.getChildren().add(btn);
        }
    }

    private void soumettreReponse(String reponse, QuestionAdaptive q, Button btnClique) {
        conteneurOptionsJeu.getChildren().forEach(n -> n.setDisable(true));

        boolean correct = q.estReponseCorrecte(reponse);
        int tempsMs = (int) (System.currentTimeMillis() - debutQuestion);

        if (correct) {
            btnClique.setStyle("-fx-background-color: #dcfce7; -fx-background-radius: 10; " +
                    "-fx-padding: 12 20; -fx-font-size: 13px; -fx-border-color: #16a34a; -fx-border-radius: 10;");
            labelFeedbackJeu.setText("✓ Correct !");
            labelFeedbackJeu.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #16a34a;");
        } else {
            btnClique.setStyle("-fx-background-color: #fee2e2; -fx-background-radius: 10; " +
                    "-fx-padding: 12 20; -fx-font-size: 13px; -fx-border-color: #dc2626; -fx-border-radius: 10;");
            labelFeedbackJeu.setText("✗ Incorrect");
            labelFeedbackJeu.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dc2626;");
        }

        mpService.envoyerReponse(q.getId(), reponse, tempsMs, SessionContext.getUtilisateurId());
    }

    private void mettreAJourScores(List<MultiplayerService.ScoreEntry> classement) {
        tableauScores.getChildren().clear();
        String[] medailles = {"🥇", "🥈", "🥉"};

        for (int i = 0; i < classement.size(); i++) {
            MultiplayerService.ScoreEntry entry = classement.get(i);
            HBox ligne = new HBox();
            ligne.setSpacing(8);
            ligne.setStyle("-fx-padding: 8 12; -fx-background-radius: 8; -fx-background-color: " +
                    (i == 0 ? "#fef3c7" : "#f9fafb") + ";");

            Label lMed = new Label(i < 3 ? medailles[i] : (i + 1) + ".");
            lMed.setStyle("-fx-font-size: 14px;");

            Label lNom = new Label(entry.getNom());
            lNom.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");
            HBox.setHgrow(lNom, Priority.ALWAYS);

            Label lPts = new Label(entry.getScore() + " pts");
            lPts.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #7c3aed;");

            ligne.getChildren().addAll(lMed, lNom, lPts);
            tableauScores.getChildren().add(ligne);
        }
    }

    private void mettreAJourListeJoueurs(List<String> joueurs) {
        listeJoueurs.getChildren().clear();
        labelNbJoueurs.setText("(" + joueurs.size() + ")");

        for (String nom : joueurs) {
            HBox row = new HBox();
            row.setSpacing(10);
            row.setStyle("-fx-padding: 8 12; -fx-background-color: #f9fafb; -fx-background-radius: 8;");

            Label dot = new Label("●");
            dot.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 10px;");
            Label lNom = new Label(nom);
            lNom.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

            row.getChildren().addAll(dot, lNom);
            listeJoueurs.getChildren().add(row);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // Fin de partie
    // ════════════════════════════════════════════════════════════════

    private void afficherFin(List<MultiplayerService.ScoreEntry> classement) {
        afficherVue(vueFin);
        podium.getChildren().clear();

        String[] medailles = {"🥇", "🥈", "🥉"};
        for (int i = 0; i < classement.size(); i++) {
            MultiplayerService.ScoreEntry entry = classement.get(i);
            HBox ligne = new HBox();
            ligne.setSpacing(12);
            ligne.setStyle("-fx-padding: 10 16; -fx-background-color: " +
                    (i == 0 ? "#fef3c7" : "#f9fafb") + "; -fx-background-radius: 10;");
            VBox.setMargin(ligne, new Insets(4, 0, 4, 0));

            Label lMed = new Label(i < 3 ? medailles[i] : (i + 1) + ".");
            lMed.setStyle("-fx-font-size: 20px;");

            Label lNom = new Label(entry.getNom());
            lNom.setStyle("-fx-font-size: 15px; -fx-text-fill: #374151;");
            HBox.setHgrow(lNom, Priority.ALWAYS);

            Label lPts = new Label(entry.getScore() + " pts");
            lPts.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #7c3aed;");

            ligne.getChildren().addAll(lMed, lNom, lPts);
            podium.getChildren().add(ligne);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // Actions fin
    // ════════════════════════════════════════════════════════════════

    @FXML
    public void rejouer() {
        arreterService();
        joueursConnectes.clear();
        questionIndex = 0;
        btnDemarrer.setDisable(false);
        btnDemarrer.setText("🚀  Démarrer la partie");
        afficherVue(vueLobby);
    }

    @FXML
    public void retourDashboard() {
        arreterService();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/DashboardCognitif.fxml"));
            Stage stage = (Stage) vueLobby.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            log.error("Erreur navigation dashboard : {}", e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // Utilitaires
    // ════════════════════════════════════════════════════════════════

    private void afficherVue(VBox vue) {
        for (VBox v : List.of(vueLobby, vueSalle, vueJeu, vueFin)) {
            boolean actif = v == vue;
            v.setVisible(actif);
            v.setManaged(actif);
        }
    }

    private void afficherErreurLobby(String msg) {
        labelErreurLobby.setText(msg);
        labelErreurLobby.setVisible(true);
        labelErreurLobby.setManaged(true);
    }

    private void arreterService() {
        if (mpService != null) {
            if (estHote) {
                try { mpService.arreterServeur(); } catch (Exception ignored) {}
            } else {
                try { mpService.deconnecter(); } catch (Exception ignored) {}
            }
            mpService = null;
        }
    }
}
