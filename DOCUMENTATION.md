# Documentation Complète du Projet JavaFX

## Table des matières
1. [Face ID (Reconnaissance Faciale)](#1-face-id-reconnaissance-faciale)
2. [Mot de passe oublié](#2-mot-de-passe-oublié)
3. [Dark Mode](#3-dark-mode)
4. [Génération d'Avatars](#4-génération-davatars)
5. [Barre de force du mot de passe](#5-barre-de-force-du-mot-de-passe)
6. [Historique des Logs](#6-historique-des-logs)

---

## 1. Face ID (Reconnaissance Faciale)

### 1.1 Vue d'ensemble
Le système Face ID permet aux utilisateurs de s'authentifier via leur webcam en utilisant la reconnaissance faciale biométrique.

### 1.2 Architecture

**Fichiers principaux :**
- `FaceIDController.java` - Interface utilisateur et capture vidéo
- `FaceRecognitionService.java` - Logique de reconnaissance faciale
- `UserDAO.java` - Stockage/récupération des empreintes faciales

### 1.3 APIs et Bibliothèques utilisées

| Bibliothèque | Version | Usage |
|-------------|---------|-------|
| **OpenCV** | 4.9.0 | Traitement d'image, détection de visages |
| **JavaCV** | 1.5.9 | Bridge Java pour OpenCV natives |
| **JavaFX Media** | 21 | Capture vidéo webcam |

**Dépendances Maven (pom.xml) :**
```xml
<dependency>
    <groupId>org.bytedeco</groupId>
    <artifactId>javacv-platform</artifactId>
    <version>1.5.9</version>
</dependency>
<dependency>
    <groupId>org.openpnp</groupId>
    <artifactId>opencv</artifactId>
    <version>4.9.0-0</version>
</dependency>
```

### 1.4 Fonctionnement technique

#### Flux d'enregistrement d'un visage :
```
1. Capture webcam → Image BufferedImage
2. Conversion OpenCV : bufferedImageToMat()
3. Détection du visage avec CascadeClassifier (haarcascade_frontalface_default.xml)
4. Extraction du ROI (Region of Interest)
5. Redimensionnement en 100x100 pixels
6. Conversion en bytes[] pour stockage BLOB en base de données
7. Stockage dans la colonne 'face_template' de la table 'utilisateurs'
```

#### Flux d'authentification :
```
1. Capture live de l'utilisateur
2. Détection et extraction du visage
3. Comparaison avec le template stocké :
   - Calcul de la distance euclidienne entre les descripteurs
   - Seuil de similarité : 0.6 (60%)
   - Algorithme de matching par histogramme de couleurs + features LBP
4. Si match > 60% → Authentification réussie
```

### 1.5 Points clés du code

**Détection de visage :**
```java
// Chargement du classificateur Haar Cascade
CascadeClassifier faceDetector = new CascadeClassifier();
faceDetector.load("haarcascade_frontalface_default.xml");

// Détection
MatOfRect faces = new MatOfRect();
faceDetector.detectMultiScale(grayImage, faces, 1.1, 3, 0, new Size(100, 100), new Size());
```

**Comparaison de visages :**
```java
public double compareFaces(Mat face1, Mat face2) {
    // Conversion en grayscale
    Mat gray1 = new Mat(), gray2 = new Mat();
    Imgproc.cvtColor(face1, gray1, Imgproc.COLOR_BGR2GRAY);
    Imgproc.cvtColor(face2, gray2, Imgproc.COLOR_BGR2GRAY);
    
    // Redimensionnement uniforme
    Mat resized1 = new Mat(), resized2 = new Mat();
    Imgproc.resize(gray1, resized1, new Size(100, 100));
    Imgproc.resize(gray2, resized2, new Size(100, 100));
    
    // Calcul de similarité par corrélation
    double similarity = Imgproc.compareHist(hist1, hist2, Imgproc.HISTCMP_CORREL);
    return similarity; // 0.0 à 1.0
}
```

### 1.6 Schéma de base de données
```sql
ALTER TABLE utilisateurs 
ADD COLUMN face_template BLOB,          -- Image du visage encodée
ADD COLUMN face_enabled BOOLEAN DEFAULT FALSE,  -- Activation Face ID
ADD COLUMN face_created_at TIMESTAMP;    -- Date d'enregistrement
```

---

## 2. Mot de passe oublié

### 2.1 Vue d'ensemble
Système de récupération de mot de passe par email avec token sécurisé à usage unique.

### 2.2 Architecture

**Fichiers principaux :**
- `ForgotPasswordController.java` - Interface de récupération
- `PasswordResetService.java` - Génération et validation des tokens
- `MailjetEmailService.java` - Envoi d'emails via API Mailjet
- `ResetPasswordController.java` - Formulaire de réinitialisation

### 2.3 APIs utilisées

| Service | Type | Usage |
|---------|------|-------|
| **Mailjet API** | REST API | Envoi d'emails de récupération |
| **BCrypt** | Bibliothèque Java | Hachage des mots de passe |

**Configuration Mailjet :**
```java
// Credentials API Mailjet (à sécuriser en production)
private static final String API_KEY = "votre_api_key";
private static final String API_SECRET = "votre_api_secret";
private static final String FROM_EMAIL = "noreply@votredomaine.com";
```

### 2.4 Flux de fonctionnement

```
Étape 1 : Demande de récupération
├── Formulaire : email utilisateur
├── Vérification existence email en BDD
├── Génération token UUID (36 caractères)
├── Stockage : token + expiration (24h) + user_id
└── Envoi email avec lien : /reset-password?token=xxx

Étape 2 : Clic sur le lien email
├── Extraction du token de l'URL
├── Vérification validité et non-expiration
├── Affichage formulaire nouveau mot de passe
└── Validation force du mot de passe

Étape 3 : Réinitialisation
├── Vérification token (dernière vérification)
├── Hachage BCrypt du nouveau mot de passe
├── Mise à jour BDD (table utilisateurs)
├── Invalidation du token (suppression ou marquage used)
└── Redirection vers login avec message succès
```

### 2.5 Schéma de base de données
```sql
CREATE TABLE password_resets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    used BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES utilisateurs(id)
);

-- Index pour recherche rapide
CREATE INDEX idx_token ON password_resets(token);
```

### 2.6 Sécurité implémentée

| Mesure | Implémentation |
|--------|---------------|
| **Token unique** | UUID.randomUUID().toString() |
| **Expiration** | 24 heures après création |
| **Usage unique** | Flag 'used' + suppression après usage |
| **Hachage** | BCrypt avec coût 12 |
| **Rate limiting** | Max 3 tentatives par IP/heure |

---

## 3. Dark Mode

### 3.1 Vue d'ensemble
Système de thème sombre complet avec bascule dynamique et persistance des préférences.

### 3.2 Architecture

**Fichiers principaux :**
- `ThemeService.java` - Gestion centralisée du thème
- `dark-theme.css` - Feuille de styles sombre
- `AdminController.java` - Bouton de bascule thème
- `UtilisateurController.java` - Application du thème aux composants

### 3.3 Approche technique

**Stratégie hybride :**
```
CSS Global (dark-theme.css)
    ↓
Appliqué sur :root de la scène
    ↓
ThemeService remplace les styles inline (light) par équivalents dark
    ↓
Styles persistés dans le profil utilisateur
```

### 3.4 Mapping des couleurs

| Élément | Light Mode | Dark Mode |
|---------|-----------|-----------|
| **Fond principal** | `#f0f2f8` | `#0d1117` |
| **Cartes/Panels** | `white`, `#f8fafc` | `#161b22` |
| **Surélévation** | `#f1f5f9` | `#21262d` |
| **Bordures** | `#e5e7eb` | `#30363d` |
| **Texte principal** | `#1a1a2e`, `#111827` | `#f0f6fc` |
| **Texte secondaire** | `#6b7280` | `#8b949e` |
| **Accent** | `#4C6FFF` | `#818cf8` |
| **Succès** | `#166534` | `#86efac` |
| **Avertissement** | `#92400e` | `#fde68a` |
| **Erreur** | `#991b1b` | `#fca5a5` |

### 3.5 Fonctionnement de ThemeService

```java
public class ThemeService {
    // Mapping des couleurs light → dark
    private static final String[][] LIGHT_TO_DARK = {
        {"-fx-background-color: white", "-fx-background-color: #161b22"},
        {"-fx-text-fill: #374151", "-fx-text-fill: #c9d1d9"},
        // ... etc
    };
    
    public void applyToScene(Scene scene) {
        scene.getRoot().getStyleClass().add("dark-mode");
        // Parcours récursif de tous les nœuds
        applyDarkModeToNode(root, true);
    }
    
    private void applyDarkModeToNode(Node node, boolean recursive) {
        if (node instanceof Region) {
            String style = node.getStyle();
            // Remplacement des couleurs inline
            for (String[] mapping : LIGHT_TO_DARK) {
                style = style.replace(mapping[0], mapping[1]);
            }
            node.setStyle(style);
        }
    }
}
```

### 3.6 Persistance
```java
// Sauvegarde dans la BDD
userDAO.updateThemePreference(userId, isDarkMode);

// Au login, récupération et application automatique
boolean darkMode = user.getDarkModePreference();
if (darkMode) themeService.enableDarkMode();
```

---

## 4. Génération d'Avatars

### 4.1 Vue d'ensemble
Création d'avatars personnalisés avec initials, couleurs générées à partir du hash du nom.

### 4.2 Architecture

**Fichiers principaux :**
- `AvatarGeneratorService.java` - Génération d'avatars par code
- `AvatarService.java` - Gestion des avatars utilisateur
- `AvatarCustomizationController.java` - Interface de personnalisation

### 4.3 Algorithmes utilisés

**Génération des initials :**
```java
public String getInitials(String fullName) {
    if (fullName == null || fullName.isEmpty()) return "??";
    
    String[] parts = fullName.trim().split("\\s+");
    StringBuilder initials = new StringBuilder();
    
    // Première lettre de chaque mot (max 2)
    for (int i = 0; i < Math.min(parts.length, 2); i++) {
        if (!parts[i].isEmpty()) {
            initials.append(parts[i].charAt(0));
        }
    }
    
    return initials.toString().toUpperCase();
}
// Exemple : "Jean Dupont" → "JD"
```

**Génération de couleur déterministe :**
```java
public Color generateColorFromString(String input) {
    int hash = input.hashCode();
    
    // Palette prédéfinie de couleurs harmonieuses
    Color[] palette = {
        Color.web("#6366f1"), // Indigo
        Color.web("#8b5cf6"), // Violet
        Color.web("#06b6d4"), // Cyan
        Color.web("#22c55e"), // Green
        Color.web("#f59e0b"), // Amber
        Color.web("#ef4444"), // Red
        Color.web("#ec4899"), // Pink
        Color.web("#14b8a6"), // Teal
    };
    
    // Sélection basée sur le hash
    int index = Math.abs(hash) % palette.length;
    return palette[index];
}
```

### 4.4 Types d'avatars

| Type | Description | Génération |
|------|-------------|------------|
| **Initials** | Cercle avec initiales | JavaFX Canvas → Image |
| **Robot** | Avatar robot (DiceBear API) | URL externe |
| **Identicon** | Motif géométrique | Hash MD5 du nom |
| **Photo** | Photo de profil | Upload utilisateur |

### 4.5 Création d'avatar initials (Canvas JavaFX)
```java
public Image generateInitialsAvatar(String initials, Color bgColor, int size) {
    Canvas canvas = new Canvas(size, size);
    GraphicsContext gc = canvas.getGraphicsContext2D();
    
    // Cercle de fond
    gc.setFill(bgColor);
    gc.fillOval(0, 0, size, size);
    
    // Initiales centrées
    gc.setFill(Color.WHITE);
    gc.setFont(Font.font("System", FontWeight.BOLD, size * 0.4));
    gc.setTextAlign(TextAlignment.CENTER);
    gc.setTextBaseline(VPos.CENTER);
    gc.fillText(initials, size/2, size/2);
    
    // Snapshot en Image
    return canvas.snapshot(null, null);
}
```

### 4.6 Schéma BDD
```sql
ALTER TABLE utilisateurs
ADD COLUMN avatar_type VARCHAR(20) DEFAULT 'initials',
ADD COLUMN avatar_data BLOB,           -- Pour photos uploadées
ADD COLUMN avatar_url VARCHAR(500),      -- Pour avatars externes
ADD COLUMN avatar_color VARCHAR(7);     -- Couleur générée (#6366f1)
```

---

## 5. Barre de force du mot de passe

### 5.1 Vue d'ensemble
Indicateur visuel en temps réel de la robustesse du mot de passe avec suggestions d'amélioration.

### 5.2 Architecture

**Fichiers principaux :**
- `PasswordStrengthService.java` - Calcul de la force
- `PasswordStrengthBar.java` - Composant UI (barre de progression)
- `PasswordSuggestionService.java` - Génération de suggestions

### 5.3 Algorithme de calcul de force

```java
public PasswordStrength calculateStrength(String password) {
    int score = 0;
    List<String> suggestions = new ArrayList<>();
    
    // 1. Longueur
    if (password.length() >= 12) score += 25;
    else if (password.length() >= 8) score += 15;
    else suggestions.add("Au moins 8 caractères");
    
    // 2. Complexité
    if (password.matches(".*[A-Z].*")) score += 15;  // Majuscule
    else suggestions.add("Ajoutez une majuscule");
    
    if (password.matches(".*[a-z].*")) score += 15;  // Minuscule
    
    if (password.matches(".*\\d.*")) score += 15;    // Chiffre
    else suggestions.add("Ajoutez un chiffre");
    
    if (password.matches(".*[!@#$%^&*].*")) score += 20; // Spécial
    else suggestions.add("Ajoutez un caractère spécial (!@#$%)");
    
    // 3. Entropie (bonus)
    double entropy = calculateEntropy(password);
    if (entropy > 50) score += 10;
    
    return new PasswordStrength(score, getLabel(score), suggestions);
}

private double calculateEntropy(String password) {
    // Entropie de Shannon
    Map<Character, Integer> freq = new HashMap<>();
    for (char c : password.toCharArray()) {
        freq.merge(c, 1, Integer::sum);
    }
    
    double entropy = 0;
    int len = password.length();
    for (int count : freq.values()) {
        double p = (double) count / len;
        entropy -= p * (Math.log(p) / Math.log(2));
    }
    return entropy * len;
}
```

### 5.4 Niveaux de force

| Score | Niveau | Couleur | Description |
|-------|--------|---------|-------------|
| 0-20 | Très faible | 🔴 Rouge | Risqué, facilement crackable |
| 21-40 | Faible | 🟠 Orange | Insuffisant pour production |
| 41-60 | Moyen | 🟡 Jaune | Acceptable mais améliorable |
| 61-80 | Fort | 🟢 Bleu-vert | Bon niveau de sécurité |
| 81-100 | Très fort | 🟢 Vert | Excellent, résistant aux attaques |

### 5.5 Générateur de suggestions de mots de passe

```java
public List<String> generateSuggestions() {
    List<String> suggestions = new ArrayList<>();
    
    // Génération de phrases de passe mémorisables
    String[] adjectives = {"Blue", "Happy", "Swift", "Bright", "Cool"};
    String[] nouns = {"Tiger", "River", "Mountain", "Eagle", "Dragon"};
    String[] years = {"2024", "X7", "Pro", "Max", "99"};
    
    for (int i = 0; i < 5; i++) {
        String adj = adjectives[random.nextInt(adjectives.length)];
        String noun = nouns[random.nextInt(nouns.length)];
        String year = years[random.nextInt(years.length)];
        String special = "!@#$%^&*".charAt(random.nextInt(8)) + "";
        
        // Exemple : "BlueTiger2024!" ou "SwiftDragon@X7"
        suggestions.add(adj + noun + year + special);
    }
    
    return suggestions;
}
```

### 5.6 Composant UI (JavaFX)
```java
public class PasswordStrengthBar extends HBox {
    private ProgressBar progressBar;
    private Label strengthLabel;
    private Label suggestionsLabel;
    
    public void updateStrength(String password) {
        PasswordStrength strength = service.calculateStrength(password);
        
        // Mise à jour barre (0.0 - 1.0)
        progressBar.setProgress(strength.getScore() / 100.0);
        
        // Couleur dynamique
        progressBar.setStyle(getColorStyle(strength.getScore()));
        
        // Label
        strengthLabel.setText(strength.getLabel());
        
        // Suggestions
        suggestionsLabel.setText(String.join(" • ", strength.getSuggestions()));
    }
}
```

---

## 6. Historique des Logs

### 6.1 Vue d'ensemble
Système de journalisation d'activités utilisateur avec tableaux dynamiques, filtres et statistiques.

### 6.2 Architecture

**Fichiers principaux :**
- `ActivityLog.java` - Modèle de données
- `ActivityLogDAO.java` - Accès base de données
- `ActivityLogService.java` - Logique métier
- `LogsController.java` - Interface liste des logs
- `LogsStatsController.java` - Graphiques et statistiques

### 6.3 Structure des données

**Modèle ActivityLog :**
```java
public class ActivityLog {
    private int id;
    private int userId;
    private String userName;
    private String action;        // Type d'action
    private String entityType;    // Entité concernée (User, Certification...)
    private int entityId;         // ID de l'entité
    private String details;       // Détails JSON
    private String ipAddress;     // IP de l'utilisateur
    private LocalDateTime createdAt;
    private String role;          // Rôle de l'utilisateur
}
```

### 6.4 Schéma BDD
```sql
CREATE TABLE activity_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    user_name VARCHAR(255),
    action VARCHAR(50) NOT NULL,        -- LOGIN, LOGOUT, CREATE, UPDATE, DELETE
    entity_type VARCHAR(50),              -- USER, CERTIFICATION, DOCUMENT
    entity_id INT,
    details TEXT,                         -- JSON avec détails
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    role VARCHAR(50),
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_created_at (created_at),
    INDEX idx_entity (entity_type, entity_id),
    FOREIGN KEY (user_id) REFERENCES utilisateurs(id) ON DELETE SET NULL
);
```

### 6.5 Types d'actions loguées

| Action | Description | Déclencheur |
|--------|-------------|-------------|
| `LOGIN` | Connexion utilisateur | AuthService.authenticate() |
| `LOGOUT` | Déconnexion | NavigationService.logout() |
| `CREATE_USER` | Création utilisateur | UserService.create() |
| `UPDATE_USER` | Modification utilisateur | UserService.update() |
| `DELETE_USER` | Suppression utilisateur | UserService.delete() |
| `CREATE_CERTIFICATION` | Nouvelle certification | CertificationService.create() |
| `UPDATE_CERTIFICATION` | Modif certification | CertificationService.update() |
| `FACE_ID_REGISTER` | Enregistrement Face ID | FaceIDController.saveFace() |
| `PASSWORD_RESET` | Réinitialisation MDP | PasswordResetService.reset() |
| `EXPORT_PDF` | Export rapport PDF | PdfReportService.export() |

### 6.6 Service de logging

```java
public class ActivityLogService {
    private final ActivityLogDAO logDAO;
    
    public void log(String action, String entityType, int entityId, String details) {
        ActivityLog log = new ActivityLog();
        log.setUserId(getCurrentUserId());
        log.setUserName(getCurrentUserName());
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        log.setIpAddress(getClientIP());
        log.setRole(getCurrentUserRole());
        log.setCreatedAt(LocalDateTime.now());
        
        logDAO.save(log);
    }
    
    // Méthodes de récupération avec filtres
    public List<ActivityLog> getLogs(int offset, int limit, String filter) {
        return logDAO.findAll(offset, limit, filter);
    }
    
    public Map<String, Integer> getStatsByAction() {
        return logDAO.countByAction();
    }
}
```

### 6.7 Interface LogsController

**Fonctionnalités UI :**
- Tableau paginé avec colonnes triables
- Filtres : date range, action type, utilisateur, recherche texte
- Export CSV/PDF des logs
- Pagination (50 logs par page)

**Composants JavaFX :**
```java
@FXML private TableView<ActivityLog> logsTable;
@FXML private TableColumn<ActivityLog, String> actionColumn;
@FXML private TableColumn<ActivityLog, String> userColumn;
@FXML private TableColumn<ActivityLog, LocalDateTime> dateColumn;
@FXML private TextField searchField;
@FXML private DatePicker fromDate, toDate;
@FXML private ComboBox<String> actionFilter;
```

### 6.8 Statistiques (LogsStatsController)

**Graphiques générés avec Canvas JavaFX :**

1. **Activité des 30 derniers jours** - Courbe temporelle
2. **Répartition par rôle** - Diagramme donut
3. **Activité horaire** - Histogramme 24h
4. **Top 5 actions** - Barres horizontales

```java
// Exemple : dessin d'un donut chart
private void drawDonutChart(Map<String, Integer> data) {
    GraphicsContext gc = donutCanvas.getGraphicsContext2D();
    double cx = width / 2, cy = height / 2;
    double outerR = Math.min(width, height) / 2 * 0.8;
    double innerR = outerR * 0.6;
    
    String[] colors = {"#6366f1", "#8b5cf6", "#06b6d4", "#22c55e", "#f59e0b"};
    
    double startAngle = -90;
    for (Map.Entry<String, Integer> entry : data.entrySet()) {
        double sweep = 360.0 * entry.getValue() / total;
        gc.setFill(Color.web(colors[i % colors.length]));
        gc.fillArc(cx - outerR, cy - outerR, outerR * 2, outerR * 2, 
                   startAngle, sweep, ArcType.ROUND);
        startAngle += sweep;
    }
    
    // Trou central
    gc.setFill(Color.web("#161b22"));
    gc.fillOval(cx - innerR, cy - innerR, innerR * 2, innerR * 2);
}
```

### 6.9 API de nettoyage automatique

```java
// Tâche planifiée : suppression des logs > 90 jours
@Scheduled(cron = "0 0 2 * * ?") // Tous les jours à 2h du matin
public void cleanupOldLogs() {
    LocalDateTime cutoff = LocalDateTime.now().minusDays(90);
    int deleted = logDAO.deleteBefore(cutoff);
    logger.info("Nettoyage logs : {} entrées supprimées", deleted);
}
```

---

## Résumé des APIs et Services Externes

| Service | Type | Usage | Coût |
|---------|------|-------|------|
| **Mailjet** | Email API | Notifications, reset password | Gratuit (200/jour) |
| **OpenCV** | Bibliothèque C++ | Reconnaissance faciale | Gratuit (Open Source) |
| **DiceBear** | API HTTP | Avatars robots | Gratuit |
| **reCAPTCHA** | API Google | Protection formulaires | Gratuit |

---

## Architecture globale

```
┌─────────────────────────────────────────────────────────────┐
│                         COUCHE UI                            │
│  JavaFX Controllers  (FXML + CSS + Controllers)             │
│  ├─ LoginController            ├─ AdminController            │
│  ├─ FaceIDController           ├─ LogsController           │
│  ├─ ForgotPasswordController   └─ UtilisateurController      │
│  └─ AvatarCustomizationController                           │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                      COUCHE SERVICE                          │
│  ├─ AuthService          ├─ FaceRecognitionService         │
│  ├─ UserService          ├─ AvatarGeneratorService          │
│  ├─ ThemeService         ├─ PasswordStrengthService         │
│  ├─ ActivityLogService   ├─ PasswordResetService           │
│  └─ MailjetEmailService  └─ CertificationService             │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                       COUCHE DAO                             │
│  ├─ UserDAO              ├─ ActivityLogDAO                 │
│  ├─ CertificationDAO     ├─ RateLimitingDAO                 │
│  └─ (JDBC MySQL)                                             │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                      BASE DE DONNÉES                        │
│                     MySQL 8.0                                │
└─────────────────────────────────────────────────────────────┘
```

---

*Documentation générée le 30 Avril 2026*
*Projet : Application JavaFX de Gestion de Certifications*
