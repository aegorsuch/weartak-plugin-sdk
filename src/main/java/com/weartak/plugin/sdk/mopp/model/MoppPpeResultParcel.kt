package com.weartak.plugin.sdk.mopp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** OK requires accepted=true and persisted authoritative state; failure must never imply success. */
@Parcelize
data class MoppPpeResultParcel(
    val accepted: Boolean,
    val code: Int,
    val state: MoppPpeStateParcel?,
) : Parcelable

object MoppPpeResultCode {
    const val OK: Int = 0
    const val REVISION_CONFLICT: Int = 1
    const val INVALID_REQUEST: Int = 2
    const val UNAUTHORIZED: Int = 3
    const val INACTIVE_SESSION: Int = 4
    const val PERSISTENCE_FAILED: Int = 5
}
