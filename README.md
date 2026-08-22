# Auralis

Auralis is a greenfield Android reader prototype for text PDFs and EPUBs with a local-first audiobook pipeline. It uses native Kotlin, Jetpack Compose, Room, WorkManager, PDFBox Android, Media3, and ONNX Runtime Mobile.

## Current MVP

- Imports EPUB and selectable-text PDF files through Android's document picker.
- Copies books into app-private storage and stores extracted chapters as text files.
- Persists books, chapters, metadata, reading state, notes, voice models, audiobook jobs, and audio segments in Room.
- Runs local heuristic book analysis now, with an on-device LLM runtime boundary in `core:ai`.
- Refuses to use Android system TTS for audiobook generation.
- Supports ONNX voice-pack installation records and a neural-only audio engine boundary for Kokoro-style voices.
- Schedules audiobook generation through WorkManager and records `waiting_for_voice` when no natural voice is installed.

## Build

This shell does not currently expose Java on `PATH`, so use Android Studio's bundled JDK:

```bash
JAVA_HOME=/usr/opt/android-studio/jbr PATH=/usr/opt/android-studio/jbr/bin:$PATH ./gradlew :app:assembleDebug
```

The debug build filters native libraries to `x86_64` by default so it installs cleanly on the local `Medium_Phone` AVD. Build for an ARM64 phone with:

```bash
JAVA_HOME=/usr/opt/android-studio/jbr PATH=/usr/opt/android-studio/jbr/bin:$PATH ./gradlew :app:assembleDebug -Pauralis.abiFilters=arm64-v8a
```

## AVD

```bash
./scripts/run-avd.sh Medium_Phone
./scripts/install-debug.sh
./scripts/logcat.sh
```

Verified locally on `Medium_Phone` (`emulator-5554`) with `app-debug.apk` installed and launched.

## Next Engineering Step

The natural TTS runtime is intentionally not a fake speech generator. The remaining production work is the Kokoro/Sherpa ONNX adapter: tokenizer/phonemizer inputs, speaker embedding selection, waveform output, and voice manifest download URLs.
# auralis
