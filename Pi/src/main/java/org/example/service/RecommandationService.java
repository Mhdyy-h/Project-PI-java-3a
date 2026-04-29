package org.example.service;

import org.example.model.Aliment;
import org.example.model.Recommandation;
import org.example.model.Recommandation.Priorite;
import org.example.model.Recommandation.TypeReco;
import org.example.model.Repas;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ══════════════════════════════════════════════════════════════════════
 *  MOTEUR DE RECOMMANDATIONS NUTRITIONNELLES
 *  Basé sur des règles métier intelligentes (sans IA externe)
 *
 *  Règles implémentées :
 *    TIMING   : repas tardif, absence de petit-déjeuner, espacement,
 *               excitants le soir, régularité
 *    NUTRITION: protéines le matin, index glycémique, calories excessives,
 *               équilibre macros, pas de légumes
 *    EQUILIBRE: nombre de repas, variété, ratio protéines/glucides
 *    POSITIF  : renforcement positif quand les habitudes sont bonnes
 * ══════════════════════════════════════════════════════════════════════
 */
public class RecommandationService {

    // ── Seuils configurables ───────────────────────────────────────────
    private static final int    HEURE_REPAS_TARDIF       = 22;   // après 22h = tardif
    private static final int    HEURE_EXCITANT_SOIR      = 16;   // excitants après 16h
    private static final int    CALORIES_SEUIL_ELEVE     = 2500; // cal/jour
    private static final int    CALORIES_SEUIL_FAIBLE    = 1200; // cal/jour
    private static final double PROTEINES_MIN_MATIN_G    = 10.0; // g de protéines au petit-déj
    private static final double IG_ELEVE                 = 70.0; // index glycémique élevé
    private static final int    GAP_REPAS_HEURES         = 6;    // gap max recommandé entre repas

    // ══════════════════════════════════════════════════════════════════
    //  POINT D'ENTRÉE PRINCIPAL
    // ══════════════════════════════════════════════════════════════════

    /**
     * Analyse tous les repas d'un utilisateur et retourne une liste
     * de recommandations personnalisées, triées par priorité décroissante.
     *
     * @param tousLesRepas  Tous les repas de l'utilisateur (historique complet)
     * @return Liste de recommandations, max 6, triées par priorité
     */
    public static List<Recommandation> analyser(List<Repas> tousLesRepas) {
        if (tousLesRepas == null || tousLesRepas.isEmpty()) {
            return Collections.singletonList(
                new Recommandation("🍽️",
                    "Aucun repas enregistré",
                    "Ajoutez votre premier repas pour recevoir des conseils personnalisés.",
                    Priorite.BASSE, TypeReco.EQUILIBRE));
        }

        List<Recommandation> recommandations = new ArrayList<>();

        // Repas du jour uniquement pour les règles de timing
        LocalDate aujourd_hui = LocalDate.now();
        List<Repas> repasJour = tousLesRepas.stream()
                .filter(r -> r.getDateConsommation().toLocalDate().equals(aujourd_hui))
                .collect(Collectors.toList());

        // Repas de la semaine (7 derniers jours)
        LocalDate semaineDébut = aujourd_hui.minusDays(7);
        List<Repas> repasSemaine = tousLesRepas.stream()
                .filter(r -> !r.getDateConsommation().toLocalDate().isBefore(semaineDébut))
                .collect(Collectors.toList());

        // ── Appliquer toutes les règles ───────────────────────────────
        appliquerReglesRepasJour(repasJour, recommandations);
        appliquerReglesNutrition(repasJour, tousLesRepas, recommandations);
        appliquerReglesSemaine(repasSemaine, recommandations);
        appliquerReglesPositives(repasJour, repasSemaine, recommandations);

        // ── Trier par priorité et limiter à 6 ───────────────────────
        recommandations.sort(Comparator.comparingInt(r ->
                r.getPriorite() == Priorite.HAUTE ? 0 :
                r.getPriorite() == Priorite.MOYENNE ? 1 : 2));

        return recommandations.size() > 6
                ? recommandations.subList(0, 6)
                : recommandations;
    }

