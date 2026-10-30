# ---------- Étape 1 : compilation avec Maven ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copier d'abord le pom pour profiter du cache Docker des dépendances
COPY pom.xml .
RUN mvn -q dependency:go-offline

COPY src ./src
RUN mvn -q clean package -DskipTests

# ---------- Étape 2 : image d'exécution légère ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Ne pas exécuter l'application en root
RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
