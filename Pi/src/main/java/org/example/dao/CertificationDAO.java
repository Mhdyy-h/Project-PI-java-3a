package org.example.dao;

import org.example.DatabaseConnection;
import org.example.model.CertificationRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CertificationDAO {

    public static int countPending() {
        String sql = "SELECT COUNT(*) FROM certification WHERE statut = 'EN_ATTENTE'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("❌ CertificationDAO.countPending: " + e.getMessage());
        }
        return 0;
    }

    public static boolean insertRequest(CertificationRequest req) {
        String sql = "INSERT INTO certification (nom_complet, email, specialite, motivation, statut) VALUES (?, ?, ?, ?, 'EN_ATTENTE')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, req.getNomComplet());
            ps.setString(2, req.getEmail());
            ps.setString(3, req.getSpecialite());
            ps.setString(4, req.getMotivation());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) req.setId(keys.getInt(1));
                }
            }
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ CertificationDAO.insertRequest: " + e.getMessage());
            return false;
        }
    }

    public static List<CertificationRequest> getAllRequests() {
        List<CertificationRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM certification ORDER BY id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new CertificationRequest(
                        rs.getInt("id"),
                        rs.getString("nom_complet"),
                        rs.getString("email"),
                        rs.getString("specialite"),
                        rs.getString("motivation"),
                        rs.getString("statut"),
                        null // Date extraction can be added here if column exists
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Updates the status of a request and changes the user's role if accepted.
     * Fixed: Connection handled inside try-catch to resolve SQLException error.
     */
    public static boolean updateStatut(int certId, String statut, String email, String newRole) {
        String sqlCert = "UPDATE certification SET statut = ? WHERE id = ?";
        String sqlUser = "UPDATE utilisateur SET roles = ? WHERE email = ?";

        // We open the connection inside the try block to handle the potential SQLException
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start Transaction

            try {
                // 1. Update certification status
                try (PreparedStatement ps1 = conn.prepareStatement(sqlCert)) {
                    ps1.setString(1, statut);
                    ps1.setInt(2, certId);
                    ps1.executeUpdate();
                }

                // 2. Update user role if approved
                if ("ACCEPTE".equals(statut) && email != null && newRole != null) {
                    try (PreparedStatement ps2 = conn.prepareStatement(sqlUser)) {
                        ps2.setString(1, newRole);
                        ps2.setString(2, email);
                        ps2.executeUpdate();
                    }
                }

                conn.commit(); // Save changes
                return true;

            } catch (SQLException e) {
                conn.rollback(); // Undo changes on error
                System.err.println("❌ Transaction failed, rolling back: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("❌ Database connection error: " + e.getMessage());
            return false;
        }
    }
}