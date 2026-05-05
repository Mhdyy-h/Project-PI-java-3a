package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.controller.MenuUserController;
import org.example.model.Session;
import org.example.model.User;

public class MainUser extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Session.role = "USER";

        // ── User fictif pour tester ──────────────────────────────
        User userTest = new User(1, "Ahmed Ben Ali", "ahmed@sport.com");

        // ── Charger FXML avec FXMLLoader (pas load() direct) ─────
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/MenuUser.fxml"));
        Parent root = loader.load();

        // ── Passer le user au controller ─────────────────────────
        MenuUserController ctrl = loader.getController();
        ctrl.setCurrentUser(userTest);  // ← LIGNE CRITIQUE

        Scene scene = new Scene(root, 1100, 700);
        stage.setTitle("Gestion Sport - Utilisateur");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}