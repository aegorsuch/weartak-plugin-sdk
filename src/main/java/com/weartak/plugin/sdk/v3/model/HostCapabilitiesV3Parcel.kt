package com.weartak.plugin.sdk.v3.model

import android.os.Parcelable
import com.weartak.plugin.sdk.v3.WearTakPluginV3Contract
import kotlinx.parcelize.Parcelize

/** Capability and fixed-format snapshot for the v3 PCM control plane. */
@Parcelize
data class HostCapabilitiesV3Parcel(
    val contractApiVersion: Int = 0,
    val pcmStreaming: Boolean = false,
    val sampleRateHz: Int = WearTakPluginV3Contract.PCM_SAMPLE_RATE_HZ,
    val channelCount: Int = WearTakPluginV3Contract.PCM_CHANNEL_COUNT,
    val encoding: Int = WearTakPluginV3Contract.PCM_ENCODING,
    val samplesPerChunk: Int = WearTakPluginV3Contract.PCM_SAMPLES_PER_CHUNK,
    val maxChunksPerBatch: Int = WearTakPluginV3Contract.MAX_CHUNKS_PER_BATCH,
    val maxPendingChunks: Int = WearTakPluginV3Contract.MAX_PENDING_CHUNKS,
    val maxBatchLatencyMs: Long = WearTakPluginV3Contract.MAX_BATCH_LATENCY_MS,
) : Parcelable
