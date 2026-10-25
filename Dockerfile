# Multi-stage Dockerfile for EduPath Spring Boot Application

# Stage 1: Build with Maven and Java 21 LTS
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and build production jar
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Lightweight Production Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy executable jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Cloud hosts automatically bind to $PORT (Render, Railway, Fly.io, Cloud Run)
ENV PORT=8080
EXPOSE 8080

# Start Spring Boot listening on cloud assigned port
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
