package org.example.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

/**
 * Appels vers 3 APIs gratuites sans clé pour enrichir l'écran de sélection.
 *
 *  API 1 – ZenQuotes   : https://zenquotes.io/api/random
 *  API 2 – AdviceSlip  : https://api.adviceslip.com/advice
 *  API 3 – Quotable    : https://api.quotable.io/quotes/random?tags=inspirational
 */
public class WellnessApiService {

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(6, TimeUnit.SECONDS)
            .readTimeout(6, TimeUnit.SECONDS)
            .build();

    // ── API 1 : ZenQuotes ────────────────────────────────────────
    // GET https://zenquotes.io/api/random
    // Réponse JSON : [{"q":"texte citation","a":"auteur"}]
    public static ApiResult fetchZenQuote() {
        try {
            Request req = new Request.Builder()
                    .url("https://zenquotes.io/api/random")
                    .addHeader("Accept", "application/json")
                    .build();
            try (Response resp = CLIENT.newCall(req).execute()) {
                if (resp.isSuccessful() && resp.body() != null) {
                    JSONArray arr = new JSONArray(resp.body().string());
                    JSONObject obj = arr.getJSONObject(0);
                    String quote  = obj.getString("q");
                    String author = obj.getString("a");
                    return new ApiResult(
                            "\"" + truncate(quote, 110) + "\"  — " + author,
                            "ZenQuotes", "🌐");
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    // ── API 2 : Advice Slip ──────────────────────────────────────
    // GET https://api.adviceslip.com/advice
    // Réponse JSON : {"slip":{"id":1,"advice":"conseil"}}
    public static ApiResult fetchAdvice() {
        try {
            Request req = new Request.Builder()
                    .url("https://api.adviceslip.com/advice")
                    .addHeader("Accept", "application/json")
                    .build();
            try (Response resp = CLIENT.newCall(req).execute()) {
                if (resp.isSuccessful() && resp.body() != null) {
                    JSONObject obj = new JSONObject(resp.body().string());
                    String advice = obj.getJSONObject("slip").getString("advice");
                    return new ApiResult(advice, "AdviceSlip", "💡");
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    // ── API 3 : Quotable ─────────────────────────────────────────
    // GET https://api.quotable.io/quotes/random?tags=inspirational&limit=1
    // Réponse JSON : [{"content":"texte","author":"auteur"}]
    public static ApiResult fetchInspirationalQuote() {
        try {
            Request req = new Request.Builder()
                    .url("https://api.quotable.io/quotes/random?tags=inspirational&limit=1")
                    .addHeader("Accept", "application/json")
                    .build();
            try (Response resp = CLIENT.newCall(req).execute()) {
                if (resp.isSuccessful() && resp.body() != null) {
                    JSONArray arr = new JSONArray(resp.body().string());
                    if (!arr.isEmpty()) {
                        JSONObject obj = arr.getJSONObject(0);
                        String content = obj.getString("content");
                        String author  = obj.getString("author");
                        return new ApiResult(
                                "\"" + truncate(content, 110) + "\"  — " + author,
                                "Quotable", "✨");
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * Tente les 3 APIs dans l'ordre.
     * Si toutes échouent, retourne le conseil local.
     */
    public static ApiResult fetchWithFallback(String localFallback) {
        ApiResult r;

        r = fetchZenQuote();
        if (r != null) return r;

        r = fetchAdvice();
        if (r != null) return r;

        r = fetchInspirationalQuote();
        if (r != null) return r;

        return new ApiResult(localFallback, "Local", "💡");
    }

    private static String truncate(String s, int max) {
        return s != null && s.length() > max ? s.substring(0, max) + "…" : s;
    }

    // ── DTO résultat ─────────────────────────────────────────────
    public static class ApiResult {
        public final String text;
        public final String source;   // "ZenQuotes" / "AdviceSlip" / "Quotable" / "Local"
        public final String emoji;

        public ApiResult(String text, String source, String emoji) {
            this.text   = text;
            this.source = source;
            this.emoji  = emoji;
        }
    }
}
