package org.example.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.model.Session;
import org.example.model.User;

public class AppLauncher {

    // Appelé par Login si rôle = COACH
    public static void lancerCoach(Stage stage) throws Exception {
        Session.role = "COACH";
        Parent root = FXMLLoader.load(
                AppLauncher.class.getResource("/view/MenuCoach.fxml")
        );
        Scene scene = new Scene(root, 1100, 700);
        stage.setTitle("Gestion Sport - Coach");
        stage.setScene(scene);
        stage.show();
    }

    // Appelé par Login si rôle = USER
    public static void lancerUser(Stage stage, User user) throws Exception {
        Session.role = "USER";
        FXMLLoader loader = new FXMLLoader(
                AppLauncher.class.getResource("/view/MenuUser.fxml")
        );
        Parent root = loader.load();
        MenuUserController ctrl = loader.getController();
        ctrl.setCurrentUser(user);
        Scene scene = new Scene(root, 1100, 700);
        stage.setTitle("Gestion Sport - Utilisateur");
        stage.setScene(scene);
        stage.show();
    }
}