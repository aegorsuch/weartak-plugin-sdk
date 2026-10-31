package com.weartak.plugin.sdk.routestatus

import android.os.Binder
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.os.RemoteException
import com.weartak.plugin.sdk.model.PluginInitParcel
import com.weartak.plugin.sdk.routestatus.aidl.IPluginRouteStatusControlV1
import com.weartak.plugin.sdk.routestatus.model.PluginRouteStatusParcel
import com.weartak.plugin.sdk.validation.ContractValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RouteStatusContractTest {
    @Test
    fun parcelRoundTripsAllStatesAndNullableText() {
        for (state in RouteConnectionState.UNKNOWN..RouteConnectionState.ERROR) {
            val status = PluginRouteStatusParcel("meshtastic", "Meshtastic", state, null, Long.MAX_VALUE, 1_798_000_000_123L)
            assertEquals(status, roundTrip(status))
            val withText = status.copy(statusText = "3 nodes")
            assertEquals(withText, roundTrip(withText))
        }
    }

    @Test
    fun legacyInitializationYieldsNoReporter() {
        assertNull(PluginRouteStatusReporter.fromInit(roundTrip(PluginInitParcel("host", 1, "plugin"))))
        val flagOnly = Bundle().apply { putBoolean(RouteStatusContract.EXTRA_CAPABILITY, true) }
        assertNull(PluginRouteStatusReporter.fromInit(PluginInitParcel("host", 1, "plugin", flagOnly)))
        val binderOnly = Bundle().apply { putBinder(RouteStatusContract.EXTRA_CONTROL, RecordingHost()) }
        assertNull(PluginRouteStatusReporter.fromInit(PluginInitParcel("host", 1, "plugin", binderOnly)))
        assertNull(PluginRouteStatusReporter.fromInit(null))
    }

    @Test
    fun reporterTravelsThroughExistingInitBundleAndReportsTypedStatus() {
        val host = RecordingHost()
        val extras = Bundle().apply {
            putBoolean(RouteStatusContract.EXTRA_CAPABILITY, true)
            putBinder(RouteStatusContract.EXTRA_CONTROL, host)
        }
        val reporter = PluginRouteStatusReporter.fromInit(roundTrip(PluginInitParcel("host", 1, "plugin", extras)))
        assertNotNull(reporter)
        assertEquals(RouteStatusResultCode.OK, reporter!!.report("meshtastic", " Meshtastic ", RouteConnectionState.CONNECTING, " "))
        assertEquals(RouteStatusResultCode.OK, reporter.report("meshtastic", "Meshtastic", RouteConnectionState.CONNECTED, "3 nodes"))
        val (first, second) = host.reports
        assertEquals("Meshtastic", first.label)
        assertNull(first.statusText)
        assertEquals(RouteConnectionState.CONNECTED, second.state)
        assertEquals("3 nodes", second.statusText)
        assertTrue(second.sequence > first.sequence)
    }

    @Test
    fun generatedProxyTransportsEveryCallAndHostResultCodes() {
        val host = RecordingHost()
        val control = proxy(host)
        assertEquals(RouteStatusContract.CONTRACT_VERSION, control.getContractVersion())
        val reporter = PluginRouteStatusReporter(control) { 42L }
        assertEquals(RouteStatusResultCode.OK, reporter.report("lora.1", "LoRa", RouteConnectionState.ERROR, "Radio not paired"))
        assertEquals(42L, host.reports.single().changedAtEpochMillis)
        assertEquals(RouteStatusResultCode.OK, reporter.remove("lora.1"))
        assertEquals(listOf("lora.1"), host.removed)
        assertEquals(RouteStatusResultCode.OK, reporter.clear())
        assertEquals(1, host.clears)
        host.result = RouteStatusResultCode.UNAUTHORIZED
        assertEquals(RouteStatusResultCode.UNAUTHORIZED, reporter.report("lora.1", "LoRa", RouteConnectionState.CONNECTED))
    }

    @Test
    fun invalidRequestsAreRejectedBeforeTransport() {
        val host = RecordingHost()
        val reporter = PluginRouteStatusReporter(proxy(host))
        assertEquals(RouteStatusResultCode.INVALID_REQUEST, reporter.report("bad id", "Mesh", RouteConnectionState.CONNECTED))
        assertEquals(RouteStatusResultCode.INVALID_REQUEST, reporter.report("x".repeat(65), "Mesh", RouteConnectionState.CONNECTED))
        assertEquals(RouteStatusResultCode.INVALID_REQUEST, reporter.report("mesh", "  ", RouteConnectionState.CONNECTED))
        assertEquals(RouteStatusResultCode.INVALID_REQUEST, reporter.report("mesh", "Me\u0001sh", RouteConnectionState.CONNECTED))
        assertEquals(RouteStatusResultCode.INVALID_REQUEST, reporter.remove(""))
        assertTrue(host.reports.isEmpty())
        assertTrue(host.removed.isEmpty())
    }

    @Test
    fun validatorTruncatesTextAndMapsFutureStatesToUnknown() {
        val raw = PluginRouteStatusParcel("mesh", "L".repeat(200), 99, "s".repeat(500), 1, 1)
        val ok = RouteStatusValidator.validate(raw) as ContractValidationResult.Ok
        assertEquals(RouteStatusContract.MAX_LABEL_BYTES, ok.value.label.length)
        assertEquals(RouteStatusContract.MAX_STATUS_TEXT_BYTES, ok.value.statusText!!.length)
        assertEquals(RouteConnectionState.UNKNOWN, ok.value.state)
        assertTrue(RouteStatusValidator.validate(raw.copy(sequence = -1)) is ContractValidationResult.Err)
        assertTrue(RouteStatusValidator.validate(null) is ContractValidationResult.Err)
    }

    @Test
    fun deadTransportReportsUnavailableNeverOk() {
        val dead = IPluginRouteStatusControlV1.Stub.asInterface(object : Binder() {
            override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
                throw RemoteException("Disconnected host")
            }
        })
        val reporter = PluginRouteStatusReporter(dead)
        assertEquals(RouteStatusResultCode.UNAVAILABLE, reporter.report("mesh", "Mesh", RouteConnectionState.CONNECTED))
        assertEquals(RouteStatusResultCode.UNAVAILABLE, reporter.remove("mesh"))
        assertEquals(RouteStatusResultCode.UNAVAILABLE, reporter.clear())
    }

    private fun proxy(host: RecordingHost): IPluginRouteStatusControlV1 =
        IPluginRouteStatusControlV1.Stub.asInterface(object : Binder() {
            override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean =
                host.transact(code, data, reply, flags)
        })

    private class RecordingHost : IPluginRouteStatusControlV1.Stub() {
        var result = RouteStatusResultCode.OK
        val reports = mutableListOf<PluginRouteStatusParcel>()
        val removed = mutableListOf<String?>()
        var clears = 0

        override fun getContractVersion() = RouteStatusContract.CONTRACT_VERSION

        override fun reportRouteStatus(status: PluginRouteStatusParcel): Int {
            reports += status
            return result
        }

        override fun removeRoute(routeId: String?): Int {
            removed += routeId
            return result
        }

        override fun clearRoutes(): Int {
            clears++
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
