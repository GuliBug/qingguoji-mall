@echo off
cd /d "%~dp0"
echo Starting backend (8080) + admin web (5173) + H5 (10086) ...
start "qingguoji-backend" cmd /k "%~dp0start-backend.cmd"
timeout /t 6 /nobreak >nul
start "qingguoji-web" cmd /k "%~dp0start-web.cmd"
start "qingguoji-h5" cmd /k "%~dp0start-frontend.cmd"
echo.
echo   Admin web : http://localhost:5173        (admin / admin123)
echo   Backend   : http://localhost:8080/api
echo   H5 client : http://localhost:10086
echo.
echo Close the three new windows to stop the services.
timeout /t 5 /nobreak >nul
