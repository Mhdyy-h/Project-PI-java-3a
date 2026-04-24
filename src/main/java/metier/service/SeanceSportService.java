package metier.service;

import metier.api.ExerciseDBClient;
import metier.api.ExerciseDBClient.ExerciceData;
import metier.ia.ProgressionIAModel;
import metier.ia.ProgressionIAModel.ResultatPrediction;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * SERVICE METIER AVANCE — Combinaison IA + API
 *
 * Ce service orchestre :
 *   1. ProgressionIAModel  — modèle ML local (régression linéaire)
 *   2. ExerciseDBClient    — API externe enrichissement exercices
 *
 * Fonctionnalités avancées :
 *   - Prédiction PR (record personnel) par exercice
 *   - Analyse de surcharge musculaire intelligente
 *   - Recommandation exercices par IA + données API
 *   - Rapport de progression complet
 */
public class SeanceSportService {

    private static final Logger LOG = Logger.getLogger(SeanceSportService.class.getName());

    private final ExerciseDBClient exerciseDBClient;

    // Cache des modèles IA par exercice (userId_exerciceNom → modèle entraîné)
    private final Map<String, ProgressionIAModel> modelsCache = new HashMap<>();

    public SeanceSportService() {
        this.exerciseDBClient = new ExerciseDBClient();
    }

    // ─────────────────────────────────────────────
    // 1. PREDICTION DE PROGRESSION (IA LOCALE)
    // ─────────────────────────────────────────────

    /**
     * Prédit le record personnel futur d'un utilisateur sur un exercice.
     *
     * Ex : "Dans 4 séances, tu vas soulever 82.5 kg au bench press (+5.0 kg, +6.5%)"
     *
     * @param userId          identifiant utilisateur
     * @param nomExercice     ex: "bench press"
     * @param historique      liste des performances passées [60.0, 62.5, 65.0, 67.5]
     * @param seancesFutures  dans combien de séances prédire (ex: 4)
     * @return RapportProgression complet avec prédiction + interprétation + infos API
     */
    public RapportProgression predireProgression(String userId, String nomExercice,
                                                 List<Double> historique, int seancesFutures) {
        LOG.info("[SERVICE] Prédiction progression pour: " + userId + " / " + nomExercice);

        // ── Étape 1 : Entraîner (ou récupérer du cache) le modèle IA ──
        ProgressionIAModel model = getOuCreerModele(userId, nomExercice, historique);

        // ── Étape 2 : Prédiction ──
        ResultatPrediction prediction = model.predireFutur(historique.size(), seancesFutures);
        double r2 = model.calculerR2(historique);

        // ── Étape 3 : Enrichissement API ExerciseDB ──
        ExerciceData dataAPI = exerciseDBClient.enrichirExercice(nomExercice);

        // ── Étape 4 : Construire le rapport complet ──
        return new RapportProgression(
                userId, nomExercice,
                historique,
                prediction,
                r2,
                model.getResume(),
                dataAPI
        );
    }

    // ─────────────────────────────────────────────
    // 2. ANALYSE DE SURCHARGE MUSCULAIRE (IA METIER)
    // ─────────────────────────────────────────────

