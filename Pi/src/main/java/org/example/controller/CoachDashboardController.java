package org.example.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.dao.AlerteDAO;
import org.example.dao.RepasDAO;
import org.example.model.Alerte;
import org.example.model.ChronoScore;
import org.example.model.Repas;
import org.example.model.User;
import org.example.service.Chronoscoreservice;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dashboard nutritionnel – Vue Coach.
 * Affiche les stats + graphiques + alertes d'un utilisateur cible.
 */
public class CoachDashboardController {

    // ── Header ──
    @FXML private Label headerUserLabel;

    // ── Stats ──
    @FXML private Label scoreMoyenLabel;
    @FXML private Label meilleurScoreLabel;
    @FXML private Label pireScoreLabel;
    @FXML private Label alertesCountLabel;

    // ── Graphiques ──
    @FXML private LineChart<String, Number> scoreChart;
    @FXML private BarChart<String, Number>  caloriesChart;

    // ── Alertes ──
    @FXML private VBox   alertesContainer;
    @FXML private Label  noAlertesLabel;
    @FXML private Label  alertesBadge;

    private User utilisateur;
    private List<Repas> repasList = new ArrayList<>();
    private Map<LocalDate, List<Repas>> repasParJour = new TreeMap<>();

    // ─────────────────────────────────────────────────
    //  POINT D'ENTRÉE
    // ─────────────────────────────────────────────────

    public void setUtilisateur(User user) {
        this.utilisateur = user;
        if (user != null) {
            headerUserLabel.setText(user.getNomComplet() + " – " + user.getEmail());
        }
        chargerDonnees();
        chargerAlertes();
    }

    // ─────────────────────────────────────────────────
    //  CHARGEMENT DES DONNÉES
    // ─────────────────────────────────────────────────

    private void chargerDonnees() {
        if (utilisateur == null) return;

        // 30 derniers jours
        LocalDateTime debut = LocalDateTime.now().minusDays(30);
        repasList = RepasDAO.getByUtilisateurAndDate(
                utilisateur.getId(), debut, LocalDateTime.now());

        repasParJour = repasList.stream().collect(
                Collectors.groupingBy(
                        r -> r.getDateConsommation().toLocalDate(),
                        TreeMap::new,
                        Collectors.toList()));

        calculerStats();
        afficherGraphiqueScores();
        afficherGraphiqueCalories();
    }

    private void calculerStats() {
        List<Integer> scores = repasList.stream()
                .map(r -> Chronoscoreservice.calculerChronoScore(r).getTotalScore())
                .collect(Collectors.toList());

        double moyenne = scores.stream().mapToInt(Integer::intValue).average().orElse(0);
        int meilleur   = scores.stream().max(Integer::compareTo).orElse(0);
        int pire       = scores.stream().min(Integer::compareTo).orElse(0);

        scoreMoyenLabel.setText(String.format("%.1f/14", moyenne));
        meilleurScoreLabel.setText(meilleur + "/14");
        pireScoreLabel.setText(pire + "/14");

        // Couleur score moyen
        String couleur;
        if      (moyenne >= 10) couleur = "#10b981";
        else if (moyenne >= 7)  couleur = "#4C6FFF";
        else if (moyenne >= 4)  couleur = "#f39c12";
        else                    couleur = "#e74c3c";
        scoreMoyenLabel.setStyle("-fx-text-fill: " + couleur +
                "; -fx-font-size: 28px; -fx-font-weight: bold;");
    }

    // ─────────────────────────────────────────────────
    //  GRAPHIQUES
    // ─────────────────────────────────────────────────

    private void afficherGraphiqueScores() {
        scoreChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("ChronoScore");

        for (Map.Entry<LocalDate, List<Repas>> entry : repasParJour.entrySet()) {
            LocalDate date    = entry.getKey();
            List<Repas> repas = entry.getValue();

            double moyJour = repas.stream()
                    .mapToInt(r -> Chronoscoreservice.calculerChronoScore(r).getTotalScore())
                    .average().orElse(0);

            XYChart.Data<String, Number> point = new XYChart.Data<>(date.toString(), moyJour);
            series.getData().add(point);

            point.nodeProperty().addListener((obs, old, newNode) -> {
                if (newNode != null) ajouterTooltipScore(newNode, date, repas);
            });
        }

        scoreChart.getData().add(series);
        scoreChart.layout();

        for (XYChart.Data<String, Number> data : series.getData()) {
            if (data.getNode() != null) {
                LocalDate date = LocalDate.parse(data.getXValue());
                List<Repas> repas = repasParJour.getOrDefault(date, Collections.emptyList());
                ajouterTooltipScore(data.getNode(), date, repas);
            }
        }
    }

