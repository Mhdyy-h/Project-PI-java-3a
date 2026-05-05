package org.example.service;

import org.example.dao.DatabaseConnection;
import org.example.model.User;
import org.json.JSONObject;

import java.sql.*;

public class ProfilAthleteService {

    private final Connection conn;

    public ProfilAthleteService() {
        this.conn = DatabaseConnection.getConnection();
    }

    // Sauvegarder ou mettre à jour le profil
    public boolean sauvegarder(int userId, JSONObject profil) {
        try {
            // Vérifie si profil existe déjà
            PreparedStatement check = conn.prepareStatement(
                    "SELECT id FROM profil_athlete WHERE user_id = ?");
            check.setInt(1, userId);
            ResultSet rs = check.executeQuery();
            boolean existe = rs.next();
            rs.close(); check.close();

            PreparedStatement ps;
            if (existe) {
                ps = conn.prepareStatement(
                        "UPDATE profil_athlete SET " +
                                "age=?, poids_kg=?, taille_cm=?, sexe=?, " +
                                "historique_medical=?, blessures=?, medicaments=?, " +
                                "objectif=?, niveau_sport=?, disponibilite_semaine=?, " +
                                "etat_emotionnel=?, niveau_stress=?, qualite_sommeil=?, " +
                                "alimentation=?, date_mise_a_jour=NOW() " +
                                "WHERE user_id=?");
            } else {
                ps = conn.prepareStatement(
                        "INSERT INTO profil_athlete " +
                                "(age, poids_kg, taille_cm, sexe, " +
                                "historique_medical, blessures, medicaments, " +
                                "objectif, niveau_sport, disponibilite_semaine, " +
                                "etat_emotionnel, niveau_stress, qualite_sommeil, " +
                                "alimentation, user_id) " +
                                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            }

            ps.setInt(1,    profil.optInt("age"));
            ps.setDouble(2, profil.optDouble("poids_kg"));
            ps.setDouble(3, profil.optDouble("taille_cm"));
            ps.setString(4, profil.optString("sexe"));
            ps.setString(5, profil.optString("historique_medical"));
            ps.setString(6, profil.optString("blessures"));
            ps.setString(7, profil.optString("medicaments"));
            ps.setString(8, profil.optString("objectif"));
            ps.setString(9, profil.optString("niveau_sport"));
            ps.setInt(10,   profil.optInt("disponibilite_semaine"));
            ps.setString(11,profil.optString("etat_emotionnel"));
            ps.setInt(12,   profil.optInt("niveau_stress"));
            ps.setInt(13,   profil.optInt("qualite_sommeil"));
            ps.setString(14,profil.optString("alimentation"));
            ps.setInt(15,   userId);

            int result = ps.executeUpdate();
            ps.close();
            return result > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Charger le profil existant
    public JSONObject charger(int userId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM profil_athlete WHERE user_id = ?");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                JSONObject profil = new JSONObject()
                        .put("age",                   rs.getInt("age"))
                        .put("poids_kg",              rs.getDouble("poids_kg"))
                        .put("taille_cm",             rs.getDouble("taille_cm"))
                        .put("sexe",                  rs.getString("sexe"))
                        .put("historique_medical",    rs.getString("historique_medical"))
                        .put("blessures",             rs.getString("blessures"))
                        .put("medicaments",           rs.getString("medicaments"))
                        .put("objectif",              rs.getString("objectif"))
                        .put("niveau_sport",          rs.getString("niveau_sport"))
                        .put("disponibilite_semaine", rs.getInt("disponibilite_semaine"))
                        .put("etat_emotionnel",       rs.getString("etat_emotionnel"))
                        .put("niveau_stress",         rs.getInt("niveau_stress"))
                        .put("qualite_sommeil",       rs.getInt("qualite_sommeil"))
                        .put("alimentation",          rs.getString("alimentation"));
                rs.close(); ps.close();
                return profil;
            }
            rs.close(); ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new JSONObject(); // profil vide si pas encore rempli
    }
}