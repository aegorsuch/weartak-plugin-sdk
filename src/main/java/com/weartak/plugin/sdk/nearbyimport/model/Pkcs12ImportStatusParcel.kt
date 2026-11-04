package com.weartak.plugin.sdk.nearbyimport.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** Non-sensitive lifecycle status. Never include credentials or certificate/private-key data. */
@Parcelize
data class Pkcs12ImportStatusParcel(
    val state: Int,
    val expiresAtElapsedRealtimeMs: Long = 0L,
    val errorCode: Int = 0,
) : Parcelable
