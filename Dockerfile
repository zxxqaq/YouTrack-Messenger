# Multi-stage Dockerfile
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

# Copy build files
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle/ gradle/
COPY src/ src/

# Build application
RUN gradle build -x test

# Second stage: runtime environment
FROM openjdk:21-jdk-slim

# Install necessary tools
RUN apt-get update && \
    apt-get install -y curl jq && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy jar file from build stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Create necessary directories
RUN mkdir -p /app/data /app/logs

# Set permissions
RUN chmod 755 /app/data /app/logs

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
