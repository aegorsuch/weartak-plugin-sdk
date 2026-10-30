package com.weartak.plugin.sdk.mopp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** Authoritative host snapshot. Null selection means never saved, not the explicit "none" posture. */
@Parcelize
data class MoppPpeStateParcel(
    val revision: Long,
    val selection: String?,
    /** Null for never saved or legacy saved data whose timestamp is unknown. */
    val savedAtEpochMillis: Long?,
    val enabled: Boolean,
    /** Effective reporting availability; independent of the retained enable preference. */
    val contributing: Boolean,
    val lastMutationId: String?,
) : Parcelable
