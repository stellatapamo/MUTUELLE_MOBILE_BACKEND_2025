-- =============================================
-- DONNÉES DE TEST POUR LE DÉVELOPPEMENT
-- =============================================

-- Nettoyer les données existantes (optionnel)
-- DELETE FROM remboursements;
-- DELETE FROM transactions_renflouement;
-- DELETE FROM transactions_inscription;
-- DELETE FROM transactions_emprunt;
-- DELETE FROM transactions_solidarite;
-- DELETE FROM transactions_epargne;
-- DELETE FROM assistances;
-- DELETE FROM sessions;
-- DELETE FROM caisses_membres;
-- DELETE FROM membres;
-- DELETE FROM exercices;

-- Membres de test
INSERT INTO membres (numero_telephone, statut, email, date_inscription, montant_inscription_a_paye) VALUES
('237699887766', 'ACTIF', 'zemendouga@mutuelle.cm', '2025-01-15', 5000),
('237688776655', 'ACTIF', 'franky@mutuelle.cm', '2025-01-20', 5000),
('237677665544', 'ACTIF', 'stella@mutuelle.cm', '2025-02-01', 5000),
('237666554433', 'ACTIF', 'kevine@mutuelle.cm', '2025-02-10', 0),
('237655443322', 'ACTIF', 'darelle@mutuelle.cm', '2025-02-15', 5000);

-- Sessions de test pour l'exercice 2025
INSERT INTO sessions (nom, date_session, statut, exercice_id, montant_solidarite) VALUES
('Session Janvier 2025', '2025-01-31', 'TERMINEE', 1, 10000),
('Session Février 2025', '2025-02-28', 'TERMINEE', 1, 12000),
('Session Mars 2025', '2025-03-31', 'ACTIVE', 1, 0);

-- Transactions d'épargne de test
INSERT INTO transactions_epargne (montant, type_transaction, session_id, description) VALUES
(5000, 'DEPOT', 1, 'Épargne membre 1 - Janvier'),
(5000, 'DEPOT', 1, 'Épargne membre 2 - Janvier'),
(5000, 'DEPOT', 2, 'Épargne membre 1 - Février'),
(5000, 'DEPOT', 2, 'Épargne membre 2 - Février'),
(10000, 'DEPOT', 3, 'Épargne membre 3 - Mars');

-- Assistances de test
INSERT INTO assistances (montant, date_demande, description, statut, type_assistance_id, membre_id, exercice_id) VALUES
(50000, '2025-02-15 10:30:00', 'Aide médicale pour consultation', 'VALIDEE', 1, 1, 1),
(30000, '2025-03-01 14:45:00', 'Frais scolaires enfants', 'DEMANDE', 2, 2, 1);

-- Transactions de solidarité
INSERT INTO transactions_solidarite (montant, date_paiement, session_id) VALUES
(5000, '2025-01-31', 1),
(5000, '2025-01-31', 1),
(6000, '2025-02-28', 2),
(6000, '2025-02-28', 2);

-- Mettre à jour les totaux de l'exercice
UPDATE exercices SET 
    epargne_totale = 30000,
    solidarite_totale = 22000,
    renflouement_du = 1500
WHERE id = 1;

-- Mettre à jour la caisse mutuelle
UPDATE caisses_mutuelles SET 
    epargne_total = 30000,
    caisse_en_cours = 30000,
    renflouement_impaye = 1500
WHERE id = 1;

-- Mettre à jour les caisses membres
UPDATE caisses_membres SET 
    epargne = CASE membre_id 
        WHEN 1 THEN 10000 
        WHEN 2 THEN 10000 
        WHEN 3 THEN 10000 
        ELSE 0 
    END,
    renflouement_impaye = CASE membre_id 
        WHEN 1 THEN 500 
        WHEN 2 THEN 500 
        WHEN 3 THEN 500 
        ELSE 0 
    END
WHERE membre_id IN (1, 2, 3);

-- =============================================
-- REQUÊTES DE VÉRIFICATION
-- =============================================

-- Vérifier l'exercice actif
SELECT * FROM exercices WHERE statut = 'ACTIF';

-- Vérifier les membres
SELECT id, numero_telephone, email, statut FROM membres;

-- Vérifier les sessions
SELECT s.id, s.nom, s.date_session, e.nom as exercice 
FROM sessions s 
JOIN exercices e ON s.exercice_id = e.id;

-- Vérifier les assistances
SELECT a.id, a.montant, a.description, ta.nom as type_assistance, 
       m.email as demandeur, a.statut
FROM assistances a
JOIN types_assistance ta ON a.type_assistance_id = ta.id
JOIN membres m ON a.membre_id = m.id;

-- Vérifier les statistiques financières
SELECT * FROM statistiques_exercices;
SELECT * FROM rapports_financiers;
