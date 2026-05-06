package org.example.dao;

import org.example.DatabaseConnection;
import org.example.model.Alerte;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AlerteDAO {

    // Créer la table alerte si elle n'existe pas
    public static void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS alerte (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "type VARCHAR(50) NOT NULL," +
                "message TEXT NOT NULL," +
                "date_alerte DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "criticite ENUM('JAUNE', 'ROUGE') DEFAULT 'JAUNE'," +
                "utilisateur_id INT NOT NULL," +
                "repas_id INT," +
                "FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE," +
                "FOREIGN KEY (repas_id) REFERENCES repas(id) ON DELETE CASCADE" +
                ")";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Table 'alerte' créée ou existe déjà");
        } catch (SQLException e) {
            System.err.println("Erreur création table alerte: " + e.getMessage());
        }
    }

    // Insérer une alerte
    public static boolean insert(Alerte alerte) {
        createTableIfNotExists();
        String sql = "INSERT INTO alerte (type, message, criticite, utilisateur_id, repas_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, alerte.getType());
            pstmt.setString(2, alerte.getMessage());
            pstmt.setString(3, alerte.getCriticite());
            pstmt.setInt(4, alerte.getUtilisateurId());
            if (alerte.getRepasId() != null) {
                pstmt.setInt(5, alerte.getRepasId());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    alerte.setId(rs.getInt(1));
                }
                rs.close();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur insertion alerte: " + e.getMessage());
        }
        return false;
    }

    // Récupérer les alertes d'un utilisateur
    public static List<Alerte> getByUtilisateurId(int utilisateurId) {
        List<Alerte> alertes = new ArrayList<>();
        String sql = "SELECT * FROM alerte WHERE utilisateur_id = ? ORDER BY date_alerte DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, utilisateurId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                alertes.add(extractAlerteFromResultSet(rs));
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur récupération alertes: " + e.getMessage());
        }
        return alertes;
    }

    // Récupérer les alertes non lues (7 derniers jours)
    public static List<Alerte> getRecentByUtilisateurId(int utilisateurId) {
        List<Alerte> alertes = new ArrayList<>();
        String sql = "SELECT * FROM alerte WHERE utilisateur_id = ? AND date_alerte > DATE_SUB(NOW(), INTERVAL 7 DAY) ORDER BY date_alerte DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, utilisateurId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                alertes.add(extractAlerteFromResultSet(rs));
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur récupération alertes récentes: " + e.getMessage());
        }
        return alertes;
    }

    // Vérifier si une alerte existe déjà pour un repas
    public static boolean existsForRepas(int repasId, String type) {
        String sql = "SELECT COUNT(*) FROM alerte WHERE repas_id = ? AND type = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, repasId);
            pstmt.setString(2, type);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur vérification alerte: " + e.getMessage());
        }
        return false;
    }

    // Compter les alertes récentes d'un utilisateur par type
    public static int countRecentByType(int utilisateurId, String type, int days) {
        String sql = "SELECT COUNT(*) FROM alerte WHERE utilisateur_id = ? AND type = ? AND date_alerte > DATE_SUB(NOW(), INTERVAL ? DAY)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, utilisateurId);
            pstmt.setString(2, type);
            pstmt.setInt(3, days);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur comptage alertes: " + e.getMessage());
        }
        return 0;
    }

    // Récupérer toutes les alertes récentes (toutes plateformes – pour admin dashboard)
    public static List<Alerte> getAllRecent(int limitDays) {
        List<Alerte> alertes = new ArrayList<>();
        String sql = "SELECT * FROM alerte WHERE date_alerte > DATE_SUB(NOW(), INTERVAL ? DAY) ORDER BY date_alerte DESC LIMIT 20";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limitDays);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                alertes.add(extractAlerteFromResultSet(rs));
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur récupération toutes alertes: " + e.getMessage());
        }
        return alertes;
    }

    // Compter toutes les alertes actives (toutes plateformes)
    public static int countAllRecent(int limitDays) {
        String sql = "SELECT COUNT(*) FROM alerte WHERE date_alerte > DATE_SUB(NOW(), INTERVAL ? DAY)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limitDays);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur comptage alertes: " + e.getMessage());
        }
        return 0;
    }

    // Supprimer une alerte
    public static boolean delete(int id) {
        String sql = "DELETE FROM alerte WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression alerte: " + e.getMessage());
            return false;
        }
    }

    private static Alerte extractAlerteFromResultSet(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("date_alerte");
        LocalDateTime dateAlerte = timestamp != null ? timestamp.toLocalDateTime() : LocalDateTime.now();
        Integer repasId = rs.getObject("repas_id") != null ? rs.getInt("repas_id") : null;

        return new Alerte(
                rs.getInt("id"),
                rs.getString("type"),
                rs.getString("message"),
                dateAlerte,
                rs.getString("criticite"),
                rs.getInt("utilisateur_id"),
                repasId
        );
    }
}