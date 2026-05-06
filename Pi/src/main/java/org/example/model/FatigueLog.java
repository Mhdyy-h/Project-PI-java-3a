package org.example.model;

import java.time.LocalDateTime;

/**
 * Mesure de fatigue horodatée, enregistrée après chaque réponse.
 * Correspond à la table {@code fatigue_log} dans biosync.
 *
 * <p>Exemple d'utilisation :</p>
 * <pre>
 *   FatigueLog log = fatigueService.evaluer(sessionId, utilisateurId, numeroQuestion, reponses);
 *   serviceFatigue.enregistrer(log);
 * </pre>
 */
public class FatigueLog {

    private int id;
    private int sessionId;
    private int utilisateurId;
    /** Position de la question dans la session (1-based). */
    private int numeroQuestion;
    /** Score de fatigue normalisé 0-100. */
    private double scoreFatigue;
    /** Moyenne mobile du temps de réponse sur la fenêtre glissante configurée. */
    private int tempsReponseGlissantMs;
    /** Taux d'erreur sur la même fenêtre glissante (0.0 à 1.0). */
    private double tauxErreurGlissant;
    /** LOGICIELLE | VISION */
    private String typeDetection;
    /** CONTINUER | PAUSE_COURTE | ARRETER */
    private String suggestion;
    private LocalDateTime timestamp;

    public FatigueLog() {
        this.typeDetection = "LOGICIELLE";
        this.suggestion = "CONTINUER";
        this.timestamp = LocalDateTime.now();
    }

    public FatigueLog(int sessionId, int utilisateurId, int numeroQuestion,
                      double scoreFatigue, int tempsReponseGlissantMs,
                      double tauxErreurGlissant, String suggestion) {
        this();
        this.sessionId = sessionId;
        this.utilisateurId = utilisateurId;
        this.numeroQuestion = numeroQuestion;
        this.scoreFatigue = scoreFatigue;
        this.tempsReponseGlissantMs = tempsReponseGlissantMs;
        this.tauxErreurGlissant = tauxErreurGlissant;
        this.suggestion = suggestion;
    }

    public FatigueLog(int id, int sessionId, int utilisateurId, int numeroQuestion,
                      double scoreFatigue, int tempsReponseGlissantMs,
                      double tauxErreurGlissant, String typeDetection,
                      String suggestion, LocalDateTime timestamp) {
        this.id = id;
        this.sessionId = sessionId;
        this.utilisateurId = utilisateurId;
        this.numeroQuestion = numeroQuestion;
        this.scoreFatigue = scoreFatigue;
        this.tempsReponseGlissantMs = tempsReponseGlissantMs;
        this.tauxErreurGlissant = tauxErreurGlissant;
        this.typeDetection = typeDetection;
        this.suggestion = suggestion;
        this.timestamp = timestamp;
    }

    public boolean necessitePause()  { return "PAUSE_COURTE".equals(suggestion); }
    public boolean necessiteArret()  { return "ARRETER".equals(suggestion); }
    public boolean peutContinuer()   { return "CONTINUER".equals(suggestion); }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }

    public int getNumeroQuestion() { return numeroQuestion; }
    public void setNumeroQuestion(int numeroQuestion) { this.numeroQuestion = numeroQuestion; }

    public double getScoreFatigue() { return scoreFatigue; }
    public void setScoreFatigue(double scoreFatigue) { this.scoreFatigue = scoreFatigue; }

    public int getTempsReponseGlissantMs() { return tempsReponseGlissantMs; }
    public void setTempsReponseGlissantMs(int tempsReponseGlissantMs) { this.tempsReponseGlissantMs = tempsReponseGlissantMs; }

    public double getTauxErreurGlissant() { return tauxErreurGlissant; }
    public void setTauxErreurGlissant(double tauxErreurGlissant) { this.tauxErreurGlissant = tauxErreurGlissant; }

    public String getTypeDetection() { return typeDetection; }
    public void setTypeDetection(String typeDetection) { this.typeDetection = typeDetection; }

    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "FatigueLog{session=" + sessionId + ", q=" + numeroQuestion
                + ", score=" + scoreFatigue + ", suggestion='" + suggestion + "'}";
    }
}
