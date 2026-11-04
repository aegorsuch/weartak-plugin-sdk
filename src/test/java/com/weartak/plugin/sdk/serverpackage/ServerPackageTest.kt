package com.weartak.plugin.sdk.serverpackage

import android.os.RemoteException
import android.os.SystemClock
import com.weartak.plugin.sdk.nearbyimport.Pkcs12ImportState
import com.weartak.plugin.sdk.nearbyimport.Pkcs12StageCode
import com.weartak.plugin.sdk.nearbyimport.model.Pkcs12CancelResultParcel
import com.weartak.plugin.sdk.nearbyimport.model.Pkcs12ImportStatusParcel
import com.weartak.plugin.sdk.nearbyimport.model.Pkcs12ReviewResultParcel
import com.weartak.plugin.sdk.nearbyimport.model.Pkcs12StageResultParcel
import com.weartak.plugin.sdk.serverpackage.aidl.IWearTakServerPackageControlV1
import com.weartak.plugin.sdk.serverpackage.model.ServerPackageCapabilityParcel
import com.weartak.plugin.sdk.serverpackage.model.ServerPackageExportPayloadParcel
import com.weartak.plugin.sdk.serverpackage.model.ServerPackageExportResultParcel
import com.weartak.plugin.sdk.serverpackage.model.ServerPackageExportStatusParcel
import com.weartak.plugin.sdk.serverpackage.model.ServerPackageStageRequestParcel
import com.weartak.plugin.sdk.v3.aidl.IWearTakPcmCallback
import com.weartak.plugin.sdk.v3.aidl.IWearTakPluginHostControlV3
import com.weartak.plugin.sdk.v3.model.HostCapabilitiesV3Parcel
import com.weartak.plugin.sdk.v3.model.PcmSubscriptionRequestParcel
import com.weartak.plugin.sdk.v3.model.PcmSubscriptionResultParcel
import com.weartak.plugin.sdk.v3.model.PcmSubscriptionStatus
import java.util.Base64
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ServerPackageTest {
    private val ca1 = b64(
        "MIIBXDCCAQGgAwIBAgIJANPbB066bow1MAoGCCqGSM49BAMCMBgxFjAUBgNVBAMTDVdUU1AgVGVzdCBjYTEwIBcNMjYxMTA0MTM0NjAyWhgPMjEyNjEwMTExMzQ2MDJaMBgxFjAUBgNVBAMTDVdUU1AgVGVzdCBjYTEwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAARGUkuY8ug7aLhRLDhGUPCsXkdZzvjDYYe1ORa1sYpaPfrraX7iLtw3yUCHnoLwD6m/lpPnCbaNdlkf+grOFy4IozIwMDAdBgNVHQ4EFgQUUwXXefNXHPRt/15bDnS1I5eiE6wwDwYDVR0TAQH/BAUwAwEB/zAKBggqhkjOPQQDAgNJADBGAiEAthPEe76mlcDe+J8wxj/tVirPc6z0Es5WIicShKhQyBoCIQD06tJlgsa0yj3H666gY11r8lLw5Kk2g+y+FscOkt3tjw==",
    )
    private val ca2 = b64(
        "MIIBWzCCAQGgAwIBAgIJAP+aYE0GoFCKMAoGCCqGSM49BAMCMBgxFjAUBgNVBAMTDVdUU1AgVGVzdCBjYTIwIBcNMjYxMTA0MTM0NjAzWhgPMjEyNjEwMTExMzQ2MDNaMBgxFjAUBgNVBAMTDVdUU1AgVGVzdCBjYTIwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAAT0a53rf5aN4A/9GqeJA1vDLdBif8Po3QKIpDxmvfZd6Kiz2MIG2rxN//jT9jHoK5rmpUoDCD+4kB1a8rKcdDLsozIwMDAdBgNVHQ4EFgQU7gv7e7Hy1PsEmQ2MpAUL2DzQnyowDwYDVR0TAQH/BAUwAwEB/zAKBggqhkjOPQQDAgNIADBFAiAUmrMpjUfhpBtHN+g7452dLT4NGG9YzHQbt3w8cZt7mwIhAPOBGwxrMUfA52wUc2osnTXBcZnH9lq/cKUkKQiZEWAw",
    )
    private val leaf = b64(
        "MIIBVzCB/aADAgECAgkAg3soR/C4arkwCgYIKoZIzj0EAwIwGTEXMBUGA1UEAxMOV1RTUCBUZXN0IGxlYWYwIBcNMjYxMTA0MTM0NjAzWhgPMjEyNjEwMTExMzQ2MDNaMBkxFzAVBgNVBAMTDldUU1AgVGVzdCBsZWFmMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAECzPbBwCiFaBILXxtS4kx+JrXD97lKZHp7KP9UoL70q8vkDPgkM2Wf8PyHKTjK8X70yhOCuyv0TXvIPMi5BaxaKMsMCowHQYDVR0OBBYEFDz9OWoig7efuURzHv8sfgxqRbz4MAkGA1UdEwQCMAAwCgYIKoZIzj0EAwIDSQAwRgIhAMYc3G4ZBzXq6n2361DWuOzcmDaFeobTUa0ace3tWHsIAiEA2icQ3aNAzZ+jp+oZHYP4UyE5sQw3o0lxgjtCnUnwfwI=",
    )

    @Test
    fun goldenVector_publicEnrollment_isByteExact() {
        val expected = hex(
            "5754535001" + "01000141" + "02000b74616b2e6578616d706c65" + "0300021f99" +
                "04000101" + "05000101" + "06000101" + "07000100",
        )
        val encoded = ServerPackageCodec.encode(publicPackage(name = "A"))
        assertArrayEquals(expected, encoded)
        val decoded = ServerPackageCodec.decode(expected) as ServerPackageDecodeResult.Valid
        assertEquals("tak.example", decoded.serverPackage.host)
        assertEquals(8089, decoded.serverPackage.port)
    }

    @Test
    fun privateCa_roundTrips_andCaRulesEnforced() {
        val pkg = ServerPackage(
            "Ops", "10.0.0.5", 8089, ServerPackageTrustMode.PRIVATE_CA,
            ServerPackageIdentityMode.ADMIN_PKCS12_SEPARATE, listOf(ca1, ca2),
        )
        val decoded = ServerPackageCodec.decode(ServerPackageCodec.encode(pkg)) as ServerPackageDecodeResult.Valid
        assertEquals(2, decoded.serverPackage.caCertificates.size)
        assertArrayEquals(ca2, decoded.serverPackage.caCertificates[1])

        assertEquals(ServerPackageError.CA_REQUIRED, err(pkg.with(cas = emptyList())))
        assertEquals(ServerPackageError.CA_NOT_ALLOWED, err(publicPackage(cas = listOf(ca1))))
        assertEquals(ServerPackageError.INVALID_CA, err(pkg.with(cas = listOf(leaf))))
        assertEquals(ServerPackageError.INVALID_CA, err(pkg.with(cas = listOf(byteArrayOf(0x30, 0x00)))))
        assertEquals(ServerPackageError.DUPLICATE_CA, err(pkg.with(cas = listOf(ca1, ca1))))
        assertEquals(ServerPackageError.TOO_MANY_CA, err(pkg.with(cas = listOf(ca1, ca2, ca1, ca2, ca1))))
        assertEquals(ServerPackageError.INVALID_TRUST_MODE, err(pkg.with(trust = 0)))
        assertEquals(ServerPackageError.INVALID_IDENTITY_MODE, err(pkg.with(identity = 4)))
    }

    @Test
    fun hostAndNameValidation_rejectsNonHostInput() {
        listOf("tak.example", "a", "10.1.2.3", "::1", "fe80::1", "2001:db8::10.0.0.1", "1:2:3:4:5:6:7:8", "xn--bcher-kva.example")
            .forEach { assertTrue(it, ServerHostValidator.isValidHost(it)) }
        listOf(
            "", "TAK.example", "https://tak.example", "tak.example:8089", "tak.example/path", "user@tak.example",
            "tak.example?x", "[::1]", "fe80::1%wlan0", "256.1.1.1", "01.2.3.4", "1.2.3", "-bad.example",
            "bad-.example", "a..b", "tak example", "1:2:3:4:5:6:7:8:9", "1::2::3", "a".repeat(64) + ".x",
            "a." + "b".repeat(252), "123",
        ).forEach { assertFalse(it, ServerHostValidator.isValidHost(it)) }

        assertEquals(null, ServerPackageCodec.validateFields(publicPackage(name = "é".repeat(64))))
        assertEquals(ServerPackageError.INVALID_NAME, err(publicPackage(name = "é".repeat(64) + "a")))
        listOf("", " lead", "trail ", "a\nb", "a\u0000b", "a\u202eb").forEach {
            assertEquals(it, ServerPackageError.INVALID_NAME, err(publicPackage(name = it)))
        }
        assertEquals(ServerPackageError.INVALID_PORT, err(publicPackage(port = 0)))
        assertEquals(ServerPackageError.INVALID_PORT, err(publicPackage(port = 65_536)))
    }

    @Test
    fun decoder_rejectsNonCanonicalOrHostileBytes() {
        val good = ServerPackageCodec.encode(publicPackage())
        assertEquals(ServerPackageError.EMPTY, decodeErr(ByteArray(0)))
        assertEquals(ServerPackageError.TOO_LARGE, decodeErr(ByteArray(ServerPackageContract.MAX_PACKAGE_BYTES + 1)))
        assertEquals(ServerPackageError.BAD_MAGIC, decodeErr(good.copyOf().also { it[0] = 0 }))
        assertEquals(ServerPackageError.UNSUPPORTED_VERSION, decodeErr(good.copyOf().also { it[4] = 2 }))
        assertEquals(ServerPackageError.TRUNCATED, decodeErr(good.copyOf(good.size - 1)))
        assertEquals(ServerPackageError.TRUNCATED, decodeErr(good + byteArrayOf(7)))
        assertEquals(ServerPackageError.UNKNOWN_TAG, decodeErr(good + hex("0d0000")))
        assertEquals(ServerPackageError.TAG_ORDER, decodeErr(good + hex("07000100")))
        assertEquals(ServerPackageError.MISSING_FIELD, decodeErr(hex("575453500101000141" + "030002" + "1f99")))
        assertEquals(ServerPackageError.MISSING_FIELD, decodeErr(good.copyOf(good.size - 4)))
        assertEquals(ServerPackageError.INVALID_FLAGS, decodeErr(good.copyOf().also { it[it.size - 1] = 4 }))
        assertEquals(ServerPackageError.FLAGS_MISMATCH, decodeErr(good.copyOf().also { it[it.size - 1] = 2 }))
        assertEquals(ServerPackageError.TAG_ORDER, decodeErr(good + hex("0c000161" + "0b000161")))
        assertEquals(ServerPackageError.TAG_ORDER, decodeErr(good + hex("0c000161" + "0c000161")))
        // Protocol byte 2 (plain TCP/other) must not downgrade.
        assertEquals(ServerPackageError.UNSUPPORTED_PROTOCOL, decodeErr(good.copyOf().also { it[it.size - 13] = 2 }))
        assertEquals(ServerPackageError.UNSUPPORTED_PROTOCOL, decodeErr(good.copyOf().also { it[it.size - 13] = 0 }))
        assertEquals(ServerPackageError.INVALID_HOST, decodeErr(hex("5754535001" + "01000141" + "020001ff" + "0300021f9904000101050001010600010107000100")))
        assertEquals(ServerPackageError.INVALID_NAME, decodeErr(hex("5754535001" + "010001ff" + "020001" + "61" + "0300021f9904000101050001010600010107000100")))
        assertEquals(ServerPackageError.BAD_LENGTH, decodeErr(hex("5754535001" + "01000141" + "02000161" + "0300011f" + "04000101050001010600010107000100")))
    }

    @Test
    fun stage_wipesCallerOnlyOnValidStagedReceipt_andAlwaysZeroesTemporaryCopy() {
        val control = FakeControl(stageResult = staged())
        val bytes = ServerPackageCodec.encode(publicPackage())
        val original = bytes.copyOf()
        val result = ServerPackageClient.stage(supported(control), "req-1", bytes)
        assertEquals(Pkcs12StageCode.STAGED, result.code)
        assertArrayEquals(original, control.receivedCopy)
        assertTrue(control.request!!.encodedPackage.all { it == 0.toByte() })
        assertTrue(bytes.all { it == 0.toByte() })

        val rejectBytes = original.copyOf()
        ServerPackageClient.stage(supported(FakeControl(stageResult = Pkcs12StageResultParcel(Pkcs12StageCode.HOST_BUSY))), "req-2", rejectBytes)
        assertArrayEquals(original, rejectBytes)

        val malformed = original.copyOf()
        runCatching {
            ServerPackageClient.stage(supported(FakeControl(stageResult = Pkcs12StageResultParcel(Pkcs12StageCode.STAGED, "bad", SystemClock.elapsedRealtime() + 1000))), "req-3", malformed)
        }.also { assertTrue(it.isFailure) }
        assertArrayEquals(original, malformed)

        val throwing = FakeControl(throwOnStage = true)
        val remoteBytes = original.copyOf()
        runCatching { ServerPackageClient.stage(supported(throwing), "req-4", remoteBytes) }
            .also { assertTrue(it.exceptionOrNull() is RemoteException) }
        assertArrayEquals(original, remoteBytes)
        assertTrue(throwing.request!!.encodedPackage.all { it == 0.toByte() })
    }

    @Test
    fun stage_rejectsInvalidPackageWithoutCallingHost() {
        val control = FakeControl(stageResult = staged())
        val hostile = hex("575453500201000141")
        val result = runCatching { ServerPackageClient.stage(supported(control), "req", hostile) }
        assertEquals(ServerPackageError.UNSUPPORTED_VERSION, (result.exceptionOrNull() as ServerPackageException).error)
        assertEquals(0, control.stageCalls)
        val noReceive = supported(control).copy(supportsReceive = false)
        assertTrue(runCatching { ServerPackageClient.stage(noReceive, "req", ServerPackageCodec.encode(publicPackage())) }.isFailure)
        assertEquals(0, control.stageCalls)
    }

    @Test
    fun export_validatesReceiptAndTakenBytes() {
        val now = SystemClock.elapsedRealtime()
        val control = FakeControl(
            exportResult = ServerPackageExportResultParcel(ServerPackageExportCode.PENDING, HANDLE, now + 1000),
            takePayload = ServerPackageExportPayloadParcel(ServerPackageTakeCode.OK, ServerPackageCodec.encode(publicPackage())),
        )
        val supported = supported(control)
        assertEquals(ServerPackageExportCode.PENDING, ServerPackageClient.requestExport(supported, "exp-1").code)
        val ready = ServerPackageClient.takeExport(supported, HANDLE) as ServerPackageTakeResult.Ready
        assertEquals("tak.example", ready.serverPackage.host)

        val tooLong = FakeControl(exportResult = ServerPackageExportResultParcel(ServerPackageExportCode.PENDING, HANDLE, now + 400_000))
        assertTrue(runCatching { ServerPackageClient.requestExport(supported(tooLong), "exp-2") }.isFailure)

        val bad = hex("5754535001010001410200")
        val badControl = FakeControl(takePayload = ServerPackageExportPayloadParcel(ServerPackageTakeCode.OK, bad))
        assertTrue(ServerPackageClient.takeExport(supported(badControl), HANDLE) is ServerPackageTakeResult.Invalid)
        assertTrue(bad.all { it == 0.toByte() })

        val notReady = FakeControl(takePayload = ServerPackageExportPayloadParcel(ServerPackageTakeCode.NOT_READY))
        assertEquals(ServerPackageTakeResult.HostCode(ServerPackageTakeCode.NOT_READY), ServerPackageClient.takeExport(supported(notReady), HANDLE))
        assertTrue(runCatching { ServerPackageClient.takeExport(supported, "../foreign") }.isFailure)
        val noExport = supported.copy(supportsExport = false)
        assertTrue(runCatching { ServerPackageClient.requestExport(noExport, "exp-3") }.isFailure)
    }

    @Test
    fun discover_requiresExactKeyVersionBoundsAndControl() {
        val control = FakeControl()
        assertTrue(ServerPackageClient.discover(host(capability(), control)) is ServerPackageDiscovery.Supported)
        assertEquals(ServerPackageDiscovery.Unavailable, ServerPackageClient.discover(null))
        assertEquals(ServerPackageDiscovery.Unsupported, ServerPackageClient.discover(host(null, control)))
        assertEquals(ServerPackageDiscovery.Unsupported, ServerPackageClient.discover(host(ServerPackageCapabilityParcel(), control)))
        assertEquals(ServerPackageDiscovery.Unsupported, ServerPackageClient.discover(host(capability().copy(apiVersion = 2), control)))
        assertEquals(ServerPackageDiscovery.Unsupported, ServerPackageClient.discover(host(capability().copy(capabilityKey = "x"), control)))
        assertEquals(ServerPackageDiscovery.InvalidCapability, ServerPackageClient.discover(host(capability().copy(maxPackageBytes = 32_701), control)))
        assertEquals(ServerPackageDiscovery.InvalidCapability, ServerPackageClient.discover(host(capability().copy(supportsReceive = false, supportsExport = false), control)))
        assertEquals(ServerPackageDiscovery.InvalidCapability, ServerPackageClient.discover(host(capability(), null)))
    }

    @Test
    fun sharedIdentityPackage_requiresExplicitCompleteSensitiveFields() {
        val p12 = byteArrayOf(0x30) + ByteArray(2_000) { 7 }
        val pkg = ServerPackage(
            "Event", "tak.example", 8089, ServerPackageTrustMode.PRIVATE_CA, ServerPackageIdentityMode.SHARED_IDENTITY,
            listOf(ca1), p12.copyOf(), "p12-pass".toByteArray(), "event-user", "s3cret pass".toByteArray(),
        )
        assertEquals(ServerPackageSensitiveFlags.ALL, pkg.sensitiveFlags)
        val bytes = ServerPackageCodec.encode(pkg)
        assertEquals(null, ServerPackageCodec.validate(bytes))
        val decoded = (ServerPackageCodec.decode(bytes) as ServerPackageDecodeResult.Valid).serverPackage
        assertArrayEquals(p12, decoded.clientPkcs12)
        assertEquals("event-user", decoded.username)
        assertEquals("ServerPackage(redacted)", decoded.toString())
        decoded.wipe()
        assertTrue(decoded.clientPkcs12!!.all { it == 0.toByte() } && decoded.password!!.all { it == 0.toByte() })

        fun shared(
            identity: Int = ServerPackageIdentityMode.SHARED_IDENTITY,
            clientP12: ByteArray? = p12,
            p12Pass: ByteArray? = "x".toByteArray(),
            user: String? = null,
            pass: ByteArray? = null,
        ) = ServerPackage("E", "tak.example", 8089, ServerPackageTrustMode.PUBLIC_CA, identity, emptyList(), clientP12, p12Pass, user, pass)

        assertEquals(null, err(shared()))
        assertEquals(null, err(shared(identity = ServerPackageIdentityMode.RECEIVER_ENROLLMENT, clientP12 = null, p12Pass = null, user = "u", pass = "p".toByteArray())))
        assertEquals(ServerPackageError.IDENTITY_MISMATCH, err(shared(clientP12 = null, p12Pass = null)))
        assertEquals(ServerPackageError.IDENTITY_MISMATCH, err(shared(identity = ServerPackageIdentityMode.RECEIVER_ENROLLMENT)))
        assertEquals(ServerPackageError.IDENTITY_MISMATCH, err(shared(identity = ServerPackageIdentityMode.ADMIN_PKCS12_SEPARATE)))
        assertEquals(ServerPackageError.FLAGS_MISMATCH, err(shared(p12Pass = null)))
        assertEquals(ServerPackageError.FLAGS_MISMATCH, err(shared(user = "u")))
        assertEquals(ServerPackageError.FLAGS_MISMATCH, err(shared(pass = "p".toByteArray())))
        assertEquals(ServerPackageError.INVALID_CLIENT_PKCS12, err(shared(clientP12 = byteArrayOf(1, 2))))
        assertEquals(ServerPackageError.INVALID_SECRET, err(shared(p12Pass = ByteArray(0))))
        assertEquals(ServerPackageError.INVALID_SECRET, err(shared(p12Pass = ByteArray(257) { 0x61 })))
        assertEquals(ServerPackageError.INVALID_SECRET, err(shared(p12Pass = byteArrayOf(0x61, 0x00))))
        assertEquals(ServerPackageError.INVALID_SECRET, err(shared(p12Pass = byteArrayOf(0xc3.toByte()))))
        assertEquals(ServerPackageError.INVALID_USERNAME, err(shared(user = "a\nb", pass = "p".toByteArray())))

        val maxP12 = byteArrayOf(0x30) + ByteArray(32_700 - 40) { 1 }
        val tooLarge = runCatching { ServerPackageCodec.encode(shared(clientP12 = maxP12)) }.exceptionOrNull()
        assertEquals(ServerPackageError.TOO_LARGE, (tooLarge as ServerPackageException).error)
        val fits = byteArrayOf(0x30) + ByteArray(32_700 - 5 - 3 * 9 - 1 - 11 - 2 - 4 - 1 - 1) { 1 }
        assertEquals(32_700, ServerPackageCodec.encode(shared(clientP12 = fits)).size)
    }

    @Test
    fun lifecycleStates_distinguishConfigurationAppliedFromNetworkOutcome() {
        val states = setOf(
            Pkcs12ImportState.STAGED, Pkcs12ImportState.REVIEW_PENDING, Pkcs12ImportState.IN_REVIEW,
            Pkcs12ImportState.REJECTED, Pkcs12ImportState.CANCELLED, Pkcs12ImportState.EXPIRED,
            Pkcs12ImportState.APPLIED, Pkcs12ImportState.ENROLLED, Pkcs12ImportState.CONNECTED,
            Pkcs12ImportState.NETWORK_FAILED,
        )
        assertEquals(10, states.size)
        assertEquals(7, Pkcs12ImportState.APPLIED)
    }

    private fun err(p: ServerPackage) = ServerPackageCodec.validateFields(p)
    private fun decodeErr(b: ByteArray) = (ServerPackageCodec.decode(b) as ServerPackageDecodeResult.Invalid).error

    private fun publicPackage(name: String = "TAK", port: Int = 8089, cas: List<ByteArray> = emptyList()) =
        ServerPackage(name, "tak.example", port, ServerPackageTrustMode.PUBLIC_CA, ServerPackageIdentityMode.RECEIVER_ENROLLMENT, cas)

    private fun ServerPackage.with(
        cas: List<ByteArray> = caCertificates,
        trust: Int = trustMode,
        identity: Int = identityMode,
    ) = ServerPackage(name, host, port, trust, identity, cas)

    private fun staged() = Pkcs12StageResultParcel(Pkcs12StageCode.STAGED, HANDLE, SystemClock.elapsedRealtime() + 60_000)

    private fun capability() = ServerPackageCapabilityParcel(
        ServerPackageContract.CAPABILITY_KEY, 1, 32_700, 300_000, 300_000, supportsReceive = true, supportsExport = true,
    )

    private fun supported(control: IWearTakServerPackageControlV1) =
        ServerPackageDiscovery.Supported(control, 32_700, 300_000, 300_000, supportsReceive = true, supportsExport = true)

    private fun host(capability: ServerPackageCapabilityParcel?, control: IWearTakServerPackageControlV1?) =
        object : IWearTakPluginHostControlV3.Stub() {
            override fun subscribePcm(request: PcmSubscriptionRequestParcel?, callback: IWearTakPcmCallback?) =
                PcmSubscriptionResultParcel(false, status = PcmSubscriptionStatus.NOT_SUPPORTED)
            override fun unsubscribePcm(subscriptionId: String?) = PcmSubscriptionResultParcel(true, subscriptionId)
            override fun getHostCapabilities() = HostCapabilitiesV3Parcel(contractApiVersion = 3)
            override fun getPkcs12ImportCapability() = null
            override fun getPkcs12ImportControlV1() = null
            override fun getServerPackageCapability() = capability
            override fun getServerPackageControlV1() = control
        }

    private class FakeControl(
        val stageResult: Pkcs12StageResultParcel = Pkcs12StageResultParcel(Pkcs12StageCode.UNSUPPORTED),
        val throwOnStage: Boolean = false,
        val exportResult: ServerPackageExportResultParcel = ServerPackageExportResultParcel(ServerPackageExportCode.UNSUPPORTED),
        val takePayload: ServerPackageExportPayloadParcel = ServerPackageExportPayloadParcel(ServerPackageTakeCode.FAILED),
    ) : IWearTakServerPackageControlV1.Stub() {
        var stageCalls = 0
        var request: ServerPackageStageRequestParcel? = null
        var receivedCopy: ByteArray? = null

        override fun stageServerPackage(request: ServerPackageStageRequestParcel?): Pkcs12StageResultParcel {
            this.request = request
            receivedCopy = request?.encodedPackage?.copyOf()
            if (throwOnStage) throw RemoteException("transport failure")
            stageCalls++
            return stageResult
        }
        override fun requestReview(handle: String?) = Pkcs12ReviewResultParcel(Pkcs12ImportState.REVIEW_PENDING)
        override fun getStatus(handle: String?) = Pkcs12ImportStatusParcel(Pkcs12ImportState.STAGED)
        override fun cancel(handle: String?) = Pkcs12CancelResultParcel(Pkcs12ImportState.CANCELLED)
        override fun requestExport(requestId: String?) = exportResult
        override fun getExportStatus(handle: String?) = ServerPackageExportStatusParcel(ServerPackageExportState.PENDING)
        override fun takeExport(handle: String?) = takePayload
        override fun cancelExport(handle: String?) = Pkcs12CancelResultParcel(Pkcs12ImportState.CANCELLED)
    }

    private companion object {
        const val HANDLE = "AAAAAAAAAAAAAAAAAAAAAA"
        fun b64(s: String): ByteArray = Base64.getDecoder().decode(s)
        fun hex(s: String): ByteArray = ByteArray(s.length / 2) { s.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
    }
}
