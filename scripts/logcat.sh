#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=android-tools.sh
source "${SCRIPT_DIR}/android-tools.sh"

"$(android_tool adb)" logcat -v time | grep --line-buffered -E "Auralis|AndroidRuntime|WorkManager|ONNX|PdfBox"
