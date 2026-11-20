#!/usr/bin/env bash
# Downloads the Gradle wrapper JAR so local `./gradlew` builds work.
# Run this once after cloning: bash setup.sh

set -e

GRADLE_VERSION="8.9"
JAR_PATH="gradle/wrapper/gradle-wrapper.jar"

if [ -f "$JAR_PATH" ]; then
  echo "Gradle wrapper JAR already present."
  exit 0
fi

echo "Downloading gradle-wrapper.jar for Gradle ${GRADLE_VERSION}..."
mkdir -p gradle/wrapper

# Try fetching from GitHub source
curl -sSL \
  "https://raw.githubusercontent.com/gradle/gradle/v${GRADLE_VERSION}.0/gradle/wrapper/gradle-wrapper.jar" \
  -o "$JAR_PATH" 2>/dev/null || {
  echo "Direct fetch failed. Trying via gradle distribution..."
  TMP=$(mktemp -d)
  curl -sSL \
    "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" \
    -o "$TMP/gradle.zip"
  unzip -j "$TMP/gradle.zip" "*/gradle-wrapper.jar" -d gradle/wrapper/
  rm -rf "$TMP"
}

chmod +x gradlew
echo "Done. Run: ./gradlew assembleDebug"
