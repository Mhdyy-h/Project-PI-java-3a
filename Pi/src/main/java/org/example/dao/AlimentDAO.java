package org.example.dao;

import org.example.DatabaseConnection;
import org.example.model.Aliment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlimentDAO {

    // Créer la table aliment si elle n'existe pas
    public static void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS aliment (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "nom_aliment VARCHAR(100) NOT NULL," +
                "calories INT NOT NULL," +
                "proteines DECIMAL(5,1) DEFAULT 0," +
                "glucides DECIMAL(5,1) DEFAULT 0," +
                "lipides DECIMAL(5,1) DEFAULT 0," +
                "index_glycemique INT DEFAULT 0," +
                "est_excitant BOOLEAN DEFAULT FALSE," +
                "type_aliment VARCHAR(50)," +
                "multi_score VARCHAR(10)," +
                "nutri_score VARCHAR(2)" +
                ")";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Table 'aliment' créée ou existe déjà");
        } catch (SQLException e) {
            System.err.println("Erreur création table aliment: " + e.getMessage());
        }
    }

    // Insérer un aliment
    public static boolean insert(Aliment aliment) {
        createTableIfNotExists();
        String sql = "INSERT INTO aliment (nom_aliment, calories, proteines, glucides, lipides, " +
                "index_glycemique, est_excitant, type_aliment, multi_score, nutri_score) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, aliment.getNomAliment());
            pstmt.setInt(2, aliment.getCalories());
            pstmt.setDouble(3, aliment.getProteines());
            pstmt.setDouble(4, aliment.getGlucides());
            pstmt.setDouble(5, aliment.getLipides());
            pstmt.setInt(6, aliment.getIndexGlycemique());
            pstmt.setBoolean(7, aliment.isEstExcitant());
            pstmt.setString(8, aliment.getTypeAliment());
            pstmt.setString(9, aliment.getMultiScore());
            pstmt.setString(10, aliment.getNutriScore());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    aliment.setId(rs.getInt(1));
                }
                rs.close();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur insertion aliment: " + e.getMessage());
        }
        return false;
    }

    // Récupérer tous les aliments
    public static List<Aliment> getAll() {
        List<Aliment> aliments = new ArrayList<>();
        String sql = "SELECT * FROM aliment ORDER BY nom_aliment";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                aliments.add(extractAlimentFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération aliments: " + e.getMessage());
        }
        return aliments;
    }

    // Récupérer un aliment par ID
    public static Aliment getById(int id) {
        String sql = "SELECT * FROM aliment WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractAlimentFromResultSet(rs);
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur récupération aliment: " + e.getMessage());
        }
        return null;
    }

    // Rechercher des aliments par nom
    public static List<Aliment> searchByNom(String searchTerm) {
        List<Aliment> aliments = new ArrayList<>();
        String sql = "SELECT * FROM aliment WHERE nom_aliment LIKE ? ORDER BY nom_aliment";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                aliments.add(extractAlimentFromResultSet(rs));
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur recherche aliment: " + e.getMessage());
        }
        return aliments;
    }

    // Mettre à jour un aliment
    public static boolean update(Aliment aliment) {
        String sql = "UPDATE aliment SET nom_aliment = ?, calories = ?, proteines = ?, " +
                "glucides = ?, lipides = ?, index_glycemique = ?, est_excitant = ?, " +
                "type_aliment = ?, multi_score = ?, nutri_score = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, aliment.getNomAliment());
            pstmt.setInt(2, aliment.getCalories());
            pstmt.setDouble(3, aliment.getProteines());
            pstmt.setDouble(4, aliment.getGlucides());
            pstmt.setDouble(5, aliment.getLipides());
            pstmt.setInt(6, aliment.getIndexGlycemique());
            pstmt.setBoolean(7, aliment.isEstExcitant());
            pstmt.setString(8, aliment.getTypeAliment());
            pstmt.setString(9, aliment.getMultiScore());
            pstmt.setString(10, aliment.getNutriScore());
            pstmt.setInt(11, aliment.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur mise à jour aliment: " + e.getMessage());
            return false;
        }
    }

    // Supprimer un aliment
    public static boolean delete(int id) {
        String sql = "DELETE FROM aliment WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression aliment: " + e.getMessage());
            return false;
        }
    }

    // Récupérer les aliments excitants
    public static List<Aliment> getExcitants() {
        List<Aliment> excitants = new ArrayList<>();
        String sql = "SELECT * FROM aliment WHERE est_excitant = TRUE ORDER BY nom_aliment";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                excitants.add(extractAlimentFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération aliments excitants: " + e.getMessage());
        }
        return excitants;
    }


        public static Aliment extractAlimentFromResultSet(ResultSet rs) throws SQLException {
            return new Aliment(
                    rs.getInt("id"),
                    rs.getString("nom_aliment"),
                    rs.getInt("calories"),
                    rs.getDouble("proteines"),
                    rs.getDouble("glucides"),
                    rs.getDouble("lipides"),
                    rs.getInt("index_glycemique"),
                    rs.getBoolean("est_excitant"),
                    rs.getString("type_aliment"),
                    rs.getString("multi_score"),
                    rs.getString("nutri_score")
            );
        }
    }

