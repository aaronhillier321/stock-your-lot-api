# Build stage - Azul Zulu OpenJDK 21
FROM azul/zulu-openjdk:21 AS builder
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle .
COPY src src

RUN chmod +x gradlew && ./gradlew bootJar --no-daemon -x test

# Run stage - Azul Zulu OpenJDK 21 JRE
FROM azul/zulu-openjdk:21-jre
WORKDIR /app

RUN useradd -u 1000 -m appuser
USER appuser

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
