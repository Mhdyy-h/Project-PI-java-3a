package org.example.service;

import org.example.model.Repas;
import org.example.model.Aliment;
import org.example.model.ChronoScore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ══════════════════════════════════════════════════════════
 *  CHRONOSCORE SERVICE — Système de Scoring Chronobiologique
 *
 *  Score total = 5 composantes :
 *  ① TimingScore      : heure optimale du repas   (-6  → +4)
 *  ② NutritionScore   : qualité nutritionnelle     (-5  → +10)
 *  ③ EquilibreBonus   : répartition macros idéale  (-3  → +6)
 *  ④ InteractionScore : alignement timing+nutrition(-3  → +3)
 *  ⑤ RiskPenalty      : facteurs de risque         (-4  → 0)
 *
 *  TOTAL sur 14 points (plafonné à 14, plancher à -10)
 * ══════════════════════════════════════════════════════════
 */
public class Chronoscoreservice {

    // ── Calories idéales par moment ──────────────────────
    private static final int CAL_MATIN     = 400;
    private static final int CAL_MIDI      = 650;
    private static final int CAL_COLLATION = 150;
    private static final int CAL_SOIR      = 500;

    // ── Seuils de risque ────────────────────────────────
    private static final int CALORIES_RISQUE    = 1200;
    private static final int HEURE_RISQUE_SOIR  = 21;
    private static final int IG_ELEVE           = 70;
    private static final int IG_BAS             = 55;

    // ══════════════════════════════════════════════════════
    //  MÉTHODE PRINCIPALE
    // ══════════════════════════════════════════════════════

    /**
     * Calcule le ChronoScore complet d'un repas.
     * Retourne un objet ChronoScore avec tous les détails.
     */
    public static ChronoScore calculerChronoScore(Repas repas) {
        ChronoScore score = new ChronoScore();

        // ── ① Timing Score ──
        int timing = calculerTimingScore(repas);
        score.setTimingScore(timing);

        // ── ② Nutrition Score ──
        int nutrition = calculerNutritionScore(repas);
        score.setNutritionScore(nutrition);

        // ── ③ Équilibre Bonus ──
        int equilibre = calculerEquilibreBonus(repas);
        score.setEquilibreBonus(equilibre);

        // ── ④ Interaction Score ──
        int interaction = calculerInteractionScore(timing, nutrition, repas);
        score.setInteractionScore(interaction);

        // ── ⑤ Risk Penalty ──
        int risk = calculerRiskPenalty(repas);
        score.setRiskPenalty(risk);

        // ── Total plafonné ──
        int total = Math.min(14, Math.max(-10,
                timing + nutrition + equilibre + interaction + risk));
        score.setTotalScore(total);
        score.calculerAppreciation();

        // ── Message de risque ──
        if (risk < 0) {
            score.setMessageRisque(construireMessageRisque(repas));
        }

        return score;
    }

    // ══════════════════════════════════════════════════════
    //  ① TIMING SCORE  (-6 → +4)
    //  Évalue si l'heure du repas est chronobiologiquement
    //  optimale selon le type de moment.
    // ══════════════════════════════════════════════════════

    private static int calculerTimingScore(Repas repas) {
        int heure = repas.getDateConsommation().getHour();

        return switch (repas.getTypeMoment()) {

            case "MATIN" ->
                // Optimal : 6h-9h (pic de cortisol)
                    (heure >= 6 && heure <= 9)   ?  4 :
                            (heure >= 5 && heure <= 10)  ?  2 :
                                    (heure >= 10 && heure <= 11) ? -2 : -4;

            case "MIDI" ->
                // Optimal : 12h-13h (digestion optimale)
                    (heure >= 12 && heure <= 13) ?  4 :
                            (heure >= 11 && heure <= 14) ?  2 :
                                    (heure >= 14 && heure <= 15) ? -2 : -4;

            case "COLLATION" ->
                // Optimal : 9h-11h ou 15h-17h (creux métabolique)
                    ((heure >= 9  && heure <= 11) ||
                            (heure >= 15 && heure <= 17)) ?  4 :
                            ((heure >= 8  && heure <= 12) ||
                                    (heure >= 14 && heure <= 18)) ?  1 : -3;

            case "SOIR" ->
                // Optimal : 18h-20h (préparation au repos)
                    (heure >= 18 && heure <= 20) ?  4 :
                            (heure >= 17 && heure <= 21) ?  1 :
                                    (heure >= 21 && heure <= 22) ? -3 : -6; // très tardif

            default -> 0;
        };
    }

