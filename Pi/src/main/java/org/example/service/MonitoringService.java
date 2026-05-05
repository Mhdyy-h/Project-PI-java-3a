package org.example.service;

import io.sentry.Sentry;
import io.sentry.SentryOptions;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.example.util.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public final class MonitoringService {

    private static final Logger log = LoggerFactory.getLogger(MonitoringService.class);
    private static volatile boolean sentryEnabled = false;

    private MonitoringService() {}

    public static void init() {
        initSentryIfConfigured();
        installGlobalExceptionHandler();
        AnalyticsService.track("app_started");
    }

    private static void initSentryIfConfigured() {
        String dsn = ConfigLoader.get("sentry.dsn", "");
        if (dsn.isBlank()) {
            log.info("Sentry désactivé (sentry.dsn non configuré)");
            return;
        }

        try {
            Sentry.init(options -> {
                options.setDsn(dsn);
                options.setEnvironment(ConfigLoader.get("sentry.environment", "dev"));
                options.setRelease(ConfigLoader.get("app.release", "biosync-desktop"));
                options.setTracesSampleRate(0.0);
            });
            sentryEnabled = true;
            log.info("Sentry activé");
        } catch (Exception e) {
            log.warn("Impossible d'initialiser Sentry: {}", e.getMessage());
        }
    }

    private static void installGlobalExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            String errorId = UUID.randomUUID().toString();
            log.error("Erreur non gérée [{}] dans thread {}", errorId, thread.getName(), throwable);

            AnalyticsService.track("uncaught_exception", java.util.Map.of(
                    "errorId", errorId,
                    "thread", thread.getName(),
                    "type", throwable.getClass().getName()
            ));

            if (sentryEnabled) {
                try {
                    Sentry.setTag("errorId", errorId);
                    Sentry.captureException(throwable);
                } catch (Exception ignored) {
                }
            }

            try {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText("Une erreur est survenue");
                    alert.setContentText("ID: " + errorId + "\nConsultez logs/biosync.log");
                    alert.showAndWait();
                });
            } catch (Exception ignored) {
            }
        });
    }
}
