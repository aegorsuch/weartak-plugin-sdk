package com.weartak.plugin.sdk.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PluginMainUiSchemaParcel(
    val schemaVersion: Int = 1,
    val cards: List<MainUiCardParcel> = emptyList(),
) : Parcelable
