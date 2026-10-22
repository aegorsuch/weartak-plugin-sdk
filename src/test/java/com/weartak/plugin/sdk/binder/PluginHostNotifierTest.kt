package com.weartak.plugin.sdk.binder

import android.os.RemoteException
import com.weartak.plugin.sdk.aidl.IWearTakHostCallback
import com.weartak.plugin.sdk.model.CotReadyEventParcel
import com.weartak.plugin.sdk.model.PluginLifecycleState
import com.weartak.plugin.sdk.model.PluginStatusParcel
import com.weartak.plugin.sdk.model.SensorEventParcel
import com.weartak.plugin.sdk.model.SharedBufferParcel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PluginHostNotifierTest {

    @Test
    fun settingsValuesChanged_invokesCallback() {
        val cb = CountingHostCallback()
        PluginHostNotifier.settingsValuesChanged("t", cb)
        assertEquals(1, cb.settingsValuesCalls)
    }

    @Test
    fun mainUiStateChanged_invokesCallback() {
        val cb = CountingHostCallback()
        PluginHostNotifier.mainUiStateChanged("t", cb)
        assertEquals(1, cb.mainUiCalls)
    }

    @Test
    fun statusChanged_invokesCallbackWithParcel() {
        val cb = CountingHostCallback()
        val st = PluginStatusParcel(PluginLifecycleState.READY, "ok")
        PluginHostNotifier.statusChanged("t", cb, st)
        assertEquals(1, cb.statusCalls)
        assertEquals("ok", cb.lastStatus?.message)
    }

    @Test
    fun allNullCallbacks_noopWithoutThrowing() {
        val st = PluginStatusParcel(PluginLifecycleState.READY, "x")
        PluginHostNotifier.settingsValuesChanged("t", null)
        PluginHostNotifier.mainUiStateChanged("t", null)
        PluginHostNotifier.statusChanged("t", null, st)
    }

    @Test
    fun binderStyleTripleEmission_orderIsSettingsMainStatus() {
        val cb = OrderRecordingCallback()
        val status = PluginStatusParcel(PluginLifecycleState.RUNNING, "s")
        PluginHostNotifier.settingsValuesChanged("t", cb)
        PluginHostNotifier.mainUiStateChanged("t", cb)
        PluginHostNotifier.statusChanged("t", cb, status)
        assertEquals(listOf("settings", "main", "status"), cb.order)
    }

    @Test
    fun settingsValuesChanged_swallowsRemoteException() {
        val cb = object : IWearTakHostCallback.Stub() {
            override fun onPluginSettingsValuesChanged() {
                throw RemoteException("dead")
            }

            override fun onSensorEvent(event: SensorEventParcel?) = Unit
            override fun onSensorBatchEvent(events: MutableList<SensorEventParcel>?) = Unit
            override fun onCotReadyEvent(event: CotReadyEventParcel?) = Unit
            override fun onPluginSettingsSchemaChanged() = Unit
            override fun onPluginMainUiSchemaChanged() = Unit
            override fun onPluginMainUiStateChanged() = Unit
            override fun onPluginStatusChanged(status: PluginStatusParcel?) = Unit
            override fun onPluginError(error: com.weartak.plugin.sdk.model.PluginErrorParcel?) = Unit
            override fun onSharedBufferAvailable(buffer: SharedBufferParcel?) = Unit
        }
        PluginHostNotifier.settingsValuesChanged("t", cb)
    }

    @Test
    fun mainUiStateChanged_swallowsRemoteException() {
        val cb = object : IWearTakHostCallback.Stub() {
            override fun onPluginMainUiStateChanged() {
                throw RemoteException("dead")
            }

            override fun onSensorEvent(event: SensorEventParcel?) = Unit
            override fun onSensorBatchEvent(events: MutableList<SensorEventParcel>?) = Unit
            override fun onCotReadyEvent(event: CotReadyEventParcel?) = Unit
            override fun onPluginSettingsSchemaChanged() = Unit
            override fun onPluginSettingsValuesChanged() = Unit
            override fun onPluginMainUiSchemaChanged() = Unit
            override fun onPluginStatusChanged(status: PluginStatusParcel?) = Unit
            override fun onPluginError(error: com.weartak.plugin.sdk.model.PluginErrorParcel?) = Unit
            override fun onSharedBufferAvailable(buffer: SharedBufferParcel?) = Unit
        }
        PluginHostNotifier.mainUiStateChanged("t", cb)
    }

    @Test
    fun statusChanged_swallowsRemoteException() {
        val cb = object : IWearTakHostCallback.Stub() {
            override fun onPluginStatusChanged(status: PluginStatusParcel?) {
                throw RemoteException("dead")
            }

            override fun onSensorEvent(event: SensorEventParcel?) = Unit
            override fun onSensorBatchEvent(events: MutableList<SensorEventParcel>?) = Unit
            override fun onCotReadyEvent(event: CotReadyEventParcel?) = Unit
            override fun onPluginSettingsSchemaChanged() = Unit
            override fun onPluginSettingsValuesChanged() = Unit
            override fun onPluginMainUiSchemaChanged() = Unit
            override fun onPluginMainUiStateChanged() = Unit
            override fun onPluginError(error: com.weartak.plugin.sdk.model.PluginErrorParcel?) = Unit
            override fun onSharedBufferAvailable(buffer: SharedBufferParcel?) = Unit
        }
        PluginHostNotifier.statusChanged(
            "t",
            cb,
            PluginStatusParcel(PluginLifecycleState.READY, "x"),
        )
    }

    private class CountingHostCallback : IWearTakHostCallback.Stub() {
        var settingsValuesCalls = 0
        var mainUiCalls = 0
        var statusCalls = 0
        var lastStatus: PluginStatusParcel? = null

        override fun onPluginSettingsValuesChanged() {
            settingsValuesCalls++
        }

        override fun onPluginMainUiStateChanged() {
            mainUiCalls++
        }

        override fun onPluginStatusChanged(status: PluginStatusParcel?) {
            statusCalls++
            lastStatus = status
        }

        override fun onSensorEvent(event: SensorEventParcel?) = Unit
        override fun onSensorBatchEvent(events: MutableList<SensorEventParcel>?) = Unit
        override fun onCotReadyEvent(event: CotReadyEventParcel?) = Unit
        override fun onPluginSettingsSchemaChanged() = Unit
        override fun onPluginMainUiSchemaChanged() = Unit
        override fun onPluginError(error: com.weartak.plugin.sdk.model.PluginErrorParcel?) = Unit
        override fun onSharedBufferAvailable(buffer: SharedBufferParcel?) = Unit
    }

    private class OrderRecordingCallback : IWearTakHostCallback.Stub() {
        val order = mutableListOf<String>()

        override fun onPluginSettingsValuesChanged() {
            order += "settings"
        }

        override fun onPluginMainUiStateChanged() {
            order += "main"
        }

        override fun onPluginStatusChanged(status: PluginStatusParcel?) {
            order += "status"
        }

        override fun onSensorEvent(event: SensorEventParcel?) = Unit
        override fun onSensorBatchEvent(events: MutableList<SensorEventParcel>?) = Unit
        override fun onCotReadyEvent(event: CotReadyEventParcel?) = Unit
        override fun onPluginSettingsSchemaChanged() = Unit
        override fun onPluginMainUiSchemaChanged() = Unit
        override fun onPluginError(error: com.weartak.plugin.sdk.model.PluginErrorParcel?) = Unit
        override fun onSharedBufferAvailable(buffer: SharedBufferParcel?) = Unit
    }
}
