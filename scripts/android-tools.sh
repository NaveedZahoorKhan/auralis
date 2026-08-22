#!/usr/bin/env bash
set -euo pipefail

find_android_sdk() {
  if [[ -n "${ANDROID_HOME:-}" && -d "${ANDROID_HOME}" ]]; then
    printf '%s\n' "${ANDROID_HOME}"
    return
  fi
  if [[ -n "${ANDROID_SDK_ROOT:-}" && -d "${ANDROID_SDK_ROOT}" ]]; then
    printf '%s\n' "${ANDROID_SDK_ROOT}"
    return
  fi
  for candidate in "$HOME/Android/Sdk" "$HOME/Library/Android/sdk" "/opt/android-sdk"; do
    if [[ -d "${candidate}" ]]; then
      printf '%s\n' "${candidate}"
      return
    fi
  done
  echo "Android SDK not found. Set ANDROID_HOME or ANDROID_SDK_ROOT." >&2
  exit 1
}

android_tool() {
  local sdk tool
  sdk="$(find_android_sdk)"
  tool="$1"
  case "${tool}" in
    adb) printf '%s\n' "${sdk}/platform-tools/adb" ;;
    emulator) printf '%s\n' "${sdk}/emulator/emulator" ;;
    avdmanager) printf '%s\n' "${sdk}/cmdline-tools/latest/bin/avdmanager" ;;
    *) echo "Unknown Android tool: ${tool}" >&2; exit 1 ;;
  esac
}
