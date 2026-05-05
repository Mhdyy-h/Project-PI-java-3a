package org.example.service;

import org.example.dao.DatabaseConnection;
import org.example.model.AlerteSurcharge;
import org.example.model.AlerteSurcharge.TypeAlerte;
import org.example.model.SeanceSport;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class SurchargeService {

    private Connection conn;

    public SurchargeService() {
        this.conn = DatabaseConnection.getConnection();
    }

    // ─────────────────────────────────────────────────────────────
    //  ANALYSE PRINCIPALE
    // ─────────────────────────────────────────────────────────────
    public AlerteSurcharge analyser(int userId) {
        try {
            List<SeanceSport> toutesSeances = getSeancesByUser(userId);

            int    seances7Jours  = compterSeancesDerniersjours(toutesSeances, 7);
            int    seances30Jours = compterSeancesDerniersjours(toutesSeances, 30);
            double dureeMoyenne   = calculerDureeMoyenne(toutesSeances);
            int    joursRepos     = calculerJoursRepos(toutesSeances);

            TypeAlerte type    = determinerAlerte(seances7Jours, dureeMoyenne, joursRepos);
            String     conseil = genererConseil(type, seances7Jours, joursRepos, dureeMoyenne);

            return new AlerteSurcharge(type, seances7Jours, seances30Jours,
                    dureeMoyenne, joursRepos, conseil);

        } catch (SQLException e) {
            System.err.println("Erreur SurchargeService : " + e.getMessage());
            return new AlerteSurcharge(TypeAlerte.OPTIMAL, 0, 0, 0, 0,
                    "Impossible de charger les données.");
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  RÉCUPÉRER LES SÉANCES
    // ─────────────────────────────────────────────────────────────
    private List<SeanceSport> getSeancesByUser(int userId) throws SQLException {
        List<SeanceSport> liste = new ArrayList<>();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM seance_sport WHERE utilisateur_id = ?"
        );
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            liste.add(new SeanceSport(
                    rs.getInt("id"),
                    rs.getString("nom_seance"),
                    rs.getString("heure_debut"),
                    rs.getInt("duree_minutes"),
                    rs.getString("medaille_obtenue"),
                    rs.getString("date_seance"),
                    rs.getInt("utilisateur_id"),
                    rs.getString("heure_debut_reelle"),
                    rs.getInt("alerte_envoyee")
            ));
        }
        rs.close();
        ps.close();
        return liste;
    }

    // ─────────────────────────────────────────────────────────────
    //  COMPTER SÉANCES SUR N DERNIERS JOURS
    // ─────────────────────────────────────────────────────────────
    private int compterSeancesDerniersjours(List<SeanceSport> seances, int jours) {
        LocalDate limite = LocalDate.now().minusDays(jours);
        return (int) seances.stream()
                .filter(s -> {
                    try {
                        return LocalDate.parse(s.dateSeance).isAfter(limite);
                    } catch (Exception e) { return false; }
                })
                .count();
    }

    // ─────────────────────────────────────────────────────────────
    //  DURÉE MOYENNE DES SÉANCES (30 derniers jours)
    // ─────────────────────────────────────────────────────────────
    private double calculerDureeMoyenne(List<SeanceSport> seances) {
        LocalDate limite = LocalDate.now().minusDays(30);
        return seances.stream()
                .filter(s -> {
                    try {
                        return LocalDate.parse(s.dateSeance).isAfter(limite);
                    } catch (Exception e) { return false; }
                })
                .mapToInt(s -> s.dureeMinutes)
                .average()
                .orElse(0.0);
    }

    // ─────────────────────────────────────────────────────────────
    //  JOURS DE REPOS depuis la dernière séance
    // ─────────────────────────────────────────────────────────────
    private int calculerJoursRepos(List<SeanceSport> seances) {
        return seances.stream()
                .map(s -> {
                    try { return LocalDate.parse(s.dateSeance); }
                    catch (Exception e) { return LocalDate.MIN; }
                })
                .max(LocalDate::compareTo)
                .map(d -> (int) ChronoUnit.DAYS.between(d, LocalDate.now()))
                .orElse(999);
    }

    // ─────────────────────────────────────────────────────────────
    //  ALGORITHME DE DÉTECTION — règles métier
    // ─────────────────────────────────────────────────────────────
    private TypeAlerte determinerAlerte(int seances7j, double dureeMoy, int joursRepos) {

        // 🔴 CRITIQUE — trop de séances + longues + pas de repos
        if (seances7j >= 6 && dureeMoy >= 60 && joursRepos == 0) {
            return TypeAlerte.CRITIQUE;
        }

        // 🟠 ATTENTION — rythme intense
        if (seances7j >= 5 || (seances7j >= 4 && dureeMoy >= 60)) {
            return TypeAlerte.ATTENTION;
        }

        // 🟡 REPOS CONSEILLÉ — pas assez de récupération
        if (seances7j >= 4 && joursRepos == 0) {
            return TypeAlerte.CONSEILLE;
        }

        // 🔵 INSUFFISANT — trop peu d'activité
        if (seances7j == 0) {
            return TypeAlerte.INSUFFISANT;
        }

        // 🟢 OPTIMAL — rythme parfait (1-3 séances/semaine)
        return TypeAlerte.OPTIMAL;
    }

    // ─────────────────────────────────────────────────────────────
    //  GÉNÉRATION DU CONSEIL PERSONNALISÉ
    // ─────────────────────────────────────────────────────────────
    private String genererConseil(TypeAlerte type, int seances7j,
                                  int joursRepos, double dureeMoy) {
        switch (type) {
            case CRITIQUE:
                return String.format(
                        "Vous avez fait %d séances cette semaine avec une durée " +
                                "moyenne de %.0f min. Prenez au moins 2 jours de repos " +
                                "complets pour éviter les blessures.", seances7j, dureeMoy);

            case ATTENTION:
                return String.format(
                        "%d séances en 7 jours, c'est intense ! Pensez à intégrer " +
                                "des séances légères de récupération active (étirements, " +
                                "marche).", seances7j);

            case CONSEILLE:
                return String.format(
                        "Votre dernière séance était il y a %d jour(s). Une journée " +
                                "de repos supplémentaire optimisera vos performances.", joursRepos);

            case INSUFFISANT:
                return "Vous n'avez pas eu d'activité cette semaine. " +
                        "Essayez de faire au moins 2-3 séances par semaine " +
                        "pour maintenir votre forme.";

            default: // OPTIMAL
                return String.format(
                        "Excellent équilibre ! %d séances cette semaine avec %.0f min " +
                                "en moyenne. Continuez ainsi !", seances7j, dureeMoy);
        }
    }
}