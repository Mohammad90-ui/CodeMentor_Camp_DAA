# Multi-stage Docker build for Spring Boot
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B || true

COPY src src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN useradd -m appuser
USER appuser

COPY --from=build /app/target/campus-1.0.0.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
