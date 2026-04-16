package org.example;

import org.junit.jupiter.api.*;
import java.util.List;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

class SeanceSportTest {

    private List<SeanceSport> seances;

    @BeforeEach
    void setUp() {
        seances = List.of(
                creerSeance(1, "Cardio Matin",   45,  "2026-01-10", "Or"),
                creerSeance(2, "Football Soir",  90,  "2026-02-15", "Argent"),
                creerSeance(3, "Yoga Doux",      25,  "2026-03-01", "Aucune"),
                creerSeance(4, "Cardio Intense", 120, "2026-04-05", null),
                creerSeance(5, "Natation",       65,  "2026-04-10", "Bronze")
        );
    }

    // ── Recherche par nom ──────────────────────────────────────

    @Test
    @DisplayName("Recherche 'cardio' → 2 résultats")
    void testRechercheParNom_trouve() {
        assertEquals(2, filtrerParNom("cardio").size());
    }

    @Test
    @DisplayName("Recherche insensible à la casse")
    void testRechercheInsensibleCasse() {
        assertEquals(
                filtrerParNom("cardio").size(),
                filtrerParNom("CARDIO").size()
        );
    }

    @Test
    @DisplayName("Recherche 'basketball' → 0 résultat")
    void testRechercheParNom_aucunResultat() {
        assertTrue(filtrerParNom("basketball").isEmpty());
    }

    @Test
    @DisplayName("Recherche vide → retourne tout")
    void testRechercheVide_retourneTout() {
        assertEquals(5, filtrerParNom("").size());
    }

    // ── Filtre Médaille ────────────────────────────────────────

    @Test
    @DisplayName("Filtre 'Or' → 1 séance")
    void testFiltreMedaille_Or() {
        List<SeanceSport> resultat = filtrerParMedaille("Or");
        assertEquals(1, resultat.size());
        assertEquals("Cardio Matin", resultat.get(0).getNomSeance());
    }

    @Test
    @DisplayName("Filtre 'Argent' → 1 séance")
    void testFiltreMedaille_Argent() {
        assertEquals(1, filtrerParMedaille("Argent").size());
    }

    @Test
    @DisplayName("Filtre 'Bronze' → 1 séance")
    void testFiltreMedaille_Bronze() {
        assertEquals(1, filtrerParMedaille("Bronze").size());
    }

    @Test
    @DisplayName("Filtre 'Aucune' : null traité comme Aucune → 2 séances")
    void testFiltreMedaille_NullTraiteCommeAucune() {
        assertEquals(2, filtrerParMedaille("Aucune").size());
    }

    @Test
    @DisplayName("Filtre 'Toutes' → retourne tout")
    void testFiltreMedaille_Toutes() {
        assertEquals(5, filtrerParMedaille("Toutes").size());
    }

    // ── Filtre Durée ───────────────────────────────────────────

    @Test
    @DisplayName("Durée < 30 min → 1 séance (Yoga Doux 25min)")
    void testFiltreDuree_MoinsDe30() {
        List<SeanceSport> r = filtrerParDuree("< 30 min");
        assertEquals(1, r.size());
        assertEquals("Yoga Doux", r.get(0).getNomSeance());
    }

    @Test
    @DisplayName("Durée 30-60 min → 1 séance (Cardio Matin 45min)")
    void testFiltreDuree_30a60() {
        List<SeanceSport> r = filtrerParDuree("30 – 60 min");
        assertEquals(1, r.size());
        assertEquals("Cardio Matin", r.get(0).getNomSeance());
    }

    @Test
    @DisplayName("Durée 61-120 min → 3 séances")
    void testFiltreDuree_61a120() {
        assertEquals(3, filtrerParDuree("61 – 120 min").size());
    }

    @Test
    @DisplayName("Durée > 120 min → 0 séance")
    void testFiltreDuree_PlusDe120() {
        assertEquals(0, filtrerParDuree("> 120 min").size());
    }

    // ── Tri ───────────────────────────────────────────────────

    @Test
    @DisplayName("Tri nom A→Z : premier = Cardio Intense")
    void testTriNomAZ() {
        List<SeanceSport> tries = seances.stream()
                .sorted((a, b) -> a.getNomSeance().compareToIgnoreCase(b.getNomSeance()))
                .collect(Collectors.toList());
        assertEquals("Cardio Intense", tries.get(0).getNomSeance());
    }

