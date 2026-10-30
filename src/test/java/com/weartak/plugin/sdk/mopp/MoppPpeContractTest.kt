package com.weartak.plugin.sdk.mopp

import android.os.Binder
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.os.RemoteException
import com.weartak.plugin.sdk.model.PluginInitParcel
import com.weartak.plugin.sdk.mopp.aidl.IMoppPpeControlV1
import com.weartak.plugin.sdk.mopp.model.MoppPpeResultCode
import com.weartak.plugin.sdk.mopp.model.MoppPpeResultParcel
import com.weartak.plugin.sdk.mopp.model.MoppPpeStateParcel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MoppPpeContractTest {
    @Test
    fun neverSavedAndLegacyUnknownTimeRemainDistinctFromExplicitNone() {
        val neverSaved = MoppPpeStateParcel(0, null, null, true, false, null)
        assertEquals(neverSaved, roundTrip(neverSaved))
        val legacy = neverSaved.copy(selection = MoppPpeContract.SELECTION_NONE)
        assertEquals(legacy, roundTrip(legacy))
        assertNull(roundTrip(legacy).savedAtEpochMillis)
        assertNull(roundTrip(neverSaved).selection)
    }

    @Test
    fun everyPostureAndFullWidthRevisionAndTimestampRoundTrip() {
        for (selection in listOf("none", "0", "1", "2", "3", "4")) {
            val state = MoppPpeStateParcel(
                Long.MAX_VALUE, selection, 1_798_000_000_123L, true, true, "mutation-1",
            )
            val result = MoppPpeResultParcel(true, MoppPpeResultCode.OK, state)
            assertEquals(result, roundTrip(result))
        }
    }

    @Test
    fun explicitFailureCodesAndNullableSnapshotsRoundTrip() {
        val codes = listOf(
            MoppPpeResultCode.REVISION_CONFLICT,
            MoppPpeResultCode.INVALID_REQUEST,
            MoppPpeResultCode.UNAUTHORIZED,
            MoppPpeResultCode.INACTIVE_SESSION,
            MoppPpeResultCode.PERSISTENCE_FAILED,
        )
        assertEquals(codes.size, codes.toSet().size)
        for (code in codes) {
            assertTrue(code != MoppPpeResultCode.OK)
            val result = MoppPpeResultParcel(false, code, null)
            assertEquals(result, roundTrip(result))
        }
    }

    @Test
    fun legacyInitializationHasNoMoppCapabilityOrControl() {
        val init = roundTrip(PluginInitParcel("host", 123, "plugin"))
        assertFalse(init.hostExtras.getBoolean(MoppPpeContract.EXTRA_CAPABILITY))
        assertNull(init.hostExtras.getBinder(MoppPpeContract.EXTRA_CONTROL))
    }

    @Test
    fun optionalControlTravelsThroughExistingInitBundle() {
        val host = RecordingHost()
        val extras = Bundle().apply {
            putBoolean(MoppPpeContract.EXTRA_CAPABILITY, true)
            putBinder(MoppPpeContract.EXTRA_CONTROL, host)
        }
        val init = roundTrip(PluginInitParcel("host", 123, "plugin", extras))
        assertTrue(init.hostExtras.getBoolean(MoppPpeContract.EXTRA_CAPABILITY))
        val control = IMoppPpeControlV1.Stub.asInterface(
            init.hostExtras.getBinder(MoppPpeContract.EXTRA_CONTROL),
        )
        assertEquals(host.result, control.read())
    }

    @Test
    fun generatedProxyTransportsReadSaveAndEnableWithTypedAcknowledgements() {
        val host = RecordingHost()
        val control = proxy(host)
        assertEquals(host.result, control.read())
        assertEquals(host.result, control.save(Long.MAX_VALUE, "save-1", "4"))
        assertEquals(listOf(Long.MAX_VALUE, "save-1", "4"), host.saveArguments)
        assertEquals(host.result, control.setEnabled(42, "enable-1", false))
        assertEquals(listOf(42L, "enable-1", false), host.enableArguments)
        host.result = MoppPpeResultParcel(false, MoppPpeResultCode.UNAUTHORIZED, null)
        assertEquals(host.result, control.read())
    }

    @Test(expected = RemoteException::class)
    fun deadTransportDoesNotBecomeSuccessOrDefaultState() {
        val control = IMoppPpeControlV1.Stub.asInterface(object : Binder() {
            override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
                throw RemoteException("Disconnected host")
            }
        })
        control.read()
    }

    private fun proxy(host: RecordingHost): IMoppPpeControlV1 =
        IMoppPpeControlV1.Stub.asInterface(object : Binder() {
            override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean =
                host.transact(code, data, reply, flags)
        })

    private class RecordingHost : IMoppPpeControlV1.Stub() {
        var result = MoppPpeResultParcel(
            true, MoppPpeResultCode.OK,
            MoppPpeStateParcel(42, "none", null, true, false, "previous"),
        )
        var saveArguments: List<Any?>? = null
        var enableArguments: List<Any?>? = null

        override fun read() = result

        override fun save(expectedRevision: Long, mutationId: String?, selection: String?): MoppPpeResultParcel {
            saveArguments = listOf(expectedRevision, mutationId, selection)
            return result
        }

        override fun setEnabled(expectedRevision: Long, mutationId: String?, enabled: Boolean): MoppPpeResultParcel {
            enableArguments = listOf(expectedRevision, mutationId, enabled)
            return result
        }
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
