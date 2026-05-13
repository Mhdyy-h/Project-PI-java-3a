package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.controller.LoginController; // ✅ bon import
import org.example.dao.RateLimitingDAO;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        
        // 🔄 Clear rate limiter on startup for testing
        try {
            boolean cleared = RateLimitingDAO.clearAllAttempts();
            if (cleared) {
                System.out.println("🎉 Rate limiter cleared on startup - All users can now login!");
            }
        } catch (Exception e) {
            System.out.println("⚠️ Could not clear rate limiter: " + e.getMessage());
        }

        // ✅ Page de LOGIN (point d'entrée normal)
        FXMLLoader loaderLogin = new FXMLLoader(
                getClass().getResource("/view/login.fxml"));
        Parent rootLogin = loaderLogin.load();
        // LoginController n'a pas besoin de setCurrentUser ici
        // car l'utilisateur n'est pas encore connecté

        Scene sceneLogin = new Scene(rootLogin, 1100, 700);
        sceneLogin.getStylesheets().add(
                getClass().getResource("/styles/style.css").toExternalForm());

        primaryStage.setTitle("Gestion Sport - Connexion");
        primaryStage.setScene(sceneLogin);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}