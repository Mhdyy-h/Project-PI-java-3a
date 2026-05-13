# BioSync - Gestion des Rendez-vous

## 📋 Overview

BioSync est une application de gestion de rendez-vous médicaux développée en Java avec JavaFX. Elle permet aux patients de prendre rendez-vous avec des spécialistes, aux spécialistes de gérer leurs consultations, et aux administrateurs de superviser l'ensemble du système.

## 🚀 Features

### Pour les Patients (Utilisateurs Réguliers)
- ✅ Créer des rendez-vous avec choix de spécialiste
- ✅ Voir uniquement leurs propres rendez-vous non assignés
- ✅ Modifier la date, l'heure et le motif de leurs rendez-vous
- ✅ Supprimer leurs rendez-vous non confirmés
- ✅ Voir leurs prescriptions assignées par les spécialistes
- ✅ Rejoindre les téléconsultations actives

### Pour les Spécialistes
- ✅ Voir uniquement les rendez-vous qui leur sont assignés
- ✅ Confirmer ou annuler les rendez-vous assignés
- ✅ Démarrer des consultations (présentielles ou téléconsultations)
- ✅ Créer des prescriptions pour les patients
- ✅ Gérer les consultations en cours
- ✅ Activer les sessions de téléconsultation

### Pour les Administrateurs
- ✅ Accès complet à tous les rendez-vous
- ✅ Gestion des utilisateurs et spécialistes
- ✅ Supervision globale du système

## 🔧 Installation

### Prérequis
- Java 17 ou supérieur
- Maven 3.6+
- MySQL 8.0+
- JavaFX 21

### Configuration de la Base de Données

1. Créer la base de données MySQL:
```sql
CREATE DATABASE biosync_db;
```

2. Les tables seront créées automatiquement au premier lancement de l'application.

### Lancement de l'Application

```bash
mvn clean javafx:run
```

Ou via IntelliJ IDEA:
1. Ouvrir le projet
2. Configurer le SDK Java (17+)
3. Exécuter la classe principale avec le plugin JavaFX Maven

## 👤 Rôles Utilisateurs

### 1. Patient (ROLE_USER)
- Peut créer des rendez-vous
- Peut modifier ses rendez-vous non assignés (date, heure, motif)
- Peut supprimer ses rendez-vous non confirmés
- Peut voir ses prescriptions
- Peut rejoindre les téléconsultations actives

### 2. Spécialiste (ROLE_SPECIALISTE)
- Peut voir les rendez-vous qui lui sont assignés
- Peut confirmer/annuler les rendez-vous
- Peut démarrer des consultations
- Peut créer des prescriptions
- Peut activer les téléconsultations

### 3. Administrateur (ROLE_ADMIN)
- Accès complet à toutes les fonctionnalités
- Gestion des utilisateurs
- Supervision du système

## 📱 Guide d'Utilisation

### Connexion

1. **Patient**: Utiliser votre email et mot de passe
2. **Spécialiste**: Utiliser votre email professionnel et mot de passe
3. **Administrateur**: Utiliser les identifiants admin

### Gestion des Rendez-vous (Patients)

#### Créer un Rendez-vous
1. Cliquer sur "Nouveau Rendez-vous"
2. Sélectionner le spécialiste souhaité
3. Choisir la date et l'heure
4. Entrer le motif de consultation
5. Sélectionner le mode (présentiel/téléconsultation)
6. Cliquer sur "Enregistrer"

#### Modifier un Rendez-vous
1. Sélectionner le rendez-vous dans le tableau
2. Cliquer sur le bouton "Modifier" (📝)
3. Modifier uniquement la date, l'heure ou le motif
4. Cliquer sur "Enregistrer"

**Note**: Les patients ne peuvent modifier que les rendez-vous non assignés à un spécialiste.

#### Supprimer un Rendez-vous
1. Sélectionner le rendez-vous
2. Cliquer sur le bouton "Supprimer" (🗑️)
3. Confirmer la suppression

### Gestion des Rendez-vous (Spécialistes)

#### Voir les Rendez-vous Assignés
- Le tableau affiche uniquement les rendez-vous qui vous sont assignés
- Si aucun rendez-vous n'est assigné, cliquez sur "Actualiser" pour en assigner automatiquement

#### Confirmer un Rendez-vous
1. Cliquer sur le bouton "Confirmer" (✅) dans la colonne Actions
2. Le statut passe à "confirmé"
3. Le patient ne peut plus modifier ce rendez-vous

#### Annuler un Rendez-vous
1. Cliquer sur le bouton "Annuler" (❌) dans la colonne Actions
2. Le statut passe à "annulé"
3. Le patient peut créer un nouveau rendez-vous

