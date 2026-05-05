package org.example.model;

import java.time.LocalDate;

public class Badge {

    public enum TypeBadge {
        PREMIERE_SEANCE   ("🎯", "Première Séance",
                "Tu as complété ta toute première séance !"),
        SERIE_3           ("🔥", "Série de 3",
                "3 séances consécutives complétées !"),
        SERIE_7           ("💪", "Semaine Parfaite",
                "7 séances consécutives — Incroyable !"),
        DIVERSITE         ("🌈", "Athlète Complet",
                "Tu as pratiqué 5 sports différents !"),
        CENTENAIRE        ("💯", "100 Séances",
                "100 séances complétées — Légende !"),
        REGULIER          ("📅", "Régularité",
                "30 jours consécutifs d'activité !"),
        INTENSITE         ("⚡", "Haute Intensité",
                "10 séances de haute intensité !"),
        COACH_STAR        ("⭐", "Coach Star",
                "Tes athlètes ont complété 50 séances !");

        private final String emoji;
        private final String nom;
        private final String description;

        TypeBadge(String emoji, String nom, String description) {
            this.emoji       = emoji;
            this.nom         = nom;
            this.description = description;
        }

        public String getEmoji()       { return emoji;       }
        public String getNom()         { return nom;         }
        public String getDescription() { return description; }
    }

    private TypeBadge   type;
    private LocalDate   dateObtention;
    private boolean     debloque;

    public Badge(TypeBadge type) {
        this.type          = type;
        this.debloque      = false;
        this.dateObtention = null;
    }

    public void debloquer() {
        this.debloque      = true;
        this.dateObtention = LocalDate.now();
    }

    public boolean isDebloque()        { return debloque;      }
    public TypeBadge getType()         { return type;          }
    public LocalDate getDateObtention(){ return dateObtention; }
}