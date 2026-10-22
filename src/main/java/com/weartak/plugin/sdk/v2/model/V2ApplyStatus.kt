package com.weartak.plugin.sdk.v2.model

/** SDK-side outcome for v2 helper calls (maps from sync host result + local guards). */
enum class V2ApplyStatus {
    SUCCESS,
    HOST_REJECTED,
    INCOMPATIBLE_HOST,
    MISSING_CAPABILITY,
    INVALID_PAYLOAD,
    CLIENT_BACKPRESSURE,
    REMOTE_ERROR,
    UNAVAILABLE,
}
