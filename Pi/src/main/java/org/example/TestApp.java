package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.dao.UserDAO;

public class TestApp extends Application {

    @Override
    public void start(Stage stage) {
        VBox root = new VBox(20);
        root.setStyle("-fx-padding: 40; -fx-alignment: center; -fx-background-color: #F5F7FA;");

        Label title = new Label("BioSync System Check");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label dbStatus = new Label("Database Status: Unknown");

        Button testButton = new Button("Check Connection");
        testButton.setStyle("-fx-background-color: #4285F4; -fx-text-fill: white; -fx-padding: 10 20;");

        testButton.setOnAction(e -> {
            if (UserDAO.testConnection()) {
                dbStatus.setText("Database Status: ONLINE ✅");
                dbStatus.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
            } else {
                dbStatus.setText("Database Status: OFFLINE ❌");
                dbStatus.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            }
        });

        root.getChildren().addAll(title, dbStatus, testButton);

        stage.setScene(new Scene(root, 400, 300));
        stage.setTitle("BioSync Test Tool");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}