#!/usr/bin/env sh
# Restore gradle-wrapper.jar if missing or empty (e.g. after fresh clone).
# Run from project root: ./scripts/restore-gradle-wrapper.sh
set -e
WRAPPER_JAR="gradle/wrapper/gradle-wrapper.jar"
GRADLE_VERSION=8.7
# Need a real jar (Zip); empty or XML error response is ~300 bytes
if [ -f "$WRAPPER_JAR" ] && [ -s "$WRAPPER_JAR" ] && [ "$(wc -c < "$WRAPPER_JAR")" -gt 1000 ]; then
  echo "$WRAPPER_JAR already exists and looks valid. Nothing to do."
  exit 0
fi
mkdir -p gradle/wrapper
echo "Downloading Gradle $GRADLE_VERSION wrapper jar from GitHub..."
curl -sL -o "$WRAPPER_JAR" "https://github.com/gradle/gradle/raw/v${GRADLE_VERSION}.0/gradle/wrapper/gradle-wrapper.jar"
echo "Done. Run ./gradlew to use the wrapper."
