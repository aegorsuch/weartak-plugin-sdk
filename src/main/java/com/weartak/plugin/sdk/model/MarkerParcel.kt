package com.weartak.plugin.sdk.model

import android.os.Bundle
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Marker control-plane payload. Use [com.weartak.plugin.sdk.MarkerContract] attribute keys for
 * type, remark, and coordinates; [markerId] is the stable CoT uid across publish/update/clear.
 */
@Parcelize
data class MarkerParcel(
    val markerId: String,
    val title: String? = null,
    val attributes: Bundle = Bundle(),
) : Parcelable
