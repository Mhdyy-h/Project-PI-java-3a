package org.example.model;

import java.time.LocalDateTime;

/**
 * Représente un événement d'anomalie comportementale détecté pendant une session.
 * Correspond à la table {@code anomalie_event} dans biosync.
 *
 * <p>Types d'anomalies possibles :</p>
 * <ul>
 *   <li>{@code REPONSE_TROP_RAPIDE} — réponse en dessous du seuil minimum configuré</li>
 *   <li>{@code REPONSE_TROP_LENTE} — Z-score élevé sur le temps de réponse</li>
 *   <li>{@code COPIER_COLLER} — détection de Ctrl+V pendant la session</li>
 *   <li>{@code REGARD_HORS_ECRAN} — détection vision (FatigueService mode VISION)</li>
 *   <li>{@code SEQUENCE_SUSPECTE} — pattern inhabituel de réponses correctes/incorrectes</li>
 * </ul>
 *
 * <p>Exemple d'utilisation :</p>
 * <pre>
 *   AnomalieEvent evt = new AnomalieEvent(sessionId, utilisateurId,
 *       "REPONSE_TROP_RAPIDE", "CRITIQUE", 80.0, 800.0,
 *       "Réponse en 80ms, seuil minimum 800ms");
 *   serviceAnomalie.enregistrer(evt);
 * </pre>
 */
public class AnomalieEvent {

    private int id;
    private int sessionId;
    private int utilisateurId;
    private String typeAnomalie;
    /** FAIBLE | MODERE | CRITIQUE */
    private String niveauGravite;
    private double valeurObservee;
    private double valeurSeuil;
    private String description;
    private LocalDateTime timestamp;

    public AnomalieEvent() {
        this.niveauGravite = "FAIBLE";
        this.timestamp = LocalDateTime.now();
    }

    public AnomalieEvent(int sessionId, int utilisateurId, String typeAnomalie,
                         String niveauGravite, double valeurObservee,
                         double valeurSeuil, String description) {
        this();
        this.sessionId = sessionId;
        this.utilisateurId = utilisateurId;
        this.typeAnomalie = typeAnomalie;
        this.niveauGravite = niveauGravite;
        this.valeurObservee = valeurObservee;
        this.valeurSeuil = valeurSeuil;
        this.description = description;
    }

    public AnomalieEvent(int id, int sessionId, int utilisateurId, String typeAnomalie,
                         String niveauGravite, double valeurObservee, double valeurSeuil,
                         String description, LocalDateTime timestamp) {
        this.id = id;
        this.sessionId = sessionId;
        this.utilisateurId = utilisateurId;
        this.typeAnomalie = typeAnomalie;
        this.niveauGravite = niveauGravite;
        this.valeurObservee = valeurObservee;
        this.valeurSeuil = valeurSeuil;
        this.description = description;
        this.timestamp = timestamp;
    }

    public boolean isCritique() { return "CRITIQUE".equals(niveauGravite); }
    public boolean isModere()   { return "MODERE".equals(niveauGravite); }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }

    public String getTypeAnomalie() { return typeAnomalie; }
    public void setTypeAnomalie(String typeAnomalie) { this.typeAnomalie = typeAnomalie; }

    public String getNiveauGravite() { return niveauGravite; }
    public void setNiveauGravite(String niveauGravite) { this.niveauGravite = niveauGravite; }

    public double getValeurObservee() { return valeurObservee; }
    public void setValeurObservee(double valeurObservee) { this.valeurObservee = valeurObservee; }

    public double getValeurSeuil() { return valeurSeuil; }
    public void setValeurSeuil(double valeurSeuil) { this.valeurSeuil = valeurSeuil; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "AnomalieEvent{session=" + sessionId + ", type='" + typeAnomalie
                + "', gravite='" + niveauGravite + "', valeur=" + valeurObservee + "}";
    }
}
