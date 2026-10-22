package com.weartak.plugin.sdk.v2.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * v2 task/command submission (first step of the flow).
 *
 * [pluginId] and [commandId] are **advisory** — the host rebinds to caller package / UID.
 */
@Parcelize
data class TaskCommandSubmitParcel(
    val pluginId: String,
    val commandId: String,
    val taskType: String,
    val sequence: Long,
    val payloadJson: String,
) : Parcelable
