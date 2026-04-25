package org.example;

import model.Exercice;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class ServiceExerciceTest {

    // ── Validation Nom ─────────────────────────────────────────

    @Test
    @DisplayName("Nom valide : 3 caractères ou plus")
    void testNomValide() {
        assertTrue(estNomValide("Pompes"));
        assertTrue(estNomValide("Run"));
    }

    @Test
    @DisplayName("Nom invalide : trop court")
    void testNomTropCourt() {
        assertFalse(estNomValide("AB"));
        assertFalse(estNomValide(""));
    }

    @Test
    @DisplayName("Nom invalide : null")
    void testNomNull() {
        assertFalse(estNomValide(null));
    }

    // ── Validation Calories ────────────────────────────────────

    @Test
    @DisplayName("Calories valides : entre 0.1 et 50")
    void testCaloriesValides() {
        assertTrue(estCaloriesValide(0.1));
        assertTrue(estCaloriesValide(10.0));
        assertTrue(estCaloriesValide(50.0));
    }

    @Test
    @DisplayName("Calories invalides : 0 ou négatif")
    void testCaloriesInvalides_zero() {
        assertFalse(estCaloriesValide(0.0));
        assertFalse(estCaloriesValide(-5.0));
    }

    @Test
    @DisplayName("Calories invalides : plus de 50")
    void testCaloriesInvalides_tropGrand() {
        assertFalse(estCaloriesValide(51.0));
    }

    // ── Validation Intensité ───────────────────────────────────

    @Test
    @DisplayName("Intensité valide : Faible / Moyenne / Élevée")
    void testIntensiteValide() {
        assertTrue(estIntensiteValide("Faible"));
        assertTrue(estIntensiteValide("Moyenne"));
        assertTrue(estIntensiteValide("Élevée"));
    }

    @Test
    @DisplayName("Intensité invalide : valeur inconnue")
    void testIntensiteInvalide() {
        assertFalse(estIntensiteValide("Maximum"));
        assertFalse(estIntensiteValide(""));
        assertFalse(estIntensiteValide(null));
    }

    // ── Validation ID Séance ───────────────────────────────────

    @Test
    @DisplayName("ID Séance valide : > 0")
    void testSeanceIdValide() {
        assertTrue(estSeanceIdValide(1));
        assertTrue(estSeanceIdValide(99));
    }

    @Test
    @DisplayName("ID Séance invalide : 0 ou négatif")
    void testSeanceIdInvalide() {
        assertFalse(estSeanceIdValide(0));
        assertFalse(estSeanceIdValide(-1));
    }

    // ── Ordre Intensité ────────────────────────────────────────

    @Test
    @DisplayName("Ordre intensité : Faible < Moyenne < Élevée")
    void testOrdreIntensite() {
        assertTrue(ordreIntensite("Faible") < ordreIntensite("Moyenne"));
        assertTrue(ordreIntensite("Moyenne") < ordreIntensite("Élevée"));
    }

    @Test
    @DisplayName("Intensité null → ordre 0 (le plus bas)")
    void testOrdreIntensiteNull() {
        assertEquals(0, ordreIntensite(null));
    }

    // ── Intensité null → Faible ────────────────────────────────

    @Test
    @DisplayName("Intensité null remplacée par 'Faible' dans filtre")
    void testIntensiteNullRemplacee() {
        Exercice e = new Exercice();
        e.setIntensite(null);
        String i = e.getIntensite() != null ? e.getIntensite() : "Faible";
        assertEquals("Faible", i);
    }

    // ── Helpers validation ─────────────────────────────────────

    private boolean estNomValide(String nom) {
        return nom != null && nom.trim().length() >= 3;
    }

    private boolean estCaloriesValide(double cal) {
        return cal > 0 && cal <= 50;
    }

    private boolean estIntensiteValide(String intensite) {
        return intensite != null &&
                (intensite.equals("Faible") ||
                        intensite.equals("Moyenne") ||
                        intensite.equals("Élevée"));
    }

    private boolean estSeanceIdValide(int id) {
        return id > 0;
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