    /**
     * Détecte si un utilisateur est en surcharge musculaire.
     * Analyse la fréquence + intensité des séances récentes.
     *
     * @param seancesRecentes   liste des intensités des 7 dernières séances (0-10)
     * @param repoJours         jours de repos depuis la dernière séance
     * @return AlerteSurcharge avec niveau de risque et recommandation
     */
    public AlerteSurcharge analyserSurcharge(List<Double> seancesRecentes, int reposJours) {
        if (seancesRecentes == null || seancesRecentes.isEmpty()) {
            return new AlerteSurcharge(NiveauRisque.AUCUN, "Pas assez de données.", 0);
        }

        // Calcul score de charge accumulée (modèle ACWR simplifié)
        double chargeAigue   = moyenneDerniers(seancesRecentes, 3);  // 3 dernières séances
        double chargeChr     = moyenneDerniers(seancesRecentes, 7);  // 7 dernières séances
        double ratioACWR     = (chargeChr > 0) ? chargeAigue / chargeChr : 1.0;

        // Facteur repos : repos insuffisant aggrave le risque
        double facteurRepos  = (reposJours < 1) ? 1.5 : (reposJours < 2) ? 1.2 : 1.0;
        double scoreRisque   = ratioACWR * facteurRepos;

        NiveauRisque niveau;
        String conseil;

        if (scoreRisque > 1.5) {
            niveau  = NiveauRisque.CRITIQUE;
            conseil = "STOP. Risque de blessure elevé. Repos obligatoire 48-72h.";
        } else if (scoreRisque > 1.3) {
            niveau  = NiveauRisque.ELEVE;
            conseil = "Réduire l'intensité de 30%. Privilégier la récupération active.";
        } else if (scoreRisque > 1.1) {
            niveau  = NiveauRisque.MODERE;
            conseil = "Surveiller la fatigue. Étirements et sommeil renforcés conseillés.";
        } else {
            niveau  = NiveauRisque.FAIBLE;
            conseil = "Charge bien équilibrée. Vous pouvez maintenir ce rythme.";
        }

        return new AlerteSurcharge(niveau, conseil, scoreRisque);
    }

    // ─────────────────────────────────────────────
    // 3. RECOMMANDATION EXERCICES (IA + API)
    // ─────────────────────────────────────────────

