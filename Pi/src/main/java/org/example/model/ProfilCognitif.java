package org.example.model;

import java.time.LocalDateTime;

/**
 * Profil cognitif agrégé d'un utilisateur, mis à jour après chaque session.
 * Correspond à la table {@code profil_cognitif} dans biosync.
 *
 * <p>Exemple d'utilisation :</p>
 * <pre>
 *   ProfilCognitif profil = serviceProfilCognitif.getOuCreer(utilisateurId);
 *   profil.mettreAJourApresSession(session, reponses);
 *   serviceProfilCognitif.update(profil);
 * </pre>
 */
public class ProfilCognitif {

    private int id;
    private int utilisateurId;
    private int scoreElo;
    /** Taux de réussite global en pourcentage (0-100). */
    private double tauxReussite;
    private int tempsReponseMoyenMs;
    private int sessionsTotales;
    private String themeFort;
    private String themeFaible;
    /** DEBUTANT | INTERMEDIAIRE | AVANCE */
    private String niveauActuel;
    private LocalDateTime dateMiseAJour;

    public ProfilCognitif() {
        this.scoreElo = 1000;
        this.niveauActuel = "DEBUTANT";
        this.dateMiseAJour = LocalDateTime.now();
    }

    public ProfilCognitif(int utilisateurId) {
        this();
        this.utilisateurId = utilisateurId;
    }

    public ProfilCognitif(int id, int utilisateurId, int scoreElo,
                          double tauxReussite, int tempsReponseMoyenMs, int sessionsTotales,
                          String themeFort, String themeFaible, String niveauActuel,
                          LocalDateTime dateMiseAJour) {
        this.id = id;
        this.utilisateurId = utilisateurId;
        this.scoreElo = scoreElo;
        this.tauxReussite = tauxReussite;
        this.tempsReponseMoyenMs = tempsReponseMoyenMs;
        this.sessionsTotales = sessionsTotales;
        this.themeFort = themeFort;
        this.themeFaible = themeFaible;
        this.niveauActuel = niveauActuel;
        this.dateMiseAJour = dateMiseAJour;
    }

    /** Déduit le niveau textuel depuis la cote ELO. */
    public void recalculerNiveau() {
        if (scoreElo >= 1400) niveauActuel = "AVANCE";
        else if (scoreElo >= 1100) niveauActuel = "INTERMEDIAIRE";
        else niveauActuel = "DEBUTANT";
    }

    public boolean isAvance() { return "AVANCE".equals(niveauActuel); }
    public boolean isIntermediaire() { return "INTERMEDIAIRE".equals(niveauActuel); }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }

    public int getScoreElo() { return scoreElo; }
    public void setScoreElo(int scoreElo) { this.scoreElo = scoreElo; }

    public double getTauxReussite() { return tauxReussite; }
    public void setTauxReussite(double tauxReussite) { this.tauxReussite = tauxReussite; }

    public int getTempsReponseMoyenMs() { return tempsReponseMoyenMs; }
    public void setTempsReponseMoyenMs(int tempsReponseMoyenMs) { this.tempsReponseMoyenMs = tempsReponseMoyenMs; }

    public int getSessionsTotales() { return sessionsTotales; }
    public void setSessionsTotales(int sessionsTotales) { this.sessionsTotales = sessionsTotales; }

    public String getThemeFort() { return themeFort; }
    public void setThemeFort(String themeFort) { this.themeFort = themeFort; }

    public String getThemeFaible() { return themeFaible; }
    public void setThemeFaible(String themeFaible) { this.themeFaible = themeFaible; }

    public String getNiveauActuel() { return niveauActuel; }
    public void setNiveauActuel(String niveauActuel) { this.niveauActuel = niveauActuel; }

    public LocalDateTime getDateMiseAJour() { return dateMiseAJour; }
    public void setDateMiseAJour(LocalDateTime dateMiseAJour) { this.dateMiseAJour = dateMiseAJour; }

    @Override
    public String toString() {
        return "ProfilCognitif{utilisateurId=" + utilisateurId + ", elo=" + scoreElo
                + ", niveau='" + niveauActuel + "', taux=" + tauxReussite + "%}";
    }
}
