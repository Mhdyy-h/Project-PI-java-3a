package org.example.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * VoiceService — Text-to-Speech using Windows SAPI via PowerShell.
 *
 * No extra Maven dependencies required.
 * All speech runs on a dedicated daemon thread to keep the JavaFX thread free.
 */
public final class VoiceService {

    private static final Logger LOGGER = Logger.getLogger(VoiceService.class.getName());

    private static final ExecutorService TTS_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "voice-tts");
        t.setDaemon(true);
        return t;
    });

    private VoiceService() {}

    /**
     * Speak {@code text} synchronously on the calling thread.
     * Prefer {@link #speakAsync} to avoid blocking.
     */
    public static void speak(String text) {
        if (text == null || text.isBlank()) return;
        // Escape single quotes so the PowerShell string literal is not broken
        String safe = text.replace("'", " ").replace("\"", " ").replace(";", ",");
        try {
            String[] cmd = {
                "powershell", "-NonInteractive", "-Command",
                "Add-Type -AssemblyName System.Speech; " +
                "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                "$s.Rate = 1; " +
                "$s.Speak('" + safe + "')"
            };
            Process p = new ProcessBuilder(cmd)
                    .redirectErrorStream(true)
                    .start();
            p.waitFor();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "TTS unavailable: " + e.getMessage());
        }
    }

    /**
     * Speak {@code text} asynchronously — returns immediately.
     * Speech runs on the dedicated TTS thread; the JavaFX thread is never blocked.
     */
    public static void speakAsync(String text) {
        TTS_EXECUTOR.submit(() -> speak(text));
    }

    /** Call on application shutdown to release the TTS thread. */
    public static void shutdown() {
        TTS_EXECUTOR.shutdownNow();
    }
}
