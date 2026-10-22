package com.weartak.plugin.sdk.model

import android.os.Bundle
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** Opaque map-layer command; host interprets by phase and capability. */
@Parcelize
data class MapLayerCommandParcel(
    val commandCode: Int,
    val layerId: String = "",
    val payload: Bundle = Bundle(),
) : Parcelable
