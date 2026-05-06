package org.example.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ActivityLog {
    private int id;
    private int userId;
    private String nomUtilisateur;
    private String email;
    private String roles;
    private String action;
    private LocalDateTime dateHeure;

    public ActivityLog() {}

    public ActivityLog(int id, int userId, String nomUtilisateur, String email, String roles, String action, LocalDateTime dateHeure) {
        this.id = id;
        this.userId = userId;
        this.nomUtilisateur = nomUtilisateur;
        this.email = email;
        this.roles = roles;
        this.action = action;
        this.dateHeure = dateHeure;
    }

    public ActivityLog(int userId, String nomUtilisateur, String email, String roles, String action) {
        this.userId = userId;
        this.nomUtilisateur = nomUtilisateur;
        this.email = email;
        this.roles = roles;
        this.action = action;
        this.dateHeure = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getNomUtilisateur() { return nomUtilisateur; }
    public void setNomUtilisateur(String nomUtilisateur) { this.nomUtilisateur = nomUtilisateur; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public LocalDateTime getDateHeure() { return dateHeure; }
    public void setDateHeure(LocalDateTime dateHeure) { this.dateHeure = dateHeure; }

    public String getDateHeureFormatted() {
        if (dateHeure == null) return "";
        return dateHeure.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    public String getDisplayRole() {
        if (roles == null) return "UTILISATEUR";
        if (roles.contains("ADMIN")) return "ROLE_ADMIN";
        if (roles.contains("COACH")) return "ROLE_COACH";
        if (roles.contains("SPECIALISTE")) return "ROLE_SPECIALISTE";
        return "ROLE_USER";
    }
}
