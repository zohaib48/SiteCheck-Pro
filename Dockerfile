# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only pom first to leverage Docker cache for dependencies
COPY demo/pom.xml demo/pom.xml
RUN mvn -f demo/pom.xml -q -DskipTests dependency:go-offline

# Copy source and build
COPY demo/src demo/src
RUN mvn -f demo/pom.xml -q -DskipTests package

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built jar
COPY --from=build /app/demo/target/demo-0.0.1-SNAPSHOT.jar app.jar

# Render provides PORT env; expose for local use
ENV PORT=8080
EXPOSE 8080

# Start
CMD ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]
