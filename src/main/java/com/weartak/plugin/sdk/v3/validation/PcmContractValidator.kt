package com.weartak.plugin.sdk.v3.validation

import com.weartak.plugin.sdk.v3.WearTakPluginV3Contract
import com.weartak.plugin.sdk.v3.model.PcmSubscriptionRequestParcel

object PcmContractValidator {
    fun isSupported(request: PcmSubscriptionRequestParcel): Boolean =
        request.sampleRateHz == WearTakPluginV3Contract.PCM_SAMPLE_RATE_HZ &&
            request.channelCount == WearTakPluginV3Contract.PCM_CHANNEL_COUNT &&
            request.encoding == WearTakPluginV3Contract.PCM_ENCODING &&
            request.requestedChunksPerBatch in 1..WearTakPluginV3Contract.MAX_CHUNKS_PER_BATCH
}
