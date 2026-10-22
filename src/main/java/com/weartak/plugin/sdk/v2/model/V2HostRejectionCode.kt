package com.weartak.plugin.sdk.v2.model

/** Host-reported rejection codes in [V2HostResultParcel]. */
object V2HostRejectionCode {
    const val NONE: Int = 0
    const val UNSPECIFIED: Int = 1
    /** Host does not implement the requested v2 lane (capability flag is false). */
    const val NOT_SUPPORTED: Int = 2
}
