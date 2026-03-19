@echo off
REM ============================================
REM Gestore Comande v2.0 - Startup Script
REM Windows Batch Script
REM ============================================

setlocal enabledelayedexpansion

echo.
echo ============================================
echo  Gestore Comande v2.0 Startup
echo ============================================
echo.

REM Check if this is running from the correct directory
if not exist "backend" (
    echo ERROR: backend directory not found!
    echo Please run this script from the project root directory.
    pause
    exit /b 1
)

if not exist "frontend" (
    echo ERROR: frontend directory not found!
    echo Please run this script from the project root directory.
    pause
    exit /b 1
)

echo Please choose a startup option:
echo.
echo 1. Development Mode (Backend + Frontend in separate terminals)
echo 2. Backend Only
echo 3. Frontend Only
echo 4. Docker (Full Stack)
echo 5. Exit
echo.

set /p choice="Enter your choice (1-5): "

if "%choice%"=="1" goto dev_mode
if "%choice%"=="2" goto backend_only
if "%choice%"=="3" goto frontend_only
if "%choice%"=="4" goto docker_mode
if "%choice%"=="5" goto exit_script
echo Invalid choice. Please try again.
pause
goto start

:dev_mode
echo.
echo Starting Development Mode...
echo.
echo Checking dependencies...

REM Check Java
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java 17+ is not installed or not in PATH
    echo Please install Java and add it to your PATH
    pause
    exit /b 1
)
echo [OK] Java found

REM Check Maven
mvn -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven and add it to your PATH
    pause
    exit /b 1
)
echo [OK] Maven found

REM Check Node.js
node --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Node.js is not installed or not in PATH
    echo Please install Node.js and add it to your PATH
    pause
    exit /b 1
)
echo [OK] Node.js found

REM Check npm
npm --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: npm is not installed
    echo Please install npm
    pause
    exit /b 1
)
echo [OK] npm found

echo.
echo ============================================
echo Starting Backend...
echo ============================================
echo Backend will be available at: http://localhost:8080/api
echo.

REM Start backend in a new terminal
start cmd /k "cd backend && mvn spring-boot:run"

REM Wait a bit for backend to start
echo Waiting for backend to start (5 seconds)...
timeout /t 5 /nobreak

echo.
echo ============================================
echo Starting Frontend...
echo ============================================
echo Frontend will be available at: http://localhost:4200
echo.

REM Start frontend in a new terminal
start cmd /k "cd frontend && npm install && npm start"

echo.
echo ============================================
echo Startup Complete!
echo ============================================
echo.
echo Backend: http://localhost:8080/api
echo Frontend: http://localhost:4200
echo API Docs: http://localhost:8080/api/swagger-ui.html (if available)
echo Database Console: http://localhost:8080/api/h2-console
echo.
echo Username: sa
echo Password: (empty)
echo.
echo Press Ctrl+C in each terminal to stop the services.
echo.
pause
goto end

:backend_only
echo.
echo Starting Backend Only...
echo.
cd backend
mvn spring-boot:run
goto end

:frontend_only
echo.
echo Starting Frontend Only...
echo.
cd frontend
echo Installing dependencies...
call npm install
echo Starting dev server...
call npm start
goto end

:docker_mode
echo.
echo Starting with Docker Compose...
echo.
echo Checking if Docker is installed...
docker --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker is not installed or not in PATH
    echo Please install Docker Desktop from https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)
echo [OK] Docker found

echo.
echo Building Docker images...
docker-compose build

echo.
echo Starting containers...
docker-compose up -d

echo.
echo ============================================
echo Docker Containers Started!
echo ============================================
echo.
echo Frontend: http://localhost:4200
echo Backend: http://localhost:8080/api
echo Database Console: http://localhost:8080/api/h2-console
echo.
echo View logs:
echo   docker-compose logs -f
echo.
echo Stop containers:
echo   docker-compose down
echo.
pause
goto end

:exit_script
echo Exiting...
goto end

:end
endlocal
