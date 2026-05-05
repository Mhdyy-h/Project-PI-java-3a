package org.example.service;

import org.example.DatabaseConnection;
import org.example.model.Utilisateur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service CRUD pour la table {@code utilisateur}.
 * Gestion de l'authentification et des comptes utilisateurs.
 *
 * <p>Note : le mot de passe est hashé en SHA-256.
 * Pour un projet de production, remplacer par BCrypt.</p>
 *
 * <p>Exemple d'utilisation :</p>
 * <pre>
 *   ServiceUtilisateur svc = new ServiceUtilisateur();
 *
 *   // Connexion
 *   Utilisateur u = svc.connexion("alice@example.com", "monMotDePasse");
 *   if (u != null) SessionContext.connecter(u);
 *
 *   // Inscription
 *   Utilisateur nouvel = svc.inscrire("Dupont", "Alice", "alice@example.com", "monMotDePasse");
 * </pre>
 */
public class ServiceUtilisateur {

    private static final Logger log = LoggerFactory.getLogger(ServiceUtilisateur.class);

    // ════════════════════════════════════════════════════════════════
    // Authentification
    // ════════════════════════════════════════════════════════════════

    /**
     * Vérifie les identifiants et retourne l'utilisateur correspondant.
     *
     * @return {@link Utilisateur} si les identifiants sont corrects, {@code null} sinon
     */
    public Utilisateur connexion(String email, String motDePasse) {
        String sql = "SELECT * FROM utilisateur WHERE email = ? AND mot_de_passe = ?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, email.trim().toLowerCase());
            ps.setString(2, hasher(motDePasse));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    log.info("Connexion réussie : {}", email);
                    return mapper(rs);
                }
            }
        } catch (SQLException e) {
            log.error("Erreur connexion : {}", e.getMessage());
        }
        log.warn("Échec connexion pour : {}", email);
        return null;
    }

    /**
     * Crée un nouveau compte utilisateur.
     *
     * @return l'utilisateur créé avec son ID, ou {@code null} si l'email existe déjà
     */
    public Utilisateur inscrire(String nom, String prenom, String email, String motDePasse) {
        if (existeEmail(email)) {
            log.warn("Email déjà utilisé : {}", email);
            return null;
        }
        Utilisateur u = new Utilisateur(nom, prenom, email.trim().toLowerCase(), hasher(motDePasse));
        ajouter(u);
        return u;
    }

    // ════════════════════════════════════════════════════════════════
    // CRUD
    // ════════════════════════════════════════════════════════════════

    public void ajouter(Utilisateur u) {
        String sql = """
            INSERT INTO utilisateur (nom_complet, email, mot_de_passe, roles)
            VALUES (?, ?, ?, ?)
            """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            String nomComplet = (u.getPrenom() != null && !u.getPrenom().isBlank())
                    ? u.getPrenom() + " " + u.getNom() : u.getNom();
            ps.setString(1, nomComplet);
            ps.setString(2, u.getEmail().toLowerCase());
            ps.setString(3, u.getMotDePasse());
            ps.setString(4, u.getRole() != null ? u.getRole() : "ETUDIANT");
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) u.setId(rs.getInt(1));
            }
            log.info("Utilisateur créé : {} (id={})", u.getEmail(), u.getId());

        } catch (SQLException e) {
            log.error("Erreur ajouter utilisateur : {}", e.getMessage());
        }
    }

    public Utilisateur getById(int id) {
        String sql = "SELECT * FROM utilisateur WHERE id = ?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapper(rs);
            }
        } catch (SQLException e) {
            log.error("Erreur getById utilisateur : {}", e.getMessage());
        }
        return null;
    }

    public Utilisateur getByEmail(String email) {
        String sql = "SELECT * FROM utilisateur WHERE email = ?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, email.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapper(rs);
            }
        } catch (SQLException e) {
            log.error("Erreur getByEmail : {}", e.getMessage());
        }
        return null;
    }

    public List<Utilisateur> afficherAll() {
        List<Utilisateur> liste = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur ORDER BY nom, prenom";
        try (Connection cn = DatabaseConnection.getConnection();
             Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) {
            log.error("Erreur afficherAll utilisateurs : {}", e.getMessage());
        }
        return liste;
    }

    public void update(Utilisateur u) {
        String sql = """
            UPDATE utilisateur SET nom_complet=?, email=?, roles=? WHERE id=?
            """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            String nomComplet = (u.getPrenom() != null && !u.getPrenom().isBlank())
                    ? u.getPrenom() + " " + u.getNom() : u.getNom();
            ps.setString(1, nomComplet);
            ps.setString(2, u.getEmail().toLowerCase());
            ps.setString(3, u.getRole());
            ps.setInt(4, u.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Erreur update utilisateur : {}", e.getMessage());
        }
    }

    public void changerMotDePasse(int userId, String nouveauMotDePasse) {
        String sql = "UPDATE utilisateur SET mot_de_passe=? WHERE id=?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, hasher(nouveauMotDePasse));
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Erreur changerMotDePasse : {}", e.getMessage());
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM utilisateur WHERE id=?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Erreur delete utilisateur : {}", e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // Utilitaires
    // ════════════════════════════════════════════════════════════════

    private boolean existeEmail(String email) {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE email = ?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, email.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            log.error("Erreur existeEmail : {}", e.getMessage());
        }
        return false;
    }

    /** Hash SHA-256 du mot de passe. Pour prod : remplacer par BCrypt. */
    public static String hasher(String motDePasse) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(motDePasse.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return motDePasse; // fallback en clair (ne devrait jamais arriver)
        }
    }

    private Utilisateur mapper(ResultSet rs) throws SQLException {
        String nomComplet = rs.getString("nom_complet");
        String nom = nomComplet != null ? nomComplet : "";
        String prenom = "";
        if (nomComplet != null && nomComplet.contains(" ")) {
            int idx = nomComplet.indexOf(' ');
            prenom = nomComplet.substring(0, idx);
            nom    = nomComplet.substring(idx + 1);
        }
        LocalDateTime dateCreation = null;
        try {
            Timestamp ts = rs.getTimestamp("created_at");
            if (ts != null) dateCreation = ts.toLocalDateTime();
        } catch (SQLException ignored) {}

        return new Utilisateur(
                rs.getInt("id"),
                nom,
                prenom,
                rs.getString("email"),
                rs.getString("mot_de_passe"),
                rs.getString("roles"),
                dateCreation
        );
    }
}