#### Démarrer une Consultation
1. Sélectionner un rendez-vous confirmé
2. Cliquer sur le bouton "Démarrer Consultation" (🩺)
3. La consultation s'ouvre dans une nouvelle fenêtre
4. Remplir les symptômes, diagnostic et recommandations
5. Ajouter des prescriptions si nécessaire
6. Cliquer sur "Terminer" pour finaliser

### Téléconsultations

#### Pour les Spécialistes
1. Confirmer le rendez-vous
2. Cliquer sur "Démarrer Consultation"
3. Sélectionner "téléconsultation" comme mode
4. La session devient active automatiquement
5. Le patient peut rejoindre avec le bouton "Rejoindre" (📞)

#### Pour les Patients
1. Attendre que le spécialiste confirme le rendez-vous
2. Attendre que la téléconsultation soit active
3. Cliquer sur le bouton "Rejoindre" (📞) dans la colonne Actions
4. La téléconsultation s'ouvre

### Gestion des Prescriptions

#### Pour les Spécialistes
1. Démarrer une consultation
2. Cliquer sur "Ajouter Prescription"
3. Entrer les détails du médicament:
   - Nom du médicament
   - Dose
   - Fréquence
   - Durée (en jours)
   - Instructions
4. Cliquer sur "Enregistrer"
5. La prescription est assignée au patient

#### Pour les Patients
- Les prescriptions apparaissent dans leur interface
- Ils peuvent voir les détails de chaque prescription
- Ils ne peuvent pas créer de prescriptions

## 🎯 Workflow Typique

### Scénario 1: Consultation Présentielle
1. **Patient** crée un rendez-vous
2. **Spécialiste** reçoit le rendez-vous (assigné automatiquement)
3. **Spécialiste** confirme le rendez-vous
4. **Patient** se présente à la date/heure prévue
5. **Spécialiste** démarre la consultation
6. **Spécialiste** crée des prescriptions
7. Consultation terminée

### Scénario 2: Téléconsultation
1. **Patient** crée un rendez-vous en mode "téléconsultation"
2. **Spécialiste** confirme le rendez-vous
3. **Spécialiste** démarre la consultation
4. Session de téléconsultation devient active
5. **Patient** clique sur "Rejoindre" pour entrer dans l'appel
6. Consultation en ligne
7. **Spécialiste** crée des prescriptions
8. Consultation terminée

## 🛠️ Structure du Projet

```
Project-PI-java-3a/
├── Pi/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── org/example/
│   │   │   │       ├── controller/     # Contrôleurs JavaFX
│   │   │   │       ├── dao/            # Accès aux données
│   │   │   │       ├── model/          # Modèles de données
│   │   │   │       ├── service/        # Services métier
│   │   │   │       └── ui/             # Interfaces utilisateur
│   │   │   └── resources/
│   │   │       ├── view/               # Fichiers FXML
│   │   │       └── style.css           # Styles CSS
│   └── pom.xml
└── README.md
```

## 📝 Contrôleurs Principaux

- **RendezVousController**: Gestion des rendez-vous
- **RendezVousDialogController**: Dialogue de création/modification
- **EditRDVController**: Interface simplifiée pour les patients
- **PrescriptionController**: Gestion des prescriptions
- **ConsultationController**: Gestion des consultations
- **DashboardController**: Interface principale

## 🔐 Sécurité

- Authentification par email et mot de passe
- Rôles basés sur les permissions
- Filtrage des données par rôle
- Rate limiting pour les connexions

## 🐛 Dépannage

### Problème: Le spécialiste ne voit pas les rendez-vous
- **Solution**: Cliquez sur "Actualiser" pour assigner automatiquement des rendez-vous

### Problème: Le patient ne peut pas modifier un rendez-vous
- **Solution**: Le rendez-vous est probablement déjà assigné à un spécialiste. Les patients ne peuvent modifier que les rendez-vous non assignés.

### Problème: Le bouton "Rejoindre" n'apparaît pas
- **Solution**: Assurez-vous que:
  - Le rendez-vous est confirmé par le spécialiste
  - La téléconsultation est active (sessionActive = true)
  - Le mode du rendez-vous est "téléconsultation"

### Problème: NullPointerException lors de la modification
- **Solution**: Sélectionnez d'abord un rendez-vous dans le tableau avant de cliquer sur "Modifier"

## 📞 Support

Pour toute question ou problème, contactez l'équipe de développement.

## 📄 Licence

Ce projet est développé à des fins éducatives.

---

**Version**: 1.0  
**Dernière mise à jour**: Mai 2026