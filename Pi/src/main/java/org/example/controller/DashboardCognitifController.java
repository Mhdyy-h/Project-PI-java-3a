package org.example.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.DatabaseConnection;
import org.example.model.ProfilCognitif;
import org.example.model.QuizSession;
import org.example.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.service.AdaptiveService;
import org.example.service.PredictionService;
import org.example.service.ServiceQuizSession;
import org.example.util.SessionContext;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Contrôleur du tableau de bord cognitif ({@code DashboardCognitif.fxml}).
 *
 * <p>Charge le profil de l'utilisateur connecté, affiche ses statistiques ELO,
 * l'historique des sessions et la prédiction de progression.</p>
 */
public class DashboardCognitifController {

    private static final Logger log = LoggerFactory.getLogger(DashboardCognitifController.class);
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");

    // ── FXML ────────────────────────────────────────────────────
    @FXML private Label  labelTitreDashboard;
    @FXML private Label  labelSousTitre;
    @FXML private Label  labelNomUtilisateur;
    @FXML private Label  labelNiveauSidebar;

    @FXML private Label  labelElo;
    @FXML private Label  labelTauxReussite;
    @FXML private Label  labelSessionsTotales;
    @FXML private Label  labelNiveau;
    @FXML private Label  labelThemeFort;
    @FXML private Label  labelThemeFaible;
    @FXML private Label  labelTempsReponse;

    @FXML private Label  labelPrediction;
    @FXML private Label  labelPredictionDetail;

    @FXML private VBox   conteneurHistorique;
    @FXML private Label  labelAucuneSession;

    // ── Services ────────────────────────────────────────────────
    private final AdaptiveService       adaptiveSvc       = new AdaptiveService();
    private final ServiceQuizSession    sessionSvc        = new ServiceQuizSession();
    private final PredictionService     predictionSvc     = new PredictionService();

    private ProfilCognitif profil;
    private User currentUser;

