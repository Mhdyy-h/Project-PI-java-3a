package org.example.service;

import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Service to fetch real-time weather using Java 11+ HttpClient.
 */
public class WeatherService {

    private static final String API_KEY = "7e4ece4cf53781522cc9dfbad7955723";
    private static final String CITY = "Tunis";
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather?q=%s&units=metric&appid=%s";

    /**
     * Fetches weather data for the default city.
     * @return JSONObject containing weather data or null if the request fails.
     */
    public static JSONObject getWeather() {
        try {
            // Build the URL string using the template
            String fullUrl = String.format(API_URL, CITY, API_KEY);

            // Create a modern HttpClient with a 10-second timeout
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            // Prepare the GET request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .GET()
                    .build();

            // Send the request and get response as String
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle response based on status code
            if (response.statusCode() == 200) {
                System.out.println("✅ Weather API: Success [200]");
                return new JSONObject(response.body());
            } else {
                System.err.println("❌ Weather API Error: Status " + response.statusCode());
                System.err.println("Payload: " + response.body());
                return null;
            }

        } catch (Exception e) {
            System.err.println("⚠️ Weather Service Exception: " + e.getMessage());
            return null;
        }
    }
}