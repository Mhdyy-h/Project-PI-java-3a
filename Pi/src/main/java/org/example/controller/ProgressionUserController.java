package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import org.example.model.SeanceSport;
import org.example.model.User;
import org.example.service.CoachIAService;
import org.example.service.CoachUserService;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class ProgressionUserController {

    @FXML private Label  lblNomUser;
    @FXML private Label  lblTotalSeances;
    @FXML private Label  lblDureeMoy;
    @FXML private Label  lblAnalyseIA;
    @FXML private VBox   listeUsersContainer;

    private CoachUserService coachUserService;
    private CoachIAService   coachIAService;
    private User             currentCoach;

    @FXML
    public void initialize() {
        coachUserService = new CoachUserService();
        coachIAService   = new CoachIAService();
    }

    public void setCoach(User coach) {
        this.currentCoach = coach;
        chargerAthletes();
    }

    private void chargerAthletes() {
        listeUsersContainer.getChildren().clear();
        List<User> users = coachUserService.getUsersDuCoach(currentCoach.getId());

        if (users.isEmpty()) {
            Label vide = new Label("Aucun athlète assigné");
            vide.setStyle("-fx-text-fill: #888888; -fx-font-size: 13px;");
            listeUsersContainer.getChildren().add(vide);
            return;
        }

        for (User user : users) {
            listeUsersContainer.getChildren().add(creerCarteProgression(user));
        }
    }

    private VBox creerCarteProgression(User user) {

        List<SeanceSport> seances = coachUserService.getSeancesUser(user.getId());
        int total = seances.size();
        double dureeMoy = seances.stream()
                .mapToInt(SeanceSport::getDureeMinutes)
                .average().orElse(0);
        long medailles = seances.stream()
                .filter(s -> "Or".equalsIgnoreCase(s.getMedailleObtenue()))
                .count();

        // ── Créer la carte principale ─────────────────────
        VBox carte = new VBox(14);
        carte.setStyle(
                "-fx-background-color: #1a1a2e;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 20;" +
                        "-fx-border-color: #2a2a3e;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-width: 1;");

        // ── Header ────────────────────────────────────────
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Label avatar = new Label("👤");
        avatar.setStyle("-fx-font-size: 22px;");
        VBox info = new VBox(3);
        Label nom = new Label(user.getNomComplet());
        nom.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        Label email = new Label(user.getEmail());
        email.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");
        info.getChildren().addAll(nom, email);
        header.getChildren().addAll(avatar, info);

        // ── Statut lecture ─────────────────────────────────
        boolean derniereVue = coachUserService.derniereRecoVue(
                currentCoach.getId(), user.getId());

        HBox statutLecture = new HBox(8);
        statutLecture.setAlignment(Pos.CENTER_LEFT);

        Label iconStatut = new Label(derniereVue ? "✅" : "⏳");
        iconStatut.setStyle("-fx-font-size: 13px;");

        Label txtStatut = new Label(derniereVue
                ? "Dernière recommandation lue"
                : "En attente de lecture...");
        txtStatut.setStyle(
                "-fx-text-fill: " + (derniereVue ? "#44cc88" : "#ffaa00") + ";" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-style: italic;");

        statutLecture.getChildren().addAll(iconStatut, txtStatut);

        // ── Stats ─────────────────────────────────────────
        HBox stats = new HBox(16);
        stats.setAlignment(Pos.CENTER_LEFT);
        stats.getChildren().addAll(
                creerStat("🏃", String.valueOf(total), "Séances"),
                creerStat("⏱️", String.format("%.0f", dureeMoy), "Min/moy"),
                creerStat("🥇", String.valueOf(medailles), "Médailles")
        );

        // ── Niveau ────────────────────────────────────────
        String niveau = total >= 20 ? "🔥 Expert" :
                total >= 10 ? "💪 Intermédiaire" :
                total >= 5  ? "📈 Débutant+" : "🌱 Débutant";
        Label lblNiveau = new Label(niveau);
        lblNiveau.setStyle(
                "-fx-background-color: #2a2a3e;" +
                        "-fx-text-fill: #44cc88;" +
                        "-fx-padding: 6 12;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;");

        // ── Bouton IA ─────────────────────────────────────
        javafx.scene.control.Button btnIA =
                new javafx.scene.control.Button("🤖 Envoyer recommandation");
        btnIA.setStyle(
                "-fx-background-color: #7d3c98;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 8 16;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 12px;");

        Label lblIA = new Label("");
        lblIA.setWrapText(true);
        lblIA.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12px;");

        List<SeanceSport> seancesFinal = seances;

        btnIA.setOnAction(e -> {
            btnIA.setText("⏳ Génération en cours...");
            btnIA.setDisable(true);

            new Thread(() -> {
                try {
                    String recommandationJson = coachIAService
                            .genererRecommandation(currentCoach, user, seancesFinal);

                    if (recommandationJson == null) {
                        javafx.application.Platform.runLater(() -> {
                            lblIA.setText("❌ Erreur API — réponse nulle");
                            btnIA.setText("🤖 Envoyer recommandation");
                            btnIA.setDisable(false);
                        });
                        return;
                    }

                    String clean = recommandationJson
                            .replace("```json", "")
                            .replace("```", "")
                            .trim();

                    JSONObject data = new JSONObject(clean);
                    String titre     = data.optString("titre",    "Recommandation");
                    String message   = data.optString("message",  "");
                    String nutrition = data.optString("nutrition", "");

                    String exercices = data.optJSONArray("exercices") != null
                            ? data.getJSONArray("exercices").toString() : "[]";

                    String planSemaine = data.optJSONArray("plan_semaine") != null
                            ? data.getJSONArray("plan_semaine").toString() : "[]";

                    coachUserService.sauvegarderRecommandation(
                            currentCoach.getId(), user.getId(),
                            titre, message, exercices,
                            nutrition, planSemaine);

                    javafx.application.Platform.runLater(() -> {
                        lblIA.setText(
                                "✅ Recommandation envoyée à " +
                                        user.getNomComplet() + " !\n\n" +
                                        "📋 " + titre + "\n" +
                                        "💬 " + message);
                        btnIA.setText("✅ Envoyée !");
                        btnIA.setStyle(
                                "-fx-background-color: #44cc88;" +
                                        "-fx-text-fill: white;" +
                                        "-fx-background-radius: 10;" +
                                        "-fx-padding: 8 16;" +
                                        "-fx-cursor: hand;" +
                                        "-fx-font-size: 12px;");
                        btnIA.setDisable(false);

                        iconStatut.setText("⏳");
                        txtStatut.setText("En attente de lecture...");
                        txtStatut.setStyle(
                                "-fx-text-fill: #ffaa00;" +
                                        "-fx-font-size: 11px;" +
                                        "-fx-font-style: italic;");

                        // ✅ Rafraîchir la section réponse après envoi
                        rafraichirReponse(carte, user);
                    });

                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> {
                        lblIA.setText("❌ Erreur : " + ex.getMessage());
                        btnIA.setText("🤖 Envoyer recommandation");
                        btnIA.setDisable(false);
                    });
                }
            }).start();
        });

        // ── Assembler la carte ────────────────────────────
        carte.getChildren().addAll(header, statutLecture, stats, lblNiveau, btnIA, lblIA);

        // ── Réponse athlète (au bas de la carte) ──────────
        ajouterSectionReponse(carte, user);

        return carte;
    }

    /**
     * Ajoute la section "Réponse de l'athlète" au bas d'une carte.
     */
    private void ajouterSectionReponse(VBox carte, User user) {
        // Récupérer la dernière recommandation avec reponse_user
        JSONObject derniereReco = coachUserService.getDerniereRecommandation(
                currentCoach.getId(), user.getId());

        if (derniereReco == null) return;

        String reponse = derniereReco.optString("reponse_user", "");
        String dateRep = derniereReco.optString("date_reponse", "");

        // Séparateur
        Separator sep = new Separator();
        sep.setPadding(new Insets(5, 0, 5, 0));
        sep.setStyle("-fx-background-color: #3a3a5e;");
        carte.getChildren().add(sep);

        if (!reponse.isEmpty()) {
            // En-tête
            Label lblTitreRep = new Label("💬 Réponse de l'athlète :");
            lblTitreRep.setStyle(
                    "-fx-text-fill: #a29bfe;" +
                            "-fx-font-size: 12px;" +
                            "-fx-font-weight: bold;");
            carte.getChildren().add(lblTitreRep);

            // Texte réponse
            Label lblReponse = new Label(reponse);
            lblReponse.setWrapText(true);
            lblReponse.setMaxWidth(Double.MAX_VALUE);
            lblReponse.setStyle(
                    "-fx-text-fill: #dfe6e9;" +
                            "-fx-font-size: 12px;" +
                            "-fx-background-color: #2d2d44;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 10;");
            carte.getChildren().add(lblReponse);

            // Date réponse
            if (!dateRep.isEmpty()) {
                Label lblDateRep = new Label("🕐 " + dateRep);
                lblDateRep.setStyle(
                        "-fx-text-fill: #636e72;" +
                                "-fx-font-size: 10px;");
                carte.getChildren().add(lblDateRep);
            }

        } else {
            // Pas encore répondu
            Label lblEnAttente = new Label("⏳ En attente de réponse de l'athlète...");
            lblEnAttente.setStyle(
                    "-fx-text-fill: #636e72;" +
                            "-fx-font-size: 11px;" +
                            "-fx-font-style: italic;");
            carte.getChildren().add(lblEnAttente);
        }
    }

    /**
     * Rafraîchit uniquement la section réponse après un nouvel envoi.
     * Supprime l'ancienne section (Separator + labels) et recrée.
     */
    private void rafraichirReponse(VBox carte, User user) {
        // Supprimer tous les noeuds après le lblIA (index 5)
        // lblIA est à l'index 5 → on garde [0..5] et on recrée la section
        if (carte.getChildren().size() > 6) {
            carte.getChildren().remove(6, carte.getChildren().size());
        }
        ajouterSectionReponse(carte, user);
    }

    private VBox creerStat(String emoji, String valeur, String label) {
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER);
        box.setStyle(
                "-fx-background-color: #2a2a3e;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 10 16;");
        Label ico = new Label(emoji + " " + valeur);
        ico.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #888888; -fx-font-size: 10px;");
        box.getChildren().addAll(ico, lbl);
        return box;
    }

    @FXML
    private void retourMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/MenuCoach.fxml"));
            Parent root = loader.load();
            MenuCoachController ctrl = loader.getController();
            ctrl.setCurrentUser(currentCoach);
            listeUsersContainer.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}