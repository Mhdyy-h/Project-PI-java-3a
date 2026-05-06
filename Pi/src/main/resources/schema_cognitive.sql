-- ═══════════════════════════════════════════════════════════════
-- Schema SQL — Plateforme d'évaluation cognitive
-- Base de données : biosync
-- Aucune table existante n'est modifiée ou supprimée.
-- Exécuter ce script une seule fois après la Phase 1.
-- ═══════════════════════════════════════════════════════════════

USE biosync;

-- ── 1. Utilisateurs ──────────────────────────────────────────
-- utilisateurId est déjà référencé dans seance_sport comme entier brut.
-- Cette table formalise l'entité sans casser l'existant.
CREATE TABLE IF NOT EXISTS utilisateur (
    id            INT          AUTO_INCREMENT PRIMARY KEY,
    nom           VARCHAR(100) NOT NULL,
    prenom        VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    mot_de_passe  VARCHAR(255) NOT NULL COMMENT 'Hash BCrypt',
    role          ENUM('ETUDIANT', 'ADMIN') NOT NULL DEFAULT 'ETUDIANT',
    date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ── 2. Sessions d'évaluation cognitive ───────────────────────
CREATE TABLE IF NOT EXISTS quiz_session (
    id                INT    AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id    INT    NOT NULL,
    theme             VARCHAR(100),
    niveau_difficulte INT    NOT NULL DEFAULT 5 COMMENT 'Échelle ELO traduite 1-10',
    score_final       DOUBLE COMMENT 'Pourcentage de bonnes réponses 0-100',
    score_elo         INT    NOT NULL DEFAULT 1000 COMMENT 'Cote ELO de l utilisateur après session',
    score_fatigue     DOUBLE COMMENT 'Score de fatigue moyen 0-100',
    nombre_questions  INT    NOT NULL DEFAULT 0,
    statut            ENUM('EN_COURS', 'TERMINEE', 'ABANDONNEE') NOT NULL DEFAULT 'EN_COURS',
    date_debut        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_fin          DATETIME,
    duree_secondes    INT,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE
);

-- ── 3. Banque de questions ────────────────────────────────────
CREATE TABLE IF NOT EXISTS question (
    id                INT          AUTO_INCREMENT PRIMARY KEY,
    enonce            TEXT         NOT NULL,
    type              ENUM('QCM', 'VRAI_FAUX', 'OUVERTE') NOT NULL DEFAULT 'QCM',
    theme             VARCHAR(100) NOT NULL,
    niveau_difficulte INT          NOT NULL DEFAULT 5 COMMENT '1 = très facile, 10 = très difficile',
    source            ENUM('BANQUE', 'IA_GENEREE') NOT NULL DEFAULT 'BANQUE',
    reponse_correcte  VARCHAR(500) NOT NULL,
    options           JSON         COMMENT 'Tableau JSON des choix : ["Choix A","Choix B",...]',
    explication       TEXT         COMMENT 'Explication générée par Claude API',
    date_creation     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ── 4. Réponses utilisateur (une ligne par question répondue) ─
CREATE TABLE IF NOT EXISTS reponse_utilisateur (
    id                 INT     AUTO_INCREMENT PRIMARY KEY,
    session_id         INT     NOT NULL,
    question_id        INT     NOT NULL,
    reponse_donnee     VARCHAR(500),
    est_correcte       BOOLEAN NOT NULL DEFAULT FALSE,
    temps_reponse_ms   INT     COMMENT 'Durée entre affichage et soumission en ms',
    ordre_session      INT     NOT NULL COMMENT 'Position de la question dans la session',
    date_reponse       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id)  REFERENCES quiz_session(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES question(id)
);

-- ── 5. Profil cognitif agrégé par utilisateur ─────────────────
-- Mis à jour automatiquement après chaque session terminée.
CREATE TABLE IF NOT EXISTS profil_cognitif (
    id                      INT          AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id           INT          NOT NULL UNIQUE,
    score_elo                INT          NOT NULL DEFAULT 1000,
    taux_reussite            DOUBLE       NOT NULL DEFAULT 0   COMMENT '0-100',
    temps_reponse_moyen_ms   INT          NOT NULL DEFAULT 0,
    sessions_totales         INT          NOT NULL DEFAULT 0,
    theme_fort               VARCHAR(100) COMMENT 'Thème avec le meilleur taux de réussite',
    theme_faible             VARCHAR(100) COMMENT 'Thème avec le pire taux de réussite',
    niveau_actuel            ENUM('DEBUTANT', 'INTERMEDIAIRE', 'AVANCE') NOT NULL DEFAULT 'DEBUTANT',
    date_mise_a_jour         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE
);

-- ── 6. Logs de fatigue (une ligne par question répondue) ──────
CREATE TABLE IF NOT EXISTS fatigue_log (
    id                        INT    AUTO_INCREMENT PRIMARY KEY,
    session_id                INT    NOT NULL,
    utilisateur_id             INT    NOT NULL,
    numero_question            INT    NOT NULL COMMENT 'Position dans la session (1-based)',
    score_fatigue              DOUBLE NOT NULL COMMENT '0-100',
    temps_reponse_glissant_ms  INT    COMMENT 'Moyenne mobile sur les N dernières réponses',
    taux_erreur_glissant       DOUBLE COMMENT 'Taux d erreur sur les N dernières réponses 0-1',
    type_detection             ENUM('LOGICIELLE', 'VISION') NOT NULL DEFAULT 'LOGICIELLE',
    suggestion                 ENUM('CONTINUER', 'PAUSE_COURTE', 'ARRETER') NOT NULL DEFAULT 'CONTINUER',
    timestamp                  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES quiz_session(id) ON DELETE CASCADE
);

-- ── 7. Événements d'anomalie comportementale ─────────────────
CREATE TABLE IF NOT EXISTS anomalie_event (
    id               INT    AUTO_INCREMENT PRIMARY KEY,
    session_id       INT    NOT NULL,
    utilisateur_id    INT    NOT NULL,
    type_anomalie    VARCHAR(100) NOT NULL COMMENT 'Ex: REPONSE_TROP_RAPIDE, COPIER_COLLER',
    niveau_gravite   ENUM('FAIBLE', 'MODERE', 'CRITIQUE') NOT NULL DEFAULT 'FAIBLE',
    valeur_observee  DOUBLE COMMENT 'Valeur mesurée (ex: 80 ms)',
    valeur_seuil     DOUBLE COMMENT 'Seuil déclencheur utilisé',
    description      TEXT,
    timestamp        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES quiz_session(id) ON DELETE CASCADE
);

-- ── 8. Cache TTL des questions générées par Claude ────────────
-- Évite les appels API répétés pour le même thème/niveau/type.
CREATE TABLE IF NOT EXISTS cache_question_ia (
    id                INT          AUTO_INCREMENT PRIMARY KEY,
    cle_cache         VARCHAR(255) NOT NULL UNIQUE COMMENT 'SHA-256(theme|niveau|type)',
    enonce            TEXT         NOT NULL,
    reponse_correcte  VARCHAR(500) NOT NULL,
    options           JSON,
    explication       TEXT,
    theme             VARCHAR(100) NOT NULL,
    niveau_difficulte INT          NOT NULL,
    date_creation     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_expiration   DATETIME     NOT NULL COMMENT 'TTL configurable, défaut +24h'
);

-- ── 9. Salons multijoueur ────────────────────────────────────
CREATE TABLE IF NOT EXISTS session_multijoueur (
    id                     INT         AUTO_INCREMENT PRIMARY KEY,
    code_salon             VARCHAR(10) NOT NULL UNIQUE COMMENT 'Code court de 6 chars ex: AX7K2P',
    theme                  VARCHAR(100),
    nombre_questions       INT         NOT NULL DEFAULT 10,
    temps_par_question_sec INT         NOT NULL DEFAULT 30,
    statut                 ENUM('ATTENTE', 'EN_COURS', 'TERMINEE') NOT NULL DEFAULT 'ATTENTE',
    createur_id            INT         NOT NULL,
    date_creation          DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_debut             DATETIME,
    date_fin               DATETIME,
    FOREIGN KEY (createur_id) REFERENCES utilisateur(id)
);

-- ── 10. Participants aux sessions multijoueur ─────────────────
CREATE TABLE IF NOT EXISTS participant_multijoueur (
    id               INT  AUTO_INCREMENT PRIMARY KEY,
    session_multi_id INT  NOT NULL,
    utilisateur_id    INT  NOT NULL,
    score            INT  NOT NULL DEFAULT 0,
    rang_final       INT  COMMENT 'Position au classement (1 = premier)',
    statut           ENUM('CONNECTE', 'DECONNECTE', 'TERMINE') NOT NULL DEFAULT 'CONNECTE',
    date_join        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_session_user (session_multi_id, utilisateur_id),
    FOREIGN KEY (session_multi_id) REFERENCES session_multijoueur(id) ON DELETE CASCADE,
    FOREIGN KEY (utilisateur_id)   REFERENCES utilisateur(id)
);

-- ═══════════════════════════════════════════════════════════════
-- Données initiales — quelques questions de test
-- ═══════════════════════════════════════════════════════════════
INSERT IGNORE INTO question (enonce, type, theme, niveau_difficulte, source, reponse_correcte, options, explication) VALUES
('Quelle est la capitale de la France ?', 'QCM', 'Géographie', 1, 'BANQUE', 'Paris',
 '["Paris","Lyon","Marseille","Bordeaux"]',
 'Paris est la capitale et la plus grande ville de France depuis le Xe siècle.'),

('La Terre est plus grande que le Soleil.', 'VRAI_FAUX', 'Sciences', 1, 'BANQUE', 'Faux',
 '["Vrai","Faux"]',
 'Le Soleil est environ 109 fois plus grand que la Terre en diamètre.'),

('Combien de bits contient un octet ?', 'QCM', 'Informatique', 2, 'BANQUE', '8',
 '["4","8","16","32"]',
 'Un octet (byte) est composé de 8 bits, unité de base de la mémoire informatique.');
