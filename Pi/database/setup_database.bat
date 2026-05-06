@echo off
echo ============================================================
echo Configuration de la base de donnees BioSync
echo ============================================================
echo.

REM Demander le mot de passe MySQL root
set /p MYSQL_PASSWORD="Entrez le mot de passe MySQL root (laissez vide si pas de mot de passe): "

echo.
echo Creation de la base de donnees...
echo.

REM Exécuter le script SQL
if "%MYSQL_PASSWORD%"=="" (
    mysql -u root < create_database.sql
) else (
    mysql -u root -p%MYSQL_PASSWORD% < create_database.sql
)

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================================
    echo Base de donnees creee avec succes !
    echo ============================================================
    echo.
    echo Utilisateurs de test crees :
    echo   - Admin:  admin@biosync.com / admin123
    echo   - Coach:  coach@biosync.com / coach123
    echo   - User:   user@biosync.com  / user123
    echo.
    echo Vous pouvez maintenant lancer l'application.
    echo ============================================================
) else (
    echo.
    echo ============================================================
    echo ERREUR: La creation de la base de donnees a echoue !
    echo ============================================================
    echo.
    echo Verifiez que :
    echo   1. MySQL est installe et demarre
    echo   2. Le mot de passe root est correct
    echo   3. Vous avez les droits necessaires
    echo ============================================================
)

echo.
pause
