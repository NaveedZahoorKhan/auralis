#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
# shellcheck source=android-tools.sh
source "${SCRIPT_DIR}/android-tools.sh"
# shellcheck source=java-env.sh
source "${SCRIPT_DIR}/java-env.sh"

ADB="$(android_tool adb)"
"${ROOT_DIR}/gradlew" -p "${ROOT_DIR}" :app:assembleDebug
"${ADB}" install -r "${ROOT_DIR}/app/build/outputs/apk/debug/app-debug.apk"
"${ADB}" shell monkey -p com.auralis.reader 1
