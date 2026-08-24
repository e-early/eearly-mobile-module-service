FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /build

COPY . .

COPY settings.xml /root/.m2/settings.xml

RUN mvn clean deploy

FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=build /build/eearly/target/eearly.jar eearly.jar

CMD ["java", "-jar", "/app/eearly.jar"]
