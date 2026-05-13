-- ============================================================
-- Script COMPLET de création de la base de données BioSync
-- Version: 1.0
-- Date: 2026-05-06
-- ============================================================

-- Créer la base de données si elle n'existe pas
CREATE DATABASE IF NOT EXISTS biosync
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- Utiliser la base de données
USE biosync;

-- ============================================================
-- SECTION 1: GESTION DES UTILISATEURS
-- ============================================================

-- Table: utilisateurs
CREATE TABLE IF NOT EXISTS utilisateurs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom_complet VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    roles VARCHAR(100) DEFAULT 'ROLE_USER',
    score_global INT DEFAULT 0,
    date_inscription TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    photo_profil VARCHAR(500),
    telephone VARCHAR(20),
    adresse TEXT,
    date_naissance DATE,
    sexe ENUM('M', 'F', 'Autre'),
    INDEX idx_email (email),
    INDEX idx_roles (roles)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: certifications (pour les coachs)
CREATE TABLE IF NOT EXISTS certifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id INT NOT NULL,
    nom_certification VARCHAR(255) NOT NULL,
    organisme VARCHAR(255),
    date_obtention DATE,
    date_expiration DATE,
    numero_certification VARCHAR(100),
    document_path VARCHAR(500),
    statut ENUM('en_attente', 'approuve', 'rejete') DEFAULT 'en_attente',
    date_demande TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_validation TIMESTAMP NULL,
    commentaire_admin TEXT,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    INDEX idx_utilisateur (utilisateur_id),
    INDEX idx_statut (statut)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: rate_limiting (sécurité)
