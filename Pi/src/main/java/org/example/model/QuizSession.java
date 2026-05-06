package org.example.model;

import java.time.LocalDateTime;

/**
 * Représente une session d'évaluation cognitive complète.
 * Correspond à la table {@code quiz_session} dans biosync.
 *
 * <p>Exemple d'utilisation :</p>
 * <pre>
 *   QuizSession session = new QuizSession(utilisateurId, "Informatique", 5);
 *   serviceQuiz.demarrer(session);
 * </pre>
 */
public class QuizSession {

    private int id;
    private int utilisateurId;
    private String theme;
    /** Difficulté courante sur l'échelle ELO traduite 1-10. */
    private int niveauDifficulte;
    /** Pourcentage de bonnes réponses, calculé à la fin. */
    private double scoreFinal;
    /** Cote ELO de l'utilisateur après cette session. */
    private int scoreElo;
    /** Score de fatigue moyen sur toute la session (0-100). */
    private double scoreFatigue;
    private int nombreQuestions;
    /** EN_COURS | TERMINEE | ABANDONNEE */
    private String statut;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private int dureeSecondes;

    public QuizSession() {
        this.scoreElo = 1000;
        this.statut = "EN_COURS";
        this.dateDebut = LocalDateTime.now();
    }

    public QuizSession(int utilisateurId, String theme, int niveauDifficulte) {
        this();
        this.utilisateurId = utilisateurId;
        this.theme = theme;
        this.niveauDifficulte = niveauDifficulte;
    }

    public QuizSession(int id, int utilisateurId, String theme, int niveauDifficulte,
                       double scoreFinal, int scoreElo, double scoreFatigue,
                       int nombreQuestions, String statut,
                       LocalDateTime dateDebut, LocalDateTime dateFin, int dureeSecondes) {
        this.id = id;
        this.utilisateurId = utilisateurId;
        this.theme = theme;
        this.niveauDifficulte = niveauDifficulte;
        this.scoreFinal = scoreFinal;
        this.scoreElo = scoreElo;
        this.scoreFatigue = scoreFatigue;
        this.nombreQuestions = nombreQuestions;
        this.statut = statut;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.dureeSecondes = dureeSecondes;
    }

    public void terminer(double scoreFinal, int scoreElo) {
        this.scoreFinal = scoreFinal;
        this.scoreElo = scoreElo;
        this.statut = "TERMINEE";
        this.dateFin = LocalDateTime.now();
        if (dateDebut != null) {
            this.dureeSecondes = (int) java.time.Duration.between(dateDebut, dateFin).getSeconds();
        }
    }

    public void abandonner() {
        this.statut = "ABANDONNEE";
        this.dateFin = LocalDateTime.now();
    }

    public boolean isEnCours() { return "EN_COURS".equals(statut); }
    public boolean isTerminee() { return "TERMINEE".equals(statut); }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public int getNiveauDifficulte() { return niveauDifficulte; }
    public void setNiveauDifficulte(int niveauDifficulte) { this.niveauDifficulte = niveauDifficulte; }

    public double getScoreFinal() { return scoreFinal; }
    public void setScoreFinal(double scoreFinal) { this.scoreFinal = scoreFinal; }

    public int getScoreElo() { return scoreElo; }
    public void setScoreElo(int scoreElo) { this.scoreElo = scoreElo; }

    public double getScoreFatigue() { return scoreFatigue; }
    public void setScoreFatigue(double scoreFatigue) { this.scoreFatigue = scoreFatigue; }

    public int getNombreQuestions() { return nombreQuestions; }
    public void setNombreQuestions(int nombreQuestions) { this.nombreQuestions = nombreQuestions; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }

    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }

    public int getDureeSecondes() { return dureeSecondes; }
    public void setDureeSecondes(int dureeSecondes) { this.dureeSecondes = dureeSecondes; }

    @Override
    public String toString() {
        return "QuizSession{id=" + id + ", theme='" + theme + "', statut='" + statut
                + "', score=" + scoreFinal + "%, elo=" + scoreElo + "}";
    }
}
