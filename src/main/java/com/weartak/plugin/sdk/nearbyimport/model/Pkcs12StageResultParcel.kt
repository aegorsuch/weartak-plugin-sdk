package com.weartak.plugin.sdk.nearbyimport.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** STAGED confirms host admission only; it does not mean reviewed or trusted. */
@Parcelize
data class Pkcs12StageResultParcel(
    val code: Int,
    val handle: String? = null,
    val expiresAtElapsedRealtimeMs: Long = 0L,
) : Parcelable
