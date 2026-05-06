package org.example.model;

/**
 * Représente une recommandation nutritionnelle générée par le moteur de règles.
 * Chaque recommandation a une priorité, un type, une icône et un message.
 */
public class Recommandation {

    public enum Priorite { HAUTE, MOYENNE, BASSE }
    public enum TypeReco  { TIMING, NUTRITION, HYDRATATION, EQUILIBRE, POSITIF }

    private final String     message;
    private final String     conseil;    // conseil actionnable supplémentaire
    private final String     icone;
    private final Priorite   priorite;
    private final TypeReco   type;

    public Recommandation(String icone, String message, String conseil,
                          Priorite priorite, TypeReco type) {
        this.icone    = icone;
        this.message  = message;
        this.conseil  = conseil;
        this.priorite = priorite;
        this.type     = type;
    }

    // ── Getters ──────────────────────────────────────────
    public String   getMessage()  { return message; }
    public String   getConseil()  { return conseil; }
    public String   getIcone()    { return icone; }
    public Priorite getPriorite() { return priorite; }
    public TypeReco getType()     { return type; }

    /** Couleur de fond selon la priorité */
    public String getCouleurFond() {
        return switch (priorite) {
            case HAUTE   -> "#fff5f5";
            case MOYENNE -> "#fffbf0";
            case BASSE   -> "#f0fdf4";
        };
    }

    /** Couleur de la barre latérale selon la priorité */
    public String getCouleurBord() {
        return switch (priorite) {
            case HAUTE   -> "#e74c3c";
            case MOYENNE -> "#f39c12";
            case BASSE   -> "#10b981";
        };
    }

    /** Couleur du badge de type */
    public String getCouleurType() {
        return switch (type) {
            case TIMING      -> "#6366f1";
            case NUTRITION   -> "#10b981";
            case HYDRATATION -> "#06b6d4";
            case EQUILIBRE   -> "#f59e0b";
            case POSITIF     -> "#10b981";
        };
    }

    /** Libellé du type affiché dans le badge */
    public String getLibelleType() {
        return switch (type) {
            case TIMING      -> "Timing";
            case NUTRITION   -> "Nutrition";
            case HYDRATATION -> "Hydratation";
            case EQUILIBRE   -> "Équilibre";
            case POSITIF     -> "Bravo !";
        };
    }
}
