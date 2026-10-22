package com.weartak.plugin.sdk.v2.binder

import com.weartak.plugin.sdk.v2.WearTakPluginV2Contract
import com.weartak.plugin.sdk.v2.model.HostCapabilitiesV2Parcel
import com.weartak.plugin.sdk.v2.model.TaskCommandOutcome
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
class PluginTaskCommandClientTest {

    private val pluginId = "com.weartak.plugin.test"
    private val commandId = "cmd-advisory"
    private val payload = """{"action":"sync"}"""

    private fun taskCaps() = HostCapabilitiesV2Parcel(
        contractApiVersion = WearTakPluginV2Contract.CONTRACT_API_VERSION,
        taskCommands = true,
    )

    @Test
    fun resolveTransportPath_rejectsIncompatibleVersion() {
        val client = PluginTaskCommandClient(pluginId)
        assertEquals(V2TransportPath.V2_SYNC, client.resolveTransportPath(V2RecordingHostControl(taskCaps())))
        assertEquals(
            V2TransportPath.HOST_INCOMPATIBLE,
            client.resolveTransportPath(
                V2RecordingHostControl(taskCaps().copy(contractApiVersion = 1)),
            ),
        )
    }

    @Test
    fun submit_progress_complete_syncFlow() {
        val control = V2RecordingHostControl(taskCaps())
        val client = PluginTaskCommandClient(pluginId)
        assertTrue(client.submit(control, commandId, "sync", payload).accepted)
        Thread.sleep(WearTakPluginV2Contract.V2_STREAM_MIN_INTERVAL_MS + 5L)
        assertTrue(client.reportProgress(control, commandId, 50, "half").accepted)
        Thread.sleep(WearTakPluginV2Contract.V2_STREAM_MIN_INTERVAL_MS + 5L)
        assertTrue(client.complete(control, commandId, TaskCommandOutcome.SUCCESS, "done").accepted)
        assertEquals(1, control.submissions.size)
        assertEquals(1, control.progressReports.size)
        assertEquals(1, control.completions.size)
        assertEquals(1L, control.submissions.single().sequence)
        assertEquals(2L, control.progressReports.single().sequence)
        assertEquals(3L, control.completions.single().sequence)
    }

    @Test
    fun submit_rejectsIncompatibleHostWithoutAidlCall() {
        val control = V2RecordingHostControl(taskCaps().copy(contractApiVersion = 1))
        val result = PluginTaskCommandClient(pluginId).submit(control, commandId, "sync", payload)
        assertEquals(V2ApplyStatus.INCOMPATIBLE_HOST, result.status)
        assertTrue(control.submissions.isEmpty())
    }

    @Test
    fun submit_rejectsInvalidPayloadBeforeHostCall() {
        val control = V2RecordingHostControl(taskCaps())
        val result = PluginTaskCommandClient(pluginId).submit(control, commandId, "sync", "[]")
        assertEquals(V2ApplyStatus.INVALID_PAYLOAD, result.status)
        assertTrue(control.submissions.isEmpty())
    }

    @Test
    fun advisoryIdentifiers_passedThroughUnmodified() {
        val control = V2RecordingHostControl(taskCaps())
        PluginTaskCommandClient(pluginId).submit(control, commandId, "sync", payload)
        assertEquals(commandId, control.submissions.single().commandId)
        assertEquals(pluginId, control.submissions.single().pluginId)
    }
}
