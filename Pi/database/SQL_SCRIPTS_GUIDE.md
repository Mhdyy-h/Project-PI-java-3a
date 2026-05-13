# 📚 Guide des Scripts SQL - BioSync

## 📁 Scripts Disponibles

### 1. `create_database.sql` ⭐ (Recommandé pour démarrer)
**Script de base - Rapide et simple**

- Crée la base `biosync`
- Crée 4 tables essentielles :
  - `utilisateurs`
  - `quiz`
  - `questions`
  - `rate_limiting`
- Insère 3 utilisateurs de test
- Insère 3 quiz de démonstration
- Insère 3 questions d'exemple

**Utilisation :**
```bash
mysql -u root -p < create_database.sql
```

---

### 2. `create_database_complete.sql` 🚀 (Complet)
**Script complet - Toutes les fonctionnalités**

- Crée la base `biosync`
- Crée **25+ tables** pour tous les modules :
  - 👥 Utilisateurs et certifications
  - 🧠 Quiz et questions (module mental)
  - 💪 Exercices et séances (module sport)
  - 🥗 Aliments et repas (module nutrition)
  - 👥 Groupes et communauté
  - 📅 Événements
  - 💬 Messagerie
  - 📊 Logs et monitoring
- Insère des données de test complètes

**Utilisation :**
```bash
mysql -u root -p < create_database_complete.sql
```

---

### 3. `update_database.sql` 🔄 (Mise à jour)
**Script de mise à jour - Sans perte de données**

- Ajoute les tables manquantes
- Ajoute les colonnes manquantes aux tables existantes
- **Ne supprime AUCUNE donnée existante**
- Idéal pour mettre à jour une base existante

**Utilisation :**
```bash
mysql -u root -p < update_database.sql
```

---

## 🎯 Quel Script Utiliser ?

### Nouveau Projet (Base de données vide)

**Option 1 - Démarrage Rapide** ⚡
```bash
cd database
setup_database_simple.bat
```
Utilise automatiquement `create_database.sql`

**Option 2 - Installation Complète** 🚀
```bash
mysql -u root -p < create_database_complete.sql
```

### Projet Existant (Mise à jour)

```bash
mysql -u root -p < update_database.sql
```

---

## 📊 Comparaison des Scripts

| Caractéristique | create_database.sql | create_database_complete.sql | update_database.sql |
|----------------|---------------------|------------------------------|---------------------|
| **Tables créées** | 4 | 25+ | Variable |
| **Données de test** | Basique | Complète | Aucune |
| **Temps d'exécution** | ~5 sec | ~15 sec | ~10 sec |
| **Taille** | Petit | Grand | Moyen |
| **Usage recommandé** | Démarrage rapide | Production | Mise à jour |
| **Supprime données** | Non | Non | Non |

---

## 🔧 Méthodes d'Exécution

### Méthode 1 : Script Automatique (Recommandé)

```bash
cd database
setup_database_simple.bat
```

### Méthode 2 : Ligne de Commande MySQL

```bash
# Se connecter à MySQL
mysql -u root -p

# Exécuter le script
source database/create_database.sql
# OU
source database/create_database_complete.sql
# OU
source database/update_database.sql
```

### Méthode 3 : Commande Directe

```bash
# Sans mot de passe
mysql -u root < database/create_database.sql

# Avec mot de passe
mysql -u root -p < database/create_database.sql
```

### Méthode 4 : MySQL Workbench

1. Ouvrir MySQL Workbench
2. File → Open SQL Script
3. Sélectionner le script désiré
4. Cliquer sur ⚡ Execute

### Méthode 5 : phpMyAdmin

1. Ouvrir phpMyAdmin
2. Onglet "SQL"
3. Copier-coller le contenu du script
4. Cliquer sur "Exécuter"

---

## 📋 Structure Complète de la Base

### Module Utilisateurs
- `utilisateurs` - Comptes utilisateurs
- `certifications` - Certifications des coachs
- `rate_limiting` - Sécurité connexion

### Module Mental (Quiz)
- `quiz` - Quiz mentaux
- `questions` - Questions des quiz
- `quiz_sessions` - Historique des passages
- `reponses_utilisateur` - Réponses données

### Module Sport
- `exercices` - Catalogue d'exercices
- `seances_sport` - Séances planifiées
- `seance_exercices` - Exercices par séance
- `progression_exercices` - Suivi progression

### Module Nutrition
- `aliments` - Catalogue d'aliments
- `repas` - Repas enregistrés
- `repas_aliments` - Composition des repas
- `objectifs_nutritionnels` - Objectifs utilisateur

### Module Communauté
- `groupes` - Groupes d'utilisateurs
- `membres_groupe` - Membres des groupes
- `evenements` - Événements
- `participants_evenement` - Participants

### Module Communication
- `messages` - Messagerie interne

### Module Monitoring
- `activity_logs` - Logs d'activité
- `alertes` - Notifications utilisateur

---

## 🔐 Comptes de Test

Tous les scripts créent ces comptes :

| Rôle | Email | Mot de passe | Permissions |
|------|-------|--------------|-------------|
| 👑 Admin | admin@biosync.com | admin123 | ADMIN, COACH, USER |
| 🏋️ Coach | coach@biosync.com | coach123 | COACH, USER |
| 👤 User | user@biosync.com | user123 | USER |

---

## 🧪 Vérification Post-Installation

### Vérifier que la base existe

```sql
SHOW DATABASES LIKE 'biosync';
```

### Vérifier les tables

```sql
USE biosync;
SHOW TABLES;
```

### Vérifier les données

```sql
SELECT COUNT(*) FROM utilisateurs;
SELECT COUNT(*) FROM quiz;
SELECT COUNT(*) FROM questions;
```

### Script de vérification automatique

```bash
verify_database.bat
```

---

## 🆘 Dépannage

### Erreur : "Access denied"

Vérifiez votre mot de passe MySQL :
```bash
mysql -u root -p
```

### Erreur : "Database exists"

La base existe déjà. Options :
1. Utiliser `update_database.sql` pour mettre à jour
2. Supprimer et recréer :
   ```sql
   DROP DATABASE biosync;
   ```
   Puis relancer le script

### Erreur : "Table already exists"

Normal avec `CREATE TABLE IF NOT EXISTS`. Le script continue.

### Erreur : "Foreign key constraint fails"

L'ordre des tables est important. Utilisez les scripts fournis qui respectent les dépendances.

---

## 📝 Personnalisation

### Modifier les données de test

Éditez la section "DONNÉES DE TEST" dans le script :

```sql
-- Modifier les utilisateurs
INSERT INTO utilisateurs (nom_complet, email, mot_de_passe, roles) 
VALUES 
    ('Votre Nom', 'votre@email.com', 'votre_mdp', 'ROLE_USER');
```

### Ajouter des tables personnalisées

Ajoutez vos tables à la fin du script, avant la section "AFFICHER UN RÉSUMÉ".

---

## 🔄 Migration de Données

### Exporter les données existantes

```bash
mysqldump -u root -p biosync > backup_biosync.sql
```

### Importer dans la nouvelle base

```bash
mysql -u root -p biosync < backup_biosync.sql
```

---

## 📞 Support

Pour toute question :
1. Consultez `README.md` dans le dossier database
2. Consultez `QUICK_START.md` à la racine
3. Consultez `FIX_LOGIN_ERROR.md` pour les problèmes de connexion

---

**✅ Choisissez le script adapté à vos besoins et suivez les instructions !**
