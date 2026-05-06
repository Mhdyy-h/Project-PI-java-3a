package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.model.Session;

public class MainCoach extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Session.role = "COACH"; // ✅ Définir le rôle
        Parent root = FXMLLoader.load(
                getClass().getResource("/view/MenuCoach.fxml")
        );
        Scene scene = new Scene(root, 1100, 700);
        stage.setTitle("Gestion Sport - Coach");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}