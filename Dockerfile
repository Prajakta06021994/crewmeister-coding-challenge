# Use a base image with Java 11
FROM eclipse-temurin:11-jre

# Set application directory
WORKDIR /app

# Copy the fat jar (assumes Maven build output)
COPY target/cm-coding-challenge-0.0.1-SNAPSHOT.jar app.jar

# Expose port (change if your app uses another one)
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
