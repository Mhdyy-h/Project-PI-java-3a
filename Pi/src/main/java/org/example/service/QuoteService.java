package org.example.service;

import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class QuoteService {
    // ZenQuotes API is free and reliable
    private static final String QUOTE_API_URL = "https://zenquotes.io/api/random";

    public static JSONObject getQuote() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(QUOTE_API_URL))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // ZenQuotes returns an array: [{ "q": "quote", "a": "author" }]
                JSONArray array = new JSONArray(response.body());
                return array.getJSONObject(0);
            }
        } catch (Exception e) {
            System.err.println("❌ QuoteService Error: " + e.getMessage());
        }
        return null;
    }
}