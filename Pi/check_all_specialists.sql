-- Check ALL users with specialist roles (both French and English variations)
SELECT 
    id,
    nom_complet as 'Full Name',
    email as 'Email',
    mot_de_passe as 'Password',
    roles as 'Roles',
    date_inscription as 'Registration Date'
FROM utilisateur 
WHERE roles LIKE '%SPECIALIST%' 
   OR roles LIKE '%specialiste%'
   OR roles LIKE '%SPECIALISTE%'
   OR nom_complet LIKE '%Dr%'
ORDER BY id;

-- Also check for any similar emails
SELECT 
    id,
    nom_complet as 'Name',
    email as 'Email',
    mot_de_passe as 'Password'
FROM utilisateur 
WHERE email LIKE '%chamem%' 
   OR nom_complet LIKE '%Chamem%'
ORDER BY id;

-- Show exact table structure
DESCRIBE utilisateur;
