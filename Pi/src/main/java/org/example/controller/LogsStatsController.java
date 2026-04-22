package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.example.model.User;
import org.example.service.ActivityLogService;
import org.example.service.NavigationService;

import java.util.*;

public class LogsStatsController {

    // ======= Stats cards =======
    @FXML private Label totalLogsLabel;
    @FXML private Label todayLogsLabel;
    @FXML private Label weekLogsLabel;
    @FXML private Label monthLogsLabel;

    // ======= Canvases =======
    @FXML private Canvas activityCanvas;
    @FXML private Canvas donutCanvas;
    @FXML private Canvas hourlyCanvas;
    @FXML private Canvas actionsCanvas;

    // ======= Top 5 users container =======
    @FXML private VBox topUsersContainer;

    private User currentUser;
    private final ActivityLogService statsService = ActivityLogService.getInstance();
    private final NavigationService navigationService = NavigationService.getInstance();

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadStats();
    }

    @FXML
    public void initialize() {
        // Charts will be drawn once data is loaded via setCurrentUser
    }

    private void loadStats() {
        // Cards
        totalLogsLabel.setText(String.valueOf(statsService.getTotalLogs()));
        todayLogsLabel.setText(String.valueOf(statsService.getLogsToday()));
        weekLogsLabel.setText(String.valueOf(statsService.getLogsThisWeek()));
        monthLogsLabel.setText(String.valueOf(statsService.getLogsThisMonth()));

        // Charts
        drawActivityChart();
        drawDonutChart();
        drawTop5Users();
        drawHourlyChart();
        drawActionsChart();
    }

    // ============================================================
    //  LINE CHART – Activité par jour (30 derniers jours)
    // ============================================================
    private void drawActivityChart() {
        Map<String, Integer> data = statsService.getActivityLast30Days();
        GraphicsContext gc = activityCanvas.getGraphicsContext2D();
        double w = activityCanvas.getWidth();
        double h = activityCanvas.getHeight();
        gc.clearRect(0, 0, w, h);

        if (data.isEmpty()) { drawEmpty(gc, w, h, "Aucune donnée"); return; }

        int maxVal = data.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        if (maxVal == 0) maxVal = 1;

        double padL = 36, padR = 16, padT = 16, padB = 40;
        double chartW = w - padL - padR;
        double chartH = h - padT - padB;

        // Grid lines
        gc.setStroke(Color.web("#e5e7eb"));
        gc.setLineWidth(1);
        int gridLines = 5;
        for (int i = 0; i <= gridLines; i++) {
            double y = padT + chartH - (i * chartH / gridLines);
            gc.strokeLine(padL, y, padL + chartW, y);
            gc.setFill(Color.web("#9ca3af"));
            gc.setFont(javafx.scene.text.Font.font(9));
            gc.fillText(String.valueOf(i * maxVal / gridLines), 2, y + 4);
        }

        List<String> keys = new ArrayList<>(data.keySet());
        List<Integer> vals = new ArrayList<>(data.values());
        int n = keys.size();
        if (n == 0) return;

        double step = chartW / Math.max(n - 1, 1);

        // Fill area
        double[] xPoints = new double[n + 2];
        double[] yPoints = new double[n + 2];
        for (int i = 0; i < n; i++) {
            xPoints[i] = padL + i * step;
            yPoints[i] = padT + chartH - ((double) vals.get(i) / maxVal) * chartH;
        }
        xPoints[n] = padL + (n - 1) * step;
        xPoints[n + 1] = padL;
        yPoints[n] = padT + chartH;
        yPoints[n + 1] = padT + chartH;
        gc.setFill(Color.web("#818cf8", 0.15));
        gc.fillPolygon(xPoints, yPoints, n + 2);

        // Line
        gc.setStroke(Color.web("#6366f1"));
        gc.setLineWidth(2.5);
        gc.beginPath();
        for (int i = 0; i < n; i++) {
            double x = padL + i * step;
            double y = padT + chartH - ((double) vals.get(i) / maxVal) * chartH;
            if (i == 0) gc.moveTo(x, y); else gc.lineTo(x, y);
        }
        gc.stroke();

        // Dots + labels
        for (int i = 0; i < n; i++) {
            double x = padL + i * step;
            double y = padT + chartH - ((double) vals.get(i) / maxVal) * chartH;
            gc.setFill(Color.web("#6366f1"));
            gc.fillOval(x - 4, y - 4, 8, 8);
            gc.setFill(Color.WHITE);
            gc.fillOval(x - 2, y - 2, 4, 4);

            // X axis label (every 5th)
            if (i % 5 == 0 || i == n - 1) {
                gc.setFill(Color.web("#6b7280"));
                gc.setFont(javafx.scene.text.Font.font(9));
                gc.fillText(keys.get(i), x - 12, padT + chartH + 14);
            }
        }
    }

    // ============================================================
    //  DONUT CHART – Répartition par rôle
    // ============================================================
    private void drawDonutChart() {
        Map<String, Integer> data = statsService.getCountByRole();
        GraphicsContext gc = donutCanvas.getGraphicsContext2D();
        double w = donutCanvas.getWidth();
        double h = donutCanvas.getHeight();
        gc.clearRect(0, 0, w, h);

        if (data.isEmpty()) { drawEmpty(gc, w, h, "Aucune donnée"); return; }

        int total = data.values().stream().mapToInt(Integer::intValue).sum();
        if (total == 0) { drawEmpty(gc, w, h, "Aucune donnée"); return; }

        String[] colors = {"#6366f1", "#10b981", "#f59e0b", "#3b82f6", "#ef4444"};
        double cx = w / 2 - 10, cy = h / 2;
        double outerR = Math.min(cx, cy) - 10;
        double innerR = outerR * 0.55;

        List<String> keys = new ArrayList<>(data.keySet());
        double startAngle = -90;
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            double sweep = 360.0 * data.get(key) / total;
            gc.setFill(Color.web(colors[i % colors.length]));
            gc.fillArc(cx - outerR, cy - outerR, outerR * 2, outerR * 2, startAngle, sweep, javafx.scene.shape.ArcType.ROUND);
            startAngle += sweep;
        }
        // Center hole
        gc.setFill(Color.WHITE);
        gc.fillOval(cx - innerR, cy - innerR, innerR * 2, innerR * 2);

        // Legend (right side)
        double legX = w - 120;
        double legY = cy - (keys.size() * 18) / 2.0;
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            gc.setFill(Color.web(colors[i % colors.length]));
            gc.fillOval(legX, legY + i * 22, 10, 10);
            gc.setFill(Color.web("#374151"));
            gc.setFont(javafx.scene.text.Font.font(11));
            gc.fillText(key, legX + 16, legY + i * 22 + 9);
        }
    }

    // ============================================================
    //  TOP 5 USERS – vertical list
    // ============================================================
    private void drawTop5Users() {
        if (topUsersContainer == null) return;
        topUsersContainer.getChildren().clear();

        Map<String, Integer> data = statsService.getTop5Users();
        String[] avatarColors = {"#6366f1", "#8b5cf6", "#06b6d4", "#10b981", "#f59e0b"};
        int rank = 1;

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            HBox row = new HBox(14);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            row.setStyle("-fx-padding: 10 16 10 16; -fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 4, 0, 0, 1);");

            // Rank circle
            javafx.scene.layout.StackPane rankPane = new javafx.scene.layout.StackPane();
            javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(16);
            circle.setFill(Color.web(avatarColors[(rank - 1) % avatarColors.length]));
            Label rankLbl = new Label(String.valueOf(rank));
            rankLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white;");
            rankPane.getChildren().addAll(circle, rankLbl);

            // Name
            Label nameLbl = new Label(entry.getKey());
            nameLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #111827;");
            HBox.setHgrow(nameLbl, javafx.scene.layout.Priority.ALWAYS);

            // Count badge
            Label countLbl = new Label(entry.getValue() + " actions");
            countLbl.setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #065f46; -fx-background-radius: 12; -fx-padding: 4 12; -fx-font-size: 11px; -fx-font-weight: bold;");

            row.getChildren().addAll(rankPane, nameLbl, countLbl);
            topUsersContainer.getChildren().add(row);
            rank++;
        }
    }

    // ============================================================
    //  BAR CHART – Distribution horaire
    // ============================================================
    private void drawHourlyChart() {
        int[] data = statsService.getHourlyDistribution();
        GraphicsContext gc = hourlyCanvas.getGraphicsContext2D();
        double w = hourlyCanvas.getWidth();
        double h = hourlyCanvas.getHeight();
        gc.clearRect(0, 0, w, h);

        int maxVal = 0;
        for (int v : data) if (v > maxVal) maxVal = v;
        if (maxVal == 0) { drawEmpty(gc, w, h, "Aucune donnée"); return; }

        double padL = 28, padR = 10, padT = 10, padB = 28;
        double chartW = w - padL - padR;
        double chartH = h - padT - padB;
        double barW = chartW / 24 * 0.7;
        double gap = chartW / 24;

        for (int i = 0; i < 24; i++) {
            double barH = ((double) data[i] / maxVal) * chartH;
            double x = padL + i * gap + (gap - barW) / 2;
            double y = padT + chartH - barH;
            gc.setFill(Color.web("#3b82f6"));
            gc.fillRoundRect(x, y, barW, barH, 4, 4);

            // X label every 2 hours
            if (i % 2 == 0) {
                gc.setFill(Color.web("#6b7280"));
                gc.setFont(javafx.scene.text.Font.font(8));
                gc.fillText(String.format("%02d:00", i), x - 4, padT + chartH + 16);
            }
        }

        // Y axis labels
        gc.setFill(Color.web("#9ca3af"));
        gc.setFont(javafx.scene.text.Font.font(9));
        for (int i = 0; i <= 4; i++) {
            double y = padT + chartH - (i * chartH / 4);
            gc.fillText(String.valueOf(i * maxVal / 4), 2, y + 4);
        }
    }

    // ============================================================
    //  HORIZONTAL BAR CHART – Top 5 actions
    // ============================================================
    private void drawActionsChart() {
        Map<String, Integer> data = statsService.getTop5Actions();
        GraphicsContext gc = actionsCanvas.getGraphicsContext2D();
        double w = actionsCanvas.getWidth();
        double h = actionsCanvas.getHeight();
        gc.clearRect(0, 0, w, h);

        if (data.isEmpty()) { drawEmpty(gc, w, h, "Aucune donnée"); return; }

        int maxVal = data.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        if (maxVal == 0) maxVal = 1;

        String[] barColors = {"#10b981", "#3b82f6", "#f59e0b", "#8b5cf6", "#ef4444"};
        double padL = 170, padR = 60, padT = 10, padB = 10;
        double chartW = w - padL - padR;
        double chartH = h - padT - padB;

        List<String> keys = new ArrayList<>(data.keySet());
        int n = keys.size();
        double barH = (chartH / n) * 0.5;
        double gap = chartH / n;

        for (int i = 0; i < n; i++) {
            String key = keys.get(i);
            int val = data.get(key);
            double bw = ((double) val / maxVal) * chartW;
            double y = padT + i * gap + (gap - barH) / 2;

            // Label (left)
            gc.setFill(Color.web("#374151"));
            gc.setFont(javafx.scene.text.Font.font(11));
            String label = key.length() > 22 ? key.substring(0, 22) : key;
            gc.fillText(label, 4, y + barH / 2 + 4);

            // Bar
            gc.setFill(Color.web(barColors[i % barColors.length]));
            gc.fillRoundRect(padL, y, bw, barH, 4, 4);

            // Value label (right)
            gc.setFill(Color.web("#374151"));
            gc.setFont(javafx.scene.text.Font.font(11));
            gc.fillText(String.valueOf(val), padL + bw + 6, y + barH / 2 + 4);
        }
    }

    private void drawEmpty(GraphicsContext gc, double w, double h, String msg) {
        gc.setFill(Color.web("#9ca3af"));
        gc.setFont(javafx.scene.text.Font.font(13));
        gc.fillText(msg, w / 2 - 40, h / 2);
    }

    @FXML
    private void handleRetourLogs(MouseEvent event) {
        navigationService.navigateToLogs(totalLogsLabel, currentUser);
    }
}
