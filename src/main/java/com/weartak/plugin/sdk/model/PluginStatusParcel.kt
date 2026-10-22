package com.weartak.plugin.sdk.model

import android.os.Bundle
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PluginStatusParcel(
    val state: PluginLifecycleState,
    val message: String = "",
    /** Optional fine-grained status for diagnostics (not part of AIDL versioning). */
    val detailCode: Int = 0,
    val extras: Bundle = Bundle(),
) : Parcelable
