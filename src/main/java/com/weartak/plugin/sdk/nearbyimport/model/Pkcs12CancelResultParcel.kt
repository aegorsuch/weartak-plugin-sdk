package com.weartak.plugin.sdk.nearbyimport.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** Terminal result for a host-side cancellation request. */
@Parcelize
data class Pkcs12CancelResultParcel(
    val state: Int,
    val errorCode: Int = 0,
) : Parcelable
