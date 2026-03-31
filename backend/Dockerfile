FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Cache dependencies
COPY .mvn/ .mvn/
COPY mvnw mvnw
COPY pom.xml pom.xml
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

# Build
COPY src/ src/
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:17-jre

WORKDIR /app
ENV JAVA_OPTS=""

# Render will set PORT; Spring should bind to it
ENV SERVER_PORT=8080

COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080
CMD ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-$SERVER_PORT} -jar /app/app.jar"]