    // ══════════════════════════════════════════════════════
    //  ② NUTRITION SCORE  (-5 → +10)
    //  Évalue la qualité nutritionnelle :
    //  - cohérence calories / moment
    //  - index glycémique
    //  - pénalité excitants
    //  - bonus protéines
    // ══════════════════════════════════════════════════════

    private static int calculerNutritionScore(Repas repas) {
        int calories      = repas.getTotalCalories();
        int calIdeales    = getCaloriesIdeales(repas.getTypeMoment());
        double proteines  = repas.getTotalProteines();
        double avgIG      = repas.getAliments().stream()
                .mapToInt(Aliment::getIndexGlycemique)
                .average().orElse(0);

        int score = 0;

        // ── Score calories (cohérence avec le moment) ──
        double ratio = calIdeales > 0 ? (double) calories / calIdeales : 0;
        if      (ratio >= 0.7 && ratio <= 1.1) score += 5;  // dans la plage idéale
        else if (ratio >= 0.5 && ratio <= 1.3) score += 2;  // acceptable
        else if (ratio > 1.5 || ratio < 0.3)   score -= 3;  // très hors norme

        // ── Bonus protéines (reconstituent les muscles) ──
        if      (proteines >= 30) score += 3;
        else if (proteines >= 15) score += 2;
        else if (proteines >= 8)  score += 1;

        // ── Score index glycémique ──
        if      (avgIG > 0 && avgIG < IG_BAS)  score += 2;  // IG bas = bien
        else if (avgIG > IG_ELEVE)              score -= 2;  // IG élevé = risque

        // ── Pénalité aliments excitants ──
        long nbExcitants = repas.getAliments().stream()
                .filter(Aliment::isEstExcitant).count();
        score -= (int)(nbExcitants * 2);

        // ── Pénalité glucides excessifs ──
        if (repas.getTotalGlucides() > 100) score -= 2;

        // ── Pénalité lipides excessifs ──
        if (repas.getTotalLipides() > 40)   score -= 2;

        return Math.max(-5, Math.min(10, score));
    }

    // ══════════════════════════════════════════════════════
    //  ③ ÉQUILIBRE BONUS  (-3 → +6)
    //  Répartition idéale : 50% glucides / 20% protéines / 30% lipides
    // ══════════════════════════════════════════════════════

    private static int calculerEquilibreBonus(Repas repas) {
        if (repas.getTotalCalories() == 0) return 0;

        double cal = repas.getTotalCalories();
        double pctProteines = (repas.getTotalProteines() * 4) / cal * 100;
        double pctGlucides  = (repas.getTotalGlucides()  * 4) / cal * 100;
        double pctLipides   = (repas.getTotalLipides()   * 9) / cal * 100;

        int score = 0;

        // Protéines : idéal 15-25%
        if      (pctProteines >= 15 && pctProteines <= 25) score += 2;
        else if (pctProteines >= 10 && pctProteines <= 30) score += 1;
        else score -= 1;

        // Glucides : idéal 45-55%
        if      (pctGlucides >= 45 && pctGlucides <= 55) score += 2;
        else if (pctGlucides >= 40 && pctGlucides <= 60) score += 1;
        else score -= 1;

        // Lipides : idéal 25-35%
        if      (pctLipides >= 25 && pctLipides <= 35) score += 2;
        else if (pctLipides >= 20 && pctLipides <= 40) score += 1;
        else score -= 1;

        // Bonus équilibre parfait (les 3 en même temps)
        if (pctProteines >= 15 && pctProteines <= 25
                && pctGlucides  >= 45 && pctGlucides  <= 55
                && pctLipides   >= 25 && pctLipides   <= 35) {
            score += 1; // bonus équilibre parfait
        }

        return Math.max(-3, Math.min(6, score));
    }

