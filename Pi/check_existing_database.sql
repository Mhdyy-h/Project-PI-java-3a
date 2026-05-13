-- CHECK EXISTING DATABASE FOR SPECIALISTE ACCOUNTS
-- This will show all specialist accounts that already exist

-- Show all users with specialist roles
SELECT 
    id,
    nom_complet as 'Full Name',
    email as 'Email',
    LEFT(mot_de_passe, 10) as 'Password (first 10 chars)',
    roles as 'Roles',
    date_inscription as 'Registration Date'
FROM utilisateur 
WHERE roles LIKE '%SPECIALIST%' 
   OR roles LIKE '%specialiste%'
   OR roles LIKE '%SPECIALISTE%'
ORDER BY id;

-- Look specifically for Chamem
SELECT 
    id,
    nom_complet as 'Full Name',
    email as 'Email',
    LEFT(mot_de_passe, 10) as 'Password (first 10 chars)',
    roles as 'Roles'
FROM utilisateur 
WHERE nom_complet LIKE '%Chamem%' 
   OR email LIKE '%chamem%'
ORDER BY id;

-- Check database info
SELECT DATABASE() as 'Current Database', USER() as 'Current User';

-- Show table structure
DESCRIBE utilisateur;
