package org.example.service;

import org.example.dao.DatabaseConnection;
import org.example.model.IndiceFormePhysique;
import org.example.model.IndiceFormePhysique.NiveauForme;
import org.example.model.SeanceSport;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class IndiceFormeService {

    private Connection conn;

    public IndiceFormeService() {
        this.conn = DatabaseConnection.getConnection();
    }

    // ─────────────────────────────────────────────────────────────
    //  CALCUL PRINCIPAL — Score 0-100
    // ─────────────────────────────────────────────────────────────
    public IndiceFormePhysique calculerIndice(int userId) {
        try {
            List<SeanceSport> seances = getSeancesByUser(userId);

            // ── Données brutes ────────────────────────────────────
            int    total        = seances.size();
            int    seances7j    = compterSeancesDerniersJours(seances, 7);
            int    seances30j   = compterSeancesDerniersJours(seances, 30);
            double dureeMoy     = calculerDureeMoyenne(seances);
            int    joursRepos   = calculerJoursRepos(seances);
            int    serie        = calculerSerie(seances);

            // ── 4 critères × 25 pts chacun = 100 pts max ─────────
            int scoreReg  = calculerScoreRegularite(seances7j, seances30j);
            int scoreInt  = calculerScoreIntensite(dureeMoy, seances7j);
            int scoreCons = calculerScoreConsistance(total, serie);
            int scoreRec  = calculerScoreRecuperation(joursRepos, seances7j);

            String conseil = genererConseil(
                    scoreReg, scoreInt, scoreCons, scoreRec,
                    seances7j, joursRepos, dureeMoy, serie);

            return new IndiceFormePhysique(
                    scoreReg, scoreInt, scoreCons, scoreRec,
                    total, seances7j, dureeMoy, joursRepos, serie, conseil);

        } catch (SQLException e) {
            System.err.println("Erreur IndiceFormeService : " + e.getMessage());
            return new IndiceFormePhysique(0,0,0,0,0,0,0,0,0,
                    "Impossible de charger les données.");
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  CRITÈRE 1 — RÉGULARITÉ (0-25 pts)
    //  Basé sur : fréquence hebdomadaire et mensuelle
    // ─────────────────────────────────────────────────────────────
    private int calculerScoreRegularite(int seances7j, int seances30j) {
        int score = 0;

        // Fréquence hebdomadaire idéale = 3 séances
        if      (seances7j >= 4) score += 15;
        else if (seances7j == 3) score += 25; // parfait
        else if (seances7j == 2) score += 18;
        else if (seances7j == 1) score += 8;
        else                     score += 0;  // aucune séance

        // Bonus mensuel
        if      (seances30j >= 12) score = Math.min(25, score + 5);
        else if (seances30j >= 8)  score = Math.min(25, score + 3);

        return Math.min(25, score);
    }

    // ─────────────────────────────────────────────────────────────
    //  CRITÈRE 2 — INTENSITÉ (0-25 pts)
    //  Basé sur : durée moyenne des séances
    // ─────────────────────────────────────────────────────────────
    private int calculerScoreIntensite(double dureeMoy, int seances7j) {
        int score = 0;

        // Durée idéale : 30-60 min
        if      (dureeMoy >= 45 && dureeMoy <= 75) score += 25; // parfait
        else if (dureeMoy >= 30 && dureeMoy <  45) score += 18;
        else if (dureeMoy >  75 && dureeMoy <= 90) score += 15; // trop long
        else if (dureeMoy >  90)                   score += 8;  // surcharge
        else if (dureeMoy >  0)                    score += 5;  // trop court
        else                                       score += 0;

        return Math.min(25, score);
    }

    // ─────────────────────────────────────────────────────────────
    //  CRITÈRE 3 — CONSISTANCE (0-25 pts)
    //  Basé sur : total séances + série consécutive
    // ─────────────────────────────────────────────────────────────
    private int calculerScoreConsistance(int total, int serie) {
        int score = 0;

        // Total des séances
        if      (total >= 50) score += 15;
        else if (total >= 20) score += 12;
        else if (total >= 10) score += 8;
        else if (total >= 5)  score += 5;
        else if (total >= 1)  score += 2;

        // Bonus série consécutive
        if      (serie >= 7)  score += 10;
        else if (serie >= 5)  score += 7;
        else if (serie >= 3)  score += 5;
        else if (serie >= 1)  score += 2;

        return Math.min(25, score);
    }

    // ─────────────────────────────────────────────────────────────
    //  CRITÈRE 4 — RÉCUPÉRATION (0-25 pts)
    //  Basé sur : jours de repos entre séances
    // ─────────────────────────────────────────────────────────────
    private int calculerScoreRecuperation(int joursRepos, int seances7j) {
        int score = 0;

        // Repos idéal : 1-2 jours
        if      (joursRepos == 1 || joursRepos == 2) score += 25; // parfait
        else if (joursRepos == 0 && seances7j <= 3)  score += 20;
        else if (joursRepos == 0 && seances7j > 3)   score += 5;  // surcharge
        else if (joursRepos == 3)                    score += 18;
        else if (joursRepos <= 7)                    score += 10;
        else                                         score += 2;  // trop de repos

        return Math.min(25, score);
    }

    // ─────────────────────────────────────────────────────────────
    //  CONSEIL INTELLIGENT — identifie le point faible
    // ─────────────────────────────────────────────────────────────
    private String genererConseil(int scoreReg, int scoreInt,
                                  int scoreCons, int scoreRec,
                                  int seances7j, int joursRepos,
                                  double dureeMoy, int serie) {
        // Trouve le critère le plus faible
        int minScore = Math.min(Math.min(scoreReg, scoreInt),
                Math.min(scoreCons, scoreRec));

        if (minScore == scoreReg) {
            if (seances7j == 0)
                return "🎯 Priorité : recommencez à vous entraîner ! "
                        + "Visez 3 séances cette semaine.";
            return "🎯 Améliorez votre régularité : visez 3 séances/semaine "
                    + "pour maximiser votre score.";
        }
        if (minScore == scoreInt) {
            if (dureeMoy > 75)
                return "⏱ Vos séances sont trop longues. "
                        + "Visez 45-60 min pour une intensité optimale.";
            return "⏱ Augmentez la durée de vos séances à 45-60 min "
                    + "pour améliorer votre score d'intensité.";
        }
        if (minScore == scoreCons) {
            return "📅 Votre consistance peut s'améliorer. "
                    + "Maintenez une série d'au moins 3 jours consécutifs.";
        }
        if (minScore == scoreRec) {
            if (joursRepos == 0)
                return "😴 Prenez 1-2 jours de repos entre vos séances "
                        + "pour optimiser la récupération musculaire.";
            return "😴 Trop de repos ! Reprenez l'entraînement "
                    + "pour maintenir votre forme.";
        }
        return "🏆 Votre forme est excellente ! Continuez sur cette lancée !";
    }

    // ─────────────────────────────────────────────────────────────
    //  MÉTHODES UTILITAIRES
    // ─────────────────────────────────────────────────────────────
    private List<SeanceSport> getSeancesByUser(int userId) throws SQLException {
        List<SeanceSport> liste = new ArrayList<>();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM seance_sport WHERE utilisateur_id = ?");
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            liste.add(new SeanceSport(
                    rs.getInt("id"), rs.getString("nom_seance"),
                    rs.getString("heure_debut"), rs.getInt("duree_minutes"),
                    rs.getString("medaille_obtenue"), rs.getString("date_seance"),
                    rs.getInt("utilisateur_id"), rs.getString("heure_debut_reelle"),
                    rs.getInt("alerte_envoyee")));
        }
        rs.close(); ps.close();
        return liste;
    }

    private int compterSeancesDerniersJours(List<SeanceSport> s, int jours) {
        LocalDate limite = LocalDate.now().minusDays(jours);
        return (int) s.stream().filter(ss -> {
            try { return LocalDate.parse(ss.dateSeance).isAfter(limite); }
            catch (Exception e) { return false; }
        }).count();
    }

    private double calculerDureeMoyenne(List<SeanceSport> seances) {
        return seances.stream()
                .mapToInt(s -> s.dureeMinutes)
                .average().orElse(0.0);
    }

    private int calculerJoursRepos(List<SeanceSport> seances) {
        return seances.stream()
                .map(s -> { try { return LocalDate.parse(s.dateSeance); }
                catch (Exception e) { return LocalDate.MIN; } })
                .max(LocalDate::compareTo)
                .map(d -> (int) ChronoUnit.DAYS.between(d, LocalDate.now()))
                .orElse(999);
    }

    private int calculerSerie(List<SeanceSport> seances) {
        if (seances == null || seances.isEmpty()) return 0;
        List<LocalDate> dates = new ArrayList<>();
        for (SeanceSport s : seances) {
            try { dates.add(LocalDate.parse(s.dateSeance)); }
            catch (Exception ignored) {}
        }
        if (dates.isEmpty()) return 0;
        dates.sort((a, b) -> b.compareTo(a));
        int serie = 1;
        for (int i = 0; i < dates.size() - 1; i++) {
            long diff = ChronoUnit.DAYS.between(dates.get(i+1), dates.get(i));
            if (diff == 1) serie++;
            else break;
        }
        return serie;
    }
}