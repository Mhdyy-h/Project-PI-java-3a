package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MenuController {

    private static final Logger log = LoggerFactory.getLogger(MenuController.class);

    @FXML private Label totalSeancesLabel;
    @FXML private Label totalExercicesLabel;
    @FXML private Label moyenneDureeLabel;
    @FXML private Label totalMedaillesLabel;

    @FXML
    public void initialize() {
        if (totalSeancesLabel   != null) totalSeancesLabel.setText("—");
        if (totalExercicesLabel != null) totalExercicesLabel.setText("—");
        if (moyenneDureeLabel   != null) moyenneDureeLabel.setText("—");
        if (totalMedaillesLabel != null) totalMedaillesLabel.setText("—");
    }

    // ── Cognitif ─────────────────────────────────────────────────

    @FXML
    public void ouvrirDashboardCognitif(MouseEvent event) {
        naviguerVers("/DashboardCognitif.fxml");
    }

    @FXML
    public void ouvrirDashboardCognitifNav(ActionEvent event) {
        naviguerVers("/DashboardCognitif.fxml");
    }

    // ── Sports (stubs) ────────────────────────────────────────────

    @FXML public void ouvrirSeances(MouseEvent event)     { log.info("Module Séances non disponible"); }
    @FXML public void ouvrirExercices(MouseEvent event)   { log.info("Module Exercices non disponible"); }
    @FXML public void ouvrirAdmin(MouseEvent event)       { log.info("Module Admin non disponible"); }
    @FXML public void ouvrirSeancesNav(ActionEvent event)   { log.info("Module Séances non disponible"); }
    @FXML public void ouvrirExercicesNav(ActionEvent event) { log.info("Module Exercices non disponible"); }
    @FXML public void ouvrirAdminNav(ActionEvent event)     { log.info("Module Admin non disponible"); }

    // ── Navigation ────────────────────────────────────────────────

    private void naviguerVers(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Label ancre = totalSeancesLabel != null ? totalSeancesLabel : totalExercicesLabel;
            if (ancre == null || ancre.getScene() == null) return;
            Scene scene = ancre.getScene();
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            scene.setRoot(root);
        } catch (IOException e) {
            log.error("Erreur navigation vers {} : {}", fxml, e.getMessage());
        }
    }
}
