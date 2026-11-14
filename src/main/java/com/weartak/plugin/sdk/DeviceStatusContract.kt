package com.weartak.plugin.sdk

/**
 * Optional keys in [com.weartak.plugin.sdk.model.DeviceStatusParcel.telemetry].
 * Hosts populate only values that are known at query time; absent keys mean unknown/unavailable.
 */
object DeviceStatusContract {
    const val KEY_BATTERY_PERCENT: String = "batteryPercent"
    const val KEY_TAK_CONNECTED: String = "takConnected"
    const val KEY_SITX_CONNECTED: String = "sitxConnected"
    const val KEY_HEART_RATE_BPM: String = "heartRateBpm"
    const val KEY_LOCATION_AVAILABLE: String = "locationAvailable"
    const val KEY_LAT: String = "lat"
    const val KEY_LON: String = "lon"
    const val KEY_CALLSIGN: String = "callsign"
    /**
     * Optional host-provided TAK UID, distinct from Android's Binder UID.
     * A missing key means identity is unsupported or unavailable. Callsign may be blank or editable
     * and is not a stable identity. TAK UID is scoped to installation, user, and signing context;
     * callers must not assume durability, exactly-once behavior, or a permanent identity.
     */
    const val KEY_TAK_UID: String = "takUid"
    const val KEY_SNAPSHOT_TIME_MS: String = "snapshotTimeMs"
}
