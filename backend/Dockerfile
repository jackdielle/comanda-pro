# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copia pom.xml
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copia il codice sorgente
COPY src ./src

# Build l'applicazione
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copia il JAR dalla fase di build
COPY --from=builder /app/target/*.jar app.jar

# Crea la directory per i dati
RUN mkdir -p /app/data

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD java -cp app.jar org.springframework.boot.loader.JarLauncher \
  -c "curl -f http://localhost:8080/api/health || exit 1"

# Run
ENTRYPOINT ["java", "-jar", "app.jar"]
