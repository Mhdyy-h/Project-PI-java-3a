package org.example.dao;

import org.example.DatabaseConnection;
import org.example.model.Evenement;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvenementDAO {

    public static List<Evenement> getAllEvents() throws SQLException {
        List<Evenement> list = new ArrayList<>();
        String sql = "SELECT * FROM evenement_sante ORDER BY date_event ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Evenement(
                        rs.getInt("id"), rs.getString("titre_event"),
                        rs.getTimestamp("date_event"), rs.getInt("points_participation"),
                        rs.getInt("groupe_id"), rs.getString("location_name"),
                        rs.getString("address")
                ));
            }
        }
        return list;
    }

    public static List<Evenement> getEventsByGroupId(int groupId) throws SQLException {
        List<Evenement> list = new ArrayList<>();
        String sql = "SELECT * FROM evenement_sante WHERE groupe_id = ? ORDER BY date_event ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Evenement(
                            rs.getInt("id"), rs.getString("titre_event"),
                            rs.getTimestamp("date_event"), rs.getInt("points_participation"),
                            rs.getInt("groupe_id"), rs.getString("location_name"),
                            rs.getString("address")
                    ));
                }
            }
        }
        return list;
    }

    public static void create(Evenement e) throws SQLException {
        String sql = "INSERT INTO evenement_sante (titre_event, date_event, points_participation, groupe_id, location_name, address) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getTitreEvent());
            ps.setTimestamp(2, e.getDateEvent());
            ps.setInt(3, e.getPointsParticipation());
            ps.setInt(4, e.getGroupeId());
            ps.setString(5, e.getLocationName());
            ps.setString(6, e.getAddress());
            ps.executeUpdate();
        }
    }

    public static void update(Evenement e) throws SQLException {
        String sql = "UPDATE evenement_sante SET titre_event=?, date_event=?, points_participation=?, location_name=?, address=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getTitreEvent());
            ps.setTimestamp(2, e.getDateEvent());
            ps.setInt(3, e.getPointsParticipation());
            ps.setString(4, e.getLocationName());
            ps.setString(5, e.getAddress());
            ps.setInt(6, e.getId());
            ps.executeUpdate();
        }
    }

    public static void delete(int id) throws SQLException {
        String sql = "DELETE FROM evenement_sante WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}