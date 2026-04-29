package org.example.service;

import org.example.model.Repas;
import org.example.model.Aliment;
import org.example.model.Alerte;
import org.example.dao.AlerteDAO;

import java.util.List;

public class AlertService {

    // ── Seuils ───────────────────────────────────────────
    private static final int SEUIL_ALERTES_HEBDOMADAIRES = 2;
    private static final int HEURE_EXCITANT_TARDIF       = 16;
    private static final int HEURE_DINER_TRES_TARDIF     = 22;
    private static final int CALORIES_EXCESSIVES         = 1200;
    private static final int SEUIL_SCORE_FAIBLE          = 4;

    // ═════════════════════════════════════════════════════
    //  POINT D'ENTRÉE PRINCIPAL
    // ═════════════════════════════════════════════════════

    public static void verifierRepas(Repas repas) {
        detecterExcitantsTardifs(repas);
        detecterCaloriesExcessives(repas);
        detecterDinerTresTardif(repas);
        detecterScoreFaible(repas);
        verifierTendanceAlertes(repas.getUtilisateurId());
    }

    // ═════════════════════════════════════════════════════
    //  ALERTE 1 : EXCITANTS TARDIFS (après 16h) 🟡/🔴
    // ═════════════════════════════════════════════════════

    public static void detecterExcitantsTardifs(Repas repas) {
        int heure = repas.getDateConsommation().getHour();
        if (heure < HEURE_EXCITANT_TARDIF) return;

        List<Aliment> excitants = repas.getAliments().stream()
                .filter(Aliment::isEstExcitant)
                .toList();
        if (excitants.isEmpty()) return;

        if (AlerteDAO.existsForRepas(repas.getId(), "EXCITANT_TARDIF")) return;

        String nomsExcitants = excitants.stream()
                .map(Aliment::getNomAliment)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        String message = String.format(
                "Consommation d'aliments excitants apres %dh : %s a %s. "
                        + "Risque : perturbation du sommeil.",
                HEURE_EXCITANT_TARDIF,
                nomsExcitants,
                repas.getHeureFormatee()
        );

        int alertesRecentes = AlerteDAO.countRecentByType(
                repas.getUtilisateurId(), "EXCITANT_TARDIF", 7);
        String criticite = alertesRecentes >= SEUIL_ALERTES_HEBDOMADAIRES - 1
                ? "ROUGE" : "JAUNE";

        creerEtSauvegarderAlerte(
                "EXCITANT_TARDIF", message, criticite,
                repas.getUtilisateurId(), repas.getId());

        // ── SMS Twilio ──
        String nomPatient = getNomPatient(repas.getUtilisateurId());
        TwilioService.alerteAlimentExcitantApres16h(
                nomPatient, nomsExcitants, repas.getHeureFormatee());
    }

    // ═════════════════════════════════════════════════════
    //  ALERTE 2 : CALORIES EXCESSIVES (> 1200) 🔴
    // ═════════════════════════════════════════════════════

    public static void detecterCaloriesExcessives(Repas repas) {
        if (repas.getTotalCalories() <= CALORIES_EXCESSIVES) return;
        if (AlerteDAO.existsForRepas(repas.getId(), "CALORIES_EXCESSIVES")) return;

        String message = String.format(
                "Repas tres calorique : %d cal (seuil : %d cal). "
                        + "Risque de surcharge metabolique.",
                repas.getTotalCalories(),
                CALORIES_EXCESSIVES
        );

        creerEtSauvegarderAlerte(
                "CALORIES_EXCESSIVES", message, "ROUGE",
                repas.getUtilisateurId(), repas.getId());

        // ── SMS Twilio ──
        String nomPatient = getNomPatient(repas.getUtilisateurId());
        TwilioService.alerteCaloriesElevees(nomPatient, repas.getTotalCalories());
    }

    // ═════════════════════════════════════════════════════
    //  ALERTE 3 : DÎNER TRÈS TARDIF (après 22h) 🔴
    // ═════════════════════════════════════════════════════

    public static void detecterDinerTresTardif(Repas repas) {
        int heure = repas.getDateConsommation().getHour();
        if (heure < HEURE_DINER_TRES_TARDIF) return;
        if (!repas.getTypeMoment().equals("SOIR")) return;
        if (AlerteDAO.existsForRepas(repas.getId(), "DINER_TARDIF")) return;

        String message = String.format(
                "Diner tres tardif a %s. "
                        + "Manger apres 22h perturbe le sommeil.",
                repas.getHeureFormatee()
        );

        int alertesRecentes = AlerteDAO.countRecentByType(
                repas.getUtilisateurId(), "DINER_TARDIF", 7);
        String criticite = alertesRecentes >= 1 ? "ROUGE" : "JAUNE";

        creerEtSauvegarderAlerte(
                "DINER_TARDIF", message, criticite,
                repas.getUtilisateurId(), repas.getId());

        // ── SMS Twilio ──
        String nomPatient = getNomPatient(repas.getUtilisateurId());
        TwilioService.alerteRepasSoirApres22h(nomPatient, repas.getHeureFormatee());
    }

