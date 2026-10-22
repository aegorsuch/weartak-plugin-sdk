package com.weartak.plugin.sdk.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** Stable plugin identity and presentation metadata. */
@Parcelize
data class PluginInfoParcel(
    val pluginId: String,
    val displayName: String,
    val versionName: String,
    val packageName: String,
    val description: String? = null,
) : Parcelable
