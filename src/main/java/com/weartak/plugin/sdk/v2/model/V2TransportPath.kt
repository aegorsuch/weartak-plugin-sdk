package com.weartak.plugin.sdk.v2.model

/**
 * v2 transport selection for plugin→host event helpers.
 *
 * v2 helpers use [V2_SYNC] only and **do not** fall back to v1 / v1.x surfaces.
 */
enum class V2TransportPath {
    /** Synchronous v2 control plane ([com.weartak.plugin.sdk.v2.aidl.IWearTakPluginHostControlV2]). */
    V2_SYNC,
    /** Host present but contract version or v2 capability does not match (reject policy). */
    HOST_INCOMPATIBLE,
    /** No v2 control binder or required family capability. */
    UNAVAILABLE,
}
