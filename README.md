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

## Contract versions

- v1: base lifecycle, settings, UI, and host control.
- v2: plugin-to-host event families.
- v3: capability-negotiated host-to-plugin PCM streaming using bounded Binder batches.

The v3 initializer is appended to the base AIDL surface so existing v1/v2 Binder transaction IDs remain unchanged. PCM subscriptions are idempotent, carry the original host chunk sequence and timestamps, and use the fixed WearTAK capture format advertised by `HostCapabilitiesV3Parcel`.

The v3 sources match upstream `WEARTAK-261` commit
`8f08374b69ed0dd0cfbffc22487a2e016a9abf72`. Consumers requiring v3 must not use
older SDK main-branch artifacts that omit this API.

## Hosted main UI actions

Plugins advertise `PluginCapabilitiesParcel.hostedMainUi` and return cards from
`IWearTakPlugin.getMainUiSchema()`. Keep feature UI inside its host plugin page;
the SDK has no menu-entry registration API. Host discovery and menu routing remain
host responsibilities.

`MainUiCardTypes.ACTION` (`"ACTION"`) adds a button to that page without changing
AIDL or parcel layouts. Original v1 hosts only guarantee `STATUS` and
`KEY_VALUE_LIST`; update the host renderer before publishing ACTION cards. There
is no dedicated ACTION capability negotiation flag. Strict schema validation in
this SDK accepts ACTION and validates its content even outside strict mode.

Each card needs a unique nonblank `cardId` and nonblank `title`. ACTION `content`
uses these exact keys:

| Key | Type | Requirement |
| --- | --- | --- |
| `actionId` | String | Required, nonblank, at most 128 UTF-8 bytes |
| `actionLabel` | String | Required, nonblank, at most 256 UTF-8 bytes |
| `enabled` | Boolean | Optional; defaults to `true` |
| `actionArgsJson` | String | Optional; empty/omitted means empty arguments; otherwise JSON object with string values only |

JSON text is limited to 2048 UTF-8 bytes. Decoded arguments follow Bundle limits:
at most 64 keys, keys at most 64 characters, string values at most 2048 UTF-8 bytes.
Null, numeric, Boolean, array, and nested-object argument values are invalid.
All cards retain the existing 16-card schema limit and content Bundle limits.

```kotlin
val refresh = MainUiCardParcel(
    cardType = MainUiCardTypes.ACTION,
    cardId = "refresh",
    title = "Connection",
    content = MainUiActionContract.createContent(
        actionId = "gotenna.refresh",
        actionLabel = "Refresh",
    ),
)
```

`MainUiActionContract.parseContent(content)` returns a typed `Action` through
`ContractValidationResult`; invalid shapes produce explicit issues. Hosts must
render `actionLabel`, honor `enabled`, and only on an explicit enabled-button tap
call `invokeMainUiAction(action.actionId, action.args)` on that plugin's Binder.
Rendering or refreshing schemas must never dispatch actions. No intents or
activity launches are part of this contract. Plugins must validate supported
action IDs and arguments themselves. IDs such as `gotenna.refresh` and
`takcad.refresh` are plugin-defined, not SDK-reserved.

Plugins notify `onPluginMainUiSchemaChanged()` when card declarations change and
`onPluginMainUiStateChanged()` when state changes. Existing STATUS/KEY_VALUE_LIST
content keys and state-to-card binding conventions are not specified by this
SDK; do not infer them from ACTION.

Secret text input is unsupported. Never put tokens or credentials in schema,
state, action arguments, logs, or host persistence. A secure input contract needs
separate design; ACTION does not provide one.

After SDK changes, rebuild both AAR variants and update matching consumers in the
host and each plugin/template; copying an AAR alone does not add renderer support.

### Reproducible consumer CI

The source repository is
`https://git.tak.gov/weartak-plugins/weartak-plugin-sdk.git`. Consumer CI should
check out an explicitly pinned SDK commit with its configured repository access,
then run these tasks in that checkout using JDK 17 and Android SDK API 34:

```powershell
.\gradlew.bat testDebugUnitTest testReleaseUnitTest assembleDebug assembleRelease
```

Set `ANDROID_HOME` to the installed SDK location. On non-Windows runners use
`./gradlew` with the same tasks. Copy the generated debug and release AARs into
the consumer's corresponding library inputs before building that consumer.
Fail explicitly when source access, the pinned revision, or either artifact is
unavailable; do not silently use older template AARs. Workspace-local AAR paths
are development inputs, not reproducible CI dependencies.

This repository does not configure Maven publication or a release artifact
download endpoint. A consumer using downloaded AARs instead must supply a real
published artifact URL and verified SHA-256 for each variant. Uncommitted SDK
changes cannot be acquired from a pinned remote revision.

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
