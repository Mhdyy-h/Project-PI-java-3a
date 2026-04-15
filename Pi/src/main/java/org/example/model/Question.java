package org.example.model;

public class Question {

    private int    id;
    private int    quizId;
    private String contenu;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String bonneReponse;   // "A" | "B" | "C" | "D"
    private int    points;
    private String explication;

    public Question() {}

    public Question(int quizId, String contenu,
                    String optionA, String optionB, String optionC, String optionD,
                    String bonneReponse, int points, String explication) {
        this.quizId       = quizId;
        this.contenu      = contenu;
        this.optionA      = optionA;
        this.optionB      = optionB;
        this.optionC      = optionC;
        this.optionD      = optionD;
        this.bonneReponse = bonneReponse;
        this.points       = points;
        this.explication  = explication;
    }

    // ── Getters / Setters ──────────────────────────────────────

    public int getId()              { return id; }
    public void setId(int id)       { this.id = id; }

    public int getQuizId()              { return quizId; }
    public void setQuizId(int quizId)   { this.quizId = quizId; }

    public String getContenu()                { return contenu; }
    public void setContenu(String contenu)    { this.contenu = contenu; }

    public String getOptionA()                { return optionA; }
    public void setOptionA(String optionA)    { this.optionA = optionA; }

    public String getOptionB()                { return optionB; }
    public void setOptionB(String optionB)    { this.optionB = optionB; }

    public String getOptionC()                { return optionC; }
    public void setOptionC(String optionC)    { this.optionC = optionC; }

    public String getOptionD()                { return optionD; }
    public void setOptionD(String optionD)    { this.optionD = optionD; }

    public String getBonneReponse()                   { return bonneReponse; }
    public void setBonneReponse(String bonneReponse)  { this.bonneReponse = bonneReponse; }

    public int getPoints()              { return points; }
    public void setPoints(int points)   { this.points = points; }

    public String getExplication()                  { return explication; }
    public void setExplication(String explication)  { this.explication = explication; }
}
