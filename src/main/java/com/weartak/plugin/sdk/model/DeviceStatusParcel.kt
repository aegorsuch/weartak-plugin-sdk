package com.weartak.plugin.sdk.model

import android.os.Bundle
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeviceStatusParcel(
    val online: Boolean,
    val summary: String = "",
    val telemetry: Bundle = Bundle(),
) : Parcelable
