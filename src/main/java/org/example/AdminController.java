package org.example;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AdminController {

    // ── SIDEBAR BOUTONS ─────────────────────────────────────────
    @FXML private Button btnDashboard;
    @FXML private Button btnSeances;
    @FXML private Button btnExercices;
    @FXML private Label  topBarTitle;

    // ── STATS ────────────────────────────────────────────────────
    @FXML private Label statSeances;
    @FXML private Label statExercices;
    @FXML private Label statCalories;
    @FXML private Label statMedailles;

    // ── PANES ────────────────────────────────────────────────────
    @FXML private VBox paneDashboard;
    @FXML private VBox paneSeances;
    @FXML private VBox paneExercices;
    @FXML private ScrollPane paneAjouterSeance;
    @FXML private ScrollPane paneModifierSeance;
    @FXML private ScrollPane paneAjouterExercice;
    @FXML private ScrollPane paneModifierExercice;
    @FXML private VBox paneConfirmSuppr;

    // ── TABLES ───────────────────────────────────────────────────
    @FXML private VBox tableSeances;
    @FXML private VBox tableExercices;

    // ── CONTENUS FORMULAIRES ─────────────────────────────────────
    @FXML private VBox contenuAjouterSeance;
    @FXML private VBox contenuModifierSeance;
    @FXML private VBox contenuAjouterExercice;
    @FXML private VBox contenuModifierExercice;

    // ── SUPPRESSION ──────────────────────────────────────────────
    @FXML private Label supprType;
    @FXML private Label supprNom;
    @FXML private Label supprDetail;

    private SeanceSport seanceASupprimer;
    private Exercice    exerciceASupprimer;
    private String      retourApresSuppr;

    // ── STYLES SIDEBAR ───────────────────────────────────────────
    private static final String BTN_ACTIF =
            "-fx-background-color: #f0ebff; -fx-text-fill: #2d1b69; " +
                    "-fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 10; " +
                    "-fx-cursor: hand; -fx-padding: 12 16; -fx-alignment: CENTER_LEFT; " +
                    "-fx-border-color: #2d1b69; -fx-border-radius: 10; -fx-border-width: 2;";

    private static final String BTN_INACTIF =
            "-fx-background-color: transparent; -fx-text-fill: #555; " +
                    "-fx-font-size: 13px; -fx-background-radius: 10; " +
                    "-fx-cursor: hand; -fx-padding: 12 16; -fx-alignment: CENTER_LEFT;";

    @FXML
    public void initialize() {
        chargerStats();
        chargerTableSeances();
        chargerTableExercices();
    }

    // ══════════════════════════════════════════════════════════════
    // NAVIGATION SIDEBAR
    // ══════════════════════════════════════════════════════════════

    private void afficherPane(Node pane, String titre, Button btnActif) {
        // Cacher tout
        paneDashboard.setVisible(false);        paneDashboard.setManaged(false);
        paneSeances.setVisible(false);          paneSeances.setManaged(false);
        paneExercices.setVisible(false);        paneExercices.setManaged(false);
        paneAjouterSeance.setVisible(false);    paneAjouterSeance.setManaged(false);
        paneModifierSeance.setVisible(false);   paneModifierSeance.setManaged(false);
        paneAjouterExercice.setVisible(false);  paneAjouterExercice.setManaged(false);
        paneModifierExercice.setVisible(false); paneModifierExercice.setManaged(false);
        paneConfirmSuppr.setVisible(false);     paneConfirmSuppr.setManaged(false);

        // Réinitialiser styles sidebar
        btnDashboard.setStyle(BTN_INACTIF);
        btnSeances.setStyle(BTN_INACTIF);
        btnExercices.setStyle(BTN_INACTIF);
        if (btnActif != null) btnActif.setStyle(BTN_ACTIF);

        // ← CORRIGÉ : Node.setManaged() au lieu de ((Region) pane).setManaged()
        pane.setVisible(true);
        pane.setManaged(true);
        topBarTitle.setText(titre);
    }

    @FXML public void showDashboard() {
        chargerStats();
        afficherPane(paneDashboard, "📊 Dashboard", btnDashboard);
    }

    @FXML public void showSeances() {
        chargerTableSeances();
        afficherPane(paneSeances, "🏃 Gestion Séances", btnSeances);
    }

    @FXML public void showExercices() {
        chargerTableExercices();
        afficherPane(paneExercices, "💪 Gestion Exercices", btnExercices);
    }

    // ══════════════════════════════════════════════════════════════
    // STATS
    // ══════════════════════════════════════════════════════════════

    private void chargerStats() {
        try {
            List<SeanceSport> seances   = new ServiceSeanceSport().afficherAll();
            List<Exercice>    exercices = new ServiceExercice().afficherAll();

            statSeances.setText(String.valueOf(seances.size()));
            statExercices.setText(String.valueOf(exercices.size()));

            long or     = seances.stream().filter(s -> "Or".equals(s.getMedailleObtenue())).count();
            long argent = seances.stream().filter(s -> "Argent".equals(s.getMedailleObtenue())).count();
            long bronze = seances.stream().filter(s -> "Bronze".equals(s.getMedailleObtenue())).count();
            statMedailles.setText("🥇" + or + "  🥈" + argent + "  🥉" + bronze);

            double moy = exercices.stream()
                    .mapToDouble(e -> e.caloriesParMinute)
                    .average().orElse(0);
            statCalories.setText(String.format("%.1f cal/min", moy));

        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ══════════════════════════════════════════════════════════════
    // TABLE SÉANCES
    // ══════════════════════════════════════════════════════════════

    private void chargerTableSeances() {
        tableSeances.getChildren().clear();
        try {
            List<SeanceSport> liste = new ServiceSeanceSport().afficherAll();
            tableSeances.getChildren().add(creerLigneHeader("ID","Nom","Durée","Date","Médaille","Actions"));

            for (SeanceSport s : liste) {
                HBox ligne = new HBox(10);
                ligne.setPadding(new Insets(10, 20, 10, 20));
                ligne.setStyle("-fx-background-color: white; -fx-border-color: #f5f0ff; -fx-border-width: 0 0 1 0;");

                ligne.getChildren().addAll(
                        cellule(String.valueOf(s.getId()), 40),
                        cellule(s.getNomSeance(), 160),
                        cellule(s.getDureeMinutes() + " min", 80),
                        cellule(s.getDateSeance(), 100),
                        cellule(s.getMedailleObtenue() != null ? s.getMedailleObtenue() : "—", 80)
                );

                Button btnMod = bouton("✏️", "#f0c040", "#333");
                btnMod.setOnAction(e -> naviguerModifierSeance(s));

                Button btnDel = bouton("🗑️", "#ffe8ec", "#e94560");
                btnDel.setOnAction(e -> demanderSuppressionSeance(s));

                ligne.getChildren().add(new HBox(6, btnMod, btnDel));
                tableSeances.getChildren().add(ligne);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ══════════════════════════════════════════════════════════════
    // TABLE EXERCICES
    // ══════════════════════════════════════════════════════════════

    private void chargerTableExercices() {
        tableExercices.getChildren().clear();
        try {
            List<Exercice> liste = new ServiceExercice().afficherAll();
            tableExercices.getChildren().add(creerLigneHeader("ID","Nom","Intensité","Cal/min","Actions"));

            for (Exercice ex : liste) {
                HBox ligne = new HBox(10);
                ligne.setPadding(new Insets(10, 20, 10, 20));
                ligne.setStyle("-fx-background-color: white; -fx-border-color: #f5f0ff; -fx-border-width: 0 0 1 0;");

                ligne.getChildren().addAll(
                        cellule(String.valueOf(ex.id), 40),
                        cellule(ex.nomExercice, 180),
                        cellule(ex.intensite, 100),
                        cellule(String.format("%.1f", ex.caloriesParMinute), 80)
                );

                Button btnMod = bouton("✏️", "#f0c040", "#333");
                btnMod.setOnAction(e -> naviguerModifierExercice(ex));

                Button btnDel = bouton("🗑️", "#ffe8ec", "#e94560");
                btnDel.setOnAction(e -> demanderSuppressionExercice(ex));

                ligne.getChildren().add(new HBox(6, btnMod, btnDel));
                tableExercices.getChildren().add(ligne);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ══════════════════════════════════════════════════════════════
    // NAVIGATION FORMULAIRES (dans la même fenêtre)
    // ══════════════════════════════════════════════════════════════

    @FXML public void ajouterSeance() {
        try {
            contenuAjouterSeance.getChildren().clear();
            Node form = FXMLLoader.load(getClass().getResource("/AjouterSeance.fxml"));
            contenuAjouterSeance.getChildren().add(form);
            afficherPane(paneAjouterSeance, "➕ Ajouter une Séance", null);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML public void ajouterExercice() {
        try {
            contenuAjouterExercice.getChildren().clear();
            Node form = FXMLLoader.load(getClass().getResource("/AjouterExercice.fxml"));
            contenuAjouterExercice.getChildren().add(form);
            afficherPane(paneAjouterExercice, "➕ Ajouter un Exercice", null);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void naviguerModifierSeance(SeanceSport s) {
        try {
            SeanceSelection.seance = s;
            contenuModifierSeance.getChildren().clear();
            Node form = FXMLLoader.load(getClass().getResource("/ModifierSeance.fxml"));
            contenuModifierSeance.getChildren().add(form);
            afficherPane(paneModifierSeance, "✏️ Modifier Séance — " + s.getNomSeance(), null);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void naviguerModifierExercice(Exercice ex) {
        try {
            ExerciceSelection.exercice = ex;
            contenuModifierExercice.getChildren().clear();
            Node form = FXMLLoader.load(getClass().getResource("/ModifierExercice.fxml"));
            contenuModifierExercice.getChildren().add(form);
            afficherPane(paneModifierExercice, "✏️ Modifier Exercice — " + ex.nomExercice, null);
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ══════════════════════════════════════════════════════════════
    // SUPPRESSION AVEC PAGE DÉDIÉE
    // ══════════════════════════════════════════════════════════════

    private void demanderSuppressionSeance(SeanceSport s) {
        seanceASupprimer   = s;
        exerciceASupprimer = null;
        retourApresSuppr   = "seances";

        supprType.setText("SÉANCE SPORT");
        supprNom.setText(s.getNomSeance());
        supprDetail.setText("Durée : " + s.getDureeMinutes() + " min  •  Date : " + s.getDateSeance()
                + "  •  Médaille : " + (s.getMedailleObtenue() != null ? s.getMedailleObtenue() : "—"));

        afficherPane(paneConfirmSuppr, "🗑️ Confirmer la suppression", null);
    }

    private void demanderSuppressionExercice(Exercice ex) {
        exerciceASupprimer = ex;
        seanceASupprimer   = null;
        retourApresSuppr   = "exercices";

        supprType.setText("EXERCICE");
        supprNom.setText(ex.nomExercice);
        supprDetail.setText("Intensité : " + ex.intensite
                + "  •  " + String.format("%.1f", ex.caloriesParMinute) + " cal/min");

        afficherPane(paneConfirmSuppr, "🗑️ Confirmer la suppression", null);
    }

    @FXML public void confirmerSuppression() {
        try {
            if (seanceASupprimer != null) {
                new ServiceSeanceSport().delete(seanceASupprimer.getId());
                seanceASupprimer = null;
                showSeances();
            } else if (exerciceASupprimer != null) {
                new ServiceExercice().delete(exerciceASupprimer.id);
                exerciceASupprimer = null;
                showExercices();
            }
            chargerStats();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML public void annulerSuppression() {
        if ("seances".equals(retourApresSuppr)) showSeances();
        else showExercices();
    }

    // ══════════════════════════════════════════════════════════════
    // FERMER
    // ══════════════════════════════════════════════════════════════

    @FXML public void fermerFenetre() {
        btnDashboard.getScene().getWindow().hide();
    }

    // ══════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════

    private HBox creerLigneHeader(String... cols) {
        HBox h = new HBox(10);
        h.setPadding(new Insets(12, 20, 12, 20));
        h.setStyle("-fx-background-color: #f4f1fb; -fx-border-color: #e8e0f5; -fx-border-width: 0 0 2 0;");
        for (String col : cols) {
            Label l = new Label(col);
            l.setStyle("-fx-text-fill: #2d1b69; -fx-font-weight: bold; -fx-font-size: 12;");
            l.setMinWidth(col.equals("Actions") ? 80 : 100);
            h.getChildren().add(l);
        }
        return h;
    }

    private Label cellule(String texte, double largeur) {
        Label l = new Label(texte != null ? texte : "—");
        l.setMinWidth(largeur);
        l.setStyle("-fx-font-size: 12; -fx-text-fill: #444;");
        return l;
    }

    private Button bouton(String text, String bg, String fg) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; " +
                "-fx-background-radius: 7; -fx-cursor: hand; -fx-padding: 5 10;");
        return b;
    }
}