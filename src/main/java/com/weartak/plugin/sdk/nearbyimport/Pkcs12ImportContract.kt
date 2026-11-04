package com.weartak.plugin.sdk.nearbyimport

/** Versioned optional host capability for staging PKCS#12 data for wearer review. */
object Pkcs12ImportContract {
    const val CAPABILITY_KEY: String = "com.weartak.plugin.sdk.pkcs12-stage"
    const val API_VERSION: Int = 1
    const val MAX_PAYLOAD_BYTES: Int = 32_700
    const val MAX_STAGE_LIFETIME_MS: Long = 300_000L
}

object Pkcs12StageCode {
    const val STAGED: Int = 1
    const val UNSUPPORTED: Int = 2
    const val INVALID_REQUEST: Int = 3
    const val TOO_LARGE: Int = 4
    const val REPLAYED_REQUEST: Int = 5
    const val UNAUTHORIZED: Int = 6
    const val HOST_BUSY: Int = 7
    const val FAILED: Int = 8
}

object Pkcs12ImportState {
    const val STAGED: Int = 1
    const val REVIEW_PENDING: Int = 2
    const val IN_REVIEW: Int = 3
    const val REJECTED: Int = 4
    const val CANCELLED: Int = 5
    const val EXPIRED: Int = 6
    /** Host saved configuration/trust after explicit wearer confirmation; not network success. */
    const val APPLIED: Int = 7

    /** Server package only: post-apply enrollment produced a fresh receiver identity. */
    const val ENROLLED: Int = 8

    /** Server package only: post-apply TLS connection established. */
    const val CONNECTED: Int = 9

    /** Server package only: applied configuration failed enrollment or connection. */
    const val NETWORK_FAILED: Int = 10
}

object Pkcs12ImportErrorCode {
    const val NONE: Int = 0
    const val INVALID_HANDLE: Int = 1
    const val UNAUTHORIZED: Int = 2
    const val EXPIRED: Int = 3
    const val TERMINAL: Int = 4
    const val HOST_BUSY: Int = 5
    const val FAILED: Int = 6
}

object Pkcs12ImportHandle {
    const val BASE64URL_LENGTH: Int = 22

    fun isValidOpaqueHandle(value: String?): Boolean =
        value != null && value.matches(Regex("[A-Za-z0-9_-]{$BASE64URL_LENGTH}"))
}
