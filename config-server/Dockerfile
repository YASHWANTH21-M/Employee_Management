# ============================================================
# Stage 1: BUILD — Compile Config Server using Maven
# ============================================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copy pom.xml from config-server
COPY pom.xml .

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B || true

# Copy source code
COPY src src

# Build application
RUN mvn clean package -DskipTests -B

# ============================================================
# Stage 2: RUN — Lightweight runtime image
# ============================================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy built artifact
COPY --from=builder /app/target/*.jar app.jar

# Switch to non-root user
USER appuser

# Expose Config Server port
EXPOSE 8888

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8888/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]

