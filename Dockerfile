# ==============================================================================
# Multi-stage Dockerfile pour fotolou-backend (Java 21 / Spring Boot 4)
# Optimisé pour Render, Docker Compose et serveurs VPS
# ==============================================================================

# --- Étape 1 : Compilation Maven ---
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace/app

# Copie des fichiers de configuration Maven et des sources
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Autorisation d'exécution du wrapper et compilation du JAR de production
RUN chmod +x ./mvnw && ./mvnw clean package -Pprod -DskipTests -B

# --- Étape 2 : Image d'exécution légère (JRE 21) ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Création d'un utilisateur non-root pour la sécurité
RUN addgroup -S fotolou && adduser -S fotolou -G fotolou

# Récupération du JAR généré
COPY --from=builder /workspace/app/target/*.jar app.jar

# Dossier d'upload local (fallback si Cloudinary inactif)
RUN mkdir -p /app/uploads && chown -R fotolou:fotolou /app

USER fotolou

# Exposition du port (Render injecte la variable PORT dynamiquement)
ENV PORT=8080
EXPOSE 8080

# Optimisations JVM mémoire pour conteneurs cloud (Render Free Tier 512MB RAM)
ENV JAVA_OPTS="-Xms128m -Xmx384m -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar --spring.profiles.active=prod"]
