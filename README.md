<div align="center">

# 🏋️ BioSync

### *Your Intelligent Health & Wellness Management Platform*

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/17/)
[![JavaFX](https://img.shields.io/badge/JavaFX-17.0.6-2196F3?style=for-the-badge&logo=java&logoColor=white)](https://openjfx.io/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-00758F?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Maven](https://img.shields.io/badge/Maven-3.x-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](LICENSE)

**BioSync** is a comprehensive JavaFX desktop application for holistic health management — combining sport tracking, nutrition analysis, mental wellness, cognitive training, and AI-powered coaching into a single, unified platform.

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Screenshots & Modules](#-screenshots--modules)
- [Tech Stack](#-tech-stack)
- [Project Architecture](#-project-architecture)
- [Prerequisites](#-prerequisites)
- [Getting Started](#-getting-started)
- [Database Setup](#-database-setup)
- [Configuration](#-configuration)
- [Default Accounts](#-default-accounts)
- [AI Integrations](#-ai-integrations)
- [Project Structure](#-project-structure)
- [Contributing](#-contributing)
- [Team](#-team)

---

## 🌟 Overview

BioSync is a full-featured **health & fitness management desktop application** built as an academic project (Projet Intégré — PI) using Java 17 and JavaFX. It targets three user roles — **Administrators**, **Coaches**, and **Athletes** — each with a dedicated dashboard and feature set.

The platform goes beyond traditional fitness apps by incorporating:
- 🤖 **Generative AI** (Google Gemini, Ollama/Llama3) for nutrition analysis and mental health coaching
- 🧠 **Cognitive Training** with adaptive quizzes and multiplayer game sessions
- 📊 **Predictive Analytics** for workout progression and mental wellness trends
- 🎯 **Personalized Recommendations** driven by ML algorithms
- 🔐 **Advanced Security** including Face ID authentication and rate limiting

---

## ✨ Features

### 🔐 Authentication & Security
| Feature | Description |
|---------|-------------|
| **Face ID Login** | Biometric authentication via webcam using OpenCV (Haar Cascade, LBP) |
| **Password Recovery** | Secure UUID token-based reset via Mailjet email API (24h expiry) |
| **Password Strength Meter** | Real-time Shannon entropy scoring with improvement suggestions |
| **Rate Limiting** | Brute-force protection (max 3 attempts/IP/hour) |
| **reCAPTCHA** | Bot protection on sensitive forms |
| **Dark Mode** | Full theme switching with persistent user preferences |
| **Activity Logs** | Detailed audit trail (login, CRUD, exports) with statistics dashboard |

### 🏋️ Sport & Exercise Management
| Feature | Description |
|---------|-------------|
| **Workout Sessions** | Create, modify, assign and schedule sport sessions |
| **Exercise Library** | CRUD for exercises powered by ExerciseDB API |
| **Progression Tracking** | Visual charts (line, bar, donut) via JavaFX Canvas |
| **Overload Alerts** | Automated fatigue detection and training load warnings |
| **Athlete Profile** | Physical form index (BMI, fitness score, performance level) |
| **Coach Dashboard** | Overview of assigned athletes, session history, performance metrics |

### 🥗 Nutrition Module
| Feature | Description |
|---------|-------------|
| **Food Database** | Full CRUD for nutritional items with macros tracking |
| **AI Nutritional Analysis** | Google Gemini 1.5 Flash identifies food and returns calories/macros/GI |
| **Meal Planning** | Coach-side meal assignment and dietary program builder |
| **Calories Ninja API** | External caloric data lookup |
| **Nutritional Dashboard** | Visual breakdown of daily intake with recommendations |
| **PDF Export** | Generate printable nutritional reports |

### 🧠 Mental Health & Wellness
| Feature | Description |
|---------|-------------|
| **Mental Health Dashboard** | Real-time wellbeing score (0–100), stress, anxiety, depression indicators |
| **AI Profile Analysis** | Automated trend detection (improving / stable / declining) using linear regression |
| **Personalized Recommendations** | 6 types: breathing, meditation, exercise, therapy, lifestyle, urgent |
| **Risk Alerts** | Critical-level detection with emergency contact display (3114) |
| **Predictive Stress Model** | 7-day stress forecast based on historical quiz sessions |
| **Anomaly Detection** | Z-score based detection of sudden mental state changes |
| **Ollama Local AI** | On-device LLM (Llama3/Mistral) for mindfulness exercises, insights, action plans |

### 🎯 Cognitive Training (Quiz System)
| Feature | Description |
|---------|-------------|
| **Adaptive Quizzes** | Questions adjust difficulty based on performance and stress level |
| **AI Question Generation** | Ollama + Gemini auto-generate quiz questions on any topic |
| **Multiplayer Sessions** | Real-time competitive quiz via WebSocket |
| **Leaderboard** | Global ranking with badge system and chrono scoring |
| **Session Analytics** | Detailed post-quiz breakdown with cognitive agility insights |
| **Quiz Management** | Admin panel for quiz/question CRUD with media support |

### 👥 Community & Social
| Feature | Description |
|---------|-------------|
| **Groups** | Create and manage wellness groups |
| **Events** | Schedule and track community events |
| **Messaging** | In-app messaging between users |
| **Certification System** | Athletes can request certifications; admins approve/reject |
| **User Profiles** | Customizable avatars (initials, DiceBear robots, identicons, photo upload) |

### 📊 Reporting & Export
| Feature | Description |
|---------|-------------|
| **PDF Reports** | Full health/progression reports via iTextPDF and OpenPDF |
| **CSV Export** | Activity logs and session data export |
| **Visual Analytics** | Canvas-drawn charts: donut, timeline, histogram, bar |
| **Log Statistics** | 30-day activity, hourly heatmap, top actions dashboard |

---

## 🛠 Tech Stack

### Core
| Technology | Version | Role |
|-----------|---------|------|
| Java | 17 (LTS) | Primary language |
| JavaFX | 17.0.6 | Desktop UI framework |
| FXML | — | Declarative UI layout |
| MySQL | 8.0 | Relational database |
| JDBC | MySQL Connector 8.0.33 | Database connectivity |

### AI & External APIs
| Service | Usage |
|---------|-------|
| **Google Gemini 1.5 Flash** | Nutritional analysis, food identification |
| **Ollama (Llama3/Mistral)** | Local LLM for mental health coaching and quiz generation |
| **Mailjet API** | Transactional emails (password reset, notifications) |
| **CaloriesNinja API** | External caloric data |
| **ExerciseDB API** | Exercise library population |
| **DiceBear API** | Avatar generation |

### Libraries & Dependencies
| Library | Version | Purpose |
|---------|---------|---------|
| OkHttp | 4.12.0 | HTTP client for API calls |
| Gson | 2.10.1 | JSON serialization |
| Jackson | 2.17.0 | Advanced JSON processing |
| OpenCV (openpnp) | 4.9.0-0 | Face recognition |
| webcam-capture | 0.3.12 | Webcam access |
| iTextPDF | 5.5.13.3 | PDF generation |
| OpenPDF | 2.0.2 | PDF generation (alternative) |
| Apache PDFBox | 3.0.1 | PDF reading |
| Jakarta Mail | 2.0.1 | Email sending |
| Java-WebSocket | 1.5.4 | Multiplayer real-time sessions |
| Commons Math3 | 3.6.1 | Statistical computations |
| SLF4J + Logback | 2.0.9 / 1.4.14 | Structured logging |
| Sentry | 7.14.0 | Error monitoring (optional) |
| JUnit Jupiter | 5.10.0 | Unit testing |

---

## 🏗 Project Architecture

BioSync follows a **3-tier layered architecture** (MVC pattern):

```
┌─────────────────────────────────────────────────────────────────┐
│                          PRESENTATION LAYER                      │
│   JavaFX Controllers + FXML Views + CSS Stylesheets             │
│                                                                   │
│  LoginController │ AdminController │ MentalHealthDashboard       │
│  QuizViewController │ NutritionController │ CoachDashboard       │
│  AnalyseIAController │ LogsController │ VueUtilisateurController │
└───────────────────────────────┬─────────────────────────────────┘
                                │
┌───────────────────────────────▼─────────────────────────────────┐
│                           SERVICE LAYER                          │
│                                                                   │
│  AuthService │ GeminiService │ MentalHealthAIService            │
│  NavigationService │ ThemeService │ ExportPdfService            │
│  MultiplayerService │ OllamaChatService │ RecommandationService  │
│  FaceRecognitionService │ PasswordResetService │ AlertService    │
└───────────────────────────────┬─────────────────────────────────┘
                                │
┌───────────────────────────────▼─────────────────────────────────┐
│                            DAO LAYER                             │
│                                                                   │
│  UserDAO │ QuizDAO │ QuestionDAO │ RepasDAO │ AlimentDAO        │
│  ActivityLogDAO │ AlerteDAO │ CertificationDAO │ EvenementDAO   │
│  GroupeDAO │ RateLimitingDAO │ SeanceExerciceDAO               │
└───────────────────────────────┬─────────────────────────────────┘
                                │
┌───────────────────────────────▼─────────────────────────────────┐
│                         DATABASE LAYER                           │
│                       MySQL 8.0 — "biosync"                     │
└─────────────────────────────────────────────────────────────────┘
```

### Key Design Patterns
- **MVC** — Clean separation via FXML (View), Controllers, Services, DAOs
- **Singleton** — Shared services (NavigationService, ThemeService, AI services)
- **DAO Pattern** — Abstracted database access with connection pooling
- **Observer** — JavaFX property binding for reactive UI updates
- **Strategy** — Pluggable AI backends (Gemini vs Ollama)

---

## 📦 Prerequisites

Before running BioSync, ensure you have:

| Requirement | Version | Download |
|-------------|---------|----------|
| Java JDK | 17+ | [Adoptium](https://adoptium.net/) |
| Maven | 3.8+ | [maven.apache.org](https://maven.apache.org/download.cgi) |
| MySQL Server | 8.0+ | [mysql.com](https://dev.mysql.com/downloads/mysql/) |
| IntelliJ IDEA | 2023+ (recommended) | [jetbrains.com](https://www.jetbrains.com/idea/) |
| Ollama *(optional)* | Latest | [ollama.ai](https://ollama.ai/) — for local AI features |

> **Note:** JavaFX 17 is bundled via Maven. No separate JavaFX installation is required.

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/your-org/Project-PI-java-3a.git
cd Project-PI-java-3a
```

### 2. Set Up the Database

```bash
# Start MySQL server, then run the setup script:
cd Pi/database

# Windows (Batch):
setup_database.bat

# Windows (PowerShell):
.\setup_database.ps1

# Or manually via MySQL CLI:
mysql -u root -p < create_database.sql
```

> For a full schema with demo data, use `create_database_complete.sql` and `seed_complete_data.sql`.

### 3. Configure the Application

Edit `Pi/src/main/resources/config.properties`:

```properties
# Database
db.url=jdbc:mysql://localhost:3306/biosync
db.username=root
db.password=YOUR_PASSWORD_HERE

# Mailjet (for password reset emails)
mailjet.api.key=YOUR_MAILJET_API_KEY
mailjet.api.secret=YOUR_MAILJET_SECRET
mailjet.from.email=noreply@yourdomain.com
mailjet.from.name=BioSync
```

### 4. Build & Run

**Via IntelliJ IDEA:**
```
Run → MainFX (or click the ▶️ button)
```

**Via Maven:**
```bash
cd Pi
mvn javafx:run
```

**Via batch script:**
```bash
Pi/run.bat
```

---

## 🗄 Database Setup

### Database Name: `biosync`

The full schema includes the following tables:

| Table | Description |
|-------|-------------|
| `utilisateurs` | User accounts with roles, avatars, Face ID data |
| `quiz` | Mental health / cognitive quiz definitions |
| `questions` | Quiz questions with answers and explanations |
| `quiz_sessions` | Completed quiz sessions with scores and analytics |
| `seances_sport` | Workout sessions |
| `exercices` | Exercise library |
| `repas` | Meal plans |
| `aliments` | Nutritional food database |
| `activity_logs` | Full audit trail of user actions |
| `certifications` | Certification requests and approvals |
| `groupes` / `membres` | Community groups membership |
| `evenements` | Community events |
| `password_resets` | Secure password reset tokens |
| `rate_limiting` | Brute-force protection records |
| `alertes` | System alerts for overload/fatigue |

### SQL Scripts

| Script | Purpose |
|--------|---------|
| `create_database.sql` | Minimal schema + test users |
| `create_database_complete.sql` | Full schema with all tables |
| `seed_complete_data.sql` | Rich demo data (29K+ SQL) |
| `update_database.sql` | Migration patches |
| `schema_cognitive.sql` | Cognitive/mental health tables |

---

## ⚙ Configuration

### `config.properties` Reference

```properties
# ── Database ──────────────────────────────────
db.url=jdbc:mysql://localhost:3306/biosync
db.username=root
db.password=
db.driver=com.mysql.cj.jdbc.Driver

# ── Email (Mailjet) ───────────────────────────
mailjet.api.key=
mailjet.api.secret=
mailjet.from.email=
mailjet.from.name=BioSync

# ── AI — Ollama (local LLM) ───────────────────
ollama.baseUrl=http://localhost:11434
ollama.model=llama3
ollama.timeout.seconds=60

# ── Mental Health AI ──────────────────────────
mental.ai.enabled=true
mental.ai.min_sessions_for_analysis=3
mental.ai.prediction_days=7
mental.ai.anomaly_threshold=2.0

# ── Recommendations ───────────────────────────
mental.recommendations.max_count=5
mental.recommendations.expiry_days=7
```

---

## 🔑 Default Accounts

After running the setup script, these test accounts are available:

| Role | Email | Password |
|------|-------|----------|
| 👑 Administrator | `admin@biosync.com` | `admin123` |
| 🏋️ Coach | `coach@biosync.com` | `coach123` |
| 👤 Athlete | `user@biosync.com` | `user123` |

> ⚠️ **Important:** Change these credentials before deploying to any shared environment.

---

## 🤖 AI Integrations

### Google Gemini 1.5 Flash — Nutritional Analysis

```java
// Intelligent food detection + full nutritional data retrieval
Aliment data = GeminiService.obtenirInfoNutritionnelle("salmon");
// Returns: calories, proteins, carbs, lipids, glycemic index, food type
```

The service includes a smart validation pipeline:
1. **Java-side pre-filter** — Blocks obvious non-food inputs (greetings, city names, etc.)
2. **Local food cache** — 100+ common foods answered instantly without API call
3. **Gemini API call** — For unknown items, Gemini returns structured JSON or `{"erreur":"non_alimentaire"}`
4. **Fallback base** — Local nutritional database used when API is unavailable

### Ollama (Local LLM) — Mental Health AI

Requires [Ollama](https://ollama.ai/) installed and a model pulled:
```bash
ollama pull llama3
# or
ollama pull mistral
```

Features powered by Ollama:
- Personalized mental health insights
- Post-quiz feedback
- Guided mindfulness exercises
- 7-day action plans
- Pattern analysis across sessions

### Mental Health AI Algorithms

| Algorithm | Implementation |
|-----------|---------------|
| Wellbeing Score | `100 - (stress×3 + anxiety×3 + depression×4) / 10` |
| Resilience Score | Based on improvement across first/last session |
| Trend Detection | Linear regression (slope threshold: ±0.5) |
| Stress Prediction | `currentStress + (trend × days)` |
| Anomaly Detection | Z-score (`|x - μ| / σ`, threshold: 2.0–3.0) |
| Emotional State | Rule-based classifier (calm/anxious/stressed/depressed/happy) |

---

## 📁 Project Structure

```
Project-PI-java-3a/
├── Pi/
│   ├── pom.xml                      # Maven build configuration
│   ├── database/                    # SQL scripts and setup tools
│   │   ├── create_database.sql
│   │   ├── create_database_complete.sql
│   │   ├── seed_complete_data.sql
│   │   └── setup_database.bat/.ps1
│   └── src/
│       ├── main/
│       │   ├── java/org/example/
│       │   │   ├── MainFX.java              # Application entry point
│       │   │   ├── DatabaseConnection.java  # JDBC connection pool
│       │   │   ├── controller/              # 70+ JavaFX controllers
│       │   │   │   ├── LoginController.java
│       │   │   │   ├── AdminController.java
│       │   │   │   ├── MentalHealthDashboardController.java
│       │   │   │   ├── AnalyseIAController.java
│       │   │   │   ├── QuizViewController.java
│       │   │   │   ├── NutritionController.java
│       │   │   │   └── ...
│       │   │   ├── service/                 # 60+ business services
│       │   │   │   ├── GeminiService.java       # Gemini AI integration
│       │   │   │   ├── MentalHealthAIService.java
│       │   │   │   ├── MentalHealthOllamaService.java
│       │   │   │   ├── NavigationService.java   # Scene routing
│       │   │   │   ├── ThemeService.java        # Dark/light mode
│       │   │   │   ├── MultiplayerService.java  # WebSocket sessions
│       │   │   │   ├── ExportPdfService.java
│       │   │   │   ├── FaceRecognitionService.java
│       │   │   │   └── ...
│       │   │   ├── dao/                     # 14 Data Access Objects
│       │   │   │   ├── UserDAO.java
│       │   │   │   ├── QuizDAO.java
│       │   │   │   ├── ActivityLogDAO.java
│       │   │   │   └── ...
│       │   │   ├── model/                   # 33 entity classes
│       │   │   │   ├── User.java
│       │   │   │   ├── MentalHealthProfile.java
│       │   │   │   ├── QuizSession.java
│       │   │   │   └── ...
│       │   │   ├── metier/
│       │   │   │   ├── api/ExerciseDBClient.java
│       │   │   │   ├── ia/ProgressionIAModel.java
│       │   │   │   └── service/SeanceSportService.java
│       │   │   └── util/
│       │   │       ├── ConfigLoader.java
│       │   │       ├── SessionContext.java
│       │   │       ├── UserSession.java
│       │   │       └── InputValidator.java
│       │   └── resources/
│       │       ├── view/                    # 57 FXML layout files
│       │       │   ├── login.fxml
│       │       │   ├── dashboard.fxml
│       │       │   ├── mental_health_dashboard.fxml
│       │       │   ├── quiz_player.fxml
│       │       │   └── ...
│       │       ├── styles/                  # CSS stylesheets
│       │       │   ├── style.css
│       │       │   └── dark-theme.css       # 18KB dark mode styles
│       │       ├── config.properties        # App configuration
│       │       ├── logback.xml              # Logging config
│       │       ├── haarcascade_frontalface_default.xml  # Face detection model
│       │       ├── data_seed.sql
│       │       └── data_seed_mental_health.sql
│       └── test/java/                       # JUnit 5 tests
└── pom.xml                                  # Root POM
```

---

## 🔐 Security Highlights

| Feature | Implementation |
|---------|---------------|
| **Face ID** | OpenCV Haar Cascade + LBP histogram comparison (threshold: 60% similarity) |
| **Password Hashing** | BCrypt with cost factor 12 |
| **Reset Tokens** | UUID v4, 24h expiry, single-use with DB invalidation |
| **Rate Limiting** | DB-backed IP/identifier throttling, locked until timestamp |
| **Input Validation** | Centralized `ValidationService` + `InputValidator` |
| **Dark/Light Mode** | `ThemeService` with recursive node traversal + DB persistence |
| **Audit Logging** | All CRUD, auth, and export events logged with IP, timestamp, role |

---

## 🧪 Running Tests

```bash
cd Pi
mvn test
```

Tests use **JUnit Jupiter 5.10.0**. Test sources are located in `Pi/src/test/java/`.

---

## 🤝 Contributing

1. **Fork** this repository
2. **Create** a feature branch: `git checkout -b feature/your-feature-name`
3. **Commit** your changes: `git commit -m 'feat: add new feature'`
4. **Push** to the branch: `git push origin feature/your-feature-name`
5. **Open** a Pull Request

Please follow the PR template in `Pi/PULL_REQUEST_TEMPLATE.md`.

### Commit Convention

| Prefix | Usage |
|--------|-------|
| `feat:` | New feature |
| `fix:` | Bug fix |
| `refactor:` | Code restructure |
| `docs:` | Documentation only |
| `test:` | Adding/fixing tests |
| `style:` | Formatting, CSS |

---

## 📚 Additional Documentation

| Document | Description |
|----------|-------------|
| [`DOCUMENTATION.md`](DOCUMENTATION.md) | Deep-dive technical docs (Face ID, Auth, Dark Mode, Avatars) |
| [`Pi/MENTAL_HEALTH_AI_FEATURES.md`](Pi/MENTAL_HEALTH_AI_FEATURES.md) | Mental health AI algorithms and usage |
| [`Pi/GUIDE_OLLAMA_IA.md`](Pi/GUIDE_OLLAMA_IA.md) | Ollama local AI setup guide |
| [`Pi/QUICK_START.md`](Pi/QUICK_START.md) | Quick troubleshooting guide |
| [`Pi/DESIGN_SYSTEM_REFERENCE.md`](Pi/DESIGN_SYSTEM_REFERENCE.md) | Color palette and UI design tokens |
| [`Pi/database/README.md`](Pi/database/README.md) | Database administration guide |

---

## 🏫 Academic Context

This project was developed as a **Projet Intégré (PI)** at **ESPRIT** (École Supérieure Privée d'Ingénierie et de Technologies), Tunisia — by a team of 3rd-year Computer Engineering students.

---

## 📄 License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.

---

<div align="center">

**Made with ❤️ by the BioSync Team — ESPRIT 3A41

*If this project helped you, please give it a ⭐!*

</div>
