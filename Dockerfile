# ── Build stage ──
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

# ── Run stage ──
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render/Railway inject PORT; bind it via a shell entrypoint so ${PORT:-8080} works
EXPOSE 8080

# Configure via env vars (see .env.example):
#   DB_URL, DB_USERNAME, DB_PASSWORD  — cloud MySQL (e.g. Aiven free tier)
#   OPENROUTER_API_KEY                — AI features
# Shell form so ${PORT:-8080} expands at runtime (Render/Railway inject PORT)
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]
