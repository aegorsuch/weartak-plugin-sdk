package com.weartak.plugin.sdk.v3.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** Asynchronous state for an accepted PCM subscription. */
@Parcelize
data class PcmStreamStateParcel(
    val subscriptionId: String,
    val state: Int,
    val reasonCode: Int = PcmSubscriptionStatus.NONE,
) : Parcelable

object PcmStreamState {
    const val STARTING: Int = 1
    const val LISTENING: Int = 2
    const val STOPPED: Int = 3
    const val ERROR: Int = 4
}
