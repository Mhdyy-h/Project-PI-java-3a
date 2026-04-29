package org.example.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import org.example.model.Exercice;
import org.example.model.SeanceSport;
import org.example.service.ServiceExercice;
import org.example.service.ServiceSeanceSport;
import org.example.service.CaloriesNinjaService;
import java.io.IOException;
import java.util.List;

public class AjouterExerciceController {

    @FXML private TextField        nomField;
    @FXML private ComboBox<String> intensiteBox;
    @FXML private TextField        caloriesField;
    @FXML private TextField        seanceIdField;
    @FXML private Label            messageLabel;
    @FXML private Label            errNom;
    @FXML private Label            errIntensite;
    @FXML private Label            errCalories;
    @FXML private Label            errSeance;
    @FXML private Button           btnVoirListe;
    @FXML private Button           btnMenu;
    private CaloriesNinjaService caloriesNinjaService = new CaloriesNinjaService();
    private double caloriesCalculeesAPI = -1;
    // ── Nouveaux éléments pour les séances ──
    @FXML private FlowPane seancesContainer;
    @FXML private Label    labelSeanceSelectionnee;

    private static final String STYLE_NORMAL =
            "-fx-background-color: #faf8ff; -fx-text-fill: #4a235a; " +
                    "-fx-border-color: #d4af37; -fx-border-radius: 12; " +
                    "-fx-background-radius: 12; -fx-pref-height: 46; " +
                    "-fx-font-size: 13px; -fx-padding: 0 16;";

    private static final String STYLE_ERREUR =
            "-fx-background-color: #fff5f5; -fx-text-fill: #4a235a; " +
                    "-fx-border-color: #e94560; -fx-border-width: 2; -fx-border-radius: 12; " +
                    "-fx-background-radius: 12; -fx-pref-height: 46; " +
                    "-fx-font-size: 13px; -fx-padding: 0 16;";

    private static final String STYLE_OK =
            "-fx-background-color: #f0fff6; -fx-text-fill: #4a235a; " +
                    "-fx-border-color: #27ae60; -fx-border-width: 2; -fx-border-radius: 12; " +
                    "-fx-background-radius: 12; -fx-pref-height: 46; " +
                    "-fx-font-size: 13px; -fx-padding: 0 16;";

    private static final String ERR_STYLE =
            "-fx-font-size: 11px; -fx-text-fill: #e94560; -fx-font-weight: bold;";

    private static final String OK_STYLE =
            "-fx-font-size: 11px; -fx-text-fill: #27ae60; -fx-font-weight: bold;";

    // ── Initialize ────────────────────────────────────────────
    @FXML
    public void initialize() {
        intensiteBox.setItems(FXCollections.observableArrayList(
                "Faible", "Moyenne", "Élevée"));

        // Validation temps réel
        nomField.textProperty().addListener((obs, old, val) -> validerNom());
        caloriesField.textProperty().addListener((obs, old, val) -> validerCalories());
        seanceIdField.textProperty().addListener((obs, old, val) -> validerSeanceId());
        intensiteBox.valueProperty().addListener((obs, old, val) -> validerIntensite());

        // Charger les séances au démarrage
        chargerSeances();
        // Auto-calcul calories quand nom change
        nomField.textProperty().addListener((obs, old, val) -> {
            validerNom();
            caloriesCalculeesAPI = -1;
            caloriesField.clear();
            caloriesField.setPromptText("Tapez le nom puis cliquez Calculer");
        });
    }

