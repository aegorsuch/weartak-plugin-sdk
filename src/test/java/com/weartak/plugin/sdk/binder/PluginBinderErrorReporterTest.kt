package com.weartak.plugin.sdk.binder

import android.os.RemoteException
import com.weartak.plugin.sdk.aidl.IWearTakHostCallback
import com.weartak.plugin.sdk.model.CotReadyEventParcel
import com.weartak.plugin.sdk.model.PluginErrorCodes
import com.weartak.plugin.sdk.model.PluginErrorParcel
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
class PluginBinderErrorReporterTest {

    @Test
    fun report_deliversToCallback() {
        val cb = RecordingHostCallback()
        PluginBinderErrorReporter.report(
            tag = "t",
            operation = "op",
            error = PluginErrorParcel(PluginErrorCodes.VALIDATION_FAILED, "bad", false),
            callback = cb,
        )
        assertEquals(1, cb.errors.size)
        assertEquals("bad", cb.errors.first().message)
    }

    @Test
    fun report_noopsWithoutCallback() {
        PluginBinderErrorReporter.report(
            tag = "t",
            operation = "op",
            error = PluginErrorParcel(1, "x", false),
            callback = null,
        )
    }

    @Test
    fun validationFailed_helper() {
        val cb = RecordingHostCallback()
        PluginBinderErrorReporter.validationFailed("t", "apply", "msg", cb)
        assertEquals(PluginErrorCodes.VALIDATION_FAILED, cb.errors.single().code)
    }

    @Test
    fun report_swallowsRemoteExceptionFromHost() {
        val cb = object : IWearTakHostCallback.Stub() {
            override fun onPluginError(error: PluginErrorParcel) {
                throw RemoteException("host died")
            }

            override fun onSensorEvent(event: SensorEventParcel?) = Unit
            override fun onSensorBatchEvent(events: MutableList<SensorEventParcel>?) = Unit
            override fun onCotReadyEvent(event: CotReadyEventParcel?) = Unit
            override fun onPluginSettingsSchemaChanged() = Unit
            override fun onPluginSettingsValuesChanged() = Unit
            override fun onPluginMainUiSchemaChanged() = Unit
            override fun onPluginMainUiStateChanged() = Unit
            override fun onPluginStatusChanged(status: PluginStatusParcel?) = Unit
            override fun onSharedBufferAvailable(buffer: SharedBufferParcel?) = Unit
        }
        PluginBinderErrorReporter.report(
            tag = "t",
            operation = "op",
            error = PluginErrorParcel(1, "x", false),
            callback = cb,
        )
    }

    @Test
    fun validationFailed_swallowsRemoteExceptionFromHost() {
        val cb = object : IWearTakHostCallback.Stub() {
            override fun onPluginError(error: PluginErrorParcel) {
                throw RemoteException("host died")
            }

            override fun onSensorEvent(event: SensorEventParcel?) = Unit
            override fun onSensorBatchEvent(events: MutableList<SensorEventParcel>?) = Unit
            override fun onCotReadyEvent(event: CotReadyEventParcel?) = Unit
            override fun onPluginSettingsSchemaChanged() = Unit
            override fun onPluginSettingsValuesChanged() = Unit
            override fun onPluginMainUiSchemaChanged() = Unit
            override fun onPluginMainUiStateChanged() = Unit
            override fun onPluginStatusChanged(status: PluginStatusParcel?) = Unit
            override fun onSharedBufferAvailable(buffer: SharedBufferParcel?) = Unit
        }
        PluginBinderErrorReporter.validationFailed("t", "apply", "bad", cb)
    }

    private class RecordingHostCallback : IWearTakHostCallback.Stub() {
        val errors = mutableListOf<PluginErrorParcel>()

        override fun onPluginError(error: PluginErrorParcel) {
            errors += error
        }

        override fun onSensorEvent(event: SensorEventParcel?) = Unit
        override fun onSensorBatchEvent(events: MutableList<SensorEventParcel>?) = Unit
        override fun onCotReadyEvent(event: CotReadyEventParcel?) = Unit
        override fun onPluginSettingsSchemaChanged() = Unit
        override fun onPluginSettingsValuesChanged() = Unit
        override fun onPluginMainUiSchemaChanged() = Unit
        override fun onPluginMainUiStateChanged() = Unit
        override fun onPluginStatusChanged(status: PluginStatusParcel?) = Unit
        override fun onSharedBufferAvailable(buffer: SharedBufferParcel?) = Unit
    }
}
