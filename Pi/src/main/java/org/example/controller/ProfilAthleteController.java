package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import org.example.model.User;
import org.example.service.ProfilAthleteService;
import org.json.JSONObject;

import java.io.IOException;

public class ProfilAthleteController {

    // ── Physique ─────────────────────────────────────
    @FXML private TextField     fieldAge;
    @FXML private TextField     fieldPoids;
    @FXML private TextField     fieldTaille;
    @FXML private ComboBox<String> comboSexe;

    // ── Santé ────────────────────────────────────────
    @FXML private TextArea      areaBlessures;
    @FXML private TextArea      areaMedicaments;
    @FXML private TextArea      areaHistorique;

    // ── Sport ────────────────────────────────────────
    @FXML private ComboBox<String> comboObjectif;
    @FXML private ComboBox<String> comboNiveau;
    @FXML private Slider        sliderDispo;
    @FXML private Label         lblDispo;

    // ── Mental ───────────────────────────────────────
    @FXML private ComboBox<String> comboEmotion;
    @FXML private Slider        sliderStress;
    @FXML private Label         lblStress;
    @FXML private Slider        sliderSommeil;
    @FXML private Label         lblSommeil;

    // ── Alimentation ─────────────────────────────────
    @FXML private ComboBox<String> comboAlimentation;

    // ── Résultat ─────────────────────────────────────
    @FXML private Label         lblResultat;

    private ProfilAthleteService service;
    private User                 currentUser;

    @FXML
    public void initialize() {
        service = new ProfilAthleteService();

        // Remplir les ComboBox
        comboSexe.getItems().addAll("Homme", "Femme");
        comboObjectif.getItems().addAll(
                "Perte de poids", "Prise de masse", "Cardio / Endurance",
                "Tonification", "Compétition", "Santé générale");
        comboNiveau.getItems().addAll(
                "Débutant", "Intermédiaire", "Avancé", "Expert");
        comboEmotion.getItems().addAll(
                "😊 Très bien", "🙂 Bien", "😐 Moyen",
                "😔 Fatigué", "😰 Stressé", "😢 Mal");
        comboAlimentation.getItems().addAll(
                "Équilibré", "Végétarien", "Végétalien",
                "Sans gluten", "Hyperprotéiné", "Autre");

        // Sliders — afficher valeur en temps réel
        sliderDispo.valueProperty().addListener((o, ov, nv) ->
                lblDispo.setText(String.format("%.0f jours/semaine", nv.doubleValue())));
        sliderStress.valueProperty().addListener((o, ov, nv) ->
                lblStress.setText(String.format("%.0f / 10", nv.doubleValue())));
        sliderSommeil.valueProperty().addListener((o, ov, nv) ->
                lblSommeil.setText(String.format("%.0f / 10", nv.doubleValue())));
    }

    public void setUser(User user) {
        this.currentUser = user;
        chargerProfilExistant();
    }

    // Charger profil existant si déjà rempli
    private void chargerProfilExistant() {
        JSONObject profil = service.charger(currentUser.getId());
        if (profil.length() == 0) return; // profil vide

        fieldAge.setText(profil.optInt("age") > 0 ?
                String.valueOf(profil.optInt("age")) : "");
        fieldPoids.setText(profil.optDouble("poids_kg") > 0 ?
                String.valueOf(profil.optDouble("poids_kg")) : "");
        fieldTaille.setText(profil.optDouble("taille_cm") > 0 ?
                String.valueOf(profil.optDouble("taille_cm")) : "");

        comboSexe.setValue(profil.optString("sexe", null));
        comboObjectif.setValue(profil.optString("objectif", null));
        comboNiveau.setValue(profil.optString("niveau_sport", null));
        comboEmotion.setValue(profil.optString("etat_emotionnel", null));
        comboAlimentation.setValue(profil.optString("alimentation", null));

        areaBlessures.setText(profil.optString("blessures", ""));
        areaMedicaments.setText(profil.optString("medicaments", ""));
        areaHistorique.setText(profil.optString("historique_medical", ""));

        sliderDispo.setValue(profil.optInt("disponibilite_semaine", 3));
        sliderStress.setValue(profil.optInt("niveau_stress", 5));
        sliderSommeil.setValue(profil.optInt("qualite_sommeil", 5));

        lblResultat.setText("✅ Profil chargé — modifiez et sauvegardez");
        lblResultat.setStyle("-fx-text-fill: #44cc88;");
    }

    @FXML
    private void sauvegarder() {
        // Debug — voir ce que contiennent les champs
        System.out.println("Age: '" + fieldAge.getText() + "'");
        System.out.println("Poids: '" + fieldPoids.getText() + "'");
        System.out.println("Taille: '" + fieldTaille.getText() + "'");

        String age    = fieldAge.getText() == null ? "" : fieldAge.getText().trim();
        String poids  = fieldPoids.getText() == null ? "" : fieldPoids.getText().trim();
        String taille = fieldTaille.getText() == null ? "" : fieldTaille.getText().trim();

        if (age.isEmpty() || poids.isEmpty() || taille.isEmpty()) {
            lblResultat.setText("⚠️ Remplissez au moins : âge=" + age
                    + " poids=" + poids + " taille=" + taille);
            lblResultat.setStyle("-fx-text-fill: #ffaa00;");
            return;
        }

        try {
            JSONObject profil = new JSONObject()
                    .put("age",                   Integer.parseInt(age))
                    .put("poids_kg",              Double.parseDouble(poids))
                    .put("taille_cm",             Double.parseDouble(taille))
                    .put("sexe",                  comboSexe.getValue() != null ? comboSexe.getValue() : "")
                    .put("historique_medical",    areaHistorique.getText())
                    .put("blessures",             areaBlessures.getText())
                    .put("medicaments",           areaMedicaments.getText())
                    .put("objectif",              comboObjectif.getValue() != null ? comboObjectif.getValue() : "")
                    .put("niveau_sport",          comboNiveau.getValue() != null ? comboNiveau.getValue() : "")
                    .put("disponibilite_semaine", (int) sliderDispo.getValue())
                    .put("etat_emotionnel",       comboEmotion.getValue() != null ? comboEmotion.getValue() : "")
                    .put("niveau_stress",         (int) sliderStress.getValue())
                    .put("qualite_sommeil",       (int) sliderSommeil.getValue())
                    .put("alimentation",          comboAlimentation.getValue() != null ? comboAlimentation.getValue() : "");

            boolean ok = service.sauvegarder(currentUser.getId(), profil);

            if (ok) {
                lblResultat.setText("✅ Profil sauvegardé !");
                lblResultat.setStyle("-fx-text-fill: #44cc88;");
            } else {
                lblResultat.setText("❌ Erreur sauvegarde");
                lblResultat.setStyle("-fx-text-fill: #ff4444;");
            }

        } catch (NumberFormatException e) {
            lblResultat.setText("⚠️ Erreur : " + e.getMessage());
            lblResultat.setStyle("-fx-text-fill: #ffaa00;");
        }
    }

    @FXML
    private void retourMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/MenuUser.fxml"));
            Parent root = loader.load();
            MenuUserController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            lblResultat.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}