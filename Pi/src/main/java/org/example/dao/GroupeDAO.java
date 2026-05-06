package org.example.dao;

import org.example.DatabaseConnection;
import org.example.model.Groupe;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupeDAO {

    public static List<Groupe> getAllGroups() throws SQLException {
        List<Groupe> list = new ArrayList<>();
        // Note: Using your exact DB column names from the screenshot
        String sql = "SELECT id, nom_groupe, thematique, capacite_max, description, image FROM groupe_soutien ORDER BY id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Groupe g = new Groupe();
                g.setId(rs.getInt("id"));
                g.setNomGroupe(rs.getString("nom_groupe"));
                g.setThematique(rs.getString("thematique"));
                g.setCapaciteMax(rs.getInt("capacite_max"));
                g.setDescription(rs.getString("description"));
                g.setImage(rs.getString("image"));
                list.add(g);
            }
        }
        return list;
    }

    public static void create(Groupe g) throws SQLException {
        String sql = "INSERT INTO groupe_soutien (nom_groupe, thematique, description, capacite_max, image) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, g.getNomGroupe());
            ps.setString(2, g.getThematique());
            ps.setString(3, g.getDescription());
            ps.setInt(4, g.getCapaciteMax());
            ps.setString(5, g.getImage());
            ps.executeUpdate();
        }
    }

    public static void update(Groupe g) throws SQLException {
        String sql = "UPDATE groupe_soutien SET nom_groupe=?, thematique=?, description=?, capacite_max=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, g.getNomGroupe());
            ps.setString(2, g.getThematique());
            ps.setString(3, g.getDescription());
            ps.setInt(4, g.getCapaciteMax());
            ps.setInt(5, g.getId());
            ps.executeUpdate();
        }
    }

    public static void delete(int id) throws SQLException {
        String sql = "DELETE FROM groupe_soutien WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public static boolean nameExists(String nom, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM groupe_soutien WHERE nom_groupe = ? AND id != ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }
}