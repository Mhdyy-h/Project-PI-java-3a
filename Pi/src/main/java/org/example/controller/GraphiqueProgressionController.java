package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.example.model.SeanceSport;
import org.example.model.User;
import org.example.service.CoachUserService;

import java.io.IOException;
import java.util.List;

public class GraphiqueProgressionController {

    @FXML private VBox   chartsContainer;
    @FXML private Label  lblNom;
    @FXML private Label  lblTotalSeances;
    @FXML private Label  lblDureeMoy;
    @FXML private Label  lblNiveau;

    private CoachUserService coachUserService;
    private User             currentUser;

    @FXML
    public void initialize() {
        coachUserService = new CoachUserService();
    }

    public void setUser(User user) {
        this.currentUser = user;
        chargerGraphiques();
    }

    private void chargerGraphiques() {
        List<SeanceSport> seances =
                coachUserService.getSeancesUser(currentUser.getId());

        // Stats
        int total = seances.size();
        double dureeMoy = seances.stream()
                .mapToInt(SeanceSport::getDureeMinutes)
                .average().orElse(0);
        String niveau = total >= 20 ? "🔥 Expert" :
                total >= 10 ? "💪 Intermédiaire" :
                total >= 5  ? "📈 Débutant+" : "🌱 Débutant";

        lblNom.setText("📊 Progression de " + currentUser.getNomComplet());
        lblTotalSeances.setText(String.valueOf(total));
        lblDureeMoy.setText(String.format("%.0f min", dureeMoy));
        lblNiveau.setText(niveau);

        chartsContainer.getChildren().clear();

        if (seances.isEmpty()) {
            Label vide = new Label("Aucune séance pour afficher les graphiques");
            vide.setStyle("-fx-text-fill: #888888; -fx-font-size: 13px;");
            chartsContainer.getChildren().add(vide);
            return;
        }

        // Ligne 1 — 2 graphiques côte à côte
        HBox ligne1 = new HBox(16);
        ligne1.getChildren().addAll(
                creerGraphiqueDuree(seances),
                creerGraphiqueBarres(seances)
        );

        // Ligne 2 — 2 graphiques côte à côte
        HBox ligne2 = new HBox(16);
        ligne2.getChildren().addAll(
                creerGraphiqueMedailles(seances),
                creerGraphiqueProgression(seances)
        );

        chartsContainer.getChildren().addAll(ligne1, ligne2);
    }

    // ── GRAPHIQUE 1 — Courbe durée par séance ────────────────
    private VBox creerGraphiqueDuree(List<SeanceSport> seances) {
        VBox box = creerBoiteGraphique("📈 Durée par séance (min)");
        Canvas canvas = new Canvas(380, 200);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        int n = Math.min(seances.size(), 10);
        if (n < 2) {
            gc.setFill(Color.web("#888888"));
            gc.fillText("Pas assez de données", 120, 100);
            box.getChildren().add(canvas);
            return box;
        }

        double maxDuree = seances.stream()
                .limit(n).mapToInt(SeanceSport::getDureeMinutes).max().orElse(1);

        double padL = 40, padR = 20, padT = 20, padB = 30;
        double w = canvas.getWidth() - padL - padR;
        double h = canvas.getHeight() - padT - padB;

        // Grille
        gc.setStroke(Color.web("#2a2a3e"));
        gc.setLineWidth(1);
        for (int i = 0; i <= 4; i++) {
            double y = padT + h - (i / 4.0) * h;
            gc.strokeLine(padL, y, padL + w, y);
            gc.setFill(Color.web("#666666"));
            gc.fillText(String.format("%.0f", (i / 4.0) * maxDuree), 2, y + 4);
        }

        // Aire sous la courbe
        double[] xs = new double[n];
        double[] ys = new double[n];
        for (int i = 0; i < n; i++) {
            xs[i] = padL + (i / (double)(n - 1)) * w;
            ys[i] = padT + h - (seances.get(i).getDureeMinutes() / maxDuree) * h;
        }

        // Remplissage
        gc.setFill(Color.web("#7d3c98", 0.2));
        gc.beginPath();
        gc.moveTo(xs[0], padT + h);
        for (int i = 0; i < n; i++) gc.lineTo(xs[i], ys[i]);
        gc.lineTo(xs[n-1], padT + h);
        gc.closePath();
        gc.fill();

        // Ligne
        gc.setStroke(Color.web("#7d3c98"));
        gc.setLineWidth(2.5);
        gc.beginPath();
        gc.moveTo(xs[0], ys[0]);
        for (int i = 1; i < n; i++) gc.lineTo(xs[i], ys[i]);
        gc.stroke();

        // Points
        for (int i = 0; i < n; i++) {
            gc.setFill(Color.web("#7d3c98"));
            gc.fillOval(xs[i] - 4, ys[i] - 4, 8, 8);
            gc.setFill(Color.WHITE);
            gc.fillOval(xs[i] - 2, ys[i] - 2, 4, 4);
        }

        box.getChildren().add(canvas);
        return box;
    }

