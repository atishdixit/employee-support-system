@echo off
setlocal
cd /d "%~dp0"

echo === Employee Support System - Starting ===

if not exist .env (
    copy .env.example .env >nul
)

for /f "usebackq tokens=1,2 delims==" %%A in (".env") do (
    if not "%%A"=="" if not "%%A:~0,1%"=="#" set "%%A=%%B"
)

if "%SPRING_PROFILES_ACTIVE%"=="" set SPRING_PROFILES_ACTIVE=dev
if "%API_PORT%"=="" set API_PORT=8080
if "%UI_PORT%"=="" set UI_PORT=4200

tasklist /FI "IMAGENAME eq ollama.exe" 2>nul | findstr /i "ollama.exe" >nul
if errorlevel 1 (
    echo Starting Ollama service ...
    start "Ollama" /min ollama serve
    timeout /t 3 >nul
) else (
    echo Ollama already running.
)

echo Starting backend (Spring Boot, profile=%SPRING_PROFILES_ACTIVE%) on http://localhost:%API_PORT% ...
start "Employee Support - Backend" cmd /k "cd /d "%~dp0backend\employee-support-app" && java -jar target\employee-support-app.jar --spring.profiles.active=%SPRING_PROFILES_ACTIVE%"

echo Starting frontend (Angular) on http://localhost:%UI_PORT% ...
start "Employee Support - Frontend" cmd /k "cd /d "%~dp0frontend" && npx ng serve --port %UI_PORT%"

echo Waiting for the servers to come up ...
timeout /t 15 >nul

start "" "http://localhost:%UI_PORT%"

echo.
echo Two windows were opened (backend + frontend). Close them to stop the app.
