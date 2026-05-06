# Configuration de la Base de Données BioSync

## Prérequis

- MySQL Server installé et démarré
- Accès root à MySQL

## Installation Automatique (Windows)

1. Ouvrez un terminal dans le dossier `database/`
2. Exécutez le script :
   ```bash
   setup_database.bat
   ```
3. Entrez votre mot de passe MySQL root quand demandé (laissez vide si pas de mot de passe)

## Installation Manuelle

### Option 1 : Via ligne de commande MySQL

```bash
# Sans mot de passe
mysql -u root < create_database.sql

# Avec mot de passe
mysql -u root -p < create_database.sql
```

### Option 2 : Via MySQL Workbench

1. Ouvrez MySQL Workbench
2. Connectez-vous à votre serveur MySQL
3. Ouvrez le fichier `create_database.sql`
4. Exécutez le script (⚡ Execute)

### Option 3 : Via phpMyAdmin

1. Ouvrez phpMyAdmin
2. Cliquez sur l'onglet "SQL"
3. Copiez-collez le contenu de `create_database.sql`
4. Cliquez sur "Exécuter"

## Comptes de Test Créés

Après l'installation, vous pouvez vous connecter avec :

| Rôle  | Email                  | Mot de passe |
|-------|------------------------|--------------|
| Admin | admin@biosync.com      | admin123     |
| Coach | coach@biosync.com      | coach123     |
| User  | user@biosync.com       | user123      |

## Structure de la Base de Données

### Tables créées :

1. **utilisateurs** - Gestion des utilisateurs
2. **quiz** - Gestion des quiz mentaux
3. **questions** - Questions des quiz
4. **rate_limiting** - Limitation des tentatives de connexion

### Données de test :

- 3 utilisateurs (admin, coach, user)
- 3 quiz de démonstration
- 3 questions pour le premier quiz

## Vérification

Pour vérifier que tout fonctionne :

```sql
USE biosync;
SHOW TABLES;
SELECT * FROM utilisateurs;
SELECT * FROM quiz;
SELECT * FROM questions;
```

## Dépannage

### Erreur : "Access denied for user 'root'@'localhost'"

- Vérifiez votre mot de passe MySQL root
- Assurez-vous que MySQL est démarré

### Erreur : "mysql: command not found"

- Ajoutez MySQL au PATH système
- Ou utilisez le chemin complet : `"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"`

### Erreur : "Can't connect to MySQL server"

- Démarrez le service MySQL :
  ```bash
  net start MySQL80
  ```
- Ou via Services Windows (services.msc)

## Configuration de l'Application

Le fichier `src/main/resources/config.properties` contient :

```properties
db.url=jdbc:mysql://localhost:3306/biosync
db.username=root
db.password=
```

Modifiez ces valeurs si nécessaire pour correspondre à votre configuration MySQL.
