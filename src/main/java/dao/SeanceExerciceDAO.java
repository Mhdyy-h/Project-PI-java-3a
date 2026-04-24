package dao;

import model.Exercice;
import model.SeanceExercice;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeanceExerciceDAO {

    private Connection conn;

    public SeanceExerciceDAO(Connection conn) {
        this.conn = conn;
    }

    public void ajouter(SeanceExercice se) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO seance_exercice (seance_id, exercice_id, ordre, series, repetitions) VALUES (?, ?, ?, ?, ?)"
        );
        ps.setInt(1, se.getSeanceId());
        ps.setInt(2, se.getExerciceId());
        ps.setInt(3, se.getOrdre());
        ps.setInt(4, se.getSeries());
        ps.setInt(5, se.getRepetitions());
        ps.executeUpdate();
    }

    public List<SeanceExercice> getParSeance(int seanceId) throws SQLException {
        List<SeanceExercice> liste = new ArrayList<>();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT se.*, e.nom_exercice, e.intensite, e.calories_par_minute " +
                        "FROM seance_exercice se " +
                        "JOIN exercice e ON se.exercice_id = e.id " +
                        "WHERE se.seance_id = ? ORDER BY se.ordre ASC"
        );
        ps.setInt(1, seanceId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            SeanceExercice se = new SeanceExercice();
            se.setId(rs.getInt("id"));
            se.setSeanceId(rs.getInt("seance_id"));
            se.setExerciceId(rs.getInt("exercice_id"));
            se.setOrdre(rs.getInt("ordre"));
            se.setSeries(rs.getInt("series"));
            se.setRepetitions(rs.getInt("repetitions"));

            Exercice ex = new Exercice(
                    rs.getInt("exercice_id"),
                    rs.getString("nom_exercice"),
                    rs.getString("intensite"),
                    rs.getDouble("calories_par_minute"),
                    seanceId
            );
            se.setExercice(ex);
            liste.add(se);
        }
        return liste;
    }

    public void supprimer(int seanceId, int exerciceId) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM seance_exercice WHERE seance_id = ? AND exercice_id = ?"
        );
        ps.setInt(1, seanceId);
        ps.setInt(2, exerciceId);
        ps.executeUpdate();
    }
}