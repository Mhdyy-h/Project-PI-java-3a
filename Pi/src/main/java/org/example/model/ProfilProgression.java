package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class ProfilProgression {

    private User           user;
    private int            totalSeances;
    private int            serieActuelle;        // jours consécutifs
    private int            sportsDistincts;
    private int            seancesHauteIntensite;
    private NiveauAthlete  niveau;
    private List<Badge>    badges;
    private int            pointsXP;             // Points d'expérience

    public ProfilProgression(User user) {
        this.user                 = user;
        this.badges               = new ArrayList<>();
        this.totalSeances         = 0;
        this.serieActuelle        = 0;
        this.sportsDistincts      = 0;
        this.seancesHauteIntensite= 0;
        this.niveau               = NiveauAthlete.DEBUTANT;
        this.pointsXP             = 0;

        // Initialiser tous les badges comme verrouillés
        for (Badge.TypeBadge type : Badge.TypeBadge.values()) {
            badges.add(new Badge(type));
        }
    }

    /** Pourcentage de progression vers le niveau suivant */
    public double getPourcentageProgression() {
        if (niveau == NiveauAthlete.ELITE) return 100.0;
        int range  = niveau.getSeancesMax() - niveau.getSeancesMin() + 1;
        int avance = totalSeances - niveau.getSeancesMin();
        return Math.min(100.0, (avance * 100.0) / range);
    }

    /** Séances restantes pour monter de niveau */
    public int getSeancesRestantes() {
        return niveau.seancesRestantes(totalSeances);
    }

    // ── Getters / Setters ─────────────────────────────────────────
    public User          getUser()                  { return user;                  }
    public int           getTotalSeances()          { return totalSeances;          }
    public void          setTotalSeances(int n)     { this.totalSeances = n;        }
    public int           getSerieActuelle()         { return serieActuelle;         }
    public void          setSerieActuelle(int n)    { this.serieActuelle = n;       }
    public int           getSportsDistincts()       { return sportsDistincts;       }
    public void          setSportsDistincts(int n)  { this.sportsDistincts = n;     }
    public int           getSeancesHauteIntensite() { return seancesHauteIntensite; }
    public void          setSeancesHauteIntensite(int n){ this.seancesHauteIntensite = n; }
    public NiveauAthlete getNiveau()                { return niveau;                }
    public void          setNiveau(NiveauAthlete n) { this.niveau = n;              }
    public List<Badge>   getBadges()                { return badges;                }
    public int           getPointsXP()              { return pointsXP;              }
    public void          setPointsXP(int xp)        { this.pointsXP = xp;           }
}