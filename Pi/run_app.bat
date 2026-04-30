@echo off
echo Starting BioSync Application...
echo.

cd /d "C:\Users\DHIA\Desktop\FINALJAVA\Project-PI-java-3a\Pi"

java -cp ".;lib\mysql-connector-j-8.4.0\mysql-connector-j-8.4.0.jar;target\classes" org.example.MainApplication

echo.
echo Application closed.
pause
