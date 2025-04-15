FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build

COPY . /build

RUN mvn clean package -pl venue-service -am -DskipTests

FROM eclipse-temurin:21-jdk
WORKDIR /app

COPY --from=builder /build/venue-service/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
