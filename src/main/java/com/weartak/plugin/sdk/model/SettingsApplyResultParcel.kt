package com.weartak.plugin.sdk.model

import android.os.Bundle
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Result of [com.weartak.plugin.sdk.aidl.IWearTakPlugin.applySettingsWithResult].
 */
@Parcelize
data class SettingsApplyResultParcel(
    val success: Boolean,
    /** One of [SettingsApplyMode]. */
    val applyMode: Int = SettingsApplyMode.PARTIAL_PATCH,
    val values: Bundle = Bundle(),
    val restartRequired: Boolean = false,
    val anyValueChanged: Boolean = false,
    val errors: List<SettingsValidationErrorParcel> = emptyList(),
    /** Field ids populated from the host snapshot during this apply. */
    val hostDerivedFieldIds: List<String> = emptyList(),
) : Parcelable
