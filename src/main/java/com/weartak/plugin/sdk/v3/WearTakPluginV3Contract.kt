package com.weartak.plugin.sdk.v3

import android.media.AudioFormat

/** v3 host-to-plugin PCM streaming contract. v1 and v2 remain unchanged. */
object WearTakPluginV3Contract {
    const val CONTRACT_API_VERSION: Int = 3

    const val PCM_SAMPLE_RATE_HZ: Int = 44_100
    const val PCM_CHANNEL_COUNT: Int = 1
    const val PCM_ENCODING: Int = AudioFormat.ENCODING_PCM_16BIT
    const val PCM_SAMPLES_PER_CHUNK: Int = 1_024

    const val DEFAULT_CHUNKS_PER_BATCH: Int = 4
    const val MAX_CHUNKS_PER_BATCH: Int = 4
    const val MAX_PENDING_CHUNKS: Int = 50
    const val MAX_BATCH_LATENCY_MS: Long = 100L
}
