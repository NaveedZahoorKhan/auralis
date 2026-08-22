#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=android-tools.sh
source "${SCRIPT_DIR}/android-tools.sh"

AVD_NAME="${1:-Medium_Phone}"
EMULATOR="$(android_tool emulator)"
ADB="$(android_tool adb)"

if "${ADB}" devices | grep -q "emulator-.*device"; then
  echo "An emulator is already running."
  exit 0
fi

"${EMULATOR}" -avd "${AVD_NAME}" -netdelay none -netspeed full >/tmp/auralis-emulator.log 2>&1 &
echo "Starting AVD ${AVD_NAME}. Logs: /tmp/auralis-emulator.log"
"${ADB}" wait-for-device
"${ADB}" shell getprop sys.boot_completed | grep -m 1 "1" >/dev/null || {
  until [[ "$("${ADB}" shell getprop sys.boot_completed | tr -d '\r')" == "1" ]]; do
    sleep 2
  done
}
echo "AVD ${AVD_NAME} is ready."
