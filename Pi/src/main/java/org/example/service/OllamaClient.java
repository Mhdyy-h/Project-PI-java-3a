package org.example.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.example.util.ConfigLoader;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OllamaClient {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final Gson gson;

    public OllamaClient() {
        int timeoutSeconds = ConfigLoader.getInt("ollama.timeout.seconds", 60);
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .build();
        this.gson = new GsonBuilder().create();
    }

    public String chat(String model, List<Map<String, String>> messages) throws IOException {
        String baseUrl = ConfigLoader.get("ollama.baseUrl", "http://localhost:11434");

        JsonObject body = new JsonObject();
        body.addProperty("model", model);
        body.addProperty("stream", false);

        body.add("messages", gson.toJsonTree(messages));

        Request request = new Request.Builder()
                .url(baseUrl + "/api/chat")
                .post(RequestBody.create(gson.toJson(body), JSON))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Ollama HTTP " + response.code() + " - " + response.message());
            }
            String raw = response.body() != null ? response.body().string() : "";
            JsonObject json = gson.fromJson(raw, JsonObject.class);
            if (json == null || !json.has("message")) return null;
            JsonObject msg = json.getAsJsonObject("message");
            return msg != null && msg.has("content") ? msg.get("content").getAsString() : null;
        }
    }
}
