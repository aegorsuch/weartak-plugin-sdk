package com.weartak.plugin.sdk.v2.binder

import com.weartak.plugin.sdk.v2.WearTakPluginV2Contract
import com.weartak.plugin.sdk.v2.model.HostCapabilitiesV2Parcel
import com.weartak.plugin.sdk.v2.model.V2ApplyStatus
import com.weartak.plugin.sdk.v2.model.V2TransportPath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PluginDetectionEventsClientTest {

    private val pluginId = "com.weartak.plugin.test"

    private fun detectionCaps() = HostCapabilitiesV2Parcel(
        contractApiVersion = WearTakPluginV2Contract.CONTRACT_API_VERSION,
        detectionEvents = true,
    )

    @Test
    fun resolveTransportPath_rejectsIncompatibleVersion() {
        val client = PluginDetectionEventsClient(pluginId)
        assertEquals(
            V2TransportPath.V2_SYNC,
            client.resolveTransportPath(V2RecordingHostControl(detectionCaps())),
        )
        assertEquals(
            V2TransportPath.HOST_INCOMPATIBLE,
            client.resolveTransportPath(
                V2RecordingHostControl(detectionCaps().copy(contractApiVersion = 1)),
            ),
        )
    }

    @Test
    fun publish_syncSuccess() {
        val control = V2RecordingHostControl(detectionCaps())
        val client = PluginDetectionEventsClient(pluginId)
        val result = client.publish(control, "cam.front", "det-1", "person", confidence = 0.9f)
        assertTrue(result.accepted)
        assertEquals(V2ApplyStatus.SUCCESS, result.status)
        assertEquals(1, control.detections.size)
    }

    @Test
    fun publish_rejectsIncompatibleHostWithoutAidlCall() {
        val control = V2RecordingHostControl(detectionCaps().copy(contractApiVersion = 1))
        val result = PluginDetectionEventsClient(pluginId).publish(control, "cam.front", "det-1", "person")
        assertEquals(V2ApplyStatus.INCOMPATIBLE_HOST, result.status)
        assertTrue(control.detections.isEmpty())
    }

    @Test
    fun publish_rejectsInvalidAttributesBeforeHostCall() {
        val control = V2RecordingHostControl(detectionCaps())
        val result = PluginDetectionEventsClient(pluginId).publish(
            control,
            "cam.front",
            "det-1",
            "person",
            attributesJson = "[]",
        )
        assertEquals(V2ApplyStatus.INVALID_PAYLOAD, result.status)
        assertTrue(control.detections.isEmpty())
    }

    @Test
    fun publish_idempotentSkipsSecondHostCall() {
        val control = V2RecordingHostControl(detectionCaps())
        val client = PluginDetectionEventsClient(pluginId)
        assertTrue(client.publish(control, "cam.front", "det-1", "person", confidence = 0.5f).accepted)
        Thread.sleep(WearTakPluginV2Contract.V2_STREAM_MIN_INTERVAL_MS + 5L)
        assertTrue(client.publish(control, "cam.front", "det-2", "person", confidence = 0.5f).accepted)
        assertEquals(1, control.detections.size)
    }

    @Test
    fun advisoryIdentifiers_passedThroughUnmodified() {
        val control = V2RecordingHostControl(detectionCaps())
        PluginDetectionEventsClient(pluginId).publish(control, "advisory-stream", "advisory-det", "vehicle")
        assertEquals("advisory-stream", control.detections.single().streamKey)
        assertEquals("advisory-det", control.detections.single().detectionId)
    }
}
