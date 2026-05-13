-- ============================================================
-- BioSync – Données réalistes complètes
-- Gestion mentale, tableau de bord cognitif, prédictions, sport
-- ============================================================
USE biosync;

-- ══════════════════════════════════════════════════════════
-- 0. Tables manquantes
-- ══════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS coach_user (
    coach_id INT NOT NULL, user_id INT NOT NULL,
    PRIMARY KEY (coach_id, user_id),
    FOREIGN KEY (coach_id) REFERENCES utilisateur(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id)  REFERENCES utilisateur(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS recommandations (
    id INT AUTO_INCREMENT PRIMARY KEY, coach_id INT NOT NULL, user_id INT NOT NULL,
    titre VARCHAR(255) NOT NULL, message TEXT NOT NULL, exercices_json TEXT,
    nutrition TEXT, plan_semaine TEXT, vue BOOLEAN DEFAULT FALSE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reponse_user TEXT, date_reponse TIMESTAMP NULL,
    FOREIGN KEY (coach_id) REFERENCES utilisateur(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id)  REFERENCES utilisateur(id) ON DELETE CASCADE,
    INDEX idx_user (user_id), INDEX idx_coach (coach_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS profil_athlete (
    id INT AUTO_INCREMENT PRIMARY KEY, user_id INT NOT NULL UNIQUE,
    age INT, poids_kg DOUBLE, taille_cm DOUBLE, sexe VARCHAR(10),
    historique_medical TEXT, blessures TEXT, medicaments TEXT,
    objectif VARCHAR(255), niveau_sport VARCHAR(50), disponibilite_semaine INT,
    etat_emotionnel VARCHAR(50), niveau_stress INT, qualite_sommeil INT,
    alimentation TEXT, date_mise_a_jour TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES utilisateur(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS quiz_session (
    id INT AUTO_INCREMENT PRIMARY KEY, utilisateur_id INT NOT NULL,
    theme VARCHAR(100), niveau_difficulte INT NOT NULL DEFAULT 5,
    score_final DOUBLE, score_elo INT NOT NULL DEFAULT 1000,
    score_fatigue DOUBLE, nombre_questions INT NOT NULL DEFAULT 0,
    statut ENUM('EN_COURS','TERMINEE','ABANDONNEE') NOT NULL DEFAULT 'EN_COURS',
    date_debut DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_fin DATETIME, duree_secondes INT,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE,
    INDEX idx_utilisateur (utilisateur_id), INDEX idx_statut (statut)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS profil_cognitif (
    id INT AUTO_INCREMENT PRIMARY KEY, utilisateur_id INT NOT NULL UNIQUE,
    score_elo INT NOT NULL DEFAULT 1000, taux_reussite DOUBLE NOT NULL DEFAULT 0,
    temps_reponse_moyen_ms INT NOT NULL DEFAULT 0, sessions_totales INT NOT NULL DEFAULT 0,
    theme_fort VARCHAR(100), theme_faible VARCHAR(100),
    niveau_actuel ENUM('DEBUTANT','INTERMEDIAIRE','AVANCE') NOT NULL DEFAULT 'DEBUTANT',
    date_mise_a_jour DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reponse_utilisateur (
    id INT AUTO_INCREMENT PRIMARY KEY, session_id INT NOT NULL,
    question_id INT NOT NULL, reponse_donnee VARCHAR(500),
    est_correcte BOOLEAN NOT NULL DEFAULT FALSE, temps_reponse_ms INT,
    ordre_session INT NOT NULL, date_reponse DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES quiz_session(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES question(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS fatigue_log (
    id INT AUTO_INCREMENT PRIMARY KEY, session_id INT NOT NULL,
    utilisateur_id INT NOT NULL, numero_question INT NOT NULL,
    score_fatigue DOUBLE NOT NULL, temps_reponse_glissant_ms INT,
    taux_erreur_glissant DOUBLE,
    type_detection ENUM('LOGICIELLE','VISION') NOT NULL DEFAULT 'LOGICIELLE',
    suggestion ENUM('CONTINUER','PAUSE_COURTE','ARRETER') NOT NULL DEFAULT 'CONTINUER',
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES quiz_session(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ══════════════════════════════════════════════════════════
-- 1. Utilisateurs
-- ══════════════════════════════════════════════════════════

INSERT INTO utilisateur (id, nom_complet, email, mot_de_passe, roles, score_global, date_inscription) VALUES
(1, 'Amir Benali',    'amir@biosync.com',   'amir123',  '["ROLE_ADMIN","ROLE_COACH"]', 1200, '2025-09-01'),
(2, 'Sofia Khelifi',  'sofia@biosync.com',  'sofia123', '["ROLE_COACH"]',              800, '2025-10-15'),
(3, 'Yacine Mebarki', 'yacine@biosync.com', 'yacine123','["ROLE_USER"]',               450, '2025-11-20'),
(4, 'Lina Boudiaf',   'lina@biosync.com',   'lina123',  '["ROLE_USER"]',               320, '2025-12-01'),
(5, 'Rami Hamdi',     'rami@biosync.com',   'rami123',  '["ROLE_USER"]',               280, '2026-01-10'),
(6, 'Nour Bouzid',    'nour@biosync.com',   'nour123',  '["ROLE_USER"]',               150, '2026-03-05'),
(7, 'Karim Hadj',     'karim@biosync.com',  'karim123', '["ROLE_COACH","ROLE_USER"]',  600, '2025-08-20')
ON DUPLICATE KEY UPDATE nom_complet=VALUES(nom_complet), roles=VALUES(roles), score_global=VALUES(score_global);

-- ══════════════════════════════════════════════════════════
-- 2. Questions (banque cognitive)
-- ══════════════════════════════════════════════════════════

INSERT IGNORE INTO question (id, enonce, type, theme, niveau_difficulte, source, reponse_correcte, options, explication) VALUES
(1,  'Quelle hormone est libérée pendant un stress aigu ?', 'QCM', 'Gestion du Stress', 3, 'BANQUE', 'Le cortisol', '["L\'insuline","Le cortisol","La mélatonine","La dopamine"]', 'Le cortisol est sécrété par les glandes surrénales.'),
(2,  'Quelle technique de respiration réduit le stress ?', 'QCM', 'Gestion du Stress', 2, 'BANQUE', 'La respiration 4-7-8', '["L\'hyperventilation","La respiration 4-7-8","La rétention d\'air","La respiration buccale"]', 'La 4-7-8 active le système parasympathique.'),
(3,  'Le burn-out se caractérise par :', 'QCM', 'Gestion du Stress', 5, 'BANQUE', 'Épuisement, dépersonnalisation, perte d\'efficacité', '["Une augmentation d\'énergie","Épuisement, dépersonnalisation, perte d\'efficacité","Des troubles visuels","Une hyperactivité"]', 'Les 3 dimensions de Maslach.'),
(4,  'Quelle habitude aggrave le stress chronique ?', 'QCM', 'Gestion du Stress', 4, 'BANQUE', 'La consommation excessive de caféine', '["La marche en nature","La consommation excessive de caféine","La méditation","Le sommeil régulier"]', 'La caféine augmente le cortisol.'),
(5,  'Quelle durée de sommeil est recommandée pour un adulte ?', 'QCM', 'Sommeil et Récupération', 1, 'BANQUE', '7 à 9 heures', '["4 à 5 heures","7 à 9 heures","10 à 12 heures","5 à 6 heures"]', 'Recommandation du NSF.'),
(6,  'Quelle hormone régule le cycle veille-sommeil ?', 'QCM', 'Sommeil et Récupération', 2, 'BANQUE', 'La mélatonine', '["L\'adrénaline","La mélatonine","Le cortisol","La dopamine"]', 'Sécrétée par la glande pinéale.'),
(7,  'Quelle température favorise l\'endormissement ?', 'QCM', 'Sommeil et Récupération', 3, 'BANQUE', 'Entre 16 et 19°C', '["22-25°C","16-19°C","10-14°C","20-23°C"]', 'Température fraîche = meilleur sommeil.'),
(8,  'La technique grounding 5-4-3-2-1 consiste à :', 'QCM', 'Anxiété', 4, 'BANQUE', 'Identifier 5 choses vues, 4 entendues, 3 touchées, 2 senties, 1 goûtée', '["Compter jusqu\'à 5","Identifier 5 choses vues, 4 entendues, 3 touchées, 2 senties, 1 goûtée","Faire 5 respirations","Répéter 5 affirmations"]', 'Ancrage dans le présent via les 5 sens.'),
(9,  'La TCC agit sur :', 'QCM', 'Anxiété', 6, 'BANQUE', 'Les pensées automatiques négatives et comportements d\'évitement', '["Les pensées automatiques négatives et comportements d\'évitement","La chimie cérébrale uniquement","Les souvenirs d\'enfance","Les relations familiales"]', 'Restructuration cognitive.'),
(10, 'Quelle structure est essentielle pour la mémoire à long terme ?', 'QCM', 'Mémoire et Cognition', 3, 'BANQUE', 'L\'hippocampe', '["Le cervelet","L\'hippocampe","L\'amygdale","Le bulbe rachidien"]', 'Consolidation des souvenirs.'),
(11, 'Capacité de la mémoire de travail ?', 'QCM', 'Mémoire et Cognition', 5, 'BANQUE', '7 éléments (±2)', '["3 éléments","7 éléments (±2)","15 éléments","Illimitée"]', 'Modèle de Miller 1956.'),
(12, 'La technique Pomodoro alterne :', 'QCM', 'Mémoire et Cognition', 2, 'BANQUE', '25 min travail et 5 min pause', '["1h travail 1h repos","25 min travail et 5 min pause","10 min travail 50 min repos","45 min sans pause"]', 'Cycles courts de concentration.'),
(13, 'L\'intelligence émotionnelle selon Goleman est :', 'QCM', 'Bien-être Émotionnel', 4, 'BANQUE', 'Reconnaître, comprendre et gérer ses émotions et celles des autres', '["Le QI lié aux émotions","Reconnaître, comprendre et gérer ses émotions et celles des autres","L\'absence d\'émotions négatives","La simulation d\'émotions"]', '5 composantes de Goleman.'),
(14, 'Les émotions primaires universelles (Ekman) :', 'QCM', 'Bien-être Émotionnel', 3, 'BANQUE', 'Joie, tristesse, peur, colère, dégoût, surprise', '["Jalousie et fierté","Joie, tristesse, peur, colère, dégoût, surprise","Amour et haine","Anxiété et euphorie"]', '6 émotions basiques universelles.'),
(15, 'Combien de bits contient un octet ?', 'QCM', 'Informatique', 1, 'BANQUE', '8', '["4","8","16","32"]', '1 octet = 8 bits.'),
(16, 'Quel protocole sécurise la navigation web ?', 'QCM', 'Informatique', 3, 'BANQUE', 'HTTPS', '["FTP","HTTP","HTTPS","SMTP"]', 'HTTPS = HTTP + TLS/SSL.'),
(17, 'Quel nutriment est important pour les neurones ?', 'QCM', 'Nutrition et Santé', 4, 'BANQUE', 'Les acides gras oméga-3', '["Le fer uniquement","Les acides gras oméga-3","Le calcium","Le sodium"]', 'DHA dans les membranes neuronales.'),
(18, 'Le Nutri-Score A indique :', 'QCM', 'Nutrition et Santé', 2, 'BANQUE', 'Excellente qualité nutritionnelle', '["Excellente qualité nutritionnelle","Produit transformé","Beaucoup de sucres","Riche en sel"]', 'A=excellent, E=mauvais.');

-- ══════════════════════════════════════════════════════════
-- 3. Sessions cognitives (quiz_session)
-- ══════════════════════════════════════════════════════════

-- Yacine (3) : progression débutant → intermédiaire
INSERT INTO quiz_session (id, utilisateur_id, theme, niveau_difficulte, score_final, score_elo, score_fatigue, nombre_questions, statut, date_debut, date_fin, duree_secondes) VALUES
(1,  3, 'Gestion du Stress',       2, 35.0, 1000, 20.0, 7, 'TERMINEE', '2026-01-15 09:00:00', '2026-01-15 09:08:30', 510),
(2,  3, 'Sommeil et Récupération', 2, 42.0, 1010, 25.0, 7, 'TERMINEE', '2026-01-22 10:00:00', '2026-01-22 10:10:00', 600),
(3,  3, 'Mémoire et Cognition',    3, 50.0, 1025, 30.0, 7, 'TERMINEE', '2026-02-01 14:00:00', '2026-02-01 14:12:00', 720),
(4,  3, 'Anxiété',                 3, 55.0, 1040, 28.0, 7, 'TERMINEE', '2026-02-10 09:30:00', '2026-02-10 09:39:00', 540),
(5,  3, 'Gestion du Stress',       4, 60.0, 1060, 35.0, 7, 'TERMINEE', '2026-02-18 11:00:00', '2026-02-18 11:09:30', 570),
(6,  3, 'Bien-être Émotionnel',    4, 65.0, 1080, 32.0, 7, 'TERMINEE', '2026-02-28 16:00:00', '2026-02-28 16:08:00', 480),
(7,  3, 'Mémoire et Cognition',    5, 68.0, 1100, 38.0, 7, 'TERMINEE', '2026-03-07 10:00:00', '2026-03-07 10:11:00', 660),
(8,  3, 'Sommeil et Récupération', 4, 72.0, 1125, 30.0, 7, 'TERMINEE', '2026-03-15 08:30:00', '2026-03-15 08:37:30', 450),
(9,  3, 'Gestion du Stress',       5, 75.0, 1150, 33.0, 7, 'TERMINEE', '2026-03-22 14:00:00', '2026-03-22 14:09:00', 540),
(10, 3, 'Anxiété',                 5, 78.0, 1180, 28.0, 7, 'TERMINEE', '2026-03-30 09:00:00', '2026-03-30 09:07:00', 420),
(11, 3, 'Nutrition et Santé',      4, 80.0, 1200, 25.0, 7, 'TERMINEE', '2026-04-05 11:30:00', '2026-04-05 11:37:30', 450),
(12, 3, 'Mémoire et Cognition',    6, 82.0, 1230, 35.0, 7, 'TERMINEE', '2026-04-12 15:00:00', '2026-04-12 15:08:00', 480),
(13, 3, 'Bien-être Émotionnel',    5, 85.0, 1260, 22.0, 7, 'TERMINEE', '2026-04-20 10:00:00', '2026-04-20 10:06:30', 390),
(14, 3, 'Informatique',            3, 70.0, 1250, 30.0, 7, 'TERMINEE', '2026-04-25 13:00:00', '2026-04-25 13:08:00', 480),
(15, 3, 'Gestion du Stress',       6, 88.0, 1300, 20.0, 7, 'TERMINEE', '2026-05-02 09:00:00', '2026-05-02 09:06:00', 360),

-- Lina (4) : progression modérée
(16, 4, 'Gestion du Stress',       2, 40.0, 1000, 30.0, 7, 'TERMINEE', '2026-02-01 09:00:00', '2026-02-01 09:12:00', 720),
(17, 4, 'Sommeil et Récupération', 2, 45.0, 1015, 35.0, 7, 'TERMINEE', '2026-02-15 10:00:00', '2026-02-15 10:11:00', 660),
(18, 4, 'Mémoire et Cognition',    3, 38.0, 1005, 40.0, 7, 'TERMINEE', '2026-03-01 14:00:00', '2026-03-01 14:15:00', 900),
(19, 4, 'Anxiété',                 3, 52.0, 1025, 32.0, 7, 'TERMINEE', '2026-03-15 09:00:00', '2026-03-15 09:10:00', 600),
(20, 4, 'Bien-être Émotionnel',    4, 58.0, 1050, 28.0, 7, 'TERMINEE', '2026-04-01 11:00:00', '2026-04-01 11:09:00', 540),
(21, 4, 'Gestion du Stress',       4, 62.0, 1070, 30.0, 7, 'TERMINEE', '2026-04-15 08:00:00', '2026-04-15 08:08:00', 480),
(22, 4, 'Mémoire et Cognition',    4, 55.0, 1060, 38.0, 7, 'TERMINEE', '2026-05-01 10:00:00', '2026-05-01 10:10:00', 600),

-- Rami (5) : débutant
(23, 5, 'Gestion du Stress',       1, 30.0, 1000, 45.0, 7, 'TERMINEE', '2026-03-20 09:00:00', '2026-03-20 09:14:00', 840),
(24, 5, 'Sommeil et Récupération', 2, 48.0, 1015, 40.0, 7, 'TERMINEE', '2026-04-05 10:00:00', '2026-04-05 10:12:00', 720),
(25, 5, 'Informatique',            2, 55.0, 1030, 35.0, 7, 'TERMINEE', '2026-04-20 14:00:00', '2026-04-20 14:10:00', 600),

-- Nour (6) : 1 session
(26, 6, 'Gestion du Stress',       1, 42.0, 1000, 50.0, 7, 'TERMINEE', '2026-04-10 09:00:00', '2026-04-10 09:11:00', 660),

-- Sessions abandonnées
(27, 3, 'Informatique',            4, NULL, 1180, NULL, 3, 'ABANDONNEE', '2026-04-18 16:00:00', '2026-04-18 16:04:00', 240),
(28, 4, 'Nutrition et Santé',      3, NULL, 1040, NULL, 2, 'ABANDONNEE', '2026-04-28 15:00:00', '2026-04-28 15:03:00', 180);

-- ══════════════════════════════════════════════════════════
-- 4. Profil cognitif
-- ══════════════════════════════════════════════════════════

INSERT INTO profil_cognitif (utilisateur_id, score_elo, taux_reussite, temps_reponse_moyen_ms, sessions_totales, theme_fort, theme_faible, niveau_actuel) VALUES
(1, 1400, 88.0, 3200, 25, 'Gestion du Stress',    'Informatique',         'AVANCE'),
(2, 1100, 72.0, 4500, 12, 'Bien-être Émotionnel',  'Mémoire et Cognition', 'INTERMEDIAIRE'),
(3, 1300, 68.5, 3800, 15, 'Gestion du Stress',     'Informatique',         'INTERMEDIAIRE'),
(4, 1070, 50.0, 5500,  7, 'Bien-être Émotionnel',  'Mémoire et Cognition', 'DEBUTANT'),
(5, 1030, 44.3, 6200,  3, 'Informatique',           'Gestion du Stress',    'DEBUTANT'),
(6, 1000, 42.0, 6800,  1, 'Gestion du Stress',     NULL,                   'DEBUTANT'),
(7, 1150, 75.0, 4000, 10, 'Nutrition et Santé',    'Anxiété',              'INTERMEDIAIRE')
ON DUPLICATE KEY UPDATE score_elo=VALUES(score_elo), taux_reussite=VALUES(taux_reussite),
    temps_reponse_moyen_ms=VALUES(temps_reponse_moyen_ms), sessions_totales=VALUES(sessions_totales),
    theme_fort=VALUES(theme_fort), theme_faible=VALUES(theme_faible), niveau_actuel=VALUES(niveau_actuel);

-- ══════════════════════════════════════════════════════════
-- 5. Réponses utilisateur
-- ══════════════════════════════════════════════════════════

-- Session 1 : Yacine, 35% (2/7)
INSERT INTO reponse_utilisateur (session_id, question_id, reponse_donnee, est_correcte, temps_reponse_ms, ordre_session) VALUES
(1, 1, 'L\'insuline',       FALSE, 8000, 1),
(1, 2, 'La respiration 4-7-8', TRUE, 6000, 2),
(1, 3, 'Une augmentation d\'énergie', FALSE, 9000, 3),
(1, 4, 'La consommation excessive de caféine', TRUE, 5000, 4),
(1, 5, '4 à 5 heures',     FALSE, 7000, 5),
(1, 8, 'Compter jusqu\'à 5', FALSE, 11000, 6),
(1, 10,'Le cervelet',      FALSE, 8500, 7),

-- Session 5 : Yacine, 60% (4/7)
(5, 1, 'Le cortisol',      TRUE, 4000, 1),
(5, 2, 'La respiration 4-7-8', TRUE, 3500, 2),
(5, 3, 'Épuisement, dépersonnalisation, perte d\'efficacité', TRUE, 6000, 3),
(5, 4, 'La marche en nature', FALSE, 5000, 4),
(5, 9, 'La chimie cérébrale uniquement', FALSE, 7000, 5),
(5, 13,'Reconnaître, comprendre et gérer ses émotions et celles des autres', TRUE, 4500, 6),
(5, 14,'Jalousie et fierté', FALSE, 5500, 7),

-- Session 13 : Yacine, 85% (6/7)
(13, 13,'Reconnaître, comprendre et gérer ses émotions et celles des autres', TRUE, 3000, 1),
(13, 14,'Joie, tristesse, peur, colère, dégoût, surprise', TRUE, 2800, 2),
(13, 8, 'Identifier 5 choses vues, 4 entendues, 3 touchées, 2 senties, 1 goûtée', TRUE, 3500, 3),
(13, 9, 'Les pensées automatiques négatives et comportements d\'évitement', TRUE, 3200, 4),
(13, 3, 'Épuisement, dépersonnalisation, perte d\'efficacité', TRUE, 2500, 5),
(13, 4, 'La consommation excessive de caféine', TRUE, 2200, 6),
(13, 2, 'L\'hyperventilation', FALSE, 4000, 7);

-- ══════════════════════════════════════════════════════════
-- 6. Logs de fatigue
-- ══════════════════════════════════════════════════════════

INSERT INTO fatigue_log (session_id, utilisateur_id, numero_question, score_fatigue, temps_reponse_glissant_ms, taux_erreur_glissant, type_detection, suggestion) VALUES
(1, 3, 1, 15.0, 8000, 1.0,   'LOGICIELLE', 'CONTINUER'),
(1, 3, 2, 18.0, 7000, 0.5,   'LOGICIELLE', 'CONTINUER'),
(1, 3, 3, 25.0, 7667, 0.667, 'LOGICIELLE', 'CONTINUER'),
(1, 3, 4, 22.0, 7000, 0.5,   'LOGICIELLE', 'CONTINUER'),
(1, 3, 5, 30.0, 7200, 0.6,   'LOGICIELLE', 'CONTINUER'),
(1, 3, 6, 40.0, 7600, 0.667, 'LOGICIELLE', 'PAUSE_COURTE'),
(1, 3, 7, 50.0, 7929, 0.714, 'LOGICIELLE', 'ARRETER'),
(5, 3, 1, 10.0, 4000, 0.0,   'LOGICIELLE', 'CONTINUER'),
(5, 3, 2, 12.0, 3750, 0.0,   'LOGICIELLE', 'CONTINUER'),
(5, 3, 3, 18.0, 4500, 0.0,   'LOGICIELLE', 'CONTINUER'),
(5, 3, 4, 22.0, 4625, 0.25,  'LOGICIELLE', 'CONTINUER'),
(5, 3, 5, 28.0, 5000, 0.4,   'LOGICIELLE', 'CONTINUER'),
(5, 3, 6, 30.0, 4667, 0.333, 'LOGICIELLE', 'CONTINUER'),
(5, 3, 7, 35.0, 4786, 0.429, 'LOGICIELLE', 'CONTINUER');

-- ══════════════════════════════════════════════════════════
-- 7. Séances sportives
-- ══════════════════════════════════════════════════════════

INSERT INTO seance_sport (id, nom_seance, heure_debut, duree_minutes, medaille_obtenue, date_seance, utilisateur_id, heure_debut_reelle, alerte_envoyee) VALUES
(1, 'Cardio Matinal',     '07:00', 30, 'Or',     '2026-04-01', 3, '07:02', 0),
(2, 'Musculation Haut',   '18:00', 45, 'Argent', '2026-04-03', 3, '18:05', 0),
(3, 'Yoga Récupération',  '08:00', 60, 'Or',     '2026-04-06', 3, '08:00', 0),
(4, 'HIIT Brûleur',       '17:30', 25, 'Bronze', '2026-04-08', 3, '17:35', 0),
(5, 'Course 5km',         '07:00', 28, 'Or',     '2026-04-10', 3, '07:01', 0),
(6, 'Gainage & Abdos',    '19:00', 20, 'Argent', '2026-04-12', 3, '19:03', 0),
(7, 'Natation 1500m',     '12:00', 40, 'Or',     '2026-04-15', 3, '12:05', 0),
(8, 'Stretching Actif',   '08:30', 30, 'Or',     '2026-04-18', 3, '08:30', 0),
(9, 'Pilates Débutant',   '09:00', 45, 'Argent', '2026-03-10', 4, '09:05', 0),
(10,'Marche Rapide',      '07:30', 40, 'Or',     '2026-03-15', 4, '07:32', 0),
(11,'Circuit Training',   '18:00', 35, 'Bronze', '2026-04-01', 4, '18:10', 0),
(12,'Vélo 20km',          '10:00', 50, 'Argent', '2026-04-08', 4, '10:02', 0),
(13,'Footing Débutant',   '07:00', 20, 'Bronze', '2026-04-05', 5, '07:10', 0),
(14,'Pompes & Squats',    '17:00', 25, 'Bronze', '2026-04-12', 5, '17:08', 0),
(15,'Marche Récupération','08:00', 35, 'Argent', '2026-04-20', 5, '08:00', 0);

-- ══════════════════════════════════════════════════════════
-- 8. Coach ↔ User
-- ══════════════════════════════════════════════════════════

INSERT IGNORE INTO coach_user (coach_id, user_id) VALUES
(1, 3), (1, 4), (2, 5), (2, 6), (7, 3), (7, 5);

-- ══════════════════════════════════════════════════════════
-- 9. Recommandations du coach
-- ══════════════════════════════════════════════════════════

INSERT INTO recommandations (coach_id, user_id, titre, message, exercices_json, nutrition, plan_semaine, vue, date_creation) VALUES
(1, 3, 'Programme Prise de Masse',
 'Bonjour Yacine, vu votre progression en cardio, ajoutez des séances de musculation pour un développement harmonieux.',
 '[{"nom":"Développé couché","series":4,"repetitions":10,"temps_repos":"90s","conseil":"Contrôlez la descente"},{"nom":"Tirage dorsal","series":3,"repetitions":12,"temps_repos":"75s","conseil":"Serrez les omoplates"}]',
 'Protéines 1.8g/kg : poulet, œufs, légumineuses. Collation protéinée après chaque séance.',
 '[{"jour":"Lundi","seance":"Musculation Haut","duree":"50 min","intensite":"Élevée"},{"jour":"Mercredi","seance":"Cardio + Abdos","duree":"40 min","intensite":"Moyenne"},{"jour":"Vendredi","seance":"Musculation Bas","duree":"50 min","intensite":"Élevée"}]',
 TRUE, '2026-04-10 10:00:00'),

(1, 3, 'Récupération Active après HIIT',
 'Votre score de fatigue après le HIIT était élevé. Intégrez des séances de récupération active.',
 '[{"nom":"Marche lente","series":1,"repetitions":1,"temps_repos":"0","conseil":"Rythme très facile"},{"nom":"Étirements dynamiques","series":1,"repetitions":1,"temps_repos":"0","conseil":"10 min minimum"}]',
 'Hydratez-vous (2.5L/jour). Aliments anti-inflammatoires : curcuma, gingembre, poissons gras.',
 '[{"jour":"Mardi","seance":"Récupération active","duree":"30 min","intensite":"Faible"},{"jour":"Jeudi","seance":"Yoga/Stretching","duree":"45 min","intensite":"Faible"}]',
 FALSE, '2026-04-20 14:00:00'),

(7, 3, 'Amélioration Endurance Cardio',
 'Pour améliorer votre VO2max, alternez course lente et fractions courts.',
 '[{"nom":"Fraction court","series":8,"repetitions":1,"temps_repos":"60s","conseil":"30s rapide / 30s lent"},{"nom":"Course lente 30min","series":1,"repetitions":1,"temps_repos":"0","conseil":"Rythme conversation"}]',
 'Avant course : banane + beurre de cacahuète 1h avant. Après : chocolat lait.',
 '[{"jour":"Mardi","seance":"Fraction","duree":"35 min","intensite":"Élevée"},{"jour":"Jeudi","seance":"Footing facile","duree":"40 min","intensite":"Faible"},{"jour":"Samedi","seance":"Sortie longue","duree":"55 min","intensite":"Moyenne"}]',
 FALSE, '2026-05-01 09:00:00'),

(1, 4, 'Renforcement Postural',
 'Lina, vos séances pilates sont excellentes. Ajoutons du renforcement postural.',
 '[{"nom":"Planche frontale","series":3,"repetitions":1,"temps_repos":"45s","conseil":"30s par série"},{"nom":"Superman","series":3,"repetitions":10,"temps_repos":"45s","conseil":"Maintenez 3s en haut"}]',
 'Calcium : produits laitiers, amandes, sardines. Vitamine D : 15 min soleil/jour.',
 '[{"jour":"Lundi","seance":"Renforcement postural","duree":"35 min","intensite":"Moyenne"},{"jour":"Mercredi","seance":"Pilates","duree":"50 min","intensite":"Moyenne"}]',
 TRUE, '2026-04-05 11:00:00'),

(2, 5, 'Démarrage Progressif Fitness',
 'Rami, on commence doucement. 3 séances par semaine max.',
 '[{"nom":"Marche rapide","series":1,"repetitions":1,"temps_repos":"0","conseil":"20 min pour commencer"},{"nom":"Pompes genoux","series":3,"repetitions":8,"temps_repos":"60s","conseil":"Corps aligné"}]',
 'Assiette équilibrée : 1/2 légumes, 1/4 protéines, 1/4 féculents. 2L eau/jour.',
 '[{"jour":"Lundi","seance":"Full body léger","duree":"25 min","intensite":"Faible"},{"jour":"Jeudi","seance":"Cardio marche","duree":"30 min","intensite":"Faible"}]',
 FALSE, '2026-04-08 16:00:00'),

(2, 6, 'Introduction Bien-être Sportif',
 'Nour, félicitations pour votre première session ! Voici un programme doux.',
 '[{"nom":"Yoga débutant","series":1,"repetitions":1,"temps_repos":"0","conseil":"Respirez profondément"},{"nom":"Marche méditative","series":1,"repetitions":1,"temps_repos":"0","conseil":"Concentrez-vous sur chaque pas"}]',
 'Aliments riches en tryptophane : banane, noix, dinde pour favoriser sérotonine.',
 '[{"jour":"Mardi","seance":"Yoga","duree":"30 min","intensite":"Faible"},{"jour":"Vendredi","seance":"Marche","duree":"25 min","intensite":"Faible"}]',
 FALSE, '2026-04-15 10:00:00');

-- ══════════════════════════════════════════════════════════
-- 10. Profils athlètes
-- ══════════════════════════════════════════════════════════

INSERT INTO profil_athlete (user_id, age, poids_kg, taille_cm, sexe, historique_medical, blessures, medicaments, objectif, niveau_sport, disponibilite_semaine, etat_emotionnel, niveau_stress, qualite_sommeil, alimentation) VALUES
(3, 28, 75.0, 178.0, 'M', 'Aucun antécédent', 'Entorse cheville (2025)', 'Aucun', 'Prise de masse sèche', 'Intermédiaire', 5, 'Motivé', 3, 8, 'Équilibrée, riche en protéines'),
(4, 32, 62.0, 165.0, 'F', 'Lombalgie chronique légère', 'Aucune', 'Ibuprofène occasionnel', 'Renforcement postural', 'Débutant', 4, 'Stable', 4, 7, 'Manque de calcium'),
(5, 22, 80.0, 182.0, 'M', 'Aucun', 'Aucune', 'Aucun', 'Perdre 5kg', 'Débutant', 3, 'Enthousiaste', 2, 6, 'Trop de fast-food'),
(6, 25, 58.0, 160.0, 'F', 'Anxiété diagnostiquée', 'Aucune', 'Anxiolytique léger', 'Bien-être global', 'Débutant', 2, 'Anxieux', 7, 5, 'Irrégulière, saute des repas')
ON DUPLICATE KEY UPDATE age=VALUES(age), poids_kg=VALUES(poids_kg), taille_cm=VALUES(taille_cm);

-- ══════════════════════════════════════════════════════════
-- RÉSUMÉ
-- ══════════════════════════════════════════════════════════

SELECT 'Données insérées avec succès !' AS Message;
SELECT 'Utilisateurs' AS Table_, COUNT(*) AS Nb FROM utilisateur
UNION ALL SELECT 'Questions', COUNT(*) FROM question
UNION ALL SELECT 'Sessions cognitives', COUNT(*) FROM quiz_session
UNION ALL SELECT 'Profils cognitifs', COUNT(*) FROM profil_cognitif
UNION ALL SELECT 'Réponses', COUNT(*) FROM reponse_utilisateur
UNION ALL SELECT 'Fatigue logs', COUNT(*) FROM fatigue_log
UNION ALL SELECT 'Séances sport', COUNT(*) FROM seance_sport
UNION ALL SELECT 'Coach-User', COUNT(*) FROM coach_user
UNION ALL SELECT 'Recommandations', COUNT(*) FROM recommandations
UNION ALL SELECT 'Profils athlètes', COUNT(*) FROM profil_athlete;
