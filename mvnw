#!/bin/sh
set -e

MAVEN_HOME="${MAVEN_HOME:-$HOME/.m2/wrapper/dists}"
MAVEN_VERSION="3.9.6"
MAVEN_URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.tar.gz"
MAVEN_DIR="${MAVEN_HOME}/apache-maven-${MAVEN_VERSION}"

if [ ! -d "$MAVEN_DIR" ]; then
    echo "Downloading Maven ${MAVEN_VERSION}..."
    mkdir -p "$MAVEN_HOME"
    curl -fsSL "$MAVEN_URL" | tar -xz -C "$MAVEN_HOME"
fi

exec "$MAVEN_DIR/bin/mvn" "$@"
