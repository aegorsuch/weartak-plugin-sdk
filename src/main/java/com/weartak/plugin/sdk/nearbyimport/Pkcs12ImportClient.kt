package com.weartak.plugin.sdk.nearbyimport

import android.os.RemoteException
import android.os.SystemClock
import com.weartak.plugin.sdk.nearbyimport.aidl.IWearTakPkcs12ImportControlV1
import com.weartak.plugin.sdk.nearbyimport.model.Pkcs12ImportCapabilityParcel
import com.weartak.plugin.sdk.nearbyimport.model.Pkcs12StageResultParcel
import com.weartak.plugin.sdk.nearbyimport.model.Pkcs12StageRequestParcel
import com.weartak.plugin.sdk.v3.aidl.IWearTakPluginHostControlV3
import java.util.UUID

sealed class Pkcs12ImportDiscovery {
    data class Supported(
        val control: IWearTakPkcs12ImportControlV1,
        val maxPayloadBytes: Int,
        val maxStageLifetimeMs: Long,
    ) : Pkcs12ImportDiscovery()

    data object Unsupported : Pkcs12ImportDiscovery()
    data object Unavailable : Pkcs12ImportDiscovery()
    data object InvalidCapability : Pkcs12ImportDiscovery()
}

/** Helpers for optional discovery and explicit, caller-triggered stage/review operations. */
object Pkcs12ImportClient {
    fun discover(hostControl: IWearTakPluginHostControlV3?): Pkcs12ImportDiscovery {
        if (hostControl == null) return Pkcs12ImportDiscovery.Unavailable

        val capability = try {
            hostControl.getPkcs12ImportCapability()
        } catch (_: RemoteException) {
            // Older v3 hosts do not implement this appended optional transaction.
            return Pkcs12ImportDiscovery.Unavailable
        } ?: return Pkcs12ImportDiscovery.Unsupported

        if (capability.capabilityKey != Pkcs12ImportContract.CAPABILITY_KEY ||
            capability.apiVersion != Pkcs12ImportContract.API_VERSION
        ) {
            return Pkcs12ImportDiscovery.Unsupported
        }
        if (!capability.isValid()) return Pkcs12ImportDiscovery.InvalidCapability

        val control = try {
            hostControl.getPkcs12ImportControlV1()
        } catch (_: RemoteException) {
            return Pkcs12ImportDiscovery.Unavailable
        } ?: return Pkcs12ImportDiscovery.InvalidCapability

        return Pkcs12ImportDiscovery.Supported(
            control = control,
            maxPayloadBytes = capability.maxPayloadBytes,
            maxStageLifetimeMs = capability.maxStageLifetimeMs,
        )
    }

    /** Creates a fresh, random replay key for one user-initiated stage attempt. */
    fun newRequestId(): String = UUID.randomUUID().toString()

    /**
     * Stage once. Wipes caller bytes only after host confirms STAGED; callers must retain them
     * on every rejection or transport failure and must not automatically retry.
     */
    @Throws(RemoteException::class, IllegalArgumentException::class)
    fun stage(
        capability: Pkcs12ImportDiscovery.Supported,
        requestId: String,
        pkcs12Bytes: ByteArray,
    ): Pkcs12StageResultParcel {
        val maxPayloadBytes = capability.maxPayloadBytes
        val maxStageLifetimeMs = capability.maxStageLifetimeMs
        require(maxPayloadBytes in 1..Pkcs12ImportContract.MAX_PAYLOAD_BYTES) { "Invalid negotiated PKCS#12 size limit" }
        require(maxStageLifetimeMs in 1L..Pkcs12ImportContract.MAX_STAGE_LIFETIME_MS) {
            "Invalid negotiated PKCS#12 stage lifetime"
        }
        require(requestId.isNotBlank()) { "requestId must not be blank" }
        require(pkcs12Bytes.isNotEmpty()) { "PKCS#12 payload must not be empty" }
        require(pkcs12Bytes.size <= maxPayloadBytes) { "PKCS#12 payload exceeds negotiated limit" }
        val stagedCopy = pkcs12Bytes.copyOf()
        val result = try {
            capability.control.stagePkcs12(Pkcs12StageRequestParcel(requestId, stagedCopy))
        } finally {
            stagedCopy.fill(0)
        }
        if (result.code == Pkcs12StageCode.STAGED) {
            val now = SystemClock.elapsedRealtime()
            check(Pkcs12ImportHandle.isValidOpaqueHandle(result.handle)) {
                "Host returned malformed PKCS#12 stage handle"
            }
            check(
                result.expiresAtElapsedRealtimeMs > now &&
                    result.expiresAtElapsedRealtimeMs <= now + maxStageLifetimeMs,
            ) {
                "Host returned invalid PKCS#12 stage expiry"
            }
            pkcs12Bytes.fill(0)
        }
        return result
    }

    private fun Pkcs12ImportCapabilityParcel.isValid(): Boolean =
        maxPayloadBytes in 1..Pkcs12ImportContract.MAX_PAYLOAD_BYTES &&
            maxStageLifetimeMs in 1L..Pkcs12ImportContract.MAX_STAGE_LIFETIME_MS
}
