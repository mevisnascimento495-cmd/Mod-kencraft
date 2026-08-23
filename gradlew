#!/bin/sh
set -eu
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
URL="https://raw.githubusercontent.com/gradle/gradle/v8.10.2/gradle/wrapper/gradle-wrapper.jar"
mkdir -p "$(dirname "$JAR")"
if [ ! -f "$JAR" ]; then
  echo "Downloading Gradle 8.10.2 wrapper..." >&2
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$URL" -o "$JAR"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$URL" -O "$JAR"
  else
    python3 - "$URL" "$JAR" <<'PY'
import sys
from urllib.request import urlopen
url, path = sys.argv[1], sys.argv[2]
with urlopen(url, timeout=60) as r:
    data = r.read()
with open(path, "wb") as f:
    f.write(data)
PY
  fi
fi
exec java -jar "$JAR" "$@"
