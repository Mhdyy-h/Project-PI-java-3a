@echo off
echo.
echo ╔════════════════════════════════════════════════════════════╗
echo ║         Vérification de la Base de Données BioSync        ║
echo ╚════════════════════════════════════════════════════════════╝
echo.

set /p MYSQL_USER="Nom d'utilisateur MySQL [root]: "
if "%MYSQL_USER%"=="" set MYSQL_USER=root

set /p MYSQL_PASS="Mot de passe MySQL (laissez vide si aucun): "

echo.
echo 🔍 Vérification en cours...
echo.

if "%MYSQL_PASS%"=="" (
    mysql -u %MYSQL_USER% -e "USE biosync; SELECT 'Base de données OK' AS Status; SELECT COUNT(*) AS 'Utilisateurs' FROM utilisateurs; SELECT COUNT(*) AS 'Quiz' FROM quiz; SELECT COUNT(*) AS 'Questions' FROM questions;"
) else (
    mysql -u %MYSQL_USER% -p%MYSQL_PASS% -e "USE biosync; SELECT 'Base de données OK' AS Status; SELECT COUNT(*) AS 'Utilisateurs' FROM utilisateurs; SELECT COUNT(*) AS 'Quiz' FROM quiz; SELECT COUNT(*) AS 'Questions' FROM questions;"
)

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ La base de données biosync existe et contient des données !
    echo.
) else (
    echo.
    echo ❌ La base de données biosync n'existe pas ou est vide.
    echo    Exécutez d'abord : database\setup_database_simple.bat
    echo.
)

pause
