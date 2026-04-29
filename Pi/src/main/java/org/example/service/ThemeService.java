package org.example.service;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service de gestion du thème Dark/Light.
 * Applique le thème à toutes les scènes de l'application.
 * Gère les remplacements de couleurs inline pour surpasser les style= FXML.
 */
public class ThemeService {

    private static ThemeService instance;
    private boolean darkMode = false;
    private final List<Scene> registeredScenes = new ArrayList<>();

    // Stocke les styles inline originaux pour pouvoir les restaurer
    private final Map<Node, String> originalStyles = new IdentityHashMap<>();

    // Couleurs claires -> couleurs sombres (mapping de remplacement)
    // Ordre important: les couleurs plus spécifiques doivent venir AVANT les plus génériques
    // Palette GitHub Dark: #0d1117 (root), #161b22 (surface), #21262d (elevated)
    private static final String[][] LIGHT_TO_DARK = {
            // ===== BACKGROUNDS - Page/Root (light → #0d1117) =====
            {"-fx-background-color: #f0f2f8", "-fx-background-color: #0d1117"},
            {"-fx-background-color: #e8f0fe", "-fx-background-color: #0d1117"},
            {"-fx-background-color: #e5e7eb", "-fx-background-color: #0d1117"},
            // ===== BACKGROUNDS - Cards/Panels (white → #161b22) =====
            {"-fx-background-color: white", "-fx-background-color: #161b22"},
            {"-fx-background-color: #ffffff", "-fx-background-color: #161b22"},
            {"-fx-background-color: #fff", "-fx-background-color: #161b22"},
            {"-fx-background-color: #f8fafc", "-fx-background-color: #161b22"},
            {"-fx-background-color: #f3f4f6", "-fx-background-color: #161b22"},
            {"-fx-background-color: #f9fafb", "-fx-background-color: #161b22"},
            {"-fx-background-color: #f0f5ff", "-fx-background-color: #161b22"},
            // ===== BACKGROUNDS - Elevated (light gray → #21262d) =====
            {"-fx-background-color: #f1f5f9", "-fx-background-color: #21262d"},
            {"-fx-background-color: #e2e8f0", "-fx-background-color: #21262d"},
            {"-fx-background-color: #e8edff", "-fx-background-color: #21262d"},
            {"-fx-background-color: #eef2ff", "-fx-background-color: #21262d"},
            {"-fx-background-color: #eff6ff", "-fx-background-color: #21262d"},
            // ===== BACKGROUNDS - Status tints (pastel → dark translucent) =====
            {"-fx-background-color: #d1fae5", "-fx-background-color: rgba(34, 197, 94, 0.15)"},
            {"-fx-background-color: #ede9fe", "-fx-background-color: rgba(139, 92, 246, 0.15)"},
            {"-fx-background-color: #fef3c7", "-fx-background-color: rgba(245, 158, 11, 0.15)"},
            {"-fx-background-color: #fffbeb", "-fx-background-color: rgba(245, 158, 11, 0.15)"},
            // ===== BACKGROUND (shorthand) =====
            {"-fx-background: #e8f0fe", "-fx-background: #0d1117"},
            {"-fx-background: #f0f2f8", "-fx-background: #0d1117"},
            // ===== TEXT FILLS - Dark text → Light text (#f0f6fc, #c9d1d9, #8b949e) =====
            {"-fx-text-fill: #1a1a2e", "-fx-text-fill: #f0f6fc"},
            {"-fx-text-fill: #111827", "-fx-text-fill: #f0f6fc"},
            {"-fx-text-fill: #374151", "-fx-text-fill: #c9d1d9"},
            {"-fx-text-fill: #4b5563", "-fx-text-fill: #c9d1d9"},
            {"-fx-text-fill: #6b7280", "-fx-text-fill: #8b949e"},
            {"-fx-text-fill: #9ca3af", "-fx-text-fill: #8b949e"},
            {"-fx-text-fill: #64748b", "-fx-text-fill: #6e7681"},
            {"-fx-text-fill: #475569", "-fx-text-fill: #6e7681"},
            // ===== BORDER COLORS (light → #30363d, #484f58) =====
            {"-fx-border-color: #e5e7eb", "-fx-border-color: #30363d"},
            {"-fx-border-color: #f0f2f8", "-fx-border-color: #30363d"},
            {"-fx-border-color: #d1d5db", "-fx-border-color: #30363d"},
            {"-fx-border-color: #e8edff", "-fx-border-color: #30363d"},
            {"-fx-border-color: #d1fae5", "-fx-border-color: rgba(34, 197, 94, 0.4)"},
            {"-fx-border-color: #ede9fe", "-fx-border-color: rgba(139, 92, 246, 0.4)"},
            {"-fx-border-color: #fbbf24", "-fx-border-color: rgba(245, 158, 11, 0.4)"},
            {"-fx-border-color: #f1f5f9", "-fx-border-color: #30363d"},
            {"-fx-border-color: #cbd5e1", "-fx-border-color: #484f58"},
            // ===== TEXT FILLS - Success/Warning/Error colors =====
            {"-fx-text-fill: #065f46", "-fx-text-fill: #86efac"},
            {"-fx-text-fill: #166534", "-fx-text-fill: #86efac"},
            {"-fx-text-fill: #92400e", "-fx-text-fill: #fde68a"},
            {"-fx-text-fill: #c2410c", "-fx-text-fill: #fde68a"},
            {"-fx-text-fill: #991b1b", "-fx-text-fill: #fca5a5"},
            // ===== ROLE BADGE BACKGROUNDS (pastel → translucent glow) =====
            {"-fx-background-color: #fce7f3", "-fx-background-color: rgba(239, 68, 68, 0.15)"},
            {"-fx-background-color: #fef3c7", "-fx-background-color: rgba(245, 158, 11, 0.15)"},
            {"-fx-background-color: #ffedd5", "-fx-background-color: rgba(245, 158, 11, 0.15)"},
            {"-fx-background-color: #e0e7ff", "-fx-background-color: rgba(99, 102, 241, 0.15)"},
            {"-fx-background-color: #dbeafe", "-fx-background-color: rgba(99, 102, 241, 0.15)"},
            {"-fx-background-color: #dcfce7", "-fx-background-color: rgba(34, 197, 94, 0.15)"},
            {"-fx-background-color: #f3e8ff", "-fx-background-color: rgba(139, 92, 246, 0.15)"},
            // ===== ROLE BADGE TEXT (dark → light with accent) =====
            {"-fx-text-fill: #9d174d", "-fx-text-fill: #fca5a5"},
            {"-fx-text-fill: #92400e", "-fx-text-fill: #fde68a"},
            {"-fx-text-fill: #c2410c", "-fx-text-fill: #fde68a"},
            {"-fx-text-fill: #3730a3", "-fx-text-fill: #a5b4fc"},
            {"-fx-text-fill: #1e40af", "-fx-text-fill: #a5b4fc"},
            {"-fx-text-fill: #166534", "-fx-text-fill: #86efac"},
            {"-fx-text-fill: #6b21a8", "-fx-text-fill: #d8b4fe"},
            // ===== ACCENT COLORS - Primary (indigo/violet) =====
            {"-fx-text-fill: #4C6FFF", "-fx-text-fill: #818cf8"},
            {"-fx-background-color: #4C6FFF", "-fx-background-color: #6366f1"},
    };

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