    @Test
    @DisplayName("Tri nom Z→A : premier = Yoga Doux")
    void testTriNomZA() {
        List<SeanceSport> tries = seances.stream()
                .sorted((a, b) -> b.getNomSeance().compareToIgnoreCase(a.getNomSeance()))
                .collect(Collectors.toList());
        assertEquals("Yoga Doux", tries.get(0).getNomSeance());
    }

    @Test
    @DisplayName("Tri durée croissant : premier = 25 min")
    void testTriDureeAscendant() {
        List<SeanceSport> tries = seances.stream()
                .sorted((a, b) -> Integer.compare(a.getDureeMinutes(), b.getDureeMinutes()))
                .collect(Collectors.toList());
        assertEquals(25, tries.get(0).getDureeMinutes());
    }

    @Test
    @DisplayName("Tri durée décroissant : premier = 120 min")
    void testTriDureeDescendant() {
        List<SeanceSport> tries = seances.stream()
                .sorted((a, b) -> Integer.compare(b.getDureeMinutes(), a.getDureeMinutes()))
                .collect(Collectors.toList());
        assertEquals(120, tries.get(0).getDureeMinutes());
    }

    @Test
    @DisplayName("Tri date récente : premier = 2026-04-10")
    void testTriDateRecente() {
        List<SeanceSport> tries = seances.stream()
                .sorted((a, b) -> b.getDateSeance().compareTo(a.getDateSeance()))
                .collect(Collectors.toList());
        assertEquals("2026-04-10", tries.get(0).getDateSeance());
    }

    // ── Combinaison ────────────────────────────────────────────

    @Test
    @DisplayName("Combinaison : nom 'cardio' + médaille 'Or' → 1 résultat")
    void testCombinaisonNomEtMedaille() {
        List<SeanceSport> resultat = seances.stream()
                .filter(s -> s.getNomSeance().toLowerCase().contains("cardio"))
                .filter(s -> {
                    String med = s.getMedailleObtenue() != null
                            ? s.getMedailleObtenue() : "Aucune";
                    return med.equalsIgnoreCase("Or");
                })
                .collect(Collectors.toList());
        assertEquals(1, resultat.size());
        assertEquals("Cardio Matin", resultat.get(0).getNomSeance());
    }

    @Test
    @DisplayName("Combinaison : durée 61-120min + médaille 'Argent' → 1 résultat")
    void testCombinaisonDureeEtMedaille() {
        List<SeanceSport> resultat = seances.stream()
                .filter(s -> s.getDureeMinutes() >= 61 && s.getDureeMinutes() <= 120)
                .filter(s -> {
                    String med = s.getMedailleObtenue() != null
                            ? s.getMedailleObtenue() : "Aucune";
                    return med.equalsIgnoreCase("Argent");
                })
                .collect(Collectors.toList());
        assertEquals(1, resultat.size());
        assertEquals("Football Soir", resultat.get(0).getNomSeance());
    }

    // ── Helpers ───────────────────────────────────────────────

    private SeanceSport creerSeance(int id, String nom, int duree,
                                    String date, String medaille) {
        SeanceSport s = new SeanceSport();
        s.setId(id);
        s.setNomSeance(nom);
        s.setDureeMinutes(duree);
        s.setDateSeance(date);
        s.setMedailleObtenue(medaille);
        s.setHeureDebut("08:00:00");
        return s;
    }

    private List<SeanceSport> filtrerParNom(String recherche) {
        return seances.stream()
                .filter(s -> recherche.isEmpty() ||
                        (s.getNomSeance() != null &&
                                s.getNomSeance().toLowerCase().contains(recherche.toLowerCase())))
                .collect(Collectors.toList());
    }

    private List<SeanceSport> filtrerParMedaille(String medaille) {
        return seances.stream()
                .filter(s -> {
                    if (medaille == null || medaille.equals("Toutes")) return true;
                    String med = s.getMedailleObtenue() != null
                            ? s.getMedailleObtenue() : "Aucune";
                    return med.equalsIgnoreCase(medaille);
                })
                .collect(Collectors.toList());
    }

    private List<SeanceSport> filtrerParDuree(String range) {
        return seances.stream()
                .filter(s -> {
                    int d = s.getDureeMinutes();
                    return switch (range) {
                        case "< 30 min"     -> d < 30;
                        case "30 – 60 min"  -> d >= 30 && d <= 60;
                        case "61 – 120 min" -> d >= 61 && d <= 120;
                        case "> 120 min"    -> d > 120;
                        default             -> true;
                    };
                })
                .collect(Collectors.toList());
    }
}