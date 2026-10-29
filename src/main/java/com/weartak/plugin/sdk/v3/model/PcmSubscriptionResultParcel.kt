package com.weartak.plugin.sdk.v3.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** Immediate result for PCM subscribe and unsubscribe operations. */
@Parcelize
data class PcmSubscriptionResultParcel(
    val accepted: Boolean,
    val subscriptionId: String? = null,
    val status: Int = PcmSubscriptionStatus.NONE,
) : Parcelable

object PcmSubscriptionStatus {
    const val NONE: Int = 0
    const val NOT_SUPPORTED: Int = 1
    const val INVALID_REQUEST: Int = 2
    const val UNAUTHORIZED: Int = 3
    const val CALLBACK_REQUIRED: Int = 4
    const val NOT_ACTIVE: Int = 5
    const val HOST_INCOMPATIBLE: Int = 6
    const val REMOTE_ERROR: Int = 7
    const val INTERNAL_ERROR: Int = 8
}
