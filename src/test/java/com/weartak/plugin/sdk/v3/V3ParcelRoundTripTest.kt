package com.weartak.plugin.sdk.v3

import android.os.Parcel
import android.os.Parcelable
import com.weartak.plugin.sdk.v3.model.HostCapabilitiesV3Parcel
import com.weartak.plugin.sdk.v3.model.PcmBatchParcel
import com.weartak.plugin.sdk.v3.model.PcmChunkParcel
import com.weartak.plugin.sdk.v3.model.PcmStreamState
import com.weartak.plugin.sdk.v3.model.PcmStreamStateParcel
import com.weartak.plugin.sdk.v3.model.PcmSubscriptionRequestParcel
import com.weartak.plugin.sdk.v3.model.PcmSubscriptionResultParcel
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class V3ParcelRoundTripTest {
    @Test
    fun pcmBatch_roundTripsOriginalChunkMetadataAndSamples() {
        val chunk = PcmChunkParcel(
            sessionId = "audio-1",
            sequence = 42L,
            timestampUtcMillis = 1_700_000_000_000L,
            timestampElapsedMillis = 12_345L,
            sampleRateHz = WearTakPluginV3Contract.PCM_SAMPLE_RATE_HZ,
            channelCount = WearTakPluginV3Contract.PCM_CHANNEL_COUNT,
            encoding = WearTakPluginV3Contract.PCM_ENCODING,
            samples = shortArrayOf(-2, -1, 0, 1, 2),
        )
        val actual = roundTrip(PcmBatchParcel("sub-1", listOf(chunk)))

        assertEquals("sub-1", actual.subscriptionId)
        assertEquals(1, actual.chunks.size)
        assertEquals(chunk.sessionId, actual.chunks.single().sessionId)
        assertEquals(chunk.sequence, actual.chunks.single().sequence)
        assertArrayEquals(chunk.samples, actual.chunks.single().samples)
    }

    @Test
    fun capabilityAndLifecycleParcels_roundTrip() {
        val capabilities = roundTrip(
            HostCapabilitiesV3Parcel(
                contractApiVersion = WearTakPluginV3Contract.CONTRACT_API_VERSION,
                pcmStreaming = true,
            ),
        )
        val request = roundTrip(PcmSubscriptionRequestParcel(requestedChunksPerBatch = 2))
        val result = roundTrip(PcmSubscriptionResultParcel(true, "sub-2"))
        val state = roundTrip(PcmStreamStateParcel("sub-2", PcmStreamState.LISTENING))

        assertEquals(3, capabilities.contractApiVersion)
        assertEquals(true, capabilities.pcmStreaming)
        assertEquals(2, request.requestedChunksPerBatch)
        assertEquals("sub-2", result.subscriptionId)
        assertEquals(PcmStreamState.LISTENING, state.state)
    }

    private inline fun <reified T : Parcelable> roundTrip(value: T): T {
        val parcel = Parcel.obtain()
        return try {
            value.writeToParcel(parcel, 0)
            parcel.setDataPosition(0)
            @Suppress("UNCHECKED_CAST")
            val creator = value::class.java.getField("CREATOR").get(null) as Parcelable.Creator<T>
            creator.createFromParcel(parcel)
        } finally {
            parcel.recycle()
        }
    }
}
