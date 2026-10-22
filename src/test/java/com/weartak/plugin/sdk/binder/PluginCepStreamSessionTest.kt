package com.weartak.plugin.sdk.binder

import android.os.RemoteException
import com.weartak.plugin.sdk.WearTakPluginContract
import com.weartak.plugin.sdk.aidl.IWearTakMapLayerControl
import com.weartak.plugin.sdk.aidl.IWearTakPluginControl
import com.weartak.plugin.sdk.model.AlertRequestParcel
import com.weartak.plugin.sdk.model.CepRuleEventParcel
import com.weartak.plugin.sdk.model.CepRuleOperation
import com.weartak.plugin.sdk.model.CotRequestParcel
import com.weartak.plugin.sdk.model.DeviceStatusParcel
import com.weartak.plugin.sdk.model.HostCapabilitiesParcel
import com.weartak.plugin.sdk.model.MarkerParcel
import com.weartak.plugin.sdk.model.ToastRequestParcel
import com.weartak.plugin.sdk.validation.PluginStringPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PluginCepStreamSessionTest {

    private val pluginId = "com.weartak.plugin.test"
    private val sensorKey = "test.motion"
    private val rulesV1 = """{"version":1,"rules":[]}"""
    private val rulesV2 = """{"version":1,"rules":[{"id":"r1"}]}"""

    @Test
    fun add_usesRegisterOnV1Host() {
        val control = RecordingPluginControl(HostCapabilitiesParcel(cepRules = true, cepRuleStreaming = false))
        val session = PluginCepStreamSession(pluginId)
        assertTrue(session.add(control, sensorKey, "set-1", rulesV1))
        assertEquals(1, control.registerCalls.size)
        assertTrue(control.streamEvents.isEmpty())
    }

    @Test
    fun update_usesApplyCepRuleEventOnStreamingHost() {
        val control = RecordingPluginControl(HostCapabilitiesParcel(cepRules = true, cepRuleStreaming = true))
        val session = PluginCepStreamSession(pluginId)
        assertTrue(session.add(control, sensorKey, "set-1", rulesV1))
        Thread.sleep(WearTakPluginContract.CEP_STREAM_MIN_INTERVAL_MS + 5L)
        assertTrue(session.update(control, sensorKey, "set-1", rulesV2))
        assertEquals(2, control.streamEvents.size)
        assertEquals(CepRuleOperation.ADD, control.streamEvents[0].operation)
        assertEquals(CepRuleOperation.UPDATE, control.streamEvents[1].operation)
        assertEquals(1L, control.streamEvents[0].sequence)
        assertEquals(2L, control.streamEvents[1].sequence)
    }

    @Test
    fun remove_usesUnregisterOnV1Host() {
        val control = RecordingPluginControl(HostCapabilitiesParcel(cepRules = true))
        val session = PluginCepStreamSession(pluginId)
        assertTrue(session.add(control, sensorKey, "set-1", rulesV1))
        Thread.sleep(WearTakPluginContract.CEP_STREAM_MIN_INTERVAL_MS + 5L)
        assertTrue(session.remove(control, sensorKey, "set-1"))
        assertEquals(1, control.unregisterCalls.size)
    }

    @Test
    fun update_isIdempotentForIdenticalPayload() {
        val control = RecordingPluginControl(HostCapabilitiesParcel(cepRules = true, cepRuleStreaming = true))
        val session = PluginCepStreamSession(pluginId)
        assertTrue(session.update(control, sensorKey, "set-1", rulesV1))
        assertTrue(session.update(control, sensorKey, "set-1", rulesV1))
        assertEquals(1, control.streamEvents.size)
    }

    @Test
    fun invalidJsonRejected() {
        val control = RecordingPluginControl(HostCapabilitiesParcel(cepRules = true, cepRuleStreaming = true))
        val session = PluginCepStreamSession(pluginId)
        assertFalse(session.add(control, sensorKey, "set-1", "[]"))
        assertTrue(control.streamEvents.isEmpty())
    }

    @Test
    fun cepRulesDisabled_returnsFalse() {
        val control = RecordingPluginControl(HostCapabilitiesParcel(cepRules = false))
        val session = PluginCepStreamSession(pluginId)
        assertFalse(session.add(control, sensorKey, "set-1", rulesV1))
    }

    @Test
    fun register_unregister_v1Compatibility() {
        val control = RecordingPluginControl(HostCapabilitiesParcel(cepRules = true))
        val session = PluginCepStreamSession(pluginId)
        assertTrue(session.register(control, sensorKey, rulesV1))
        Thread.sleep(WearTakPluginContract.CEP_STREAM_MIN_INTERVAL_MS + 5L)
        assertTrue(session.unregister(control, sensorKey))
        assertEquals(1, control.registerCalls.size)
        assertEquals(1, control.unregisterCalls.size)
    }

    @Test
    fun remoteException_returnsFalse() {
        val control = ThrowingPluginControl()
        val session = PluginCepStreamSession(pluginId)
        assertFalse(session.add(control, sensorKey, "set-1", rulesV1))
    }

    class RecordingPluginControl(
        private val hostCapabilities: HostCapabilitiesParcel = HostCapabilitiesParcel(),
    ) : IWearTakPluginControl.Stub() {

        data class RegisterCall(val pluginId: String, val sensorKey: String, val rulesJson: String)

        val registerCalls = mutableListOf<RegisterCall>()
        val unregisterCalls = mutableListOf<Pair<String, String>>()
        val streamEvents = mutableListOf<CepRuleEventParcel>()

        override fun registerCepRules(pluginId: String?, sensorKey: String?, rulesJson: String?) {
            if (pluginId != null && sensorKey != null && rulesJson != null) {
                registerCalls += RegisterCall(pluginId, sensorKey, rulesJson)
            }
        }

        override fun unregisterCepRules(pluginId: String?, sensorKey: String?) {
            if (pluginId != null && sensorKey != null) {
                unregisterCalls += pluginId to sensorKey
            }
        }

        override fun applyCepRuleEvent(event: CepRuleEventParcel?) {
            if (event != null) streamEvents += event
        }

        override fun getHostCapabilities(): HostCapabilitiesParcel = hostCapabilities

        override fun requestAlert(request: AlertRequestParcel?) = Unit
        override fun requestToast(request: ToastRequestParcel?) = Unit
        override fun requestCotDispatch(request: CotRequestParcel?) = Unit
        override fun requestMarkerPublish(marker: MarkerParcel?) = Unit
        override fun requestMarkerUpdate(marker: MarkerParcel?) = Unit
        override fun requestMarkerClear(marker: MarkerParcel?) = Unit
        override fun getDeviceStatus(): DeviceStatusParcel = DeviceStatusParcel(online = false)
        override fun isCotConnected(): Boolean = false
        override fun getMapLayerControl(): IWearTakMapLayerControl? = null
    }

    class ThrowingPluginControl : IWearTakPluginControl.Stub() {
        override fun getHostCapabilities(): HostCapabilitiesParcel {
            throw RemoteException("dead")
        }

        override fun registerCepRules(pluginId: String?, sensorKey: String?, rulesJson: String?) = Unit
        override fun unregisterCepRules(pluginId: String?, sensorKey: String?) = Unit
        override fun applyCepRuleEvent(event: CepRuleEventParcel?) = Unit
        override fun requestAlert(request: AlertRequestParcel?) = Unit
        override fun requestToast(request: ToastRequestParcel?) = Unit
        override fun requestCotDispatch(request: CotRequestParcel?) = Unit
        override fun requestMarkerPublish(marker: MarkerParcel?) = Unit
        override fun requestMarkerUpdate(marker: MarkerParcel?) = Unit
        override fun requestMarkerClear(marker: MarkerParcel?) = Unit
        override fun getDeviceStatus(): DeviceStatusParcel = DeviceStatusParcel(online = false)
        override fun isCotConnected(): Boolean = false
        override fun getMapLayerControl(): IWearTakMapLayerControl? = null
    }
}
