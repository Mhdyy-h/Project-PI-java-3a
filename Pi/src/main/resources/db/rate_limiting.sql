-- Table pour stocker les tentatives de connexion (rate limiting)
-- Cette table persiste les blocages entre les redémarrages de l'application

CREATE TABLE IF NOT EXISTS rate_limiting (
    email VARCHAR(255) PRIMARY KEY,
    attempt_count INT NOT NULL DEFAULT 0,
    last_attempt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Index pour les recherches rapides
CREATE INDEX IF NOT EXISTS idx_rate_limiting_last_attempt ON rate_limiting(last_attempt);

COMMENT ON TABLE rate_limiting IS 'Stockage des tentatives de connexion pour rate limiting';
