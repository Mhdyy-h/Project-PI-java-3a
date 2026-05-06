package org.example.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.example.service.NavigationService;
import org.example.service.OllamaChatService;

public class AiChatController {

    @FXML private TextArea chatArea;
    @FXML private TextField inputField;
    @FXML private Button sendButton;
    @FXML private Button resetButton;
    @FXML private Button backButton;

    private final OllamaChatService chatService = new OllamaChatService();



    @FXML
    public void initialize() {
        if (chatArea != null) {
            chatArea.setEditable(false);
        }
    }

    @FXML
    private void handleSend() {
        String msg = inputField.getText() != null ? inputField.getText().trim() : "";
        if (msg.isEmpty()) return;

        append("Vous", msg);
        inputField.clear();
        sendButton.setDisable(true);

        Thread t = new Thread(() -> {
            String reply = chatService.chat(msg);
            Platform.runLater(() -> {
                append("Assistant", reply);
                sendButton.setDisable(false);
            });
        });
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void handleReset() {
        chatService.reset();
        if (chatArea != null) chatArea.clear();
    }

    @FXML
    private void handleBack() {
        NavigationService.getInstance().navigateToDashboard(backButton, currentUser);
    }

    private void append(String who, String text) {
        if (chatArea == null) return;
        chatArea.appendText(who + " : " + text + "\n\n");
    }
}
