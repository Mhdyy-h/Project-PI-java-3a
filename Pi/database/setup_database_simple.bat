@echo off
chcp 65001 >nul
echo.
echo ╔════════════════════════════════════════════════════════════╗
echo ║         Configuration BioSync - Base de Données           ║
echo ╚════════════════════════════════════════════════════════════╝
echo.

REM Vérifier si MySQL est accessible
where mysql >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ MySQL n'est pas trouvé dans le PATH système.
    echo.
    echo 💡 Solutions possibles :
    echo    1. Ajoutez MySQL au PATH
    echo    2. Ou utilisez le chemin complet ci-dessous :
    echo.
    set /p MYSQL_PATH="Entrez le chemin complet vers mysql.exe (ou laissez vide pour annuler): "
    if "!MYSQL_PATH!"=="" (
        echo.
        echo ⚠️  Installation annulée.
        pause
        exit /b 1
    )
) else (
    set MYSQL_PATH=mysql
)

echo.
echo 🔐 Configuration MySQL
echo.
set /p MYSQL_USER="Nom d'utilisateur MySQL [root]: "
if "%MYSQL_USER%"=="" set MYSQL_USER=root

set /p MYSQL_PASS="Mot de passe MySQL (laissez vide si aucun): "

echo.
echo 📦 Création de la base de données...
echo.

REM Exécuter le script SQL
if "%MYSQL_PASS%"=="" (
    "%MYSQL_PATH%" -u %MYSQL_USER% < create_database.sql
) else (
    "%MYSQL_PATH%" -u %MYSQL_USER% -p%MYSQL_PASS% < create_database.sql
)

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ╔════════════════════════════════════════════════════════════╗
    echo ║              ✅ INSTALLATION RÉUSSIE !                     ║
    echo ╚════════════════════════════════════════════════════════════╝
    echo.
    echo 📊 Base de données créée : biosync
    echo 👥 Utilisateurs de test :
    echo.
    echo    ┌─────────────┬───────────────────────┬──────────────┐
    echo    │    Rôle     │        Email          │ Mot de passe │
    echo    ├─────────────┼───────────────────────┼──────────────┤
    echo    │ 👑 Admin    │ admin@biosync.com     │ admin123     │
    echo    │ 🏋️ Coach    │ coach@biosync.com     │ coach123     │
    echo    │ 👤 User     │ user@biosync.com      │ user123      │
    echo    └─────────────┴───────────────────────┴──────────────┘
    echo.
    echo 🚀 Vous pouvez maintenant lancer l'application !
    echo.
) else (
    echo.
    echo ╔════════════════════════════════════════════════════════════╗
    echo ║                ❌ ERREUR D'INSTALLATION                    ║
    echo ╚════════════════════════════════════════════════════════════╝
    echo.
    echo 🔍 Vérifications à faire :
    echo    1. MySQL est installé et démarré
    echo    2. Le mot de passe est correct
    echo    3. Vous avez les droits nécessaires
    echo.
    echo 💡 Pour démarrer MySQL :
    echo    net start MySQL80
    echo.
)

echo.
pause
