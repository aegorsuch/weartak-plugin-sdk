package com.weartak.plugin.sdk.v3.model

import android.os.Parcelable
import com.weartak.plugin.sdk.v3.WearTakPluginV3Contract
import kotlinx.parcelize.Parcelize

/** Plugin request for the single PCM format currently produced by WearTAK. */
@Parcelize
data class PcmSubscriptionRequestParcel(
    val sampleRateHz: Int = WearTakPluginV3Contract.PCM_SAMPLE_RATE_HZ,
    val channelCount: Int = WearTakPluginV3Contract.PCM_CHANNEL_COUNT,
    val encoding: Int = WearTakPluginV3Contract.PCM_ENCODING,
    val requestedChunksPerBatch: Int = WearTakPluginV3Contract.DEFAULT_CHUNKS_PER_BATCH,
) : Parcelable
