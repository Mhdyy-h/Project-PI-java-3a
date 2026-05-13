-- List all users in the database
SELECT 
    id,
    nom_complet as 'Full Name',
    email as 'Email',
    roles as 'Role',
    LEFT(mot_de_passe, 10) as 'Password (first 10 chars)',
    date_inscription as 'Registration Date'
FROM utilisateur 
ORDER BY id;
