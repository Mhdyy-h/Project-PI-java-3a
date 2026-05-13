-- ============================================================
-- Script de MISE À JOUR de la base de données BioSync
-- Ajoute les tables manquantes sans supprimer les données existantes
-- ============================================================

USE biosync;

-- ============================================================
-- Ajouter les colonnes manquantes aux tables existantes
-- ============================================================

-- Utilisateurs : ajouter des colonnes supplémentaires
ALTER TABLE utilisateurs 
ADD COLUMN IF NOT EXISTS telephone VARCHAR(20) AFTER photo_profil,
ADD COLUMN IF NOT EXISTS adresse TEXT AFTER telephone,
ADD COLUMN IF NOT EXISTS date_naissance DATE AFTER adresse,
ADD COLUMN IF NOT EXISTS sexe ENUM('M', 'F', 'Autre') AFTER date_naissance;

-- Quiz : ajouter description et durée
ALTER TABLE quiz
ADD COLUMN IF NOT EXISTS description TEXT AFTER agilite_cognitive,
ADD COLUMN IF NOT EXISTS duree_estimee INT COMMENT 'Durée en minutes' AFTER description;

-- Questions : ajouter ordre
ALTER TABLE questions
ADD COLUMN IF NOT EXISTS ordre INT DEFAULT 0 AFTER explication;

-- ============================================================
-- Créer les nouvelles tables si elles n'existent pas
-- ============================================================

-- Table: certifications
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

-- Table: quiz_sessions
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

-- Table: exercices
CREATE TABLE IF NOT EXISTS exercices (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    description TEXT,
    categorie VARCHAR(100),
    difficulte ENUM('Débutant', 'Intermédiaire', 'Avancé') DEFAULT 'Débutant',
    duree_estimee INT,
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
    duree_totale INT,
    statut ENUM('planifie', 'en_cours', 'termine', 'annule') DEFAULT 'planifie',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    FOREIGN KEY (coach_id) REFERENCES utilisateurs(id) ON DELETE SET NULL,
    INDEX idx_utilisateur (utilisateur_id),
    INDEX idx_coach (coach_id),
    INDEX idx_date (date_seance)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: seance_exercices
CREATE TABLE IF NOT EXISTS seance_exercices (
    id INT AUTO_INCREMENT PRIMARY KEY,
    seance_id INT NOT NULL,
    exercice_id INT NOT NULL,
    ordre INT DEFAULT 0,
    series INT DEFAULT 3,
    repetitions INT DEFAULT 10,
    poids DECIMAL(5,2),
    temps_repos INT,
    notes TEXT,
    FOREIGN KEY (seance_id) REFERENCES seances_sport(id) ON DELETE CASCADE,
    FOREIGN KEY (exercice_id) REFERENCES exercices(id) ON DELETE CASCADE,
    INDEX idx_seance (seance_id),
    INDEX idx_exercice (exercice_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: aliments
CREATE TABLE IF NOT EXISTS aliments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    categorie VARCHAR(100),
    calories DECIMAL(8,2),
    proteines DECIMAL(6,2),
    glucides DECIMAL(6,2),
    lipides DECIMAL(6,2),
    fibres DECIMAL(6,2),
    sucres DECIMAL(6,2),
    sodium DECIMAL(6,2),
    vitamines TEXT,
    mineraux TEXT,
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

-- Table: repas_aliments
CREATE TABLE IF NOT EXISTS repas_aliments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    repas_id INT NOT NULL,
    aliment_id INT NOT NULL,
    quantite DECIMAL(8,2),
    FOREIGN KEY (repas_id) REFERENCES repas(id) ON DELETE CASCADE,
    FOREIGN KEY (aliment_id) REFERENCES aliments(id) ON DELETE CASCADE,
    INDEX idx_repas (repas_id),
    INDEX idx_aliment (aliment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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

-- Table: activity_logs
CREATE TABLE IF NOT EXISTS activity_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id INT,
    action VARCHAR(255) NOT NULL,
    module VARCHAR(100),
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
-- AFFICHER UN RÉSUMÉ
-- ============================================================

SELECT '✅ Base de données mise à jour avec succès !' AS Message;
SELECT 'Toutes les tables ont été créées ou mises à jour' AS Info;
