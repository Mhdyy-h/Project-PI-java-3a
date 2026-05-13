# 🔧 Correction de l'Erreur de Connexion

## ❌ Erreur Actuelle

```
Login error: Cannot invoke "java.sql.Connection.prepareStatement(String)" 
because "conn" is null
```

## 🎯 Cause

La base de données `biosync` **n'existe pas encore** sur votre serveur MySQL.

## ✅ Solution en 3 Étapes

### Étape 1 : Vérifier que MySQL est démarré

**Option A - Via Services Windows :**
1. Appuyez sur `Win + R`
2. Tapez `services.msc` et validez
3. Cherchez "MySQL" dans la liste
4. Si arrêté : Clic droit → Démarrer

**Option B - Via ligne de commande (Admin) :**
```bash
net start MySQL80
```

### Étape 2 : Créer la Base de Données

**🎯 Méthode Recommandée - Script Automatique :**

1. Ouvrez un terminal dans le dossier du projet
2. Exécutez :
   ```bash
   cd database
   setup_database_simple.bat
   ```
3. Entrez votre mot de passe MySQL (ou laissez vide)

**Alternative - Ligne de Commande MySQL :**

```bash
# Ouvrez MySQL
mysql -u root -p

# Puis exécutez
source database/create_database.sql
```

**Alternative - MySQL Workbench :**

1. Ouvrez MySQL Workbench
2. File → Open SQL Script
3. Sélectionnez `database/create_database.sql`
4. Cliquez sur ⚡ Execute

### Étape 3 : Vérifier l'Installation

Exécutez le script de vérification :
```bash
verify_database.bat
```

Ou vérifiez manuellement :
```sql
USE biosync;
SHOW TABLES;
SELECT * FROM utilisateurs;
```

## 🎉 Résultat Attendu

Après l'installation, vous devriez voir :

```
✅ Base de données créée avec succès !

📊 Contenu :
   - 3 utilisateurs
   - 3 quiz
   - 3 questions
```

## 🔐 Comptes de Test

Une fois la base créée, connectez-vous avec :

| Rôle  | Email                 | Mot de passe |
|-------|-----------------------|--------------|
| Admin | admin@biosync.com     | admin123     |
| Coach | coach@biosync.com     | coach123     |
| User  | user@biosync.com      | user123      |

## 🚀 Relancer l'Application

Après avoir créé la base de données :

1. Dans IntelliJ IDEA, cliquez sur ▶️ Run
2. Ou utilisez : `mvn javafx:run`
3. Connectez-vous avec un des comptes de test

## ❓ Problèmes Courants

### "mysql: command not found"

MySQL n'est pas dans le PATH. Utilisez le chemin complet :

```bash
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < database/create_database.sql
```

### "Access denied for user 'root'"

Vérifiez votre mot de passe MySQL dans `src/main/resources/config.properties` :

```properties
db.url=jdbc:mysql://localhost:3306/biosync
db.username=root
db.password=VOTRE_MOT_DE_PASSE_ICI
```

### "Can't connect to MySQL server"

MySQL n'est pas démarré :
```bash
net start MySQL80
```

### "Unknown database 'biosync'"

La base n'a pas été créée. Retournez à l'Étape 2.

## 📞 Besoin d'Aide ?

1. Consultez `QUICK_START.md`
2. Consultez `database/README.md`
3. Vérifiez que MySQL est installé : `mysql --version`
4. Vérifiez que le service MySQL est démarré

## 🔄 Ordre des Opérations

```
1. Démarrer MySQL
   ↓
2. Créer la base de données (database/setup_database_simple.bat)
   ↓
3. Vérifier l'installation (verify_database.bat)
   ↓
4. Lancer l'application (mvn javafx:run)
   ↓
5. Se connecter avec un compte de test
```

---

**✅ Une fois la base de données créée, l'erreur de connexion disparaîtra !**
