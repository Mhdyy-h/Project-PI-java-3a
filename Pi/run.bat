@echo off
title BioSync - Lancement
echo ================================
echo   BioSync - Application Desktop
echo ================================
echo.

:: Chercher le JAR fat
set JAR=target\ProjetJDBC-1.0-SNAPSHOT-fat.jar

if not exist "%JAR%" (
    echo [!] JAR introuvable. Build en cours...
    call mvn -B clean package -DskipTests
    if errorlevel 1 (
        echo [ERREUR] Le build a echoue.
        pause
        exit /b 1
    )
)

echo [OK] Lancement de BioSync...
echo.
java --add-opens javafx.graphics/com.sun.javafx.application=ALL-UNNAMED ^
     --add-opens javafx.base/com.sun.javafx.reflect=ALL-UNNAMED ^
     -jar "%JAR%"

if errorlevel 1 (
    echo.
    echo [ERREUR] L'application s'est arretee avec une erreur.
    pause
)
