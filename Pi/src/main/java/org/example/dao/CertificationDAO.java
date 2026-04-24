package org.example.dao;

import org.example.DatabaseConnection;
import org.example.model.CertificationRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CertificationDAO {

    /**
     * Creates the certification table if it doesn't exist.
     * Uses ALTER TABLE to add missing columns to an existing table safely.
     */
    public static void createTableIfNotExists() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();

            // Create base table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS certification (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  nom_complet VARCHAR(100) NOT NULL," +
                "  email VARCHAR(100) NOT NULL," +
                "  specialite VARCHAR(50) NOT NULL," +
                "  motivation TEXT," +
                "  statut VARCHAR(20) DEFAULT 'EN_ATTENTE'," +
                "  date_envoi TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            // Safely add columns that might be missing (ignore errors if they already exist)
            String[] alterCols = {
                "ALTER TABLE certification ADD COLUMN IF NOT EXISTS nom_complet VARCHAR(100) NOT NULL DEFAULT ''",
                "ALTER TABLE certification ADD COLUMN IF NOT EXISTS email VARCHAR(100) NOT NULL DEFAULT ''",
                "ALTER TABLE certification ADD COLUMN IF NOT EXISTS specialite VARCHAR(50) NOT NULL DEFAULT 'COACH'",
                "ALTER TABLE certification ADD COLUMN IF NOT EXISTS motivation TEXT",
                "ALTER TABLE certification ADD COLUMN IF NOT EXISTS statut VARCHAR(20) DEFAULT 'EN_ATTENTE'",
                "ALTER TABLE certification ADD COLUMN IF NOT EXISTS date_envoi TIMESTAMP DEFAULT CURRENT_TIMESTAMP",
                "ALTER TABLE certification ADD COLUMN IF NOT EXISTS numero_enregistrement VARCHAR(50) DEFAULT 'NON_FOURNI'",
                "ALTER TABLE certification ADD COLUMN IF NOT EXISTS chemin_pdf VARCHAR(255)",
                "ALTER TABLE certification ALTER COLUMN type SET DEFAULT 'PROFESSIONNEL'",
                "ALTER TABLE certification MODIFY utilisateur_id INT NULL"
            };

            for (String sql : alterCols) {
                try { stmt.execute(sql); } catch (SQLException ignored) {}
            }

            stmt.close();
        } catch (SQLException e) {
            System.err.println("CertificationDAO.createTableIfNotExists: " + e.getMessage());
        }
    }

    /** Insert a new certification request */
    public static boolean insertRequest(CertificationRequest req) {
        createTableIfNotExists();
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO certification (nom_complet, email, specialite, motivation, statut, type, numero_enregistrement, chemin_pdf, diplome_filename, utilisateur_id) VALUES (?, ?, ?, ?, 'EN_ATTENTE', 'PROFESSIONNEL', 'NON_FOURNI', ?, ?, NULL)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, req.getNomComplet());
            ps.setString(2, req.getEmail());
            ps.setString(3, req.getSpecialite());
            ps.setString(4, req.getMotivation());
            ps.setString(5, req.getCheminPdf() != null ? req.getCheminPdf() : "");
            ps.setString(6, req.getCheminPdf() != null ? req.getCheminPdf() : "NON_FOURNI");
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) req.setId(keys.getInt(1));
                keys.close();
            }
            ps.close();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("CertificationDAO.insertRequest: " + e.getMessage());
            return false;
        }
    }

    /** Get all certification requests */
    public static List<CertificationRequest> getAllRequests() {
        createTableIfNotExists();
        List<CertificationRequest> list = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM certification ORDER BY date_envoi DESC");
            while (rs.next()) {
                String dateEnvoi = null;
                try {
                    Timestamp ts = rs.getTimestamp("date_envoi");
                    if (ts != null) {
                        dateEnvoi = String.format("%02d/%02d/%04d",
                            ts.toLocalDateTime().getDayOfMonth(),
                            ts.toLocalDateTime().getMonthValue(),
                            ts.toLocalDateTime().getYear());
                    }
                } catch (SQLException ignored) {}

                CertificationRequest r = new CertificationRequest(
                    rs.getInt("id"),
                    safeGetString(rs, "nom_complet"),
                    safeGetString(rs, "email"),
                    safeGetString(rs, "specialite"),
                    safeGetString(rs, "motivation"),
                    safeGetString(rs, "statut"),
                    dateEnvoi,
                    safeGetString(rs, "chemin_pdf")
                );
                list.add(r);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("CertificationDAO.getAllRequests: " + e.getMessage());
        }
        return list;
    }

    /** Count pending (EN_ATTENTE) requests */
    public static int countPending() {
        createTableIfNotExists();
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM certification WHERE statut = 'EN_ATTENTE'");
            if (rs.next()) return rs.getInt(1);
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("CertificationDAO.countPending: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Update statut and optionally update user role in utilisateur table or create new user.
     * @param req       the certification request object
     * @param statut    "ACCEPTE" or "REFUSE"
     * @param newRole   role string e.g. "[\"ROLE_COACH\"]" or "[\"ROLE_SPECIALISTE\"]"
     */
    public static boolean updateStatut(CertificationRequest req, String statut, String newRole) {
        try {
            Connection conn = DatabaseConnection.getConnection();

            // 1) Update certification statut
            PreparedStatement ps1 = conn.prepareStatement(
                "UPDATE certification SET statut = ? WHERE id = ?"
            );
            ps1.setString(1, statut);
            ps1.setInt(2, req.getId());
            ps1.executeUpdate();
            ps1.close();

            // 2) If accepted, update user role
            if ("ACCEPTE".equals(statut) && req.getEmail() != null && newRole != null) {
                PreparedStatement ps2 = conn.prepareStatement(
                    "UPDATE utilisateur SET roles = ? WHERE email = ?"
                );
                ps2.setString(1, newRole);
                ps2.setString(2, req.getEmail());
                int rowsAffected = ps2.executeUpdate();
                ps2.close();

                // 3) If no user exists with this email, create one
                if (rowsAffected == 0) {
                    PreparedStatement ps3 = conn.prepareStatement(
                        "INSERT INTO utilisateur (nom_complet, email, mot_de_passe, score_global, date_inscription, roles) VALUES (?, ?, ?, 0, CURDATE(), ?)"
                    );
                    ps3.setString(1, req.getNomComplet() == null || req.getNomComplet().isEmpty() ? "Utilisateur" : req.getNomComplet());
                    ps3.setString(2, req.getEmail());
                    ps3.setString(3, "password123"); // Default password
                    ps3.setString(4, newRole);
                    ps3.executeUpdate();
                    ps3.close();
                }
            }

            return true;
        } catch (SQLException e) {
            System.err.println("CertificationDAO.updateStatut: " + e.getMessage());
            return false;
        }
    }

    private static String safeGetString(ResultSet rs, String col) {
        try { return rs.getString(col); } catch (SQLException e) { return ""; }
    }
}
