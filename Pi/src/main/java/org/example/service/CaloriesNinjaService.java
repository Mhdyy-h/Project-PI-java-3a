package org.example.service;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CaloriesNinjaService {

    private static final String API_KEY = "kddcA5uGLWg5YPxv1dqAtDRG0xbHGjC8je89wNde";
     private static final String BASE_URL = "https://api.api-ninjas.com/v1/caloriesburned";

    public double getCalories(String nomExercice, int dureeMinutes) {
        String nomAnglais = traduire(nomExercice.toLowerCase().trim());

        try {
            String url = BASE_URL + "?activity=" +
                    nomAnglais.replace(" ", "%20") +
                    "&duration=" + dureeMinutes;

            System.out.println("🌐 URL: " + url);

            java.net.URL urlObj = new java.net.URL(url);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) urlObj.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-Api-Key", API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int status = conn.getResponseCode();
            System.out.println("📡 Status: " + status);

            if (status == 200) {
                java.io.BufferedReader br = new java.io.BufferedReader(
                        new java.io.InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                String body = sb.toString();
                System.out.println("✅ Response: " + body);

                JSONArray items = new JSONArray(body);
                if (!items.isEmpty()) {
                    return items.getJSONObject(0).getDouble("total_calories");
                } else {
                    System.out.println("⚠️ Liste vide pour: " + nomAnglais);
                }
            } else {
                java.io.BufferedReader br = new java.io.BufferedReader(
                        new java.io.InputStreamReader(conn.getErrorStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();
                System.out.println("❌ Erreur: " + sb.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }
    private String traduire(String nom) {
        java.util.Map<String, String> dico = new java.util.HashMap<>();
        dico.put("squat",     "weight lifting");
        dico.put("burpee",    "calisthenics");
        dico.put("burpeeps",  "calisthenics");
        dico.put("traction",  "calisthenics");
        dico.put("tractions", "calisthenics");
        dico.put("pompe",  "pushups");
        dico.put("pompes", "pushups");
        dico.put("burpees",       "burpees");
        dico.put("course",        "running");
        dico.put("course à pied", "running");
        dico.put("running",       "running");
        dico.put("vélo",          "cycling");
        dico.put("velo",          "cycling");
        dico.put("natation",      "swimming");
        dico.put("nage",          "swimming");
        dico.put("marche",        "walking");
        dico.put("yoga",          "yoga");
        dico.put("abdominaux",    "crunches");
        dico.put("abdo",          "crunches");
        dico.put("abdos",         "crunches");
        dico.put("cardio",        "aerobics");
        dico.put("corde",         "jump rope");
        dico.put("saut",          "jumping");
        dico.put("sauts",         "jumping");
        dico.put("sprint",        "running");
        dico.put("boxe",          "boxing");
        dico.put("rameur",        "rowing");
        dico.put("gainage",       "plank");
        dico.put("planche",       "plank");
        dico.put("fente",         "lunges");
        dico.put("fentes",        "lunges");
        dico.put("traction",      "pull-ups");
        dico.put("tractions",     "pull-ups");
        dico.put("dips",          "dips");
        dico.put("samedi",        "aerobics");
        dico.put("vendredi",      "aerobics");
        dico.put("lundi",         "aerobics");

        if (dico.containsKey(nom)) return dico.get(nom);

        for (java.util.Map.Entry<String, String> entry : dico.entrySet()) {
            if (nom.contains(entry.getKey())) return entry.getValue();
        }

        return nom;
    }
}
