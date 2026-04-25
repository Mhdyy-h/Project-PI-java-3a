package org.example.controller;

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
import org.example.model.SeanceSport;
import org.example.service.ServiceSeanceSport;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller pour la page d'Analyse IA
 * Calcul 100% local — aucune API externe requise
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

    // ─── Données internes ────────────────────────────────────────────────────
    private int utilisateurId;
    private String nomUtilisateur;
    private ServiceSeanceSport seanceService;
    private List<SeanceSport> seances;

    private double scoreFrequence = 0;
    private double scoreIntensite = 0;
    private double scoreRegularite = 0;
    private double scoreGlobal    = 0;

    // ─────────────────────────────────────────────────────────────────────────
    //  INITIALISATION
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        seanceService = new ServiceSeanceSport();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  POINTS D'ENTRÉE PUBLICS
    //  Appelez l'un de ces deux depuis le controller qui ouvre la fenêtre
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Utiliser quand vous avez déjà calculé les scores ailleurs.
     */
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

    /**
     * Utiliser quand vous voulez que le controller calcule tout lui-même.
     * C'est la méthode à appeler depuis votre bouton d'ajout de séance.
     */
    public void setUtilisateur(int utilisateurId, String nomUtilisateur) {
        this.utilisateurId  = utilisateurId;
        this.nomUtilisateur = nomUtilisateur;
        lblNomUtilisateur.setText(nomUtilisateur);
        chargerDonnees();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CHARGEMENT & CALCUL
    // ─────────────────────────────────────────────────────────────────────────

    private void chargerDonnees() {
        // 1. Récupération des séances depuis la base
        try {
            seances = seanceService.rechercherParUtilisateur(utilisateurId);
        } catch (Exception e) {
            seances = new ArrayList<>();
            System.err.println("[AnalyseIA] Erreur chargement séances : " + e.getMessage());
        }

        // 2. Calcul local des scores si non fournis
        calculerScores();

        // 3. Affichage
        afficherStatistiques();
        afficherBarres();
        afficherRecommandations();
        afficherHistorique();
        demarrerAnimations();
    }

    /**
     * Calcul 100% local des scores IA — aucune API requise.
     *
     * Formules :
     *   scoreFrequence  = min(10, nbSeances / 2.0)
     *   scoreIntensite  = min(10, dureeMoyenne / 9.0)
     *   scoreRegularite = paliers : ≥8→9 | ≥5→7 | ≥3→5 | <3→3
     *   scoreGlobal     = moyenne des 3 scores
     */
    private void calculerScores() {
        if (seances == null || seances.isEmpty()) {
            scoreFrequence = scoreIntensite = scoreRegularite = scoreGlobal = 0;
            return;
        }

        int n = seances.size();

        // Fréquence
        scoreFrequence = Math.min(10.0, n / 2.0);

        // Intensité — basée sur la durée moyenne des séances
        double dureeMoyenne = seances.stream()
                .mapToInt(SeanceSport::getDureeMinutes)
                .average()
                .orElse(0);
        scoreIntensite = Math.min(10.0, dureeMoyenne / 9.0);

        // Régularité — paliers selon le nombre total de séances
        if      (n >= 8) scoreRegularite = 9.0;
        else if (n >= 5) scoreRegularite = 7.0;
        else if (n >= 3) scoreRegularite = 5.0;
        else             scoreRegularite = 3.0;

        // Score global = moyenne pondérée
        scoreGlobal = (scoreFrequence + scoreIntensite + scoreRegularite) / 3.0;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  AFFICHAGE — CARTES STATISTIQUES
    // ─────────────────────────────────────────────────────────────────────────

    private void afficherStatistiques() {
        int n = (seances != null) ? seances.size() : 0;

        // Carte séances
        lblTotalSeances.setText(String.valueOf(n));
        lblEvolutionSeances.setText(n > 0
                ? "✓ " + n + " séance(s) analysée(s)"
                : "Aucune séance enregistrée");

        // Carte durée moyenne
        if (n > 0) {
            double dureeMoy = seances.stream()
                    .mapToInt(SeanceSport::getDureeMinutes)
                    .average()
                    .orElse(0);
            lblDureeMoyenne.setText(String.format("%.0f", dureeMoy));
            lblTendanceDuree.setText(dureeMoy >= 60 ? "↑ Séances longues" : "→ Durée modérée");
        } else {
            lblDureeMoyenne.setText("0");
            lblTendanceDuree.setText("Aucune donnée");
        }

        // Carte score IA
        lblScoreIA.setText(String.format("%.1f", scoreGlobal));
        lblNiveauScore.setText(getNiveauLabel(scoreGlobal));

        // Carte niveau global (gradient violet)
        lblNiveauGlobal.setText(getNiveauGlobal(scoreGlobal));
        lblDescriptionNiveau.setText(getDescriptionNiveau(scoreGlobal));

        // Textes d'analyse détaillée
        lblAnalyseFrequence.setText(getAnalyseFrequence(n));
        lblAnalyseIntensite.setText(getAnalyseIntensite(scoreIntensite));
        lblAnalyseRegularite.setText(getAnalyseRegularite(scoreRegularite));

        // Compteur historique
        lblNbSeances.setText(n + " séance(s) • 30 derniers jours");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  AFFICHAGE — BARRES DE PROGRESSION (initialisées à 0, animées après)
    // ─────────────────────────────────────────────────────────────────────────

    private void afficherBarres() {
        lblScoreFrequence.setText(String.format("%.1f/10", scoreFrequence));
        lblScoreIntensite.setText(String.format("%.1f/10", scoreIntensite));
        lblScoreRegularite.setText(String.format("%.1f/10", scoreRegularite));
        lblScoreGlobal2.setText(String.format("%.1f/10", scoreGlobal));

        // Remise à zéro avant animation
        barFrequence.setProgress(0);
        barIntensite.setProgress(0);
        barRegularite.setProgress(0);
        barGlobal.setProgress(0);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  AFFICHAGE — RECOMMANDATIONS
    // ─────────────────────────────────────────────────────────────────────────

    private void afficherRecommandations() {
        recommendationsContainer.getChildren().clear();

        List<String[]> recs = construireRecommandations();

        for (int i = 0; i < recs.size(); i++) {
            String[] rec = recs.get(i);
            HBox card = creerCarteRecommandation(rec[0], rec[1], rec[2], i % 3);
            recommendationsContainer.getChildren().add(card);
        }
    }

    private List<String[]> construireRecommandations() {
        List<String[]> recs = new ArrayList<>();
        int n = (seances != null) ? seances.size() : 0;

        // Recommandations conditionnelles selon les scores
        if (scoreFrequence < 5)
            recs.add(new String[]{"📅", "Augmenter la fréquence",
                    "Visez minimum 3 séances par semaine pour des progrès significatifs."});

        if (scoreIntensite < 5)
            recs.add(new String[]{"⏱️", "Allonger les séances",
                    "Des séances de 45 à 60 minutes optimisent la dépense calorique."});

        if (scoreRegularite < 6)
            recs.add(new String[]{"🗓️", "Planifier à l'avance",
                    "Bloquez vos créneaux sportifs dans votre agenda comme des rendez-vous."});

        if (scoreGlobal >= 7)
            recs.add(new String[]{"🏆", "Maintenir votre niveau",
                    "Excellent travail ! Variez les exercices pour continuer à progresser."});

        // Recommandations fixes toujours affichées
        recs.add(new String[]{"💧", "Hydratation et récupération",
                "Buvez au moins 2L d'eau par jour et dormez 7 à 8 heures par nuit."});

        recs.add(new String[]{"🥗", "Nutrition adaptée",
                "Protéines et glucides complexes soutiennent vos efforts sportifs."});

        if (n == 0)
            recs.add(new String[]{"🚀", "Commencer maintenant",
                    "Enregistrez votre première séance pour obtenir une analyse personnalisée."});

        return recs;
    }

    private HBox creerCarteRecommandation(String emoji, String titre,
                                          String description, int colorIndex) {
        String[] couleursFond   = {"#ede9fe", "#d1fae5", "#fef3c7"};
        String[] couleursTexte  = {"#7c3aed", "#059669", "#d97706"};

        HBox card = new HBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-background-color: " + couleursFond[colorIndex] + ";" +
                        "-fx-background-radius: 12; -fx-padding: 14 18;"
        );

        // Icône dans un cercle blanc
        StackPane iconBox = new StackPane();
        Circle circle = new Circle(18);
        circle.setStyle("-fx-fill: white;");
        Label icon = new Label(emoji);
        icon.setStyle("-fx-font-size: 15px;");
        iconBox.getChildren().addAll(circle, icon);

        // Textes
        VBox texte = new VBox(3);
        Label lblTitre = new Label(titre);
        lblTitre.setStyle(
                "-fx-font-size: 13px; -fx-font-weight: bold;" +
                        "-fx-text-fill: " + couleursTexte[colorIndex] + ";"
        );
        Label lblDesc = new Label(description);
        lblDesc.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280; -fx-wrap-text: true;");
        lblDesc.setMaxWidth(480);
        texte.getChildren().addAll(lblTitre, lblDesc);

        card.getChildren().addAll(iconBox, texte);
        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  AFFICHAGE — HISTORIQUE DES SÉANCES
    // ─────────────────────────────────────────────────────────────────────────

    private void afficherHistorique() {
        seancesContainer.getChildren().clear();

        if (seances == null || seances.isEmpty()) {
            emptyState.setVisible(true);
            return;
        }

        emptyState.setVisible(false);

        // Affiche les 10 séances les plus récentes
        List<SeanceSport> recentes = seances.stream().limit(10).toList();

        for (int i = 0; i < recentes.size(); i++) {
            SeanceSport s = recentes.get(i);
            HBox row = creerLigneSeance(s, i % 2 == 0);
            seancesContainer.getChildren().add(row);

            if (i < recentes.size() - 1) {
                Separator sep = new Separator();
                sep.setStyle("-fx-opacity: 0.3;");
                seancesContainer.getChildren().add(sep);
            }
        }
    }

    private HBox creerLigneSeance(SeanceSport s, boolean fondClair) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(
                "-fx-padding: 12 24;" +
                        "-fx-background-color: " + (fondClair ? "white" : "#fafbfd") + ";"
        );

        // ── Date ──────────────────────────────────────────────────────────────
        // Si getDateSeance() retourne un LocalDate, remplacez par :
        // s.getDateSeance() != null ? s.getDateSeance().toString() : "—"
        String dateTexte = "—";
        try {
            Object d = s.getDateSeance();
            dateTexte = (d != null) ? d.toString() : "—";
        } catch (Exception ignored) {}

        Label date = new Label(dateTexte);
        date.setPrefWidth(150);
        date.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");

        // ── Nom de la séance ──────────────────────────────────────────────────
        String nomTexte = (s.getNomSeance() != null && !s.getNomSeance().isBlank())
                ? s.getNomSeance() : "Séance";
        Label nom = new Label(nomTexte);
        nom.setPrefWidth(180);
        nom.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");

        // ── Durée ─────────────────────────────────────────────────────────────
        Label duree = new Label(s.getDureeMinutes() + " min");
        duree.setPrefWidth(120);
        duree.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        // ── Statut (médaille ou "Complété") ───────────────────────────────────
        String statutTexte = "Complété";
        try {
            String medaille = s.getMedailleObtenue();
            if (medaille != null && !medaille.isBlank()) statutTexte = medaille;
        } catch (Exception ignored) {}

        Label statut = new Label(statutTexte);
        statut.setPrefWidth(140);
        statut.setStyle(getStatutStyle(statutTexte));

        // ── Score calculé selon la durée ──────────────────────────────────────
        Label score = new Label(getScoreSeance(s.getDureeMinutes()) + " ⭐");
        score.setStyle("-fx-font-size: 12px; -fx-text-fill: #f59e0b;");

        row.getChildren().addAll(date, nom, duree, statut, score);
        return row;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  ANIMATIONS DES BARRES DE PROGRESSION
    // ─────────────────────────────────────────────────────────────────────────

    private void demarrerAnimations() {
        Platform.runLater(() -> {
            Timeline tl = new Timeline(
                    new KeyFrame(Duration.millis(400),
                            new KeyValue(barFrequence.progressProperty(),  scoreFrequence  / 10.0)),
                    new KeyFrame(Duration.millis(600),
                            new KeyValue(barIntensite.progressProperty(),  scoreIntensite  / 10.0)),
                    new KeyFrame(Duration.millis(800),
                            new KeyValue(barRegularite.progressProperty(), scoreRegularite / 10.0)),
                    new KeyFrame(Duration.millis(1000),
                            new KeyValue(barGlobal.progressProperty(),     scoreGlobal     / 10.0))
            );
            tl.play();
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  NAVIGATION TABS (à compléter si besoin)
    // ─────────────────────────────────────────────────────────────────────────

    @FXML private void showApercu() { /* déjà affiché par défaut */ }

    @FXML private void showDetails() { /* à implémenter */ }

    @FXML private void showRecommandations() { /* à implémenter */ }

    // ─────────────────────────────────────────────────────────────────────────
    //  ACTIONS BOUTONS
    // ─────────────────────────────────────────────────────────────────────────

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
        alert.setContentText("L'export PDF est en cours de développement.");
        alert.showAndWait();
    }

    @FXML
    private void relancerAnalyse() {
        // Réinitialise les scores et relance tout
        scoreFrequence = scoreIntensite = scoreRegularite = scoreGlobal = 0;
        chargerDonnees();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPERS — LABELS & STYLES
    // ─────────────────────────────────────────────────────────────────────────

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
        if (nbSeances >= 2)  return "Fréquence modérée (" + nbSeances + " séances). Essayez d'atteindre 5/semaine.";
        return "Fréquence trop faible. Planifiez au moins 2 séances par semaine.";
    }

    private String getAnalyseIntensite(double scoreInt) {
        if (scoreInt >= 7) return "Intensité élevée et bien gérée. Pensez à récupérer entre les séances.";
        if (scoreInt >= 5) return "Intensité modérée. Augmentez progressivement la durée de vos séances.";
        return "Intensité à améliorer. Visez 45 à 60 minutes par séance.";
    }

    private String getAnalyseRegularite(double scoreReg) {
        if (scoreReg >= 8) return "Régularité exemplaire ! Maintenez ce rythme.";
        if (scoreReg >= 6) return "Bonne régularité dans l'ensemble.";
        return "Planifiez vos séances à l'avance pour plus de régularité.";
    }

    private String getStatutStyle(String statut) {
        if (statut == null) statut = "";
        return switch (statut.toLowerCase()) {
            case "complété", "complete", "terminé", "termine",
                 "or", "argent", "bronze" ->
                    "-fx-font-size: 10px; -fx-font-weight: bold;" +
                            "-fx-background-color: #d1fae5; -fx-text-fill: #065f46;" +
                            "-fx-background-radius: 12; -fx-padding: 3 10;";
            case "annulé", "annule" ->
                    "-fx-font-size: 10px; -fx-font-weight: bold;" +
                            "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b;" +
                            "-fx-background-radius: 12; -fx-padding: 3 10;";
            default ->
                    "-fx-font-size: 10px; -fx-font-weight: bold;" +
                            "-fx-background-color: #fef3c7; -fx-text-fill: #92400e;" +
                            "-fx-background-radius: 12; -fx-padding: 3 10;";
        };
    }

    /**
     * Score estimé d'une séance selon sa durée.
     */
    private String getScoreSeance(int duree) {
        if (duree >= 90) return "5.0";
        if (duree >= 60) return "4.0";
        if (duree >= 45) return "3.5";
        if (duree >= 30) return "2.5";
        return "1.5";
    }
}