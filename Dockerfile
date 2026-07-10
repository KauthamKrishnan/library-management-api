# ---- Stage 1: build the fat jar ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Cache dependencies first for faster rebuilds.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- Stage 2: minimal runtime image ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# Run as a non-root user for better container security.
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080

# Defaults to H2 profile; override via SPRING_PROFILES_ACTIVE at runtime.
ENTRYPOINT ["java", "-jar", "app.jar"]
