# Build stage - official Gradle image (includes JDK 21)
FROM gradle:8.7-jdk21 AS builder
WORKDIR /app

COPY build.gradle settings.gradle .
COPY src src

RUN gradle bootJar --no-daemon

# Run stage - Azul Zulu OpenJDK 21 JRE
FROM azul/zulu-openjdk:21-jre
WORKDIR /app

RUN useradd -u 1000 -m appuser
USER appuser

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
