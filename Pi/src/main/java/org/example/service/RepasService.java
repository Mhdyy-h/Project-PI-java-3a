package org.example.service;

import org.example.model.Repas;
import org.example.model.ChronoScore;
import org.example.dao.RepasDAO;

import java.util.List;

public class RepasService {

    /**
     * Calcule le score intelligent combinant timing et nutrition
     */
    public static int calculerScoreIntelligent(Repas repas) {
        ChronoScore chronoScore = Chronoscoreservice.calculerChronoScore(repas);
        return chronoScore.getTotalScore();
    }

    /**
     * Met à jour les points d'un repas
     */
    public static void mettreAJourPointsRepas(Repas repas) {
        int points = calculerScoreIntelligent(repas);
        repas.setPointsGagnes(points);
        RepasDAO.update(repas);
    }

    /**
     * Obtient les détails du calcul de score
     */
    public static ChronoScore getDetailsScore(Repas repas) {
        return Chronoscoreservice.calculerChronoScore(repas);
    }

    /**
     * Grille de points par moment de journée
     */
    public static int getPointsParMoment(String typeMoment, int heure) {
        switch (typeMoment) {
            case "MATIN":
                if (heure >= 6 && heure <= 9) return 10;
                if (heure >= 5 && heure <= 10) return 5;
                return 0;
            case "MIDI":
                if (heure >= 12 && heure <= 13) return 10;
                if (heure >= 11 && heure <= 14) return 5;
                return 0;
            case "COLLATION":
                if ((heure >= 9 && heure <= 11) || (heure >= 15 && heure <= 17)) return 8;
                return 0;
            case "SOIR":
                if (heure >= 18 && heure <= 20) return 10;
                if (heure >= 17 && heure <= 21) return 5;
                return 0;
            default:
                return 0;
        }
    }

    /**
     * Récupère les repas du jour avec leurs scores
     */
    public static List<Repas> getRepasDuJourAvecScores(int utilisateurId) {
        List<Repas> repasList = RepasDAO.getTodayRepas(utilisateurId);
        for (Repas repas : repasList) {
            ChronoScore score = getDetailsScore(repas);
            repas.setPointsGagnes(score.getTotalScore());
        }
        return repasList;
    }

    /**
     * Calcule les calories totales du jour
     */
    public static int getCaloriesTotalesJour(int utilisateurId) {
        List<Repas> repasList = RepasDAO.getTodayRepas(utilisateurId);
        return repasList.stream().mapToInt(Repas::getTotalCalories).sum();
    }

    /**
     * Vérifie si le repas est valide (non vide, titre présent)
     */
    public static boolean isRepasValide(Repas repas) {
        if (repas.getTitreRepas() == null || repas.getTitreRepas().trim().isEmpty()) {
            return false;
        }
        if (repas.getTitreRepas().length() < 3 || repas.getTitreRepas().length() > 100) {
            return false;
        }
        if (repas.getAliments().isEmpty()) {
            return false;
        }
        return true;
    }
}