package com.weartak.plugin.sdk.model

import android.os.Bundle
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** Context passed from host when binding initializes the plugin. */
@Parcelize
data class PluginInitParcel(
    val hostPackageName: String,
    val hostUid: Int,
    val pluginIdExpected: String,
    val hostExtras: Bundle = Bundle(),
) : Parcelable
