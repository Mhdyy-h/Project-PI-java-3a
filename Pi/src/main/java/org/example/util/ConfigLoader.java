package org.example.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Chargement centralisé de la configuration depuis {@code config.properties}.
 * Supporte une surcharge locale via {@code config.local.properties} (ignoré par git).
 *
 * <p>Si une clé est absente ou vide, une valeur par défaut est retournée
 * sans lever d'exception — ce qui permet un fonctionnement dégradé
 * (ex : Claude API désactivée si la clé est manquante).</p>
 *
 * <p>Exemple d'utilisation :</p>
 * <pre>
 *   String apiKey = ConfigLoader.get("claude.api.key");
 *   if (apiKey.isEmpty()) {
 *       // fallback sur la banque de questions MySQL
 *   }
 *
 *   int port = ConfigLoader.getInt("websocket.port", 8887);
 * </pre>
 */
public final class ConfigLoader {

    private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);
    private static final Properties props = new Properties();

    static {
        charger("config.properties");
        charger("config.local.properties"); // surcharge locale silencieuse si absent
    }

    private ConfigLoader() {}

    private static void charger(String nomFichier) {
        try (InputStream in = ConfigLoader.class.getClassLoader().getResourceAsStream(nomFichier)) {
            if (in != null) {
                props.load(in);
                log.debug("Configuration chargée depuis {}", nomFichier);
            }
        } catch (IOException e) {
            log.warn("Impossible de charger {} : {}", nomFichier, e.getMessage());
        }
    }

    /**
     * Retourne la valeur d'une clé, ou une chaîne vide si absente.
     * Ne lève jamais d'exception.
     */
    public static String get(String cle) {
        return props.getProperty(cle, "").trim();
    }

    /**
     * Retourne la valeur d'une clé, ou {@code defaut} si absente ou vide.
     */
    public static String get(String cle, String defaut) {
        String val = props.getProperty(cle, "").trim();
        return val.isEmpty() ? defaut : val;
    }

    /**
     * Retourne une valeur entière. Retourne {@code defaut} si la clé est absente
     * ou si la valeur n'est pas un entier valide.
     */
    public static int getInt(String cle, int defaut) {
        try {
            String val = props.getProperty(cle, "").trim();
            return val.isEmpty() ? defaut : Integer.parseInt(val);
        } catch (NumberFormatException e) {
            log.warn("Valeur non entière pour la clé '{}', valeur par défaut {} utilisée", cle, defaut);
            return defaut;
        }
    }

    /**
     * Retourne une valeur double. Retourne {@code defaut} si la clé est absente.
     */
    public static double getDouble(String cle, double defaut) {
        try {
            String val = props.getProperty(cle, "").trim();
            return val.isEmpty() ? defaut : Double.parseDouble(val);
        } catch (NumberFormatException e) {
            log.warn("Valeur non numérique pour la clé '{}', valeur par défaut {} utilisée", cle, defaut);
            return defaut;
        }
    }

    /**
     * Retourne un booléen. Interprète "true" (insensible à la casse) comme {@code true}.
     */
    public static boolean getBoolean(String cle, boolean defaut) {
        String val = props.getProperty(cle, "").trim();
        if (val.isEmpty()) return defaut;
        return "true".equalsIgnoreCase(val);
    }

    // ── Raccourcis pour les clés fréquemment utilisées ─────────────

    /** Retourne la clé Claude API. Vide si non configurée → mode fallback activé. */
    public static String claudeApiKey() {
        return get("claude.api.key");
    }

    /** Indique si Claude API est configurée et utilisable. */
    public static boolean claudeDisponible() {
        return !claudeApiKey().isEmpty();
    }

    public static String claudeApiUrl() {
        return get("claude.api.url", "https://api.anthropic.com/v1/messages");
    }

    public static String claudeModel() {
        return get("claude.model", "claude-haiku-4-5-20251001");
    }

    public static int claudeTimeoutSecondes() {
        return getInt("claude.timeout.seconds", 30);
    }

    public static int claudeCacheTtlHeures() {
        return getInt("claude.cache.ttl.hours", 24);
    }

    public static String dbUrl() {
        return get("db.url", "jdbc:mysql://localhost:3306/biosync");
    }

    public static String dbUser() {
        return get("db.user", "root");
    }

    public static String dbPassword() {
        return get("db.password", "");
    }

    public static int websocketPort() {
        return getInt("websocket.port", 8887);
    }

    public static int fatigueSlowThresholdMs() {
        return getInt("fatigue.response.slow.threshold.ms", 8000);
    }

    public static int fatigueSlidingWindowSize() {
        return getInt("fatigue.sliding.window.size", 5);
    }

    public static double fatiguePauseThreshold() {
        return getDouble("fatigue.pause.threshold", 65.0);
    }

    public static double fatigueStopThreshold() {
        return getDouble("fatigue.stop.threshold", 85.0);
    }

    public static int anomalyMinResponseMs() {
        return getInt("anomaly.min.response.ms", 800);
    }

    public static double anomalyZscoreThreshold() {
        return getDouble("anomaly.zscore.threshold", 2.5);
    }

    public static int eloInitialScore() {
        return getInt("elo.initial.score", 1000);
    }

    public static int eloKFactor() {
        return getInt("elo.k.factor", 32);
    }

    public static String reportOutputDir() {
        return get("report.output.dir", "reports");
    }
}
