package org.example;

import org.junit.jupiter.api.*;
import java.util.List;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

class ExerciceTest {

    private List<Exercice> exercices;

    @BeforeEach
    void setUp() {
        exercices = List.of(
                creerExercice(1, "Pompes",        "Faible",  3.5,  1),
                creerExercice(2, "Squat Lourd",   "Élevée",  18.0, 1),
                creerExercice(3, "Vélo Cardio",   "Moyenne", 10.0, 2),
                creerExercice(4, "Planche",        "Faible",  4.0,  2),
                creerExercice(5, "Burpees",        "Élevée",  20.0, 3),
                creerExercice(6, "Pompes Larges",  "Moyenne", null, 3)
        );
    }

    // ── Recherche par nom ──────────────────────────────────────

    @Test
    @DisplayName("Recherche 'pompes' → 2 résultats")
    void testRechercheParNom_trouve() {
        assertEquals(2, filtrerParNom("pompes").size());
    }

    @Test
    @DisplayName("Recherche insensible à la casse")
    void testRechercheInsensibleCasse() {
        assertEquals(
                filtrerParNom("squat").size(),
                filtrerParNom("SQUAT").size()
        );
    }

    @Test
    @DisplayName("Recherche vide → retourne tout")
    void testRechercheVide() {
        assertEquals(6, filtrerParNom("").size());
    }

    @Test
    @DisplayName("Recherche 'natation' → 0 résultat")
    void testRechercheAucunResultat() {
        assertTrue(filtrerParNom("natation").isEmpty());
    }

    // ── Filtre Intensité ───────────────────────────────────────

    @Test
    @DisplayName("Filtre 'Faible' → 2 exercices")
    void testFiltreIntensite_Faible() {
        assertEquals(2, filtrerParIntensite("Faible").size());
    }

    @Test
    @DisplayName("Filtre 'Moyenne' → 2 exercices")
    void testFiltreIntensite_Moyenne() {
        assertEquals(2, filtrerParIntensite("Moyenne").size());
    }

    @Test
    @DisplayName("Filtre 'Élevée' → 2 exercices")
    void testFiltreIntensite_Elevee() {
        assertEquals(2, filtrerParIntensite("Élevée").size());
    }

    @Test
    @DisplayName("Filtre 'Toutes' → retourne tout")
    void testFiltreIntensite_Toutes() {
        assertEquals(6, filtrerParIntensite("Toutes").size());
    }

    // ── Filtre Calories ────────────────────────────────────────

    @Test
    @DisplayName("Calories < 5 cal/min → 3 exercices")
    void testFiltreCalories_MoinsDe5() {
        assertEquals(3, filtrerParCalories("< 5 cal/min").size());
    }

    @Test
    @DisplayName("Calories 5-15 cal/min → 1 exercice")
    void testFiltreCalories_5a15() {
        assertEquals(1, filtrerParCalories("5 \u2013 15 cal/min").size());
    }

    @Test
    @DisplayName("Calories > 15 cal/min → 2 exercices")
    void testFiltreCalories_PlusDe15() {
        assertEquals(2, filtrerParCalories("> 15 cal/min").size());
    }

    // ── Tri ───────────────────────────────────────────────────

    @Test
    @DisplayName("Tri nom A→Z : premier = Burpees")
    void testTriNomAZ() {
        List<Exercice> tries = exercices.stream()
                .filter(e -> e.getNomExercice() != null)
                .sorted((a, b) -> a.getNomExercice().compareToIgnoreCase(b.getNomExercice()))
                .collect(Collectors.toList());
        assertEquals("Burpees", tries.get(0).getNomExercice());
    }

    @Test
    @DisplayName("Tri nom Z→A : premier = Vélo Cardio")
    void testTriNomZA() {
        List<Exercice> tries = exercices.stream()
                .filter(e -> e.getNomExercice() != null)
                .sorted((a, b) -> b.getNomExercice().compareToIgnoreCase(a.getNomExercice()))
                .collect(Collectors.toList());
        assertEquals("Vélo Cardio", tries.get(0).getNomExercice());
    }

    @Test
    @DisplayName("Tri intensité Faible→Élevée : premier = Faible")
    void testTriIntensiteAscendant() {
        List<Exercice> tries = exercices.stream()
                .sorted((a, b) -> Integer.compare(
                        ordreIntensite(a.getIntensite()),
                        ordreIntensite(b.getIntensite())))
                .collect(Collectors.toList());
        assertEquals("Faible", tries.get(0).getIntensite());
    }

    @Test
    @DisplayName("Tri intensité Élevée→Faible : premier = Élevée")
    void testTriIntensiteDescendant() {
        List<Exercice> tries = exercices.stream()
                .sorted((a, b) -> Integer.compare(
                        ordreIntensite(b.getIntensite()),
                        ordreIntensite(a.getIntensite())))
                .collect(Collectors.toList());
        assertEquals("Élevée", tries.get(0).getIntensite());
    }

    // ── Helpers ───────────────────────────────────────────────

    private Exercice creerExercice(int id, String nom, String intensite,
                                   Double calories, int seanceId) {
        Exercice e = new Exercice();
        e.setId(id);
        e.setNomExercice(nom);
        e.setIntensite(intensite);
        e.setCaloriesParMinute(calories != null ? calories : 0.0);
        e.setSeanceId(seanceId);
        return e;
    }

    private List<Exercice> filtrerParNom(String recherche) {
        return exercices.stream()
                .filter(e -> recherche.isEmpty() ||
                        (e.getNomExercice() != null &&
                                e.getNomExercice().toLowerCase().contains(recherche.toLowerCase())))
                .collect(Collectors.toList());
    }

    private List<Exercice> filtrerParIntensite(String intensite) {
        return exercices.stream()
                .filter(e -> {
                    if (intensite == null || intensite.equals("Toutes")) return true;
                    String i = e.getIntensite() != null ? e.getIntensite() : "Faible";
                    return i.equalsIgnoreCase(intensite);
                })
                .collect(Collectors.toList());
    }

    private List<Exercice> filtrerParCalories(String range) {
        return exercices.stream()
                .filter(e -> {
                    double c = e.getCaloriesParMinute();
                    return switch (range) {
                        case "< 5 cal/min"      -> c < 5;
                        case "5 \u2013 15 cal/min" -> c >= 5 && c <= 15;
                        case "> 15 cal/min"     -> c > 15;
                        default                 -> true;
                    };
                })
                .collect(Collectors.toList());
    }

    private int ordreIntensite(String intensite) {
        if (intensite == null) return 0;
        return switch (intensite) {
            case "Faible"  -> 1;
            case "Moyenne" -> 2;
            case "Élevée"  -> 3;
            default        -> 0;
        };
    }
}