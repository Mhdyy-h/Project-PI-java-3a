package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import org.example.model.User;
import org.example.model.SeanceSport;
import org.example.service.ServiceSeanceSport;

import java.io.IOException;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;
import org.example.controller.MessagerieController;
public class MenuCoachController {

    @FXML private Label totalSeancesLabel;
    @FXML private Label moyenneDureeLabel;
    @FXML private Label totalMedaillesLabel;

    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void initialize() {

        // ✅ User temporaire pour tester sans login
        // APRÈS
        if (currentUser == null) {
            currentUser = new User(2, "Coach Ahmed", "ahmed@test.com");
            currentUser.setRoles("[\"ROLE_COACH\"]");
        }

        new Thread(() -> {
            try {
                List<SeanceSport> seances = new ServiceSeanceSport().afficherAll();
                double moy = seances.stream()
                        .mapToInt(SeanceSport::getDureeMinutes)
                        .average().orElse(0);
                long or = seances.stream()
                        .filter(s -> "Or".equalsIgnoreCase(s.getMedailleObtenue()))
                        .count();
                javafx.application.Platform.runLater(() -> {
                    totalSeancesLabel.setText(String.valueOf(seances.size()));
                    moyenneDureeLabel.setText(String.format("%.0f", moy));
                    totalMedaillesLabel.setText(String.valueOf(or));
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    totalSeancesLabel.setText("0");
                    moyenneDureeLabel.setText("0");
                    totalMedaillesLabel.setText("0");
                });
            }
        }).start();
    }

    @FXML public void ouvrirSeances(MouseEvent e)     { naviguer("/view/AfficherSeance.fxml"); }
    @FXML public void ouvrirSeancesNav(ActionEvent e) { naviguer("/view/AfficherSeance.fxml"); }

    @FXML
    public void ouvrirMesUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/MesUsers.fxml"));
            Parent root = loader.load();

            MesUsersController ctrl = loader.getController();
            ctrl.setCoach(currentUser); // ✅ currentUser pas null maintenant

            totalSeancesLabel.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void naviguer(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            totalSeancesLabel.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Erreur navigation : " + e.getMessage());
        }
    }@FXML
    public void ouvrirProgression() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/ProgressionUser.fxml"));
            Parent root = loader.load();

            ProgressionUserController ctrl = loader.getController();
            ctrl.setCoach(currentUser);

            totalSeancesLabel.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }@FXML
    public void ouvrirAnalyseIA(ActionEvent e) {
        ouvrirAnalyseIAStage();
    }

    @FXML
    public void ouvrirAnalyseIAMouse(MouseEvent e) {
        ouvrirAnalyseIAStage();
    }

    private void ouvrirAnalyseIAStage() {
        try {
            if (currentUser == null)
                currentUser = new User(1, "Coach Test", "coach@test.com");

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/AnalyseIA.fxml"));
            Parent root = loader.load();

            AnalyseIAController ctrl = loader.getController();
            ctrl.setUtilisateur(currentUser.getId(), currentUser.getNomComplet());

            totalSeancesLabel.getScene().setRoot(root);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }@FXML
    public void ouvrirMessagerie(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/Messagerie.fxml"));
            Parent root = loader.load();
            MessagerieController ctrl = loader.getController();
            ctrl.setUser(currentUser);
            ((javafx.scene.Node) e.getSource()).getScene().setRoot(root);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}