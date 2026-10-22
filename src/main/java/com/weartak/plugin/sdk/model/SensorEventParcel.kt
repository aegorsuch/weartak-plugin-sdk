package com.weartak.plugin.sdk.model

import android.os.Bundle
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SensorEventParcel(
    val sensorKey: String,
    val timestampUtcMillis: Long,
    val sequence: Long = 0L,
    val values: Bundle = Bundle(),
) : Parcelable
