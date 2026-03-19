# Multi-stage build for Spring Boot application
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY backend/pom.xml .
RUN mvn dependency:resolve

# Copy source code
COPY backend/src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# Run with production profile and environment variables
# Render passes env vars to the process, Spring reads them via --spring.config.location
CMD java \
  -jar app.jar \
  --spring.profiles.active=prod \
  --spring.datasource.url="$DATABASE_URL" \
  --spring.datasource.username="$DATABASE_USERNAME" \
  --spring.datasource.password="$DATABASE_PASSWORD" \
  --jwt.secret="$JWT_SECRET" \
  --cors.allowed-origins="$CORS_ALLOWED_ORIGINS"
