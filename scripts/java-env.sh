#!/usr/bin/env bash
set -euo pipefail

if [[ -n "${JAVA_HOME:-}" && -x "${JAVA_HOME}/bin/java" ]]; then
  export JAVA_HOME
  export PATH="${JAVA_HOME}/bin:${PATH}"
  return
fi

for candidate in "/usr/opt/android-studio/jbr" "/opt/android-studio/jbr" "$HOME/.jdks"/*; do
  if [[ -x "${candidate}/bin/java" ]]; then
    export JAVA_HOME="${candidate}"
    export PATH="${JAVA_HOME}/bin:${PATH}"
    return
  fi
done

echo "JDK not found. Install a JDK or set JAVA_HOME." >&2
exit 1
