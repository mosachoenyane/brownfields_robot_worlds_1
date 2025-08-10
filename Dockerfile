# Use a base image with Java and Maven
FROM maven:3.9-eclipse-temurin-17 AS build

# Set the working directory
WORKDIR /app

# Copy the project files
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Create the runtime image
FROM eclipse-temurin:17-jre-jammy

RUN apt-get update && apt-get install -y git

WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=build /app/target/robot-world-0.0.2.jar ./app.jar

# Expose the port the server runs on
EXPOSE 5050
# Command to run the application
CMD ["java", "-jar", "app.jar", "-p","5050","-s","10","-o","1,1"]
