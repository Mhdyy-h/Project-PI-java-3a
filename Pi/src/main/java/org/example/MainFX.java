package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.service.MonitoringService;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        MonitoringService.init();
        Parent root = FXMLLoader.load(
                getClass().getResource("/view/login.fxml"));
        primaryStage.setTitle("BioSync");

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setScene(scene);
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