    // ════════════════════════════════════════════════════════════════
    // Initialisation
    // ════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        if (!SessionContext.estConnecte()) {
            // Fallback : charger le premier utilisateur disponible
            currentUser = chargerPremierUtilisateur();
            if (currentUser == null) {
                labelSousTitre.setText("Aucun utilisateur connecté.");
                return;
            }
            SessionContext.connecter(currentUser);
        } else {
            currentUser = SessionContext.getUtilisateur();
        }

        int userId = currentUser.getId();
        String nomComplet = currentUser.getNomComplet() != null ? currentUser.getNomComplet() : "";

        labelNomUtilisateur.setText(nomComplet);
        labelTitreDashboard.setText("Tableau de bord cognitif");
        labelSousTitre.setText("Bienvenue, " + nomComplet + " !");

        // Chargement en arrière-plan pour ne pas bloquer l'UI
        Thread t = new Thread(() -> {
            ProfilCognitif p = adaptiveSvc.getOuCreerProfil(userId);
            List<QuizSession> sessions = sessionSvc.getDernieres(userId, 8);
            PredictionService.ResultatPrediction pred = predictionSvc.predireProgression(userId, 3);
            String[] themes = adaptiveSvc.analyserThemes(userId);

            Platform.runLater(() -> {
                profil = p;
                afficherProfil(p, themes);
                afficherHistorique(sessions);
                afficherPrediction(pred);
            });
        });
        t.setDaemon(true);
        t.start();
    }

    // ════════════════════════════════════════════════════════════════
    // Affichage
    // ════════════════════════════════════════════════════════════════

    private void afficherProfil(ProfilCognitif p, String[] themes) {
        labelElo.setText(String.valueOf(p.getScoreElo()));
        labelTauxReussite.setText(String.format("%.0f%%", p.getTauxReussite()));
        labelSessionsTotales.setText(String.valueOf(p.getSessionsTotales()));
        labelNiveau.setText(p.getNiveauActuel());
        labelNiveauSidebar.setText(p.getNiveauActuel());

        labelThemeFort.setText(themes[0] != null ? themes[0] : "—");
        labelThemeFaible.setText(themes[1] != null ? themes[1] : "—");

        String tempsMs = p.getTempsReponseMoyenMs() > 0
                ? String.format("%.1f s", p.getTempsReponseMoyenMs() / 1000.0)
                : "—";
        labelTempsReponse.setText(tempsMs);

        // Coloration du niveau
        String couleurNiveau = switch (p.getNiveauActuel()) {
            case "AVANCE"        -> "#7c3aed";
            case "INTERMEDIAIRE" -> "#2563eb";
            default               -> "#16a34a";
        };
        labelNiveau.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + couleurNiveau + ";");
        labelNiveauSidebar.setStyle("-fx-font-size: 11px; -fx-text-fill: " + couleurNiveau + "; -fx-font-weight: bold;");
    }

    private void afficherHistorique(List<QuizSession> sessions) {
        conteneurHistorique.getChildren().clear();

        if (sessions.isEmpty()) {
            labelAucuneSession.setVisible(true);
            labelAucuneSession.setManaged(true);
            return;
        }

        for (QuizSession s : sessions) {
            HBox ligne = creerLigneSession(s);
            conteneurHistorique.getChildren().add(ligne);
        }
    }

    private HBox creerLigneSession(QuizSession s) {
        HBox row = new HBox();
        row.setStyle("-fx-padding: 10 16; -fx-background-radius: 8;");
        row.setOnMouseEntered(e -> row.setStyle("-fx-padding: 10 16; -fx-background-color: #f9fafb; -fx-background-radius: 8;"));
        row.setOnMouseExited(e  -> row.setStyle("-fx-padding: 10 16; -fx-background-radius: 8;"));

        String theme = s.getTheme() != null ? s.getTheme() : "—";
        String score = String.format("%.0f%%", s.getScoreFinal());
        String couleurScore = s.getScoreFinal() >= 70 ? "#16a34a" : s.getScoreFinal() >= 50 ? "#d97706" : "#dc2626";

        Label lTheme = creerCell(theme, "ALWAYS", "#374151", false);
        Label lScore = label(score, couleurScore, true, 80);
        Label lElo   = label(String.valueOf(s.getScoreElo()), "#7c3aed", false, 70);
        Label lFat   = label(String.format("%.0f", s.getScoreFatigue()), "#6b7280", false, 70);
        Label lDate  = label(s.getDateDebut() != null ? s.getDateDebut().format(DTF) : "—", "#9ca3af", false, 100);

        row.getChildren().addAll(lTheme, lScore, lElo, lFat, lDate);
        return row;
    }

    private void afficherPrediction(PredictionService.ResultatPrediction pred) {
        if (pred == null) {
            labelPrediction.setText("—");
            labelPredictionDetail.setText("Complétez au moins 2 sessions pour activer la prédiction.");
            return;
        }
        labelPrediction.setText(String.format("%.0f%%", pred.getScorePredit()));
        String tendance = pred.getProgression() >= 0
                ? String.format("+%.1f pts — %s", pred.getProgression(), pred.getNiveauConfiance())
                : String.format("%.1f pts — %s", pred.getProgression(), pred.getNiveauConfiance());
        labelPredictionDetail.setText(tendance + " (R²=" + String.format("%.2f", pred.getR2()) + ")");
    }

    // ════════════════════════════════════════════════════════════════
    // Navigation
    // ════════════════════════════════════════════════════════════════

    @FXML
    public void ouvrirChoixQuiz() {
        lancerQuiz();
    }

    @FXML
    public void lancerQuiz() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/QuizView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) labelElo.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            log.error("Erreur ouverture QuizView : {}", e.getMessage());
        }
    }

    @FXML
    public void ouvrirMultijoueur() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/SalonMultijoueur.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) labelElo.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            log.error("Erreur ouverture SalonMultijoueur : {}", e.getMessage());
        }
    }

    @FXML
    public void retourMenu() {
        try {
            boolean isCoach = currentUser != null && currentUser.getRoles() != null
                && currentUser.getRoles().contains("COACH");
            String fxml = isCoach ? "/view/MenuCoach.fxml" : "/view/MenuUser.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Object ctrl = loader.getController();
            if (ctrl instanceof MenuUserController) {
                ((MenuUserController) ctrl).setCurrentUser(currentUser);
            } else if (ctrl instanceof MenuCoachController) {
                ((MenuCoachController) ctrl).setCurrentUser(currentUser);
            }
            Stage stage = (Stage) labelElo.getScene().getWindow();
            Scene scene = new Scene(root);
            java.net.URL css = getClass().getResource("/style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            log.error("Erreur retour menu : {}", e.getMessage());
        }
    }

    /** Charge le premier utilisateur de la BD comme fallback. */
    private User chargerPremierUtilisateur() {
        try (Connection cn = DatabaseConnection.getConnection();
             Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT id, nom_complet, email, roles FROM utilisateur ORDER BY id LIMIT 1")) {
            if (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setNomComplet(rs.getString("nom_complet"));
                u.setEmail(rs.getString("email"));
                u.setRoles(rs.getString("roles"));
                return u;
            }
        } catch (Exception e) {
            log.error("Erreur chargement utilisateur fallback : {}", e.getMessage());
        }
        return null;
    }

    private void naviguerVers(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) labelElo.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            log.error("Erreur navigation vers {} : {}", fxml, e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // Helpers UI
    // ════════════════════════════════════════════════════════════════

    private Label creerCell(String texte, String hgrow, String couleur, boolean bold) {
        Label l = new Label(texte);
        l.setStyle("-fx-font-size: 13px; -fx-text-fill: " + couleur + ";"
                + (bold ? " -fx-font-weight: bold;" : ""));
        HBox.setHgrow(l, javafx.scene.layout.Priority.ALWAYS);
        return l;
    }

    private Label label(String texte, String couleur, boolean bold, double minWidth) {
        Label l = new Label(texte);
        l.setMinWidth(minWidth);
        l.setStyle("-fx-font-size: 13px; -fx-text-fill: " + couleur + ";"
                + (bold ? " -fx-font-weight: bold;" : ""));
        return l;
    }
}