    // ── GRAPHIQUE 2 — Barres séances par semaine ─────────────
    private VBox creerGraphiqueBarres(List<SeanceSport> seances) {
        VBox box = creerBoiteGraphique("📊 Séances par semaine");
        Canvas canvas = new Canvas(380, 200);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Compter séances des 6 dernières semaines
        int[] parSemaine = new int[6];
        for (SeanceSport s : seances) {
            try {
                java.time.LocalDate date =
                        java.time.LocalDate.parse(s.getDateSeance());
                java.time.LocalDate now = java.time.LocalDate.now();
                long diff = java.time.temporal.ChronoUnit.WEEKS.between(date, now);
                if (diff >= 0 && diff < 6) {
                    parSemaine[(int) diff]++;
                }
            } catch (Exception ignored) {}
        }

        int max = 1;
        for (int v : parSemaine) if (v > max) max = v;

        double padL = 30, padR = 10, padT = 20, padB = 30;
        double w = canvas.getWidth() - padL - padR;
        double h = canvas.getHeight() - padT - padB;
        double barW = w / 6 - 8;

        String[] labels = {"S-5","S-4","S-3","S-2","S-1","Cette sem."};
        String[] colors = {"#7d3c98","#9b59b6","#8e44ad","#6c3483","#a569bd","#44cc88"};

        for (int i = 0; i < 6; i++) {
            double x = padL + i * (w / 6) + 4;
            double barH = (parSemaine[5 - i] / (double) max) * h;
            double y = padT + h - barH;

            // Barre
            gc.setFill(Color.web(colors[i]));
            gc.fillRoundRect(x, y, barW, barH, 6, 6);

            // Valeur
            if (parSemaine[5 - i] > 0) {
                gc.setFill(Color.WHITE);
                gc.fillText(String.valueOf(parSemaine[5 - i]),
                        x + barW / 2 - 4, y - 4);
            }

            // Label
            gc.setFill(Color.web("#888888"));
            gc.fillText(labels[i], x, padT + h + 18);
        }

        box.getChildren().add(canvas);
        return box;
    }

