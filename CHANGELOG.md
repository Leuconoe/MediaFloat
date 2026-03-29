# Changelog

All notable user-facing changes to MediaFloat are recorded here.

## v0.2.2

Title display release for the floating widget.

- Added current-media title rendering to the floating widget overlay
- Kept the title strip locked to the full widget width across Compact, Standard, and Large presets
- Added marquee-style scrolling for long titles while preserving a single-line title bar
- Updated release notes and store changelog metadata for the `v0.2.2` release flow

See also: `docs/releases/v0.2.2.md`

## v0.2.1

Focused follow-up release for shortcut convenience and distribution prep.

- Added a `Stop widget` launcher shortcut to pair with `Launch widget`
- Moved the app package/application id to `sw2.io.mediafloat`
- Hardened runtime entry paths and fixed overlay drag crashes
- Added reusable screenshots and listing metadata for Google Play, F-Droid, and IzzyOnDroid

See also: `docs/releases/v0.2.1.md`

## v0.2.0

First full product-shape release of MediaFloat.

- Added app-language support with `System default`, English, Korean, Chinese, Japanese, Spanish, and French
- Added Main, Settings, Advanced, Support, and hidden Debug surfaces with simplified control grouping
- Added real widget controls for size, width, opacity, sidebar side, and theme presets
- Added safer runtime entry handling for notification settings, overlay start, and launcher shortcut activation
- Added media-session recovery after temporary interruption paths
- Added Apache-2.0 licensing and refreshed release-facing project documentation

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