    // ── Charger les séances disponibles ──────────────────────
    @FXML
    public void chargerSeances() {
        seancesContainer.getChildren().clear();
        try {
            List<SeanceSport> seances = new ServiceSeanceSport().afficherAll();

            if (seances.isEmpty()) {
                Label vide = new Label("Aucune séance disponible.");
                vide.setStyle("-fx-text-fill: #9b89a8; -fx-font-style: italic; -fx-font-size: 12px;");
                seancesContainer.getChildren().add(vide);
                return;
            }

            for (SeanceSport s : seances) {

                // Chip de séance
                VBox chip = new VBox(3);
                chip.setStyle(
                        "-fx-background-color: #f0fdf4;" +
                                "-fx-background-radius: 10;" +
                                "-fx-padding: 8 12;" +
                                "-fx-border-color: #10b981;" +
                                "-fx-border-width: 1.5;" +
                                "-fx-border-radius: 10;" +
                                "-fx-cursor: hand;"
                );

                Label idLabel = new Label("ID : " + s.getId());
                idLabel.setStyle(
                        "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #10b981;"
                );

                Label nomLabel = new Label("🏃 " + s.getNomSeance());
                nomLabel.setStyle(
                        "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2d1b69;"
                );

                Label dureeLabel = new Label("⏱ " + s.getDureeMinutes() + " min");
                dureeLabel.setStyle(
                        "-fx-font-size: 11px; -fx-text-fill: #555;"
                );

                chip.getChildren().addAll(idLabel, nomLabel, dureeLabel);

                // Clic → remplir automatiquement le champ ID
                chip.setOnMouseClicked(e -> {
                    // Reset toutes les chips
                    seancesContainer.getChildren().forEach(node -> {
                        node.setStyle(
                                "-fx-background-color: #f0fdf4;" +
                                        "-fx-background-radius: 10;" +
                                        "-fx-padding: 8 12;" +
                                        "-fx-border-color: #10b981;" +
                                        "-fx-border-width: 1.5;" +
                                        "-fx-border-radius: 10;" +
                                        "-fx-cursor: hand;"
                        );
                        if (node instanceof VBox v) {
                            v.getChildren().forEach(child -> {
                                if (child instanceof Label lbl) {
                                    String txt = lbl.getText();
                                    if (txt.startsWith("ID :")) {
                                        lbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #10b981;");
                                    } else if (txt.startsWith("🏃")) {
                                        lbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2d1b69;");
                                    } else {
                                        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #555;");
                                    }
                                }
                            });
                        }
                    });

                    // Surligner la chip sélectionnée
                    chip.setStyle(
                            "-fx-background-color: #2d1b69;" +
                                    "-fx-background-radius: 10;" +
                                    "-fx-padding: 8 12;" +
                                    "-fx-border-color: #10b981;" +
                                    "-fx-border-width: 1.5;" +
                                    "-fx-border-radius: 10;" +
                                    "-fx-cursor: hand;"
                    );
                    idLabel.setStyle(
                            "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #10b981;"
                    );
                    nomLabel.setStyle(
                            "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white;"
                    );
                    dureeLabel.setStyle(
                            "-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.8);"
                    );

                    // Remplir le champ ID automatiquement
                    seanceIdField.setText(String.valueOf(s.getId()));
                    labelSeanceSelectionnee.setText(
                            s.getNomSeance() + "  (ID : " + s.getId() + ")"
                    );
                    labelSeanceSelectionnee.setStyle(
                            "-fx-font-size: 12px; -fx-text-fill: #10b981; -fx-font-weight: bold;"
                    );
                });

                seancesContainer.getChildren().add(chip);
            }

        } catch (Exception e) {
            Label err = new Label("❌ Erreur chargement : " + e.getMessage());
            err.setStyle("-fx-text-fill: #e94560; -fx-font-weight: bold; -fx-font-size: 12px;");
            seancesContainer.getChildren().add(err);
        }
    }

    // ── Validations ───────────────────────────────────────────
    private boolean validerNom() {
        String val = nomField.getText().trim();
        if (val.isEmpty()) {
            nomField.setStyle(STYLE_ERREUR);
            errNom.setText("❌ Le nom est obligatoire !");
            errNom.setStyle(ERR_STYLE);
            return false;
        }
        if (val.length() < 3) {
            nomField.setStyle(STYLE_ERREUR);
            errNom.setText("❌ Minimum 3 caractères !");
            errNom.setStyle(ERR_STYLE);
            return false;
        }
        if (val.length() > 50) {
            nomField.setStyle(STYLE_ERREUR);
            errNom.setText("❌ Maximum 50 caractères !");
            errNom.setStyle(ERR_STYLE);
            return false;
        }
        nomField.setStyle(STYLE_OK);
        errNom.setText("✅ Nom valide");
        errNom.setStyle(OK_STYLE);
        return true;
    }

    private boolean validerIntensite() {
        if (intensiteBox.getValue() == null) {
            errIntensite.setText("❌ Choisissez une intensité !");
            errIntensite.setStyle(ERR_STYLE);
            return false;
        }
        errIntensite.setText("✅ Intensité : " + intensiteBox.getValue());
        errIntensite.setStyle(OK_STYLE);
        return true;
    }

    private boolean validerCalories() {
        String val = caloriesField.getText().trim();
        if (val.isEmpty()) {
            caloriesField.setStyle(STYLE_ERREUR);
            errCalories.setText("❌ Les calories sont obligatoires !");
            errCalories.setStyle(ERR_STYLE);
            return false;
        }
        try {
            double cal = Double.parseDouble(val.replace(",", "."));
            if (cal <= 0) {
                caloriesField.setStyle(STYLE_ERREUR);
                errCalories.setText("❌ Les calories doivent être > 0 !");
                errCalories.setStyle(ERR_STYLE);
                return false;
            }
            if (cal > 100) {
                caloriesField.setStyle(STYLE_ERREUR);
                errCalories.setText("❌ Maximum 100 cal/min !");
                errCalories.setStyle(ERR_STYLE);
                return false;
            }
            caloriesField.setStyle(STYLE_OK);
            errCalories.setText("✅ " + cal + " cal/min");
            errCalories.setStyle(OK_STYLE);
            return true;
        } catch (NumberFormatException e) {
            caloriesField.setStyle(STYLE_ERREUR);
            errCalories.setText("❌ Nombre décimal requis (ex: 8.5) !");
            errCalories.setStyle(ERR_STYLE);
            return false;
        }
    }

