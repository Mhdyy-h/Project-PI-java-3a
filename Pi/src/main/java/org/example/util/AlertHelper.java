package org.example.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Helper centralisé pour afficher les dialogues JavaFX.
 */
public final class AlertHelper {

    private AlertHelper() {}

    public static void showSuccess(String titre, String message) {
        show(Alert.AlertType.INFORMATION, titre, message);
    }

    public static void showError(String titre, String message) {
        show(Alert.AlertType.ERROR, titre, message);
    }

    public static void showWarning(String titre, String message) {
        show(Alert.AlertType.WARNING, titre, message);
    }

    /**
     * @return true si l'utilisateur a cliqué OK.
     */
    public static boolean showConfirmation(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static void show(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
