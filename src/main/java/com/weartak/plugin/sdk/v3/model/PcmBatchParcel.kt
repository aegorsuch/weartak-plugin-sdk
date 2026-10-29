package com.weartak.plugin.sdk.v3.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** Bounded batch of original chunks delivered in one Binder transaction. */
@Parcelize
data class PcmBatchParcel(
    val subscriptionId: String,
    val chunks: List<PcmChunkParcel>,
) : Parcelable
