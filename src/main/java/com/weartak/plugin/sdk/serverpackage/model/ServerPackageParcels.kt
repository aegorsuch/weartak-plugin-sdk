package com.weartak.plugin.sdk.serverpackage.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** Optional server package capability; zero version or missing control means unsupported. */
@Parcelize
data class ServerPackageCapabilityParcel(
    val capabilityKey: String = "",
    val apiVersion: Int = 0,
    val maxPackageBytes: Int = 0,
    val maxStageLifetimeMs: Long = 0L,
    val maxExportLifetimeMs: Long = 0L,
    val supportsReceive: Boolean = false,
    val supportsExport: Boolean = false,
) : Parcelable

/** Canonical [com.weartak.plugin.sdk.serverpackage.ServerPackageCodec] bytes plus replay key. */
@Parcelize
data class ServerPackageStageRequestParcel(
    val requestId: String,
    val encodedPackage: ByteArray,
) : Parcelable

/** PENDING means host-owned server selection UI was launched; not export completion. */
@Parcelize
data class ServerPackageExportResultParcel(
    val code: Int,
    val handle: String? = null,
    val expiresAtElapsedRealtimeMs: Long = 0L,
) : Parcelable

@Parcelize
data class ServerPackageExportStatusParcel(
    val state: Int,
    val expiresAtElapsedRealtimeMs: Long = 0L,
    val errorCode: Int = 0,
) : Parcelable

/** One-use release of non-secret canonical package bytes after wearer-selected export. */
@Parcelize
data class ServerPackageExportPayloadParcel(
    val code: Int,
    val encodedPackage: ByteArray? = null,
) : Parcelable