    private void ajouterTooltipScore(Node node, LocalDate date, List<Repas> repas) {
        StringBuilder sb = new StringBuilder();
        sb.append("Date : ").append(date).append("\n");
        for (Repas r : repas) {
            ChronoScore sc = Chronoscoreservice.calculerChronoScore(r);
            sb.append("  ").append(r.getTitreRepas())
              .append(" → ").append(sc.getTotalScore()).append("/14\n");
        }
        Tooltip tt = new Tooltip(sb.toString());
        tt.setShowDelay(Duration.millis(80));
        tt.setStyle("-fx-background-color: white; -fx-text-fill: #1a1a2e; " +
                "-fx-border-color: #4C6FFF; -fx-border-width: 2; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-padding: 10;");
        Tooltip.install(node, tt);
    }

    private void afficherGraphiqueCalories() {
        caloriesChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Calories");

        // Limiter aux 7 derniers jours
        LocalDate limiteDebut = LocalDate.now().minusDays(7);
        for (Map.Entry<LocalDate, List<Repas>> entry : repasParJour.entrySet()) {
            if (entry.getKey().isBefore(limiteDebut)) continue;
            LocalDate date    = entry.getKey();
            List<Repas> repas = entry.getValue();
            int totalCal = repas.stream().mapToInt(Repas::getTotalCalories).sum();

            XYChart.Data<String, Number> barre = new XYChart.Data<>(date.toString(), totalCal);
            series.getData().add(barre);

            barre.nodeProperty().addListener((obs, old, newNode) -> {
                if (newNode != null) {
                    Tooltip tt = new Tooltip(totalCal + " cal le " + date);
                    tt.setShowDelay(Duration.millis(80));
                    tt.setStyle("-fx-background-color: white; -fx-text-fill: #1a1a2e; " +
                            "-fx-border-color: #10b981; -fx-border-width: 2; " +
                            "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10;");
                    Tooltip.install(newNode, tt);
                }
            });
        }
        caloriesChart.getData().add(series);
    }

    // ─────────────────────────────────────────────────
    //  ALERTES
    // ─────────────────────────────────────────────────

    private void chargerAlertes() {
        if (utilisateur == null) return;

        List<Alerte> alertes = AlerteDAO.getByUtilisateurId(utilisateur.getId());

        alertesCountLabel.setText(String.valueOf(alertes.size()));
        alertesBadge.setText(String.valueOf(alertes.size()));

        alertesContainer.getChildren().clear();

        if (alertes.isEmpty()) {
            noAlertesLabel.setVisible(true);
            noAlertesLabel.setManaged(true);
        } else {
            noAlertesLabel.setVisible(false);
            noAlertesLabel.setManaged(false);

            for (Alerte a : alertes) {
                alertesContainer.getChildren().add(buildAlerteCard(a));
            }
        }
    }

    private HBox buildAlerteCard(Alerte alerte) {
        HBox card = new HBox(16);
        card.setStyle("-fx-background-color: " + (alerte.getCriticite().equals("ROUGE") ? "#fff5f5" : "#fffbf0")
                + "; -fx-background-radius: 10; -fx-padding: 14 16; -fx-alignment: CENTER_LEFT;"
                + "-fx-border-color: " + alerte.getCouleurCriticite() + ";"
                + "-fx-border-width: 0 0 0 4; -fx-border-radius: 0 10 10 0;");

        // Icône criticité
        Label icone = new Label(alerte.getCriticite().equals("ROUGE") ? "🔴" : "🟡");
        icone.setStyle("-fx-font-size: 18px;");

        // Contenu texte
        VBox textes = new VBox(4);
        textes.setStyle("-fx-pref-width: 10000;"); // s'étire

        Label type = new Label(alerte.getType());
        type.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: "
                + alerte.getCouleurCriticite() + "; -fx-background-color: "
                + (alerte.getCriticite().equals("ROUGE") ? "#fee2e2" : "#fef3c7")
                + "; -fx-background-radius: 20; -fx-padding: 2 10;");

        Label msg = new Label(alerte.getMessage());
        msg.setStyle("-fx-font-size: 13px; -fx-text-fill: #1a1a2e;");
        msg.setWrapText(true);

        textes.getChildren().addAll(type, msg);

        // Date
        Label date = new Label(alerte.getDateFormatee());
        date.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");

        card.getChildren().addAll(icone, textes, date);
        return card;
    }

    // ─────────────────────────────────────────────────
    //  ACTIONS
    // ─────────────────────────────────────────────────

    @FXML
    private void fermer() {
        Stage stage = (Stage) scoreChart.getScene().getWindow();
        stage.close();
    }
}
