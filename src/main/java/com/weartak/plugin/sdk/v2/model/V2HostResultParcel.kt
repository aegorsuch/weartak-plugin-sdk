package com.weartak.plugin.sdk.v2.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Synchronous v2 host response for plugin→host event calls on [com.weartak.plugin.sdk.v2.aidl.IWearTakPluginHostControlV2].
 */
@Parcelize
data class V2HostResultParcel(
    val accepted: Boolean,
    val rejectionCode: Int = V2HostRejectionCode.NONE,
) : Parcelable
