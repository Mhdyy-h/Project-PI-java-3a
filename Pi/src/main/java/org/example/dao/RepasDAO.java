package org.example.dao;

import org.example.DatabaseConnection;
import org.example.model.Repas;
import org.example.model.Aliment;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RepasDAO {

    // Créer les tables repas et repas_aliments si elles n'existent pas
    public static void createTablesIfNotExists() {
        String sqlRepas = "CREATE TABLE IF NOT EXISTS repas (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "titre_repas VARCHAR(100) NOT NULL," +
                "type_moment ENUM('MATIN', 'MIDI', 'COLLATION', 'SOIR') NOT NULL," +
                "date_consommation DATETIME NOT NULL," +
                "points_gagnes INT DEFAULT 0," +
                "utilisateur_id INT NOT NULL," +
                "FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE" +
                ")";

        String sqlRepasAliments = "CREATE TABLE IF NOT EXISTS repas_aliments (" +
                "repas_id INT NOT NULL," +
                "aliment_id INT NOT NULL," +
                "quantite INT DEFAULT 1," +
                "FOREIGN KEY (repas_id) REFERENCES repas(id) ON DELETE CASCADE," +
                "FOREIGN KEY (aliment_id) REFERENCES aliment(id) ON DELETE CASCADE," +
                "PRIMARY KEY (repas_id, aliment_id)" +
                ")";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlRepas);
            stmt.execute(sqlRepasAliments);
            System.out.println("Tables 'repas' et 'repas_aliments' créées ou existent déjà");
        } catch (SQLException e) {
            System.err.println("Erreur création tables repas: " + e.getMessage());
        }
    }

    // Insérer un repas
    public static boolean insert(Repas repas) {
        createTablesIfNotExists();
        String sql = "INSERT INTO repas (titre_repas, type_moment, date_consommation, points_gagnes, utilisateur_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, repas.getTitreRepas());
            pstmt.setString(2, repas.getTypeMoment());
            pstmt.setTimestamp(3, Timestamp.valueOf(repas.getDateConsommation()));
            pstmt.setInt(4, repas.getPointsGagnes());
            pstmt.setInt(5, repas.getUtilisateurId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    repas.setId(rs.getInt(1));
                }
                rs.close();
                // Insérer les aliments du repas
                insertRepasAliments(repas);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur insertion repas: " + e.getMessage());
        }
        return false;
    }

    // Insérer les aliments d'un repas
    private static void insertRepasAliments(Repas repas) throws SQLException {
        String sql = "INSERT INTO repas_aliments (repas_id, aliment_id, quantite) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            for (int i = 0; i < repas.getAliments().size(); i++) {
                pstmt.setInt(1, repas.getId());
                pstmt.setInt(2, repas.getAliments().get(i).getId());
                pstmt.setInt(3, repas.getQuantites().get(i));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    // Récupérer tous les repas d'un utilisateur
    public static List<Repas> getByUtilisateurId(int utilisateurId) {
        List<Repas> repasList = new ArrayList<>();
        String sql = "SELECT * FROM repas WHERE utilisateur_id = ? ORDER BY date_consommation DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, utilisateurId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Repas repas = extractRepasFromResultSet(rs);
                loadAlimentsForRepas(repas);
                repasList.add(repas);
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur récupération repas: " + e.getMessage());
        }
        return repasList;
    }

    // Récupérer les repas d'un utilisateur par date
    public static List<Repas> getByUtilisateurAndDate(int utilisateurId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Repas> repasList = new ArrayList<>();
        String sql = "SELECT * FROM repas WHERE utilisateur_id = ? AND date_consommation BETWEEN ? AND ? ORDER BY date_consommation DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, utilisateurId);
            pstmt.setTimestamp(2, Timestamp.valueOf(startDate));
            pstmt.setTimestamp(3, Timestamp.valueOf(endDate));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Repas repas = extractRepasFromResultSet(rs);
                loadAlimentsForRepas(repas);
                repasList.add(repas);
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur récupération repas par date: " + e.getMessage());
        }
        return repasList;
    }

    // Récupérer les repas du jour pour un utilisateur
    public static List<Repas> getTodayRepas(int utilisateurId) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        return getByUtilisateurAndDate(utilisateurId, startOfDay, endOfDay);
    }

    // Récupérer un repas par ID
    public static Repas getById(int id) {
        String sql = "SELECT * FROM repas WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Repas repas = extractRepasFromResultSet(rs);
                loadAlimentsForRepas(repas);
                rs.close();
                return repas;
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur récupération repas: " + e.getMessage());
        }
        return null;
    }

    // Mettre à jour un repas
    public static boolean update(Repas repas) {
        String sql = "UPDATE repas SET titre_repas = ?, type_moment = ?, date_consommation = ?, points_gagnes = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, repas.getTitreRepas());
            pstmt.setString(2, repas.getTypeMoment());
            pstmt.setTimestamp(3, Timestamp.valueOf(repas.getDateConsommation()));
            pstmt.setInt(4, repas.getPointsGagnes());
            pstmt.setInt(5, repas.getId());

            boolean updated = pstmt.executeUpdate() > 0;
            if (updated) {
                // Supprimer les anciens aliments et réinsérer
                deleteRepasAliments(repas.getId());
                insertRepasAliments(repas);
            }
            return updated;
        } catch (SQLException e) {
            System.err.println("Erreur mise à jour repas: " + e.getMessage());
            return false;
        }
    }

    // Supprimer un repas
    public static boolean delete(int id) {
        // Les aliments seront supprimés automatiquement par CASCADE
        String sql = "DELETE FROM repas WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression repas: " + e.getMessage());
            return false;
        }
    }

    private static void deleteRepasAliments(int repasId) throws SQLException {
        String sql = "DELETE FROM repas_aliments WHERE repas_id = ?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, repasId);
            pstmt.executeUpdate();
        }
    }

    private static void loadAlimentsForRepas(Repas repas) throws SQLException {
        String sql = "SELECT a.*, ra.quantite FROM aliment a " +
                "JOIN repas_aliments ra ON a.id = ra.aliment_id " +
                "WHERE ra.repas_id = ?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, repas.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Aliment aliment = AlimentDAO.extractAlimentFromResultSet(rs); // Besoin de rendre cette méthode publique dans AlimentDAO
                int quantite = rs.getInt("quantite");
                repas.getAliments().add(aliment);
                repas.getQuantites().add(quantite);
            }
            rs.close();
        }
    }

    private static Repas extractRepasFromResultSet(ResultSet rs) throws SQLException {
        return new Repas(
                rs.getInt("id"),
                rs.getString("titre_repas"),
                rs.getString("type_moment"),
                rs.getTimestamp("date_consommation").toLocalDateTime(),
                rs.getInt("points_gagnes"),
                rs.getInt("utilisateur_id")
        );
    }

    // Compter tous les repas de la plateforme (pour le dashboard admin)
    public static int countAll() {
        String sql = "SELECT COUNT(*) FROM repas";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur comptage repas: " + e.getMessage());
        }
        return 0;
    }

    // Compter les repas enregistrés aujourd'hui (toute la plateforme)
    public static int countToday() {
        String sql = "SELECT COUNT(*) FROM repas WHERE DATE(date_consommation) = CURDATE()";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur comptage repas du jour: " + e.getMessage());
        }
        return 0;
    }
}