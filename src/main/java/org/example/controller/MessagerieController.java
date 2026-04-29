package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.model.User;
import org.example.service.CoachUserService;
import org.example.service.MessageService;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class MessagerieController {

    @FXML private VBox   listeContacts;
    @FXML private VBox   listeMessages;
    @FXML private ScrollPane scrollMessages;
    @FXML private TextArea   txtMessage;
    @FXML private Label      lblNomContact;
    @FXML private Label      lblStatutContact;

    private User currentUser;
    private User contactSelectionne;
    private MessageService messageService;
    private CoachUserService coachUserService;
    private javafx.animation.Timeline refreshTimer;

    public void setUser(User user) {
        this.currentUser = user;
        this.messageService  = new MessageService();
        this.coachUserService = new CoachUserService();
        chargerContacts();
        demarrerRefresh();
    }

    private void chargerContacts() {
        listeContacts.getChildren().clear();
        List<User> contacts = coachUserService.getContactsMessage(currentUser.getId());

        if (contacts.isEmpty()) {
            Label vide = new Label("Aucun contact");
            vide.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px; -fx-padding: 10;");
            listeContacts.getChildren().add(vide);
            return;
        }

        for (User contact : contacts) {
            int nonLus = messageService.compterNonLusDepuis(contact.getId(), currentUser.getId());
            listeContacts.getChildren().add(creerCarteContact(contact, nonLus));
        }
    }

    private HBox creerCarteContact(User contact, int nonLus) {
        HBox carte = new HBox(10);
        carte.setAlignment(Pos.CENTER_LEFT);
        carte.setPadding(new Insets(10, 12, 10, 12));
        carte.setStyle("-fx-background-radius: 10; -fx-cursor: hand;");

        Label avatar = new Label("👤");
        avatar.setStyle("-fx-font-size: 20px;");

        VBox info = new VBox(2);
        Label nom = new Label(contact.getNomComplet());
        nom.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
        info.getChildren().add(nom);
        HBox.setHgrow(info, Priority.ALWAYS);

        carte.getChildren().addAll(avatar, info);

        if (nonLus > 0) {
            Label badge = new Label(String.valueOf(nonLus));
            badge.setStyle("-fx-background-color: #7d3c98; -fx-text-fill: white;" +
                    "-fx-font-size: 10px; -fx-font-weight: bold;" +
                    "-fx-background-radius: 10; -fx-padding: 2 6;");
            carte.getChildren().add(badge);
        }

        carte.setOnMouseClicked(e -> selectionnerContact(contact, carte));
        carte.setOnMouseEntered(e -> carte.setStyle(
                "-fx-background-color: #2a2a3e; -fx-background-radius: 10; -fx-cursor: hand;"));
        carte.setOnMouseExited(e -> {
            if (contactSelectionne == null || contactSelectionne.getId() != contact.getId())
                carte.setStyle("-fx-background-radius: 10; -fx-cursor: hand;");
        });

        return carte;
    }

    private void selectionnerContact(User contact, HBox carte) {
        // Reset style toutes les cartes
        listeContacts.getChildren().forEach(n ->
                n.setStyle("-fx-background-radius: 10; -fx-cursor: hand;"));
        carte.setStyle("-fx-background-color: #2a2a3e; -fx-background-radius: 10; -fx-cursor: hand;");

        contactSelectionne = contact;
        lblNomContact.setText(contact.getNomComplet());
        lblStatutContact.setText("🟢 En ligne");

        messageService.marquerLus(contact.getId(), currentUser.getId());
        chargerMessages();
        chargerContacts(); // Rafraîchir badges
    }

    private void chargerMessages() {
        if (contactSelectionne == null) return;
        listeMessages.getChildren().clear();

        List<JSONObject> messages = messageService.getConversation(
                currentUser.getId(), contactSelectionne.getId());

        for (JSONObject msg : messages) {
            listeMessages.getChildren().add(creerBulle(msg));
        }

        // Scroller en bas
        javafx.application.Platform.runLater(() ->
                scrollMessages.setVvalue(1.0));
    }

    private HBox creerBulle(JSONObject msg) {
        boolean estMoi = msg.getInt("expediteur_id") == currentUser.getId();

        HBox ligne = new HBox();
        ligne.setAlignment(estMoi ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        Label bulle = new Label(msg.getString("contenu"));
        bulle.setWrapText(true);
        bulle.setMaxWidth(400);
        bulle.setPadding(new Insets(10, 14, 10, 14));
        bulle.setStyle(
                "-fx-background-color: " + (estMoi ? "#7d3c98" : "#2a2a3e") + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-background-radius: " + (estMoi ? "18 4 18 18" : "4 18 18 18") + ";");

        VBox conteneur = new VBox(4);
        conteneur.setAlignment(estMoi ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        Label date = new Label(msg.getString("date_envoi").substring(11, 16));
        date.setStyle("-fx-text-fill: #555555; -fx-font-size: 10px;");

        conteneur.getChildren().addAll(bulle, date);
        ligne.getChildren().add(conteneur);
        return ligne;
    }

    @FXML
    private void envoyerMessage() {
        if (contactSelectionne == null) return;
        String contenu = txtMessage.getText().trim();
        if (contenu.isEmpty()) return;

        messageService.envoyerMessage(currentUser.getId(), contactSelectionne.getId(), contenu);
        txtMessage.clear();
        chargerMessages();
    }

    private void demarrerRefresh() {
        refreshTimer = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                        javafx.util.Duration.seconds(10),
                        e -> {
                            chargerContacts();
                            if (contactSelectionne != null) chargerMessages();
                        }));
        refreshTimer.setCycleCount(javafx.animation.Animation.INDEFINITE);
        refreshTimer.play();
    }

    @FXML

    private void retourMenu() {
        if (refreshTimer != null) refreshTimer.stop();
        try {
            // ← LIGNE 179 : remplace l'ancienne ligne par ça
            String fxml = (currentUser.getRoles() != null && currentUser.getRoles().contains("coach"))
                    ? "/view/MenuCoach.fxml" : "/view/MenuUser.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            // ← LIGNE 183 : remplace l'ancienne ligne par ça
            if (currentUser.getRoles() != null && currentUser.getRoles().contains("coach")) {
                MenuCoachController ctrl = loader.getController();
                ctrl.setCurrentUser(currentUser);
            } else {
                MenuUserController ctrl = loader.getController();
                ctrl.setCurrentUser(currentUser);
            }
            listeContacts.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    }
