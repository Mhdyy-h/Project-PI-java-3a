package org.example;

import model.Exercice;
import model.SeanceSport;
import dao.DatabaseConnection;
import service.ServiceExercice;
import service.ServiceSeanceSport;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {

        ServiceSeanceSport serviceSeance = new ServiceSeanceSport();
        ServiceExercice serviceExercice = new ServiceExercice();

        // 🟢 AJOUTER une seance
        SeanceSport s = new SeanceSport(0, "Cardio Matin", "08:00:00", 45,
                "Or", "2025-04-10", 15, null, 0);
        serviceSeance.ajouter(s);

        // 🔵 AFFICHER toutes les seances
        System.out.println("\n--- Seances ---");
        for (SeanceSport seance : serviceSeance.afficherAll()) {
            System.out.println(seance);
        }

        // 🟢 AJOUTER un exercice
        Exercice e = new Exercice(0, "Jumping Jack", "Haute", 8.5, 1);
        serviceExercice.ajouter(e);

        // 🔵 AFFICHER tous les exercices
        System.out.println("\n--- Exercices ---");
        for (Exercice ex : serviceExercice.afficherAll()) {
            System.out.println(ex);
        }

        // 🟡 MODIFIER une seance (id=1)
        SeanceSport s2 = new SeanceSport(1, "Cardio Soir", "18:00:00", 60,
                "Argent", "2025-04-10", 15, null, 0);
        serviceSeance.update(s2);

        // 🔴 SUPPRIMER un exercice (id=1)
        serviceExercice.delete(1);

        // 🔍 RECHERCHER par nom
        System.out.println("\n--- Recherche par nom ---");
        for (SeanceSport seance : serviceSeance.rechercherParNom("Cardio Matin")) {
            System.out.println(seance);
        }

        // 🔍 RECHERCHER par utilisateur
        System.out.println("\n--- Recherche par utilisateur ---");
        for (SeanceSport seance : serviceSeance.rechercherParUtilisateur(15)) {
            System.out.println(seance);
        }

        // Fermer la connexion
        DatabaseConnection.getConnection().close();
        System.out.println("✅ Connexion fermee !");
    }
}