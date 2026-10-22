package com.weartak.plugin.sdk.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CotReadyEventParcel(
    val ready: Boolean,
    val detail: String = "",
) : Parcelable
