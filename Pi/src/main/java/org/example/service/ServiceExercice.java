package org.example.service;

import org.example.model.Exercice;
import org.example.dao.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;

public class ServiceExercice {

    Connection conn = DatabaseConnection.getConnection();

    // 🟢 AJOUTER
    public void ajouter(Exercice e) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO exercice (nom_exercice, intensite, calories_par_minute, seance_id) " +
                        "VALUES (?, ?, ?, ?)"
        );
        ps.setString(1, e.nomExercice);
        ps.setString(2, e.intensite);
        ps.setDouble(3, e.caloriesParMinute);
        ps.setInt(4, e.seanceId);
        ps.executeUpdate();
        System.out.println("✅ Exercice ajoute !");
    }

    // 🔵 AFFICHER TOUT
    public ArrayList<Exercice> afficherAll() throws SQLException {
        ArrayList<Exercice> liste = new ArrayList<>();
        Statement ste = conn.createStatement();
        ResultSet rs = ste.executeQuery("SELECT * FROM exercice");
        while (rs.next()) {
            liste.add(new Exercice(
                    rs.getInt("id"),
                    rs.getString("nom_exercice"),
                    rs.getString("intensite"),
                    rs.getDouble("calories_par_minute"),
                    rs.getInt("seance_id")
            ));
        }
        rs.close();
        ste.close();
        return liste;
    }

    // 🟡 MODIFIER
    public void update(Exercice e) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "UPDATE exercice SET nom_exercice=?, intensite=?, " +
                        "calories_par_minute=?, seance_id=? WHERE id=?"
        );
        ps.setString(1, e.nomExercice);
        ps.setString(2, e.intensite);
        ps.setDouble(3, e.caloriesParMinute);
        ps.setInt(4, e.seanceId);
        ps.setInt(5, e.id);
        ps.executeUpdate();
        System.out.println("✅ Exercice modifie !");
    }

    // 🔴 SUPPRIMER
    public void delete(int id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM exercice WHERE id=?"
        );
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("✅ Exercice supprime !");
    }

    // 🔍 TROUVER PAR ID
    public Exercice getById(int id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM exercice WHERE id = ?"
        );
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new Exercice(
                    rs.getInt("id"),
                    rs.getString("nom_exercice"),
                    rs.getString("intensite"),
                    rs.getDouble("calories_par_minute"),
                    rs.getInt("seance_id")
            );
        }
        return null;
    }
}