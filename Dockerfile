# Use Eclipse Temurin (OpenJDK 21) as base image
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Copy Maven wrapper and configuration
COPY mvnw mvnw.cmd .mvn .mvn/wrapper .mvn/wrapper/maven-wrapper.properties pom.xml ./

# Make mvnw executable
RUN chmod +x mvnw

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Run the application
CMD ["java", "-jar", "target/*.jar"]
