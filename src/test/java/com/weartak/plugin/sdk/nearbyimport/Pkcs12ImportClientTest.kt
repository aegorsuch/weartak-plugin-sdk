package com.weartak.plugin.sdk.nearbyimport

import android.os.RemoteException
import android.os.SystemClock
import com.weartak.plugin.sdk.nearbyimport.aidl.IWearTakPkcs12ImportControlV1
import com.weartak.plugin.sdk.nearbyimport.model.Pkcs12CancelResultParcel
import com.weartak.plugin.sdk.nearbyimport.model.Pkcs12ImportCapabilityParcel
import com.weartak.plugin.sdk.nearbyimport.model.Pkcs12ImportStatusParcel
import com.weartak.plugin.sdk.nearbyimport.model.Pkcs12ReviewResultParcel
import com.weartak.plugin.sdk.nearbyimport.model.Pkcs12StageRequestParcel
import com.weartak.plugin.sdk.nearbyimport.model.Pkcs12StageResultParcel
import com.weartak.plugin.sdk.v3.aidl.IWearTakPcmCallback
import com.weartak.plugin.sdk.v3.aidl.IWearTakPluginHostControlV3
import com.weartak.plugin.sdk.v3.model.HostCapabilitiesV3Parcel
import com.weartak.plugin.sdk.v3.model.PcmSubscriptionRequestParcel
import com.weartak.plugin.sdk.v3.model.PcmSubscriptionResultParcel
import com.weartak.plugin.sdk.v3.model.PcmSubscriptionStatus
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class Pkcs12ImportClientTest {
    @Test
    fun discover_requiresExactKeyVersionAndBounds() {
        val control = RecordingImportControl()
        val supported = Pkcs12ImportClient.discover(host(capability(), control))
        assertTrue(supported is Pkcs12ImportDiscovery.Supported)

        assertEquals(
            Pkcs12ImportDiscovery.Unsupported,
            Pkcs12ImportClient.discover(
                host(capability().copy(apiVersion = 2), control),
            ),
        )
        assertEquals(
            Pkcs12ImportDiscovery.InvalidCapability,
            Pkcs12ImportClient.discover(
                host(capability().copy(maxPayloadBytes = Pkcs12ImportContract.MAX_PAYLOAD_BYTES + 1), control),
            ),
        )
    }

    @Test
    fun stage_wipesCallerBytesOnlyAfterConfirmedAdmission() {
        val bytes = byteArrayOf(1, 2, 3)
        val control = RecordingImportControl(
            stageResult = Pkcs12StageResultParcel(
                Pkcs12StageCode.STAGED,
                handle = "abcdefghijklmnopqrstuv",
                expiresAtElapsedRealtimeMs = SystemClock.elapsedRealtime() + 60_000L,
            ),
        )

        val result = Pkcs12ImportClient.stage(supported(control), "request-1", bytes)

        assertEquals(Pkcs12StageCode.STAGED, result.code)
        assertArrayEquals(byteArrayOf(0, 0, 0), bytes)
        assertEquals("request-1", control.request?.requestId)
        assertArrayEquals(byteArrayOf(1, 2, 3), control.receivedBytes)
        assertArrayEquals(byteArrayOf(0, 0, 0), control.request?.pkcs12Bytes)
    }

    @Test
    fun malformedAdmissionReceiptDoesNotWipeCallerBytes() {
        val bytes = byteArrayOf(3, 2, 1)
        val control = RecordingImportControl(
            stageResult = Pkcs12StageResultParcel(Pkcs12StageCode.STAGED),
        )

        try {
            Pkcs12ImportClient.stage(supported(control), "request-invalid", bytes)
            throw AssertionError("Expected malformed receipt to be rejected")
        } catch (_: IllegalStateException) {
            assertArrayEquals(byteArrayOf(3, 2, 1), bytes)
        }
    }

    @Test
    fun stageExpiryCannotExceedNegotiatedLease() {
        val bytes = byteArrayOf(6, 5, 4)
        val control = RecordingImportControl(
            stageResult = Pkcs12StageResultParcel(
                Pkcs12StageCode.STAGED,
                handle = "abcdefghijklmnopqrstuv",
                expiresAtElapsedRealtimeMs = SystemClock.elapsedRealtime() + 60_000L,
            ),
        )

        try {
            Pkcs12ImportClient.stage(
                supported(control, maxStageLifetimeMs = 5_000L),
                "request-expiry",
                bytes,
            )
            throw AssertionError("Expected lease above negotiated limit to be rejected")
        } catch (_: IllegalStateException) {
            assertArrayEquals(byteArrayOf(6, 5, 4), bytes)
        }
    }

    @Test
    fun rejectedOrInvalidStagePreservesBytesAndDoesNotCallHost() {
        val bytes = byteArrayOf(9, 8, 7)
        val rejected = RecordingImportControl(
            stageResult = Pkcs12StageResultParcel(Pkcs12StageCode.TOO_LARGE),
        )

        Pkcs12ImportClient.stage(supported(rejected), "request-2", bytes)
        assertArrayEquals(byteArrayOf(9, 8, 7), bytes)

        val oversized = byteArrayOf(1, 2, 3)
        try {
            Pkcs12ImportClient.stage(supported(rejected).copy(maxPayloadBytes = 2), "request-3", oversized)
            throw AssertionError("Expected oversized request to be rejected")
        } catch (_: IllegalArgumentException) {
            assertArrayEquals(byteArrayOf(1, 2, 3), oversized)
            assertEquals(1, rejected.stageCalls)
        }
    }

    @Test
    fun transportFailurePreservesBytes() {
        val bytes = byteArrayOf(4, 5, 6)
        val control = RecordingImportControl(throwOnStage = true)

        try {
            Pkcs12ImportClient.stage(supported(control), "request-4", bytes)
            throw AssertionError("Expected transport failure")
        } catch (_: RemoteException) {
            assertArrayEquals(byteArrayOf(4, 5, 6), bytes)
            assertArrayEquals(byteArrayOf(4, 5, 6), control.receivedBytes)
            assertArrayEquals(byteArrayOf(0, 0, 0), control.request?.pkcs12Bytes)
        }
    }

    @Test
    fun requestIdsAreFreshUuidValues() {
        assertTrue(Pkcs12ImportClient.newRequestId() != Pkcs12ImportClient.newRequestId())
    }

    @Test
    fun maximumPayloadIsAcceptedAndOneByteOverIsRejected() {
        val control = RecordingImportControl()
        val maximum = ByteArray(Pkcs12ImportContract.MAX_PAYLOAD_BYTES)
        Pkcs12ImportClient.stage(supported(control), "request-5", maximum)
        assertEquals(1, control.stageCalls)

        val oversized = ByteArray(Pkcs12ImportContract.MAX_PAYLOAD_BYTES + 1)
        try {
            Pkcs12ImportClient.stage(supported(control), "request-6", oversized)
            throw AssertionError("Expected 32,701-byte request to be rejected")
        } catch (_: IllegalArgumentException) {
            assertEquals(1, control.stageCalls)
        }
    }

    private fun capability() = Pkcs12ImportCapabilityParcel(
        capabilityKey = Pkcs12ImportContract.CAPABILITY_KEY,
        apiVersion = Pkcs12ImportContract.API_VERSION,
        maxPayloadBytes = Pkcs12ImportContract.MAX_PAYLOAD_BYTES,
        maxStageLifetimeMs = Pkcs12ImportContract.MAX_STAGE_LIFETIME_MS,
    )

    private fun supported(
        control: IWearTakPkcs12ImportControlV1,
        maxPayloadBytes: Int = Pkcs12ImportContract.MAX_PAYLOAD_BYTES,
        maxStageLifetimeMs: Long = Pkcs12ImportContract.MAX_STAGE_LIFETIME_MS,
    ) = Pkcs12ImportDiscovery.Supported(control, maxPayloadBytes, maxStageLifetimeMs)

    private fun host(
        capability: Pkcs12ImportCapabilityParcel,
        control: IWearTakPkcs12ImportControlV1,
    ): IWearTakPluginHostControlV3 =
        object : IWearTakPluginHostControlV3.Stub() {
            override fun subscribePcm(
                request: PcmSubscriptionRequestParcel?,
                callback: IWearTakPcmCallback?,
            ) = PcmSubscriptionResultParcel(false, status = PcmSubscriptionStatus.NOT_SUPPORTED)

            override fun unsubscribePcm(subscriptionId: String?) =
                PcmSubscriptionResultParcel(true, subscriptionId)

            override fun getHostCapabilities() = HostCapabilitiesV3Parcel(contractApiVersion = 3)

            override fun getPkcs12ImportCapability() = capability

            override fun getPkcs12ImportControlV1() = control
            override fun getServerPackageCapability() = null
            override fun getServerPackageControlV1() = null
        }

    private class RecordingImportControl(
        var stageResult: Pkcs12StageResultParcel =
            Pkcs12StageResultParcel(Pkcs12StageCode.UNSUPPORTED),
        private val throwOnStage: Boolean = false,
    ) : IWearTakPkcs12ImportControlV1.Stub() {
        var stageCalls = 0
        var request: Pkcs12StageRequestParcel? = null
        var receivedBytes: ByteArray? = null

        override fun stagePkcs12(request: Pkcs12StageRequestParcel?): Pkcs12StageResultParcel {
            this.request = request
            receivedBytes = request?.pkcs12Bytes?.copyOf()
            if (throwOnStage) throw RemoteException("transport failure")
            stageCalls++
            return stageResult
        }

        override fun requestReview(handle: String?) =
            Pkcs12ReviewResultParcel(Pkcs12ImportState.REVIEW_PENDING)

        override fun getStatus(handle: String?) = Pkcs12ImportStatusParcel(Pkcs12ImportState.STAGED)

        override fun cancel(handle: String?) = Pkcs12CancelResultParcel(Pkcs12ImportState.CANCELLED)
    }
}
