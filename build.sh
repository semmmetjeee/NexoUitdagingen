#!/usr/bin/env bash
set -euo pipefail
VERSION="${1:-1.0.0}"
mvn -B -ntp -Drevision="$VERSION" clean package
echo "Built target/NexoUitdagingen-${VERSION}.jar"
