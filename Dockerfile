# Build stage - official Gradle image (includes JDK 17)
FROM gradle:8.7-jdk17 AS builder
WORKDIR /app

COPY build.gradle settings.gradle .
COPY src src

RUN gradle bootJar --no-daemon

# Run stage - Azul Zulu OpenJDK 17 JRE
FROM azul/zulu-openjdk:17-jre
WORKDIR /app

RUN useradd -u 1000 -m appuser
USER appuser

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
