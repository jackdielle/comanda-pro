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

# Run with production profile and pass environment variables
CMD java \
  -Dspring.profiles.active=prod \
  -Dspring.datasource.url=${DATABASE_URL} \
  -Dspring.datasource.username=${DATABASE_USERNAME} \
  -Dspring.datasource.password=${DATABASE_PASSWORD} \
  -Djwt.secret=${JWT_SECRET} \
  -Dcors.allowed-origins=${CORS_ALLOWED_ORIGINS} \
  -jar app.jar
