package org.example.model;

public class IndiceFormePhysique {

    public enum NiveauForme {
        CRITIQUE    ("💔", "Critique",    "#ff4444", 0,  20),
        FAIBLE      ("❤️",  "Faible",      "#ff8800", 21, 40),
        MOYEN       ("🧡", "Moyen",       "#f0c040", 41, 60),
        BON         ("💚", "Bon",         "#44cc88", 61, 80),
        EXCELLENT   ("💎", "Excellent",   "#00d4ff", 81, 100);

        private final String emoji;
        private final String libelle;
        private final String couleur;
        private final int    scoreMin;
        private final int    scoreMax;

        NiveauForme(String emoji, String libelle, String couleur,
                    int scoreMin, int scoreMax) {
            this.emoji    = emoji;
            this.libelle  = libelle;
            this.couleur  = couleur;
            this.scoreMin = scoreMin;
            this.scoreMax = scoreMax;
        }

        public static NiveauForme calculer(int score) {
            for (NiveauForme n : values()) {
                if (score >= n.scoreMin && score <= n.scoreMax)
                    return n;
            }
            return CRITIQUE;
        }

        public String getEmoji()   { return emoji;   }
        public String getLibelle() { return libelle; }
        public String getCouleur() { return couleur; }
    }

    // ── Score global et détail des critères ──────────────────────
    private int    scoreGlobal;
    private int    scoreRegularite;     // 0-25 pts
    private int    scoreIntensity;      // 0-25 pts
    private int    scoreConsistance;    // 0-25 pts
    private int    scoreRecuperation;   // 0-25 pts
    private NiveauForme niveau;

    // ── Données brutes utilisées ──────────────────────────────────
    private int    totalSeances;
    private int    seances7Jours;
    private double dureeMoyenne;
    private int    joursRepos;
    private int    serie;
    private String conseil;

    public IndiceFormePhysique(int scoreRegularite, int scoreIntensity,
                               int scoreConsistance, int scoreRecuperation,
                               int totalSeances, int seances7Jours,
                               double dureeMoyenne, int joursRepos,
                               int serie, String conseil) {
        this.scoreRegularite  = scoreRegularite;
        this.scoreIntensity   = scoreIntensity;
        this.scoreConsistance = scoreConsistance;
        this.scoreRecuperation= scoreRecuperation;
        this.scoreGlobal      = scoreRegularite + scoreIntensity
                + scoreConsistance + scoreRecuperation;
        this.niveau           = NiveauForme.calculer(scoreGlobal);
        this.totalSeances     = totalSeances;
        this.seances7Jours    = seances7Jours;
        this.dureeMoyenne     = dureeMoyenne;
        this.joursRepos       = joursRepos;
        this.serie            = serie;
        this.conseil          = conseil;
    }

    public int         getScoreGlobal()      { return scoreGlobal;      }
    public int         getScoreRegularite()  { return scoreRegularite;  }
    public int         getScoreIntensity()   { return scoreIntensity;   }
    public int         getScoreConsistance() { return scoreConsistance; }
    public int         getScoreRecuperation(){ return scoreRecuperation;}
    public NiveauForme getNiveau()           { return niveau;           }
    public int         getTotalSeances()     { return totalSeances;     }
    public int         getSeances7Jours()    { return seances7Jours;    }
    public double      getDureeMoyenne()     { return dureeMoyenne;     }
    public int         getJoursRepos()       { return joursRepos;       }
    public int         getSerie()            { return serie;            }
    public String      getConseil()          { return conseil;          }
}