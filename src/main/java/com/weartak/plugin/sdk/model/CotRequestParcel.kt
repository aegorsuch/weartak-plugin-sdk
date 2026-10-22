package com.weartak.plugin.sdk.model

import android.os.Bundle
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CotRequestParcel(
    val payloadFormat: String,
    val payload: Bundle = Bundle(),
    val priority: Int = 0,
) : Parcelable
