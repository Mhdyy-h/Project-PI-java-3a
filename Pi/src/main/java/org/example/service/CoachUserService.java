package org.example.service;

import org.example.dao.DatabaseConnection;
import org.example.model.User;
import org.example.model.SeanceSport;
import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoachUserService {

    private final Connection conn;

    public CoachUserService() {
        this.conn = DatabaseConnection.getConnection();
    }

    // ─────────────────────────────────────────
    // Coach assigne un User
    // ─────────────────────────────────────────
    public boolean assignerUser(int coachId, int userId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT IGNORE INTO coach_user (coach_id, user_id) VALUES (?, ?)");
            ps.setInt(1, coachId);
            ps.setInt(2, userId);
            int result = ps.executeUpdate();
            ps.close();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ─────────────────────────────────────────
    // Coach récupère sa liste de Users
    // ─────────────────────────────────────────
    public List<User> getUsersDuCoach(int coachId) {
        List<User> liste = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT u.* FROM utilisateur u " +
                            "JOIN coach_user cu ON u.id = cu.user_id " +
                            "WHERE cu.coach_id = ?");
            ps.setInt(1, coachId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(new User(
                        rs.getInt("id"),
                        rs.getString("nom_complet"),
                        rs.getString("email")
                ));
            }
            rs.close(); ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

    // ─────────────────────────────────────────
    // Récupère tous les Users sans coach
    // ─────────────────────────────────────────
    public List<User> getUsersSansCoach() {
        List<User> liste = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM utilisateur " +
                            "WHERE roles LIKE '%ROLE_USER%' " +
                            "AND id NOT IN (SELECT user_id FROM coach_user)");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(new User(
                        rs.getInt("id"),
                        rs.getString("nom_complet"),
                        rs.getString("email")
                ));
            }
            rs.close(); ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

    // ─────────────────────────────────────────
    // Supprimer lien Coach ↔ User
    // ─────────────────────────────────────────
    public boolean retirerUser(int coachId, int userId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM coach_user WHERE coach_id = ? AND user_id = ?");
            ps.setInt(1, coachId);
            ps.setInt(2, userId);
            int result = ps.executeUpdate();
            ps.close();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ─────────────────────────────────────────
    // Récupérer séances d'un user
    // ─────────────────────────────────────────
    public List<SeanceSport> getSeancesUser(int userId) {
        List<SeanceSport> liste = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM seance_sport WHERE utilisateur_id = ? " +
                            "ORDER BY date_seance DESC");
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
            rs.close(); ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

    // ─────────────────────────────────────────
    // Sauvegarder recommandation ✅
    // ─────────────────────────────────────────
    public boolean sauvegarderRecommandation(int coachId, int userId,
                                             String titre, String message, String exercicesJson,
                                             String nutrition, String planSemaine) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO recommandations " +
                            "(coach_id, user_id, titre, message, exercices_json, nutrition, plan_semaine) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)");
            ps.setInt(1, coachId);
            ps.setInt(2, userId);
            ps.setString(3, titre);
            ps.setString(4, message);
            ps.setString(5, exercicesJson);
            ps.setString(6, nutrition);
            ps.setString(7, planSemaine);
            int result = ps.executeUpdate();
            ps.close();
            return result > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() != 1146) e.printStackTrace();
            return false;
        }
    }

    // ─────────────────────────────────────────
    // Récupérer recommandations ✅
    // ─────────────────────────────────────────
    public List<JSONObject> getRecommandationsUser(int userId) {
        List<JSONObject> liste = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM recommandations " +
                            "WHERE user_id = ? " +
                            "ORDER BY date_creation DESC");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(new JSONObject()
                        .put("id",           rs.getInt("id"))
                        .put("user_id",      rs.getInt("user_id"))
                        .put("titre",        rs.getString("titre"))
                        .put("message",      rs.getString("message"))
                        .put("exercices",    rs.getString("exercices_json"))
                        .put("plan_semaine", rs.getString("plan_semaine") != null ? rs.getString("plan_semaine") : "")
                        .put("nutrition",    rs.getString("nutrition")    != null ? rs.getString("nutrition")    : "")
                        .put("date",         rs.getString("date_creation"))
                        .put("vue",          rs.getBoolean("vue"))
                        .put("reponse_user", rs.getString("reponse_user") != null ? rs.getString("reponse_user") : "")
                        .put("date_reponse", rs.getString("date_reponse") != null ? rs.getString("date_reponse") : ""));
            }
            rs.close(); ps.close();
        } catch (SQLException e) {
            if (e.getErrorCode() != 1146) e.printStackTrace();
        }
        return liste;
    }

    // ─────────────────────────────────────────
    // Marquer recommandation comme vue
    // ─────────────────────────────────────────
    public void marquerVue(int recommandationId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE recommandations SET vue = TRUE WHERE id = ?");
            ps.setInt(1, recommandationId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            if (e.getErrorCode() != 1146) e.printStackTrace();
        }
    }

    // Compter recommandations non vues
    public int compterNonVues(int userId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM recommandations " +
                            "WHERE user_id = ? AND vue = FALSE");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
            rs.close(); ps.close();
        } catch (SQLException e) {
            if (e.getErrorCode() != 1146) e.printStackTrace();
        }
        return 0;
    }

    // Vérifier si la dernière recommandation envoyée à un user a été vue
    public boolean derniereRecoVue(int coachId, int userId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT vue FROM recommandations " +
                            "WHERE coach_id = ? AND user_id = ? " +
                            "ORDER BY date_creation DESC LIMIT 1");
            ps.setInt(1, coachId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBoolean("vue");
            rs.close(); ps.close();
        } catch (SQLException e) {
            if (e.getErrorCode() != 1146) e.printStackTrace();
        }
        return false; // pas encore vue ou aucune reco
    }

    // User envoie sa réponse au coach
    public boolean envoyerReponseUser(int recoId, String reponse) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE recommandations SET reponse_user = ?, date_reponse = NOW() WHERE id = ?");
            ps.setString(1, reponse);
            ps.setInt(2, recoId);
            int result = ps.executeUpdate();
            ps.close();
            return result > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() != 1146) e.printStackTrace();
            return false;
        }
    }

    public JSONObject getDerniereRecommandation(int coachId, int userId) {
        String sql = "SELECT reponse_user, date_reponse FROM recommandations " +
                "WHERE coach_id = ? AND user_id = ? " +
                "ORDER BY date_creation DESC LIMIT 1";
        try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
            ps.setInt(1, coachId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new JSONObject()
                        .put("reponse_user", rs.getString("reponse_user") != null ? rs.getString("reponse_user") : "")
                        .put("date_reponse", rs.getString("date_reponse") != null ? rs.getString("date_reponse") : "");
            }
        } catch (Exception e) {
            if (e instanceof SQLException && ((SQLException) e).getErrorCode() != 1146) e.printStackTrace();
        }
        return null;
    }

    // NOUVEAU — colle ça à la place
    public List<User> getContactsMessage(int userId) {
        List<User> liste = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT DISTINCT u.* FROM utilisateur u " +
                            "JOIN coach_user cu ON (cu.coach_id = u.id OR cu.user_id = u.id) " +
                            "WHERE (cu.coach_id = ? OR cu.user_id = ?) AND u.id != ?");
            ps.setInt(1, userId); ps.setInt(2, userId); ps.setInt(3, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                User u = new User(
                        rs.getInt("id"),
                        rs.getString("nom_complet"),
                        rs.getString("email")
                );
                u.setRoles(rs.getString("roles"));
                liste.add(u);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return liste;
    }}