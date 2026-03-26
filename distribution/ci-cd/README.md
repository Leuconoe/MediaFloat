# MediaFloat CI/CD notes

## Workflows

- `.github/workflows/android-ci.yml`
  - runs on pushes to `main` and pull requests
  - executes `:app:testDebugUnitTest`
  - assembles the debug APK
  - uploads the debug APK as a workflow artifact

- `.github/workflows/android-release.yml`
  - runs on `v*` tags or manual dispatch
  - restores the signing keystore from GitHub Actions secrets
  - runs release checks
  - builds the signed release APK
  - publishes `MediaFloat-android-<tag>-signed.apk` to the GitHub Release

## Required GitHub secrets

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_STORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

## Expected tag format

- `v0.2.1`
- `v0.3.0`

The release workflow uses the Git tag as the filename version segment.
