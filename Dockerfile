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

# Create entrypoint script to pass environment variables
# Converts postgresql:// URL to jdbc:postgresql:// format
RUN echo '#!/bin/sh' > /app/entrypoint.sh && \
    echo 'JDBC_URL="jdbc:${DATABASE_URL}"' >> /app/entrypoint.sh && \
    echo 'exec java -jar app.jar \' >> /app/entrypoint.sh && \
    echo '  --spring.profiles.active=prod \' >> /app/entrypoint.sh && \
    echo '  --spring.datasource.url="$JDBC_URL" \' >> /app/entrypoint.sh && \
    echo '  --spring.datasource.username="$DATABASE_USERNAME" \' >> /app/entrypoint.sh && \
    echo '  --spring.datasource.password="$DATABASE_PASSWORD" \' >> /app/entrypoint.sh && \
    echo '  --jwt.secret="$JWT_SECRET" \' >> /app/entrypoint.sh && \
    echo '  --cors.allowed-origins="$CORS_ALLOWED_ORIGINS"' >> /app/entrypoint.sh && \
    chmod +x /app/entrypoint.sh

ENTRYPOINT ["/app/entrypoint.sh"]
