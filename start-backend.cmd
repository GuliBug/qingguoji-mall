@echo off
setlocal
cd /d "%~dp0backend"

rem ---- JDK 21 (required by Spring Boot 3.5) ----
set "JAVA_HOME=C:\Users\27978\jdks\jdk-21.0.2"
if not exist "%JAVA_HOME%\bin\java.exe" (
  echo [ERROR] JDK 21 not found at %JAVA_HOME%
  echo         Edit JAVA_HOME at the top of this script.
  pause
  exit /b 1
)
set "PATH=%JAVA_HOME%\bin;%PATH%"

rem ---- Clear inherited SERVER__PORT (it would override server.port) ----
set "SERVER__PORT="

rem ---- Port check ----
netstat -ano | findstr /r /c:"LISTENING.*:8080 " >nul 2>&1
if %errorlevel%==0 (
  echo [WARN] Port 8080 is already in use. Stop that process first.
  pause
  exit /b 1
)

echo [INFO] Java: %JAVA_HOME%
"%JAVA_HOME%\bin\java" -version
echo.
echo [INFO] Backend API   : http://localhost:8080/api
echo [INFO] Admin console : http://localhost:8080/admin.html  (admin / admin123)
echo [INFO] H2 console    : http://localhost:8080/h2-console
echo.

rem ---- Prefer local Maven, fall back to project wrapper ----
where mvn >nul 2>&1
if %errorlevel%==0 (set "MVN=mvn") else (set "MVN=mvnw.cmd")
echo [INFO] Using: %MVN%
echo.

call %MVN% spring-boot:run "-Dspring-boot.run.arguments=--server.port=8080"
if errorlevel 1 (
  echo.
  echo [ERROR] Backend failed to start. See log above.
  pause
)

endlocal
