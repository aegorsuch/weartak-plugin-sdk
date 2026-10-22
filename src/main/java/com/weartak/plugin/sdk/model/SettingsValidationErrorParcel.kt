package com.weartak.plugin.sdk.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** Structured validation issue for settings schema/apply. */
@Parcelize
data class SettingsValidationErrorParcel(
    val fieldId: String = "",
    val code: String,
    val message: String,
) : Parcelable
