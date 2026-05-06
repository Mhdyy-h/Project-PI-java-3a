-- ============================================================
-- Script de création de la base de données BioSync
-- ============================================================

-- Créer la base de données si elle n'existe pas
CREATE DATABASE IF NOT EXISTS biosync
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- Utiliser la base de données
USE biosync;

-- ============================================================
-- Table: utilisateurs
-- ============================================================
CREATE TABLE IF NOT EXISTS utilisateurs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom_complet VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    roles VARCHAR(100) DEFAULT 'ROLE_USER',
    score_global INT DEFAULT 0,
    date_inscription TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    photo_profil VARCHAR(500),
    INDEX idx_email (email),
    INDEX idx_roles (roles)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Table: quiz
-- ============================================================
CREATE TABLE IF NOT EXISTS quiz (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    niveau_stress_cible INT DEFAULT 5,
    score_resultat INT DEFAULT 0,
    medaille_quiz VARCHAR(50),
    date_quiz TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    utilisateur_id INT,
    statut VARCHAR(50) DEFAULT 'disponible',
    temps_moyen_reponse DOUBLE,
    agilite_cognitive TEXT,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    INDEX idx_utilisateur (utilisateur_id),
    INDEX idx_statut (statut)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Table: questions
-- ============================================================
CREATE TABLE IF NOT EXISTS questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    quiz_id INT NOT NULL,
    enonce TEXT NOT NULL,
    reponse_correcte VARCHAR(10) NOT NULL,
    options_fausses TEXT,
    points_valeur INT DEFAULT 1,
    explication TEXT,
    FOREIGN KEY (quiz_id) REFERENCES quiz(id) ON DELETE CASCADE,
    INDEX idx_quiz (quiz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Table: rate_limiting (pour la sécurité)
-- ============================================================
CREATE TABLE IF NOT EXISTS rate_limiting (
    id INT AUTO_INCREMENT PRIMARY KEY,
    identifier VARCHAR(255) NOT NULL,
    attempt_count INT DEFAULT 0,
    last_attempt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    locked_until TIMESTAMP NULL,
    UNIQUE KEY unique_identifier (identifier),
    INDEX idx_identifier (identifier)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Données de test
-- ============================================================

-- Insérer un utilisateur admin par défaut (mot de passe: admin123)
INSERT INTO utilisateurs (nom_complet, email, mot_de_passe, roles, score_global) 
VALUES 
    ('Administrateur', 'admin@biosync.com', 'admin123', 'ROLE_ADMIN,ROLE_COACH,ROLE_USER', 0),
    ('Coach Test', 'coach@biosync.com', 'coach123', 'ROLE_COACH,ROLE_USER', 0),
    ('Utilisateur Test', 'user@biosync.com', 'user123', 'ROLE_USER', 0)
ON DUPLICATE KEY UPDATE id=id;

-- Insérer un quiz de test
INSERT INTO quiz (titre, niveau_stress_cible, score_resultat, utilisateur_id, statut, agilite_cognitive)
VALUES 
    ('Quiz de Stress - Niveau Débutant', 3, 70, 1, 'disponible', 'Évaluation du niveau de stress quotidien'),
    ('Test de Mémoire', 5, 80, 1, 'disponible', 'Évaluation des capacités de mémorisation'),
    ('Quiz Anxiété', 4, 75, 1, 'disponible', 'Mesure du niveau d\'anxiété')
ON DUPLICATE KEY UPDATE id=id;

-- Insérer des questions de test pour le premier quiz
INSERT INTO questions (quiz_id, enonce, reponse_correcte, options_fausses, points_valeur, explication)
VALUES 
    (1, 'Comment vous sentez-vous généralement le matin ?', 'B', 'Très stressé|Calme et reposé|Légèrement anxieux|Fatigué', 1, 'Se sentir calme le matin indique un bon niveau de repos'),
    (1, 'À quelle fréquence ressentez-vous du stress au travail ?', 'C', 'Jamais|Rarement|Parfois|Souvent', 1, 'Un stress occasionnel est normal'),
    (1, 'Avez-vous des difficultés à vous endormir ?', 'A', 'Non, jamais|Oui, souvent|Parfois|Toujours', 1, 'Un bon sommeil est essentiel pour gérer le stress')
ON DUPLICATE KEY UPDATE id=id;

-- ============================================================
-- Afficher un message de succès
-- ============================================================
SELECT 'Base de données BioSync créée avec succès !' AS Message;
SELECT COUNT(*) AS 'Nombre d\'utilisateurs' FROM utilisateurs;
SELECT COUNT(*) AS 'Nombre de quiz' FROM quiz;
SELECT COUNT(*) AS 'Nombre de questions' FROM questions;
