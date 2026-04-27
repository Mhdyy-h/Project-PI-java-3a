package org.example.service;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.CompletableFuture;

/**
 * Helper JavaFX pour exécuter reCAPTCHA v3 dans un WebView invisible.
 * Charge une mini page HTML avec le script Google reCAPTCHA,
 * exécute grecaptcha.execute() et retourne le token.
 */
public class RecaptchaWebView {

    private final RecaptchaService recaptchaService = RecaptchaService.getInstance();

    /**
     * Exécute reCAPTCHA v3 et retourne un CompletableFuture contenant le token.
     * Doit être appelé depuis le thread JavaFX.
     * @param action L'action reCAPTCHA (ex: "login", "register")
     */
    public CompletableFuture<String> execute(String action) {
        CompletableFuture<String> future = new CompletableFuture<>();

        if (!recaptchaService.isConfigured()) {
            System.out.println("[reCAPTCHA WebView] Non configuré, skip.");
            future.complete("NOT_CONFIGURED");
            return future;
        }

        String siteKey = recaptchaService.getSiteKey();

        Runnable task = () -> {
            try {
                WebView webView = new WebView();
                webView.setPrefSize(1, 1);
                webView.setVisible(false);
                WebEngine engine = webView.getEngine();

                // Activer JavaScript
                engine.setJavaScriptEnabled(true);

                // HTML simplifié sans script externe - on l'injecte après
                String html = "<!DOCTYPE html><html><head>"
                    + "<meta charset='UTF-8'>"
                    + "</head><body>"
                    + "<div id='status'>Loading...</div>"
                    + "</body></html>";

                System.out.println("[reCAPTCHA WebView] Chargement page de base...");

                engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                    System.out.println("[reCAPTCHA WebView] LoadWorker state: " + newState);
                    if (newState == Worker.State.SUCCEEDED) {
                        System.out.println("[reCAPTCHA WebView] Page chargée, injection du script...");
                        injectRecaptchaScript(engine, siteKey, action, future);
                    } else if (newState == Worker.State.FAILED) {
                        System.err.println("[reCAPTCHA WebView] Échec du chargement!");
                        future.complete("ERROR:LOAD_FAILED");
                    }
                });

                engine.loadContent(html, "text/html");

                // Timeout after 10 seconds
                new Thread(() -> {
                    try {
                        Thread.sleep(10000);
                        if (!future.isDone()) {
                            System.err.println("[reCAPTCHA WebView] Timeout!");
                            future.complete("ERROR:TIMEOUT");
                        }
                    } catch (InterruptedException ignored) {}
                }).start();

            } catch (Exception e) {
                future.complete("ERROR:" + e.getMessage());
            }
        };

        if (Platform.isFxApplicationThread()) {
            task.run();
        } else {
            Platform.runLater(task);
        }

        return future;
    }

    /**
     * Injecte le script reCAPTCHA après que la page de base soit chargée.
     */
    private void injectRecaptchaScript(WebEngine engine, String siteKey, String action, CompletableFuture<String> future) {
        try {
            // Injecter le script Google
            String injectScript = """
                var script = document.createElement('script');
                script.src = 'https://www.google.com/recaptcha/api.js?render=%s';
                script.onload = function() {
                    document.getElementById('status').innerText = 'Script loaded';
                    waitForGrecaptcha();
                };
                script.onerror = function() {
                    window.recaptchaToken = 'ERROR:SCRIPT_LOAD_FAILED';
                };
                document.head.appendChild(script);

                function waitForGrecaptcha() {
                    if (typeof grecaptcha !== 'undefined' && grecaptcha.execute) {
                        grecaptcha.execute('%s', {action: '%s'}).then(function(token) {
                            window.recaptchaToken = token;
                            document.getElementById('status').innerText = 'Token received';
                        }).catch(function(err) {
                            window.recaptchaToken = 'ERROR:' + err;
                        });
                    } else {
                        setTimeout(waitForGrecaptcha, 100);
                    }
                }
                """.formatted(siteKey, siteKey, action);

            engine.executeScript(injectScript);
            System.out.println("[reCAPTCHA WebView] Script injecté, polling pour token...");

            // Poll for token
            pollForToken(engine, future, 0);

        } catch (Exception e) {
            System.err.println("[reCAPTCHA WebView] Erreur injection: " + e.getMessage());
            future.complete("ERROR:" + e.getMessage());
        }
    }

    /**
     * Polling du token dans le WebView (max 50 tentatives, 200ms chacune).
     */
    private void pollForToken(WebEngine engine, CompletableFuture<String> future, int attempt) {
        if (attempt > 50 || future.isDone()) {
            if (!future.isDone()) future.complete("ERROR:POLL_TIMEOUT");
            return;
        }

        Platform.runLater(() -> {
            try {
                Object result = engine.executeScript("window.recaptchaToken || ''");
                String token = result != null ? result.toString() : "";
                if (!token.isEmpty() && !token.equals("undefined")) {
                    System.out.println("[reCAPTCHA WebView] Token obtenu!");
                    future.complete(token);
                } else {
                    // Retry after 200ms
                    new Thread(() -> {
                        try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                        Platform.runLater(() -> pollForToken(engine, future, attempt + 1));
                    }).start();
                }
            } catch (Exception e) {
                future.complete("ERROR:" + e.getMessage());
            }
        });
    }
}
