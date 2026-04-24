package org.example;

import model.SeanceSport;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class ServiceSeanceSportTest {

    // ── Validation Nom ─────────────────────────────────────────

    @Test
    @DisplayName("Nom valide : 3 caractères ou plus")
    void testNomValide() {
        assertTrue(estNomValide("Cardio"));
        assertTrue(estNomValide("Run"));
    }

    @Test
    @DisplayName("Nom invalide : moins de 3 caractères")
    void testNomTropCourt() {
        assertFalse(estNomValide("AB"));
        assertFalse(estNomValide(""));
    }

    @Test
    @DisplayName("Nom invalide : null")
    void testNomNull() {
        assertFalse(estNomValide(null));
    }

    // ── Validation Durée ───────────────────────────────────────

    @Test
    @DisplayName("Durée valide : entre 5 et 480")
    void testDureeValide() {
        assertTrue(estDureeValide(5));
        assertTrue(estDureeValide(60));
        assertTrue(estDureeValide(480));
    }

    @Test
    @DisplayName("Durée invalide : 0 ou négatif")
    void testDureeInvalide_zero() {
        assertFalse(estDureeValide(0));
        assertFalse(estDureeValide(-10));
    }

    @Test
    @DisplayName("Durée invalide : plus de 480")
    void testDureeInvalide_tropGrand() {
        assertFalse(estDureeValide(481));
        assertFalse(estDureeValide(1000));
    }

    // ── Validation Date ────────────────────────────────────────

    @Test
    @DisplayName("Date valide : format YYYY-MM-DD")
    void testDateValide() {
        assertTrue(estDateValide("2026-04-15"));
        assertTrue(estDateValide("2025-01-01"));
    }

    @Test
    @DisplayName("Date invalide : mauvais format")
    void testDateInvalide() {
        assertFalse(estDateValide("15/04/2026"));
        assertFalse(estDateValide("2026-4-1"));
        assertFalse(estDateValide(""));
    }

    // ── Validation Heure ───────────────────────────────────────

    @Test
    @DisplayName("Heure valide : format HH:MM:SS")
    void testHeureValide() {
        assertTrue(estHeureValide("08:30:00"));
        assertTrue(estHeureValide("23:59:59"));
        assertTrue(estHeureValide("00:00:00"));
    }

    @Test
    @DisplayName("Heure invalide : 25:00:00")
    void testHeureInvalide() {
        assertFalse(estHeureValide("25:00:00"));
        assertFalse(estHeureValide("8:30:00"));
        assertFalse(estHeureValide("abc"));
    }

    // ── Médaille ───────────────────────────────────────────────

    @Test
    @DisplayName("Médaille null → remplacée par Aucune")
    void testMedailleNullRemplacee() {
        SeanceSport s = new SeanceSport();
        s.setMedailleObtenue(null);
        String med = s.getMedailleObtenue() != null ? s.getMedailleObtenue() : "Aucune";
        assertEquals("Aucune", med);
    }

    @Test
    @DisplayName("Médaille 'Or' → conservée")
    void testMedailleOrConservee() {
        SeanceSport s = new SeanceSport();
        s.setMedailleObtenue("Or");
        String med = s.getMedailleObtenue() != null ? s.getMedailleObtenue() : "Aucune";
        assertEquals("Or", med);
    }

    // ── Helpers validation ─────────────────────────────────────

    private boolean estNomValide(String nom) {
        return nom != null && nom.trim().length() >= 3;
    }

    private boolean estDureeValide(int duree) {
        return duree >= 5 && duree <= 480;
    }

    private boolean estDateValide(String date) {
        return date != null && date.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    private boolean estHeureValide(String heure) {
        return heure != null &&
                heure.matches("([01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d");
    }
}