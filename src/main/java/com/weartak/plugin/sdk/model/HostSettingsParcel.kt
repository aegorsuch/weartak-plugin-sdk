package com.weartak.plugin.sdk.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Host-owned settings snapshot pushed to plugins via
 * [com.weartak.plugin.sdk.aidl.IWearTakPlugin.onHostSettingsChanged].
 *
 * [revision] is monotonic per host process; plugins use it to detect stale snapshots.
 */
@Parcelize
data class HostSettingsParcel(
    val revision: Long,
    val hostApiVersion: Int,
    val hostPackageName: String,
    val snapshotTimeUtcMillis: Long,
    val callsign: String = "",
    val team: String = "",
    val role: String = "",
    val reportingIntervalSecs: Int = 0,
    val desiredReportingIntervalSecs: Int = 0,
    val takConnected: Boolean = false,
    val sitxConnected: Boolean = false,
    val cotConnected: Boolean = false,
) : Parcelable
