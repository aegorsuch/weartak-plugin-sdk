package com.weartak.plugin.sdk.binder

import android.os.RemoteException
import com.weartak.plugin.sdk.aidl.IWearTakPluginControl
import com.weartak.plugin.sdk.model.AlertRequestParcel
import com.weartak.plugin.sdk.model.CotRequestParcel
import com.weartak.plugin.sdk.model.DeviceStatusParcel
import com.weartak.plugin.sdk.model.HostCapabilitiesParcel
import com.weartak.plugin.sdk.model.MarkerParcel
import com.weartak.plugin.sdk.model.ToastRequestParcel
import com.weartak.plugin.sdk.aidl.IWearTakMapLayerControl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PluginToastTest {

    @Test
    fun show_returnsFalseWhenControlNull() {
        assertFalse(PluginToast.show("tag", null, "hello"))
    }

    @Test
    fun show_returnsFalseWhenHostDisablesToasts() {
        val control = RecordingPluginControl(HostCapabilitiesParcel(toasts = false))
        assertFalse(PluginToast.show("tag", control, "hello"))
        assertTrue(control.toastRequests.isEmpty())
    }

    @Test
    fun show_returnsTrueWhenHostSupportsToasts() {
        val control = RecordingPluginControl(HostCapabilitiesParcel(toasts = true))
        assertTrue(
            PluginToast.show(
                tag = "sync",
                control = control,
                message = "Saved",
                duration = ToastRequestParcel.DURATION_LONG,
            ),
        )
        assertEquals(1, control.toastRequests.size)
        assertEquals("Saved", control.toastRequests.single().message)
        assertEquals(ToastRequestParcel.DURATION_LONG, control.toastRequests.single().duration)
        assertEquals("sync", control.toastRequests.single().tag)
    }

    @Test
    fun show_returnsFalseOnRemoteException() {
        val control = ThrowingPluginControl()
        assertFalse(PluginToast.show("tag", control, "hello"))
    }

    @Test
    fun show_returnsFalseWhenMessageBlank() {
        val control = RecordingPluginControl(HostCapabilitiesParcel(toasts = true))
        assertFalse(PluginToast.show("tag", control, " "))
        assertTrue(control.toastRequests.isEmpty())
    }

    class RecordingPluginControl(
        private val hostCapabilities: HostCapabilitiesParcel = HostCapabilitiesParcel(),
    ) : IWearTakPluginControl.Stub() {

        val toastRequests = mutableListOf<ToastRequestParcel>()

        override fun requestToast(request: ToastRequestParcel?) {
            if (request != null) toastRequests += request
        }

        override fun getHostCapabilities(): HostCapabilitiesParcel = hostCapabilities

        override fun requestAlert(request: AlertRequestParcel?) = Unit
        override fun requestCotDispatch(request: CotRequestParcel?) = Unit
        override fun requestMarkerPublish(marker: MarkerParcel?) = Unit
        override fun requestMarkerUpdate(marker: MarkerParcel?) = Unit
        override fun requestMarkerClear(marker: MarkerParcel?) = Unit
        override fun getDeviceStatus(): DeviceStatusParcel = DeviceStatusParcel(online = false)
        override fun registerCepRules(pluginId: String?, sensorKey: String?, rulesJson: String?) = Unit
        override fun unregisterCepRules(pluginId: String?, sensorKey: String?) = Unit
        override fun applyCepRuleEvent(event: com.weartak.plugin.sdk.model.CepRuleEventParcel?) = Unit
        override fun isCotConnected(): Boolean = false
        override fun getMapLayerControl(): IWearTakMapLayerControl? = null
    }

    class ThrowingPluginControl : IWearTakPluginControl.Stub() {
        override fun getHostCapabilities(): HostCapabilitiesParcel {
            throw RemoteException("binder dead")
        }

        override fun requestToast(request: ToastRequestParcel?) = Unit
        override fun requestAlert(request: AlertRequestParcel?) = Unit
        override fun requestCotDispatch(request: CotRequestParcel?) = Unit
        override fun requestMarkerPublish(marker: MarkerParcel?) = Unit
        override fun requestMarkerUpdate(marker: MarkerParcel?) = Unit
        override fun requestMarkerClear(marker: MarkerParcel?) = Unit
        override fun getDeviceStatus(): DeviceStatusParcel = DeviceStatusParcel(online = false)
        override fun registerCepRules(pluginId: String?, sensorKey: String?, rulesJson: String?) = Unit
        override fun unregisterCepRules(pluginId: String?, sensorKey: String?) = Unit
        override fun applyCepRuleEvent(event: com.weartak.plugin.sdk.model.CepRuleEventParcel?) = Unit
        override fun isCotConnected(): Boolean = false
        override fun getMapLayerControl(): IWearTakMapLayerControl? = null
    }
}
