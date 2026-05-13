@echo off
echo ========================================
echo    Fix Database Password Issue
echo ========================================
echo.

echo Current config.properties:
type "c:\Users\DHIA\Desktop\INTEGRATION\Project-PI-java-3a\Pi\src\main\resources\config.properties"
echo.

echo.
echo Adding password to config.properties...
echo db.password=root >> "c:\Users\DHIA\Desktop\INTEGRATION\Project-PI-java-3a\Pi\src\main\resources\config.properties"

echo.
echo ✅ Password added to config.properties!
echo.
echo Now restart your application and try login!
echo.
echo ========================================
pause
