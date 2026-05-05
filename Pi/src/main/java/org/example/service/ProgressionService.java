package org.example.service;

import org.example.model.*;
import org.example.dao.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ProgressionService {

    private Connection conn;

    public ProgressionService() {
        this.conn = DatabaseConnection.getConnection();
    }

    // ─────────────────────────────────────────────────────────────
    //  CALCUL PRINCIPAL — construit le profil complet d'un user
    // ─────────────────────────────────────────────────────────────
    public ProfilProgression calculerProfil(User user) {

        ProfilProgression profil = new ProfilProgression(user);

        try {
            // 1. Récupérer toutes les séances de l'utilisateur
            List<SeanceSport> seances = getSeancesByUser(user.getId());

            int total           = seances.size();
            int serie           = calculerSerieConsecutive(seances);
            int sportsDistincts = compterSportsDistincts(seances);
            int hauteIntensite  = compterHauteIntensite(seances);

            profil.setTotalSeances(total);
            profil.setSerieActuelle(serie);
            profil.setSportsDistincts(sportsDistincts);
            profil.setSeancesHauteIntensite(hauteIntensite);

            // 2. Calculer le niveau
            NiveauAthlete niveau = NiveauAthlete.calculer(total);
            profil.setNiveau(niveau);

            // 3. Calculer les XP
            profil.setPointsXP(calculerXP(total, serie, hauteIntensite));

            // 4. Débloquer les badges mérités
            debloquerBadges(profil);

        } catch (SQLException e) {
            System.err.println("Erreur ProgressionService : " + e.getMessage());
        }

        return profil;
    }

    // ─────────────────────────────────────────────────────────────
    //  RÉCUPÉRER LES SÉANCES D'UN USER — adapté à ton vrai DAO
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
    //  CALCUL XP
    // ─────────────────────────────────────────────────────────────
    private int calculerXP(int total, int serie, int hauteIntensite) {
        int xp = 0;
        xp += total          * 10;
        xp += serie          * 5;
        xp += hauteIntensite * 15;
        return xp;
    }

    // ─────────────────────────────────────────────────────────────
    //  SÉRIE CONSÉCUTIVE — basé sur date_seance (String "yyyy-MM-dd")
    // ─────────────────────────────────────────────────────────────
    private int calculerSerieConsecutive(List<SeanceSport> seances) {
        if (seances == null || seances.isEmpty()) return 0;

        // Convertir et trier par date décroissante
        List<LocalDate> dates = new ArrayList<>();
        for (SeanceSport s : seances) {
            try {
                dates.add(LocalDate.parse(s.dateSeance));
            } catch (Exception e) {
                // ignorer les dates malformées
            }
        }

        if (dates.isEmpty()) return 0;

        dates.sort((a, b) -> b.compareTo(a)); // décroissant

        int serie = 1;
        for (int i = 0; i < dates.size() - 1; i++) {
            long diff = ChronoUnit.DAYS.between(dates.get(i + 1), dates.get(i));
            if (diff == 1) serie++;
            else break;
        }
        return serie;
    }

    // ─────────────────────────────────────────────────────────────
    //  SPORTS DISTINCTS — basé sur nom_seance (approximation)
    // ─────────────────────────────────────────────────────────────
    private int compterSportsDistincts(List<SeanceSport> seances) {
        return (int) seances.stream()
                .map(s -> s.nomSeance)
                .distinct()
                .count();
    }

    // ─────────────────────────────────────────────────────────────
    //  HAUTE INTENSITÉ — séances avec duree_minutes > 45
    // ─────────────────────────────────────────────────────────────
    private int compterHauteIntensite(List<SeanceSport> seances) {
        return (int) seances.stream()
                .filter(s -> s.dureeMinutes > 45)
                .count();
    }

    // ─────────────────────────────────────────────────────────────
    //  DÉBLOCAGE DES BADGES
    // ─────────────────────────────────────────────────────────────
    private void debloquerBadges(ProfilProgression profil) {
        for (Badge badge : profil.getBadges()) {
            if (badge.isDebloque()) continue;

            switch (badge.getType()) {
                case PREMIERE_SEANCE:
                    if (profil.getTotalSeances() >= 1)
                        badge.debloquer();
                    break;
                case SERIE_3:
                    if (profil.getSerieActuelle() >= 3)
                        badge.debloquer();
                    break;
                case SERIE_7:
                    if (profil.getSerieActuelle() >= 7)
                        badge.debloquer();
                    break;
                case DIVERSITE:
                    if (profil.getSportsDistincts() >= 5)
                        badge.debloquer();
                    break;
                case CENTENAIRE:
                    if (profil.getTotalSeances() >= 100)
                        badge.debloquer();
                    break;
                case REGULIER:
                    if (profil.getSerieActuelle() >= 30)
                        badge.debloquer();
                    break;
                case INTENSITE:
                    if (profil.getSeancesHauteIntensite() >= 10)
                        badge.debloquer();
                    break;
                default:
                    break;
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  MESSAGE MOTIVANT
    // ─────────────────────────────────────────────────────────────
    public String getMessageMotivant(ProfilProgression profil) {
        NiveauAthlete niveau = profil.getNiveau();

        if (niveau == NiveauAthlete.ELITE) {
            return "🏆 Tu as atteint le sommet ! Tu es une légende du sport !";
        }

        NiveauAthlete prochain = NiveauAthlete.values()[niveau.ordinal() + 1];

        return String.format(
                "%s Plus que %d séance(s) pour atteindre le niveau %s %s !",
                niveau.getEmoji(),
                profil.getSeancesRestantes(),
                prochain.getEmoji(),
                prochain.getLibelle()
        );
    }
}