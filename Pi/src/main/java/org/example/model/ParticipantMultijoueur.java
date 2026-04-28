package org.example.model;

import java.time.LocalDateTime;

/**
 * Représente un joueur dans une session multijoueur.
 * Correspond à la table {@code participant_multijoueur} dans biosync.
 *
 * <p>Exemple d'utilisation :</p>
 * <pre>
 *   ParticipantMultijoueur p = new ParticipantMultijoueur(sessionMultiId, utilisateurId);
 *   serviceMultijoueur.rejoindre(p);
 * </pre>
 */
public class ParticipantMultijoueur {

    private int id;
    private int sessionMultiId;
    private int utilisateurId;
    private int score;
    /** Position finale dans le classement (1 = premier). 0 = non encore calculé. */
    private int rangFinal;
    /** CONNECTE | DECONNECTE | TERMINE */
    private String statut;
    private LocalDateTime dateJoin;

    public ParticipantMultijoueur() {
        this.statut = "CONNECTE";
        this.dateJoin = LocalDateTime.now();
    }

    public ParticipantMultijoueur(int sessionMultiId, int utilisateurId) {
        this();
        this.sessionMultiId = sessionMultiId;
        this.utilisateurId = utilisateurId;
    }

    public ParticipantMultijoueur(int id, int sessionMultiId, int utilisateurId,
                                   int score, int rangFinal, String statut,
                                   LocalDateTime dateJoin) {
        this.id = id;
        this.sessionMultiId = sessionMultiId;
        this.utilisateurId = utilisateurId;
        this.score = score;
        this.rangFinal = rangFinal;
        this.statut = statut;
        this.dateJoin = dateJoin;
    }

    public void ajouterPoints(int points) { this.score += points; }

    public void deconnecter() { this.statut = "DECONNECTE"; }
    public void terminer()    { this.statut = "TERMINE"; }

    public boolean isConnecte()    { return "CONNECTE".equals(statut); }
    public boolean isDeconnecte()  { return "DECONNECTE".equals(statut); }
    public boolean isTermine()     { return "TERMINE".equals(statut); }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSessionMultiId() { return sessionMultiId; }
    public void setSessionMultiId(int sessionMultiId) { this.sessionMultiId = sessionMultiId; }

    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getRangFinal() { return rangFinal; }
    public void setRangFinal(int rangFinal) { this.rangFinal = rangFinal; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public LocalDateTime getDateJoin() { return dateJoin; }
    public void setDateJoin(LocalDateTime dateJoin) { this.dateJoin = dateJoin; }

    @Override
    public String toString() {
        return "ParticipantMultijoueur{sessionId=" + sessionMultiId
                + ", utilisateurId=" + utilisateurId + ", score=" + score
                + ", rang=" + rangFinal + "}";
    }
}
