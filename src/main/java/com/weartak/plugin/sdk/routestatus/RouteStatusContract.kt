package com.weartak.plugin.sdk.routestatus

/**
 * Optional plugin route connection-status extension. Existing plugin API versions, parcel
 * layouts and Binder transactions are unchanged.
 */
object RouteStatusContract {
    const val EXTRA_CAPABILITY: String = "routeStatusV1"
    const val EXTRA_CONTROL: String = "routeStatusControlV1"

    const val CONTRACT_VERSION: Int = 1

    const val MAX_ROUTES_PER_PLUGIN: Int = 8
    const val MAX_ROUTE_ID_CHARS: Int = 64
    const val MAX_LABEL_BYTES: Int = 64
    const val MAX_STATUS_TEXT_BYTES: Int = 128

    /** Route ids are stable, case-sensitive, and limited to `[A-Za-z0-9._-]`. */
    val ROUTE_ID_PATTERN: Regex = Regex("^[A-Za-z0-9._-]{1,$MAX_ROUTE_ID_CHARS}$")
}

/** Wire values for [com.weartak.plugin.sdk.routestatus.model.PluginRouteStatusParcel.state]. */
object RouteConnectionState {
    const val UNKNOWN: Int = 0
    const val DISCONNECTED: Int = 1
    const val CONNECTING: Int = 2
    const val CONNECTED: Int = 3
    const val ERROR: Int = 4

    /** Hosts must display values outside this set as [UNKNOWN], never as connected. */
    fun isKnown(state: Int): Boolean = state in UNKNOWN..ERROR
}

/** Results from [com.weartak.plugin.sdk.routestatus.aidl.IPluginRouteStatusControlV1]. */
object RouteStatusResultCode {
    const val OK: Int = 0
    const val INVALID_REQUEST: Int = 1
    const val UNAUTHORIZED: Int = 2
    const val INACTIVE_SESSION: Int = 3
    const val LIMIT_EXCEEDED: Int = 4
    const val STALE_SEQUENCE: Int = 5
    const val UNKNOWN_ROUTE: Int = 6

    /** Client-only: capability missing, Binder dead, or transport failure. Never sent by hosts. */
    const val UNAVAILABLE: Int = -1
}
