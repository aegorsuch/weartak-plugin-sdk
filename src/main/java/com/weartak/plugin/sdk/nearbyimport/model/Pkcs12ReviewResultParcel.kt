package com.weartak.plugin.sdk.nearbyimport.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** Result of requesting host-owned wearer review; this cannot apply trust. */
@Parcelize
data class Pkcs12ReviewResultParcel(
    val state: Int,
    val errorCode: Int = 0,
) : Parcelable
