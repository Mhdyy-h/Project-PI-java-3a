package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.dao.AlerteDAO;
import org.example.dao.CertificationDAO;
import org.example.dao.RepasDAO;
import org.example.dao.UserDAO;
import org.example.model.Alerte;
import org.example.model.User;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AdminController {

    // ── Header ──
    @FXML private Label adminNameLabel;
    @FXML private Label adminInitialLabel;
    @FXML private Label adminRoleLabel;
    @FXML private Label dateLabel;

    // ── Stats cards ──
    @FXML private Label activeUsersLabel;
    @FXML private Label usersGrowthLabel;
    @FXML private Label certCountLabel;
    @FXML private Label totalRepasLabel;
    @FXML private Label repasJourLabel;
    @FXML private Label alertesCountLabel;
    @FXML private Label alertesRougeLabel;

    // ── Sidebar ──
    @FXML private Label sidebarAlertesLabel;
    @FXML private HBox  nutritionNavItem;

    // ── Graphiques ──
    @FXML private BarChart<String, Number> rolesChart;
    @FXML private BarChart<String, Number> alertesChart;

    // ── Utilisateurs récents ──
    @FXML private VBox  recentUsersContainer;
    @FXML private Label statusLabel;

    // ── Alertes récentes ──
    @FXML private VBox  recentAlertesContainer;
    @FXML private Label noAlertesLabel;
    @FXML private Label alertesBadge;

    private User currentUser;

    // ─────────────────────────────────────────────────
    //  POINT D'ENTRÉE
    // ─────────────────────────────────────────────────

    public void setUser(User user) {
        this.currentUser = user;
        updateHeader();
        chargerTout();
    }

    @FXML
    private void handleRefresh() {
        chargerTout();
    }

    @FXML
    private void handleScrollToAlertes() {
        // Simple scroll hint : le click sur Alertes dans la sidebar recharge juste
        chargerAlertes();
    }

    private void chargerTout() {
        chargerStats();
        chargerGraphiqueRoles();
        chargerGraphiqueAlertes();
        chargerUtilisateursRecents();
        chargerAlertes();
    }

    // ─────────────────────────────────────────────────
    //  HEADER
    // ─────────────────────────────────────────────────

    private void updateHeader() {
        if (currentUser == null) return;

        adminNameLabel.setText(currentUser.getNomComplet());

        // Initiales
        String name = currentUser.getNomComplet();
        String initials = "";
        if (name != null && !name.isEmpty()) {
            String[] parts = name.trim().split("\\s+");
            initials = parts.length >= 2
                    ? (String.valueOf(parts[0].charAt(0)) + String.valueOf(parts[1].charAt(0))).toUpperCase()
                    : name.substring(0, Math.min(2, name.length())).toUpperCase();
        }
        adminInitialLabel.setText(initials);

        // Rôle affiché
        String role = extractDisplayRole(currentUser.getRoles());
        adminRoleLabel.setText(role);

        // Date
        dateLabel.setText("Aujourd'hui, " +
                LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRENCH)));
    }

    // ─────────────────────────────────────────────────
    //  STATS CARDS
    // ─────────────────────────────────────────────────

    private void chargerStats() {
        try {
            List<User> users = UserDAO.getAllUsers();
            activeUsersLabel.setText(String.valueOf(users.size()));

            long roleUser = users.stream().filter(u -> {
                String r = u.getRoles();
                return r == null || (!r.contains("ADMIN") && !r.contains("COACH") && !r.contains("SPECIALISTE"));
            }).count();
            usersGrowthLabel.setText("👤 " + roleUser + " utilisateurs · "
                    + users.stream().filter(u -> u.getRoles() != null && u.getRoles().contains("COACH")).count()
                    + " coachs");

        } catch (Exception e) {
            activeUsersLabel.setText("?");
        }

        try {
            int certPending = CertificationDAO.countPending();
            certCountLabel.setText(String.valueOf(certPending));
        } catch (Exception ignored) {}

        // Total repas – 2 requêtes SQL directes (pas de N+1)
        try {
            int totalRepas = RepasDAO.countAll();
            totalRepasLabel.setText(String.valueOf(totalRepas));

            int repasJour = RepasDAO.countToday();
            repasJourLabel.setText("🍽️ " + repasJour + " repas enregistré"
                    + (repasJour > 1 ? "s" : "") + " aujourd'hui");
        } catch (Exception ignored) {}

        // Alertes
        try {
            int total = AlerteDAO.countAllRecent(7);
            alertesCountLabel.setText(String.valueOf(total));
            sidebarAlertesLabel.setText(total + " actives");
            alertesBadge.setText(String.valueOf(total));

            List<Alerte> alertes = AlerteDAO.getAllRecent(7);
            long rouge = alertes.stream().filter(a -> "ROUGE".equals(a.getCriticite())).count();
            alertesRougeLabel.setText("🔴 " + rouge + " critiques · 🟡 " + (total - rouge) + " modérées");
        } catch (Exception ignored) {}
    }

    // ─────────────────────────────────────────────────
    //  GRAPHIQUE RÔLES
    // ─────────────────────────────────────────────────

    private void chargerGraphiqueRoles() {
        try {
            List<User> users = UserDAO.getAllUsers();

            long nbUsers  = users.stream().filter(u -> {
                String r = u.getRoles();
                return r == null || (!r.contains("ADMIN") && !r.contains("COACH") && !r.contains("SPECIALISTE"));
            }).count();
            long nbCoach  = users.stream().filter(u -> u.getRoles() != null && u.getRoles().contains("COACH")).count();
            long nbAdmin  = users.stream().filter(u -> u.getRoles() != null && u.getRoles().contains("ADMIN")).count();
            long nbSpec   = users.stream().filter(u -> u.getRoles() != null && u.getRoles().contains("SPECIALISTE")).count();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Utilisateurs");
            series.getData().add(new XYChart.Data<>("Utilisateurs", nbUsers));
            series.getData().add(new XYChart.Data<>("Coachs", nbCoach));
            series.getData().add(new XYChart.Data<>("Admins", nbAdmin));
            series.getData().add(new XYChart.Data<>("Spécialistes", nbSpec));

            rolesChart.getData().clear();
            rolesChart.getData().add(series);

            // Colorisation des barres après layout
            rolesChart.layout();
            String[] colors = {"#4C6FFF", "#10b981", "#ef4444", "#f59e0b"};
            for (int i = 0; i < series.getData().size(); i++) {
                XYChart.Data<String, Number> data = series.getData().get(i);
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: " + colors[i % colors.length] + ";");
                }
                final int idx = i;
                data.nodeProperty().addListener((obs, old, node) -> {
                    if (node != null)
                        node.setStyle("-fx-bar-fill: " + colors[idx % colors.length] + ";");
                });
            }
        } catch (Exception ignored) {}
    }

    // ─────────────────────────────────────────────────
    //  GRAPHIQUE ALERTES PAR JOUR
    // ─────────────────────────────────────────────────

    private void chargerGraphiqueAlertes() {
        try {
            List<Alerte> alertes = AlerteDAO.getAllRecent(7);

            // Grouper par jour
            Map<LocalDate, Long> parJour = alertes.stream().collect(
                    Collectors.groupingBy(
                            a -> a.getDateAlerte().toLocalDate(),
                            TreeMap::new,
                            Collectors.counting()));

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Alertes");

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
            // Remplir les 7 derniers jours (même si 0)
            for (int i = 6; i >= 0; i--) {
                LocalDate jour = LocalDate.now().minusDays(i);
                long count = parJour.getOrDefault(jour, 0L);
                series.getData().add(new XYChart.Data<>(jour.format(fmt), count));
            }

            alertesChart.getData().clear();
            alertesChart.getData().add(series);

            // Couleur rouge pour alertes
            alertesChart.layout();
            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null)
                    data.getNode().setStyle("-fx-bar-fill: #e74c3c;");
                data.nodeProperty().addListener((obs, old, node) -> {
                    if (node != null) node.setStyle("-fx-bar-fill: #e74c3c;");
                });
            }
        } catch (Exception ignored) {}
    }

    // ─────────────────────────────────────────────────
    //  UTILISATEURS RÉCENTS
    // ─────────────────────────────────────────────────

    private void chargerUtilisateursRecents() {
        try {
            List<User> users = UserDAO.getAllUsers();
            recentUsersContainer.getChildren().clear();

            int count = Math.min(users.size(), 6);
            for (int i = 0; i < count; i++) {
                recentUsersContainer.getChildren().add(buildUserRow(users.get(i), i % 2 == 0));
            }

            statusLabel.setText(users.isEmpty() ? "Aucun utilisateur trouvé" : "");
        } catch (Exception e) {
            statusLabel.setText("Erreur : " + e.getMessage());
        }
    }

    private HBox buildUserRow(User user, boolean alternate) {
        HBox row = new HBox();
        row.setStyle("-fx-padding: 12 14 12 14; -fx-border-color: transparent transparent #f8fafc transparent; "
                + "-fx-border-width: 0 0 1 0;"
                + (alternate ? "-fx-background-color: white;" : "-fx-background-color: #fafbfd;"));

        Label nameLbl = new Label(user.getNomComplet());
        nameLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #1a1a2e; -fx-font-weight: bold;");
        nameLbl.setPrefWidth(170);

        Label emailLbl = new Label(user.getEmail());
        emailLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #4C6FFF;");
        emailLbl.setPrefWidth(210);

        String role = extractDisplayRole(user.getRoles());
        Label roleLbl = new Label(role);
        roleLbl.setStyle(getRoleBadgeStyle(role));
        roleLbl.setPrefWidth(100);

        Label scoreLbl = new Label(user.getScoreGlobal() > 0 ? String.valueOf(user.getScoreGlobal()) : "—");
        scoreLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280; -fx-font-weight: bold;");
        scoreLbl.setPrefWidth(70);

        row.getChildren().addAll(nameLbl, emailLbl, roleLbl, scoreLbl);
        return row;
    }

    // ─────────────────────────────────────────────────
    //  ALERTES RÉCENTES
    // ─────────────────────────────────────────────────

    private void chargerAlertes() {
        try {
            List<Alerte> alertes = AlerteDAO.getAllRecent(7);
            recentAlertesContainer.getChildren().clear();

            if (alertes.isEmpty()) {
                noAlertesLabel.setVisible(true);
                noAlertesLabel.setManaged(true);
            } else {
                noAlertesLabel.setVisible(false);
                noAlertesLabel.setManaged(false);

                // Afficher les 8 plus récentes
                int limit = Math.min(alertes.size(), 8);
                for (int i = 0; i < limit; i++) {
                    recentAlertesContainer.getChildren().add(buildAlerteCard(alertes.get(i)));
                }
            }
        } catch (Exception ignored) {}
    }

    private HBox buildAlerteCard(Alerte alerte) {
        HBox card = new HBox(12);
        card.setStyle("-fx-background-color: " + (alerte.getCriticite().equals("ROUGE") ? "#fff5f5" : "#fffbf0")
                + "; -fx-background-radius: 10; -fx-padding: 10 14; -fx-alignment: CENTER_LEFT;"
                + "-fx-border-color: " + alerte.getCouleurCriticite() + ";"
                + "-fx-border-width: 0 0 0 3; -fx-border-radius: 0 8 8 0;");

        Label icone = new Label(alerte.getCriticite().equals("ROUGE") ? "🔴" : "🟡");
        icone.setStyle("-fx-font-size: 14px;");

        VBox textes = new VBox(3);
        textes.setStyle("-fx-pref-width: 10000;");

        Label typeLbl = new Label(alerte.getType());
        typeLbl.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: "
                + alerte.getCouleurCriticite() + ";");

        Label msgLbl = new Label(alerte.getMessage());
        msgLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #1a1a2e;");
        msgLbl.setWrapText(true);

        Label dateLbl = new Label(alerte.getDateFormatee());
        dateLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");

        textes.getChildren().addAll(typeLbl, msgLbl, dateLbl);
        card.getChildren().addAll(icone, textes);
        return card;
    }

    // ─────────────────────────────────────────────────
    //  NAVIGATION
    // ─────────────────────────────────────────────────

    @FXML
    private void handleNavUtilisateurs(MouseEvent event) {
        navigateToUtilisateurs();
    }

    @FXML
    private void handleNavCertifications(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/certifications_admin.fxml"));
            Parent root = loader.load();
            CertificationsAdminController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight()));
            stage.setTitle("BioSync – Certifications");
        } catch (IOException e) {
            statusLabel.setText("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void handleGererMembres(MouseEvent event) {
        navigateToUtilisateurs();
    }

    private void navigateToUtilisateurs() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/utilisateurs.fxml"));
            Parent root = loader.load();
            UtilisateursController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight()));
            stage.setTitle("BioSync – Utilisateurs");
        } catch (IOException e) {
            statusLabel.setText("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 400, 550));
            stage.setTitle("BioSync – Connexion");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ===== NUTRITION =====

    @FXML
    private void handleNavNutrition(MouseEvent event) {
        if (currentUser != null && currentUser.getRoles() != null
                && currentUser.getRoles().contains("COACH")) {
            ouvrirVueCoach();
        } else {
            ouvrirVueNutritionUser();
        }
    }

    private void ouvrirVueNutritionUser() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/nutrition/repas_index.fxml"));
            Parent root = loader.load();
            Nutritioncontroller controller = loader.getController();
            controller.setCurrentUser(currentUser);
            controller.setUtilisateurId(getCurrentUserId());
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight()));
            stage.setTitle("BioSync – Mes Repas");
        } catch (IOException e) {
            statusLabel.setText("Erreur: " + e.getMessage());
        }
    }

    private void ouvrirVueCoach() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/nutrition/coach_users.fxml"));
            Parent root = loader.load();
            CoachUsersController controller = loader.getController();
            controller.setCoachUser(currentUser);
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight()));
            stage.setTitle("BioSync – Suivi Nutritionnel Coach");
        } catch (IOException e) {
            statusLabel.setText("Erreur: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────

    private String extractDisplayRole(String roles) {
        if (roles == null) return "UTILISATEUR";
        if (roles.contains("ADMIN"))       return "ADMIN";
        if (roles.contains("COACH"))       return "COACH";
        if (roles.contains("SPECIALISTE")) return "SPÉCIALISTE";
        return "UTILISATEUR";
    }

    private String getRoleBadgeStyle(String role) {
        String base = "-fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 20; " +
                "-fx-padding: 3 10 3 10; -fx-text-fill: white;";
        return switch (role) {
            case "ADMIN"       -> base + "-fx-background-color: #ef4444;";
            case "COACH"       -> base + "-fx-background-color: #10b981;";
            case "SPÉCIALISTE" -> base + "-fx-background-color: #f59e0b;";
            default            -> base + "-fx-background-color: #6b7280;";
        };
    }

    private int getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : 1;
    }
}