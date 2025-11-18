# --- STAGE 1: The Build Stage ---
# We use a full JDK and Maven image to build our app
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copy the Maven wrapper and pom.xml
# Note: This assumes .mvn, mvnw, and pom.xml are in this directory
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

#FIX: Make mvnw executable for building in jenkins day10
RUN chmod +x mvnw

# Download all dependencies first to leverage Docker layer caching
RUN ./mvnw dependency:go-offline

# Copy the rest of the source code and build the application
COPY src ./src
RUN ./mvnw clean package -DskipTests

# --- STAGE 2: The Production/Run Stage ---
# We use a minimal JRE image for a small, secure final container
FROM eclipse-temurin:17-jre-alpine

# Set the working directory
WORKDIR /app

# Copy the built JAR from the 'build' stage
# This JAR name must match your <artifactId> in this project's pom.xml
COPY --from=build /app/target/my-spring-project-0.0.1-SNAPSHOT.jar app.jar

# Expose the port Spring Boot runs on
EXPOSE 8080

# The command to run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]