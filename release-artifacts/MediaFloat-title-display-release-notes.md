# MediaFloat Title Display Release Notes

## Highlights

- Added current media title rendering to the floating widget overlay.
- Kept the title strip locked to the full floating bar width for every widget size preset.
- Added marquee-style horizontal scrolling for long titles while preserving a single-line layout.

## Details

- Title metadata now prefers media `DISPLAY_TITLE` and falls back to `TITLE`.
- Compact, Standard, and Large presets now scale title spacing and text size with the widget.
- Runtime `WindowManager` overlay and in-app preview now show the same title-strip structure.

## Validation

- Passed `./gradlew.bat testDebugUnitTest`.
- Passed `./gradlew.bat compileDebugAndroidTestKotlin`.
- Passed `./gradlew.bat assembleDebug`.
- Passed `./gradlew.bat connectedDebugAndroidTest`.
- Installed the latest debug build on emulator with `./gradlew.bat installDebug`.
