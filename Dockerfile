# Dockerfile for Connectra Backend

# STAGE 1: Build the application
FROM eclipse-temurin:21-jdk-alpine AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy Maven wrapper and pom.xml files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make Maven wrapper executable
RUN chmod +x ./mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy the source code
COPY src src

# Build the application (skip tests for faster builds)
RUN ./mvnw clean package -DskipTests


# STAGE 2: Create the runtime image
FROM eclipse-temurin:21-jre-alpine

# Set the working directory
WORKDIR /app

# Create a non-root user for security
RUN addgroup -S connectra-group && adduser -S connectra-user -G connectra-group

# Copy the JAR file from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Change ownership to the connectra user
RUN chown connectra-user:connectra-group app.jar

# Switch to the non-root user
USER connectra-user

# Expose the port your app runs on
EXPOSE 8080

# Set default Spring profile to production
ENV SPRING_PROFILES_ACTIVE=prod

# Run the application
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
