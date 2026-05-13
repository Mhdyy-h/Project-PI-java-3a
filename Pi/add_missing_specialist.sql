-- Add the missing specialist account that you're trying to login with
INSERT IGNORE INTO utilisateur 
(nom_complet, email, mot_de_passe, roles, score_global, date_inscription) 
VALUES 
('Dr Chamem', 'dr.chamem@biosync.com', 'Azerty123', '["ROLE_SPECIALISTE","ROLE_USER"]', 0, CURDATE());

-- Verify the account was created
SELECT 
    id,
    nom_complet as 'Full Name',
    email as 'Email',
    roles as 'Roles',
    'Password: Azerty123' as 'Password'
FROM utilisateur 
WHERE email = 'dr.chamem@biosync.com';

-- Show all specialists for reference
SELECT 
    id,
    nom_complet as 'Name',
    email as 'Email',
    LEFT(mot_de_passe, 10) as 'Password (first 10 chars)',
    roles as 'Roles'
FROM utilisateur 
WHERE roles LIKE '%SPECIALIST%' OR roles LIKE '%specialiste%'
ORDER BY id;
