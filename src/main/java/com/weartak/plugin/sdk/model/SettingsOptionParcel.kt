package com.weartak.plugin.sdk.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SettingsOptionParcel(
    val value: String,
    val label: String,
) : Parcelable
