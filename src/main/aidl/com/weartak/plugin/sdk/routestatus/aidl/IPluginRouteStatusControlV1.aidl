package com.weartak.plugin.sdk.routestatus.aidl;

import com.weartak.plugin.sdk.routestatus.model.PluginRouteStatusParcel;

/**
 * Host-implemented sink for plugin-provided network route connection status.
 * Advertised through PluginInitParcel.hostExtras; see RouteStatusContract.
 * Synchronous calls return a RouteStatusResultCode. Call off the main thread.
 */
interface IPluginRouteStatusControlV1 {
    int getContractVersion();
    /** Upsert one route keyed by (authorized calling plugin, routeId). */
    int reportRouteStatus(in PluginRouteStatusParcel status);
    /** Remove one route the plugin no longer provides. */
    int removeRoute(String routeId);
    /** Remove every route reported by the calling plugin. */
    int clearRoutes();
}
