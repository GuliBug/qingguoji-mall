@echo off
setlocal
cd /d "%~dp0frontend-h5\mall-client"

if not exist "node_modules" (
  echo [INFO] node_modules not found, installing dependencies ...
  call npm install --registry=https://registry.npmmirror.com --no-audit --no-fund
  if errorlevel 1 (
    echo [ERROR] npm install failed.
    pause
    exit /b 1
  )
)

echo [INFO] Starting H5 dev server ... http://localhost:10086
echo [INFO] Keep this window open while developing. Ctrl+C to stop.
echo.

call npm run dev:h5

if errorlevel 1 (
  echo.
  echo [ERROR] Dev server exited with an error. See log above.
  pause
)

endlocal
