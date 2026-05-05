package org.example.service;

import org.example.dao.DatabaseConnection;
import org.example.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageService {

    private final Connection conn;

    public MessageService() {
        this.conn = DatabaseConnection.getConnection();
    }

    // Envoyer un message
    public boolean envoyerMessage(int expediteurId, int destinataireId, String contenu) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO messages (expediteur_id, destinataire_id, contenu) VALUES (?, ?, ?)");
            ps.setInt(1, expediteurId);
            ps.setInt(2, destinataireId);
            ps.setString(3, contenu);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Récupérer la conversation entre 2 personnes
    public List<org.json.JSONObject> getConversation(int userId1, int userId2) {
        List<org.json.JSONObject> liste = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM messages " +
                            "WHERE (expediteur_id = ? AND destinataire_id = ?) " +
                            "OR (expediteur_id = ? AND destinataire_id = ?) " +
                            "ORDER BY date_envoi ASC");
            ps.setInt(1, userId1); ps.setInt(2, userId2);
            ps.setInt(3, userId2); ps.setInt(4, userId1);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(new org.json.JSONObject()
                        .put("id",              rs.getInt("id"))
                        .put("expediteur_id",   rs.getInt("expediteur_id"))
                        .put("destinataire_id", rs.getInt("destinataire_id"))
                        .put("contenu",         rs.getString("contenu"))
                        .put("date_envoi",      rs.getString("date_envoi"))
                        .put("lu",              rs.getBoolean("lu")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return liste;
    }

    // Compter messages non lus
    public int compterNonLus(int destinataireId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM messages WHERE destinataire_id = ? AND lu = FALSE");
            ps.setInt(1, destinataireId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Marquer comme lus
    public void marquerLus(int expediteurId, int destinataireId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE messages SET lu = TRUE " +
                            "WHERE expediteur_id = ? AND destinataire_id = ?");
            ps.setInt(1, expediteurId);
            ps.setInt(2, destinataireId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }public int compterNonLusDepuis(int expediteurId, int destinataireId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM messages " +
                            "WHERE expediteur_id = ? AND destinataire_id = ? AND lu = FALSE");
            ps.setInt(1, expediteurId);
            ps.setInt(2, destinataireId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }
}