    /**
     * Recommande les meilleurs exercices pour un muscle donné,
     * en combinant les règles métier IA + données ExerciseDB API.
     *
     * @param muscle        ex: "chest", "biceps", "back"
     * @param niveau        DEBUTANT, INTERMEDIAIRE, AVANCE
     * @param equipements   liste équipements disponibles (ex: ["barbell", "dumbbell"])
     * @return liste classée d'ExerciceRecommande avec score IA
     */
    public List<ExerciceRecommande> recommanderExercices(String muscle,
                                                         NiveauSportif niveau,
                                                         List<String> equipements) {
        LOG.info("[SERVICE] Recommandation pour muscle: " + muscle + " niveau: " + niveau);

        // ── API : récupérer les exercices disponibles ──
        List<ExerciceData> exercicesAPI = exerciseDBClient.getExercicesParMuscle(muscle);

        // ── IA : scorer chaque exercice selon le profil ──
        List<ExerciceRecommande> recommandations = new ArrayList<>();

        for (ExerciceData ex : exercicesAPI) {
            double score = calculerScoreIA(ex, niveau, equipements);
            if (score > 0.3) { // Seuil minimum de pertinence
                recommandations.add(new ExerciceRecommande(ex, score, genererRaison(ex, niveau)));
            }
        }

        // Tri par score décroissant, top 5
        return recommandations.stream()
                .sorted(Comparator.comparingDouble(ExerciceRecommande::getScore).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────
    // METHODES PRIVEES — LOGIQUE IA
    // ─────────────────────────────────────────────

    private ProgressionIAModel getOuCreerModele(String userId, String exercice,
                                                List<Double> historique) {
        String cle = userId + "_" + exercice.toLowerCase();
        ProgressionIAModel model = modelsCache.get(cle);

        if (model == null || !model.isEntrainee()) {
            model = new ProgressionIAModel();
            model.entrainer(historique);
            modelsCache.put(cle, model);
            LOG.info("[IA] Nouveau modèle entraîné pour: " + cle);
        } else {
            // Ré-entraîner si l'historique a changé (nouvelles données)
            model.entrainer(historique);
        }

        return model;
    }

    private double moyenneDerniers(List<Double> valeurs, int n) {
        int debut = Math.max(0, valeurs.size() - n);
        return valeurs.subList(debut, valeurs.size())
                .stream().mapToDouble(d -> d).average().orElse(0);
    }

    private double calculerScoreIA(ExerciceData ex, NiveauSportif niveau,
                                   List<String> equipements) {
        double score = 0.5; // base

        // Bonus équipement disponible
        if (equipements.stream().anyMatch(e ->
                e.equalsIgnoreCase(ex.getEquipement()))) {
            score += 0.3;
        }

        // Ajustement selon niveau
        switch (niveau) {
            case DEBUTANT:
                if (ex.getMusclesSecondaires().size() <= 1) score += 0.2;
                break;
            case INTERMEDIAIRE:
                score += 0.1;
                break;
            case AVANCE:
                if (ex.getMusclesSecondaires().size() >= 2) score += 0.2;
                break;
        }

        return Math.min(score, 1.0);
    }

    private String genererRaison(ExerciceData ex, NiveauSportif niveau) {
        return String.format(
                "Cible %s | Secondaires: %s | Équipement: %s | Adapté niveau %s",
                ex.getMusclesCibles(),
                String.join(", ", ex.getMusclesSecondaires()),
                ex.getEquipement(),
                niveau
        );
    }

    // ─────────────────────────────────────────────
    // CLASSES INTERNES : RESULTATS
    // ─────────────────────────────────────────────

    public static class RapportProgression {
        private final String userId;
        private final String exercice;
        private final List<Double> historique;
        private final ResultatPrediction prediction;
        private final double r2;
        private final String resumeModele;
        private final ExerciceData donneesAPI;

        public RapportProgression(String userId, String exercice, List<Double> historique,
                                  ResultatPrediction prediction, double r2,
                                  String resumeModele, ExerciceData donneesAPI) {
            this.userId = userId; this.exercice = exercice;
            this.historique = historique; this.prediction = prediction;
            this.r2 = r2; this.resumeModele = resumeModele;
            this.donneesAPI = donneesAPI;
        }

        public ResultatPrediction getPrediction() { return prediction; }
        public double getR2()                     { return r2; }
        public ExerciceData getDonneesAPI()       { return donneesAPI; }
        public String getResumeModele()           { return resumeModele; }

        @Override
        public String toString() {
            return String.format(
                    "\n═══ RAPPORT IA — %s (%s) ═══" +
                            "\n  Historique    : %s" +
                            "\n  Modèle IA     : %s" +
                            "\n  Fiabilité R²  : %.2f (%s)" +
                            "\n  %s" +
                            "\n  [API] Muscle  : %s | Équipement: %s" +
                            "\n  [API] Instruct: %s",
                    exercice.toUpperCase(), userId,
                    historique,
                    resumeModele,
                    r2, r2 > 0.8 ? "Très fiable" : r2 > 0.5 ? "Fiable" : "Données insuffisantes",
                    prediction,
                    donneesAPI.getMusclesCibles(), donneesAPI.getEquipement(),
                    donneesAPI.getInstructions().isEmpty() ? "N/A"
                            : donneesAPI.getInstructions().get(0)
            );
        }
    }

    public enum NiveauRisque { AUCUN, FAIBLE, MODERE, ELEVE, CRITIQUE }
    public enum NiveauSportif { DEBUTANT, INTERMEDIAIRE, AVANCE }

    public static class AlerteSurcharge {
        private final NiveauRisque niveau;
        private final String conseil;
        private final double score;

        public AlerteSurcharge(NiveauRisque niveau, String conseil, double score) {
            this.niveau = niveau; this.conseil = conseil; this.score = score;
        }

        public NiveauRisque getNiveau() { return niveau; }
        public String getConseil()      { return conseil; }
        public double getScore()        { return score; }

        @Override
        public String toString() {
            return String.format("[SURCHARGE] Niveau: %s | Score: %.2f | %s",
                    niveau, score, conseil);
        }
    }

    public static class ExerciceRecommande {
        private final ExerciceData exercice;
        private final double score;
        private final String raison;

        public ExerciceRecommande(ExerciceData exercice, double score, String raison) {
            this.exercice = exercice; this.score = score; this.raison = raison;
        }

        public ExerciceData getExercice() { return exercice; }
        public double getScore()          { return score; }
        public String getRaison()         { return raison; }

        @Override
        public String toString() {
            return String.format("[%.0f%%] %s — %s",
                    score * 100, exercice.getNom(), raison);
        }
    }
}
