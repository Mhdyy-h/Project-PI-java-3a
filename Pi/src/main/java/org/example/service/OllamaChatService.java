package org.example.service;

import org.example.util.ConfigLoader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OllamaChatService {

    private final OllamaClient client;
    private final List<Map<String, String>> history = new ArrayList<>();

    public OllamaChatService() {
        this.client = new OllamaClient();
        reset();
    }

    public void reset() {
        history.clear();
        history.add(Map.of(
                "role", "system",
                "content", ConfigLoader.get("ollama.chat.system",
                        "Tu es l'assistant BioSync. Tu réponds en français. Réponses courtes et pratiques. Pas de markdown.")
        ));
    }

    public String chat(String userMessage) {
        String model = ConfigLoader.get("ollama.model", "llama3");

        history.add(new LinkedHashMap<>(Map.of("role", "user", "content", userMessage)));
        try {
            String reply = client.chat(model, history);
            if (reply == null || reply.isBlank()) {
                reply = "Je n'ai pas pu générer une réponse. Vérifiez qu'Ollama est lancé.";
            }
            history.add(new LinkedHashMap<>(Map.of("role", "assistant", "content", reply)));
            if (history.size() > 30) {
                history.subList(1, Math.min(history.size(), 10)).clear();
            }
            return reply;
        } catch (Exception e) {
            return "Erreur Ollama : " + e.getMessage();
        }
    }
}