    private boolean validerSeanceId() {
        String val = seanceIdField.getText().trim();
        if (val.isEmpty()) {
            seanceIdField.setStyle(STYLE_ERREUR);
            errSeance.setText("❌ L'ID séance est obligatoire !");
            errSeance.setStyle(ERR_STYLE);
            return false;
        }
        if (!val.matches("\\d+") || Integer.parseInt(val) <= 0) {
            seanceIdField.setStyle(STYLE_ERREUR);
            errSeance.setText("❌ Entier positif requis (ex: 1, 2, 3) !");
            errSeance.setStyle(ERR_STYLE);
            return false;
        }
        seanceIdField.setStyle(STYLE_OK);
        errSeance.setText("✅ ID valide");
        errSeance.setStyle(OK_STYLE);
        return true;
    }
    @FXML
    public void calculerCaloriesAPI(ActionEvent event) {
        String nom = nomField.getText().trim();
        String seanceIdStr = seanceIdField.getText().trim();

        if (nom.isEmpty()) {
            errCalories.setText("❌ Remplis d'abord le nom de l'exercice !");
            errCalories.setStyle(ERR_STYLE);
            return;
        }
        if (seanceIdStr.isEmpty()) {
            errCalories.setText("❌ Sélectionne d'abord une séance !");
            errCalories.setStyle(ERR_STYLE);
            return;
        }

        // Récupérer la durée de la séance sélectionnée
        int seanceId = Integer.parseInt(seanceIdStr);
        int dureeMinutes = 30; // défaut
        try {
            List<SeanceSport> seances = new ServiceSeanceSport().afficherAll();
            for (SeanceSport s : seances) {
                if (s.getId() == seanceId) {
                    dureeMinutes = s.getDureeMinutes();
                    break;
                }
            }
        } catch (Exception ignored) {}

        final int duree = dureeMinutes;

        caloriesField.setPromptText("⏳ Calcul en cours...");
        caloriesField.setDisable(true);
        errCalories.setText("⏳ Appel API Ninja...");
        errCalories.setStyle("-fx-font-size: 11px; -fx-text-fill: #f39c12;");

        new Thread(() -> {
            double cal = caloriesNinjaService.getCalories(nom, duree);
            javafx.application.Platform.runLater(() -> {
                caloriesField.setDisable(false);
                if (cal >= 0) {
                    // Convertir total → cal/minute
                    double calParMin = cal / duree;
                    caloriesCalculeesAPI = calParMin;
                    caloriesField.setText(String.format("%.1f", calParMin));
                    caloriesField.setStyle(STYLE_OK);
                    errCalories.setText("✅ " + String.format("%.0f", cal) +
                            " cal totales ÷ " + duree + " min = " +
                            String.format("%.1f", calParMin) + " cal/min");
                    errCalories.setStyle(OK_STYLE);
                } else {
                    caloriesField.setPromptText("Ex: 8.5, 12.0...");
                    errCalories.setText("⚠️ Exercice non reconnu — saisir manuellement");
                    errCalories.setStyle("-fx-font-size: 11px; -fx-text-fill: #f39c12;");
                }
            });
        }).start();
    }
    // ── Soumission ────────────────────────────────────────────
    @FXML
    public void ajouterExercice(ActionEvent event) {
        boolean ok =
                validerNom() &
                        validerIntensite() &
                        validerCalories() &
                        validerSeanceId();

        if (!ok) {
            messageLabel.setText("⚠️ Corrigez les erreurs avant de continuer !");
            messageLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #e94560;");
            return;
        }

        try {
            double cal      = Double.parseDouble(caloriesField.getText().trim().replace(",", "."));
            int    idSeance = Integer.parseInt(seanceIdField.getText().trim());

            Exercice ex = new Exercice(
                    0,
                    nomField.getText().trim(),
                    intensiteBox.getValue(),
                    cal,
                    idSeance
            );
            new ServiceExercice().ajouter(ex);

            messageLabel.setText("✅ Exercice ajouté avec succès !");
            messageLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
            viderFormulaire();

        } catch (Exception e) {
            messageLabel.setText("❌ Erreur BD : " + e.getMessage());
            messageLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #e94560;");
            e.printStackTrace();
        }
    }

    private void viderFormulaire() {
        nomField.clear();
        intensiteBox.setValue(null);
        caloriesField.clear();
        seanceIdField.clear();
        nomField.setStyle(STYLE_NORMAL);
        caloriesField.setStyle(STYLE_NORMAL);
        seanceIdField.setStyle(STYLE_NORMAL);
        errNom.setText("");
        errIntensite.setText("");
        errCalories.setText("");
        errSeance.setText("");
        labelSeanceSelectionnee.setText("Aucune séance sélectionnée");
        labelSeanceSelectionnee.setStyle("-fx-font-size: 12px; -fx-text-fill: #555; -fx-font-style: italic;");
        // Reset les chips
        chargerSeances();
    }

    // ── Navigation ────────────────────────────────────────────
    @FXML
    public void ouvrirAfficher(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/AfficherExercice.fxml"));
            btnVoirListe.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Erreur AfficherExercice : " + e.getMessage());
        }
    }

    @FXML
    public void ouvrirMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/MenuUser.fxml"));
            btnMenu.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Erreur Menu : " + e.getMessage());
        }
    }
}