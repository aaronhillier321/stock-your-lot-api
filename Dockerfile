# Build stage - Azul Zulu OpenJDK 21
FROM azul/zulu-openjdk:21 AS builder
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle .
COPY src src

# Ensure wrapper jar exists (in case it's not in the build context, e.g. gitignored in CI)
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && curl -sL -o gradle/wrapper/gradle-wrapper.jar https://services.gradle.org/distributions/gradle-8.7-wrapper.jar \
    && apt-get purge -y curl && apt-get autoremove -y && rm -rf /var/lib/apt/lists/*

RUN chmod +x gradlew && ./gradlew bootJar --no-daemon

# Run stage - Azul Zulu OpenJDK 21 JRE
FROM azul/zulu-openjdk:21-jre
WORKDIR /app

RUN useradd -u 1000 -m appuser
USER appuser

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
