-- ============================================================
-- SCRIPT SQL COMPLET - BASE DE DONNÉES BIOSYNC
-- Généré à partir de l'analyse complète du projet Java
-- Date: 2026-05-07
-- Version: 2.0 COMPLETE
-- ============================================================

-- Supprimer la base si elle existe
DROP DATABASE IF EXISTS biosync;

-- Créer la base de données
CREATE DATABASE biosync CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Utiliser la base de données
USE biosync;

-- ============================================================
-- SECTION 1: GESTION DES UTILISATEURS
-- ============================================================

-- Table: utilisateur
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

-- Table: certification
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

-- Table: rate_limiting
CREATE TABLE rate_limiting (
    email VARCHAR(255) PRIMARY KEY,
    attempt_count INT NOT NULL DEFAULT 0,
    last_attempt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_last_attempt (last_attempt)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- SECTION 2: MODULE SANTÉ MENTALE / QUIZ
-- ============================================================

-- Table: quiz_mental
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

-- Table: question
CREATE TABLE question (
    id INT AUTO_INCREMENT PRIMARY KEY,
    quiz_id INT NOT NULL,
    enonce TEXT NOT NULL,
    reponse_correcte VARCHAR(255) NOT NULL,
    options_fausses TEXT,
    points_valeur INT DEFAULT 50,
    explication TEXT,
    FOREIGN KEY (quiz_id) REFERENCES quiz_mental(id) ON DELETE CASCADE,
    INDEX idx_quiz (quiz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: quiz_session (sessions de quiz)
CREATE TABLE quiz_session (
    id INT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id INT NOT NULL,
    theme VARCHAR(100),
    niveau_difficulte INT DEFAULT 5,
    score_final DOUBLE DEFAULT 0,
    score_elo INT DEFAULT 1000,
    score_fatigue DOUBLE DEFAULT 0,
    nombre_questions INT DEFAULT 0,
    statut VARCHAR(20) DEFAULT 'EN_COURS',
    date_debut DATETIME DEFAULT CURRENT_TIMESTAMP,
    date_fin DATETIME NULL,
    duree_secondes INT DEFAULT 0,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE,
    INDEX idx_utilisateur (utilisateur_id),
    INDEX idx_statut (statut)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: reponse_utilisateur
CREATE TABLE reponse_utilisateur (
    id INT AUTO_INCREMENT PRIMARY KEY,
    session_id INT NOT NULL,
    question_id INT NOT NULL,
    reponse_donnee VARCHAR(255),
    est_correcte BOOLEAN DEFAULT FALSE,
    temps_reponse_ms INT DEFAULT 0,
    ordre_session INT DEFAULT 0,
    date_reponse DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES quiz_session(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE,
    INDEX idx_session (session_id),
    INDEX idx_question (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- SECTION 3: MODULE SPORT / EXERCICES
-- ============================================================

-- Table: exercice
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

-- Table: seance_sport
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
    medaille_obtenue VARCHAR(50),
    heure_debut_reelle TIME,
    alerte_envoyee INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE,
    FOREIGN KEY (coach_id) REFERENCES utilisateur(id) ON DELETE SET NULL,
    INDEX idx_utilisateur (utilisateur_id),
    INDEX idx_coach (coach_id),
    INDEX idx_date (date_seance)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: seance_exercice
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
-- SECTION 4: MODULE NUTRITION
-- ============================================================

-- Table: aliment
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

-- Table: repas
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

-- Table: repas_aliments
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
-- SECTION 5: MODULE COMMUNAUTÉ
-- ============================================================

-- Table: groupe_soutien
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

-- Table: membre_groupe
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

-- Table: evenement_sante
CREATE TABLE evenement_sante (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titre_event VARCHAR(255) NOT NULL,
    date_event TIMESTAMP NOT NULL,
    points_participation INT DEFAULT 0,
    groupe_id INT,
    location_name VARCHAR(255),
    address TEXT,
    latitude DOUBLE,
    longitude DOUBLE,
    FOREIGN KEY (groupe_id) REFERENCES groupe_soutien(id) ON DELETE SET NULL,
    INDEX idx_groupe (groupe_id),
    INDEX idx_date (date_event)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- SECTION 6: LOGS ET ALERTES
-- ============================================================

-- Table: activity_log
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

-- Table: alerte
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
-- SECTION 7: DONNÉES DE TEST
-- ============================================================

-- Insérer des utilisateurs de test
INSERT INTO utilisateur (nom_complet, email, mot_de_passe, roles, score_global, date_inscription) VALUES
('Administrateur BioSync', 'admin@biosync.com', 'admin123', '["ROLE_ADMIN","ROLE_COACH","ROLE_USER"]', 1000, CURDATE()),
('Coach Sportif', 'coach@biosync.com', 'coach123', '["ROLE_COACH","ROLE_USER"]', 500, CURDATE()),
('Utilisateur Test', 'user@biosync.com', 'user123', '["ROLE_USER"]', 250, CURDATE()),
('Jean Dupont', 'jean.dupont@example.com', 'user123', '["ROLE_USER"]', 150, CURDATE()),
('Marie Martin', 'marie.martin@example.com', 'user123', '["ROLE_USER"]', 200, CURDATE());

-- Insérer des quiz de test
INSERT INTO quiz_mental (titre, niveau_stress_cible, score_resultat, utilisateur_id, statut, agilite_cognitive) VALUES
('Évaluation du Stress au Quotidien', 5, 60, 1, 'disponible', 'Mesurez votre niveau de stress quotidien et découvrez des stratégies pour mieux le gérer.'),
('Qualité du Sommeil et Récupération', 2, 50, 1, 'disponible', 'Évaluez vos habitudes de sommeil et identifiez les pistes pour améliorer votre récupération nocturne.'),
('Comprendre et Gérer l\'Anxiété', 7, 70, 1, 'disponible', 'Approfondissez votre compréhension des mécanismes anxieux et des outils thérapeutiques validés pour les gérer.'),
('Performance Cognitive et Mémoire', 2, 60, 1, 'disponible', 'Testez vos connaissances sur le fonctionnement de la mémoire, de l\'attention et des habitudes qui optimisent vos capacités cognitives.'),
('Bien-être Émotionnel et Intelligence Affective', 5, 60, 1, 'disponible', 'Explorez les fondements de l\'intelligence émotionnelle, apprenez à identifier et réguler vos émotions pour un bien-être durable.'),
('Santé Mentale et Équilibre Vie Pro/Perso', 5, 65, 1, 'disponible', 'Évaluez votre équilibre professionnel et identifiez les signaux d\'alarme du burn-out pour agir avant l\'épuisement.');

-- Insérer des questions pour le Quiz 1 (Stress)
INSERT INTO question (quiz_id, enonce, reponse_correcte, options_fausses, points_valeur) VALUES
(1, 'Quelle est la principale hormone libérée lors d\'une réaction de stress aigu ?', 'Le cortisol', 'La mélatonine|La sérotonine|L\'insuline', 50),
(1, 'Parmi les symptômes suivants, lequel est caractéristique d\'un stress chronique ?', 'Fatigue persistante et troubles du sommeil', 'Augmentation de l\'énergie|Amélioration de la concentration|Gain d\'appétit', 50),
(1, 'Quelle technique de respiration est reconnue pour réduire rapidement le stress ?', 'La respiration abdominale lente (4-7-8)', 'L\'hyperventilation volontaire|La respiration thoracique rapide|La rétention d\'air prolongée', 50),
(1, 'Combien de minutes d\'activité physique modérée par jour sont recommandées pour réduire le stress ?', '30 minutes', '5 minutes|2 heures|1 heure', 50),
(1, 'Quel terme désigne le phénomène d\'épuisement professionnel lié au stress chronique au travail ?', 'Le burn-out', 'Le bore-out|La dépression saisonnière|L\'hyperactivité', 50),
(1, 'Parmi ces habitudes, laquelle aggrave le stress chronique ?', 'La consommation excessive de caféine', 'La pratique régulière du yoga|Les promenades en nature|La méditation quotidienne', 50),
(1, 'La technique STOP en gestion du stress signifie :', 'Stopper, Prendre du recul, Observer, Poursuivre consciemment', 'Souffler, Tenir, Observer, Parler|Stimuler, Tolérer, Oublier, Progresser|Stopper, Traiter, Optimiser, Planifier', 100);

-- Insérer des questions pour le Quiz 2 (Sommeil)
INSERT INTO question (quiz_id, enonce, reponse_correcte, options_fausses, points_valeur) VALUES
(2, 'Quelle est la durée de sommeil recommandée pour un adulte en bonne santé ?', '7 à 9 heures', '4 à 5 heures|10 à 12 heures|5 à 6 heures', 50),
(2, 'Quelle hormone joue un rôle clé dans la régulation du cycle veille-sommeil ?', 'La mélatonine', 'L\'adrénaline|La dopamine|Le glucagon', 50),
(2, 'Quelle pratique améliore significativement la qualité du sommeil ?', 'Se coucher et se lever à des heures régulières', 'Regarder un écran lumineux au lit|Faire une sieste de 2 heures le soir|Pratiquer un sport intensif juste avant dormir', 50),
(2, 'Le trouble du sommeil caractérisé par des arrêts respiratoires répétés pendant la nuit s\'appelle :', 'L\'apnée du sommeil', 'L\'insomnie primaire|La narcolepsie|Le somnambulisme', 50),
(2, 'Quelle température ambiante favorise le plus l\'endormissement ?', 'Entre 16 et 19 °C', 'Entre 24 et 27 °C|Entre 10 et 14 °C|Entre 20 et 23 °C', 50),
(2, 'Combien de cycles de sommeil (d\'environ 90 min) réalise-t-on en moyenne par nuit ?', '4 à 6 cycles', '1 à 2 cycles|7 à 9 cycles|10 cycles ou plus', 50),
(2, 'Quelle est la phase du sommeil indispensable à la consolidation de la mémoire ?', 'Le sommeil paradoxal (REM)', 'Le sommeil léger (N1)|Le sommeil lent profond uniquement|L\'éveil nocturne bref', 100);

-- Insérer des exercices de test
INSERT INTO exercice (nom_exercice, intensite, calories_par_minute, description, categorie) VALUES
('Pompes', 'Intermédiaire', 8.5, 'Exercice de musculation pour le haut du corps', 'Force'),
('Squats', 'Intermédiaire', 9.0, 'Exercice pour les jambes et fessiers', 'Force'),
('Course à pied', 'Avancé', 12.0, 'Cardio pour améliorer l\'endurance', 'Cardio'),
('Yoga', 'Débutant', 4.0, 'Exercice de flexibilité et relaxation', 'Flexibilité'),
('Planche', 'Intermédiaire', 5.0, 'Exercice de gainage pour le core', 'Force'),
('Burpees', 'Avancé', 15.0, 'Exercice complet du corps', 'Cardio'),
('Vélo', 'Intermédiaire', 10.0, 'Cardio à faible impact', 'Cardio');

-- Insérer des aliments de test
INSERT INTO aliment (nom_aliment, calories, proteines, glucides, lipides, type_aliment, nutri_score) VALUES
('Pomme', 52, 0.3, 14.0, 0.2, 'Fruit', 'A'),
('Banane', 89, 1.1, 23.0, 0.3, 'Fruit', 'A'),
('Poulet grillé', 165, 31.0, 0.0, 3.6, 'Viande', 'B'),
('Saumon', 208, 20.0, 0.0, 13.0, 'Poisson', 'A'),
('Riz complet', 111, 2.6, 23.0, 0.9, 'Céréale', 'B'),
('Brocoli', 34, 2.8, 7.0, 0.4, 'Légume', 'A'),
('Œuf', 155, 13.0, 1.1, 11.0, 'Protéine', 'B'),
('Avocat', 160, 2.0, 9.0, 15.0, 'Fruit', 'B'),
('Pain complet', 247, 13.0, 41.0, 3.4, 'Céréale', 'B'),
('Yaourt nature', 59, 10.0, 3.6, 0.4, 'Produit laitier', 'A');

-- Insérer des groupes de test
INSERT INTO groupe_soutien (nom_groupe, thematique, description, capacite_max) VALUES
('Groupe Motivation Sport', 'Sport', 'Groupe d\'entraide pour rester motivé dans la pratique sportive', 30),
('Nutrition Saine', 'Nutrition', 'Partage de recettes et conseils nutritionnels', 50),
('Bien-être Mental', 'Mental', 'Soutien et partage sur le bien-être mental', 40),
('Runners Club', 'Sport', 'Groupe pour les passionnés de course à pied', 25);

-- ============================================================
-- AFFICHER UN RÉSUMÉ
-- ============================================================

SELECT '✅ Base de données BioSync créée avec succès !' AS Message;
SELECT '📊 RÉSUMÉ DES TABLES CRÉÉES' AS '';
SELECT CONCAT('Total de ', COUNT(*), ' tables créées') AS Tables 
FROM information_schema.tables 
WHERE table_schema = 'biosync';

SELECT '📋 COMPTES DE TEST DISPONIBLES :' AS '';
SELECT email, roles, 'Voir script SQL pour mot de passe' AS mot_de_passe FROM utilisateur;

SELECT '📈 DONNÉES DE TEST INSÉRÉES :' AS '';
SELECT 
    (SELECT COUNT(*) FROM utilisateur) AS Utilisateurs,
    (SELECT COUNT(*) FROM quiz_mental) AS Quiz,
    (SELECT COUNT(*) FROM question) AS Questions,
    (SELECT COUNT(*) FROM exercice) AS Exercices,
    (SELECT COUNT(*) FROM aliment) AS Aliments,
    (SELECT COUNT(*) FROM groupe_soutien) AS Groupes;
