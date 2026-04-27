package org.example.service;

import java.util.concurrent.CompletableFuture;

/**
 * Stub - reCAPTCHA désactivé pour application desktop.
 */
public class RecaptchaWebView {

    public CompletableFuture<String> execute(String action) {
        CompletableFuture<String> future = new CompletableFuture<>();
        future.complete("DISABLED");
        return future;
    }
}
