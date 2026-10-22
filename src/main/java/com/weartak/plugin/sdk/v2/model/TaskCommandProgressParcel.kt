package com.weartak.plugin.sdk.v2.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * v2 task/command progress update for an in-flight [commandId].
 *
 * [sequence] is monotonic per `(pluginId, commandId)`.
 */
@Parcelize
data class TaskCommandProgressParcel(
    val pluginId: String,
    val commandId: String,
    val sequence: Long,
    val progressPercent: Int,
    val message: String? = null,
) : Parcelable
