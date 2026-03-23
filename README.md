<p align="center">
  <img src="docs/assets/mediafloat-mark.svg" width="120" alt="MediaFloat icon" />
</p>

<h1 align="center">MediaFloat</h1>

<p align="center">
  Floating media controls for Android.
</p>

<p align="center">
  A compact overlay bar that keeps previous, play/pause, and next within reach while you stay in other apps.
</p>

<p align="center">
  <strong>v0.1.0</strong> · <strong>Android 10+</strong> · <strong>Kotlin</strong> · <strong>Jetpack Compose</strong>
</p>

## Why MediaFloat

MediaFloat focuses on one job: giving Android media controls a small, movable surface that stays available above other apps. The app keeps the scope intentionally tight so setup, behavior, and debugging stay understandable.

## What it ships with

- A floating overlay bar for `Previous`, `Play / pause`, and `Next`
- A right-side drag handle and saved overlay position
- A foreground-service runtime with readiness checks for overlay and notification access
- A Settings surface with supported button layouts, size presets, and a live preview
- A Debug console with runtime state, media state, transport controls, and recent logs
- A Support surface with setup help, version details, product constraints, and lightweight notices
- An exported automation action for launching the overlay flow from routines or shortcuts

## Quick start

1. Clone the repository.
2. Open it in Android Studio.
3. Let Gradle sync.
4. Run the `app` configuration on an emulator or Android device.

Command line install:

```bash
./gradlew installDebug
```

Windows:

```bat
gradlew.bat installDebug
```

## First-run setup

MediaFloat depends on Android system capabilities before the overlay can remain active:

1. Open `MediaFloat`.
2. Grant overlay access, also known as display over other apps.
3. Grant notification-listener access so MediaFloat can observe active media sessions.
4. Allow app notifications, especially on Android 13+, so the foreground-service notification stays visible.
5. Start playback in a media app.
6. Use `Settings` or `Debug` to start the overlay.

If readiness is blocked, the app exposes shortcuts back to the relevant system settings screens.

## Permissions

| Permission or access | Why MediaFloat needs it |
| --- | --- |
| `SYSTEM_ALERT_WINDOW` | Shows the floating control bar above other apps |
| `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_SPECIAL_USE` | Keeps the overlay runtime visible and recoverable |
| `POST_NOTIFICATIONS` | Shows the required foreground-service notification |
| Notification listener access | Reads active media-session state and supported transport actions |

## App surfaces

- `Settings` - adjust visible buttons, choose a size preset, review the live preview, and start the overlay
- `Debug` - inspect runtime readiness, inspect media readiness, send transport commands, start or stop the overlay, and clear recent logs
- `Support` - review setup help, version details, current constraints, and lightweight notices

## Automation hook

MediaFloat includes an exported action for launching the overlay flow from automation tools:

```text
com.mediacontrol.floatingwidget.action.SHOW_OVERLAY
```

If readiness is blocked, the app falls back to the main interface so the missing access can be fixed.

## Release signing

This repository includes `keystore.properties.example` as the expected release-signing shape.

To wire a signed local release build:

1. Copy `keystore.properties.example` to `keystore.properties`.
2. Generate or place your release keystore at the configured path.
3. Update the store password, key alias, and key password values.
4. Run `gradlew.bat assembleRelease` on Windows or `./gradlew assembleRelease` elsewhere.

`keystore.properties` and local keystore files are ignored by Git.

## Current status

MediaFloat `v0.1.0` is a focused first release.

- The overlay supports one horizontal control family
- Freeform resizing is not supported; use the built-in size presets
- The drag handle is fixed to the right side
- Button combinations are limited to supported previous, play/pause, and next layouts
- Overlay behavior depends on Android permission state and whether an active media session is available
- Emulator validation is useful for setup and UI checks, but target-device validation still matters before a public release

## Changelog

Release notes for the first release live in `CHANGELOG.md` and `docs/releases/v0.1.0.md`.

## License

This repository does not currently include a top-level `LICENSE` file.

The app itself includes a concise notice summary for the main third-party libraries used in the shipped Android build, including AndroidX, Compose Material 3, Google Material Components, and Kotlin standard library components.