CREATE TABLE IF NOT EXISTS rate_limiting (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    attempt_count INT DEFAULT 0,
    last_attempt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_email (email),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- SECTION 2: MODULE MENTAL / QUIZ
-- ============================================================

-- Table: quiz
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
    description TEXT,
    duree_estimee INT COMMENT 'Durée en minutes',
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    INDEX idx_utilisateur (utilisateur_id),
    INDEX idx_statut (statut)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: questions
CREATE TABLE IF NOT EXISTS questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    quiz_id INT NOT NULL,
    enonce TEXT NOT NULL,
    reponse_correcte VARCHAR(10) NOT NULL,
    options_fausses TEXT COMMENT 'Options séparées par |',
    points_valeur INT DEFAULT 1,
    explication TEXT,
    ordre INT DEFAULT 0,
    FOREIGN KEY (quiz_id) REFERENCES quiz(id) ON DELETE CASCADE,
    INDEX idx_quiz (quiz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: quiz_sessions (historique des passages)
CREATE TABLE IF NOT EXISTS quiz_sessions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    quiz_id INT NOT NULL,
    utilisateur_id INT NOT NULL,
    score_obtenu INT DEFAULT 0,
    score_maximum INT DEFAULT 0,
    pourcentage DECIMAL(5,2),
    temps_total INT COMMENT 'Temps en secondes',
    date_debut TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_fin TIMESTAMP NULL,
    statut ENUM('en_cours', 'termine', 'abandonne') DEFAULT 'en_cours',
    FOREIGN KEY (quiz_id) REFERENCES quiz(id) ON DELETE CASCADE,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    INDEX idx_quiz (quiz_id),
    INDEX idx_utilisateur (utilisateur_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: reponses_utilisateur
CREATE TABLE IF NOT EXISTS reponses_utilisateur (
    id INT AUTO_INCREMENT PRIMARY KEY,
    session_id INT NOT NULL,
    question_id INT NOT NULL,
    reponse_donnee VARCHAR(10),
    est_correcte BOOLEAN DEFAULT FALSE,
    temps_reponse INT COMMENT 'Temps en secondes',
    date_reponse TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES quiz_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    INDEX idx_session (session_id),
    INDEX idx_question (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- SECTION 3: MODULE SPORT / EXERCICES
-- ============================================================

-- Table: exercices
CREATE TABLE IF NOT EXISTS exercices (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    description TEXT,
    categorie VARCHAR(100) COMMENT 'Cardio, Musculation, Flexibilité, etc.',
    difficulte ENUM('Débutant', 'Intermédiaire', 'Avancé') DEFAULT 'Débutant',
    duree_estimee INT COMMENT 'Durée en minutes',
    calories_brulees INT,
    equipement_requis TEXT,
    instructions TEXT,
    video_url VARCHAR(500),
    image_url VARCHAR(500),
    muscle_cible VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_categorie (categorie),
    INDEX idx_difficulte (difficulte)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: seances_sport
CREATE TABLE IF NOT EXISTS seances_sport (
    id INT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id INT NOT NULL,
    coach_id INT,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    date_seance DATE NOT NULL,
    heure_debut TIME,
    heure_fin TIME,
    duree_totale INT COMMENT 'Durée en minutes',
    statut ENUM('planifie', 'en_cours', 'termine', 'annule') DEFAULT 'planifie',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    FOREIGN KEY (coach_id) REFERENCES utilisateurs(id) ON DELETE SET NULL,
    INDEX idx_utilisateur (utilisateur_id),
    INDEX idx_coach (coach_id),
    INDEX idx_date (date_seance)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: seance_exercices (relation many-to-many)
CREATE TABLE IF NOT EXISTS seance_exercices (
    id INT AUTO_INCREMENT PRIMARY KEY,
    seance_id INT NOT NULL,
    exercice_id INT NOT NULL,
    ordre INT DEFAULT 0,
    series INT DEFAULT 3,
    repetitions INT DEFAULT 10,
    poids DECIMAL(5,2) COMMENT 'Poids en kg',
    temps_repos INT COMMENT 'Repos en secondes',
    notes TEXT,
    FOREIGN KEY (seance_id) REFERENCES seances_sport(id) ON DELETE CASCADE,
    FOREIGN KEY (exercice_id) REFERENCES exercices(id) ON DELETE CASCADE,
    INDEX idx_seance (seance_id),
    INDEX idx_exercice (exercice_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: progression_exercices
CREATE TABLE IF NOT EXISTS progression_exercices (
    id INT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id INT NOT NULL,
    exercice_id INT NOT NULL,
    date_execution DATE NOT NULL,
    series_realisees INT,
    repetitions_realisees INT,
    poids_utilise DECIMAL(5,2),
    duree INT COMMENT 'Durée en minutes',
    calories_brulees INT,
    ressenti ENUM('Très facile', 'Facile', 'Modéré', 'Difficile', 'Très difficile'),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    FOREIGN KEY (exercice_id) REFERENCES exercices(id) ON DELETE CASCADE,
    INDEX idx_utilisateur (utilisateur_id),
    INDEX idx_exercice (exercice_id),
    INDEX idx_date (date_execution)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- SECTION 4: MODULE NUTRITION
-- ============================================================

-- Table: aliments
CREATE TABLE IF NOT EXISTS aliments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    categorie VARCHAR(100) COMMENT 'Fruits, Légumes, Protéines, etc.',
    calories DECIMAL(8,2) COMMENT 'Calories pour 100g',
    proteines DECIMAL(6,2) COMMENT 'Protéines en g pour 100g',
    glucides DECIMAL(6,2) COMMENT 'Glucides en g pour 100g',
    lipides DECIMAL(6,2) COMMENT 'Lipides en g pour 100g',
    fibres DECIMAL(6,2),
    sucres DECIMAL(6,2),
    sodium DECIMAL(6,2),
    vitamines TEXT COMMENT 'JSON des vitamines',
    mineraux TEXT COMMENT 'JSON des minéraux',
    INDEX idx_categorie (categorie),
    INDEX idx_nom (nom)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: repas
CREATE TABLE IF NOT EXISTS repas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id INT NOT NULL,
    type_repas ENUM('Petit-déjeuner', 'Déjeuner', 'Dîner', 'Collation') NOT NULL,
    date_repas DATE NOT NULL,
    heure_repas TIME,
    nom_repas VARCHAR(255),
    description TEXT,
    calories_totales DECIMAL(8,2),
    proteines_totales DECIMAL(6,2),
    glucides_totales DECIMAL(6,2),
    lipides_totales DECIMAL(6,2),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    INDEX idx_utilisateur (utilisateur_id),
    INDEX idx_date (date_repas),
    INDEX idx_type (type_repas)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: repas_aliments (relation many-to-many)
CREATE TABLE IF NOT EXISTS repas_aliments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    repas_id INT NOT NULL,
    aliment_id INT NOT NULL,
    quantite DECIMAL(8,2) COMMENT 'Quantité en grammes',
    FOREIGN KEY (repas_id) REFERENCES repas(id) ON DELETE CASCADE,
    FOREIGN KEY (aliment_id) REFERENCES aliments(id) ON DELETE CASCADE,
    INDEX idx_repas (repas_id),
    INDEX idx_aliment (aliment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: objectifs_nutritionnels
CREATE TABLE IF NOT EXISTS objectifs_nutritionnels (
    id INT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id INT NOT NULL,
    calories_cible INT,
    proteines_cible DECIMAL(6,2),
    glucides_cible DECIMAL(6,2),
    lipides_cible DECIMAL(6,2),
    eau_cible DECIMAL(6,2) COMMENT 'Eau en litres',
    date_debut DATE NOT NULL,
    date_fin DATE,
    actif BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    INDEX idx_utilisateur (utilisateur_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- SECTION 5: GROUPES ET COMMUNAUTÉ
-- ============================================================

-- Table: groupes
CREATE TABLE IF NOT EXISTS groupes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    description TEXT,
    type_groupe ENUM('Sport', 'Nutrition', 'Mental', 'Général') DEFAULT 'Général',
    createur_id INT NOT NULL,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    est_prive BOOLEAN DEFAULT FALSE,
    code_acces VARCHAR(50),
    image_url VARCHAR(500),
    FOREIGN KEY (createur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    INDEX idx_createur (createur_id),
    INDEX idx_type (type_groupe)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: membres_groupe
CREATE TABLE IF NOT EXISTS membres_groupe (
    id INT AUTO_INCREMENT PRIMARY KEY,
    groupe_id INT NOT NULL,
    utilisateur_id INT NOT NULL,
    role ENUM('admin', 'moderateur', 'membre') DEFAULT 'membre',
    date_adhesion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (groupe_id) REFERENCES groupes(id) ON DELETE CASCADE,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    UNIQUE KEY unique_membre (groupe_id, utilisateur_id),
    INDEX idx_groupe (groupe_id),
    INDEX idx_utilisateur (utilisateur_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- SECTION 6: ÉVÉNEMENTS
-- ============================================================

-- Table: evenements
CREATE TABLE IF NOT EXISTS evenements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    type_evenement ENUM('Sport', 'Nutrition', 'Mental', 'Social') DEFAULT 'Social',
    date_debut DATETIME NOT NULL,
    date_fin DATETIME,
    lieu VARCHAR(255),
    adresse TEXT,
    capacite_max INT,
    organisateur_id INT NOT NULL,
    groupe_id INT,
    est_public BOOLEAN DEFAULT TRUE,
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (organisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    FOREIGN KEY (groupe_id) REFERENCES groupes(id) ON DELETE SET NULL,
    INDEX idx_organisateur (organisateur_id),
    INDEX idx_groupe (groupe_id),
    INDEX idx_date (date_debut)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: participants_evenement
CREATE TABLE IF NOT EXISTS participants_evenement (
    id INT AUTO_INCREMENT PRIMARY KEY,
    evenement_id INT NOT NULL,
    utilisateur_id INT NOT NULL,
    statut ENUM('confirme', 'en_attente', 'refuse', 'annule') DEFAULT 'confirme',
    date_inscription TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (evenement_id) REFERENCES evenements(id) ON DELETE CASCADE,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    UNIQUE KEY unique_participant (evenement_id, utilisateur_id),
    INDEX idx_evenement (evenement_id),
    INDEX idx_utilisateur (utilisateur_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- SECTION 7: MESSAGERIE
-- ============================================================

-- Table: messages
CREATE TABLE IF NOT EXISTS messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    expediteur_id INT NOT NULL,
    destinataire_id INT NOT NULL,
    sujet VARCHAR(255),
    contenu TEXT NOT NULL,
    date_envoi TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    est_lu BOOLEAN DEFAULT FALSE,
    date_lecture TIMESTAMP NULL,
    FOREIGN KEY (expediteur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    FOREIGN KEY (destinataire_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    INDEX idx_expediteur (expediteur_id),
    INDEX idx_destinataire (destinataire_id),
    INDEX idx_date (date_envoi)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- SECTION 8: LOGS ET MONITORING
-- ============================================================

-- Table: activity_logs
CREATE TABLE IF NOT EXISTS activity_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id INT,
    action VARCHAR(255) NOT NULL,
    module VARCHAR(100) COMMENT 'Sport, Nutrition, Mental, etc.',
    details TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    date_action TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE SET NULL,
    INDEX idx_utilisateur (utilisateur_id),
    INDEX idx_date (date_action),
    INDEX idx_module (module)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: alertes
CREATE TABLE IF NOT EXISTS alertes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id INT NOT NULL,
    type_alerte ENUM('info', 'warning', 'danger', 'success') DEFAULT 'info',
    titre VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    est_lu BOOLEAN DEFAULT FALSE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_lecture TIMESTAMP NULL,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    INDEX idx_utilisateur (utilisateur_id),
    INDEX idx_est_lu (est_lu)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- SECTION 9: DONNÉES DE TEST
-- ============================================================

-- Insérer des utilisateurs de test
INSERT INTO utilisateurs (nom_complet, email, mot_de_passe, roles, score_global) 
VALUES 
    ('Administrateur BioSync', 'admin@biosync.com', 'admin123', 'ROLE_ADMIN,ROLE_COACH,ROLE_USER', 0),
    ('Coach Professionnel', 'coach@biosync.com', 'coach123', 'ROLE_COACH,ROLE_USER', 0),
    ('Utilisateur Test', 'user@biosync.com', 'user123', 'ROLE_USER', 0),
    ('Jean Dupont', 'jean.dupont@example.com', 'user123', 'ROLE_USER', 150),
    ('Marie Martin', 'marie.martin@example.com', 'user123', 'ROLE_USER', 200)
ON DUPLICATE KEY UPDATE id=id;

-- Insérer des quiz de test
INSERT INTO quiz (titre, niveau_stress_cible, score_resultat, utilisateur_id, statut, agilite_cognitive, description)
VALUES 
    ('Quiz de Stress - Niveau Débutant', 3, 70, 1, 'disponible', 'Évaluation du niveau de stress quotidien', 'Ce quiz évalue votre niveau de stress au quotidien'),
    ('Test de Mémoire', 5, 80, 1, 'disponible', 'Évaluation des capacités de mémorisation', 'Testez vos capacités de mémorisation'),
    ('Quiz Anxiété', 4, 75, 1, 'disponible', 'Mesure du niveau d\'anxiété', 'Évaluez votre niveau d\'anxiété'),
    ('Bien-être Mental', 6, 85, 1, 'disponible', 'Évaluation du bien-être général', 'Mesurez votre bien-être mental'),
    ('Concentration et Focus', 7, 90, 1, 'disponible', 'Test de concentration', 'Évaluez votre capacité de concentration')
ON DUPLICATE KEY UPDATE id=id;

-- Insérer des questions de test
INSERT INTO questions (quiz_id, enonce, reponse_correcte, options_fausses, points_valeur, explication, ordre)
VALUES 
    (1, 'Comment vous sentez-vous généralement le matin ?', 'B', 'Très stressé|Calme et reposé|Légèrement anxieux|Fatigué', 1, 'Se sentir calme le matin indique un bon niveau de repos', 1),
    (1, 'À quelle fréquence ressentez-vous du stress au travail ?', 'C', 'Jamais|Rarement|Parfois|Souvent', 1, 'Un stress occasionnel est normal', 2),
    (1, 'Avez-vous des difficultés à vous endormir ?', 'A', 'Non, jamais|Oui, souvent|Parfois|Toujours', 1, 'Un bon sommeil est essentiel pour gérer le stress', 3),
    (2, 'Combien de chiffres pouvez-vous mémoriser en une fois ?', 'C', '3-4|5-6|7-8|9-10', 1, 'La moyenne est de 7±2 éléments', 1),
    (2, 'Oubliez-vous souvent où vous avez mis vos clés ?', 'B', 'Toujours|Rarement|Parfois|Jamais', 1, 'Des oublis occasionnels sont normaux', 2),
    (3, 'Ressentez-vous souvent de l\'inquiétude ?', 'B', 'Jamais|Parfois|Souvent|Toujours', 1, 'Une inquiétude modérée est normale', 1),
    (3, 'Avez-vous des palpitations cardiaques ?', 'A', 'Rarement|Parfois|Souvent|Toujours', 1, 'Des palpitations rares sont normales', 2)
ON DUPLICATE KEY UPDATE id=id;

-- Insérer des exercices de test
INSERT INTO exercices (nom, description, categorie, difficulte, duree_estimee, calories_brulees, muscle_cible)
VALUES 
    ('Pompes', 'Exercice de musculation pour le haut du corps', 'Musculation', 'Débutant', 10, 50, 'Pectoraux, Triceps'),
    ('Squats', 'Exercice pour les jambes et fessiers', 'Musculation', 'Débutant', 10, 60, 'Quadriceps, Fessiers'),
    ('Course à pied', 'Exercice cardiovasculaire', 'Cardio', 'Intermédiaire', 30, 300, 'Jambes, Cardio'),
    ('Planche', 'Exercice de gainage', 'Musculation', 'Débutant', 5, 30, 'Abdominaux, Core'),
    ('Burpees', 'Exercice complet du corps', 'Cardio', 'Avancé', 15, 150, 'Corps entier')
ON DUPLICATE KEY UPDATE id=id;

-- Insérer des aliments de test
INSERT INTO aliments (nom, categorie, calories, proteines, glucides, lipides)
VALUES 
    ('Poulet grillé', 'Protéines', 165, 31, 0, 3.6),
    ('Riz blanc', 'Glucides', 130, 2.7, 28, 0.3),
    ('Brocoli', 'Légumes', 34, 2.8, 7, 0.4),
    ('Banane', 'Fruits', 89, 1.1, 23, 0.3),
    ('Œuf', 'Protéines', 155, 13, 1.1, 11),
    ('Avocat', 'Lipides', 160, 2, 9, 15),
    ('Saumon', 'Protéines', 208, 20, 0, 13)
ON DUPLICATE KEY UPDATE id=id;

-- Insérer un groupe de test
INSERT INTO groupes (nom, description, type_groupe, createur_id)
VALUES 
    ('Groupe Fitness', 'Groupe pour les passionnés de fitness', 'Sport', 2),
    ('Nutrition Saine', 'Partage de recettes et conseils nutrition', 'Nutrition', 2),
    ('Bien-être Mental', 'Soutien et partage sur le bien-être mental', 'Mental', 1)
ON DUPLICATE KEY UPDATE id=id;

-- ============================================================
-- AFFICHER UN RÉSUMÉ
-- ============================================================

SELECT '✅ Base de données BioSync créée avec succès !' AS Message;
SELECT '📊 RÉSUMÉ DES DONNÉES' AS '';
SELECT COUNT(*) AS 'Utilisateurs' FROM utilisateurs;
SELECT COUNT(*) AS 'Quiz' FROM quiz;
SELECT COUNT(*) AS 'Questions' FROM questions;
SELECT COUNT(*) AS 'Exercices' FROM exercices;
SELECT COUNT(*) AS 'Aliments' FROM aliments;
SELECT COUNT(*) AS 'Groupes' FROM groupes;

SELECT '🔐 COMPTES DE TEST' AS '';
SELECT 'Admin: admin@biosync.com / admin123' AS 'Compte 1';
SELECT 'Coach: coach@biosync.com / coach123' AS 'Compte 2';
SELECT 'User: user@biosync.com / user123' AS 'Compte 3';
