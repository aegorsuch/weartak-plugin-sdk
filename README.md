# WearTAK Plugin SDK

Standalone Android library for the WearTAK plugin contracts, AIDL interfaces, parcelables, and validation helpers. The source is the `weartak-plugin-sdk` module from `PluginTemplate/`.

## Requirements

- JDK 17
- Android SDK with API 34

Open this directory as the project root in Android Studio, or build from the command line:

```bash
./gradlew assembleDebug assembleRelease
./gradlew testDebugUnitTest testReleaseUnitTest
```

The generated AARs are in `build/outputs/aar/`. The WearTAK plugin template consumes `weartak-plugin-sdk-debug.aar` and `weartak-plugin-sdk-release.aar` from its `app/libs/` directory. Copy the matching build artifact there when updating that template.

## Signing

An Android library cannot use the plugin template's APK `signingConfigs`. The optional `signDebugAar` and `signReleaseAar` tasks use the same keystore values to JAR-sign the outer AAR. They create `*-signed.aar` beside the standard AAR; ordinary builds do not need signing credentials.

Set these environment variables for CI or local builds:

| Debug | Release |
| --- | --- |
| `WEARTAK_DEBUG_KEYSTORE_FILE` | `WEARTAK_RELEASE_KEYSTORE_FILE` |
| `WEARTAK_DEBUG_STORE_PASSWORD` | `WEARTAK_RELEASE_STORE_PASSWORD` |
| `WEARTAK_DEBUG_KEY_ALIAS` | `WEARTAK_RELEASE_KEY_ALIAS` |
| `WEARTAK_DEBUG_KEY_PASSWORD` | `WEARTAK_RELEASE_KEY_PASSWORD` |

Alternatively, put `DEBUG_*` and `RELEASE_*` keys with the same suffixes in a root-level `signing.properties` file, as in the plugin template. Environment variables take precedence. Keystore paths are relative to this project unless absolute. Keep `signing.properties` and keystores out of version control.

```bash
./gradlew signReleaseAar
jarsigner -verify build/outputs/aar/weartak-plugin-sdk-release-signed.aar
```

A JAR signature on an AAR can be verified as an archive signature. Android does not use it as an APK install signature; plugins still need the template's APK signing when packaged.
