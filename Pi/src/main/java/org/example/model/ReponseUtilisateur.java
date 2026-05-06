package org.example.model;

import java.time.LocalDateTime;

/**
 * Représente la réponse d'un utilisateur à une question dans une session.
 * Correspond à la table {@code reponse_utilisateur} dans biosync.
 *
 * <p>Exemple d'utilisation :</p>
 * <pre>
 *   ReponseUtilisateur rep = new ReponseUtilisateur(sessionId, questionId, "Paris", true, 3200, 1);
 *   serviceQuiz.enregistrerReponse(rep);
 * </pre>
 */
public class ReponseUtilisateur {

    private int id;
    private int sessionId;
    private int questionId;
    private String reponseDonnee;
    private boolean estCorrecte;
    /** Durée entre l'affichage de la question et la soumission, en millisecondes. */
    private int tempsReponseMs;
    /** Position de cette question dans la session (1-based). */
    private int ordreSession;
    private LocalDateTime dateReponse;

    public ReponseUtilisateur() {
        this.dateReponse = LocalDateTime.now();
    }

    public ReponseUtilisateur(int sessionId, int questionId,
                               String reponseDonnee, boolean estCorrecte,
                               int tempsReponseMs, int ordreSession) {
        this();
        this.sessionId = sessionId;
        this.questionId = questionId;
        this.reponseDonnee = reponseDonnee;
        this.estCorrecte = estCorrecte;
        this.tempsReponseMs = tempsReponseMs;
        this.ordreSession = ordreSession;
    }

    public ReponseUtilisateur(int id, int sessionId, int questionId,
                               String reponseDonnee, boolean estCorrecte,
                               int tempsReponseMs, int ordreSession,
                               LocalDateTime dateReponse) {
        this.id = id;
        this.sessionId = sessionId;
        this.questionId = questionId;
        this.reponseDonnee = reponseDonnee;
        this.estCorrecte = estCorrecte;
        this.tempsReponseMs = tempsReponseMs;
        this.ordreSession = ordreSession;
        this.dateReponse = dateReponse;
    }

    /** Retourne le temps de réponse en secondes (arrondi). */
    public double getTempsReponseSecondes() { return tempsReponseMs / 1000.0; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public String getReponseDonnee() { return reponseDonnee; }
    public void setReponseDonnee(String reponseDonnee) { this.reponseDonnee = reponseDonnee; }

    public boolean isEstCorrecte() { return estCorrecte; }
    public void setEstCorrecte(boolean estCorrecte) { this.estCorrecte = estCorrecte; }

    public int getTempsReponseMs() { return tempsReponseMs; }
    public void setTempsReponseMs(int tempsReponseMs) { this.tempsReponseMs = tempsReponseMs; }

    public int getOrdreSession() { return ordreSession; }
    public void setOrdreSession(int ordreSession) { this.ordreSession = ordreSession; }

    public LocalDateTime getDateReponse() { return dateReponse; }
    public void setDateReponse(LocalDateTime dateReponse) { this.dateReponse = dateReponse; }

    @Override
    public String toString() {
        return "ReponseUtilisateur{sessionId=" + sessionId + ", questionId=" + questionId
                + ", correct=" + estCorrecte + ", tempsMs=" + tempsReponseMs + "}";
    }
}
