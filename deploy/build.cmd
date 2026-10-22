@echo off
rem ============================================================
rem  Qingguoji Mall - production build (Windows)
rem  Output: dist\app.jar + dist\web\ + dist\h5\
rem ============================================================
setlocal enabledelayedexpansion

set "ROOT=%~dp0.."
cd /d "%ROOT%"

echo [1/5] Checking JDK 21 ...
rem Prefer the JDK 21 bundled for this project; fall back to an existing JAVA_HOME.
rem Override by setting JAVA_HOME to your own JDK 21 before running this script.
if not defined QGJ_FORCE_JAVA_HOME (
    if exist "%USERPROFILE%\jdks\jdk-21.0.2\bin\java.exe" (
        set "JAVA_HOME=%USERPROFILE%\jdks\jdk-21.0.2"
    )
)
if not defined JAVA_HOME (
    echo [ERROR] JAVA_HOME is not set. Install JDK 21 first.
    exit /b 1
)
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [ERROR] Invalid JAVA_HOME: %JAVA_HOME%
    exit /b 1
)
set "PATH=%JAVA_HOME%\bin;%PATH%"
for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do set "JVER=%%~v"
echo       JAVA_HOME=%JAVA_HOME%
echo       java version %JVER%
echo %JVER% | findstr /b "21." >nul
if errorlevel 1 (
    echo [ERROR] JDK 21 is required, current: %JVER%
    exit /b 1
)

echo [2/5] Building backend jar ...
call :resolve_mvn
if errorlevel 1 exit /b 1
pushd backend
call %MVN% -B -q clean package -DskipTests -Dspotless.check.skip=true
if errorlevel 1 (
    popd
    echo [ERROR] Backend build failed.
    exit /b 1
)
popd
if not exist "backend\target\mall-backend-0.0.1-SNAPSHOT.jar" (
    echo [ERROR] jar not found: backend\target\mall-backend-0.0.1-SNAPSHOT.jar
    exit /b 1
)

echo [3/5] Building admin web ...
pushd frontend-web
if not exist node_modules (
    call npm install --registry=https://registry.npmmirror.com --no-audit --no-fund
    if errorlevel 1 (
        popd
        echo [ERROR] npm install failed for frontend-web.
        exit /b 1
    )
)
call npm run build:prod
if errorlevel 1 (
    popd
    echo [ERROR] Admin web build failed.
    exit /b 1
)
popd

echo [4/5] Building H5 client ...
pushd frontend-h5\mall-client
if not exist node_modules (
    call npm install --registry=https://registry.npmmirror.com --no-audit --no-fund
    if errorlevel 1 (
        popd
        echo [ERROR] npm install failed for H5.
        exit /b 1
    )
)
call npm run build:h5
if errorlevel 1 (
    popd
    echo [ERROR] H5 build failed.
    exit /b 1
)
popd

echo [5/5] Collecting artifacts into dist\ ...
if exist dist rd /s /q dist
mkdir dist
mkdir dist\web
mkdir dist\h5
copy /y backend\target\mall-backend-0.0.1-SNAPSHOT.jar dist\app.jar >nul
xcopy /e /i /q /y frontend-web\dist dist\web >nul
xcopy /e /i /q /y frontend-h5\mall-client\dist dist\h5 >nul

echo.
echo ============================================================
echo  Build complete: %ROOT%\dist
echo    dist\app.jar   backend executable jar
echo    dist\web\      admin web static files
echo    dist\h5\       H5 client static files
echo.
echo  Upload:
echo    scp -r dist/* user@your-server:/opt/qingguoji/
echo ============================================================
endlocal
exit /b 0

rem ---------- locate maven ----------
:resolve_mvn
where mvn >nul 2>nul
if not errorlevel 1 (
    set "MVN=mvn"
    exit /b 0
)
if exist "backend\mvnw.cmd" (
    set "MVN=%ROOT%\backend\mvnw.cmd"
    exit /b 0
)
echo [ERROR] Neither mvn nor backend\mvnw.cmd is available.
exit /b 1
