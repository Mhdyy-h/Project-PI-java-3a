-- ============================================================
-- Script de CRÉATION COMPLÈTE de la base de données BioSync
-- Adapté strictement au projet Java
-- ============================================================

-- Supprimer la base si elle existe (ATTENTION: supprime toutes les données)
DROP DATABASE IF EXISTS biosync;

-- Créer la base de données
CREATE DATABASE biosync CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Utiliser la base de données
USE biosync;

-- ============================================================
-- TABLE: utilisateur
-- Utilisée par: UserDAO.java
-- ============================================================
CREATE TABLE utilisateur (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom_complet VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(100) NOT NULL,
    roles VARCHAR(100) DEFAULT '["ROLE_USER"]',
    score_global INT DEFAULT 0,
    date_inscription DATE,
    photo_profil VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_roles (roles)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: quiz_mental
-- Utilisée par: QuizDAO.java
-- ============================================================
CREATE TABLE quiz_mental (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    niveau_stress_cible INT DEFAULT 0,
    score_resultat INT DEFAULT 0,
    medaille_quiz VARCHAR(50),
    date_quiz TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    utilisateur_id INT NOT NULL,
    statut VARCHAR(50) DEFAULT 'disponible',
    temps_moyen_reponse DOUBLE,
    agilite_cognitive TEXT,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE,
    INDEX idx_utilisateur (utilisateur_id),
    INDEX idx_statut (statut)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: question
-- Utilisée par: QuestionDAO.java
-- ============================================================
CREATE TABLE question (
    id INT AUTO_INCREMENT PRIMARY KEY,
    quiz_id INT NOT NULL,
    enonce TEXT NOT NULL,
    reponse_correcte VARCHAR(255) NOT NULL,
    options_fausses TEXT,
    points_valeur INT DEFAULT 50,
    FOREIGN KEY (quiz_id) REFERENCES quiz_mental(id) ON DELETE CASCADE,
    INDEX idx_quiz (quiz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: exercice
-- Utilisée par: SeanceExerciceDAO.java
-- ============================================================
CREATE TABLE exercice (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom_exercice VARCHAR(255) NOT NULL,
    intensite VARCHAR(50),
    calories_par_minute DOUBLE,
    description TEXT,
    categorie VARCHAR(100),
    duree_estimee INT,
    equipement_requis TEXT,
    video_url VARCHAR(500),
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_categorie (categorie),
    INDEX idx_intensite (intensite)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: seance_sport
-- Utilisée par: SeanceExerciceDAO.java
-- ============================================================
CREATE TABLE seance_sport (
    id INT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id INT NOT NULL,
    coach_id INT,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    date_seance DATE NOT NULL,
    heure_debut TIME,
    heure_fin TIME,
    duree_totale INT,
    statut ENUM('planifie', 'en_cours', 'termine', 'annule') DEFAULT 'planifie',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE,
    FOREIGN KEY (coach_id) REFERENCES utilisateur(id) ON DELETE SET NULL,
    INDEX idx_utilisateur (utilisateur_id),
    INDEX idx_coach (coach_id),
    INDEX idx_date (date_seance)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: seance_exercice
-- Utilisée par: SeanceExerciceDAO.java
-- ============================================================
CREATE TABLE seance_exercice (
    id INT AUTO_INCREMENT PRIMARY KEY,
    seance_id INT NOT NULL,
    exercice_id INT NOT NULL,
    ordre INT DEFAULT 0,
    series INT DEFAULT 3,
    repetitions INT DEFAULT 10,
    poids DECIMAL(5,2),
    temps_repos INT,
    notes TEXT,
    FOREIGN KEY (seance_id) REFERENCES seance_sport(id) ON DELETE CASCADE,
    FOREIGN KEY (exercice_id) REFERENCES exercice(id) ON DELETE CASCADE,
    INDEX idx_seance (seance_id),
    INDEX idx_exercice (exercice_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: aliment
-- Utilisée par: AlimentDAO.java
-- ============================================================
CREATE TABLE aliment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom_aliment VARCHAR(100) NOT NULL,
    calories INT NOT NULL,
    proteines DECIMAL(5,1) DEFAULT 0,
    glucides DECIMAL(5,1) DEFAULT 0,
    lipides DECIMAL(5,1) DEFAULT 0,
    index_glycemique INT DEFAULT 0,
    est_excitant BOOLEAN DEFAULT FALSE,
    type_aliment VARCHAR(50),
    multi_score VARCHAR(10),
    nutri_score VARCHAR(2),
    INDEX idx_nom (nom_aliment),
    INDEX idx_type (type_aliment)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: repas
-- Utilisée par: RepasDAO.java
-- ============================================================
CREATE TABLE repas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titre_repas VARCHAR(100) NOT NULL,
    type_moment ENUM('MATIN', 'MIDI', 'COLLATION', 'SOIR') NOT NULL,
    date_consommation DATETIME NOT NULL,
    points_gagnes INT DEFAULT 0,
    utilisateur_id INT NOT NULL,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE,
    INDEX idx_utilisateur (utilisateur_id),
    INDEX idx_date (date_consommation),
    INDEX idx_type (type_moment)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: repas_aliments
-- Utilisée par: RepasDAO.java
-- ============================================================
CREATE TABLE repas_aliments (
    repas_id INT NOT NULL,
    aliment_id INT NOT NULL,
    quantite INT DEFAULT 1,
    FOREIGN KEY (repas_id) REFERENCES repas(id) ON DELETE CASCADE,
    FOREIGN KEY (aliment_id) REFERENCES aliment(id) ON DELETE CASCADE,
    PRIMARY KEY (repas_id, aliment_id),
    INDEX idx_repas (repas_id),
    INDEX idx_aliment (aliment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: groupe_soutien
-- Utilisée par: GroupeDAO.java
-- ============================================================
CREATE TABLE groupe_soutien (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom_groupe VARCHAR(255) NOT NULL,
    thematique VARCHAR(100),
    description TEXT,
    capacite_max INT DEFAULT 50,
    image VARCHAR(500),
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_thematique (thematique)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: membre_groupe
-- Utilisée par: MembreDAO.java
-- ============================================================
CREATE TABLE membre_groupe (
    id INT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id INT NOT NULL,
    groupe_id INT NOT NULL,
    date_adhesion DATE DEFAULT (CURRENT_DATE),
    role_membre VARCHAR(50) DEFAULT 'MEMBRE',
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE,
    FOREIGN KEY (groupe_id) REFERENCES groupe_soutien(id) ON DELETE CASCADE,
    UNIQUE KEY unique_membre (utilisateur_id, groupe_id),
    INDEX idx_utilisateur (utilisateur_id),
    INDEX idx_groupe (groupe_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: evenement_sante
-- Utilisée par: EvenementDAO.java
-- ============================================================
CREATE TABLE evenement_sante (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titre_event VARCHAR(255) NOT NULL,
    date_event TIMESTAMP NOT NULL,
    points_participation INT DEFAULT 0,
    groupe_id INT,
    location_name VARCHAR(255),
    address TEXT,
    FOREIGN KEY (groupe_id) REFERENCES groupe_soutien(id) ON DELETE SET NULL,
    INDEX idx_groupe (groupe_id),
    INDEX idx_date (date_event)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: certification
-- Utilisée par: CertificationDAO.java
-- ============================================================
CREATE TABLE certification (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom_complet VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    specialite VARCHAR(50) NOT NULL,
    motivation TEXT,
    statut VARCHAR(20) DEFAULT 'EN_ATTENTE',
    date_envoi TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    type VARCHAR(50) DEFAULT 'PROFESSIONNEL',
    numero_enregistrement VARCHAR(50) DEFAULT 'NON_FOURNI',
    chemin_pdf VARCHAR(255),
    diplome_filename VARCHAR(255),
    utilisateur_id INT NULL,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE SET NULL,
    INDEX idx_statut (statut),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: activity_log
-- Utilisée par: ActivityLogDAO.java
-- ============================================================
CREATE TABLE activity_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL DEFAULT 0,
    nom_utilisateur VARCHAR(100) NOT NULL DEFAULT '',
    email VARCHAR(100) NOT NULL DEFAULT '',
    roles VARCHAR(100) NOT NULL DEFAULT '',
    action VARCHAR(100) NOT NULL,
    date_heure DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_date (date_heure),
    INDEX idx_action (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: alerte
-- Utilisée par: AlerteDAO.java
-- ============================================================
CREATE TABLE alerte (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    date_alerte DATETIME DEFAULT CURRENT_TIMESTAMP,
    criticite ENUM('JAUNE', 'ROUGE') DEFAULT 'JAUNE',
    utilisateur_id INT NOT NULL,
    repas_id INT,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE,
    FOREIGN KEY (repas_id) REFERENCES repas(id) ON DELETE CASCADE,
    INDEX idx_utilisateur (utilisateur_id),
    INDEX idx_criticite (criticite)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: rate_limiting
-- Utilisée par: RateLimitingDAO.java
-- ============================================================
CREATE TABLE rate_limiting (
    email VARCHAR(255) PRIMARY KEY,
    attempt_count INT NOT NULL DEFAULT 0,
    last_attempt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_last_attempt (last_attempt)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- DONNÉES DE TEST
-- ============================================================

-- Insérer 3 utilisateurs de test
INSERT INTO utilisateur (nom_complet, email, mot_de_passe, roles, score_global, date_inscription) VALUES
('Administrateur BioSync', 'admin@biosync.com', 'admin123', '["ROLE_ADMIN"]', 1000, CURDATE()),
('Coach Sportif', 'coach@biosync.com', 'coach123', '["ROLE_COACH"]', 500, CURDATE()),
('Utilisateur Test', 'user@biosync.com', 'user123', '["ROLE_USER"]', 250, CURDATE());

-- Insérer des quiz de test
INSERT INTO quiz_mental (titre, niveau_stress_cible, score_resultat, utilisateur_id, statut) VALUES
('Quiz de Gestion du Stress', 5, 0, 1, 'disponible'),
('Test d''Agilité Cognitive', 3, 0, 1, 'disponible'),
('Évaluation Mentale Complète', 7, 0, 1, 'disponible');

-- Insérer des questions de test
INSERT INTO question (quiz_id, enonce, reponse_correcte, options_fausses, points_valeur) VALUES
(1, 'Quelle technique aide à réduire le stress ?', 'Respiration profonde', 'Caféine excessive,Manque de sommeil,Surcharge de travail', 50),
(1, 'Combien d''heures de sommeil sont recommandées ?', '7-9 heures', '3-4 heures,10-12 heures,1-2 heures', 50),
(2, 'Quel exercice améliore la concentration ?', 'Méditation', 'Regarder la TV,Multitâche,Procrastination', 50);

-- Insérer des exercices de test
INSERT INTO exercice (nom_exercice, intensite, calories_par_minute, description, categorie) VALUES
('Pompes', 'Intermédiaire', 8.5, 'Exercice de musculation pour le haut du corps', 'Force'),
('Course à pied', 'Avancé', 12.0, 'Cardio pour améliorer l''endurance', 'Cardio'),
('Yoga', 'Débutant', 4.0, 'Exercice de flexibilité et relaxation', 'Flexibilité');

-- Insérer des aliments de test
INSERT INTO aliment (nom_aliment, calories, proteines, glucides, lipides, type_aliment, nutri_score) VALUES
('Pomme', 52, 0.3, 14.0, 0.2, 'Fruit', 'A'),
('Poulet grillé', 165, 31.0, 0.0, 3.6, 'Viande', 'B'),
('Riz complet', 111, 2.6, 23.0, 0.9, 'Céréale', 'B'),
('Brocoli', 34, 2.8, 7.0, 0.4, 'Légume', 'A');

-- Insérer un groupe de test
INSERT INTO groupe_soutien (nom_groupe, thematique, description, capacite_max) VALUES
('Groupe Motivation Sport', 'Sport', 'Groupe d''entraide pour rester motivé dans la pratique sportive', 30),
('Nutrition Saine', 'Nutrition', 'Partage de recettes et conseils nutritionnels', 50);

-- ============================================================
-- AFFICHER UN RÉSUMÉ
-- ============================================================

SELECT '✅ Base de données BioSync créée avec succès !' AS Message;
SELECT 'Toutes les tables ont été créées avec les données de test' AS Info;
SELECT CONCAT('Total de ', COUNT(*), ' tables créées') AS Tables FROM information_schema.tables WHERE table_schema = 'biosync';

-- Afficher les comptes de test
SELECT '📋 Comptes de test disponibles :' AS '';
SELECT email, roles, 'Mot de passe dans le script SQL' AS mot_de_passe FROM utilisateur;
