# ── Stage 1: Build ──────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Copy Gradle wrapper + root build files first (layer-cache friendly)
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./

# Copy only this subproject's source
COPY api-gateway/ api-gateway/

RUN chmod +x gradlew && \
    ./gradlew :api-gateway:bootJar -x test --no-daemon

# ── Stage 2: Runtime ────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /workspace/api-gateway/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
