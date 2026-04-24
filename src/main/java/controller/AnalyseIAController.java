package controller;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.SeanceSport;
import service.ServiceSeanceSport;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller pour la page d'Analyse IA
 * Affiche les statistiques de progression sportive calculées par l'IA
 */
public class AnalyseIAController implements Initializable {

    // ─── Header ───────────────────────────────────────────────────────────────
    @FXML private Label lblNomUtilisateur;

    // ─── Cartes statistiques ─────────────────────────────────────────────────
    @FXML private Label lblTotalSeances;
    @FXML private Label lblEvolutionSeances;
    @FXML private Label lblDureeMoyenne;
    @FXML private Label lblTendanceDuree;
    @FXML private Label lblScoreIA;
    @FXML private Label lblNiveauScore;
    @FXML private Label lblNiveauGlobal;
    @FXML private Label lblDescriptionNiveau;

    // ─── Analyse textuelle ───────────────────────────────────────────────────
    @FXML private Label lblAnalyseFrequence;
    @FXML private Label lblAnalyseIntensite;
    @FXML private Label lblAnalyseRegularite;

    // ─── Barres de progression ───────────────────────────────────────────────
    @FXML private ProgressBar barFrequence;
    @FXML private ProgressBar barIntensite;
    @FXML private ProgressBar barRegularite;
    @FXML private ProgressBar barGlobal;
    @FXML private Label lblScoreFrequence;
    @FXML private Label lblScoreIntensite;
    @FXML private Label lblScoreRegularite;
    @FXML private Label lblScoreGlobal2;

    // ─── Recommandations ─────────────────────────────────────────────────────
    @FXML private VBox recommendationsContainer;

    // ─── Historique ──────────────────────────────────────────────────────────
    @FXML private VBox seancesContainer;
    @FXML private VBox emptyState;
    @FXML private Label lblNbSeances;

    // ─── Données ─────────────────────────────────────────────────────────────
    private int utilisateurId;
    private String nomUtilisateur;
    private ServiceSeanceSport seanceService;          // ✅ nom correct
    private List<SeanceSport> seances;

