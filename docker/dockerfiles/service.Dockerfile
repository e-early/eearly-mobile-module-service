FROM maven:3.9.8-eclipse-temurin-22 AS build

WORKDIR /app

COPY . .

RUN mvn clean verify -DskipTests

FROM openjdk:22-jdk-slim

WORKDIR /app

COPY --from=build /app/eearly/target/eearly-0.0.1-SNAPSHOT.jar eearly.jar

CMD ["java", "-jar", "/app/eearly.jar"]
