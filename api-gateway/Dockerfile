# ============================================================
# Stage 1: BUILD — Compile API Gateway using Maven
# ============================================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copy pom.xml from api-gateway
COPY pom.xml .

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B || true

# Copy source code
COPY src src

# Build the application (skip tests)
RUN mvn clean package -DskipTests -B

# ============================================================
# Stage 2: RUN — Lightweight runtime image
# ============================================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy built artifact from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Switch to non-root user
USER appuser

# Expose API Gateway port
EXPOSE 8085

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8085/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]

