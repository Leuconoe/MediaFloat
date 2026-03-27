# MediaFloat CI/CD notes

## Workflows

- `.github/workflows/android-ci.yml`
  - runs on pushes to `main` and pull requests
  - executes `:app:testDebugUnitTest`
  - assembles the debug APK
  - uploads the debug APK as a workflow artifact

- `.github/workflows/android-release.yml`
  - runs on `v*` tags or manual dispatch
  - requires a matching `docs/releases/<tag>.md` file for the GitHub Release body
  - restores the signing keystore from GitHub Actions secrets
  - runs release checks
  - builds the signed release APK and signed Android App Bundle
  - publishes `MediaFloat-android-<tag>-signed.apk` and `MediaFloat-android-<tag>-signed.aab` to the GitHub Release
  - uses the exact `docs/releases/<tag>.md` content as the GitHub Release notes body

## Required GitHub secrets

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_STORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

## Expected tag format

- `v0.2.1`
- `v0.3.0`

The release workflow uses the Git tag as the filename version segment.

## Release notes contract

- Every GitHub release tag must have a matching file at `docs/releases/<tag>.md`.
- Tag pushes read that file directly as the GitHub Release body.
- Manual dispatch must be given the same tag value and should be launched from the trusted workflow definition on `main`.
- Manual dispatch checks out the requested tag before building, so the release body and binaries both come from the tagged revision.
- Release tags must point to commits reachable from `main`.
- The workflow fails before the release build starts if the tag format is invalid or the matching release notes file is missing.
