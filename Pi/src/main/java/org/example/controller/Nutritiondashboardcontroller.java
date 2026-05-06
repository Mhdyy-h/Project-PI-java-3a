package org.example.controller;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.model.Repas;
import org.example.model.ChronoScore;
import org.example.dao.RepasDAO;
import org.example.service.Chronoscoreservice;
import org.example.service.AlertService;
import org.example.service.ExportPdfService;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.collections.FXCollections;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Nutritiondashboardcontroller {

    @FXML private LineChart<String, Number> scoreChart;
    @FXML private BarChart<String, Number>  caloriesChart;
    @FXML private Label scoreMoyenLabel;
    @FXML private Label meilleurScoreLabel;
    @FXML private Label pireScoreLabel;
    @FXML private Label alertesCountLabel;
    @FXML private Label tendanceLabel;
    @FXML private ComboBox<String> periodeCombo;

    private int utilisateurId;
    private List<Repas> repasList;
    private String tendanceCourante = "";
    private Map<LocalDate, List<Repas>> repasParJour = new TreeMap<>();

    // ─────────────────────────────────────────────────
    //  STYLE TOOLTIP COMMUN — fond blanc, texte noir
    // ─────────────────────────────────────────────────

    private static final String STYLE_TOOLTIP_SCORE =
            "-fx-font-family: 'Segoe UI', Arial, sans-serif;"
                    + "-fx-font-size: 12px;"
                    + "-fx-background-color: white;"
                    + "-fx-text-fill: #1a1a2e;"
                    + "-fx-background-radius: 10;"
                    + "-fx-border-color: #4C6FFF;"
                    + "-fx-border-width: 2px;"
                    + "-fx-border-radius: 10;"
                    + "-fx-padding: 14;"
                    + "-fx-effect: dropshadow(gaussian, rgba(76,111,255,0.25), 12, 0, 0, 4);";

    private static final String STYLE_TOOLTIP_CALORIES =
            "-fx-font-family: 'Segoe UI', Arial, sans-serif;"
                    + "-fx-font-size: 12px;"
                    + "-fx-background-color: white;"
                    + "-fx-text-fill: #1a1a2e;"
                    + "-fx-background-radius: 10;"
                    + "-fx-border-color: #27ae60;"
                    + "-fx-border-width: 2px;"
                    + "-fx-border-radius: 10;"
                    + "-fx-padding: 14;"
                    + "-fx-effect: dropshadow(gaussian, rgba(39,174,96,0.25), 12, 0, 0, 4);";

    // ─────────────────────────────────────────────────
    //  INITIALISATION
    // ─────────────────────────────────────────────────

    public void setUtilisateurId(int id) {
        this.utilisateurId = id;
        periodeCombo.setItems(FXCollections.observableArrayList(
                "Cette semaine", "Ce mois", "Tous"));
        periodeCombo.setValue("Cette semaine");
        periodeCombo.setOnAction(e -> chargerDonnees());
        chargerDonnees();
    }

    // ─────────────────────────────────────────────────
    //  CHARGEMENT
    // ─────────────────────────────────────────────────

    private void chargerDonnees() {
        String periode = periodeCombo.getValue();
        LocalDateTime startDate = switch (periode) {
            case "Cette semaine" -> LocalDateTime.now().minusDays(7);
            case "Ce mois"       -> LocalDateTime.now().minusDays(30);
            default              -> LocalDateTime.now().minusYears(10);
        };

        repasList = RepasDAO.getByUtilisateurAndDate(
                utilisateurId, startDate, LocalDateTime.now());

        repasParJour = repasList.stream().collect(
                Collectors.groupingBy(
                        r -> r.getDateConsommation().toLocalDate(),
                        TreeMap::new,
                        Collectors.toList()));

        calculerStats();
        afficherGraphiqueScores();
        afficherGraphiqueCalories();
        afficherTendance();
    }

    // ─────────────────────────────────────────────────
    //  STATISTIQUES
    // ─────────────────────────────────────────────────

    private void calculerStats() {
        List<Integer> scores = repasList.stream()
                .map(r -> Chronoscoreservice.calculerChronoScore(r).getTotalScore())
                .collect(Collectors.toList());

        double moyenne = scores.stream().mapToInt(Integer::intValue).average().orElse(0);
        int meilleur   = scores.stream().max(Integer::compareTo).orElse(0);
        int pire       = scores.stream().min(Integer::compareTo).orElse(0);
        int alertes    = AlertService.getAlertesActives(utilisateurId).size();

        scoreMoyenLabel.setText(String.format("%.1f/14", moyenne));
        meilleurScoreLabel.setText(meilleur + "/14");
        pireScoreLabel.setText(pire + "/14");
        alertesCountLabel.setText(String.valueOf(alertes));

        if      (moyenne >= 10) scoreMoyenLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 24px; -fx-font-weight: bold;");
        else if (moyenne >= 7)  scoreMoyenLabel.setStyle("-fx-text-fill: #4C6FFF; -fx-font-size: 24px; -fx-font-weight: bold;");
        else if (moyenne >= 4)  scoreMoyenLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 24px; -fx-font-weight: bold;");
        else                    scoreMoyenLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 24px; -fx-font-weight: bold;");
    }

    // ─────────────────────────────────────────────────
    //  GRAPHIQUE SCORES — tooltip calcul détaillé
    // ─────────────────────────────────────────────────

    private void afficherGraphiqueScores() {
        scoreChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("ChronoScore");

        for (Map.Entry<LocalDate, List<Repas>> entry : repasParJour.entrySet()) {
            LocalDate date    = entry.getKey();
            List<Repas> repas = entry.getValue();

            double moyenneJour = repas.stream()
                    .mapToInt(r -> Chronoscoreservice.calculerChronoScore(r).getTotalScore())
                    .average().orElse(0);

            XYChart.Data<String, Number> point =
                    new XYChart.Data<>(date.toString(), moyenneJour);
            series.getData().add(point);

            point.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null)
                    ajouterTooltipScore(newNode, date, repas);
            });
        }

        scoreChart.getData().add(series);
        scoreChart.layout();

        for (XYChart.Data<String, Number> data : series.getData()) {
            if (data.getNode() != null) {
                LocalDate date = LocalDate.parse(data.getXValue());
                List<Repas> repas = repasParJour.getOrDefault(date, new ArrayList<>());
                ajouterTooltipScore(data.getNode(), date, repas);
            }
        }
    }

    private void ajouterTooltipScore(Node node, LocalDate date, List<Repas> repas) {
        StringBuilder sb = new StringBuilder();
        sb.append("Date : ").append(date.toString()).append("\n");
        sb.append("________________________________\n\n");

        for (Repas r : repas) {
            ChronoScore sc = Chronoscoreservice.calculerChronoScore(r);

            sb.append("Repas : ").append(r.getTitreRepas())
                    .append("  (").append(r.getTypeMoment())
                    .append(" - ").append(r.getHeureFormatee()).append(")\n\n");

            sb.append("  1. Timing       : ")
                    .append(sc.getTimingScore() >= 0 ? "+" : "")
                    .append(sc.getTimingScore()).append(" pts")
                    .append(construireExplicationTiming(r)).append("\n");

            sb.append("  2. Nutrition    : ")
                    .append(sc.getNutritionScore() >= 0 ? "+" : "")
                    .append(sc.getNutritionScore()).append(" pts")
                    .append("  (").append(r.getTotalCalories()).append(" cal)\n");

            sb.append("  3. Equilibre    : ")
                    .append(sc.getEquilibreBonus() >= 0 ? "+" : "")
                    .append(sc.getEquilibreBonus()).append(" pts")
                    .append(construireExplicationEquilibre(r)).append("\n");

            sb.append("  4. Interaction  : ")
                    .append(sc.getInteractionScore() >= 0 ? "+" : "")
                    .append(sc.getInteractionScore()).append(" pts\n");

            sb.append("  5. Risque       : ")
                    .append(sc.getRiskPenalty()).append(" pts");
            if (sc.getRiskPenalty() < 0)
                sb.append("  -> ").append(sc.getMessageRisque());
            sb.append("\n\n");

            sb.append("  SCORE TOTAL : ")
                    .append(sc.getTotalScore()).append(" / 14")
                    .append("   ").append(sc.getAppreciation()).append("\n");
            sb.append("________________________________\n\n");
        }

        if (repas.size() > 1) {
            double moy = repas.stream()
                    .mapToInt(r -> Chronoscoreservice.calculerChronoScore(r).getTotalScore())
                    .average().orElse(0);
            sb.append("Moyenne du jour : ")
                    .append(String.format("%.1f", moy)).append(" / 14");
        }

        Tooltip tooltip = new Tooltip(sb.toString());
        tooltip.setShowDelay(Duration.millis(80));
        tooltip.setHideDelay(Duration.millis(200));
        tooltip.setStyle(STYLE_TOOLTIP_SCORE);
        Tooltip.install(node, tooltip);

        node.setOnMouseEntered(e -> node.setStyle(
                "-fx-background-color: #4C6FFF;"
                        + "-fx-background-radius: 6;"
                        + "-fx-padding: 6;"));
        node.setOnMouseExited(e -> node.setStyle(""));
    }

    // ─────────────────────────────────────────────────
    //  GRAPHIQUE CALORIES — tooltip macros détaillées
    // ─────────────────────────────────────────────────

    private void afficherGraphiqueCalories() {
        caloriesChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Calories");

        for (Map.Entry<LocalDate, List<Repas>> entry : repasParJour.entrySet()) {
            LocalDate date    = entry.getKey();
            List<Repas> repas = entry.getValue();

            int totalCal = repas.stream().mapToInt(Repas::getTotalCalories).sum();

            XYChart.Data<String, Number> barre =
                    new XYChart.Data<>(date.toString(), totalCal);
            series.getData().add(barre);

            barre.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null)
                    ajouterTooltipCalories(newNode, date, repas);
            });
        }

        caloriesChart.getData().add(series);
        caloriesChart.layout();

        for (XYChart.Data<String, Number> data : series.getData()) {
            if (data.getNode() != null) {
                LocalDate date = LocalDate.parse(data.getXValue());
                List<Repas> repas = repasParJour.getOrDefault(date, new ArrayList<>());
                ajouterTooltipCalories(data.getNode(), date, repas);
            }
        }
    }

    private void ajouterTooltipCalories(Node node, LocalDate date, List<Repas> repas) {
        int    totalCal  = repas.stream().mapToInt(Repas::getTotalCalories).sum();
        double totalProt = repas.stream().mapToDouble(Repas::getTotalProteines).sum();
        double totalGluc = repas.stream().mapToDouble(Repas::getTotalGlucides).sum();
        double totalLip  = repas.stream().mapToDouble(Repas::getTotalLipides).sum();

        StringBuilder sb = new StringBuilder();
        sb.append("Date : ").append(date.toString()).append("\n");
        sb.append("________________________________\n\n");

        sb.append("Calories totales : ").append(totalCal).append(" cal\n");
        sb.append("Proteines        : ").append(String.format("%.1f", totalProt)).append(" g\n");
        sb.append("Glucides         : ").append(String.format("%.1f", totalGluc)).append(" g\n");
        sb.append("Lipides          : ").append(String.format("%.1f", totalLip)).append(" g\n");

        if (totalCal > 0) {
            double pctProt = (totalProt * 4) / totalCal * 100;
            double pctGluc = (totalGluc * 4) / totalCal * 100;
            double pctLip  = (totalLip  * 9) / totalCal * 100;

            sb.append("\nRepartition macros :\n");
            sb.append("  Proteines : ").append(String.format("%.0f%%", pctProt))
                    .append(pctProt >= 15 && pctProt <= 25 ? "  OK" : "  A revoir").append("\n");
            sb.append("  Glucides  : ").append(String.format("%.0f%%", pctGluc))
                    .append(pctGluc >= 45 && pctGluc <= 55 ? "  OK" : "  A revoir").append("\n");
            sb.append("  Lipides   : ").append(String.format("%.0f%%", pctLip))
                    .append(pctLip >= 25 && pctLip <= 35 ? "  OK" : "  A revoir").append("\n");
        }

        sb.append("\nDetail par repas :\n");
        for (Repas r : repas) {
            sb.append("  - ").append(r.getTitreRepas())
                    .append(" (").append(r.getTypeMoment()).append(") : ")
                    .append(r.getTotalCalories()).append(" cal\n");
            if (r.getTotalProteines() > 0 || r.getTotalGlucides() > 0) {
                sb.append("    P : ").append(String.format("%.0f", r.getTotalProteines())).append("g")
                        .append("  G : ").append(String.format("%.0f", r.getTotalGlucides())).append("g")
                        .append("  L : ").append(String.format("%.0f", r.getTotalLipides())).append("g\n");
            }
        }

        sb.append("\n");
        if (totalCal > 2500)
            sb.append("Attention : apport calorique eleve (> 2500 cal)");
        else if (totalCal < 1200)
            sb.append("Attention : apport insuffisant (< 1200 cal)");
        else
            sb.append("Apport calorique dans la normale");

        Tooltip tooltip = new Tooltip(sb.toString());
        tooltip.setShowDelay(Duration.millis(80));
        tooltip.setHideDelay(Duration.millis(200));
        tooltip.setStyle(STYLE_TOOLTIP_CALORIES);
        Tooltip.install(node, tooltip);

        node.setOnMouseEntered(e -> node.setStyle("-fx-bar-fill: #27ae60;"));
        node.setOnMouseExited(e  -> node.setStyle("-fx-bar-fill: #e67e22;"));
    }

    // ─────────────────────────────────────────────────
    //  TENDANCE
    // ─────────────────────────────────────────────────

    private void afficherTendance() {
        tendanceCourante = Chronoscoreservice.analyserTendanceHebdomadaire(
                repasList.stream()
                        .map(Chronoscoreservice::calculerChronoScore)
                        .collect(Collectors.toList()));
        tendanceLabel.setText(tendanceCourante);

        if ("CRITIQUE".equals(AlertService.analyserTendanceAlertes(utilisateurId))) {
            tendanceLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        } else {
            tendanceLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        }
    }

    // ─────────────────────────────────────────────────
    //  HELPERS EXPLICATION
    // ─────────────────────────────────────────────────

    private String construireExplicationTiming(Repas repas) {
        int heure = repas.getDateConsommation().getHour();
        return switch (repas.getTypeMoment()) {
            case "MATIN"     -> (heure >= 6 && heure <= 9)
                    ? "  (optimal 6h - 9h)" : "  (hors plage ideale)";
            case "MIDI"      -> (heure >= 12 && heure <= 13)
                    ? "  (optimal 12h - 13h)" : "  (hors plage ideale)";
            case "COLLATION" -> ((heure >= 9 && heure <= 11) || (heure >= 15 && heure <= 17))
                    ? "  (optimal 9h-11h ou 15h-17h)" : "  (hors plage ideale)";
            case "SOIR"      -> (heure >= 18 && heure <= 20)
                    ? "  (optimal 18h - 20h)"
                    : (heure >= 22 ? "  -> tres tardif !" : "  (acceptable)");
            default -> "";
        };
    }

    private String construireExplicationEquilibre(Repas repas) {
        if (repas.getTotalCalories() == 0) return "";
        double cal = repas.getTotalCalories();
        double pctP = (repas.getTotalProteines() * 4) / cal * 100;
        double pctG = (repas.getTotalGlucides()  * 4) / cal * 100;
        double pctL = (repas.getTotalLipides()   * 9) / cal * 100;
        return String.format("  (P:%.0f%%  G:%.0f%%  L:%.0f%%)", pctP, pctG, pctL);
    }

    // ─────────────────────────────────────────────────
    //  EXPORT PDF
    // ─────────────────────────────────────────────────

    @FXML
    private void exporterRapportPdf() {
        if (repasList == null || repasList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Export PDF",
                    "Aucune donnee pour cette periode.");
            return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer le rapport");
        fc.setInitialFileName("rapport_nutritionnel_"
                + LocalDate.now().toString().replace("-", "") + ".pdf");
        fc.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File fichier = fc.showSaveDialog(scoreChart.getScene().getWindow());
        if (fichier == null) return;
        try {
            ExportPdfService.exporterRapportNutritionnel(
                    repasList, periodeCombo.getValue(),
                    tendanceCourante, fichier.getAbsolutePath());
            showAlert(Alert.AlertType.INFORMATION, "Export reussi",
                    "PDF genere : " + fichier.getAbsolutePath());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de generer le PDF.\n" + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────

    @FXML
    private void fermer() {
        ((Stage) scoreChart.getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType type, String titre, String contenu) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(contenu);
        alert.showAndWait();
    }
}