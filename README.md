# WearTAK Plugin SDK

## Purpose and Capabilities

Standalone Android library for the WearTAK plugin contracts, AIDL interfaces, parcelables, and validation helpers. The source is the `weartak-plugin-sdk` module from `PluginTemplate/`.

## Status

The repository contains v1/v2/v3 contracts and optional Binder extensions.
Forge CI is configured for unit tests, lint, and AAR assembly; this is not a
claim that a particular revision has passed CI or is approved for deployment.
GitHub rolling-release publishing is disabled, and no downloadable SDK release
endpoint is configured. Consumers must build a pinned canonical source revision.
See [Forge CI and rolling-release status](#forge-ci-and-rolling-release-status)
for publishing prerequisites and limitations.

## Point of Contacts

Maintainer and support contacts are not documented in this repository.

## Ports Required

No required network ports are documented for the SDK library itself. Its plugin
contracts use Android Binder/AIDL. Host and plugin transport requirements must
be established for each deployment; the SDK does not define those network ports.
Build dependency repositories and credential routing are documented under
[Reproducible consumer CI](#reproducible-consumer-ci).

## Equipment Required

Building requires a development machine with JDK 17 and Android SDK API 34.
Android Studio is optional; the Gradle wrapper supports command-line builds.
Running consumers requires a compatible WearTAK host and plugin environment;
specific required hardware is not documented here.

## Equipment Supported

The Android library declares minimum SDK API 30 and compile SDK API 34 in
`build.gradle.kts`. These settings do not establish device certification or
host compatibility. Supported watch models and a tested hardware matrix are not
documented. Consumers must honor the contract-version and capability negotiation
requirements below.

## Compilation

### Requirements

- JDK 17
- Android SDK with API 34

Open this directory as the project root in Android Studio, or build from the command line:

```bash
./gradlew assembleDebug assembleRelease
./gradlew testDebugUnitTest testReleaseUnitTest
```

The generated AARs are in `build/outputs/aar/`. The WearTAK plugin template consumes `weartak-plugin-sdk-debug.aar` and `weartak-plugin-sdk-release.aar` from its `app/libs/` directory. Copy the matching build artifact there when updating that template.

## Developer Notes

### Contract versions

- v1: base lifecycle, settings, UI, and host control.
- v2: plugin-to-host event families.
- v3: capability-negotiated host-to-plugin PCM streaming using bounded Binder batches.
- Optional v1 PKCS#12 staging capability on the v3 host control; versioned independently.
- Optional v1 typed server package capability (receive + host-owned export) on the v3 host control.
- Optional extensions negotiated through `PluginInitParcel.hostExtras`: MOPP/PPE control v1 and plugin route status v1.

The v3 initializer is appended to the base AIDL surface so existing v1/v2 Binder transaction IDs remain unchanged. PCM subscriptions are idempotent, carry the original host chunk sequence and timestamps, and use the fixed WearTAK capture format advertised by `HostCapabilitiesV3Parcel`.

The v3 sources match upstream `WEARTAK-261` commit
`8f08374b69ed0dd0cfbffc22487a2e016a9abf72`. Consumers requiring v3 must not use
older SDK main-branch artifacts that omit this API.

### Optional PKCS#12 staging v1

`IWearTakPluginHostControlV3.getPkcs12ImportCapability()` advertises key
`com.weartak.plugin.sdk.pkcs12-stage`, API version `1`, payload limit (at most
32,700 bytes), stage lifetime (at most five minutes), and an endpoint obtained
with `getPkcs12ImportControlV1()`. Missing/unknown capability or old-host
transaction is unavailable; callers must not attempt the extension without an
exact supported key and version.

`IWearTakPkcs12ImportControlV1` exposes `stagePkcs12`, `requestReview`,
`getStatus`, and `cancel`. Stage accepts only PKCS#12 bytes plus a per-attempt
request ID; it never accepts a password or destination. `STAGED` confirms host
admission only. Only a separate wearer action may call `requestReview`; the host
owns password entry, certificate/identity/destination inspection, and final
explicit trust/save confirmation. There is deliberately no plugin apply method.
Status distinguishes `STAGED`, `REVIEW_PENDING`, `IN_REVIEW`, `REJECTED`,
`CANCELLED`, `EXPIRED`, and `APPLIED`; receipt or review request is never proof
that trust was applied.

All Binder methods are synchronous and have no callback thread. Call them off
the UI thread; `requestReview` must enqueue/launch host-owned UI and return
without waiting for wearer input. Host validates UID/package and active
authenticated session on every call, binds opaque one-use handles to that
origin, enforces replay/expiry checks, and wipes staged bytes/password on every
terminal state or caller/session death. Neither status nor logs may expose
credentials, private keys, or imported material. SDK `stage` wipes the supplied
byte array only after a structurally valid `STAGED` receipt; rejection and
transport exceptions preserve it. A transport exception leaves stage outcome unknown, so callers
must not automatically retry with a new request ID. Handles are opaque 128-bit
base64url tokens (22 characters); expiry uses device monotonic
`SystemClock.elapsedRealtime()` milliseconds. Calls are synchronous Binder
transactions: they have no SDK timeout or callback. Binder death surfaces as
`RemoteException`; callers must treat timeout/transport failure as an unknown
stage outcome, and use the known handle to query or cancel after a failed review
request.

### Optional server package v1

`IWearTakPluginHostControlV3.getServerPackageCapability()` advertises key
`com.weartak.plugin.sdk.server-package-stage`, API version `1`,
`maxPackageBytes` (at most 32,700, one Nearby frame), stage/export lifetimes
(at most five minutes), and `supportsReceive`/`supportsExport`; the endpoint
comes from `getServerPackageControlV1()`. Absent, unknown, or old-host results
are unsupported.

`ServerPackageCodec` (`encode`, `decode`, `validate`) is the single canonical
`ServerPackageV1` format used by plugin and host: `"WTSP"`, version byte `1`,
then `u8 tag | u16 big-endian length | value` records. Tags 1..7 are required
once, in order: name (UTF-8 1..128 bytes, no controls/format chars or outer
whitespace), host (lowercase DNS name, IPv4 or bracketless IPv6 literal, at most
253 chars; no scheme/port/path/userinfo/query), CoT port (u16 1..65535,
typically 8089), protocol (u8 `1`=TLS only; no TCP/plaintext downgrade), trust
mode (`1` public CA, no certificates; `2` private CA), identity mode (`1`
receiver enrollment with a fresh key on fixed port 8446, `2` admin PKCS#12 staged
separately through PKCS#12 v1, `3` shared event identity), and sensitive flags
(bit 0 client identity, bit 1 shared credentials). Tag 8 holds 0..4 distinct
DER X.509 CA certificates (each at most 8 KiB, basic constraints CA). Optional
tags then follow, strictly ascending: 9 client PKCS#12, 10 its password, 11
username, 12 password. Flags must exactly match present pairs; identity `3`
requires bit 0, other modes forbid it. Unknown/duplicate/out-of-order tags,
trailing data, settings over 1 KiB, and totals over 32,700 bytes are rejected.
`ServerPackage` byte fields are owner-held and wiped by `wipe()`.

Sender export: an explicit foreground tap calls `requestExport(requestId)`;
`PENDING` only means the host picker is open. The host chooses one configured
server and shows a separate opt-in for each sensitive item. It refuses
non-exportable, managed, or oversized material, and it never exports device UID,
callsign, or device-specific settings. `getExportStatus` reports `PENDING`,
`READY`, `TAKEN`, `CANCELLED`, `EXPIRED`, or `REJECTED` with
`ServerPackageExportReason`. `takeExport` releases bytes once. `cancelExport`
wipes them.

Receiver: `stageServerPackage` shares the PKCS#12 v1 stage, review, status, and
cancel lifecycle. `APPLIED` means the host saved the configuration after
explicit wearer confirmation. `ENROLLED`, `CONNECTED`, and `NETWORK_FAILED`
report later network outcomes separately. There is no plugin apply method.
Sensitive fields have no independent app-layer encryption in v1: they rely on the
Nearby Connections UKEY2-encrypted connection plus mutual code confirmation and
receiver acceptance, with memory-only handling. Never log, persist, or display
secret values.

### Hosted main UI actions

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
| `actionPresentation` | String | Optional; `TEXT` (default) or `ICON` |
| `actionIcon` | String | `NOTE`, `BLOODHOUND`, `STOP`, `TARGET`, or `BACK`; required for `ICON`, forbidden for `TEXT` |
| `actionPlacement` | String | Optional; `BODY` (default) or `HEADER_END` |
| `actionGroup` | String | Optional nonblank string, at most 128 UTF-8 bytes; only allowed for `BODY` `ICON` actions |
| `showTitle` | Boolean | Optional; defaults to `true`; must be `false` for `ICON` |

Unknown keys, wrong types (including explicit nulls), unknown/case-mismatched
enum values, and invalid combinations are rejected. `TEXT` requires `BODY`
placement without an icon or group; it may suppress its title. `HEADER_END`
requires `ICON`, `BACK`, `showTitle=false`, and no group. At most one
`HEADER_END` action is allowed per schema, even if disabled. Titles remain
mandatory semantic text when hidden; `actionLabel` remains mandatory and is the
accessibility label for icon actions.

Hosts render their own NOTE/BLOODHOUND/STOP/TARGET/BACK assets, not plugin-provided
images. `BLOODHOUND` means the WearTAK Bloodhound icon, not dog paws. `STOP`
means the WearTAK stop icon for an active incident Bloodhound action; only
`BODY` `ICON` placement is allowed. Operational state and stopping remain
host-owned; the icon adds no state or dispatch behavior. Body icons
with the same nonblank group share an icon-only action row; group identity is
scoped to the schema. `HEADER_END` places Back at the top-right of the page,
not in the body. Hidden titles must not create a title row or text button.
Hosts advertise Int `1` (`MainUiActionContract.PRESENTATION_VERSION`) in
`PluginInitParcel.hostExtras` under
`MainUiActionContract.HOST_PRESENTATION_VERSION_KEY`
(`"com.weartak.hostedUi.actionPresentationVersion"`). Plugins must verify an
actual Int of at least `1` before emitting presentation metadata. Missing,
wrong-typed, or older values require legacy `createContent` output: exactly
`actionId`, `actionLabel`, `enabled`, and `actionArgsJson`. This handshake uses
the existing Bundle; it does not alter AIDL/parcel layouts.

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

The original `createContent(actionId, actionLabel, enabled, args)` API,
including its Kotlin default-argument JVM entry point, is unchanged.
Use the new helper for presentation:

```kotlin
val bloodhound = MainUiCardParcel(
    cardType = MainUiCardTypes.ACTION,
    cardId = "incident-bloodhound",
    title = "Bloodhound",
    content = MainUiActionContract.createPresentedContent(
        actionId = "takcad.bloodhound",
        actionLabel = "Bloodhound incident",
        actionPresentation = MainUiActionContract.ICON,
        actionIcon = MainUiActionContract.BLOODHOUND,
        actionGroup = "incident-actions",
        showTitle = false,
        args = mapOf("incidentId" to incidentId),
    ),
)
val backContent = MainUiActionContract.createPresentedContent(
    actionId = "takcad.back",
    actionLabel = "Back",
    actionPresentation = MainUiActionContract.ICON,
    actionIcon = MainUiActionContract.BACK,
    actionPlacement = MainUiActionContract.HEADER_END,
    showTitle = false,
)
```

`MainUiActionContract.parseContent(content)` returns a typed `Action` through
`ContractValidationResult`; invalid shapes produce explicit issues. Hosts must
render text or icon presentation, honor title visibility and `enabled`, and
only on an explicit enabled-button tap
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

Both Gradle plugin and library dependency resolution prefer
`https://artifacts.tak.gov/artifactory/maven-release` when a complete read-only
credential pair is available. Username and password are chosen independently
from the first nonblank environment value in these orders:

| Username | Password |
| --- | --- |
| `ARTIFACTORY_CONSUME_USER` | `ARTIFACTORY_CONSUME_PASSWORD` |
| `ARTIFACTORY_USER` | `ARTIFACTORY_PASSWORD` |
| `TAK_USERNAME` | `TAK_PASSWORD` |

Missing/blank values are skipped; partially configured credentials do not
enable the private repository. Google, Maven Central, and the Gradle plugin
portal remain public fallbacks in their existing scopes. Configuration logs
only credential-presence and routing booleans, never values. Forge CI includes
`devsecops/gitlab-ci-common`'s `/template/artifactory_token_service.yml` and
uses `.artifactory_service_create_token_bash` for its root JDK image. Each Gradle
job exchanges its job identity for a runtime Artifactory token before setup,
then supplies `ARTIFACTORY_USER` and `ARTIFACTORY_PASSWORD` to the build. No
launcher repository init script is installed: that would conflict with
`FAIL_ON_PROJECT_REPOS`. This repository does not create static secrets,
variables, or relax credential protections. If token exchange requires a user
login, sign into `https://artifacts.tak.gov` once as the pipeline user and retry.
Robolectric test JVMs use the same paired repository configuration because
their Android-jar downloads bypass Gradle's dependency repositories.
Without that pair, public resolution remains subject to upstream rate limits.

Run `bash gradle/tests/repository-routing.sh` to exercise the actual settings
offline against eight synthetic credential cases, including alias precedence,
whitespace, partial pairs and mixed aliases in both scopes. The test substitutes
an empty root build without Android/dependency resolution; no private service
is contacted and no real credential is used. Forge CI runs this matrix before
the Android verification tasks.

This repository does not configure Maven publication or a release artifact
download endpoint. A consumer using downloaded AARs instead must supply a real
published artifact URL and verified SHA-256 for each variant. Uncommitted SDK
changes cannot be acquired from a pinned remote revision.

### Signing

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

### Forge CI and rolling-release status

`.gitlab-ci.yml` runs on branch pushes, merge requests, and manually started
branch pipelines. Push pipelines are suppressed when an open merge request
already supplies its pipeline. Each pipeline uses JDK 17, checksum-verified
Android command-line tools, API 34, and Build Tools 34.0.0 to run both variants'
unit tests, lint, and AAR assembly. Missing or empty AAR outputs fail the job.
JUnit, test HTML, and lint reports are retained for 14 days, including on failure.
Unsigned AARs are built for validation only and are not uploaded as job artifacts.
No signing credentials are required, and Maven/dependency publication is unchanged.

Forge project `weartak-plugins/weartak-plugin-sdk` has CI and shared runners
enabled, with the default CI configuration path. The job explicitly targets the
online `linux-canary` x86-64 Linux runner; the untagged Linux runner reported
ARM64 during inspection. Keep this tag aligned with an available x86-64 Linux
container runner. Android host build tools do not support ARM64 Linux.

**GitHub rolling-release publishing is disabled.** Inspection found no
project-level CI variables. The existing `signReleaseAar` task signs but does
not verify signer identity or reject partially signed archives. Plain
`jarsigner -verify` alone is not a trusted-signer gate, and signing support does
not prove that release credentials or inherited variables are correctly configured.

Before enabling a publisher, an authorized maintainer must:

- Supply the four existing `WEARTAK_RELEASE_*` values through protected Forge
  variables, with the keystore supplied as a file variable. Keep passwords masked
  and restrict credentials to protected `main` pipelines, never merge requests.
- Establish an independently confirmed release certificate SHA-256 fingerprint
  or truststore and implement fail-closed archive verification: verify every
  payload entry, reject unsigned/tampered entries, require the expected signer,
  and define certificate validity/trust policy. Add positive and negative tests
  covering unsigned, tampered, partially signed, and wrong-signer AARs.
- Provide a protected, masked GitHub credential restricted to contents/releases
  write access on **`aegorsuch/weartak-plugin-sdk` only**. No company repository
  may be targeted. No credentials or Forge variables were created by this change.
- Choose and document the fixed rolling-release tag. Gate publishing on a
  successful canonical Forge protected-`main` pipeline, serialize updates, and
  reject stale pipelines so older builds cannot replace newer release assets.
- Build `signReleaseAar` only after successful tests/lint, verify that exact
  `weartak-plugin-sdk-release-signed.aar`, and transfer only that verified AAR
  plus its SHA-256 and Forge source/pipeline provenance. Never rebuild or select
  artifacts via broad globs in the upload job. Update the personal mirror/tag
  to the corresponding source commit before updating its fixed release.

Until these prerequisites are established, there is no release job, GitHub
write operation, or downloadable SDK release endpoint. Consumers must continue
using the pinned-source build procedure above.

### Optional MOPP/PPE control v1

`com.weartak.plugin.sdk.mopp.aidl.IMoppPpeControlV1` is an independent,
host-implemented synchronous Binder extension. No existing AIDL method,
transaction order, API version, capability parcel, or parcel layout changes.
An implementing host puts Boolean `true` under
`MoppPpeContract.EXTRA_CAPABILITY` (`"moppPpeV1"`) and the control's Binder under
`MoppPpeContract.EXTRA_CONTROL` (`"moppPpeControlV1"`) in the existing
`PluginInitParcel.hostExtras`. Both are required. Older hosts/plugins omit or
ignore these Bundle entries; clients must treat missing support, dead Binder,
and incompatible responses as unavailable, never as a successful default state.

The plugin owns its native screen and immediate-save buttons. The host owns
durable state and all CoT/Ditto/Companion transport. The host must authorize the
calling UID against the currently active compatible plugin binding on **every**
call, including reads; possessing an old Binder is not authorization. Revoke
access when the binding ends. An absent/incompatible plugin stops operational
reporting without deleting saved selection, timestamp, or enable preference.

| Call | Meaning |
| --- | --- |
| `read()` | Return current authoritative persisted snapshot, without mutation. |
| `save(expectedRevision, mutationId, selection)` | Persist explicit posture tap and host-assigned timestamp, then acknowledge. |
| `setEnabled(expectedRevision, mutationId, enabled)` | Persist enable preference without changing saved posture/time. |

`MoppPpeStateParcel` contains:

| Field | Semantics |
| --- | --- |
| `revision: Long` | Nonnegative host-owned durable revision, shared by save and enable writes. |
| `selection: String?` | Null means never saved; allowed saved values are `"none"`, `"0"` through `"4"`. Explicit `"none"` is a real saved posture, not missing state. |
| `savedAtEpochMillis: Long?` | UTC epoch milliseconds; null means never saved or legacy saved time unknown. Preserve legacy unknown time until an explicit save. |
| `enabled: Boolean` | Retained user enable preference, not effective reporting availability. |
| `contributing: Boolean` | Effective operational reporting availability, computed by host from compatibility, active binding, saved posture, and enable state. Not a client-writable setting. |
| `lastMutationId: String?` | Last durably acknowledged mutation identity; null for legacy/unmodified state. |

`MoppPpeResultParcel(accepted, code, state)` uses `MoppPpeResultCode`:
`OK=0`, `REVISION_CONFLICT=1`, `INVALID_REQUEST=2`, `UNAUTHORIZED=3`,
`INACTIVE_SESSION=4`, `PERSISTENCE_FAILED=5`. Success requires `accepted=true`,
`code=OK`, and a nonnull authoritative snapshot. Failure requires
`accepted=false`; a conflict can include current state, but denial must not
disclose it. Unknown codes or contradictory result shapes are protocol errors.
Binder/transport exceptions are errors, never acknowledgements.

Host implementation obligations:

- Validate nonnegative expected revision, nonblank mutation identity and allowed
  posture before writes. Bound untrusted strings before persistence. Reject
  malformed requests explicitly; null AIDL inputs are invalid.
- Serialize save and enable operations against the same revision. Compare and
  persist atomically; increment revision only on a successful new mutation.
  Return `REVISION_CONFLICT` for stale writes. Do not wrap revision on overflow.
- Persist selection/time/enable/revision/mutation identity as one transaction
  before returning `OK`. Storage failure returns `PERSISTENCE_FAILED` without
  reporting an uncommitted state or dispatching it operationally.
- Assign time only on an explicit successful save, including a same-posture tap.
  Reads, reconnects, initialization, enable changes, and failure paths must not
  invent or refresh timestamps.
- Recognize an identical repeat of the last successful mutation before CAS so
  it cannot increment revision, refresh time, or repeat operational side effects.
  Reusing that identity for a different operation/payload is `INVALID_REQUEST`.
  Reconcile a lost acknowledgement by reading `lastMutationId`; clients must not
  automatically replay writes after disconnect/conflict. Refresh first and
  require a new explicit user action with a fresh identity.

SDK tests exercise generated remote-proxy transactions, typed parcel roundtrips,
nullable/legacy states, every failure code, disconnected transport, and extension
discovery through unchanged initialization. Host integration tests must establish
authorization, durable atomicity, conflict/idempotency and transport gating;
these cannot be enforced by parcel definitions alone.

Consumers must pin a merged canonical Forge source commit and run the existing
source-build CI path for **both** variants. Record source SHA, successful Forge
pipeline, task invocation and SHA-256 for each local AAR input. Local hashes
identify those exact files, not a promise of byte-identical cross-platform
builds. This extension does not enable unsigned AAR job downloads or releases.

### Optional plugin route status v1

`com.weartak.plugin.sdk.routestatus.aidl.IPluginRouteStatusControlV1` lets a
plugin that provides a network route/transport (for example a Meshtastic
radio link) push live connection status to the host, for display in Network
Preferences and the home connection-status indicator. It is an independent,
host-implemented synchronous Binder extension: no existing AIDL method,
transaction order, API version, capability parcel, or parcel layout changes.

An implementing host puts Boolean `true` under
`RouteStatusContract.EXTRA_CAPABILITY` (`"routeStatusV1"`) and the control's
Binder under `RouteStatusContract.EXTRA_CONTROL` (`"routeStatusControlV1"`) in
the existing `PluginInitParcel.hostExtras`. Both are required. Older hosts omit
them; older plugins ignore them.

| Call | Meaning |
| --- | --- |
| `getContractVersion()` | Returns `RouteStatusContract.CONTRACT_VERSION` (`1`). |
| `reportRouteStatus(status)` | Upsert one route keyed by authorized calling plugin plus `routeId`. |
| `removeRoute(routeId)` | Remove one route the plugin no longer provides. |
| `clearRoutes()` | Remove every route reported by the calling plugin. |

`PluginRouteStatusParcel` fields: `routeId` (stable, `[A-Za-z0-9._-]{1,64}`),
`label` (nonblank display name, max 64 UTF-8 bytes), `state`
(`RouteConnectionState`: `UNKNOWN=0`, `DISCONNECTED=1`, `CONNECTING=2`,
`CONNECTED=3`, `ERROR=4`), optional `statusText` (max 128 bytes), `sequence`
(monotonic per binding) and `changedAtEpochMillis`. Plugins never send their
plugin id; the host derives it from the calling UID and active binding.

Results use `RouteStatusResultCode`: `OK=0`, `INVALID_REQUEST=1`,
`UNAUTHORIZED=2`, `INACTIVE_SESSION=3`, `LIMIT_EXCEEDED=4`,
`STALE_SEQUENCE=5`, `UNKNOWN_ROUTE=6`. `UNAVAILABLE=-1` is client-only for a
missing capability, dead Binder, or transport failure and is never an
acknowledgement.

Plugins use `PluginRouteStatusReporter.fromInit(init)`; `null` means the host
does not support route status. The reporter assigns sequence/time, validates
locally, and maps transport failures to `UNAVAILABLE`. After every
(re)initialization the plugin must report all its current routes.

Host implementation obligations:

- Authorize the calling UID against the currently active plugin binding on
  every call; prefer one control instance per binding.
- Validate with `RouteStatusValidator` and store the sanitized value. Display
  unrecognized future state values as `UNKNOWN`, never as connected.
- Drop updates whose `sequence` is not newer than the stored route
  (`STALE_SEQUENCE`). Enforce `MAX_ROUTES_PER_PLUGIN` (8).
- Start each binding with no routes for that plugin, and remove its routes when
  the binding ends, the Binder dies, or the plugin is disabled. Never retain a
  stale `CONNECTED` state.
- Do not block Binder threads; hand updates to host state observers.

## License

The user's original code in this repository is licensed under the GNU General
Public License, version 3 or (at your option) any later version
(`GPL-3.0-or-later`). See [LICENSE](LICENSE) for the complete GPL version 3 text.

For Covered Work as defined in the
[additional permission](LICENSE-ADDITIONAL-PERMISSION.md), the copyright holder
grants the narrow GPL version 3, section 7 permission stated there for the
identified Google Play services components. This permission does not apply to
material for which the copyright holder lacks authority.

This declaration covers only the user's original code, not third-party
components, which retain their own licenses and copyright notices. In
particular, the Gradle wrapper launchers (`gradlew` and `gradlew.bat`) retain
their Apache License 2.0 notices. Bundled Gradle tooling and external
dependencies remain subject to their respective upstream terms. Existing
attributions and rights notices are preserved.
