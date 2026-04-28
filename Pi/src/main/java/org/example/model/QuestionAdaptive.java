package org.example.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente une question de la banque ou générée par Claude API.
 * Correspond à la table {@code question} dans biosync.
 *
 * <p>Exemple d'utilisation :</p>
 * <pre>
 *   Question q = new Question("Quelle est la capitale de France ?", "QCM", "Géographie", 1);
 *   q.setReponseCorrecte("Paris");
 *   q.setOptions(List.of("Paris", "Lyon", "Marseille", "Bordeaux"));
 *   serviceQuestion.ajouter(q);
 * </pre>
 */
public class QuestionAdaptive {

    private int id;
    private String enonce;
    /** QCM | VRAI_FAUX | OUVERTE */
    private String type;
    private String theme;
    /** Difficulté 1 (très facile) à 10 (très difficile). */
    private int niveauDifficulte;
    /** BANQUE | IA_GENEREE */
    private String source;
    private String reponseCorrecte;
    /** Liste des choix pour les QCM. Null pour VRAI_FAUX et OUVERTE. */
    private List<String> options;
    /** Explication générée par Claude API après la réponse. */
    private String explication;
    private LocalDateTime dateCreation;

    public QuestionAdaptive() {
        this.type = "QCM";
        this.source = "BANQUE";
        this.options = new ArrayList<>();
        this.dateCreation = LocalDateTime.now();
    }

    public QuestionAdaptive(String enonce, String type, String theme, int niveauDifficulte) {
        this();
        this.enonce = enonce;
        this.type = type;
        this.theme = theme;
        this.niveauDifficulte = niveauDifficulte;
    }

    public QuestionAdaptive(int id, String enonce, String type, String theme, int niveauDifficulte,
                            String source, String reponseCorrecte, List<String> options,
                            String explication, LocalDateTime dateCreation) {
        this.id = id;
        this.enonce = enonce;
        this.type = type;
        this.theme = theme;
        this.niveauDifficulte = niveauDifficulte;
        this.source = source;
        this.reponseCorrecte = reponseCorrecte;
        this.options = options != null ? options : new ArrayList<>();
        this.explication = explication;
        this.dateCreation = dateCreation;
    }

    public boolean estReponseCorrecte(String reponse) {
        if (reponse == null || reponseCorrecte == null) return false;
        return reponseCorrecte.trim().equalsIgnoreCase(reponse.trim());
    }

    public boolean isQcm() { return "QCM".equals(type); }
    public boolean isVraiFaux() { return "VRAI_FAUX".equals(type); }
    public boolean isOuverte() { return "OUVERTE".equals(type); }
    public boolean isGenereeParIA() { return "IA_GENEREE".equals(source); }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEnonce() { return enonce; }
    public void setEnonce(String enonce) { this.enonce = enonce; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public int getNiveauDifficulte() { return niveauDifficulte; }
    public void setNiveauDifficulte(int niveauDifficulte) { this.niveauDifficulte = niveauDifficulte; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getReponseCorrecte() { return reponseCorrecte; }
    public void setReponseCorrecte(String reponseCorrecte) { this.reponseCorrecte = reponseCorrecte; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public String getExplication() { return explication; }
    public void setExplication(String explication) { this.explication = explication; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    @Override
    public String toString() {
        return "Question{id=" + id + ", type='" + type + "', theme='" + theme
                + "', difficulte=" + niveauDifficulte + ", source='" + source + "'}";
    }
}
