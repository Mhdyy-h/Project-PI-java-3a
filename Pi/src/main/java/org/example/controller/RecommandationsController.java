package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import org.example.model.User;
import org.example.service.CoachUserService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class RecommandationsController {

    @FXML private VBox  listeContainer;
    @FXML private Label lblTitre;

    private CoachUserService coachUserService;
    private User             currentUser;

    @FXML
    public void initialize() {
        coachUserService = new CoachUserService();
    }

    public void setUser(User user) {
        this.currentUser = user;
        lblTitre.setText("🔔 Recommandations de votre Coach");
        chargerRecommandations();
    }

    private void chargerRecommandations() {
        listeContainer.getChildren().clear();
        List<JSONObject> recommandations =
                coachUserService.getRecommandationsUser(currentUser.getId());

        if (recommandations.isEmpty()) {
            Label vide = new Label(
                    "Aucune recommandation pour le moment.\n" +
                            "Votre coach vous enverra bientôt des conseils !");
            vide.setWrapText(true);
            vide.setStyle("-fx-text-fill: #888888; -fx-font-size: 13px; -fx-padding: 20;");
            listeContainer.getChildren().add(vide);
            return;
        }

        for (JSONObject reco : recommandations) {
            listeContainer.getChildren().add(creerCarte(reco));
            coachUserService.marquerVue(reco.getInt("id"));
        }
    }

    private VBox creerCarte(JSONObject reco) {
        VBox carte = new VBox(14);

        String titre = reco.optString("titre", "Recommandation");
        String couleurBord = titre.toLowerCase().contains("masse")     ? "#e67e22" :
                titre.toLowerCase().contains("poids")     ? "#27ae60" :
                titre.toLowerCase().contains("cardio")    ? "#2980b9" :
                titre.toLowerCase().contains("tonifico")  ? "#8e44ad" : "#7d3c98";

        carte.setStyle(
                "-fx-background-color: #1a1a2e;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 20;" +
                        "-fx-border-color: " + couleurBord + ";" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-width: 2;");

        // ── HEADER ───────────────────────────────────────
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label ico = new Label("📋");
        ico.setStyle("-fx-font-size: 22px;");
        VBox titreBox = new VBox(2);
        Label lblTitreReco = new Label(titre);
        lblTitreReco.setStyle(
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px;");
        Label date = new Label("📅 " + reco.optString("date", ""));
        date.setStyle("-fx-text-fill: #888888; -fx-font-size: 10px;");
        titreBox.getChildren().addAll(lblTitreReco, date);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label badge = new Label(reco.optBoolean("vue") ? "✓ Vu" : "🆕 Nouveau");
        badge.setStyle(
                "-fx-background-color: " + (reco.optBoolean("vue") ? "#2a2a3e" : couleurBord) + ";" +
                        "-fx-text-fill: white; -fx-padding: 4 10;" +
                        "-fx-background-radius: 8; -fx-font-size: 10px; -fx-font-weight: bold;");
        header.getChildren().addAll(ico, titreBox, spacer, badge);

        // ── MESSAGE ──────────────────────────────────────
        Label message = new Label(reco.optString("message", ""));
        message.setWrapText(true);
        message.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12px; -fx-line-spacing: 3;");

        // ── NUTRITION ────────────────────────────────────
        String nutrition = reco.optString("nutrition", "");
        VBox nutritionBox = new VBox(4);
        if (!nutrition.isEmpty()) {
            nutritionBox.setStyle(
                    "-fx-background-color: #1a2e1a;" +
                            "-fx-background-radius: 10; -fx-padding: 10;");
            Label nutTitle = new Label("🥗 Nutrition :");
            nutTitle.setStyle(
                    "-fx-text-fill: #44cc88; -fx-font-weight: bold; -fx-font-size: 12px;");
            Label nutLabel = new Label(nutrition);
            nutLabel.setWrapText(true);
            nutLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11px;");
            nutritionBox.getChildren().addAll(nutTitle, nutLabel);
        }

        // ── EXERCICES ────────────────────────────────────
        VBox exercicesBox = new VBox(8);
        exercicesBox.setStyle(
                "-fx-background-color: #2a2a3e;" +
                        "-fx-background-radius: 10; -fx-padding: 14;");
        Label exTitle = new Label("💪 Exercices recommandés :");
        exTitle.setStyle(
                "-fx-text-fill: " + couleurBord + ";" +
                        "-fx-font-weight: bold; -fx-font-size: 12px;");
        exercicesBox.getChildren().add(exTitle);

        try {
            JSONArray exercices = new JSONArray(reco.optString("exercices", "[]"));
            for (int i = 0; i < exercices.length(); i++) {
                JSONObject ex = exercices.getJSONObject(i);
                HBox exRow = new HBox(8);
                exRow.setAlignment(Pos.CENTER_LEFT);
                Label num = new Label(String.valueOf(i + 1));
                num.setStyle(
                        "-fx-background-color: " + couleurBord + ";" +
                                "-fx-text-fill: white; -fx-font-weight: bold;" +
                                "-fx-font-size: 10px; -fx-padding: 2 6; -fx-background-radius: 4;");
                VBox exInfo = new VBox(2);
                Label exNom = new Label(
                        ex.optString("nom") + "  —  " +
                                ex.optInt("series") + " séries × " +
                                ex.optInt("repetitions") + " reps" +
                                (ex.has("temps_repos") ? "  ⏱ " + ex.optString("temps_repos") : ""));
                exNom.setStyle(
                        "-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
                Label conseil = new Label("💡 " + ex.optString("conseil", ""));
                conseil.setWrapText(true);
                conseil.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");
                exInfo.getChildren().addAll(exNom, conseil);
                exRow.getChildren().addAll(num, exInfo);
                exercicesBox.getChildren().add(exRow);
                if (i < exercices.length() - 1) {
                    javafx.scene.shape.Line sep = new javafx.scene.shape.Line(0, 0, 400, 0);
                    sep.setStyle("-fx-stroke: #3a3a4e;");
                    exercicesBox.getChildren().add(sep);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ── PLAN SEMAINE ─────────────────────────────────
        VBox planBox = new VBox(8);
        planBox.setStyle(
                "-fx-background-color: #1a1a2e;" +
                        "-fx-background-radius: 10; -fx-padding: 14;" +
                        "-fx-border-color: #2a2a3e; -fx-border-radius: 10; -fx-border-width: 1;");
        Label planTitle = new Label("📅 Plan de la semaine :");
        planTitle.setStyle(
                "-fx-text-fill: #7d3c98; -fx-font-weight: bold; -fx-font-size: 12px;");
        planBox.getChildren().add(planTitle);

        try {
            String planJson = reco.optString("plan_semaine", "");
            if (!planJson.isEmpty() && !planJson.equals("null")) {
                JSONArray plan = new JSONArray(planJson);
                for (int i = 0; i < plan.length(); i++) {
                    JSONObject jour = plan.getJSONObject(i);
                    String intensite = jour.optString("intensite", "Moyenne");
                    String couleurInt = intensite.equalsIgnoreCase("Élevée") ? "#e74c3c" :
                            intensite.equalsIgnoreCase("Moyenne") ? "#f39c12" : "#27ae60";
                    HBox jourRow = new HBox(10);
                    jourRow.setAlignment(Pos.CENTER_LEFT);
                    jourRow.setStyle(
                            "-fx-background-color: #2a2a3e;" +
                                    "-fx-background-radius: 8; -fx-padding: 8 12;");
                    Label lblJour = new Label(jour.optString("jour", ""));
                    lblJour.setStyle(
                            "-fx-text-fill: white; -fx-font-weight: bold;" +
                                    "-fx-font-size: 11px; -fx-pref-width: 70;");
                    Label lblSeance = new Label(jour.optString("seance", ""));
                    lblSeance.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 11px;");
                    HBox.setHgrow(lblSeance, Priority.ALWAYS);
                    Label lblDuree = new Label("⏱ " + jour.optString("duree", ""));
                    lblDuree.setStyle("-fx-text-fill: #888888; -fx-font-size: 10px;");
                    Label lblInt = new Label(intensite);
                    lblInt.setStyle(
                            "-fx-background-color: " + couleurInt + ";" +
                                    "-fx-text-fill: white; -fx-padding: 2 8;" +
                                    "-fx-background-radius: 6; -fx-font-size: 9px; -fx-font-weight: bold;");
                    jourRow.getChildren().addAll(lblJour, lblSeance, lblDuree, lblInt);
                    planBox.getChildren().add(jourRow);
                }
            } else {
                Label vide = new Label("Plan semaine non disponible");
                vide.setStyle("-fx-text-fill: #555555; -fx-font-size: 11px;");
                planBox.getChildren().add(vide);
            }
        } catch (Exception e) {
            Label err = new Label("Plan semaine non disponible");
            err.setStyle("-fx-text-fill: #555555; -fx-font-size: 11px;");
            planBox.getChildren().add(err);
        }

        // ── ÉTAPE C — RÉPONDRE AU COACH ──────────────────
        VBox reponseBox = new VBox(8);
        reponseBox.setStyle(

                        "-fx-control-inner-background: #2a2a3e;" +
                                "-fx-text-fill: white;" +
                                "-fx-prompt-text-fill: #666666;" +
                                "-fx-highlight-fill: #7d3c98;" +
                                "-fx-border-color: #3a3a5e;" +
                                "-fx-border-radius: 8;" +
                                "-fx-background-radius: 8;" +
                                "-fx-font-size: 12px;");

        Label reponseTitle = new Label("💬 Répondre à votre coach :");
        reponseTitle.setStyle(
                "-fx-text-fill: #aaaaaa; -fx-font-weight: bold; -fx-font-size: 12px;");

        // Afficher réponse existante si déjà envoyée
        String reponseExistante = reco.optString("reponse_user", "");

        if (!reponseExistante.isEmpty()) {
            // Déjà répondu → afficher la réponse
            Label dejaRep = new Label("✅ Votre réponse envoyée :");
            dejaRep.setStyle(
                    "-fx-text-fill: #44cc88; -fx-font-size: 11px; -fx-font-weight: bold;");
            Label txtRep = new Label("\"" + reponseExistante + "\"");
            txtRep.setWrapText(true);
            txtRep.setStyle(
                    "-fx-text-fill: #cccccc; -fx-font-size: 11px; -fx-font-style: italic;");
            reponseBox.getChildren().addAll(reponseTitle, dejaRep, txtRep);

        } else {
            // Pas encore répondu → afficher champ de saisie
            TextArea txtReponse = new TextArea();
            txtReponse.setPromptText("Écrivez votre retour au coach...");
            txtReponse.setPrefRowCount(3);
            txtReponse.setWrapText(true);
            txtReponse.setStyle(
                    "-fx-background-color: #1a1a2e;" +
                            "-fx-text-fill: white;" +
                            "-fx-prompt-text-fill: #555555;" +
                            "-fx-border-color: #3a3a5e;" +
                            "-fx-border-radius: 8;" +
                            "-fx-background-radius: 8;" +
                            "-fx-font-size: 12px;");

            // Boutons réponses rapides
            HBox boutonsRapides = new HBox(8);
            boutonsRapides.setAlignment(Pos.CENTER_LEFT);
            String[] rapides = {"👍 Super programme !", "💪 Je commence demain", "❓ J'ai une question"};
            for (String texte : rapides) {
                Button btnRapide = new Button(texte);
                btnRapide.setStyle(
                        "-fx-background-color: #2a2a3e;" +
                                "-fx-text-fill: #aaaaaa;" +
                                "-fx-background-radius: 6;" +
                                "-fx-padding: 4 10;" +
                                "-fx-font-size: 10px;" +
                                "-fx-cursor: hand;");
                btnRapide.setOnAction(ev -> txtReponse.setText(texte));
                boutonsRapides.getChildren().add(btnRapide);
            }

            Button btnEnvoyer = new Button("📤 Envoyer ma réponse");
            btnEnvoyer.setStyle(
                    "-fx-background-color: #7d3c98;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 8 16;" +
                            "-fx-cursor: hand;" +
                            "-fx-font-size: 12px;");

            Label lblConfirm = new Label("");
            lblConfirm.setStyle("-fx-text-fill: #44cc88; -fx-font-size: 11px;");

            btnEnvoyer.setOnAction(ev -> {
                String texte = txtReponse.getText().trim();
                if (texte.isEmpty()) {
                    lblConfirm.setText("⚠️ Écrivez un message avant d'envoyer.");
                    lblConfirm.setStyle("-fx-text-fill: #ffaa00; -fx-font-size: 11px;");
                    return;
                }
                boolean ok = coachUserService.envoyerReponseUser(
                        reco.getInt("id"), texte);
                if (ok) {
                    lblConfirm.setText("✅ Réponse envoyée au coach !");
                    lblConfirm.setStyle("-fx-text-fill: #44cc88; -fx-font-size: 11px;");
                    btnEnvoyer.setDisable(true);
                    txtReponse.setDisable(true);
                    boutonsRapides.setDisable(true);
                } else {
                    lblConfirm.setText("❌ Erreur lors de l'envoi.");
                    lblConfirm.setStyle("-fx-text-fill: #ff4444; -fx-font-size: 11px;");
                }
            });

            reponseBox.getChildren().addAll(
                    reponseTitle, boutonsRapides, txtReponse, btnEnvoyer, lblConfirm);
        }

        carte.getChildren().addAll(
                header, message, nutritionBox, exercicesBox, planBox, reponseBox);
        return carte;
    }

    @FXML
    private void retourMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/MenuUser.fxml"));
            Parent root = loader.load();
            MenuUserController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            listeContainer.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}