package com.weartak.plugin.sdk

/**
 * Canonical [com.weartak.plugin.sdk.model.SettingsFieldParcel.hostSettingKey] values and
 * [com.weartak.plugin.sdk.model.HostSettingsParcel] field semantics.
 */
object HostSettingsContract {
    const val KEY_CALLSIGN: String = "callsign"
    const val KEY_TEAM: String = "team"
    const val KEY_ROLE: String = "role"
    const val KEY_REPORTING_INTERVAL_SECS: String = "reportingIntervalSecs"
    const val KEY_DESIRED_REPORTING_INTERVAL_SECS: String = "desiredReportingIntervalSecs"
    const val KEY_TAK_CONNECTED: String = "takConnected"
    const val KEY_SITX_CONNECTED: String = "sitxConnected"
    const val KEY_COT_CONNECTED: String = "cotConnected"

    val KNOWN_HOST_SETTING_KEYS: Set<String> = setOf(
        KEY_CALLSIGN,
        KEY_TEAM,
        KEY_ROLE,
        KEY_REPORTING_INTERVAL_SECS,
        KEY_DESIRED_REPORTING_INTERVAL_SECS,
        KEY_TAK_CONNECTED,
        KEY_SITX_CONNECTED,
        KEY_COT_CONNECTED,
    )
}
