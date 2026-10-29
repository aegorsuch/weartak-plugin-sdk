package com.weartak.plugin.sdk.v3.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** One original host capture chunk. Sequence values are monotonic within [sessionId]. */
@Parcelize
data class PcmChunkParcel(
    val sessionId: String,
    val sequence: Long,
    val timestampUtcMillis: Long,
    val timestampElapsedMillis: Long,
    val sampleRateHz: Int,
    val channelCount: Int,
    val encoding: Int,
    val samples: ShortArray,
) : Parcelable
