package com.weartak.plugin.sdk.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Shared-memory transport is deferred to v1.x (design §6.2). [transportSupported] remains false
 * until hosts enable the feature; [onSharedBufferAvailable] is reserved.
 */
@Parcelize
data class SharedBufferParcel(
    val bufferToken: Long = -1L,
    val sizeBytes: Long = 0L,
    val flags: Int = 0,
    val transportSupported: Boolean = false,
) : Parcelable
