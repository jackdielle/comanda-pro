#!/bin/bash

# ============================================
# Gestore Comande v2.0 - Startup Script
# Unix/Linux/Mac Shell Script
# ============================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Functions
check_prerequisites() {
    echo "Checking prerequisites..."

    # Check Java
    if ! command -v java &> /dev/null; then
        echo -e "${RED}ERROR: Java 17+ is not installed${NC}"
        exit 1
    fi
    echo -e "${GREEN}[OK]${NC} Java found"

    # Check Maven
    if ! command -v mvn &> /dev/null; then
        echo -e "${RED}ERROR: Maven is not installed${NC}"
        exit 1
    fi
    echo -e "${GREEN}[OK]${NC} Maven found"

    # Check Node.js
    if ! command -v node &> /dev/null; then
        echo -e "${RED}ERROR: Node.js is not installed${NC}"
        exit 1
    fi
    echo -e "${GREEN}[OK]${NC} Node.js found"

    # Check npm
    if ! command -v npm &> /dev/null; then
        echo -e "${RED}ERROR: npm is not installed${NC}"
        exit 1
    fi
    echo -e "${GREEN}[OK]${NC} npm found
}

dev_mode() {
    echo ""
    echo "============================================"
    echo "Starting Development Mode"
    echo "============================================"
    echo ""

    check_prerequisites

    echo ""
    echo -e "${YELLOW}Starting Backend...${NC}"
    echo "Backend will be available at: http://localhost:8080/api"
    echo ""

    # Start backend in background
    (cd backend && mvn spring-boot:run) &
    BACKEND_PID=$!

    # Wait for backend to start
    echo "Waiting for backend to start..."
    sleep 5

    echo ""
    echo -e "${YELLOW}Starting Frontend...${NC}"
    echo "Frontend will be available at: http://localhost:4200"
    echo ""

    # Start frontend
    (cd frontend && npm install && npm start)
    FRONTEND_PID=$!

    # Cleanup on exit
    trap "kill $BACKEND_PID $FRONTEND_PID 2>/dev/null" EXIT
    wait
}

backend_only() {
    echo ""
    echo "============================================"
    echo "Starting Backend Only"
    echo "============================================"
    echo ""

    cd backend
    mvn spring-boot:run
}

frontend_only() {
    echo ""
    echo "============================================"
    echo "Starting Frontend Only"
    echo "============================================"
    echo ""

    cd frontend
    echo "Installing dependencies..."
    npm install
    echo "Starting dev server..."
    npm start
}

docker_mode() {
    echo ""
    echo "============================================"
    echo "Starting with Docker Compose"
    echo "============================================"
    echo ""

    # Check Docker
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}ERROR: Docker is not installed${NC}"
        exit 1
    fi
    echo -e "${GREEN}[OK]${NC} Docker found"

    echo ""
    echo "Building Docker images..."
    docker-compose build

    echo ""
    echo "Starting containers..."
    docker-compose up -d

    echo ""
    echo -e "${GREEN}============================================${NC}"
    echo -e "${GREEN}Docker Containers Started!${NC}"
    echo -e "${GREEN}============================================${NC}"
    echo ""
    echo "Frontend: http://localhost:4200"
    echo "Backend: http://localhost:8080/api"
    echo "Database Console: http://localhost:8080/api/h2-console"
    echo ""
    echo "View logs:"
    echo "  docker-compose logs -f"
    echo ""
    echo "Stop containers:"
    echo "  docker-compose down"
    echo ""
    echo "Press Ctrl+C to exit"
}

# Main
echo ""
echo "============================================"
echo " Gestore Comande v2.0 Startup"
echo "============================================"
echo ""

# Check if running from correct directory
if [ ! -d "backend" ] || [ ! -d "frontend" ]; then
    echo -e "${RED}ERROR: backend or frontend directory not found!${NC}"
    echo "Please run this script from the project root directory."
    exit 1
fi

echo "Please choose a startup option:"
echo ""
echo "1. Development Mode (Backend + Frontend)"
echo "2. Backend Only"
echo "3. Frontend Only"
echo "4. Docker (Full Stack)"
echo "5. Exit"
echo ""

read -p "Enter your choice (1-5): " choice

case $choice in
    1)
        dev_mode
        ;;
    2)
        backend_only
        ;;
    3)
        frontend_only
        ;;
    4)
        docker_mode
        ;;
    5)
        echo "Exiting..."
        exit 0
        ;;
    *)
        echo -e "${RED}Invalid choice. Please try again.${NC}"
        exit 1
        ;;
esac