package com.weartak.plugin.sdk.serverpackage

import android.os.RemoteException
import android.os.SystemClock
import com.weartak.plugin.sdk.nearbyimport.Pkcs12ImportHandle
import com.weartak.plugin.sdk.nearbyimport.Pkcs12StageCode
import com.weartak.plugin.sdk.nearbyimport.model.Pkcs12StageResultParcel
import com.weartak.plugin.sdk.serverpackage.aidl.IWearTakServerPackageControlV1
import com.weartak.plugin.sdk.serverpackage.model.ServerPackageCapabilityParcel
import com.weartak.plugin.sdk.serverpackage.model.ServerPackageExportResultParcel
import com.weartak.plugin.sdk.serverpackage.model.ServerPackageStageRequestParcel
import com.weartak.plugin.sdk.v3.aidl.IWearTakPluginHostControlV3
import java.util.UUID

sealed class ServerPackageDiscovery {
    data class Supported(
        val control: IWearTakServerPackageControlV1,
        val maxPackageBytes: Int,
        val maxStageLifetimeMs: Long,
        val maxExportLifetimeMs: Long,
        val supportsReceive: Boolean,
        val supportsExport: Boolean,
    ) : ServerPackageDiscovery()

    data object Unsupported : ServerPackageDiscovery()
    data object Unavailable : ServerPackageDiscovery()
    data object InvalidCapability : ServerPackageDiscovery()
}

sealed class ServerPackageTakeResult {
    class Ready(val encodedPackage: ByteArray, val serverPackage: ServerPackage) : ServerPackageTakeResult()
    data class HostCode(val code: Int) : ServerPackageTakeResult()
    data class Invalid(val error: ServerPackageError) : ServerPackageTakeResult()
}

/** Helpers for optional, explicit, wearer-initiated server package export and staging. */
object ServerPackageClient {
    fun discover(hostControl: IWearTakPluginHostControlV3?): ServerPackageDiscovery {
        if (hostControl == null) return ServerPackageDiscovery.Unavailable
        val capability = try {
            hostControl.getServerPackageCapability()
        } catch (_: RemoteException) {
            return ServerPackageDiscovery.Unavailable
        } ?: return ServerPackageDiscovery.Unsupported

        if (capability.capabilityKey != ServerPackageContract.CAPABILITY_KEY ||
            capability.apiVersion != ServerPackageContract.API_VERSION
        ) {
            return ServerPackageDiscovery.Unsupported
        }
        if (!capability.isValid()) return ServerPackageDiscovery.InvalidCapability

        val control = try {
            hostControl.getServerPackageControlV1()
        } catch (_: RemoteException) {
            return ServerPackageDiscovery.Unavailable
        } ?: return ServerPackageDiscovery.InvalidCapability

        return ServerPackageDiscovery.Supported(
            control = control,
            maxPackageBytes = capability.maxPackageBytes,
            maxStageLifetimeMs = capability.maxStageLifetimeMs,
            maxExportLifetimeMs = capability.maxExportLifetimeMs,
            supportsReceive = capability.supportsReceive,
            supportsExport = capability.supportsExport,
        )
    }

    fun newRequestId(): String = UUID.randomUUID().toString()

    /**
     * Receive side: validates canonical bytes locally, stages once, wipes caller bytes only after
     * a well-formed STAGED receipt. Rejections/transport failures keep bytes; never auto-retry.
     */
    @Throws(RemoteException::class, IllegalArgumentException::class)
    fun stage(
        capability: ServerPackageDiscovery.Supported,
        requestId: String,
        encodedPackage: ByteArray,
    ): Pkcs12StageResultParcel {
        require(capability.supportsReceive) { "Host does not support server package receive" }
        require(capability.maxPackageBytes in 1..ServerPackageContract.MAX_PACKAGE_BYTES)
        require(capability.maxStageLifetimeMs in 1L..ServerPackageContract.MAX_STAGE_LIFETIME_MS)
        require(requestId.isNotBlank()) { "requestId must not be blank" }
        require(encodedPackage.size <= capability.maxPackageBytes) { "Server package exceeds negotiated limit" }
        ServerPackageCodec.validate(encodedPackage)?.let { throw ServerPackageException(it) }

        val stagedCopy = encodedPackage.copyOf()
        val result = try {
            capability.control.stageServerPackage(ServerPackageStageRequestParcel(requestId, stagedCopy))
        } finally {
            stagedCopy.fill(0)
        }
        if (result.code == Pkcs12StageCode.STAGED) {
            checkReceipt(result.handle, result.expiresAtElapsedRealtimeMs, capability.maxStageLifetimeMs)
            encodedPackage.fill(0)
        }
        return result
    }

    /** Sender side: explicit foreground tap only. PENDING means host picker launched. */
    @Throws(RemoteException::class, IllegalArgumentException::class)
    fun requestExport(
        capability: ServerPackageDiscovery.Supported,
        requestId: String,
    ): ServerPackageExportResultParcel {
        require(capability.supportsExport) { "Host does not support server package export" }
        require(capability.maxExportLifetimeMs in 1L..ServerPackageContract.MAX_EXPORT_LIFETIME_MS)
        require(requestId.isNotBlank()) { "requestId must not be blank" }
        val result = capability.control.requestExport(requestId)
        if (result.code == ServerPackageExportCode.PENDING) {
            checkReceipt(result.handle, result.expiresAtElapsedRealtimeMs, capability.maxExportLifetimeMs)
        }
        return result
    }

    /** One-use take of a READY export. Ready holds sensitive data: wipe bytes and package after send. */
    @Throws(RemoteException::class, IllegalArgumentException::class)
    fun takeExport(capability: ServerPackageDiscovery.Supported, handle: String): ServerPackageTakeResult {
        require(Pkcs12ImportHandle.isValidOpaqueHandle(handle)) { "Malformed export handle" }
        val payload = capability.control.takeExport(handle)
        if (payload.code != ServerPackageTakeCode.OK) {
            payload.encodedPackage?.fill(0)
            return ServerPackageTakeResult.HostCode(payload.code)
        }
        val bytes = payload.encodedPackage
            ?: return ServerPackageTakeResult.Invalid(ServerPackageError.EMPTY)
        if (bytes.size > capability.maxPackageBytes) {
            bytes.fill(0)
            return ServerPackageTakeResult.Invalid(ServerPackageError.TOO_LARGE)
        }
        return when (val decoded = ServerPackageCodec.decode(bytes)) {
            is ServerPackageDecodeResult.Valid -> ServerPackageTakeResult.Ready(bytes, decoded.serverPackage)
            is ServerPackageDecodeResult.Invalid -> {
                bytes.fill(0)
                ServerPackageTakeResult.Invalid(decoded.error)
            }
        }
    }

    private fun checkReceipt(handle: String?, expiresAt: Long, maxLifetimeMs: Long) {
        val now = SystemClock.elapsedRealtime()
        check(Pkcs12ImportHandle.isValidOpaqueHandle(handle)) { "Host returned malformed handle" }
        check(expiresAt > now && expiresAt <= now + maxLifetimeMs) { "Host returned invalid expiry" }
    }

    private fun ServerPackageCapabilityParcel.isValid(): Boolean =
        maxPackageBytes in 1..ServerPackageContract.MAX_PACKAGE_BYTES &&
            (supportsReceive || supportsExport) &&
            (!supportsReceive || maxStageLifetimeMs in 1L..ServerPackageContract.MAX_STAGE_LIFETIME_MS) &&
            (!supportsExport || maxExportLifetimeMs in 1L..ServerPackageContract.MAX_EXPORT_LIFETIME_MS)
}
