package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AfficherSeanceController {

    @FXML private FlowPane         cardsContainer;
    @FXML private TextField        rechercheField;
    @FXML private ComboBox<String> filtreMedaille;
    @FXML private ComboBox<String> filtreDuree;
    @FXML private Label            compteurLabel;
    @FXML private Label            messageLabel;
    @FXML private Button           btnAjouter;
    @FXML private Button           btnMenu;
    @FXML private Button           btnReset;
    @FXML private Button           btnTriNomAZ;
    @FXML private Button           btnTriNomZA;
    @FXML private Button           btnTriDureeAsc;
    @FXML private Button           btnTriDureeDesc;
    @FXML private Button           btnTriDateRecent;

    private ObservableList<SeanceSport> toutesLesSeances;
    private List<SeanceSport>           seancesFiltrees;

    @FXML
    public void initialize() {
        try {
            toutesLesSeances = FXCollections.observableArrayList(
                    new ServiceSeanceSport().afficherAll()
            );
        } catch (Exception e) {
            toutesLesSeances = FXCollections.observableArrayList();
        }

        filtreMedaille.setItems(FXCollections.observableArrayList(
                "Toutes", "Or", "Argent", "Bronze", "Aucune"
        ));
        filtreMedaille.setValue("Toutes");

        filtreDuree.setItems(FXCollections.observableArrayList(
                "Toutes", "< 30 min", "30 – 60 min", "61 – 120 min", "> 120 min"
        ));
        filtreDuree.setValue("Toutes");

        rechercheField.textProperty().addListener((obs, old, val) -> appliquerFiltres());
        filtreMedaille.valueProperty().addListener((obs, old, val) -> appliquerFiltres());
        filtreDuree.valueProperty().addListener((obs, old, val) -> appliquerFiltres());

        appliquerFiltres();
    }

    // ── Filtres ───────────────────────────────────────────────
    private void appliquerFiltres() {
        String recherche  = rechercheField.getText().trim().toLowerCase();
        String medaille   = filtreMedaille.getValue();
        String dureeRange = filtreDuree.getValue();

        seancesFiltrees = toutesLesSeances.stream()
                .filter(s -> {
                    boolean matchNom = recherche.isEmpty()
                            || (s.getNomSeance() != null &&
                            s.getNomSeance().toLowerCase().contains(recherche));

                    String med = s.getMedailleObtenue() != null
                            ? s.getMedailleObtenue() : "Aucune";
                    boolean matchMedaille = medaille == null
                            || medaille.equals("Toutes")
                            || med.equalsIgnoreCase(medaille);

                    int d = s.getDureeMinutes();
                    boolean matchDuree = true;
                    if (dureeRange != null) {
                        matchDuree = switch (dureeRange) {
                            case "< 30 min"     -> d < 30;
                            case "30 – 60 min"  -> d >= 30 && d <= 60;
                            case "61 – 120 min" -> d >= 61 && d <= 120;
                            case "> 120 min"    -> d > 120;
                            default             -> true;
                        };
                    }
                    return matchNom && matchMedaille && matchDuree;
                })
                .collect(Collectors.toList());

        afficherCards();
        mettreAJourCompteur();
    }

    // ── Afficher les Cards ────────────────────────────────────
    private void afficherCards() {
        cardsContainer.getChildren().clear();
        for (SeanceSport seance : seancesFiltrees) {
            cardsContainer.getChildren().add(creerCard(seance));
        }
    }

    private VBox creerCard(SeanceSport seance) {
        String couleurBord = switch (
                seance.getMedailleObtenue() != null
                        ? seance.getMedailleObtenue() : "Aucune") {
            case "Or"     -> "#d4af37";
            case "Argent" -> "#aaaaaa";
            case "Bronze" -> "#cd7f32";
            default       -> "#7d3c98";
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

        // Nom
        Label nomLabel = new Label("🏃 " + seance.getNomSeance());
        nomLabel.setStyle(
                "-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #4a235a;"
        );

        // Durée
        Label dureeLabel = new Label("⏱️  " + seance.getDureeMinutes() + " minutes");
        dureeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #27ae60;");

        // Date
        Label dateLabel = new Label("📅  " + seance.getDateSeance());
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2980b9;");

        // Heure
        Label heureLabel = new Label("⏰  " + seance.getHeureDebut());
        heureLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #8e44ad;");

        // Médaille
        String medEmoji = switch (
                seance.getMedailleObtenue() != null
                        ? seance.getMedailleObtenue() : "Aucune") {
            case "Or"     -> "🥇 Or";
            case "Argent" -> "🥈 Argent";
            case "Bronze" -> "🥉 Bronze";
            default       -> "— Aucune";
        };
        Label medailleLabel = new Label(medEmoji);
        medailleLabel.setStyle(
                "-fx-font-size: 12px; -fx-font-weight: bold;" +
                        "-fx-text-fill: " + couleurBord + ";"
        );

        // Séparateur
        Label sep = new Label("─────────────────");
        sep.setStyle("-fx-text-fill: #e8dff5; -fx-font-size: 10px;");

        // ── Boutons ───────────────────────────────────────────
        HBox boutons = new HBox(6);

        // 👁 Détails  ← NOUVEAU
        Button btnDetails = new Button("👁 Détails");
        btnDetails.setStyle(
                "-fx-background-color: #2d1b69;" +
                        "-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;" +
                        "-fx-pref-height: 32; -fx-pref-width: 90;" +
                        "-fx-background-radius: 8; -fx-cursor: hand;"
        );
        btnDetails.setOnAction(e -> voirDetails(seance));

        // ✏️ Modifier
        Button btnModifier = new Button("✏️ Modifier");
        btnModifier.setStyle(
                "-fx-background-color: linear-gradient(to right, #d4af37, #b8960c);" +
                        "-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;" +
                        "-fx-pref-height: 32; -fx-pref-width: 90;" +
                        "-fx-background-radius: 8; -fx-cursor: hand;"
        );
        btnModifier.setOnAction(e -> modifierSeance(seance));

        // 🗑️ Supprimer
        Button btnSupprimer = new Button("🗑️ Supprimer");
        btnSupprimer.setStyle(
                "-fx-background-color: #e94560;" +
                        "-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;" +
                        "-fx-pref-height: 32; -fx-pref-width: 95;" +
                        "-fx-background-radius: 8; -fx-cursor: hand;"
        );
        btnSupprimer.setOnAction(e -> supprimerSeance(seance));

        boutons.getChildren().addAll(btnDetails, btnModifier, btnSupprimer);

        card.getChildren().addAll(
                nomLabel, dureeLabel, dateLabel,
                heureLabel, medailleLabel, sep, boutons
        );

        return card;
    }

    // ── Compteur ──────────────────────────────────────────────
    private void mettreAJourCompteur() {
        int filtre = seancesFiltrees.size();
        int total  = toutesLesSeances.size();
        if (filtre == total) {
            compteurLabel.setText("📋 " + total + " séance(s) au total");
        } else {
            compteurLabel.setText("🔍 " + filtre + " résultat(s) sur " + total);
        }
    }

    // ── Tri ───────────────────────────────────────────────────
    @FXML public void trierNomAZ() {
        FXCollections.sort(toutesLesSeances,
                Comparator.comparing(s -> s.getNomSeance() != null ? s.getNomSeance() : ""));
        appliquerFiltres();
    }

    @FXML public void trierNomZA() {
        FXCollections.sort(toutesLesSeances,
                Comparator.comparing((SeanceSport s) ->
                        s.getNomSeance() != null ? s.getNomSeance() : "").reversed());
        appliquerFiltres();
    }

    @FXML public void trierDureeAsc() {
        FXCollections.sort(toutesLesSeances,
                Comparator.comparingInt(SeanceSport::getDureeMinutes));
        appliquerFiltres();
    }

    @FXML public void trierDureeDesc() {
        FXCollections.sort(toutesLesSeances,
                Comparator.comparingInt(SeanceSport::getDureeMinutes).reversed());
        appliquerFiltres();
    }

    @FXML public void trierDateRecent() {
        FXCollections.sort(toutesLesSeances,
                Comparator.comparing((SeanceSport s) ->
                        s.getDateSeance() != null ? s.getDateSeance() : "").reversed());
        appliquerFiltres();
    }

    @FXML public void reinitialiserFiltres() {
        rechercheField.clear();
        filtreMedaille.setValue("Toutes");
        filtreDuree.setValue("Toutes");
    }

    // ── Navigation ────────────────────────────────────────────

    // ← NOUVEAU : ouvrir la page détails d'une séance
    private void voirDetails(SeanceSport seance) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/DetailSeance.fxml"));
            Parent root = loader.load();
            DetailSeanceController ctrl = loader.getController();
            ctrl.setSeance(seance);
            btnMenu.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Erreur ouverture détails : " + e.getMessage());
        }
    }

    private void modifierSeance(SeanceSport seance) {
        try {
            SeanceSelection.seance = seance;
            Parent root = FXMLLoader.load(
                    getClass().getResource("/ModifierSeance.fxml"));
            btnMenu.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void supprimerSeance(SeanceSport seance) {
        try {
            SeanceSelection.seance = seance;
            Parent root = FXMLLoader.load(
                    getClass().getResource("/SupprimerSeance.fxml"));
            btnMenu.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @FXML public void ouvrirAjouter(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/AjouterSeance.fxml"));
            btnAjouter.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @FXML public void ouvrirMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/Menu.fxml"));
            btnMenu.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}