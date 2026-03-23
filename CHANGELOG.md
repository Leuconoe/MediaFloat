# Changelog

All notable user-facing changes to MediaFloat are recorded here.

## v0.2.0

Final v0.2.0 feature-complete release of the current single-module MediaFloat app.

- Added app-language support with `System default`, English, Korean, Chinese, Japanese, Spanish, and French
- Added Android resource-backed shell text for the main shipped surfaces and runtime-facing user notices
- Added the Advanced app-language picker and current-language visibility in Support
- Added the AppCompat locale-selection path for Android 13+ and older supported versions
- Added Apache-2.0 licensing and refreshed release-facing project documentation for v0.2.0
- Kept the existing overlay, recovery, theme, width, persistence, automation, and hidden Debug flows intact

Notes:

- The overlay still targets a single horizontal control family
- Freeform resizing and freeform button placement are still not supported
- Emulator validation remains useful for setup and UI checks, but target-device validation is still recommended

See also: `docs/releases/v0.2.0.md`

## v0.1.0

Initial public repository release of MediaFloat.

- Added a floating media-control overlay for previous, play or pause, and next actions
- Added a settings surface with supported button-set controls, size presets, and a live preview
- Added a debug console for runtime readiness, media readiness, transport testing, and recent logs
- Added a support and about surface with setup guidance, version details, and current product constraints
- Added overlay, notification-listener, and notification access handling required for the persistent runtime
- Added MediaFloat launcher branding and release-facing project documentation

Notes:

- This release is intentionally focused on a single horizontal overlay family
- Emulator validation is useful for setup and UI checks, but target-device validation is still recommended

See also: `docs/releases/v0.1.0.md`
