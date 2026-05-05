package org.example.model;

public class Question {

    private int    id;
    private int    quizId;
    private String enonce;
    private String reponseCorrecte;
    private String optionsFausses;  // pipe-separated: "opt1|opt2|opt3"
    private int    pointsValeur;
    private String explication;

    public Question() {}

    public Question(int quizId, String enonce, String reponseCorrecte,
                    String optionsFausses, int pointsValeur) {
        this.quizId          = quizId;
        this.enonce          = enonce;
        this.reponseCorrecte = reponseCorrecte;
        this.optionsFausses  = optionsFausses;
        this.pointsValeur    = pointsValeur;
    }

    // ── Getters / Setters ──────────────────────────────────────

    public int getId()              { return id; }
    public void setId(int id)       { this.id = id; }

    public int getQuizId()              { return quizId; }
    public void setQuizId(int quizId)   { this.quizId = quizId; }

    public String getEnonce()               { return enonce; }
    public void setEnonce(String enonce)    { this.enonce = enonce; }

    public String getReponseCorrecte()                      { return reponseCorrecte; }
    public void setReponseCorrecte(String reponseCorrecte)  { this.reponseCorrecte = reponseCorrecte; }

    public String getOptionsFausses()                       { return optionsFausses; }
    public void setOptionsFausses(String optionsFausses)    { this.optionsFausses = optionsFausses; }

    public int getPointsValeur()                  { return pointsValeur; }
    public void setPointsValeur(int pointsValeur) { this.pointsValeur = pointsValeur; }

    // ── Méthodes de compatibilité (alias pour ancien code) ──────────────────────────────────────

    public String getContenu() { return enonce; }
    public void setContenu(String contenu) { this.enonce = contenu; }

    public String getBonneReponse() { return reponseCorrecte; }
    public void setBonneReponse(String bonneReponse) { this.reponseCorrecte = bonneReponse; }

    public int getPoints() { return pointsValeur; }
    public void setPoints(int points) { this.pointsValeur = points; }

    /** Parse options_fausses pipe-separated string into individual options */
    public String getOptionA() {
        if (optionsFausses == null) return "";
        String[] parts = optionsFausses.split("\\|");
        return parts.length > 0 ? parts[0] : "";
    }
    public void setOptionA(String optionA) {
        // Build new pipe-separated string
        String[] parts = optionsFausses != null ? optionsFausses.split("\\|") : new String[3];
        if (parts.length < 3) parts = new String[]{"", "", ""};
        parts[0] = optionA;
        optionsFausses = String.join("|", parts);
    }

    public String getOptionB() {
        if (optionsFausses == null) return "";
        String[] parts = optionsFausses.split("\\|");
        return parts.length > 1 ? parts[1] : "";
    }
    public void setOptionB(String optionB) {
        String[] parts = optionsFausses != null ? optionsFausses.split("\\|") : new String[3];
        if (parts.length < 3) parts = new String[]{"", "", ""};
        parts[1] = optionB;
        optionsFausses = String.join("|", parts);
    }

    public String getOptionC() {
        if (optionsFausses == null) return "";
        String[] parts = optionsFausses.split("\\|");
        return parts.length > 2 ? parts[2] : "";
    }
    public void setOptionC(String optionC) {
        String[] parts = optionsFausses != null ? optionsFausses.split("\\|") : new String[3];
        if (parts.length < 3) parts = new String[]{"", "", ""};
        parts[2] = optionC;
        optionsFausses = String.join("|", parts);
    }

    public String getOptionD() {
        if (optionsFausses == null) return "";
        String[] parts = optionsFausses.split("\\|");
        return parts.length > 3 ? parts[3] : "";
    }
    public void setOptionD(String optionD) {
        String[] parts = optionsFausses != null ? optionsFausses.split("\\|") : new String[4];
        if (parts.length < 4) {
            String[] newParts = new String[4];
            System.arraycopy(parts, 0, newParts, 0, parts.length);
            parts = newParts;
        }
        parts[3] = optionD;
        optionsFausses = String.join("|", parts);
    }

    public String getExplication() { return explication; }
    public void setExplication(String explication) { this.explication = explication; }
}
