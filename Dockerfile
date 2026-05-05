# ================================================================
# Stage 1 — BUILD
# Dùng Maven image để compile và package thành JAR
# Image này nặng (~500MB) nhưng chỉ dùng trong build, không ship lên prod
# ================================================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom.xml trước → tận dụng Docker layer cache
# Nếu chỉ thay đổi source code (không đổi dependencies) → layer này không rebuild
COPY pom.xml .
RUN mvn dependency:go-offline -B --no-transfer-progress

# Copy source và build JAR (skip tests vì CI đã chạy test riêng)
COPY src ./src
RUN mvn package -DskipTests -B --no-transfer-progress

# ================================================================
# Stage 2 — RUNTIME
# Dùng JRE Alpine (chỉ Java Runtime, không có Maven/JDK) → image nhỏ hơn ~4x
# eclipse-temurin là JDK/JRE chính thức được Eclipse Foundation maintain
# ================================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Tạo user non-root để chạy app → security best practice
# Không bao giờ chạy app bằng root trong container production
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Chỉ copy JAR từ build stage (không copy Maven, source code, v.v.)
COPY --from=builder /app/target/*.jar app.jar

# Chuyển ownership file về appuser
RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8080

# JVM Flags tối ưu cho container:
# -XX:+UseContainerSupport      → JVM đọc CPU/RAM limit từ Docker (không đọc host)
# -XX:MaxRAMPercentage=75.0     → Dùng tối đa 75% RAM trong container cho JVM heap
# -Djava.security.egd=...       → Tăng tốc khởi động (entropy source nhanh hơn)
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]