package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TestApp extends Application {
    
    @Override
    public void start(Stage stage) {
        VBox root = new VBox(20);
        root.setStyle("-fx-padding: 20; -fx-alignment: center;");
        
        Label title = new Label("BioSync Test");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        Button testButton = new Button("Test Database Connection");
        testButton.setOnAction(e -> {
            boolean connected = org.example.dao.UserDAO.testConnection();
            if (connected) {
                title.setText("??? Connection Successful!");
                title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: green;");
            } else {
                title.setText("??? Connection Failed!");
                title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: red;");
            }
        });
        
        root.getChildren().addAll(title, testButton);
        
        Scene scene = new Scene(root, 400, 300);
        stage.setTitle("BioSync Test");
        stage.setScene(scene);
        stage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
