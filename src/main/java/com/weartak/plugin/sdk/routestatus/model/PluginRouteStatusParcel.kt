package com.weartak.plugin.sdk.routestatus.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * One plugin-provided network route (e.g. a Meshtastic transport). The host keys routes by
 * the authorized calling plugin plus [routeId]; plugins never supply their plugin id.
 */
@Parcelize
data class PluginRouteStatusParcel(
    /** Stable per-plugin id matching [com.weartak.plugin.sdk.routestatus.RouteStatusContract.ROUTE_ID_PATTERN]. */
    val routeId: String,
    /** Short user-facing route name, e.g. "Meshtastic". */
    val label: String,
    /** A [com.weartak.plugin.sdk.routestatus.RouteConnectionState] value. */
    val state: Int,
    /** Optional short detail, e.g. "3 nodes" or "Radio not paired". */
    val statusText: String? = null,
    /** Monotonically increasing per route for the plugin process; hosts drop stale updates. */
    val sequence: Long = 0L,
    /** Plugin wall-clock time of the state change, UTC epoch milliseconds. */
    val changedAtEpochMillis: Long = 0L,
) : Parcelable
