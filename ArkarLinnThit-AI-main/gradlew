#!/bin/sh
set -eu
BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
GRADLE_VERSION=9.3.1
DIST="$HOME/.gradle/wrapper/dists/gradle-$GRADLE_VERSION-bin"
ZIP="$DIST/gradle-$GRADLE_VERSION-bin.zip"
ROOT="$DIST/gradle-$GRADLE_VERSION"
if [ ! -x "$ROOT/bin/gradle" ]; then
  mkdir -p "$DIST"
  if [ ! -f "$ZIP" ]; then
    curl -fL --retry 3 "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o "$ZIP"
  fi
  rm -rf "$DIST/gradle-$GRADLE_VERSION"
  unzip -q "$ZIP" -d "$DIST"
fi
exec "$ROOT/bin/gradle" "$@"
