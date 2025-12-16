-- =============================================
-- SCHEMA DE LA BASE DE DONNÉES MUTUELLE MOBILE
-- PostgreSQL 12+
-- =============================================

-- Table des exercices
CREATE TABLE exercices (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL UNIQUE,
    date_debut DATE NOT NULL,
    date_fin DATE,
    statut VARCHAR(20) NOT NULL DEFAULT 'ACTIF',
    epargne_totale DECIMAL(15, 2) DEFAULT 0,
    solidarite_totale DECIMAL(15, 2) DEFAULT 0,
    emprunt_total DECIMAL(15, 2) DEFAULT 0,
    renflouement_du DECIMAL(15, 2) DEFAULT 0,
    renflouement_paye DECIMAL(15, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT check_statut CHECK (statut IN ('ACTIF', 'CLOTURE'))
);

-- Table des types d'assistance
CREATE TABLE types_assistance (
    id BIGSERIAL PRIMARY KEY,
    nantenn VARCHAR(50) NOT NULL,
    nom VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    montant_reference DECIMAL(15, 2),
    est_actif BOOLEAN DEFAULT TRUE,
    nombre_reutilisations INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des membres
CREATE TABLE membres (
    id BIGSERIAL PRIMARY KEY,
    numero_telephone VARCHAR(20) NOT NULL UNIQUE,
    statut VARCHAR(20) NOT NULL DEFAULT 'ACTIF',
    email VARCHAR(100) NOT NULL UNIQUE,
    photo_profil VARCHAR(255),
    date_inscription DATE NOT NULL,
    montant_inscription_a_paye DECIMAL(15, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT check_statut_membre CHECK (statut IN ('ACTIF', 'INACTIF', 'SUSPENDU', 'RETRAITE'))
);

-- Table des sessions
CREATE TABLE sessions (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    date_session DATE NOT NULL,
    statut VARCHAR(20),
    agape BOOLEAN DEFAULT FALSE,
    montant_solidarite DECIMAL(15, 2) DEFAULT 0,
    exercice_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_session_exercice FOREIGN KEY (exercice_id) 
        REFERENCES exercices(id) ON DELETE CASCADE,
    CONSTRAINT check_statut_session CHECK (statut IN ('PLANIFIEE', 'ACTIVE', 'TERMINEE'))
);

-- Table des assistances
CREATE TABLE assistances (
    id BIGSERIAL PRIMARY KEY,
    montant DECIMAL(15, 2) NOT NULL,
    date_demande TIMESTAMP NOT NULL,
    description TEXT,
    statut VARCHAR(20) NOT NULL DEFAULT 'DEMANDE',
    type_assistance_id BIGINT NOT NULL,
    membre_id BIGINT NOT NULL,
    exercice_id BIGINT NOT NULL,
    est_reutilisation BOOLEAN DEFAULT FALSE,
    assistance_source_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_assistance_type FOREIGN KEY (type_assistance_id) 
        REFERENCES types_assistance(id) ON DELETE RESTRICT,
    CONSTRAINT fk_assistance_membre FOREIGN KEY (membre_id) 
        REFERENCES membres(id) ON DELETE RESTRICT,
    CONSTRAINT fk_assistance_exercice FOREIGN KEY (exercice_id) 
        REFERENCES exercices(id) ON DELETE CASCADE,
    CONSTRAINT fk_assistance_source FOREIGN KEY (assistance_source_id) 
        REFERENCES assistances(id) ON DELETE SET NULL,
    CONSTRAINT check_statut_assistance CHECK (statut IN ('DEMANDE', 'VALIDEE', 'PAYEE', 'REJETEE'))
);

-- Table de configuration de la mutuelle
CREATE TABLE config_mutuelles (
    id BIGSERIAL PRIMARY KEY,
    montant_inscription DECIMAL(15, 2) NOT NULL,
    taux_interet DECIMAL(5, 4) NOT NULL,
    taux_renflouement DECIMAL(5, 4) DEFAULT 0.05,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table de la caisse mutuelle (ID = 1 toujours)
CREATE TABLE caisses_mutuelles (
    id BIGSERIAL PRIMARY KEY,
    epargne_total DECIMAL(15, 2) DEFAULT 0,
    caisse_en_cours DECIMAL(15, 2) DEFAULT 0,
    epargne DECIMAL(15, 2) DEFAULT 0,
    montant_inscription_impaye DECIMAL(15, 2) DEFAULT 0,
    emprunt DECIMAL(15, 2) DEFAULT 0,
    renflouement_impaye DECIMAL(15, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des caisses membres
CREATE TABLE caisses_membres (
    id BIGSERIAL PRIMARY KEY,
    membre_id BIGINT NOT NULL UNIQUE,
    montant_solidarite_impaye DECIMAL(15, 2) DEFAULT 0,
    epargne DECIMAL(15, 2) DEFAULT 0,
    montant_inscription_impaye DECIMAL(15, 2) DEFAULT 0,
    emprunt DECIMAL(15, 2) DEFAULT 0,
    renflouement_impaye DECIMAL(15, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_caisse_membre FOREIGN KEY (membre_id) 
        REFERENCES membres(id) ON DELETE CASCADE
);

-- Table des transactions d'épargne
CREATE TABLE transactions_epargne (
    id BIGSERIAL PRIMARY KEY,
    montant DECIMAL(15, 2) NOT NULL,
    type_transaction VARCHAR(20) NOT NULL,
    session_id BIGINT NOT NULL,
    date_transaction TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description TEXT,
    
    CONSTRAINT fk_epargne_session FOREIGN KEY (session_id) 
        REFERENCES sessions(id) ON DELETE CASCADE,
    CONSTRAINT check_type_epargne CHECK (type_transaction IN ('DEPOT', 'RETRAIT'))
);

-- Table des transactions de solidarité
CREATE TABLE transactions_solidarite (
    id BIGSERIAL PRIMARY KEY,
    montant DECIMAL(15, 2) NOT NULL,
    date_paiement DATE NOT NULL,
    session_id BIGINT NOT NULL,
    
    CONSTRAINT fk_solidarite_session FOREIGN KEY (session_id) 
        REFERENCES sessions(id) ON DELETE CASCADE
);

-- Table des transactions d'emprunt
CREATE TABLE transactions_emprunt (
    id BIGSERIAL PRIMARY KEY,
    montant_emprunte DECIMAL(15, 2) NOT NULL,
    statut VARCHAR(20) NOT NULL,
    date_emprunt DATE NOT NULL,
    taux_interet DECIMAL(5, 4),
    membre_id BIGINT NOT NULL,
    
    CONSTRAINT fk_emprunt_membre FOREIGN KEY (membre_id) 
        REFERENCES membres(id) ON DELETE RESTRICT,
    CONSTRAINT check_statut_emprunt CHECK (statut IN ('EN_COURS', 'REMBOURSE', 'EN_RETARD'))
);

-- Table des transactions d'inscription
CREATE TABLE transactions_inscription (
    id BIGSERIAL PRIMARY KEY,
    montant DECIMAL(15, 2) NOT NULL,
    date_paiement DATE NOT NULL,
    membre_id BIGINT NOT NULL,
    
    CONSTRAINT fk_inscription_membre FOREIGN KEY (membre_id) 
        REFERENCES membres(id) ON DELETE CASCADE
);

-- Table des transactions de renflouement
CREATE TABLE transactions_renflouement (
    id BIGSERIAL PRIMARY KEY,
    montant DECIMAL(15, 2) NOT NULL,
    date_paiement DATE,
    membre_id BIGINT NOT NULL,
    exercice_id BIGINT NOT NULL,
    
    CONSTRAINT fk_renflouement_membre FOREIGN KEY (membre_id) 
        REFERENCES membres(id) ON DELETE RESTRICT,
    CONSTRAINT fk_renflouement_exercice FOREIGN KEY (exercice_id) 
        REFERENCES exercices(id) ON DELETE CASCADE
);

-- Table des remboursements
CREATE TABLE remboursements (
    id BIGSERIAL PRIMARY KEY,
    montant DECIMAL(15, 2) NOT NULL,
    date_remboursement DATE NOT NULL,
    id_current_session BIGINT,
    membre_id BIGINT NOT NULL,
    
    CONSTRAINT fk_remboursement_membre FOREIGN KEY (membre_id) 
        REFERENCES membres(id) ON DELETE RESTRICT
);

-- =============================================
-- INDEX POUR LES PERFORMANCES
-- =============================================

-- Index pour exercices
CREATE INDEX idx_exercices_statut ON exercices(statut);
CREATE INDEX idx_exercices_date_debut ON exercices(date_debut);
CREATE INDEX idx_exercices_date_fin ON exercices(date_fin);

-- Index pour assistances
CREATE INDEX idx_assistances_exercice ON assistances(exercice_id);
CREATE INDEX idx_assistances_membre ON assistances(membre_id);
CREATE INDEX idx_assistances_type ON assistances(type_assistance_id);
CREATE INDEX idx_assistances_statut ON assistances(statut);

-- Index pour sessions
CREATE INDEX idx_sessions_exercice ON sessions(exercice_id);
CREATE INDEX idx_sessions_date ON sessions(date_session);

-- Index pour transactions
CREATE INDEX idx_transactions_epargne_session ON transactions_epargne(session_id);
CREATE INDEX idx_transactions_solidarite_session ON transactions_solidarite(session_id);
CREATE INDEX idx_transactions_emprunt_membre ON transactions_emprunt(membre_id);
CREATE INDEX idx_transactions_emprunt_statut ON transactions_emprunt(statut);
CREATE INDEX idx_transactions_renflouement_exercice ON transactions_renflouement(exercice_id);

-- Index pour membres
CREATE INDEX idx_membres_statut ON membres(statut);
CREATE INDEX idx_membres_date_inscription ON membres(date_inscription);

-- Index pour caisses
CREATE INDEX idx_caisses_membres_membre ON caisses_membres(membre_id);

-- =============================================
-- DONNÉES INITIALES
-- =============================================

-- Configuration par défaut
INSERT INTO config_mutuelles (montant_inscription, taux_interet, taux_renflouement) 
VALUES (5000, 0.10, 0.05);

-- Caisse mutuelle initiale (ID = 1 toujours)
INSERT INTO caisses_mutuelles (id, epargne_total, caisse_en_cours) 
VALUES (1, 0, 0) 
ON CONFLICT (id) DO NOTHING;

-- Types d'assistance par défaut
INSERT INTO types_assistance (nantenn, nom, description, montant_reference) VALUES
('ASST001', 'Aide médicale', 'Assistance pour frais médicaux', 50000),
('ASST002', 'Aide scolaire', 'Assistance pour frais scolaires', 30000),
('ASST003', 'Aide funéraire', 'Assistance pour frais funéraires', 100000),
('ASST004', 'Aide naissance', 'Assistance pour naissance', 25000);

-- Premier exercice par défaut
INSERT INTO exercices (nom, date_debut, statut) 
VALUES ('Exercice 2025', CURRENT_DATE, 'ACTIF');

-- =============================================
-- FONCTIONS ET TRIGGERS
-- =============================================

-- Fonction pour mettre à jour updated_at automatiquement
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers pour updated_at
CREATE TRIGGER update_exercices_updated_at BEFORE UPDATE ON exercices
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_membres_updated_at BEFORE UPDATE ON membres
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_caisses_mutuelles_updated_at BEFORE UPDATE ON caisses_mutuelles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_caisses_membres_updated_at BEFORE UPDATE ON caisses_membres
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_config_mutuelles_updated_at BEFORE UPDATE ON config_mutuelles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Fonction pour créer automatiquement la caisse d'un nouveau membre
CREATE OR REPLACE FUNCTION create_caisse_for_new_membre()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO caisses_membres (membre_id) VALUES (NEW.id);
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger pour créer caisse automatiquement
CREATE TRIGGER create_caisse_after_membre_insert AFTER INSERT ON membres
    FOR EACH ROW EXECUTE FUNCTION create_caisse_for_new_membre();

-- Vue pour les statistiques des exercices
CREATE VIEW statistiques_exercices AS
SELECT 
    e.id,
    e.nom,
    e.date_debut,
    e.date_fin,
    e.statut,
    e.epargne_totale,
    e.solidarite_totale,
    e.emprunt_total,
    e.renflouement_du,
    e.renflouement_paye,
    COUNT(DISTINCT s.id) as nombre_sessions,
    COUNT(DISTINCT a.id) as nombre_assistances,
    COUNT(DISTINCT m.id) as nombre_membres_inscrits
FROM exercices e
LEFT JOIN sessions s ON s.exercice_id = e.id
LEFT JOIN assistances a ON a.exercice_id = e.id
LEFT JOIN membres m ON m.date_inscription BETWEEN e.date_debut AND COALESCE(e.date_fin, CURRENT_DATE)
GROUP BY e.id;

-- Vue pour les rapports financiers
CREATE VIEW rapports_financiers AS
SELECT 
    e.id as exercice_id,
    e.nom as exercice_nom,
    e.epargne_totale,
    e.solidarite_totale,
    e.emprunt_total,
    e.renflouement_du,
    e.renflouement_paye,
    COALESCE(SUM(te.montant), 0) as total_depots_epargne,
    COALESCE(SUM(CASE WHEN te.type_transaction = 'RETRAIT' THEN te.montant ELSE 0 END), 0) as total_retraits_epargne,
    COALESCE(SUM(ts.montant), 0) as total_solidarite,
    COALESCE(SUM(em.montant_emprunte), 0) as total_emprunts,
    COALESCE(SUM(tr.montant), 0) as total_renflouement_paye
FROM exercices e
LEFT JOIN sessions s ON s.exercice_id = e.id
LEFT JOIN transactions_epargne te ON te.session_id = s.id
LEFT JOIN transactions_solidarite ts ON ts.session_id = s.id
LEFT JOIN transactions_emprunt em ON em.date_emprunt BETWEEN e.date_debut AND COALESCE(e.date_fin, CURRENT_DATE)
LEFT JOIN transactions_renflouement tr ON tr.exercice_id = e.id AND tr.date_paiement IS NOT NULL
GROUP BY e.id, e.nom;
