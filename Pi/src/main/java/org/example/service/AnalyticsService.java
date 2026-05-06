package org.example.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.util.UserSession;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AnalyticsService {

    private static final Gson gson = new GsonBuilder().create();
    private static final Path filePath = Path.of("logs", "analytics.jsonl");

    private AnalyticsService() {}

    public static void track(String eventName) {
        track(eventName, Map.of());
    }

    public static void track(String eventName, Map<String, Object> props) {
        if (eventName == null || eventName.isBlank()) return;

        User user = UserSession.getInstance().getCurrentUser();

        Map<String, Object> evt = new LinkedHashMap<>();
        evt.put("ts", Instant.now().toString());
        evt.put("event", eventName);
        evt.put("userId", user != null ? user.getId() : null);
        evt.put("props", props != null ? props : Map.of());

        try {
            Files.createDirectories(filePath.getParent());
            String line = gson.toJson(evt) + "\n";
            Files.writeString(filePath, line, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
        } catch (Exception ignored) {
        }
    }
}
