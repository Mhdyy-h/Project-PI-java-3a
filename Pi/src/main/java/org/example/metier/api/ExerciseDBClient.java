package org.example.metier.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

/**
 * CLIENT API — ExerciseDB (exercisedb.io)
 *
 * Enrichit chaque Exercice avec :
 *   - muscles ciblés / secondaires
 *   - équipement nécessaire
 *   - niveau de difficulté
 *   - GIF d'animation (URL)
 *
 * Pattern professionnel :
 *   Cache local (HashMap + TTL 1h) → Retry 3x → Fallback local
 *
 * Java pur : utilise java.net.http.HttpClient (Java 11+)
 * Parsing JSON : manuel sans librairie externe.
 */
public class ExerciseDBClient {

    private static final Logger LOG = Logger.getLogger(ExerciseDBClient.class.getName());

    // ─── CONFIG API ───────────────────────────────
    private static final String BASE_URL    = "https://exercisedb.p.rapidapi.com";
    private static final String API_KEY     = "VOTRE_CLE_API_ICI"; // RapidAPI gratuit
    private static final String API_HOST    = "exercisedb.p.rapidapi.com";
    private static final int    TIMEOUT_SEC = 5;
    private static final int    MAX_RETRY   = 3;

    // ─── CACHE LOCAL (TTL = 1 heure) ─────────────
    private final Map<String, CacheEntry> cache = new HashMap<>();
    private static final long CACHE_TTL_MINUTES = 60;

    // ─── HTTP CLIENT ──────────────────────────────
    private final HttpClient httpClient;