    // ── GRAPHIQUE 3 — Camembert médailles ────────────────────
    private VBox creerGraphiqueMedailles(List<SeanceSport> seances) {
        VBox box = creerBoiteGraphique("🥇 Répartition médailles");
        Canvas canvas = new Canvas(380, 200);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        long or     = seances.stream().filter(s -> "Or".equalsIgnoreCase(s.getMedailleObtenue())).count();
        long argent = seances.stream().filter(s -> "Argent".equalsIgnoreCase(s.getMedailleObtenue())).count();
        long bronze = seances.stream().filter(s -> "Bronze".equalsIgnoreCase(s.getMedailleObtenue())).count();
        long aucune = seances.size() - or - argent - bronze;
        long total  = seances.size();

        if (total == 0) {
            gc.setFill(Color.web("#888888"));
            gc.fillText("Aucune donnée", 140, 100);
            box.getChildren().add(canvas);
            return box;
        }

        double cx = 110, cy = 100, r = 75;
        double[] vals   = {or, argent, bronze, aucune};
        String[] colors = {"#f1c40f","#95a5a6","#cd6133","#2a2a3e"};
        String[] labels = {"Or","Argent","Bronze","Aucune"};
        double startAngle = -90;

        for (int i = 0; i < 4; i++) {
            if (vals[i] == 0) continue;
            double angle = (vals[i] / (double) total) * 360;
            gc.setFill(Color.web(colors[i]));
            gc.fillArc(cx - r, cy - r, r * 2, r * 2,
                    startAngle, angle,
                    javafx.scene.shape.ArcType.ROUND);
            startAngle += angle;
        }

        // Légende
        double lx = 210, ly = 40;
        for (int i = 0; i < 4; i++) {
            if (vals[i] == 0) continue;
            gc.setFill(Color.web(colors[i]));
            gc.fillRoundRect(lx, ly, 14, 14, 4, 4);
            gc.setFill(Color.web("#cccccc"));
            gc.fillText(labels[i] + " : " + (int)vals[i], lx + 20, ly + 12);
            ly += 26;
        }

        box.getChildren().add(canvas);
        return box;
    }

    // ── GRAPHIQUE 4 — Score progression ──────────────────────
    private VBox creerGraphiqueProgression(List<SeanceSport> seances) {
        VBox box = creerBoiteGraphique("⭐ Score de progression");
        Canvas canvas = new Canvas(380, 200);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        int total    = seances.size();
        double duree = seances.stream().mapToInt(SeanceSport::getDureeMinutes).average().orElse(0);
        long or      = seances.stream().filter(s -> "Or".equalsIgnoreCase(s.getMedailleObtenue())).count();

        // Scores par critère
        double scoreFreq   = Math.min(total / 20.0, 1.0) * 10;
        double scoreIntens = Math.min(duree / 90.0, 1.0) * 10;
        double scorePerf   = Math.min(or / (double) Math.max(total, 1), 1.0) * 10;
        double scoreGlobal = (scoreFreq + scoreIntens + scorePerf) / 3;

        String[] criteres = {"Fréquence","Intensité","Performance","Global"};
        double[] scores   = {scoreFreq, scoreIntens, scorePerf, scoreGlobal};
        String[] colors   = {"#3498db","#e67e22","#e74c3c","#44cc88"};

        double padL = 90, padR = 60, padT = 20;
        double barMaxW = canvas.getWidth() - padL - padR;
        double barH = 22, gap = 16;

        for (int i = 0; i < 4; i++) {
            double y = padT + i * (barH + gap);

            // Label
            gc.setFill(Color.web("#aaaaaa"));
            gc.fillText(criteres[i], 4, y + barH - 6);

            // Fond barre
            gc.setFill(Color.web("#2a2a3e"));
            gc.fillRoundRect(padL, y, barMaxW, barH, 8, 8);

            // Barre score
            double bw = (scores[i] / 10.0) * barMaxW;
            gc.setFill(Color.web(colors[i]));
            gc.fillRoundRect(padL, y, bw, barH, 8, 8);

            // Score texte
            gc.setFill(Color.WHITE);
            gc.fillText(String.format("%.1f/10", scores[i]),
                    padL + barMaxW + 6, y + barH - 6);
        }

        box.getChildren().add(canvas);
        return box;
    }

    // ── Boite graphique commune ───────────────────────────────
    private VBox creerBoiteGraphique(String titre) {
        VBox box = new VBox(10);
        box.setStyle(
                "-fx-background-color: #1a1a2e;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 16;" +
                        "-fx-border-color: #2a2a3e;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-width: 1;");
        HBox.setHgrow(box, Priority.ALWAYS);

        Label lbl = new Label(titre);
        lbl.setStyle(
                "-fx-text-fill: #7d3c98;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;");
        box.getChildren().add(lbl);
        return box;
    }

    @FXML
    private void retourMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/MenuUser.fxml"));
            Parent root = loader.load();
            MenuUserController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            chartsContainer.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}