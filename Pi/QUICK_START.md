# 🚀 Guide de Démarrage Rapide - BioSync

## ❌ Problème Actuel

L'application ne démarre pas car la base de données `biosync` n'existe pas.

```
Database connection failed: Unknown database 'biosync'
```

## ✅ Solution en 3 Étapes

### Étape 1 : Vérifier que MySQL est démarré

**Option A - Via Services Windows :**
1. Appuyez sur `Win + R`
2. Tapez `services.msc` et validez
3. Cherchez "MySQL" dans la liste
4. Clic droit → Démarrer (si arrêté)

**Option B - Via ligne de commande (Admin) :**
```bash
net start MySQL80
```

### Étape 2 : Créer la base de données

**🎯 Méthode Recommandée - Script Automatique :**

1. Ouvrez un terminal dans le dossier du projet
2. Allez dans le dossier database :
   ```bash
   cd database
   ```
3. Exécutez le script :
   ```bash
   setup_database.bat
   ```
4. Entrez votre mot de passe MySQL root (ou laissez vide)

**Alternative - Méthode Manuelle :**

```bash
# Ouvrez MySQL en ligne de commande
mysql -u root -p

# Puis exécutez :
source database/create_database.sql
```

**Alternative - Via MySQL Workbench :**

1. Ouvrez MySQL Workbench
2. Connectez-vous à votre serveur
3. File → Open SQL Script → Sélectionnez `database/create_database.sql`
4. Cliquez sur l'éclair ⚡ pour exécuter

### Étape 3 : Lancer l'application

Dans IntelliJ IDEA :
1. Cliquez sur le bouton ▶️ Run
2. Ou utilisez Maven : `mvn javafx:run`

## 🎉 Comptes de Test

Une fois la base créée, vous pouvez vous connecter avec :

| Rôle          | Email                 | Mot de passe |
|---------------|-----------------------|--------------|
| 👑 Admin      | admin@biosync.com     | admin123     |
| 🏋️ Coach      | coach@biosync.com     | coach123     |
| 👤 Utilisateur| user@biosync.com      | user123      |

## 🔧 Configuration MySQL

Si vous avez un mot de passe MySQL différent, modifiez :

**Fichier :** `src/main/resources/config.properties`

```properties
db.url=jdbc:mysql://localhost:3306/biosync
db.username=root
db.password=VOTRE_MOT_DE_PASSE_ICI
```

## 📊 Contenu de la Base de Données

Après installation, vous aurez :
- ✅ 3 utilisateurs de test
- ✅ 3 quiz de démonstration
- ✅ 3 questions d'exemple
- ✅ Tables pour rate limiting

## ❓ Problèmes Courants

### "mysql: command not found"

MySQL n'est pas dans le PATH. Utilisez le chemin complet :

```bash
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < database/create_database.sql
```

### "Access denied for user 'root'"

Vérifiez votre mot de passe MySQL dans `config.properties`

### "Can't connect to MySQL server"

MySQL n'est pas démarré. Voir Étape 1.

## 📚 Documentation Complète

Pour plus de détails, consultez : `database/README.md`

## 🆘 Besoin d'Aide ?

Si vous rencontrez toujours des problèmes :
1. Vérifiez que MySQL est installé : `mysql --version`
2. Vérifiez que le service MySQL est démarré
3. Testez la connexion : `mysql -u root -p`
4. Consultez les logs dans le terminal

---

**Bon développement ! 🚀**