    public ExerciseDBClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SEC))
                .build();
    }

    // ─────────────────────────────────────────────
    // METHODE PRINCIPALE : enrichir un exercice
    // ─────────────────────────────────────────────

    /**
     * Enrichit un exercice avec les données de l'API.
     * Cache → API → Fallback local automatique.
     *
     * @param nomExercice nom de l'exercice (ex: "bench press")
     * @return ExerciceData avec toutes les infos enrichies
     */
    public ExerciceData enrichirExercice(String nomExercice) {
        String cacheKey = nomExercice.toLowerCase().trim().replace(" ", "_");

        // 1. Vérifier le cache
        ExerciceData cached = getFromCache(cacheKey);
        if (cached != null) {
            LOG.info("[CACHE HIT] Exercice: " + nomExercice);
            return cached;
        }

        // 2. Appel API avec retry
        ExerciceData data = appelAPIAvecRetry(nomExercice, cacheKey);

        // 3. Si API échoue → fallback local
        if (data == null) {
            LOG.warning("[FALLBACK] API indisponible pour: " + nomExercice);
            data = fallbackLocal(nomExercice);
        }

        return data;
    }

    /**
     * Récupère tous les exercices par groupe musculaire.
     *
     * @param muscle ex: "chest", "biceps", "quadriceps"
     * @return liste d'ExerciceData pour ce muscle
     */
    public List<ExerciceData> getExercicesParMuscle(String muscle) {
        String cacheKey = "muscle_" + muscle.toLowerCase();

        // Cache check
        String rawJson = getRawFromCache(cacheKey);
        if (rawJson == null) {
            rawJson = appelHttpBrut("/exercises/muscle/" + muscle);
            if (rawJson != null) putRawInCache(cacheKey, rawJson);
        }

        if (rawJson == null) return fallbackListeExercices(muscle);

        return parseListeExercices(rawJson);
    }

    // ─────────────────────────────────────────────
    // APPEL HTTP AVEC RETRY
    // ─────────────────────────────────────────────

    private ExerciceData appelAPIAvecRetry(String nomExercice, String cacheKey) {
        String endpoint = "/exercises/name/" + nomExercice.replace(" ", "%20");

        for (int tentative = 1; tentative <= MAX_RETRY; tentative++) {
            try {
                LOG.info("[API] Tentative " + tentative + "/" + MAX_RETRY + " pour: " + nomExercice);
                String json = appelHttpBrut(endpoint);

                if (json != null && !json.isEmpty()) {
                    ExerciceData data = parseExerciceData(json, nomExercice);
                    putInCache(cacheKey, data);
                    LOG.info("[API OK] Exercice enrichi: " + nomExercice);
                    return data;
                }

            } catch (Exception e) {
                LOG.warning("[API ERREUR] Tentative " + tentative + ": " + e.getMessage());
                if (tentative < MAX_RETRY) {
                    attendreAvantRetry(tentative);
                }
            }
        }
        return null;
    }

    private String appelHttpBrut(String endpoint) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .timeout(Duration.ofSeconds(TIMEOUT_SEC))
                    .header("X-RapidAPI-Key", API_KEY)
                    .header("X-RapidAPI-Host", API_HOST)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                LOG.warning("[HTTP] Status: " + response.statusCode());
                return null;
            }

        } catch (Exception e) {
            LOG.warning("[HTTP EXCEPTION] " + e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────
    // PARSING JSON MANUEL (Java pur, sans librairie)
    // ─────────────────────────────────────────────

    private ExerciceData parseExerciceData(String json, String nomDefaut) {
        // Parse JSON tableau ou objet selon la réponse API
        String jsonItem = json.trim().startsWith("[") ? extrairePremiereEntree(json) : json;

        return new ExerciceData(
                extraireChamp(jsonItem, "name",       nomDefaut),
                extraireChamp(jsonItem, "bodyPart",   "Non spécifié"),
                extraireChamp(jsonItem, "target",      "Non spécifié"),
                extraireChamp(jsonItem, "equipment",   "Aucun"),
                extraireChamp(jsonItem, "gifUrl",      ""),
                extraireListeChamp(jsonItem, "secondaryMuscles"),
                extraireListeChamp(jsonItem, "instructions")
        );
    }

    private List<ExerciceData> parseListeExercices(String json) {
        List<ExerciceData> liste = new ArrayList<>();
        String[] entrees = splitJsonArray(json);
        for (String entree : entrees) {
            if (!entree.isBlank()) {
                liste.add(parseExerciceData(entree, "Inconnu"));
            }
        }
        return liste;
    }

    /** Extrait la valeur d'un champ JSON simple : "clé": "valeur" */
    private String extraireChamp(String json, String cle, String defaut) {
        String recherche = "\"" + cle + "\"";
        int idx = json.indexOf(recherche);
        if (idx < 0) return defaut;

        int debut = json.indexOf("\"", idx + recherche.length() + 1);
        int fin   = json.indexOf("\"", debut + 1);
        if (debut < 0 || fin < 0) return defaut;

        return json.substring(debut + 1, fin);
    }

    /** Extrait un tableau JSON simple : "clé": ["val1", "val2"] */
    private List<String> extraireListeChamp(String json, String cle) {
        List<String> result = new ArrayList<>();
        String recherche = "\"" + cle + "\"";
        int idx = json.indexOf(recherche);
        if (idx < 0) return result;

        int debut = json.indexOf("[", idx);
        int fin   = json.indexOf("]", debut);
        if (debut < 0 || fin < 0) return result;

        String contenu = json.substring(debut + 1, fin);
        for (String item : contenu.split(",")) {
            String val = item.trim().replace("\"", "");
            if (!val.isEmpty()) result.add(val);
        }
        return result;
    }

    private String extrairePremiereEntree(String jsonArray) {
        int debut = jsonArray.indexOf("{");
        int fin   = jsonArray.lastIndexOf("}", jsonArray.indexOf("},") > 0
                ? jsonArray.indexOf("},") : jsonArray.length() - 1);
        if (debut < 0 || fin < 0) return jsonArray;
        return jsonArray.substring(debut, fin + 1);
    }

    private String[] splitJsonArray(String json) {
        // Découpage simple sur les objets JSON du tableau
        List<String> objets = new ArrayList<>();
        int depth = 0, start = -1;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') { if (depth == 0) start = i; depth++; }
            else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    objets.add(json.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return objets.toArray(new String[0]);
    }

    // ─────────────────────────────────────────────
    // CACHE LOCAL
    // ─────────────────────────────────────────────

    private ExerciceData getFromCache(String key) {
        CacheEntry entry = cache.get(key + "_data");
        if (entry == null) return null;
        if (entry.isExpire()) { cache.remove(key + "_data"); return null; }
        return (ExerciceData) entry.valeur;
    }

    private String getRawFromCache(String key) {
        CacheEntry entry = cache.get(key + "_raw");
        if (entry == null) return null;
        if (entry.isExpire()) { cache.remove(key + "_raw"); return null; }
        return (String) entry.valeur;
    }

    private void putInCache(String key, ExerciceData data) {
        cache.put(key + "_data", new CacheEntry(data));
    }

    private void putRawInCache(String key, String raw) {
        cache.put(key + "_raw", new CacheEntry(raw));
    }

    private static class CacheEntry {
        final Object valeur;
        final LocalDateTime expireAt;

        CacheEntry(Object valeur) {
            this.valeur = valeur;
            this.expireAt = LocalDateTime.now().plusMinutes(CACHE_TTL_MINUTES);
        }

        boolean isExpire() {
            return LocalDateTime.now().isAfter(expireAt);
        }
    }

    // ─────────────────────────────────────────────
    // FALLBACK LOCAL (si API indisponible)
    // ─────────────────────────────────────────────

    private ExerciceData fallbackLocal(String nom) {
        Map<String, ExerciceData> baseFallback = new HashMap<>();
        baseFallback.put("bench press",  new ExerciceData("Bench Press","chest","pectorals","barbell","",
                List.of("triceps","anterior deltoid"), List.of("Allongé sur banc", "Descendre la barre", "Pousser")));
        baseFallback.put("squat", new ExerciceData("Squat","upper legs","quads","barbell","",
                List.of("glutes","hamstrings"), List.of("Pieds écartés", "Descendre", "Remonter")));
        baseFallback.put("deadlift", new ExerciceData("Deadlift","back","spine","barbell","",
                List.of("hamstrings","glutes"), List.of("Dos droit", "Soulever", "Redresser")));

        String cle = nom.toLowerCase().trim();
        return baseFallback.getOrDefault(cle,
                new ExerciceData(nom, "Non spécifié", "Non spécifié", "Non spécifié", "",
                        List.of(), List.of("Consulter un coach pour cet exercice"))
        );
    }

    private List<ExerciceData> fallbackListeExercices(String muscle) {
        return List.of(fallbackLocal(muscle + " exercise"));
    }

    private void attendreAvantRetry(int tentative) {
        try { Thread.sleep((long) tentative * 500); }
        catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }

    // ─────────────────────────────────────────────
    // CLASSE INTERNE : RESULTAT EXERCICE ENRICHI
    // ─────────────────────────────────────────────

    public static class ExerciceData {
        private final String nom;
        private final String groupeMusculaire;
        private final String musclesCibles;
        private final String equipement;
        private final String gifUrl;
        private final List<String> musclesSecondaires;
        private final List<String> instructions;

        public ExerciceData(String nom, String groupeMusculaire, String musclesCibles,
                            String equipement, String gifUrl,
                            List<String> musclesSecondaires, List<String> instructions) {
            this.nom = nom;
            this.groupeMusculaire = groupeMusculaire;
            this.musclesCibles = musclesCibles;
            this.equipement = equipement;
            this.gifUrl = gifUrl;
            this.musclesSecondaires = musclesSecondaires;
            this.instructions = instructions;
        }

        public String getNom()                      { return nom; }
        public String getGroupeMusculaire()         { return groupeMusculaire; }
        public String getMusclesCibles()            { return musclesCibles; }
        public String getEquipement()               { return equipement; }
        public String getGifUrl()                   { return gifUrl; }
        public List<String> getMusclesSecondaires() { return musclesSecondaires; }
        public List<String> getInstructions()       { return instructions; }

        @Override
        public String toString() {
            return String.format("[API] %s | Muscle: %s | Équipement: %s | Secondaires: %s",
                    nom, musclesCibles, equipement, musclesSecondaires);
        }
    }
}
