package org.example.controller;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.example.dao.ActivityLogDAO;
import org.example.model.ActivityLog;
import org.example.model.User;
import org.example.service.NavigationService;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class LogsController {

    @FXML private Label titleLabel;
    @FXML private VBox logsContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilter;
    @FXML private ComboBox<String> actionFilter;
    @FXML private ComboBox<String> periodeFilter;
    @FXML private Label statusLabel;
    @FXML private Label totalLogsLabel;

    private User currentUser;
    private List<ActivityLog> allLogs;
    private final NavigationService navigationService = NavigationService.getInstance();

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadLogs();
    }

    @FXML
    public void initialize() {
        setupFilters();
    }

    private void setupFilters() {
        if (roleFilter != null) {
            roleFilter.getItems().addAll(
                "Tous les rôles", "ROLE_ADMIN", "ROLE_COACH", "ROLE_SPECIALISTE", "ROLE_USER"
            );
            roleFilter.setValue("Tous les rôles");
            roleFilter.setOnAction(e -> applyFilters());
        }
        if (actionFilter != null) {
            actionFilter.getItems().addAll(
                "Toutes les actions", "Connexion réussie", "Connexion échouée",
                "Déconnexion", "Inscription", "Connexion Face ID", "Changement de mot de passe"
            );
            actionFilter.setValue("Toutes les actions");
            actionFilter.setOnAction(e -> applyFilters());
        }
        if (periodeFilter != null) {
            periodeFilter.getItems().addAll(
                "Toutes les périodes", "Aujourd'hui", "Cette semaine", "Ce mois", "Cette année"
            );
            periodeFilter.setValue("Toutes les périodes");
            periodeFilter.setOnAction(e -> applyFilters());
        }
    }

    private void loadLogs() {
        allLogs = ActivityLogDAO.getAllLogs();
        renderLogs(allLogs);
        updateTotalLabel(allLogs.size());
    }

    private void renderLogs(List<ActivityLog> logs) {
        logsContainer.getChildren().clear();

        if (logs.isEmpty()) {
            Label empty = new Label("Aucun log trouvé");
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #9ca3af; -fx-padding: 40;");
            logsContainer.getChildren().add(empty);
            return;
        }

        for (int i = 0; i < logs.size(); i++) {
            ActivityLog log = logs.get(i);
            HBox row = buildLogRow(log, i % 2 == 0);
            logsContainer.getChildren().add(row);
        }
    }

    private HBox buildLogRow(ActivityLog log, boolean alternate) {
        HBox row = new HBox();
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 14 24 14 24; -fx-background-color: " +
                (alternate ? "white" : "#f8fafc") + "; -fx-border-color: transparent transparent #f1f5f9 transparent; -fx-border-width: 0 0 1 0;");
        row.setSpacing(0);

        // DATE & HEURE
        Label dateLbl = new Label(log.getDateHeureFormatted());
        dateLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");
        dateLbl.setPrefWidth(165);

        // UTILISATEUR (Nom + Email)
        VBox userBox = new VBox(2);
        userBox.setPrefWidth(220);
        Label nomLbl = new Label(log.getNomUtilisateur());
        nomLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Label emailLbl = new Label(log.getEmail());
        emailLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");
        userBox.getChildren().addAll(nomLbl, emailLbl);

        // RÔLE badge
        Label roleLbl = new Label(log.getDisplayRole());
        roleLbl.setPrefWidth(160);
        roleLbl.setStyle(getRoleBadgeStyle(log.getDisplayRole()));

        // ACTION badge
        Label actionLbl = new Label(log.getAction());
        actionLbl.setStyle(getActionBadgeStyle(log.getAction()));

        row.getChildren().addAll(dateLbl, userBox, roleLbl, actionLbl);
        return row;
    }

    private String getRoleBadgeStyle(String role) {
        String color;
        switch (role) {
            case "ROLE_ADMIN": color = "#fce7f3; -fx-text-fill: #9d174d"; break;
            case "ROLE_COACH": color = "#fef3c7; -fx-text-fill: #92400e"; break;
            case "ROLE_SPECIALISTE": color = "#ffedd5; -fx-text-fill: #c2410c"; break;
            default: color = "#e0e7ff; -fx-text-fill: #3730a3"; break;
        }
        return "-fx-background-color: #" + color.split("#")[1] +
               "; -fx-background-radius: 12; -fx-padding: 4 12; -fx-font-size: 11px; -fx-font-weight: bold;";
    }

    private String getActionBadgeStyle(String action) {
        String bg, fg, dot;
        switch (action) {
            case "Connexion réussie": bg = "#d1fae5"; fg = "#065f46"; dot = "#10b981"; break;
            case "Connexion échouée": bg = "#fee2e2"; fg = "#991b1b"; dot = "#ef4444"; break;
            case "Déconnexion": bg = "#dbeafe"; fg = "#1e40af"; dot = "#3b82f6"; break;
            case "Connexion Face ID": bg = "#f0fdf4"; fg = "#166534"; dot = "#22c55e"; break;
            case "Changement de mot de passe": bg = "#ede9fe"; fg = "#4c1d95"; dot = "#7c3aed"; break;
            case "Inscription": bg = "#ecfdf5"; fg = "#065f46"; dot = "#6ee7b7"; break;
            default: bg = "#f3f4f6"; fg = "#374151"; dot = "#9ca3af"; break;
        }
        return String.format(
            "-fx-background-color: %s; -fx-text-fill: %s; -fx-background-radius: 12; -fx-padding: 4 12; -fx-font-size: 11px; -fx-font-weight: bold;",
            bg, fg
        );
    }

    @FXML
    private void handleSearch(KeyEvent event) {
        applyFilters();
    }

    @FXML
    private void handleFilter(MouseEvent event) {
        applyFilters();
    }

    @FXML
    private void handleReset(MouseEvent event) {
        searchField.clear();
        if (roleFilter != null) roleFilter.setValue("Tous les rôles");
        if (actionFilter != null) actionFilter.setValue("Toutes les actions");
        if (periodeFilter != null) periodeFilter.setValue("Toutes les périodes");
        renderLogs(allLogs);
        updateTotalLabel(allLogs.size());
    }

    private void applyFilters() {
        String search = searchField != null ? searchField.getText().toLowerCase().trim() : "";
        String role = roleFilter != null ? roleFilter.getValue() : null;
        String action = actionFilter != null ? actionFilter.getValue() : null;
        String periode = periodeFilter != null ? periodeFilter.getValue() : null;

        // Calculate period bounds
        LocalDateTime debutDT = null;
        LocalDateTime finDT = null;
        if (periode != null && !periode.equals("Toutes les périodes")) {
            finDT = LocalDateTime.now();
            switch (periode) {
                case "Aujourd'hui": debutDT = LocalDate.now().atStartOfDay(); break;
                case "Cette semaine": debutDT = LocalDate.now().minusDays(7).atStartOfDay(); break;
                case "Ce mois": debutDT = LocalDate.now().minusDays(30).atStartOfDay(); break;
                case "Cette année": debutDT = LocalDate.now().minusDays(365).atStartOfDay(); break;
            }
        }

        final LocalDateTime finalDebut = debutDT;
        final LocalDateTime finalFin = finDT;

        List<ActivityLog> filtered = allLogs.stream()
            .filter(log -> search.isEmpty() ||
                (log.getNomUtilisateur() != null && log.getNomUtilisateur().toLowerCase().contains(search)) ||
                (log.getEmail() != null && log.getEmail().toLowerCase().contains(search)) ||
                (log.getAction() != null && log.getAction().toLowerCase().contains(search)))
            .filter(log -> role == null || role.equals("Tous les rôles") ||
                (log.getRoles() != null && log.getRoles().contains(role.replace("ROLE_", ""))))
            .filter(log -> action == null || action.equals("Toutes les actions") ||
                (log.getAction() != null && log.getAction().equals(action)))
            .filter(log -> finalDebut == null || log.getDateHeure() == null || !log.getDateHeure().isBefore(finalDebut))
            .filter(log -> finalFin == null || log.getDateHeure() == null || !log.getDateHeure().isAfter(finalFin))
            .collect(Collectors.toList());

        renderLogs(filtered);
        updateTotalLabel(filtered.size());
    }

    @FXML
    private void handleEffacerHistorique(MouseEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Effacer l'historique");
        confirm.setHeaderText("Effacer tout l'historique des logs ?");
        confirm.setContentText("Cette action est irréversible.");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            ActivityLogDAO.clearAllLogs();
            loadLogs();
            showStatus("Historique effacé avec succès.", true);
        }
    }

    @FXML
    private void handleStatistiques(MouseEvent event) {
        navigationService.navigateToLogStats(logsContainer, currentUser);
    }

    @FXML
    private void handleExportPdf(MouseEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter les logs en PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
            fileChooser.setInitialFileName("BioSync_Logs_" +
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
            File file = fileChooser.showSaveDialog(logsContainer.getScene().getWindow());
            if (file == null) return;

            // Simple CSV export as fallback (PDF requires iText/OpenPDF)
            exportLogsToPdf(file);
            showStatus("Logs exportés : " + file.getName(), true);
        } catch (Exception e) {
            showStatus("Erreur export : " + e.getMessage(), false);
        }
    }

    private void exportLogsToPdf(File file) throws Exception {
        // Use OpenPDF (already in pom.xml) to generate a simple PDF
        com.lowagie.text.Document doc = new com.lowagie.text.Document();
        com.lowagie.text.pdf.PdfWriter.getInstance(doc, new java.io.FileOutputStream(file));
        doc.open();

        com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 16, com.lowagie.text.Font.BOLD);
        com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.BOLD);
        com.lowagie.text.Font cellFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9);

        doc.add(new com.lowagie.text.Paragraph("Historique des Logs – BioSync", titleFont));
        doc.add(new com.lowagie.text.Paragraph("Exporté le : " + java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), cellFont));
        doc.add(com.lowagie.text.Chunk.NEWLINE);

        com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2.5f, 2.5f, 1.5f, 2f});

        // Header
        for (String h : new String[]{"Date & Heure", "Utilisateur", "Rôle", "Action"}) {
            com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(h, headerFont));
            cell.setBackgroundColor(new java.awt.Color(76, 111, 255));
            cell.setPadding(6);
            table.addCell(cell);
        }

        // Rows
        List<ActivityLog> logsToExport = allLogs != null ? allLogs : ActivityLogDAO.getAllLogs();
        for (ActivityLog log : logsToExport) {
            table.addCell(new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(log.getDateHeureFormatted(), cellFont)));
            table.addCell(new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(log.getNomUtilisateur() + "\n" + log.getEmail(), cellFont)));
            table.addCell(new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(log.getDisplayRole(), cellFont)));
            table.addCell(new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(log.getAction(), cellFont)));
        }

        doc.add(table);
        doc.close();
    }

    @FXML
    private void handleRetour(MouseEvent event) {
        navigationService.navigateToDashboard(logsContainer, currentUser);
    }

    private void updateTotalLabel(int count) {
        if (totalLogsLabel != null) {
            totalLogsLabel.setText(count + " log(s) affiché(s)");
        }
    }

    private void showStatus(String msg, boolean success) {
        if (statusLabel != null) {
            statusLabel.setText(msg);
            statusLabel.setStyle("-fx-text-fill: " + (success ? "#10b981" : "#ef4444") + ";");
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(e -> statusLabel.setText(""));
            pause.play();
        }
    }
}
