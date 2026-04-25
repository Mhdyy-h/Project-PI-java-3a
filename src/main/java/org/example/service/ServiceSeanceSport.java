package org.example.service;

import org.example.model.SeanceSport;
import org.example.dao.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;

public class ServiceSeanceSport {

    Connection conn = DatabaseConnection.getConnection();

    // 🟢 AJOUTER
    public void ajouter(SeanceSport s) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO seance_sport (nom_seance, heure_debut, duree_minutes, " +
                        "medaille_obtenue, date_seance, utilisateur_id, heure_debut_reelle, alerte_envoyee) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
        );
        ps.setString(1, s.nomSeance);
        ps.setString(2, s.heureDebut);
        ps.setInt(3, s.dureeMinutes);
        ps.setString(4, s.medailleObtenue);
        ps.setString(5, s.dateSeance);
        ps.setInt(6, s.utilisateurId);
        ps.setString(7, s.heureDebutReelle);
        ps.setInt(8, s.alerteEnvoyee);
        ps.executeUpdate();
        System.out.println("✅ Seance ajoutee !");
    }

    // 🔵 AFFICHER TOUT
    public ArrayList<SeanceSport> afficherAll() throws SQLException {
        ArrayList<SeanceSport> liste = new ArrayList<>();
        Statement ste = conn.createStatement();
        ResultSet rs = ste.executeQuery("SELECT * FROM seance_sport");
        while (rs.next()) {
            liste.add(new SeanceSport(
                    rs.getInt("id"),
                    rs.getString("nom_seance"),
                    rs.getString("heure_debut"),
                    rs.getInt("duree_minutes"),
                    rs.getString("medaille_obtenue"),
                    rs.getString("date_seance"),
                    rs.getInt("utilisateur_id"),
                    rs.getString("heure_debut_reelle"),
                    rs.getInt("alerte_envoyee")
            ));
        }
        rs.close();
        ste.close();
        return liste;
    }

    // 🟡 MODIFIER
    public void update(SeanceSport s) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "UPDATE seance_sport SET nom_seance=?, heure_debut=?, duree_minutes=?, " +
                        "medaille_obtenue=?, date_seance=?, utilisateur_id=?, " +
                        "heure_debut_reelle=?, alerte_envoyee=? WHERE id=?"
        );
        ps.setString(1, s.nomSeance);
        ps.setString(2, s.heureDebut);
        ps.setInt(3, s.dureeMinutes);
        ps.setString(4, s.medailleObtenue);
        ps.setString(5, s.dateSeance);
        ps.setInt(6, s.utilisateurId);
        ps.setString(7, s.heureDebutReelle);
        ps.setInt(8, s.alerteEnvoyee);
        ps.setInt(9, s.id);
        ps.executeUpdate();
        System.out.println("✅ Seance modifiee !");
    }

    // 🔴 SUPPRIMER
    public void delete(int id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM seance_sport WHERE id=?"
        );
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("✅ Seance supprimee !");
    }

    // 🔍 RECHERCHER par nom
    public ArrayList<SeanceSport> rechercherParNom(String nom) throws SQLException {
        ArrayList<SeanceSport> liste = new ArrayList<>();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM seance_sport WHERE nom_seance = ?"
        );
        ps.setString(1, nom);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            liste.add(new SeanceSport(
                    rs.getInt("id"),
                    rs.getString("nom_seance"),
                    rs.getString("heure_debut"),
                    rs.getInt("duree_minutes"),
                    rs.getString("medaille_obtenue"),
                    rs.getString("date_seance"),
                    rs.getInt("utilisateur_id"),
                    rs.getString("heure_debut_reelle"),
                    rs.getInt("alerte_envoyee")
            ));
        }
        return liste;
    }

    // 🔍 RECHERCHER par utilisateur
    public ArrayList<SeanceSport> rechercherParUtilisateur(int userId) throws SQLException {
        ArrayList<SeanceSport> liste = new ArrayList<>();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM seance_sport WHERE utilisateur_id = ?"
        );
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            liste.add(new SeanceSport(
                    rs.getInt("id"),
                    rs.getString("nom_seance"),
                    rs.getString("heure_debut"),
                    rs.getInt("duree_minutes"),
                    rs.getString("medaille_obtenue"),
                    rs.getString("date_seance"),
                    rs.getInt("utilisateur_id"),
                    rs.getString("heure_debut_reelle"),
                    rs.getInt("alerte_envoyee")
            ));
        }
        return liste;
    }
}
