package org.example.model;

public enum NiveauAthlete {

    DEBUTANT("Débutant", 0, 4, "🌱",
            "Tu commences ton aventure sportive !"),

    INTERMEDIAIRE("Intermédiaire", 5, 14, "⚡",
            "Tu prends de la vitesse, continue !"),

    AVANCE("Avancé", 15, 29, "🔥",
            "Tu maîtrises les bases avancées !"),

    EXPERT("Expert", 30, 49, "💎",
            "Tu es un athlète accompli !"),

    ELITE("Élite", 50, Integer.MAX_VALUE, "🏆",
            "Tu es au sommet de ta forme !");

    private final String libelle;
    private final int seancesMin;
    private final int seancesMax;
    private final String emoji;
    private final String message;

    NiveauAthlete(String libelle, int seancesMin,
                  int seancesMax, String emoji, String message) {
        this.libelle   = libelle;
        this.seancesMin = seancesMin;
        this.seancesMax = seancesMax;
        this.emoji     = emoji;
        this.message   = message;
    }

    /** Retourne le niveau selon le nombre de séances complétées */
    public static NiveauAthlete calculer(int totalSeances) {
        for (NiveauAthlete n : values()) {
            if (totalSeances >= n.seancesMin
                    && totalSeances <= n.seancesMax) {
                return n;
            }
        }
        return DEBUTANT;
    }

    /** Séances restantes pour passer au niveau suivant */
    public int seancesRestantes(int totalSeances) {
        if (this == ELITE) return 0;
        return seancesMax - totalSeances + 1;
    }

    public String getLibelle()   { return libelle;    }
    public String getEmoji()     { return emoji;      }
    public String getMessage()   { return message;    }
    public int    getSeancesMin(){ return seancesMin; }
    public int    getSeancesMax(){ return seancesMax; }
}