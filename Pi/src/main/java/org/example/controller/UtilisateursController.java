package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.example.dao.UserDAO;
import org.example.model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class UtilisateursController {

    @FXML private TextField searchField;
    @FXML private VBox usersContainer;
    @FXML private Label statusLabel;
    @FXML private Button sortNomBtn;
    @FXML private Button sortDateBtn;
    @FXML private Button sortScoreBtn;

    private User currentUser;
    private List<User> allUsers = new ArrayList<>();
    private String currentSort = "nom";

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadUsers();
    }

    @FXML
    public void initialize() {
        // Will be populated via setCurrentUser
    }

    private void loadUsers() {
        try {
            allUsers = UserDAO.getAllUsers();
            renderUsers(allUsers);
            statusLabel.setText(allUsers.size() + " utilisateur(s) trouvé(s)");
        } catch (Exception e) {
            statusLabel.setText("Erreur chargement: " + e.getMessage());
        }
    }

    private void renderUsers(List<User> users) {
        usersContainer.getChildren().clear();

        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            HBox row = buildUserRow(u, i % 2 == 0);
            usersContainer.getChildren().add(row);
        }
    }

    private HBox buildUserRow(User user, boolean alternate) {
        HBox row = new HBox();
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 14 24 14 24; -fx-border-color: transparent transparent #f0f2f8 transparent; -fx-border-width: 0 0 1 0; "
                + (alternate ? "-fx-background-color: white;" : "-fx-background-color: #fafbfd;"));

        // Avatar circle
        StackPane avatar = buildAvatar(user);

        // Name + email VBox
        VBox nameBox = new VBox(2);
        Label nameLbl = new Label(user.getNomComplet());
        nameLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
        Label emailLbl = new Label(user.getEmail());
        emailLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
        nameBox.getChildren().addAll(nameLbl, emailLbl);

        HBox userCol = new HBox(12);
        userCol.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        userCol.setPrefWidth(280);
        userCol.getChildren().addAll(avatar, nameBox);

        // Role badge
        String role = extractDisplayRole(user.getRoles());
        Label roleLbl = new Label(role);
        roleLbl.setStyle(getRoleBadgeStyle(role));
        roleLbl.setPrefWidth(150);

        // Date inscription
        String date = user.getDateInscription() != null ? user.getDateInscription() : "20/02/2026";
        Label dateLbl = new Label(date);
        dateLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");
        dateLbl.setPrefWidth(180);

        // Score
        HBox scoreBox = new HBox(4);
        scoreBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        scoreBox.setPrefWidth(160);
        Label starLbl = new Label("☆");
        starLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #f59e0b;");
        Label scoreLbl = new Label(String.valueOf(user.getScoreGlobal()));
        scoreLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");
        scoreBox.getChildren().addAll(starLbl, scoreLbl);

        // Actions
        Button viewBtn = new Button("👁");
        viewBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 4 8 4 8;");
        viewBtn.setTooltip(new Tooltip("Voir"));
        viewBtn.setOnAction(e -> handleViewUser(user));

        Button editBtn = new Button("✏");
        editBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 15px; -fx-cursor: hand; -fx-padding: 4 8 4 8; -fx-text-fill: #f59e0b;");
        editBtn.setTooltip(new Tooltip("Modifier"));
        editBtn.setOnAction(e -> handleEditUser(user));

        Button deleteBtn = new Button("🗑");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 15px; -fx-cursor: hand; -fx-padding: 4 8 4 8; -fx-text-fill: #ef4444;");
        deleteBtn.setTooltip(new Tooltip("Supprimer"));
        deleteBtn.setOnAction(e -> handleDeleteUser(user));

        HBox actionsBox = new HBox(4);
        actionsBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        actionsBox.getChildren().addAll(viewBtn, editBtn, deleteBtn);
        HBox.setHgrow(actionsBox, javafx.scene.layout.Priority.ALWAYS);

        row.getChildren().addAll(userCol, roleLbl, dateLbl, scoreBox, actionsBox);
        return row;
    }

    private StackPane buildAvatar(User user) {
        StackPane sp = new StackPane();
        Circle circle = new Circle(20);
        circle.setFill(javafx.scene.paint.Color.web(getAvatarColor(user.getRoles())));

        String initials = getInitials(user.getNomComplet());
        Label initLbl = new Label(initials);
        initLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: white; -fx-font-weight: bold;");

        sp.getChildren().addAll(circle, initLbl);
        return sp;
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    private String getAvatarColor(String roles) {
        if (roles == null) return "#6b7280";
        if (roles.contains("ADMIN")) return "#ef4444";
        if (roles.contains("COACH")) return "#10b981";
        if (roles.contains("SPECIALISTE")) return "#f59e0b";
        return "#7c3aed";
    }

    private String extractDisplayRole(String roles) {
        if (roles == null) return "UTILISATEUR";
        if (roles.contains("ADMIN")) return "ADMIN";
        if (roles.contains("COACH")) return "COACH";
        if (roles.contains("SPECIALISTE")) return "SPÉCIALISTE";
        return "UTILISATEUR";
    }

    private String getRoleBadgeStyle(String role) {
        String base = "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 4 12 4 12; -fx-text-fill: white;";
        return switch (role) {
            case "ADMIN" -> base + " -fx-background-color: #ef4444;";
            case "COACH" -> base + " -fx-background-color: #10b981;";
            case "SPÉCIALISTE" -> base + " -fx-background-color: #f59e0b;";
            default -> base + " -fx-background-color: #6b7280;";
        };
    }

    @FXML
    private void handleSearch(KeyEvent event) {
        String query = searchField.getText().toLowerCase().trim();
        List<User> filtered = allUsers.stream()
                .filter(u -> u.getNomComplet().toLowerCase().contains(query)
                        || u.getEmail().toLowerCase().contains(query))
                .collect(Collectors.toList());
        renderUsers(filtered);
        statusLabel.setText(filtered.size() + " résultat(s)");
    }

    @FXML
    private void handleSortNom() {
        currentSort = "nom";
        List<User> sorted = allUsers.stream()
                .sorted(Comparator.comparing(User::getNomComplet, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        renderUsers(sorted);
        updateSortButtons("nom");
    }

    @FXML
    private void handleSortDate() {
        currentSort = "date";
        List<User> sorted = new ArrayList<>(allUsers);
        // Sort by ID as proxy for date (higher ID = more recent)
        sorted.sort(Comparator.comparingInt(User::getId).reversed());
        renderUsers(sorted);
        updateSortButtons("date");
    }

    @FXML
    private void handleSortScore() {
        currentSort = "score";
        List<User> sorted = allUsers.stream()
                .sorted(Comparator.comparingInt(User::getScoreGlobal).reversed())
                .collect(Collectors.toList());
        renderUsers(sorted);
        updateSortButtons("score");
    }

    private void updateSortButtons(String active) {
        String activeStyle = "-fx-background-color: #4C6FFF; -fx-text-fill: white; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand; -fx-font-size: 13px; -fx-font-weight: bold;";
        String normalStyle = "-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-radius: 8; -fx-border-width: 1; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand; -fx-font-size: 13px; -fx-text-fill: #374151;";
        sortNomBtn.setStyle(active.equals("nom") ? activeStyle : normalStyle);
        sortDateBtn.setStyle(active.equals("date") ? activeStyle : normalStyle);
        sortScoreBtn.setStyle(active.equals("score") ? activeStyle : normalStyle);
    }

    private void handleViewUser(User user) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails Utilisateur");
        alert.setHeaderText(user.getNomComplet());
        alert.setContentText(
                "Email: " + user.getEmail() + "\n" +
                "Rôle: " + extractDisplayRole(user.getRoles()) + "\n" +
                "Score: " + user.getScoreGlobal() + "\n" +
                "Date inscription: " + (user.getDateInscription() != null ? user.getDateInscription() : "N/A")
        );
        alert.showAndWait();
    }

    private void handleEditUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/edit_utilisateur.fxml"));
            Parent root = loader.load();
            EditUtilisateurController ctrl = loader.getController();
            ctrl.setData(currentUser, user);
            Stage stage = (Stage) searchField.getScene().getWindow();
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            stage.setScene(scene);
            stage.setTitle("BioSync - Modifier Utilisateur");
        } catch (IOException e) {
            statusLabel.setText("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDeleteUser(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer Utilisateur");
        confirm.setHeaderText("Supprimer " + user.getNomComplet() + "?");
        confirm.setContentText("Cette action est irréversible.");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            boolean ok = UserDAO.deleteUser(user.getId());
            if (ok) {
                allUsers.remove(user);
                renderUsers(allUsers);
                statusLabel.setText("Utilisateur supprimé. " + allUsers.size() + " restant(s).");
            } else {
                statusLabel.setText("Erreur lors de la suppression.");
            }
        }
    }

    @FXML
    private void handleNewUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/nouvel_utilisateur.fxml"));
            Parent root = loader.load();
            UtilisateurController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            Stage stage = (Stage) searchField.getScene().getWindow();
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            stage.setScene(scene);
            stage.setTitle("BioSync - Nouvel Utilisateur");
        } catch (IOException e) {
            statusLabel.setText("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExportPdf() {
        statusLabel.setText("Export PDF - fonctionnalité bientôt disponible.");
    }

    @FXML
    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dashboard.fxml"));
            Parent root = loader.load();
            DashboardController ctrl = loader.getController();
            ctrl.setUser(currentUser);
            Stage stage = (Stage) searchField.getScene().getWindow();
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            stage.setScene(scene);
            stage.setTitle("BioSync - Administration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
