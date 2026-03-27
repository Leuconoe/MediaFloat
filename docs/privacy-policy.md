# MediaFloat privacy policy

Last updated: 2026-03-27

MediaFloat is a floating Android media-controls app. This app does not collect, store, share, or sell personal data.

## What MediaFloat does with data

- MediaFloat keeps its core behavior on-device.
- MediaFloat does not require account creation.
- MediaFloat does not send personal data to a developer server or cloud service.
- MediaFloat does not include analytics, advertising SDKs, or personal-data sales flows described in this repository.

## Permissions and device access

MediaFloat uses Android permissions and system access only to provide the floating media-control overlay feature described in this repository:

- `SYSTEM_ALERT_WINDOW` to show the floating controls above other apps
- `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_SPECIAL_USE` to keep the overlay runtime visible and recoverable through a foreground notification
- `POST_NOTIFICATIONS` to show the required foreground-service notification
- Notification listener access to observe active media-session state and supported transport actions so the overlay can control playback

These permissions are used for app functionality on the device. They are not used to build user profiles, track users across apps, or sell personal data.

## Data sharing

MediaFloat does not share personal data with third parties because the app does not collect or store personal data as part of its documented core behavior.

## Contact

For privacy questions or requests about MediaFloat, contact `sw2.io@sw2.io`.

## Future publication note

For Google Play distribution, this policy should also be published as a public, non-editable web page that matches this source document.
