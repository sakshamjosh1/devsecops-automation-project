# Build stage: compile the app with Maven inside Docker
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests clean package

# Run stage: use a small JRE image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# copy the jar produced by the build stage
COPY --from=build /workspace/target/devsecops-simple-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-cp","/app/app.jar","Main"]

