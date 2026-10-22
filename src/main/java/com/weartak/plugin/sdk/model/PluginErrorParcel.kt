package com.weartak.plugin.sdk.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Structured error reported to the host via [com.weartak.plugin.sdk.aidl.IWearTakHostCallback.onPluginError].
 * Prefer [PluginErrorCodes] for `code` values.
 */
@Parcelize
data class PluginErrorParcel(
    val code: Int,
    val message: String,
    val fatal: Boolean = false,
) : Parcelable
