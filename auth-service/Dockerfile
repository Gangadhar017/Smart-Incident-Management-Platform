# Build Stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY gateway-service/pom.xml gateway-service/
COPY auth-service/pom.xml auth-service/
COPY incident-service/pom.xml incident-service/
COPY notification-service/pom.xml notification-service/
COPY reporting-service/pom.xml reporting-service/
COPY auth-service/src auth-service/src
RUN mvn -pl auth-service -am clean package -DskipTests

# Runtime Stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/auth-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