    // ══════════════════════════════════════════════════════════════════
    //  RÈGLES : TIMING DES REPAS DU JOUR
    // ══════════════════════════════════════════════════════════════════

    private static void appliquerReglesRepasJour(List<Repas> repasJour,
                                                  List<Recommandation> recos) {

        // Règle 1 : Repas tardif (après 22h)
        repasJour.stream()
                .filter(r -> r.getDateConsommation().getHour() >= HEURE_REPAS_TARDIF)
                .findFirst()
                .ifPresent(r -> recos.add(new Recommandation("🌙",
                        "Tu as mangé après " + HEURE_REPAS_TARDIF + "h",
                        "Manger tard perturbe le sommeil et réduit l'énergie le lendemain. "
                        + "Essaie de dîner avant 20h.",
                        Priorite.HAUTE, TypeReco.TIMING)));

        // Règle 2 : Pas de petit-déjeuner aujourd'hui
        boolean aPetitDej = repasJour.stream()
                .anyMatch(r -> "MATIN".equals(r.getTypeMoment()));
        if (!aPetitDej && !repasJour.isEmpty()) {
            recos.add(new Recommandation("☀️",
                    "Petit-déjeuner manqué aujourd'hui",
                    "Le petit-déjeuner active ton métabolisme. "
                    + "Même une collation légère le matin améliore la concentration.",
                    Priorite.HAUTE, TypeReco.TIMING));
        }

        // Règle 3 : Excitants le soir (café, thé fort, cola…)
        repasJour.stream()
                .filter(r -> r.getDateConsommation().getHour() >= HEURE_EXCITANT_SOIR
                          && r.contientExcitant())
                .findFirst()
                .ifPresent(r -> recos.add(new Recommandation("☕",
                        "Excitants consommés après " + HEURE_EXCITANT_SOIR + "h",
                        "Le café, le thé ou le cola pris en soirée nuisent à la qualité du sommeil. "
                        + "Préfère une tisane ou de l'eau.",
                        Priorite.HAUTE, TypeReco.TIMING)));

        // Règle 4 : Grand écart entre deux repas (> 6h)
        if (repasJour.size() >= 2) {
            List<Repas> tries = repasJour.stream()
                    .sorted(Comparator.comparing(Repas::getDateConsommation))
                    .collect(Collectors.toList());
            for (int i = 1; i < tries.size(); i++) {
                LocalDateTime prec = tries.get(i - 1).getDateConsommation();
                LocalDateTime suiv = tries.get(i).getDateConsommation();
                long heures = java.time.Duration.between(prec, suiv).toHours();
                if (heures > GAP_REPAS_HEURES) {
                    recos.add(new Recommandation("⏱️",
                            heures + "h d'écart entre deux repas",
                            "Un jeûne prolongé entre les repas peut provoquer une hypoglycémie. "
                            + "Prévois une collation légère.",
                            Priorite.MOYENNE, TypeReco.TIMING));
                    break; // une seule alerte de ce type suffit
                }
            }
        }

        // Règle 5 : Un seul repas dans la journée
        if (repasJour.size() == 1) {
            recos.add(new Recommandation("🍴",
                    "Un seul repas enregistré aujourd'hui",
                    "Fractionner en 3–4 repas améliore l'énergie, la digestion et le métabolisme.",
                    Priorite.MOYENNE, TypeReco.EQUILIBRE));
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  RÈGLES : QUALITÉ NUTRITIONNELLE
    // ══════════════════════════════════════════════════════════════════

    private static void appliquerReglesNutrition(List<Repas> repasJour,
                                                   List<Repas> tousLesRepas,
                                                   List<Recommandation> recos) {

        // Règle 6 : Pas de protéines au petit-déjeuner
        Optional<Repas> petitDej = repasJour.stream()
                .filter(r -> "MATIN".equals(r.getTypeMoment()))
                .findFirst();
        if (petitDej.isPresent()) {
            double protMatin = petitDej.get().getTotalProteines();
            if (protMatin < PROTEINES_MIN_MATIN_G) {
                recos.add(new Recommandation("💪",
                        "Peu de protéines au petit-déjeuner (" + (int) protMatin + "g)",
                        "Ajouter ≥10g de protéines le matin (œuf, yaourt grec, fromage blanc) "
                        + "améliore la satiété et la performance cognitive.",
                        Priorite.MOYENNE, TypeReco.NUTRITION));
            }
        }

        // Règle 7 : Calories du jour trop élevées
        int calJour = repasJour.stream().mapToInt(Repas::getTotalCalories).sum();
        if (calJour > CALORIES_SEUIL_ELEVE) {
            recos.add(new Recommandation("🔥",
                    "Apport calorique élevé : " + calJour + " cal aujourd'hui",
                    "L'apport recommandé est 1800–2200 cal/jour selon l'activité. "
                    + "Privilégie des aliments rassasiants faibles en calories.",
                    Priorite.HAUTE, TypeReco.NUTRITION));
        }

        // Règle 8 : Calories du jour trop faibles
        if (!repasJour.isEmpty() && calJour < CALORIES_SEUIL_FAIBLE) {
            recos.add(new Recommandation("⚡",
                    "Apport calorique insuffisant : " + calJour + " cal aujourd'hui",
                    "Un apport trop faible ralentit le métabolisme et réduit l'énergie. "
                    + "Pense à ajouter une collation nutritive.",
                    Priorite.HAUTE, TypeReco.NUTRITION));
        }

        // Règle 9 : Index glycémique moyen élevé
        List<Aliment> tousAlimentsJour = repasJour.stream()
                .flatMap(r -> r.getAliments().stream())
                .collect(Collectors.toList());
        if (!tousAlimentsJour.isEmpty()) {
            double igMoyen = tousAlimentsJour.stream()
                    .mapToInt(Aliment::getIndexGlycemique)
                    .filter(ig -> ig > 0)
                    .average().orElse(0);
            if (igMoyen > IG_ELEVE) {
                recos.add(new Recommandation("📈",
                        "Index glycémique moyen élevé (" + (int) igMoyen + ")",
                        "Des aliments à IG élevé provoquent des pics de glycémie et de la fatigue. "
                        + "Préfère des céréales complètes, légumineuses, légumes.",
                        Priorite.MOYENNE, TypeReco.NUTRITION));
            }
        }

        // Règle 10 : Déséquilibre macros (trop de lipides)
        double totalProt  = repasJour.stream().mapToDouble(Repas::getTotalProteines).sum();
        double totalGluc  = repasJour.stream().mapToDouble(Repas::getTotalGlucides).sum();
        double totalLip   = repasJour.stream().mapToDouble(Repas::getTotalLipides).sum();
        double totalMacro = totalProt + totalGluc + totalLip;
        if (totalMacro > 0) {
            double pctLip  = (totalLip  * 9 / calJour) * 100;
            double pctProt = (totalProt * 4 / calJour) * 100;
            if (pctLip > 40) {
                recos.add(new Recommandation("🧈",
                        "Apport en lipides élevé (" + (int) pctLip + "% des calories)",
                        "Les lipides ne doivent pas dépasser 35% des calories. "
                        + "Réduis les fritures, les charcuteries et les sauces grasses.",
                        Priorite.MOYENNE, TypeReco.NUTRITION));
            }
            if (pctProt < 15 && !repasJour.isEmpty()) {
                recos.add(new Recommandation("🥩",
                        "Apport protéique faible (" + (int) pctProt + "% des calories)",
                        "Les protéines doivent représenter 15–25% des calories. "
                        + "Intègre viandes maigres, poissons, légumineuses ou œufs.",
                        Priorite.BASSE, TypeReco.NUTRITION));
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  RÈGLES : TENDANCES SUR LA SEMAINE
    // ══════════════════════════════════════════════════════════════════

    private static void appliquerReglesSemaine(List<Repas> repasSemaine,
                                                List<Recommandation> recos) {

        // Règle 11 : Excitants en soirée fréquents sur la semaine
        long soirsExcitants = repasSemaine.stream()
                .filter(r -> r.getDateConsommation().getHour() >= HEURE_EXCITANT_SOIR
                          && r.contientExcitant())
                .map(r -> r.getDateConsommation().toLocalDate())
                .distinct()
                .count();
        if (soirsExcitants >= 3) {
            recos.add(new Recommandation("😴",
                    "Excitants en soirée " + soirsExcitants + " fois cette semaine",
                    "Cette habitude chronique nuit au sommeil profond. "
                    + "Essaie de couper les excitants après 14h pendant 1 semaine.",
                    Priorite.HAUTE, TypeReco.TIMING));
        }

        // Règle 12 : Repas tardifs fréquents
        long nuitsTardives = repasSemaine.stream()
                .filter(r -> r.getDateConsommation().getHour() >= HEURE_REPAS_TARDIF)
                .map(r -> r.getDateConsommation().toLocalDate())
                .distinct()
                .count();
        if (nuitsTardives >= 3) {
            recos.add(new Recommandation("🌛",
                    "Repas tardifs " + nuitsTardives + " soirs cette semaine",
                    "Manger régulièrement après " + HEURE_REPAS_TARDIF + "h perturbe "
                    + "le rythme circadien. Planifie tes dîners à heure fixe.",
                    Priorite.HAUTE, TypeReco.TIMING));
        }

        // Règle 13 : Score moyen faible sur la semaine
        if (!repasSemaine.isEmpty()) {
            double moyenneSemaine = repasSemaine.stream()
                    .mapToInt(Repas::getPointsGagnes)
                    .average().orElse(0);
            if (moyenneSemaine < 5 && repasSemaine.size() >= 3) {
                recos.add(new Recommandation("🎯",
                        "Score ChronoScore moyen bas cette semaine",
                        "Ton score hebdomadaire est faible (" + (int) moyenneSemaine + "/14). "
                        + "Essaie de manger aux horaires optimaux pour améliorer ton score.",
                        Priorite.MOYENNE, TypeReco.EQUILIBRE));
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  RÈGLES POSITIVES (Renforcement)
    // ══════════════════════════════════════════════════════════════════

    private static void appliquerReglesPositives(List<Repas> repasJour,
                                                   List<Repas> repasSemaine,
                                                   List<Recommandation> recos) {

        // Bravo : 3+ repas bien répartis aujourd'hui
        if (repasJour.size() >= 3) {
            boolean sansTardif = repasJour.stream()
                    .noneMatch(r -> r.getDateConsommation().getHour() >= HEURE_REPAS_TARDIF);
            if (sansTardif) {
                recos.add(new Recommandation("🏆",
                        "Excellente régularité des repas aujourd'hui !",
                        "3 repas bien répartis sans excès tardif = énergie stable toute la journée.",
                        Priorite.BASSE, TypeReco.POSITIF));
            }
        }

        // Bravo : bon score moyen cette semaine
        if (!repasSemaine.isEmpty()) {
            double moyenneSemaine = repasSemaine.stream()
                    .mapToInt(Repas::getPointsGagnes)
                    .average().orElse(0);
            if (moyenneSemaine >= 10) {
                recos.add(new Recommandation("⭐",
                        "Super ChronoScore cette semaine (" + (int) moyenneSemaine + "/14) !",
                        "Continue comme ça ! Tes habitudes alimentaires sont sur la bonne voie.",
                        Priorite.BASSE, TypeReco.POSITIF));
            }
        }

        // Bravo : petit-déj pris tous les jours de la semaine
        long joursAvecPetitDej = repasSemaine.stream()
                .filter(r -> "MATIN".equals(r.getTypeMoment()))
                .map(r -> r.getDateConsommation().toLocalDate())
                .distinct()
                .count();
        if (joursAvecPetitDej >= 5) {
            recos.add(new Recommandation("🌅",
                    "Petit-déjeuner pris " + joursAvecPetitDej + " fois cette semaine !",
                    "Commencer la journée avec un petit-déjeuner est l'une des meilleures habitudes.",
                    Priorite.BASSE, TypeReco.POSITIF));
        }
    }
}
