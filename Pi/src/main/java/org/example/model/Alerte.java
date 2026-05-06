package org.example.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Alerte {
    private int id;
    private String type;
    private String message;
    private LocalDateTime dateAlerte;
    private String criticite; // JAUNE, ROUGE
    private int utilisateurId;
    private Integer repasId;

    // Constructeurs
    public Alerte() {}

    public Alerte(String type, String message, String criticite, int utilisateurId, Integer repasId) {
        this.type = type;
        this.message = message;
        this.criticite = criticite;
        this.utilisateurId = utilisateurId;
        this.repasId = repasId;
        this.dateAlerte = LocalDateTime.now();
    }

    public Alerte(int id, String type, String message, LocalDateTime dateAlerte,
                  String criticite, int utilisateurId, Integer repasId) {
        this.id = id;
        this.type = type;
        this.message = message;
        this.dateAlerte = dateAlerte;
        this.criticite = criticite;
        this.utilisateurId = utilisateurId;
        this.repasId = repasId;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getDateAlerte() { return dateAlerte; }
    public void setDateAlerte(LocalDateTime dateAlerte) { this.dateAlerte = dateAlerte; }
    public String getCriticite() { return criticite; }
    public void setCriticite(String criticite) { this.criticite = criticite; }
    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }
    public Integer getRepasId() { return repasId; }
    public void setRepasId(Integer repasId) { this.repasId = repasId; }

    public String getDateFormatee() {
        return dateAlerte.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getCouleurCriticite() {
        return criticite.equals("ROUGE") ? "#e74c3c" : "#f39c12";
    }

    @Override
    public String toString() {
        return "[" + criticite + "] " + type + " - " + message;
    }
}