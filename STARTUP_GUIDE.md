# 🚀 Startup Guide - Gestore Comande v2.0

This guide explains how to start the application using various methods.

---

## ⚡ Quick Start (Easiest)

### Windows Users
```bash
# Run the startup script
START.bat
```

### Mac/Linux Users
```bash
# Make the script executable
chmod +x start.sh

# Run the startup script
./start.sh
```

---

## 🎯 Startup Options

### Option 1: Development Mode (Recommended for Development)

**Windows:**
```bash
START.bat
# Then select option 1
```

**Mac/Linux:**
```bash
./start.sh
# Then select option 1
```

**What it does:**
- Starts backend on http://localhost:8080/api
- Starts frontend on http://localhost:4200
- Both run in separate terminal windows
- Hot reload enabled for both services

**Terminal 1 - Backend:**
```bash
cd backend
mvn spring-boot:run
```

**Terminal 2 - Frontend:**
```bash
cd frontend
npm install
npm start
```

---

### Option 2: Using Make Commands

```bash
# Install all dependencies first
make install

# Run everything
make dev

# Or run individually
make backend      # Backend only
make frontend     # Frontend only
```

---

### Option 3: Docker Compose (Production-like)

**Requirements:** Docker Desktop installed

```bash
# Start everything
docker-compose up -d

# View logs
docker-compose logs -f

# Stop everything
docker-compose down
```

**What it does:**
- Builds Docker images automatically
- Runs backend and frontend in containers
- Isolated environment (like production)
- No local dependencies needed (except Docker)

---

### Option 4: Using Startup Scripts

**Windows (Recommended):**
```bash
START.bat
```

**Mac/Linux:**
```bash
./start.sh
```

Both scripts provide an interactive menu:
```
1. Development Mode (Backend + Frontend)
2. Backend Only
3. Frontend Only
4. Docker (Full Stack)
5. Exit
```

---

## 📋 Prerequisites

### For Local Development (Options 1 & 2)

Install these before running:

1. **Java 17+**
   - Download: https://www.oracle.com/java/technologies/downloads/
   - Verify: `java -version`

2. **Maven 3.8+**
   - Download: https://maven.apache.org/download.cgi
   - Verify: `mvn -version`

3. **Node.js 18+**
   - Download: https://nodejs.org/
   - Verify: `node --version` and `npm --version`

### For Docker (Option 3)

1. **Docker Desktop**
   - Download: https://www.docker.com/products/docker-desktop
   - Verify: `docker --version`

---

## 🌐 Access Points

After starting, access the application at:

| Service | URL | Purpose |
|---------|-----|---------|
| **Frontend** | http://localhost:4200 | Main application UI |
| **Backend API** | http://localhost:8080/api | REST API |
| **H2 Console** | http://localhost:8080/api/h2-console | Database browser |
| **Health Check** | http://localhost:8080/api/health | API status |

**Database Access:**
- Username: `sa`
- Password: (leave empty)
- JDBC URL: `jdbc:h2:file:./data/gestoredb`

---

## 🛑 Stopping the Application

### Development Mode
- Press `Ctrl+C` in each terminal window

### Docker Mode
```bash
docker-compose down
```

### Using Make
```bash
make clean
```

---

## 🔧 Useful Commands

### View Backend Logs
```bash
docker-compose logs backend -f
```

### View Frontend Logs
```bash
docker-compose logs frontend -f
```

### Check Service Health
```bash
# Backend health
curl http://localhost:8080/api/health

# Frontend (should return HTML)
curl http://localhost:4200
```

### Clean Up Everything
```bash
make clean
# Or
docker-compose down -v
```

---

## 🐛 Troubleshooting

### Port Already in Use

**Port 8080 (Backend)**
```bash
# Change in backend/src/main/resources/application.properties
server.port=8081
```

**Port 4200 (Frontend)**
```bash
cd frontend
ng serve --port 4201
```

### Java Not Found
```bash
# Add Java to PATH (Windows)
set JAVA_HOME=C:\Program Files\Java\jdk-17

# Or Mac/Linux
export JAVA_HOME=/usr/libexec/java_home -v 17
```

### Maven Not Found
```bash
# Add Maven to PATH (Windows)
set MAVEN_HOME=C:\Program Files\apache-maven-3.8.1
set PATH=%MAVEN_HOME%\bin;%PATH%

# Or Mac/Linux
export MAVEN_HOME=/opt/apache-maven-3.8.1
export PATH=$MAVEN_HOME/bin:$PATH
```

### npm Dependency Issues
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
npm start
```

### Docker Not Starting
```bash
# Rebuild images
docker-compose down
docker-compose up --build
```

### Database Issues
```bash
# Remove old database and restart
rm -rf backend/data/
make dev
```

---

## 📊 What Gets Started

### Backend (Spring Boot)
- REST API with 20+ endpoints
- H2 Database (embedded)
- Pre-loaded test data (3 customers, 28 products)
- Health checks and API documentation

### Frontend (Angular)
- Single Page Application (SPA)
- 5 main pages (Dashboard, Orders, Customers, Products, Login)
- Responsive design (mobile-friendly)
- Real-time data binding

### Database
- H2 embedded database
- 4 tables (Customers, Products, Orders, OrderLines)
- Auto-initialized with test data
- File-based persistence

---

## 📈 Startup Time

Expected startup times:

| Method | Time | Notes |
|--------|------|-------|
| Development Mode | 30-60 seconds | First run slower (dependencies) |
| Docker Mode | 2-5 minutes | First run slower (building images) |
| Subsequent runs | 15-30 seconds | Much faster |

---

## ✅ Successful Startup Checklist

After starting, verify everything works:

- [ ] Backend running: `curl http://localhost:8080/api/health`
- [ ] Frontend loading: Open http://localhost:4200
- [ ] Database accessible: http://localhost:8080/api/h2-console
- [ ] No error messages in console

---

## 🚀 Next Steps

After starting the application:

1. Open http://localhost:4200 in your browser
2. Explore the Dashboard
3. Create a new order
4. Add customers and products
5. View the H2 Database Console
6. Read the API docs

---

## 📞 Support

If you encounter issues:

1. Check the troubleshooting section above
2. Review the main README.md
3. Check console output for error messages
4. Verify all prerequisites are installed
5. Try `make clean` and restart

---

**Happy coding! 🍕🚀**