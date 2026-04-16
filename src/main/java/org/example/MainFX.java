package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(
                getClass().getResource("/Menu.fxml"));
        primaryStage.setTitle("Gestion Séances Sport");
        primaryStage.setScene(new Scene(root));
        primaryStage.setWidth(1280);
        primaryStage.setHeight(800);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(650);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}