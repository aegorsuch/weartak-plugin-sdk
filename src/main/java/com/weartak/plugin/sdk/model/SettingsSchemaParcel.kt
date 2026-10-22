package com.weartak.plugin.sdk.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SettingsSchemaParcel(
    val schemaVersion: Int = 1,
    val title: String? = null,
    val fields: List<SettingsFieldParcel> = emptyList(),
) : Parcelable
