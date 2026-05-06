package org.example.service;

import org.example.model.User;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.example.model.Question;
import org.example.util.ConfigLoader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OllamaQcmGeneratorService {

    private final OllamaClient client;
    private final Gson gson;

    public OllamaQcmGeneratorService() {
        this.client = new OllamaClient();
        this.gson = new GsonBuilder().create();
    }

    public List<Question> generate(String theme, int difficulty, int count, int defaultPoints) throws Exception {
        String model = ConfigLoader.get("ollama.model", "llama3");

        String prompt = "Génère " + count + " questions QCM sur le thème : '" + theme + "'. "
                + "Niveau de difficulté (1-10) = " + difficulty + ". "
                + "Retourne UNIQUEMENT du JSON valide (pas de markdown, pas de texte). "
                + "Format attendu : {\"questions\":[{" 
                + "\"enonce\":\"...\",\"A\":\"...\",\"B\":\"...\",\"C\":\"...\",\"D\":\"...\",\"bonne\":\"A|B|C|D\",\"explication\":\"...\"}]}";

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", "Tu es un générateur de QCM. Réponds en JSON strict."));
        messages.add(new LinkedHashMap<>(Map.of("role", "user", "content", prompt)));

        String raw = client.chat(model, messages);
        if (raw == null || raw.isBlank()) {
            return List.of();
        }

        String cleaned = raw.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceAll("```(json)?", "").trim();
        }

        JsonObject root = gson.fromJson(cleaned, JsonObject.class);
        if (root == null || !root.has("questions")) return List.of();

        JsonArray arr = root.getAsJsonArray("questions");
        List<Question> result = new ArrayList<>();

        for (JsonElement el : arr) {
            if (!el.isJsonObject()) continue;
            JsonObject q = el.getAsJsonObject();

            Question question = new Question();
            question.setContenu(getString(q, "enonce"));
            question.setOptionA(getString(q, "A"));
            question.setOptionB(getString(q, "B"));
            question.setOptionC(getString(q, "C"));
            question.setOptionD(getString(q, "D"));
            question.setBonneReponse(getString(q, "bonne"));
            question.setPoints(defaultPoints);
            question.setExplication(getString(q, "explication"));

            result.add(question);
        }

        return result;
    }

    private String getString(JsonObject obj, String key) {
        return obj != null && obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }
}

