# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build

# Copy the full project into the container
COPY . /build

# Build only the gateway module (and dependencies)
RUN mvn clean package -pl eureka-server -am -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /build/eureka-server/target/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
