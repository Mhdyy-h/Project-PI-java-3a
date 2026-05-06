package org.example.metier.ia;

import java.util.List;

/**
 * MODELE IA — Régression linéaire simple (Java pur, zéro librairie)
 *
 * Prédit le prochain record personnel (PR) d'un utilisateur
 * sur un exercice donné, basé sur l'historique de ses séances.
 *
 * Algorithme : y = a*x + b
 *   x = numéro de séance (1, 2, 3, ...)
 *   y = performance (poids max soulevé, ou nombre de reps)
 *   a = coefficient directeur (pente = vitesse de progression)
 *   b = ordonnée à l'origine
 */
public class ProgressionIAModel {

    private double a; // pente
    private double b; // intercept
    private boolean trained = false;

    // ─────────────────────────────────────────────
    // ENTRAINEMENT DU MODELE
    // ─────────────────────────────────────────────

    /**
     * Entraîne le modèle sur l'historique de performances.
     *
     * @param performances liste ordonnée des performances (poids ou reps)
     *                     Ex: [60.0, 62.5, 65.0, 65.0, 67.5]
     * @throws IllegalArgumentException si moins de 2 points de données
     */
    public void entrainer(List<Double> performances) {
        if (performances == null || performances.size() < 2) {
            throw new IllegalArgumentException(
                    "Il faut au moins 2 séances pour entraîner le modèle IA."
            );
        }

        int n = performances.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (int i = 0; i < n; i++) {
            double x = i + 1; // numéro de séance (commence à 1)
            double y = performances.get(i);
            sumX  += x;
            sumY  += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        // Formule moindres carrés
        double denominateur = (n * sumX2) - (sumX * sumX);
        if (denominateur == 0) {
            throw new ArithmeticException("Données insuffisantes : toutes les séances ont le même numéro.");
        }

        this.a = ((n * sumXY) - (sumX * sumY)) / denominateur;
        this.b = (sumY - (this.a * sumX)) / n;
        this.trained = true;
    }

    // ─────────────────────────────────────────────
    // PREDICTION
    // ─────────────────────────────────────────────

    /**
     * Prédit la performance à la séance numéro X.
     *
     * @param numeroSeance numéro futur (ex: historique.size() + 4 = dans 4 séances)
     * @return performance prédite (arrondie à 0.5 kg)
     */
    public double predire(int numeroSeance) {
        verifierEntrainement();
        double raw = (a * numeroSeance) + b;
        return arrondir(raw, 0.5); // arrondi au 0.5 kg le plus proche
    }

    /**
     * Prédit le PR dans N séances futures.
     *
     * @param nbSeancesPassees  nombre de séances déjà effectuées
     * @param seancesFutures    dans combien de séances (ex: 4)
     * @return ResultatPrediction contenant la valeur et l'interprétation
     */
    public ResultatPrediction predireFutur(int nbSeancesPassees, int seancesFutures) {
        verifierEntrainement();

        int seanceCible = nbSeancesPassees + seancesFutures;
        double valeurPredite = predire(seanceCible);
        double dernierePerfConnue = predire(nbSeancesPassees);

        double progression = valeurPredite - dernierePerfConnue;
        double progressionPct = (dernierePerfConnue > 0)
                ? (progression / dernierePerfConnue) * 100
                : 0;

        String interpretation = interpreterProgression(progressionPct);

        return new ResultatPrediction(
                valeurPredite,
                progression,
                progressionPct,
                seancesFutures,
                interpretation
        );
    }

    // ─────────────────────────────────────────────
    // QUALITE DU MODELE
    // ─────────────────────────────────────────────

    /**
     * Calcule le coefficient R² (qualité du modèle).
     * R² proche de 1.0 = modèle très fiable.
     * R² proche de 0.0 = progression irrégulière.
     */
    public double calculerR2(List<Double> performances) {
        verifierEntrainement();

        int n = performances.size();
        double moyenne = performances.stream().mapToDouble(d -> d).average().orElse(0);

        double ssTot = 0, ssRes = 0;
        for (int i = 0; i < n; i++) {
            double yReel = performances.get(i);
            double yPredit = predire(i + 1);
            ssTot += Math.pow(yReel - moyenne, 2);
            ssRes += Math.pow(yReel - yPredit, 2);
        }

        return (ssTot == 0) ? 1.0 : 1.0 - (ssRes / ssTot);
    }

    // ─────────────────────────────────────────────
    // GETTERS / INFOS MODELE
    // ─────────────────────────────────────────────

    public double getPente()     { return a; }
    public double getIntercept() { return b; }
    public boolean isEntrainee() { return trained; }

    public String getResume() {
        verifierEntrainement();
        String tendance = a > 0 ? "progression" : (a < 0 ? "régression" : "stagnation");
        return String.format(
                "Modèle IA: y = %.3f*x + %.3f | Tendance: %s (%.2f/séance)",
                a, b, tendance, a
        );
    }

    // ─────────────────────────────────────────────
    // METHODES PRIVEES
    // ─────────────────────────────────────────────

    private void verifierEntrainement() {
        if (!trained) {
            throw new IllegalStateException(
                    "Modèle non entraîné. Appeler entrainer() d'abord."
            );
        }
    }

    private double arrondir(double valeur, double multiple) {
        return Math.round(valeur / multiple) * multiple;
    }

    private String interpreterProgression(double pct) {
        if (pct >= 5)       return "Progression excellente — continuez ce rythme !";
        if (pct >= 2)       return "Bonne progression — objectif atteignable.";
        if (pct >= 0)       return "Progression légère — augmentez l'intensité.";
        if (pct >= -2)      return "Légère régression — vérifiez la récupération.";
        return                     "Régression importante — repos conseillé.";
    }

    // ─────────────────────────────────────────────
    // CLASSE INTERNE : RESULTAT
    // ─────────────────────────────────────────────

    public static class ResultatPrediction {
        private final double valeurPredite;
        private final double progression;
        private final double progressionPourcentage;
        private final int seancesFutures;
        private final String interpretation;

        public ResultatPrediction(double valeurPredite, double progression,
                                  double progressionPourcentage, int seancesFutures,
                                  String interpretation) {
            this.valeurPredite = valeurPredite;
            this.progression = progression;
            this.progressionPourcentage = progressionPourcentage;
            this.seancesFutures = seancesFutures;
            this.interpretation = interpretation;
        }

        public double getValeurPredite()         { return valeurPredite; }
        public double getProgression()           { return progression; }
        public double getProgressionPourcentage(){ return progressionPourcentage; }
        public int    getSeancesFutures()        { return seancesFutures; }
        public String getInterpretation()        { return interpretation; }

        @Override
        public String toString() {
            return String.format(
                    "[IA] Prédiction dans %d séances: %.1f kg/reps | +%.1f (%.1f%%) | %s",
                    seancesFutures, valeurPredite, progression,
                    progressionPourcentage, interpretation
            );
        }
    }
}
