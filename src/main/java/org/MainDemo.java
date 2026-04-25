package org;

import org.example.metier.service.SeanceSportService;

import java.util.List;

/**
 * DEMONSTRATION COMPLETE — Pour l'examinateur
 *
 * Lance ce main pour voir en action :
 *   1. Modèle IA régression linéaire (Java pur)
 *   2. ExerciseDB API avec cache + fallback
 *   3. Analyse surcharge musculaire (ACWR)
 *   4. Recommandation exercices IA + API
 */
public class MainDemo {

    public static void main(String[] args) {

        SeanceSportService service = new SeanceSportService();

        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║     DEMO IA + API — Gestion Sport Java       ║");
        System.out.println("╚══════════════════════════════════════════════╝\n");

        // ── DEMO 1 : Prédiction IA progression Bench Press ──
        System.out.println("▶ DEMO 1 — Prédiction IA (Régression Linéaire)");
        System.out.println("─".repeat(50));

        List<Double> historiqueUser1 = List.of(60.0, 62.5, 65.0, 65.0, 67.5, 70.0);
        RapportProgression rapport = service.predireProgression(
                "user_01", "bench press", historiqueUser1, 4
        );
        System.out.println(rapport);

        // ── DEMO 2 : Prédiction Squat (utilisateur différent) ──
        System.out.println("\n▶ DEMO 2 — Prédiction IA (Squat)");
        System.out.println("─".repeat(50));

        List<Double> historiqueUser2 = List.of(80.0, 82.5, 85.0, 90.0, 92.5);
        RapportProgression rapport2 = service.predireProgression(
                "user_02", "squat", historiqueUser2, 4
        );
        System.out.println(rapport2);

        // ── DEMO 3 : Détection surcharge musculaire ──
        System.out.println("\n▶ DEMO 3 — Analyse Surcharge Musculaire (ACWR)");
        System.out.println("─".repeat(50));

        // Utilisateur qui s'entraîne trop fort les 3 derniers jours
        List<Double> intensitesSurcharge = List.of(5.0, 6.0, 5.5, 9.0, 9.5, 9.8);
        AlerteSurcharge alerteCritique = service.analyserSurcharge(intensitesSurcharge, 0);
        System.out.println("Profil surcharge: " + alerteCritique);

        // Utilisateur bien équilibré
        List<Double> intensitesOk = List.of(6.0, 7.0, 5.0, 6.5, 6.0, 7.0);
        AlerteSurcharge alerteOk = service.analyserSurcharge(intensitesOk, 2);
        System.out.println("Profil équilibré: " + alerteOk);

        // ── DEMO 4 : Recommandation exercices IA + API ──
        System.out.println("\n▶ DEMO 4 — Recommandation Exercices (IA + ExerciseDB API)");
        System.out.println("─".repeat(50));

        List<ExerciceRecommande> recommandations = service.recommanderExercices(
                "chest",
                NiveauSportif.INTERMEDIAIRE,
                List.of("barbell", "dumbbell")
        );

        System.out.println("Top exercices recommandés pour chest (niveau intermédiaire) :");
        recommandations.forEach(r -> System.out.println("  " + r));

        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║            FIN DE LA DEMONSTRATION           ║");
        System.out.println("╚══════════════════════════════════════════════╝");
    }
}
