package com.weartak.plugin.sdk.nearbyimport.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** PKCS#12 bytes plus a per-attempt replay key. Never include a password or metadata. */
@Parcelize
data class Pkcs12StageRequestParcel(
    val requestId: String,
    val pkcs12Bytes: ByteArray,
) : Parcelable
