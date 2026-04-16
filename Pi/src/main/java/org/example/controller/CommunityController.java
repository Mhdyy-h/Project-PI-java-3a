package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.dao.EvenementDAO;
import org.example.dao.GroupeDAO;
import org.example.model.Evenement;
import org.example.model.Groupe;
import org.example.model.User;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class CommunityController {

    @FXML private VBox groupsContainer;
    @FXML private VBox eventsContainer;

    private User currentUser;

    /**
     * Called automatically by JavaFX after FXML loading.
     */
    @FXML
    public void initialize() {
        loadData();
    }

    /**
     * Required for integration with Mehdi's Admin/Dashboard navigation.
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Refreshes both the Groups and Events lists from the Database.
     */
    public void loadData() {
        try {
            // 1. Refresh Groups
            groupsContainer.getChildren().clear();
            List<Groupe> groups = GroupeDAO.getAllGroups();
            if (groups.isEmpty()) {
                groupsContainer.getChildren().add(createEmptyLabel("Aucun groupe créé."));
            } else {
                for (Groupe g : groups) {
                    groupsContainer.getChildren().add(buildGroupCard(g));
                }
            }

            // 2. Refresh Events
            eventsContainer.getChildren().clear();
            List<Evenement> events = EvenementDAO.getAllEvents();
            if (events.isEmpty()) {
                eventsContainer.getChildren().add(createEmptyLabel("Aucun événement planifié."));
            } else {
                for (Evenement e : events) {
                    eventsContainer.getChildren().add(buildEventCard(e));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement des données communautaires.");
        }
    }

    // --- CARD BUILDERS (UI) ---

    private HBox buildGroupCard(Groupe g) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        VBox info = new VBox(5);
        Label name = new Label(g.getNomGroupe());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1a1a2e;");

        Label theme = new Label(g.getThematique());
        theme.setStyle("-fx-background-color: #ecfdf5; -fx-text-fill: #10b981; -fx-font-size: 11px; " +
                "-fx-padding: 2 8; -fx-background-radius: 10; -fx-font-weight: bold;");
        info.getChildren().addAll(name, theme);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #3b82f6; -fx-font-weight: bold; -fx-cursor: hand;");
        editBtn.setOnAction(e -> navigateToGroupEdit(g));

        Button delBtn = new Button("Supprimer");
        delBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-cursor: hand;");
        delBtn.setOnAction(e -> {
            if (confirmDelete("groupe " + g.getNomGroupe())) {
                try {
                    GroupeDAO.delete(g.getId());
                    loadData();
                } catch (SQLException ex) { ex.printStackTrace(); }
            }
        });

        card.getChildren().addAll(info, spacer, editBtn, delBtn);
        return card;
    }

    private HBox buildEventCard(Evenement ev) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-padding: 12; -fx-background-radius: 10; " +
                "-fx-border-color: #d1fae5; -fx-border-width: 0 0 0 4; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 5, 0, 0, 2);");

        VBox info = new VBox(3);
        Label title = new Label(ev.getTitreEvent());
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");

        String dateStr = ev.getDateEvent().toString().substring(0, 10);
        Label details = new Label("📅 " + dateStr + " | 🏆 " + ev.getPointsParticipation() + " pts");
        details.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        info.getChildren().addAll(title, details);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button editBtn = new Button("✎");
        editBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-cursor: hand;");
        editBtn.setOnAction(e -> navigateToEventEdit(ev));

        Button delBtn = new Button("✕");
        delBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-cursor: hand;");
        delBtn.setOnAction(e -> {
            if (confirmDelete("événement " + ev.getTitreEvent())) {
                try {
                    EvenementDAO.delete(ev.getId());
                    loadData();
                } catch (SQLException ex) { ex.printStackTrace(); }
            }
        });

        card.getChildren().addAll(info, spacer, editBtn, delBtn);
        return card;
    }

    // --- NAVIGATION LOGIC ---

    @FXML
    private void handleGoToGroupForm() {
        switchScene("/view/group_form.fxml", "Nouveau Groupe", null, null);
    }

    @FXML
    private void handleGoToEventForm() {
        switchScene("/view/event_form.fxml", "Nouvel Événement", null, null);
    }

    private void navigateToGroupEdit(Groupe g) {
        switchScene("/view/group_form.fxml", "Modifier Groupe", g, null);
    }

    private void navigateToEventEdit(Evenement e) {
        switchScene("/view/event_form.fxml", "Modifier Événement", null, e);
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dashboard.fxml"));
            Parent root = loader.load();
            AdminController ctrl = loader.getController();
            ctrl.setUser(currentUser);
            groupsContainer.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Generic scene switcher to handle navigation between Home and Forms.
     */
    private void switchScene(String fxmlPath, String title, Groupe groupToEdit, Evenement eventToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            if (groupToEdit != null) {
                GroupFormController ctrl = loader.getController();
                ctrl.setGroupeData(groupToEdit);
            } else if (eventToEdit != null) {
                EventFormController ctrl = loader.getController();
                ctrl.setEventData(eventToEdit);
            }

            Stage stage = (Stage) groupsContainer.getScene().getWindow();
            stage.setTitle("BioSync - " + title);
            groupsContainer.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- HELPERS ---

    private Label createEmptyLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: #9ca3af; -fx-font-style: italic; -fx-padding: 10;");
        return lbl;
    }

    private boolean confirmDelete(String itemName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Êtes-vous sûr de vouloir supprimer : " + itemName + " ?");
        return alert.showAndWait().filter(r -> r == ButtonType.OK).isPresent();
    }
}