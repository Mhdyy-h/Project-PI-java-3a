package org.example.controller;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import org.example.model.User;
import org.example.service.ServiceExercice;
import org.example.service.ServiceSeanceSport;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.Animation;
import javafx.util.Duration;
import org.example.service.CoachUserService;
import java.io.IOException;

public class MenuUserController {

    @FXML private Label totalExercicesLabel;
    @FXML private Label totalSeancesLabel;

    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }


    @FXML private Label badgeRecommandations; // ← ajoute cet @FXML

    @FXML
    public void initialize() {
        if (currentUser == null)
            currentUser = new User(1, "Utilisateur Test", "test@sport.com");

        // ── Stats normales ────────────────────────
        new Thread(() -> {
            try {
                int nbEx = new ServiceExercice().afficherAll().size();
                int nbSe = new ServiceSeanceSport().afficherAll().size();
                javafx.application.Platform.runLater(() -> {
                    totalExercicesLabel.setText(String.valueOf(nbEx));
                    totalSeancesLabel.setText(String.valueOf(nbSe));
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    totalExercicesLabel.setText("0");
                    totalSeancesLabel.setText("0");
                });
            }
        }).start();

        // ── Vérification notifications en temps réel ──
        startNotificationChecker();
    }

    // ── Vérifie toutes les 10 secondes ───────────────
    private javafx.animation.Timeline notificationTimer;

    private void startNotificationChecker() {
        notificationTimer = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                        javafx.util.Duration.seconds(10),
                        e -> verifierNouvellesRecommandations()
                )
        );
        notificationTimer.setCycleCount(javafx.animation.Animation.INDEFINITE);
        notificationTimer.play();

        // Vérifier immédiatement au démarrage
        verifierNouvellesRecommandations();
    }

    private void verifierNouvellesRecommandations() {
        if (currentUser == null) return;
        new Thread(() -> {
            try {
                CoachUserService service = new CoachUserService();
                int count = service.compterNonVues(currentUser.getId());
                javafx.application.Platform.runLater(() -> {
                    if (count > 0) {
                        badgeRecommandations.setText(String.valueOf(count));
                        badgeRecommandations.setVisible(true);
                    } else {
                        badgeRecommandations.setVisible(false);
                    }
                });
            } catch (Exception e) {
                // Silencer les erreurs de table manquante pour éviter le spam console
            }
        }).start();
    }
    // ── Navigation existante ─────────────────────────────────────
    @FXML public void ouvrirExercices(MouseEvent e)       { naviguerMouse(e, "/view/AfficherExercice.fxml"); }
    @FXML public void ouvrirVoirSeances(MouseEvent e)     { naviguerMouse(e, "/view/VoirSeances.fxml"); }
    @FXML public void ouvrirExercicesNav(ActionEvent e)   { naviguerAction(e, "/view/AfficherExercice.fxml"); }
    @FXML public void ouvrirVoirSeancesNav(ActionEvent e) { naviguerAction(e, "/view/VoirSeances.fxml"); }

    // ── Profil ───────────────────────────────────────────────────
    @FXML public void ouvrirMonProfilMouse(MouseEvent e)  { ouvrirProfilStage(e.getSource()); }
    @FXML public void ouvrirMonProfil(ActionEvent e)      { ouvrirProfilStage(e.getSource()); }

    private void ouvrirProfilStage(Object source) {
        try {
            if (currentUser == null)
                currentUser = new User(1, "Utilisateur Test", "test@sport.com");

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/ProfilProgression.fxml"));
            Parent root = loader.load();

            ProfilProgressionController ctrl = loader.getController();
            ctrl.chargerProfil(currentUser);

            ((Node) source).getScene().setRoot(root);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // ── Surcharge ────────────────────────────────────────────────
    @FXML public void ouvrirSurchargeMouse(MouseEvent e)  { ouvrirSurchargeStage(e.getSource()); }
    @FXML public void ouvrirSurcharge(ActionEvent e)      { ouvrirSurchargeStage(e.getSource()); }

    private void ouvrirSurchargeStage(Object source) {
        try {
            if (currentUser == null)
                currentUser = new User(1, "Utilisateur Test", "test@sport.com");

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/Surcharge.fxml"));
            Parent root = loader.load();

            SurchargeController ctrl = loader.getController();
            ctrl.chargerAnalyse(currentUser);

            ((Node) source).getScene().setRoot(root);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // ── Indice Forme ─────────────────────────────────────────────
    @FXML public void ouvrirIndiceMouse(MouseEvent e)     { ouvrirIndiceStage(e.getSource()); }
    @FXML public void ouvrirIndice(ActionEvent e)         { ouvrirIndiceStage(e.getSource()); }

    private void ouvrirIndiceStage(Object source) {
        try {
            if (currentUser == null)
                currentUser = new User(1, "Utilisateur Test", "test@sport.com");

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/IndiceForme.fxml"));
            Parent root = loader.load();

            IndiceFormeController ctrl = loader.getController();
            ctrl.chargerIndice(currentUser);

            ((Node) source).getScene().setRoot(root);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // ── Coach IA ─────────────────────────────────────────────────
    @FXML public void ouvrirCoachMouse(MouseEvent e)      { ouvrirCoachIA(e.getSource()); }
    @FXML public void ouvrirCoach(ActionEvent e)          { ouvrirCoachIA(e.getSource()); }

    private void ouvrirCoachIA(Object source) {
        try {
            if (currentUser == null)
                currentUser = new User(1, "Utilisateur Test", "test@sport.com");

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/CoachIA.fxml"));
            Parent root = loader.load();

            CoachIAController ctrl = loader.getController();
            ctrl.setUser(currentUser);

            ((Node) source).getScene().setRoot(root);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // ── Recommandations ──────────────────────────────────────────
    @FXML public void ouvrirRecommandationsMouse(MouseEvent e) { ouvrirRecommandationsStage(e.getSource()); }
    @FXML public void ouvrirRecommandations(ActionEvent e)     { ouvrirRecommandationsStage(e.getSource()); }

    private void ouvrirRecommandationsStage(Object source) {
        try {
            if (currentUser == null)
                currentUser = new User(1, "Utilisateur Test", "test@sport.com");

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/Recommandations.fxml"));
            Parent root = loader.load();

            RecommandationsController ctrl = loader.getController();
            ctrl.setUser(currentUser);

            ((Node) source).getScene().setRoot(root);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // ── Profil Athlete ───────────────────────────────────────────
    @FXML
    public void ouvrirProfilAthlete(ActionEvent e) {
        try {
            if (currentUser == null)
                currentUser = new User(1, "Utilisateur Test", "test@sport.com");

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/ProfilAthlete.fxml"));
            Parent root = loader.load();

            ProfilAthleteController ctrl = loader.getController();
            ctrl.setUser(currentUser);

            ((Node) e.getSource()).getScene().setRoot(root);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // ── Graphiques Progression ───────────────────────────────────
    @FXML
    public void ouvrirGraphiques(ActionEvent e) {
        try {
            if (currentUser == null)
                currentUser = new User(1, "Utilisateur Test", "test@sport.com");

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/GraphiqueProgression.fxml"));
            Parent root = loader.load();

            GraphiqueProgressionController ctrl = loader.getController();
            ctrl.setUser(currentUser);

            ((Node) e.getSource()).getScene().setRoot(root);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // ── Utilitaires ──────────────────────────────────────────────
    private void naviguerMouse(MouseEvent e, String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            ((Node) e.getSource()).getScene().setRoot(root);
        } catch (IOException ex) {
            System.err.println("Erreur navigation : " + ex.getMessage());
        }
    }

    private void naviguerAction(ActionEvent e, String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            ((Node) e.getSource()).getScene().setRoot(root);
        } catch (IOException ex) {
            System.err.println("Erreur navigation : " + ex.getMessage());
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
    }@FXML
    public void retourDashboard(ActionEvent e) {
        if (currentUser == null) {
            currentUser = new User(1, "Utilisateur Test", "test@sport.com");
        }
        org.example.service.NavigationService.getInstance()
                .navigateToDashboard((Node) e.getSource(), currentUser);
    }
}