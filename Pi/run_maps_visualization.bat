@echo off
echo Starting BioSync Maps Visualization Server...
echo.

cd /d "C:\Users\DHIA\Desktop\FINALJAVA\Project-PI-java-3a\Pi"

java -cp ".;lib\mysql-connector-j-8.4.0\mysql-connector-j-8.4.0.jar;target\classes" org.example.api.MapsVisualizationAPI

echo.
echo Maps Visualization Server stopped.
pause
