.PHONY: help build dev prod clean install frontend backend docker-build docker-up docker-down

help:
	@echo "Gestore Comande - Available Commands"
	@echo "===================================="
	@echo "make install          - Install dependencies for both frontend and backend"
	@echo "make dev              - Run in development mode"
	@echo "make backend          - Run only backend"
	@echo "make frontend         - Run only frontend"
	@echo "make build            - Build both frontend and backend"
	@echo "make backend-build    - Build only backend"
	@echo "make frontend-build   - Build only frontend"
	@echo "make docker-build     - Build Docker images"
	@echo "make docker-up        - Start containers with Docker Compose"
	@echo "make docker-down      - Stop Docker containers"
	@echo "make clean            - Clean build artifacts"
	@echo "make test             - Run tests"

install:
	@echo "Installing dependencies..."
	cd backend && mvn clean install
	cd frontend && npm install

dev: install
	@echo "Starting development environment..."
	@echo "Backend will start on http://localhost:8080/api"
	@echo "Frontend will start on http://localhost:4200"
	cd backend && mvn spring-boot:run &
	cd frontend && npm start

backend:
	@echo "Starting backend only..."
	cd backend && mvn spring-boot:run

frontend:
	@echo "Starting frontend only..."
	cd frontend && npm start

build:
	@echo "Building both applications..."
	cd backend && mvn clean package -DskipTests
	cd frontend && npm run build

backend-build:
	@echo "Building backend..."
	cd backend && mvn clean package -DskipTests

frontend-build:
	@echo "Building frontend..."
	cd frontend && npm run build

docker-build:
	@echo "Building Docker images..."
	docker-compose build

docker-up:
	@echo "Starting Docker containers..."
	docker-compose up -d
	@echo "Backend: http://localhost:8080/api"
	@echo "Frontend: http://localhost:4200"

docker-down:
	@echo "Stopping Docker containers..."
	docker-compose down

docker-logs:
	docker-compose logs -f

clean:
	@echo "Cleaning build artifacts..."
	cd backend && mvn clean
	cd frontend && rm -rf dist node_modules
	docker-compose down -v

test:
	@echo "Running tests..."
	cd backend && mvn test
	cd frontend && npm test

# Database commands
db-console:
	@echo "H2 Database Console available at http://localhost:8080/api/h2-console"
	@echo "JDBC URL: jdbc:h2:file:./data/gestoredb"
	@echo "Username: sa"

# Utility
status:
	@echo "Checking services status..."
	@curl -s http://localhost:8080/api/health && echo "✓ Backend OK" || echo "✗ Backend DOWN"
	@curl -s http://localhost:4200 > /dev/null && echo "✓ Frontend OK" || echo "✗ Frontend DOWN"
