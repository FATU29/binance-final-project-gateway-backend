# Multi-stage build for Spring Boot Gateway

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Configure Maven to use Central repository
RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?><settings><mirrors><mirror><id>central</id><name>Maven Central</name><url>https://repo.maven.apache.org/maven2</url><mirrorOf>*</mirrorOf></mirror></mirrors></settings>' > /root/.m2/settings.xml

# Copy pom.xml and download dependencies (with retry)
COPY pom.xml .
RUN for i in 1 2 3 4 5; do \
      mvn dependency:go-offline -B && break || \
      (echo "Maven dependency attempt $i failed, retrying in 15s..." && sleep 15); \
    done

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -B && \
    ls -lh /app/target/*.jar | grep -v sources | grep -v javadoc

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install wget for health checks (with DNS retry)
RUN for i in 1 2 3 4 5; do \
      apk update && apk add --no-cache wget && break || \
      (echo "Attempt $i failed, retrying in 10s..." && sleep 10); \
    done

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

