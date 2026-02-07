# Multi-stage build for Spring Boot Gateway

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy pom.xml and download dependencies (with retry for network issues)
COPY pom.xml .
RUN for i in 1 2 3; do \
      mvn dependency:go-offline -B && break; \
      echo "Retry $i failed, waiting..."; \
      sleep 5; \
    done

# Copy source code and build (with retry for network issues)
COPY src ./src
RUN for i in 1 2 3; do \
      mvn clean package -DskipTests -B && break; \
      echo "Retry $i failed, waiting..."; \
      sleep 5; \
    done || (echo "Maven build failed after 3 retries" && exit 1)

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install wget for health checks (with retry & fallback mirror)
RUN for i in 1 2 3; do \
      apk update && apk add --no-cache wget && break; \
      echo "Retry $i: apk failed, trying fallback mirror..."; \
      echo 'https://dl-cdn.alpinelinux.org/alpine/v3.21/main' > /etc/apk/repositories && \
      echo 'https://dl-cdn.alpinelinux.org/alpine/v3.21/community' >> /etc/apk/repositories; \
      sleep 3; \
    done || echo 'WARN: wget install skipped'

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy built JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose port
EXPOSE 9000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:9000/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