    // ══════════════════════════════════════════════════════
    //  ④ INTERACTION SCORE  (-3 → +3)
    //  Bonus/Malus selon l'alignement timing + nutrition
    // ══════════════════════════════════════════════════════

    private static int calculerInteractionScore(
            int timingScore, int nutritionScore, Repas repas) {

        int score = 0;

        // Bonus : bon timing ET bonne nutrition
        if (timingScore >= 2 && nutritionScore >= 3) score += 3;

            // Malus : mauvais timing ET mauvaise nutrition
        else if (timingScore <= -2 && nutritionScore <= 0) score -= 3;

        // Malus spécial : repas calorique très tardif (> 900 cal après 21h)
        if (repas.getDateConsommation().getHour() >= 21
                && repas.getTotalCalories() > 900) score -= 2;

        // Malus : excitants le soir
        if ("SOIR".equals(repas.getTypeMoment())
                && repas.contientExcitant()) score -= 2;

        return Math.max(-3, Math.min(3, score));
    }

    // ══════════════════════════════════════════════════════
    //  ⑤ RISK PENALTY  (-4 → 0)
    //  Détection de risques métaboliques cumulés
    // ══════════════════════════════════════════════════════

    private static int calculerRiskPenalty(Repas repas) {
        int facteurs = 0;

        // Facteur 1 : repas après 21h
        if (repas.getDateConsommation().getHour() >= HEURE_RISQUE_SOIR)
            facteurs++;

        // Facteur 2 : calories excessives (> 1200 cal)
        if (repas.getTotalCalories() > CALORIES_RISQUE)
            facteurs++;

        // Facteur 3 : excitants en soirée
        if ("SOIR".equals(repas.getTypeMoment()) && repas.contientExcitant())
            facteurs++;

        // Facteur 4 : index glycémique très élevé (> 70)
        double avgIG = repas.getAliments().stream()
                .mapToInt(Aliment::getIndexGlycemique)
                .average().orElse(0);
        if (avgIG > IG_ELEVE) facteurs++;

        // Facteur 5 : lipides très élevés (> 40g)
        if (repas.getTotalLipides() > 40) facteurs++;

        // Pénalité proportionnelle
        if      (facteurs >= 4) return -4;
        else if (facteurs == 3) return -3;
        else if (facteurs == 2) return -2;
        else if (facteurs == 1) return -1;
        return 0;
    }

    // ══════════════════════════════════════════════════════
    //  MESSAGE DE RISQUE DÉTAILLÉ
    // ══════════════════════════════════════════════════════

    private static String construireMessageRisque(Repas repas) {
        StringBuilder sb = new StringBuilder("Risques detectes : ");
        int heure = repas.getDateConsommation().getHour();

        if (heure >= HEURE_RISQUE_SOIR)
            sb.append("repas tardif (").append(heure).append("h) | ");
        if (repas.getTotalCalories() > CALORIES_RISQUE)
            sb.append("calories excessives (")
                    .append(repas.getTotalCalories()).append(" cal) | ");
        if ("SOIR".equals(repas.getTypeMoment()) && repas.contientExcitant())
            sb.append("excitants le soir | ");

        double avgIG = repas.getAliments().stream()
                .mapToInt(Aliment::getIndexGlycemique)
                .average().orElse(0);
        if (avgIG > IG_ELEVE)
            sb.append("IG eleve (").append(String.format("%.0f", avgIG)).append(") | ");
        if (repas.getTotalLipides() > 40)
            sb.append("lipides excessifs (")
                    .append(String.format("%.1f", repas.getTotalLipides())).append("g) | ");

        String msg = sb.toString();
        if (msg.endsWith(" | ")) msg = msg.substring(0, msg.length() - 3);
        return msg;
    }

