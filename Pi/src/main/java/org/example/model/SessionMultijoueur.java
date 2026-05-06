package org.example.model;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Représente un salon de jeu multijoueur local (réseau WiFi via WebSocket).
 * Correspond à la table {@code session_multijoueur} dans biosync.
 *
 * <p>Exemple d'utilisation :</p>
 * <pre>
 *   SessionMultijoueur salon = new SessionMultijoueur(createurId, "Informatique", 10, 30);
 *   serviceMultijoueur.creerSalon(salon); // génère le codeSalon automatiquement
 * </pre>
 */
public class SessionMultijoueur {

    private int id;
    /** Code court de 6 caractères alphanumériques pour rejoindre le salon, ex: "AX7K2P". */
    private String codeSalon;
    private String theme;
    private int nombreQuestions;
    private int tempsParQuestionSec;
    /** ATTENTE | EN_COURS | TERMINEE */
    private String statut;
    private int createurId;
    private LocalDateTime dateCreation;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final Random RANDOM = new Random();

    public SessionMultijoueur() {
        this.nombreQuestions = 10;
        this.tempsParQuestionSec = 30;
        this.statut = "ATTENTE";
        this.dateCreation = LocalDateTime.now();
        this.codeSalon = genererCode();
    }

    public SessionMultijoueur(int createurId, String theme, int nombreQuestions, int tempsParQuestionSec) {
        this();
        this.createurId = createurId;
        this.theme = theme;
        this.nombreQuestions = nombreQuestions;
        this.tempsParQuestionSec = tempsParQuestionSec;
    }

    public SessionMultijoueur(int id, String codeSalon, String theme,
                               int nombreQuestions, int tempsParQuestionSec,
                               String statut, int createurId,
                               LocalDateTime dateCreation, LocalDateTime dateDebut,
                               LocalDateTime dateFin) {
        this.id = id;
        this.codeSalon = codeSalon;
        this.theme = theme;
        this.nombreQuestions = nombreQuestions;
        this.tempsParQuestionSec = tempsParQuestionSec;
        this.statut = statut;
        this.createurId = createurId;
        this.dateCreation = dateCreation;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    private static String genererCode() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        return sb.toString();
    }

    public void demarrer() {
        this.statut = "EN_COURS";
        this.dateDebut = LocalDateTime.now();
    }

    public void terminer() {
        this.statut = "TERMINEE";
        this.dateFin = LocalDateTime.now();
    }

    public boolean isEnAttente() { return "ATTENTE".equals(statut); }
    public boolean isEnCours()   { return "EN_COURS".equals(statut); }
    public boolean isTerminee()  { return "TERMINEE".equals(statut); }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCodeSalon() { return codeSalon; }
    public void setCodeSalon(String codeSalon) { this.codeSalon = codeSalon; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public int getNombreQuestions() { return nombreQuestions; }
    public void setNombreQuestions(int nombreQuestions) { this.nombreQuestions = nombreQuestions; }

    public int getTempsParQuestionSec() { return tempsParQuestionSec; }
    public void setTempsParQuestionSec(int tempsParQuestionSec) { this.tempsParQuestionSec = tempsParQuestionSec; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public int getCreateurId() { return createurId; }
    public void setCreateurId(int createurId) { this.createurId = createurId; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }

    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }

    @Override
    public String toString() {
        return "SessionMultijoueur{code='" + codeSalon + "', theme='" + theme
                + "', statut='" + statut + "'}";
    }
}
