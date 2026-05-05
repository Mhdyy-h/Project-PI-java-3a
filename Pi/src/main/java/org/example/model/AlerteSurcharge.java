package org.example.model;

public class AlerteSurcharge {

    public enum TypeAlerte {
        CRITIQUE  ("🔴", "Surcharge Critique",
                "Vous vous entraînez trop ! Risque de blessure élevé."),
        ATTENTION ("🟠", "Attention Surcharge",
                "Votre rythme est intense. Prévoyez du repos."),
        CONSEILLE ("🟡", "Repos Conseillé",
                "Une journée de récupération serait bénéfique."),
        OPTIMAL   ("🟢", "Rythme Optimal",
                "Votre rythme d'entraînement est parfait !"),
        INSUFFISANT("🔵", "Activité Insuffisante",
                "Essayez d'augmenter votre fréquence d'entraînement.");

        private final String emoji;
        private final String titre;
        private final String message;

        TypeAlerte(String emoji, String titre, String message) {
            this.emoji   = emoji;
            this.titre   = titre;
            this.message = message;
        }

        public String getEmoji()   { return emoji;   }
        public String getTitre()   { return titre;   }
        public String getMessage() { return message; }
    }

    private TypeAlerte type;
    private int        seances7Jours;
    private int        seances30Jours;
    private double     duréeMoyenne;
    private int        joursRepos;
    private String     conseil;

    public AlerteSurcharge(TypeAlerte type, int seances7Jours,
                           int seances30Jours, double dureeMoyenne,
                           int joursRepos, String conseil) {
        this.type          = type;
        this.seances7Jours = seances7Jours;
        this.seances30Jours= seances30Jours;
        this.duréeMoyenne  = dureeMoyenne;
        this.joursRepos    = joursRepos;
        this.conseil       = conseil;
    }

    public TypeAlerte getType()         { return type;          }
    public int    getSeances7Jours()    { return seances7Jours; }
    public int    getSeances30Jours()   { return seances30Jours;}
    public double getDureeMoyenne()     { return duréeMoyenne;  }
    public int    getJoursRepos()       { return joursRepos;    }
    public String getConseil()          { return conseil;       }
}