    // ══════════════════════════════════════════════════════
    //  ANALYSE TENDANCE HEBDOMADAIRE
    // ══════════════════════════════════════════════════════

    /**
     * Analyse la tendance sur une liste de scores.
     * Retourne un message selon la moyenne.
     */
    public static String analyserTendanceHebdomadaire(List<ChronoScore> scores) {
        if (scores.isEmpty()) return "Pas assez de donnees";

        double moyenne = scores.stream()
                .mapToInt(ChronoScore::getTotalScore)
                .average().orElse(0);

        if      (moyenne >= 10) return "Excellent ! Continuez ainsi !";
        else if (moyenne >= 7)  return "Bon, mais peut encore s'ameliorer";
        else if (moyenne >= 4)  return "Moyen, faites attention a vos repas";
        else                    return "Critique ! Consultez un nutritionniste";
    }

    /**
     * Retourne des statistiques détaillées sur une liste de scores.
     * Utile pour le dashboard.
     */
    public static Map<String, Object> getStatistiques(List<ChronoScore> scores) {
        Map<String, Object> stats = new HashMap<>();
        if (scores.isEmpty()) {
            stats.put("moyenne", 0.0);
            stats.put("meilleur", 0);
            stats.put("pire", 0);
            stats.put("nbExcellent", 0);
            stats.put("nbCritique", 0);
            stats.put("tendance", "Pas assez de donnees");
            return stats;
        }

        double moyenne  = scores.stream().mapToInt(ChronoScore::getTotalScore).average().orElse(0);
        int meilleur    = scores.stream().mapToInt(ChronoScore::getTotalScore).max().orElse(0);
        int pire        = scores.stream().mapToInt(ChronoScore::getTotalScore).min().orElse(0);
        long nbExcellent= scores.stream().filter(s -> s.getTotalScore() >= 10).count();
        long nbCritique = scores.stream().filter(s -> s.getTotalScore() < 4).count();

        stats.put("moyenne",     Math.round(moyenne * 10.0) / 10.0);
        stats.put("meilleur",    meilleur);
        stats.put("pire",        pire);
        stats.put("nbExcellent", (int) nbExcellent);
        stats.put("nbCritique",  (int) nbCritique);
        stats.put("tendance",    analyserTendanceHebdomadaire(scores));

        return stats;
    }

    // ══════════════════════════════════════════════════════
    //  UTILITAIRES
    // ══════════════════════════════════════════════════════

    private static int getCaloriesIdeales(String type) {
        return switch (type) {
            case "MATIN"     -> CAL_MATIN;
            case "MIDI"      -> CAL_MIDI;
            case "COLLATION" -> CAL_COLLATION;
            case "SOIR"      -> CAL_SOIR;
            default          -> 500;
        };
    }

    /**
     * Retourne la couleur CSS selon le score.
     * Utile pour l'affichage dans les controllers.
     */
    public static String getCouleurScore(int score) {
        if      (score >= 10) return "#27ae60"; // vert
        else if (score >= 7)  return "#4C6FFF"; // bleu
        else if (score >= 4)  return "#f39c12"; // orange
        else                  return "#e74c3c"; // rouge
    }

    /**
     * Retourne le statut textuel selon le score.
     */
    public static String getStatutScore(int score) {
        if      (score >= 12) return "Excellent alignement chronobiologique";
        else if (score >= 8)  return "Bon alignement";
        else if (score >= 4)  return "Alignement modere";
        else if (score >= 0)  return "Desequilibre a surveiller";
        else                  return "Risque metabolique";
    }
}