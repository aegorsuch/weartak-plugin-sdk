package com.weartak.plugin.sdk.v2.binder

import com.weartak.plugin.sdk.v2.WearTakPluginV2Contract
import com.weartak.plugin.sdk.v2.model.CepStreamOperation
import com.weartak.plugin.sdk.v2.model.HostCapabilitiesV2Parcel
import com.weartak.plugin.sdk.v2.model.V2ApplyStatus
import com.weartak.plugin.sdk.v2.model.V2HostRejectionCode
import com.weartak.plugin.sdk.v2.model.V2HostResultParcel
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
class PluginCepRulesClientTest {

    private val pluginId = "com.weartak.plugin.test"
    private val sensorKey = "test.motion"
    private val rules = """{"version":1,"rules":[]}"""

    private fun v2Caps(cepRules: Boolean = true) = HostCapabilitiesV2Parcel(
        contractApiVersion = WearTakPluginV2Contract.CONTRACT_API_VERSION,
        cepRules = cepRules,
    )

    @Test
    fun resolveTransportPath_requiresMatchingContractVersion() {
        val client = PluginCepRulesClient(pluginId)
        assertEquals(
            V2TransportPath.V2_SYNC,
            client.resolveTransportPath(V2RecordingHostControl(v2Caps())),
        )
        assertEquals(
            V2TransportPath.HOST_INCOMPATIBLE,
            client.resolveTransportPath(
                V2RecordingHostControl(v2Caps().copy(contractApiVersion = 1)),
            ),
        )
    }

    @Test
    fun add_syncSuccessPropagatesHostAcceptance() {
        val v2 = V2RecordingHostControl(v2Caps())
        val client = PluginCepRulesClient(pluginId)
        val result = client.add(v2, sensorKey, "set-1", rules)
        assertTrue(result.accepted)
        assertEquals(V2ApplyStatus.SUCCESS, result.status)
        assertEquals(V2TransportPath.V2_SYNC, result.transport)
        assertEquals(1, v2.cepEvents.size)
    }

    @Test
    fun add_syncHostRejectionPropagates() {
        val v2 = object : V2RecordingHostControl(v2Caps(), defaultAccepted = false) {}
        val client = PluginCepRulesClient(pluginId)
        val result = client.add(v2, sensorKey, "set-1", rules)
        assertFalse(result.accepted)
        assertEquals(V2ApplyStatus.HOST_REJECTED, result.status)
        assertTrue(v2.cepEvents.isNotEmpty())
    }

    @Test
    fun add_rejectsIncompatibleHostWithoutCallingAidl() {
        val v2 = V2RecordingHostControl(v2Caps().copy(contractApiVersion = 1))
        val client = PluginCepRulesClient(pluginId)
        val result = client.add(v2, sensorKey, "set-1", rules)
        assertFalse(result.accepted)
        assertEquals(V2ApplyStatus.INCOMPATIBLE_HOST, result.status)
        assertEquals(V2TransportPath.HOST_INCOMPATIBLE, result.transport)
        assertTrue(v2.cepEvents.isEmpty())
    }

    @Test
    fun add_missingCapabilityUnavailable() {
        val client = PluginCepRulesClient(pluginId)
        val result = client.add(null, sensorKey, "set-1", rules)
        assertEquals(V2ApplyStatus.UNAVAILABLE, result.status)
    }

    @Test
    fun invalidJson_rejectedBeforeHostCall() {
        val v2 = V2RecordingHostControl(v2Caps())
        val client = PluginCepRulesClient(pluginId)
        val result = client.add(v2, sensorKey, "set-1", "[]")
        assertEquals(V2ApplyStatus.INVALID_PAYLOAD, result.status)
        assertTrue(v2.cepEvents.isEmpty())
    }

    @Test
    fun update_idempotentSkipsSecondHostCall() {
        val v2 = V2RecordingHostControl(v2Caps())
        val client = PluginCepRulesClient(pluginId)
        assertTrue(client.add(v2, sensorKey, "set-1", rules).accepted)
        Thread.sleep(WearTakPluginV2Contract.V2_STREAM_MIN_INTERVAL_MS + 5L)
        assertTrue(client.update(v2, sensorKey, "set-1", rules).accepted)
        assertEquals(1, v2.cepEvents.size)
    }

    @Test
    fun advisoryIdentifiers_passedThroughUnmodified() {
        val v2 = V2RecordingHostControl(v2Caps())
        val client = PluginCepRulesClient(pluginId)
        client.add(v2, sensorKey, "advisory-set", rules)
        assertEquals(pluginId, v2.cepEvents.single().pluginId)
        assertEquals(sensorKey, v2.cepEvents.single().sensorKey)
        assertEquals("advisory-set", v2.cepEvents.single().ruleSetId)
    }

    @Test
    fun hostExplicitRejectionCodePropagates() {
        val v2 = object : V2RecordingHostControl(v2Caps()) {
            override fun applyCepRuleEvent(event: com.weartak.plugin.sdk.v2.model.CepRuleStreamEventParcel?): V2HostResultParcel {
                if (event != null) cepEvents += event
                return V2HostResultParcel(false, V2HostRejectionCode.UNSPECIFIED)
            }
        }
        val result = PluginCepRulesClient(pluginId).add(v2, sensorKey, "set-1", rules)
        assertEquals(V2ApplyStatus.HOST_REJECTED, result.status)
        assertEquals(V2HostRejectionCode.UNSPECIFIED, result.hostRejectionCode)
    }
}
