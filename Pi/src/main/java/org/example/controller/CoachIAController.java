package org.example.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.model.User;
import org.example.service.CoachIAService;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CoachIAController {

    @FXML private VBox      messagesContainer;
    @FXML private TextField inputMessage;
    @FXML private Button    btnEnvoyer;
    @FXML private ScrollPane scrollPane;
    @FXML private Label     lblNomUser;

    private CoachIAService      coachService;
    private User                currentUser;
    private List<JSONObject>    historique;

    @FXML
    public void initialize() {
        coachService = new CoachIAService();
        historique   = new ArrayList<>();

        // Envoyer avec Entrée
        inputMessage.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                envoyerMessage();
            }
        });
    }

    public void setUser(User user) {
        this.currentUser = user;
        lblNomUser.setText("Coach IA de " + user.getNomComplet());
        // Message de bienvenue
        ajouterMessageCoach(
                "👋 Bonjour " + user.getNomComplet() + " ! Je suis votre coach " +
                        "sportif personnel. Je connais votre profil et vos séances. " +
                        "Posez-moi vos questions sur l'entraînement, la récupération " +
                        "ou la nutrition sportive !");
    }

    @FXML
    private void envoyerMessage() {
        String texte = inputMessage.getText().trim();
        if (texte.isEmpty()) return;

        // Afficher message utilisateur
        ajouterMessageUser(texte);
        inputMessage.clear();
        btnEnvoyer.setDisable(true);

        // Indicateur de chargement
        Label loading = new Label("⏳ Coach réfléchit...");
        loading.setStyle("-fx-text-fill: #888888; -fx-font-style: italic;");
        messagesContainer.getChildren().add(loading);
        scrollToBottom();

        // Appel API dans un thread séparé
        String messageFinal = texte;
        new Thread(() -> {
            try {
                // Ajouter à l'historique
                historique.add(new JSONObject()
                        .put("role", "user")
                        .put("content", messageFinal));

                String reponse = coachService.envoyerMessage(
                        currentUser, messageFinal, historique);

                // Ajouter réponse à l'historique
                historique.add(new JSONObject()
                        .put("role", "assistant")
                        .put("content", reponse));

                // Mettre à jour l'UI sur le thread JavaFX
                Platform.runLater(() -> {
                    messagesContainer.getChildren().remove(loading);
                    ajouterMessageCoach(reponse);
                    btnEnvoyer.setDisable(false);
                    scrollToBottom();
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    messagesContainer.getChildren().remove(loading);
                    ajouterMessageCoach(
                            "❌ Erreur de connexion. Vérifiez votre clé API.");
                    btnEnvoyer.setDisable(false);
                });
            }
        }).start();
    }

    // ── Message utilisateur (droite) ─────────────────────────────
    private void ajouterMessageUser(String texte) {
        HBox hbox = new HBox();
        hbox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Label msg = new Label(texte);
        msg.setWrapText(true);
        msg.setMaxWidth(450);
        msg.setStyle(
                "-fx-background-color: #7d3c98;" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 10 14;" +
                        "-fx-background-radius: 16 16 4 16;" +
                        "-fx-font-size: 13px;");

        hbox.getChildren().add(msg);
        hbox.setStyle("-fx-padding: 4 16;");
        messagesContainer.getChildren().add(hbox);
    }

    // ── Message coach (gauche) ────────────────────────────────────
    private void ajouterMessageCoach(String texte) {
        HBox hbox = new HBox(10);
        hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label avatar = new Label("🏋️");
        avatar.setStyle("-fx-font-size: 20px;");

        Label msg = new Label(texte);
        msg.setWrapText(true);
        msg.setMaxWidth(450);
        msg.setStyle(
                "-fx-background-color: #1a1a2e;" +
                        "-fx-text-fill: #cccccc;" +
                        "-fx-padding: 10 14;" +
                        "-fx-background-radius: 16 16 16 4;" +
                        "-fx-font-size: 13px;");

        hbox.getChildren().addAll(avatar, msg);
        hbox.setStyle("-fx-padding: 4 16;");
        messagesContainer.getChildren().add(hbox);
    }

    private void scrollToBottom() {
        Platform.runLater(() ->
                scrollPane.setVvalue(scrollPane.getVmax()));
    }

    @FXML
    private void retourDashboard() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/view/MenuUser.fxml"));
            messagesContainer.getScene().setRoot(root);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}