    private double scoreFrequence = 0;
    private double scoreIntensite = 0;
    private double scoreRegularite = 0;
    private double scoreGlobal    = 0;

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        seanceService = new ServiceSeanceSport();      // ✅ nom correct
    }

    // ─── Injection des données depuis la fenêtre appelante ───────────────────

    public void setDonnees(int utilisateurId, String nomUtilisateur,
                           double scoreFrequence, double scoreIntensite,
                           double scoreRegularite, double scoreGlobal) {
        this.utilisateurId   = utilisateurId;
        this.nomUtilisateur  = nomUtilisateur;
        this.scoreFrequence  = scoreFrequence;
        this.scoreIntensite  = scoreIntensite;
        this.scoreRegularite = scoreRegularite;
        this.scoreGlobal     = scoreGlobal;

        lblNomUtilisateur.setText(nomUtilisateur);
        chargerDonnees();
    }

    public void setUtilisateur(int utilisateurId, String nomUtilisateur) {
        this.utilisateurId  = utilisateurId;
        this.nomUtilisateur = nomUtilisateur;
        lblNomUtilisateur.setText(nomUtilisateur);
        chargerDonnees();
    }

    // ─── Chargement et calcul ────────────────────────────────────────────────

    private void chargerDonnees() {
        try {
            seances = seanceService.rechercherParUtilisateur(utilisateurId); // ✅ méthode correcte
        } catch (Exception e) {
            seances = new java.util.ArrayList<>();
            System.err.println("Erreur chargement séances : " + e.getMessage());
        }
        calculerScoresSiNecessaire();
        afficherStatistiques();
        afficherBarres();
        afficherRecommandations();
        afficherHistorique();
        demarrerAnimations();
    }

    private void calculerScoresSiNecessaire() {
        if (scoreGlobal == 0 && !seances.isEmpty()) {
            int n = seances.size();

            scoreFrequence = Math.min(10.0, n / 2.0);

            double dureeMoyenne = seances.stream()
                    .mapToInt(SeanceSport::getDureeMinutes)   // ✅ getDureeMinutes()
                    .average().orElse(0);
            scoreIntensite = Math.min(10.0, dureeMoyenne / 9.0);

            scoreRegularite = n >= 8 ? 9.0 : n >= 5 ? 7.0 : n >= 3 ? 5.0 : 3.0;

            scoreGlobal = (scoreFrequence + scoreIntensite + scoreRegularite) / 3.0;
        }
    }

    // ─── Affichage statistiques ──────────────────────────────────────────────

    private void afficherStatistiques() {
        int n = seances.size();
        lblTotalSeances.setText(String.valueOf(n));
        lblEvolutionSeances.setText(n > 0 ? "✓ " + n + " séance(s) analysée(s)" : "Aucune séance");

        if (!seances.isEmpty()) {
            double dureeMoy = seances.stream()
                    .mapToInt(SeanceSport::getDureeMinutes)   // ✅ getDureeMinutes()
                    .average().orElse(0);
            lblDureeMoyenne.setText(String.format("%.0f", dureeMoy));
            lblTendanceDuree.setText(dureeMoy >= 60 ? "↑ Séances longues" : "→ Durée modérée");
        } else {
            lblDureeMoyenne.setText("0");
            lblTendanceDuree.setText("Aucune donnée");
        }

        lblScoreIA.setText(String.format("%.1f", scoreGlobal));
        lblNiveauScore.setText(getNiveauLabel(scoreGlobal));

        lblNiveauGlobal.setText(getNiveauGlobal(scoreGlobal));
        lblDescriptionNiveau.setText(getDescriptionNiveau(scoreGlobal));

        lblAnalyseFrequence.setText(getAnalyseFrequence(seances.size()));
        lblAnalyseIntensite.setText(getAnalyseIntensite(scoreIntensite));
        lblAnalyseRegularite.setText(getAnalyseRegularite(scoreRegularite));

        lblNbSeances.setText(n + " séance(s) • 30 derniers jours");
    }

    // ─── Barres de progression ───────────────────────────────────────────────

    private void afficherBarres() {
        lblScoreFrequence.setText(String.format("%.1f/10", scoreFrequence));
        lblScoreIntensite.setText(String.format("%.1f/10", scoreIntensite));
        lblScoreRegularite.setText(String.format("%.1f/10", scoreRegularite));
        lblScoreGlobal2.setText(String.format("%.1f/10", scoreGlobal));

        barFrequence.setProgress(0);
        barIntensite.setProgress(0);
        barRegularite.setProgress(0);
        barGlobal.setProgress(0);
    }

    // ─── Recommandations ─────────────────────────────────────────────────────

    private void afficherRecommandations() {
        recommendationsContainer.getChildren().clear();

        List<String[]> recs = getRecommandations(scoreGlobal, scoreFrequence,
                scoreIntensite, scoreRegularite, seances.size());

        for (int i = 0; i < recs.size(); i++) {
            String[] rec = recs.get(i);
            HBox card = creerCarteRecommandation(rec[0], rec[1], rec[2], i % 3);
            recommendationsContainer.getChildren().add(card);
        }
    }

    private HBox creerCarteRecommandation(String emoji, String titre, String desc, int colorIndex) {
        String[] couleurs    = {"#ede9fe", "#d1fae5", "#fef3c7"};
        String[] textCouleurs = {"#7c3aed", "#059669", "#d97706"};

        HBox card = new HBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: " + couleurs[colorIndex] + ";" +
                "-fx-background-radius: 12; -fx-padding: 14 18;");

        StackPane iconBox = new StackPane();
        Circle circle = new Circle(18);
        circle.setStyle("-fx-fill: white;");
        Label icon = new Label(emoji);
        icon.setStyle("-fx-font-size: 16px;");
        iconBox.getChildren().addAll(circle, icon);

        VBox texte = new VBox(3);
        Label lblTitre = new Label(titre);
        lblTitre.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + textCouleurs[colorIndex] + ";");
        Label lblDesc = new Label(desc);
        lblDesc.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280; -fx-wrap-text: true;");
        lblDesc.setMaxWidth(500);
        texte.getChildren().addAll(lblTitre, lblDesc);

        card.getChildren().addAll(iconBox, texte);
        return card;
    }

    // ─── Historique des séances ──────────────────────────────────────────────

    private void afficherHistorique() {
        seancesContainer.getChildren().clear();

        if (seances.isEmpty()) {
            emptyState.setVisible(true);
            return;
        }

        emptyState.setVisible(false);

        List<SeanceSport> recent = seances.stream()
                .limit(10)
                .toList();

        for (int i = 0; i < recent.size(); i++) {
            SeanceSport s = recent.get(i);
            HBox row = creerLigneSeance(s, i % 2 == 0);
            seancesContainer.getChildren().add(row);

            if (i < recent.size() - 1) {
                Separator sep = new Separator();
                sep.setStyle("-fx-opacity: 0.3;");
                seancesContainer.getChildren().add(sep);
            }
        }
    }

    private HBox creerLigneSeance(SeanceSport s, boolean isDefault) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 12 24; -fx-background-color: " +
                (isDefault ? "white" : "#fafbfd") + ";");

        // Date — getDateSeance() retourne String directement ✅
        Label date = new Label(s.getDateSeance() != null ? s.getDateSeance() : "—");
        date.setPrefWidth(150);
        date.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");

        // Nom — getNomSeance() ✅
        Label nom = new Label(s.getNomSeance() != null ? s.getNomSeance() : "Séance");
        nom.setPrefWidth(180);
        nom.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");

        // Durée — getDureeMinutes() ✅
        Label duree = new Label(s.getDureeMinutes() + " min");
        duree.setPrefWidth(120);
        duree.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        // Statut — medailleObtenue à la place de getStatut() ✅
        String statutTexte = (s.getMedailleObtenue() != null && !s.getMedailleObtenue().isEmpty())
                ? s.getMedailleObtenue() : "Complété";
        Label statut = new Label(statutTexte);
        statut.setPrefWidth(140);
        statut.setStyle(getStatutStyle(statutTexte));

        // Score
        Label score = new Label(getScoreSeance(s.getDureeMinutes()) + " ⭐");
        score.setStyle("-fx-font-size: 12px; -fx-text-fill: #f59e0b;");

        row.getChildren().addAll(date, nom, duree, statut, score);
        return row;
    }

    // ─── Animations ──────────────────────────────────────────────────────────

    private void demarrerAnimations() {
        Platform.runLater(() -> {
            Timeline timeline = new Timeline();
            timeline.getKeyFrames().addAll(
                    new KeyFrame(Duration.millis(400),
                            new KeyValue(barFrequence.progressProperty(), scoreFrequence / 10.0)),
                    new KeyFrame(Duration.millis(600),
                            new KeyValue(barIntensite.progressProperty(), scoreIntensite / 10.0)),
                    new KeyFrame(Duration.millis(800),
                            new KeyValue(barRegularite.progressProperty(), scoreRegularite / 10.0)),
                    new KeyFrame(Duration.millis(1000),
                            new KeyValue(barGlobal.progressProperty(), scoreGlobal / 10.0))
            );
            timeline.play();
        });
    }

    // ─── Navigation tabs ─────────────────────────────────────────────────────

    @FXML private void showApercu() {}

    @FXML private void showDetails() {}

    @FXML private void showRecommandations() {}

    // ─── Actions boutons ──────────────────────────────────────────────────────

    @FXML
    private void fermer() {
        Stage stage = (Stage) lblNomUtilisateur.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void exporterPDF() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export PDF");
        alert.setHeaderText(null);
        alert.setContentText("Export PDF en cours de développement.");
        alert.showAndWait();
    }

    @FXML
    private void relancerAnalyse() {
        chargerDonnees();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String getNiveauLabel(double score) {
        if (score >= 8) return "⭐ Excellent";
        if (score >= 6) return "👍 Bon niveau";
        if (score >= 4) return "🔄 En progression";
        return "💪 Débutant";
    }

    private String getNiveauGlobal(double score) {
        if (score >= 8) return "Expert 🏆";
        if (score >= 6) return "Avancé 💪";
        if (score >= 4) return "Intermédiaire 📈";
        return "Débutant 🌱";
    }

    private String getDescriptionNiveau(double score) {
        if (score >= 8) return "Performances exceptionnelles. Continuez ainsi !";
        if (score >= 6) return "Très bon niveau de régularité et d'intensité.";
        if (score >= 4) return "Bonne progression, encore quelques efforts.";
        return "Commencez par augmenter votre fréquence hebdomadaire.";
    }

    private String getAnalyseFrequence(int nbSeances) {
        if (nbSeances >= 10) return "Fréquence excellente ! " + nbSeances + " séances enregistrées.";
        if (nbSeances >= 5)  return "Bonne fréquence avec " + nbSeances + " séances.";
        if (nbSeances >= 2)  return "Fréquence modérée (" + nbSeances + " séances).";
        return "Fréquence faible. Planifiez 2 séances par semaine.";
    }

    private String getAnalyseIntensite(double scoreInt) {
        if (scoreInt >= 7) return "Intensité élevée et bien gérée. Pensez à bien récupérer.";
        if (scoreInt >= 5) return "Intensité modérée. Augmentez progressivement la durée.";
        return "Intensité à améliorer. Visez 45-60 minutes par séance.";
    }

    private String getAnalyseRegularite(double scoreReg) {
        if (scoreReg >= 8) return "Régularité exemplaire ! Maintenez ce rythme.";
        if (scoreReg >= 6) return "Bonne régularité dans l'ensemble.";
        return "Planifiez vos séances à l'avance pour plus de régularité.";
    }

    private List<String[]> getRecommandations(double global, double freq,
                                              double intens, double reg, int nbSeances) {
        List<String[]> recs = new java.util.ArrayList<>();

        if (freq < 5)
            recs.add(new String[]{"📅", "Augmenter la fréquence",
                    "Visez minimum 3 séances par semaine pour des progrès significatifs."});
        if (intens < 5)
            recs.add(new String[]{"⏱️", "Allonger les séances",
                    "Des séances de 45-60 minutes optimisent la dépense calorique."});
        if (reg < 6)
            recs.add(new String[]{"🗓️", "Planifier à l'avance",
                    "Bloquez vos créneaux sportifs comme des réunions importantes."});
        if (global >= 7)
            recs.add(new String[]{"🏆", "Maintenir votre niveau",
                    "Excellent travail ! Variez les exercices pour continuer à progresser."});

        recs.add(new String[]{"💧", "Hydratation et récupération",
                "Buvez au moins 2L d'eau par jour et dormez 7-8h."});
        recs.add(new String[]{"🥗", "Nutrition adaptée",
                "Protéines et glucides complexes soutiennent vos efforts sportifs."});

        return recs;
    }

    private String getStatutStyle(String statut) {
        if (statut == null) statut = "";
        return switch (statut.toLowerCase()) {
            case "complété", "termine", "terminé", "or", "argent", "bronze" ->
                    "-fx-font-size: 10px; -fx-font-weight: bold; " +
                            "-fx-background-color: #d1fae5; -fx-text-fill: #065f46; " +
                            "-fx-background-radius: 12; -fx-padding: 3 10;";
            case "annulé", "annule" ->
                    "-fx-font-size: 10px; -fx-font-weight: bold; " +
                            "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; " +
                            "-fx-background-radius: 12; -fx-padding: 3 10;";
            default ->
                    "-fx-font-size: 10px; -fx-font-weight: bold; " +
                            "-fx-background-color: #fef3c7; -fx-text-fill: #92400e; " +
                            "-fx-background-radius: 12; -fx-padding: 3 10;";
        };
    }

    private String getScoreSeance(int duree) {
        if (duree >= 90) return "5.0";
        if (duree >= 60) return "4.0";
        if (duree >= 45) return "3.5";
        if (duree >= 30) return "2.5";
        return "1.5";
    }
}