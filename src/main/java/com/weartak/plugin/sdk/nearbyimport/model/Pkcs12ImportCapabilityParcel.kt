package com.weartak.plugin.sdk.nearbyimport.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** Optional staging capability; a null version or control means unsupported. */
@Parcelize
data class Pkcs12ImportCapabilityParcel(
    val capabilityKey: String = "",
    val apiVersion: Int = 0,
    val maxPayloadBytes: Int = 0,
    val maxStageLifetimeMs: Long = 0L,
) : Parcelable