    // ═════════════════════════════════════════════════════
    //  ALERTE 4 : SCORE NUTRITIONNEL FAIBLE (< 4/14) 🟡
    // ═════════════════════════════════════════════════════

    public static void detecterScoreFaible(Repas repas) {
        int score = RepasService.calculerScoreIntelligent(repas);
        if (score >= SEUIL_SCORE_FAIBLE) return;
        if (AlerteDAO.existsForRepas(repas.getId(), "SCORE_FAIBLE")) return;

        String message = String.format(
                "Score nutritionnel tres bas : %d/14. "
                        + "Ce repas est desequilibre ou mal time.",
                score
        );

        creerEtSauvegarderAlerte(
                "SCORE_FAIBLE", message, "JAUNE",
                repas.getUtilisateurId(), repas.getId());

        // ── SMS Twilio ──
        String nomPatient = getNomPatient(repas.getUtilisateurId());
        TwilioService.alerteScoreFaible(nomPatient, score);
    }

    // ═════════════════════════════════════════════════════
    //  ALERTE 5 : TENDANCE CRITIQUE HEBDOMADAIRE 🔴
    // ═════════════════════════════════════════════════════

    public static void verifierTendanceAlertes(int utilisateurId) {
        String tendance = analyserTendanceAlertes(utilisateurId);
        if (!tendance.equals("CRITIQUE")) return;
        if (AlerteDAO.countRecentByType(utilisateurId, "TENDANCE_CRITIQUE", 7) > 0)
            return;

        String message = "Tendance critique : excitants tardifs consommes "
                + "plusieurs fois cette semaine. "
                + "Conseil : remplacez cafe/the du soir par une tisane.";

        creerEtSauvegarderAlerte(
                "TENDANCE_CRITIQUE", message, "ROUGE",
                utilisateurId, null);

        // ── SMS Twilio ──
        String nomPatient = getNomPatient(utilisateurId);
        TwilioService.envoyerSMSAsync(
                "[ALERTE ROUGE] NutriCoach\n"
                        + "Patient : " + nomPatient + "\n"
                        + "Tendance critique detectee cette semaine.\n"
                        + "Consommation repetee d'excitants apres 16h.\n"
                        + "Intervention recommandee.");
    }

    // ═════════════════════════════════════════════════════
    //  ANALYSE DE TENDANCE
    // ═════════════════════════════════════════════════════

    public static String analyserTendanceAlertes(int utilisateurId) {
        int alertesRecentes = AlerteDAO.countRecentByType(
                utilisateurId, "EXCITANT_TARDIF", 7);
        if (alertesRecentes >= SEUIL_ALERTES_HEBDOMADAIRES) return "CRITIQUE";
        if (alertesRecentes >= 1)                           return "ATTENTION";
        return "NORMAL";
    }

    public static List<Alerte> getAlertesActives(int utilisateurId) {
        return AlerteDAO.getRecentByUtilisateurId(utilisateurId);
    }

    public static long countAlertesRouge(int utilisateurId) {
        return getAlertesActives(utilisateurId).stream()
                .filter(a -> "ROUGE".equals(a.getCriticite()))
                .count();
    }

    // ═════════════════════════════════════════════════════
    //  HELPERS PRIVÉS
    // ═════════════════════════════════════════════════════

    private static void creerEtSauvegarderAlerte(
            String type, String message, String criticite,
            int utilisateurId, Integer repasId) {

        Alerte alerte = new Alerte(type, message, criticite, utilisateurId, repasId);
        AlerteDAO.insert(alerte);

        System.out.println("ALERTE [" + criticite + "] " + type
                + " — utilisateur " + utilisateurId + " : " + message);
    }

    /**
     * Récupère le nom du patient depuis son ID.
     * Adapte selon ta DAO (UtilisateurDAO, PatientDAO, etc.)
     */
    private static String getNomPatient(int utilisateurId) {
        try {
            // Adapte cette ligne selon le nom de ta DAO et méthode :
            // return UtilisateurDAO.getNomById(utilisateurId);
            // return PatientDAO.findById(utilisateurId).getNom();
            return "Patient #" + utilisateurId; // fallback si pas de DAO
        } catch (Exception e) {
            return "Patient #" + utilisateurId;
        }
    }
}