package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.controller.MenuUserController;
import org.example.model.Session;
import org.example.model.User;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // ✅ Dashboard USER
        Session.role = "USER";
        FXMLLoader loaderUser = new FXMLLoader(
                getClass().getResource("/view/MenuUser.fxml"));
        Parent rootUser = loaderUser.load();
        MenuUserController ctrl = loaderUser.getController();
        ctrl.setCurrentUser(new User(1, "Ahmed", "ahmed@sport.com"));
        Scene sceneUser = new Scene(rootUser, 1100, 700);
        sceneUser.getStylesheets().add(
                getClass().getResource("/styles/style.css").toExternalForm());
        primaryStage.setTitle("Gestion Sport - Utilisateur");
        primaryStage.setScene(sceneUser);
        primaryStage.centerOnScreen();
        primaryStage.show();

        // ✅ Dashboard COACH
        Session.role = "COACH";
        FXMLLoader loaderCoach = new FXMLLoader(
                getClass().getResource("/view/MenuCoach.fxml"));
        Parent rootCoach = loaderCoach.load();
        Stage stageCoach = new Stage();
        Scene sceneCoach = new Scene(rootCoach, 1100, 700);
        sceneCoach.getStylesheets().add(
                getClass().getResource("/styles/style.css").toExternalForm());
        stageCoach.setTitle("Gestion Sport - Coach");
        stageCoach.setScene(sceneCoach);
        stageCoach.centerOnScreen();
        stageCoach.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}