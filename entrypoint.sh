#!/bin/sh
# Entrypoint script - Configure Spring Boot with Render environment variables

# Convert Neon's postgresql:// URL to JDBC format by prepending "jdbc:"
JDBC_URL="jdbc:${DATABASE_URL}"

# Run Spring Boot with system properties that override everything
exec java \
  -Dspring.profiles.active=prod \
  -Dspring.datasource.url="${JDBC_URL}" \
  -Dspring.datasource.username="${DATABASE_USERNAME}" \
  -Dspring.datasource.password="${DATABASE_PASSWORD}" \
  -Djwt.secret="${JWT_SECRET}" \
  -Dcors.allowed-origins="${CORS_ALLOWED_ORIGINS}" \
  -jar /app/app.jar
