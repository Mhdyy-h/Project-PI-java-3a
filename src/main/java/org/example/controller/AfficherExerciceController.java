package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.model.Exercice;
import org.example.model.ExerciceSelection;
import org.example.service.ServiceExercice;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AfficherExerciceController {

    @FXML private FlowPane         cardsContainer;
    @FXML private TextField        rechercheField;
    @FXML private ComboBox<String> filtreIntensite;
    @FXML private ComboBox<String> filtreCalories;
    @FXML private Label            compteurLabel;
    @FXML private Label            messageLabel;
    @FXML private Button           btnAjouter;
    @FXML private Button           btnMenu;
    @FXML private Button           btnReset;
    @FXML private Button           btnTriNomAZ;
    @FXML private Button           btnTriNomZA;
    @FXML private Button           btnTriCaloriesAsc;
    @FXML private Button           btnTriCaloriesDesc;
    @FXML private Button           btnTriIntensiteAsc;
    @FXML private Button           btnTriIntensiteDesc;

    private ObservableList<Exercice> tousLesExercices;
    private List<Exercice>           exercicesFiltres;

    @FXML
    public void initialize() {
        try {
            tousLesExercices = FXCollections.observableArrayList(
                    new ServiceExercice().afficherAll()
            );
        } catch (Exception e) {
            tousLesExercices = FXCollections.observableArrayList();
        }

        filtreIntensite.setItems(FXCollections.observableArrayList(
                "Toutes", "Faible", "Moyenne", "Élevée"
        ));
        filtreIntensite.setValue("Toutes");

        filtreCalories.setItems(FXCollections.observableArrayList(
                "Toutes", "< 5 cal/min", "5 – 15 cal/min", "> 15 cal/min"
        ));
        filtreCalories.setValue("Toutes");

        rechercheField.textProperty().addListener(
                (obs, old, val) -> appliquerFiltres());
        filtreIntensite.valueProperty().addListener(
                (obs, old, val) -> appliquerFiltres());
        filtreCalories.valueProperty().addListener(
                (obs, old, val) -> appliquerFiltres());

        appliquerFiltres();
    }

    // ── Filtres ───────────────────────────────────────────────
    private void appliquerFiltres() {
        String recherche     = rechercheField.getText().trim().toLowerCase();
        String intensite     = filtreIntensite.getValue();
        String caloriesRange = filtreCalories.getValue();

        exercicesFiltres = tousLesExercices.stream()
                .filter(e -> {
                    boolean matchNom = recherche.isEmpty()
                            || (e.getNomExercice() != null &&
                            e.getNomExercice().toLowerCase().contains(recherche));

                    String intens = e.getIntensite() != null
                            ? e.getIntensite() : "Faible";
                    boolean matchIntensite = intensite == null
                            || intensite.equals("Toutes")
                            || intens.equalsIgnoreCase(intensite);

                    double c = e.getCaloriesParMinute();
                    boolean matchCalories = true;
                    if (caloriesRange != null) {
                        matchCalories = switch (caloriesRange) {
                            case "< 5 cal/min"    -> c < 5;
                            case "5 – 15 cal/min" -> c >= 5 && c <= 15;
                            case "> 15 cal/min"   -> c > 15;
                            default               -> true;
                        };
                    }
                    return matchNom && matchIntensite && matchCalories;
                })
                .collect(Collectors.toList());

        afficherCards();
        mettreAJourCompteur();
    }

    // ── Afficher les Cards ────────────────────────────────────
    private void afficherCards() {
        cardsContainer.getChildren().clear();
        for (Exercice exercice : exercicesFiltres) {
            cardsContainer.getChildren().add(creerCard(exercice));
        }
    }

    private VBox creerCard(Exercice exercice) {
        String couleurBord = switch (
                exercice.getIntensite() != null
                        ? exercice.getIntensite() : "Faible") {
            case "Élevée"  -> "#e94560";
            case "Moyenne" -> "#e67e22";
            default        -> "#27ae60";
        };

        String emojiBord = switch (
                exercice.getIntensite() != null
                        ? exercice.getIntensite() : "Faible") {
            case "Élevée"  -> "🔴";
            case "Moyenne" -> "🟡";
            default        -> "🟢";
        };

        VBox card = new VBox(10);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 20;" +
                        "-fx-border-color: " + couleurBord + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 16;" +
                        "-fx-effect: dropshadow(gaussian, rgba(74,35,90,0.15), 15, 0, 0, 4);" +
                        "-fx-pref-width: 240;"
        );

        Label nomLabel = new Label("💪 " + exercice.getNomExercice());
        nomLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #4a235a;");

        Label intensiteLabel = new Label(emojiBord + "  " + (exercice.getIntensite() != null
                ? exercice.getIntensite() : "Faible"));
        intensiteLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + couleurBord + ";");

        Label caloriesLabel = new Label("🔥  " + exercice.getCaloriesParMinute() + " cal/min");
        caloriesLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #e94560;");

        Label seanceLabel = new Label("🏋️  Séance #" + exercice.getSeanceId());
        seanceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #8e44ad;");

        Label sep = new Label("─────────────────");
        sep.setStyle("-fx-text-fill: #e8dff5; -fx-font-size: 10px;");

        HBox boutons = new HBox(8);

        Button btnModifier = new Button("✏️ Modifier");
        btnModifier.setStyle(
                "-fx-background-color: linear-gradient(to right, #d4af37, #b8960c);" +
                        "-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;" +
                        "-fx-pref-height: 32; -fx-pref-width: 100;" +
                        "-fx-background-radius: 8; -fx-cursor: hand;"
        );
        btnModifier.setOnAction(e -> modifierExercice(exercice));

        Button btnSupprimer = new Button("🗑️ Supprimer");
        btnSupprimer.setStyle(
                "-fx-background-color: #e94560;" +
                        "-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;" +
                        "-fx-pref-height: 32; -fx-pref-width: 105;" +
                        "-fx-background-radius: 8; -fx-cursor: hand;"
        );
        btnSupprimer.setOnAction(e -> supprimerExercice(exercice));

        boutons.getChildren().addAll(btnModifier, btnSupprimer);
        card.getChildren().addAll(nomLabel, intensiteLabel, caloriesLabel, seanceLabel, sep, boutons);

        return card;
    }

    // ── Compteur ──────────────────────────────────────────────
    private void mettreAJourCompteur() {
        int filtre = exercicesFiltres.size();
        int total  = tousLesExercices.size();
        if (filtre == total) {
            compteurLabel.setText("📋 " + total + " exercice(s) au total");
        } else {
            compteurLabel.setText("🔍 " + filtre + " résultat(s) sur " + total);
        }
    }

    // ── Tri ───────────────────────────────────────────────────
    @FXML public void trierNomAZ() {
        FXCollections.sort(tousLesExercices,
                Comparator.comparing(e -> e.getNomExercice() != null ? e.getNomExercice() : ""));
        appliquerFiltres();
    }

    @FXML public void trierNomZA() {
        FXCollections.sort(tousLesExercices,
                Comparator.comparing((Exercice e) -> e.getNomExercice() != null
                        ? e.getNomExercice() : "").reversed());
        appliquerFiltres();
    }

    @FXML public void trierCaloriesAsc() {
        FXCollections.sort(tousLesExercices,
                Comparator.comparingDouble(Exercice::getCaloriesParMinute));
        appliquerFiltres();
    }

    @FXML public void trierCaloriesDesc() {
        FXCollections.sort(tousLesExercices,
                Comparator.comparingDouble(Exercice::getCaloriesParMinute).reversed());
        appliquerFiltres();
    }

    @FXML public void trierIntensiteAsc() {
        FXCollections.sort(tousLesExercices, (a, b) ->
                Integer.compare(ordreIntensite(a.getIntensite()),
                        ordreIntensite(b.getIntensite())));
        appliquerFiltres();
    }

    @FXML public void trierIntensiteDesc() {
        FXCollections.sort(tousLesExercices, (a, b) ->
                Integer.compare(ordreIntensite(b.getIntensite()),
                        ordreIntensite(a.getIntensite())));
        appliquerFiltres();
    }

    private int ordreIntensite(String intensite) {
        if (intensite == null) return 0;
        return switch (intensite) {
            case "Faible"  -> 1;
            case "Moyenne" -> 2;
            case "Élevée"  -> 3;
            default        -> 0;
        };
    }

    @FXML public void reinitialiserFiltres() {
        rechercheField.clear();
        filtreIntensite.setValue("Toutes");
        filtreCalories.setValue("Toutes");
    }

    // ── Navigation ────────────────────────────────────────────
    private void modifierExercice(Exercice exercice) {
        try {
            ExerciceSelection.exercice = exercice;
            Parent root = FXMLLoader.load(getClass().getResource("/view/ModifierExercice.fxml"));
            btnMenu.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void supprimerExercice(Exercice exercice) {
        try {
            ExerciceSelection.exercice = exercice;
            Parent root = FXMLLoader.load(getClass().getResource("/view/SupprimerExercice.fxml"));
            btnMenu.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @FXML public void ouvrirAjouter(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/AjouterExercice.fxml"));
            btnMenu.getScene().setRoot(root); // ✅ FIX
        } catch (IOException e) {
            System.err.println("Erreur ouvrirAjouter: " + e.getMessage());
        }
    }

    @FXML public void ouvrirMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/Menu.fxml"));
            btnMenu.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}