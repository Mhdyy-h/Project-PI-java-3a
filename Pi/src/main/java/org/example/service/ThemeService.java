package org.example.service;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de gestion du thème Dark/Light.
 * Applique le thème à toutes les scènes de l'application.
 */
public class ThemeService {

    private static ThemeService instance;
    private boolean darkMode = false;
    private final List<Scene> registeredScenes = new ArrayList<>();

    private ThemeService() {}

    public static ThemeService getInstance() {
        if (instance == null) {
            instance = new ThemeService();
        }
        return instance;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void toggleDarkMode() {
        darkMode = !darkMode;
        applyToAll();
    }

    public void setDarkMode(boolean dark) {
        this.darkMode = dark;
        applyToAll();
    }

    /**
     * Enregistre une scène pour qu'elle suive le thème.
     */
    public void registerScene(Scene scene) {
        if (scene != null && !registeredScenes.contains(scene)) {
            registeredScenes.add(scene);
            applyToScene(scene);
        }
    }

    /**
     * Applique le thème actuel à toutes les scènes enregistrées.
     */
    private void applyToAll() {
        for (Scene scene : registeredScenes) {
            applyToScene(scene);
        }
    }

    /**
     * Applique le thème à une scène spécifique.
     */
    private void applyToScene(Scene scene) {
        if (scene == null) return;

        javafx.scene.Parent root = scene.getRoot();
        if (root == null) return;

        // Appliquer ou retirer le dark-theme sur root et tous les enfants
        applyDarkModeToNode(root, darkMode);

        // Gérer les stylesheets
        URL darkCss = getClass().getResource("/dark-theme.css");
        if (darkCss != null) {
            String darkCssStr = darkCss.toExternalForm();
            if (darkMode) {
                if (!scene.getStylesheets().contains(darkCssStr)) {
                    scene.getStylesheets().add(darkCssStr);
                }
            } else {
                scene.getStylesheets().remove(darkCssStr);
            }
        }
    }

    /**
     * Applique ou retire le mode sombre sur un nœud et tous ses enfants récursivement.
     */
    private void applyDarkModeToNode(javafx.scene.Node node, boolean dark) {
        if (node == null) return;

        // Appliquer/retirer la classe dark-mode
        if (dark) {
            if (!node.getStyleClass().contains("dark-mode")) {
                node.getStyleClass().add("dark-mode");
            }
        } else {
            node.getStyleClass().remove("dark-mode");
        }

        // Si c'est un Parent, traiter récursivement les enfants
        if (node instanceof javafx.scene.Parent) {
            for (javafx.scene.Node child : ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
                applyDarkModeToNode(child, dark);
            }
        }

        // Si c'est un Pane (VBox, HBox, etc.), traiter aussi ses enfants
        if (node instanceof Pane) {
            for (javafx.scene.Node child : ((Pane) node).getChildren()) {
                applyDarkModeToNode(child, dark);
            }
        }
    }

    /**
     * Nettoie les scènes qui ne sont plus valides.
     */
    public void cleanup() {
        registeredScenes.removeIf(scene -> {
            try {
                return scene.getWindow() == null || !scene.getWindow().isShowing();
            } catch (Exception e) {
                return true;
            }
        });
    }
}
