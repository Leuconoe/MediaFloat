# Foreground service special-use review video script

Use this checklist to record the Play review video for MediaFloat's `FOREGROUND_SERVICE_SPECIAL_USE` declaration. Keep the recording short and show the full user-visible flow on a real device or emulator with active media playback.

## What the video must show

- The user starts the overlay themselves from the app or the launcher shortcut
- The foreground-service notification becomes visible when the overlay starts
- The floating controls remain visible above another app while media is playing
- The user can stop the overlay through a clear stop path

## Suggested recording flow

1. Start on the home screen and open `MediaFloat`, or show the `Launch widget` launcher shortcut.
2. In `MediaFloat`, show that the user taps the control that starts the overlay. If needed, briefly show that required permissions are already enabled.
3. After the overlay starts, show the foreground-service notification in the notification shade or status area.
4. Open another app with active media playback and show the MediaFloat overlay above that app.
5. Show the overlay controls in use for previous, play/pause, or next while playback is active.
6. Show a user-visible stop path, either from `MediaFloat`, the `Stop widget` launcher shortcut, or another in-app stop control that exists in the shipped build.
7. Confirm the overlay is gone after the user stops it.

## Recording notes

- Keep the video focused on MediaFloat's floating media-control behavior.
- Do not claim background behavior that is not shown on screen.
- Make sure the notification text is readable enough to prove the foreground service is active.
- Use the current shipped UI and naming, including `MediaFloat`, `Launch widget`, and `Stop widget`.