        Parent root = scene.getRoot();
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
     * Remplace les couleurs inline dans les attributs style= du FXML.
     */
    private void applyDarkModeToNode(Node node, boolean dark) {
        if (node == null) return;

        // 1. Gérer la classe CSS dark-mode
        if (dark) {
            if (!node.getStyleClass().contains("dark-mode")) {
                node.getStyleClass().add("dark-mode");
            }
        } else {
            node.getStyleClass().remove("dark-mode");
        }

        // 2. Gérer les styles inline
        String currentStyle = node.getStyle();
        if (currentStyle != null && !currentStyle.isEmpty()) {
            if (dark) {
                // Sauvegarder le style original avant modification
                if (!originalStyles.containsKey(node)) {
                    originalStyles.put(node, currentStyle);
                }
                // Remplacer les couleurs claires par des sombres
                String darkStyle = convertToDarkStyle(currentStyle);
                node.setStyle(darkStyle);
            } else {
                // Restaurer le style original
                String original = originalStyles.get(node);
                if (original != null) {
                    node.setStyle(original);
                    originalStyles.remove(node);
                }
            }
        }

        // 3. Traiter récursivement les enfants
        if (node instanceof Parent) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                applyDarkModeToNode(child, dark);
            }
        }
        if (node instanceof Pane) {
            for (Node child : ((Pane) node).getChildren()) {
                applyDarkModeToNode(child, dark);
            }
        }
    }

    /**
     * Convertit un style inline clair en style sombre.
     */
    private String convertToDarkStyle(String style) {
        String result = style;
        for (String[] mapping : LIGHT_TO_DARK) {
            result = result.replace(mapping[0], mapping[1]);
        }

        // Remplacer les ombres claires par des ombres sombres
        result = result.replace("rgba(0,0,0,0.04)", "rgba(0,0,0,0.2)");
        result = result.replace("rgba(0,0,0,0.06)", "rgba(0,0,0,0.3)");
        result = result.replace("rgba(0,0,0,0.08)", "rgba(0,0,0,0.4)");
        result = result.replace("rgba(0,0,0,0.10)", "rgba(0,0,0,0.5)");
        result = result.replace("rgba(76,111,255,0.3)", "rgba(76,111,255,0.4)");
        result = result.replace("rgba(76,111,255,0.35)", "rgba(76,111,255,0.5)");
        result = result.replace("rgba(76,111,255,0.08)", "rgba(76,111,255,0.2)");
        result = result.replace("rgba(124,58,237,0.3)", "rgba(124,58,237,0.4)");
        result = result.replace("rgba(124,58,237,0.08)", "rgba(124,58,237,0.2)");
        result = result.replace("rgba(6,182,212,0.08)", "rgba(6,182,212,0.2)");
        result = result.replace("rgba(139,92,246,0.08)", "rgba(139,92,246,0.2)");

        return result;
    }

    /**
     * Applique le thème à un nœud spécifique (utile pour les nœuds créés dynamiquement).
     * À appeler depuis les contrôleurs quand ils ajoutent des rows/cards après coup.
     */
    public void applyToNode(Node node) {
        if (node == null || !darkMode) return;
        applyDarkModeToNode(node, true);
    }

    /**
     * Ré-applique le thème à toute la scène (pour les nœuds ajoutés dynamiquement).
     */
    public void reapplyCurrentScene(Scene scene) {
        if (scene == null) return;
        cleanup();
        applyToScene(scene);
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
