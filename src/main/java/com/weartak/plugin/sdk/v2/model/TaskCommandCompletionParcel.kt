package com.weartak.plugin.sdk.v2.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * v2 task/command terminal report for [commandId].
 *
 * [sequence] is monotonic per `(pluginId, commandId)` and should be greater than prior steps.
 */
@Parcelize
data class TaskCommandCompletionParcel(
    val pluginId: String,
    val commandId: String,
    val sequence: Long,
    val outcome: Int,
    val message: String? = null,
    val resultJson: String? = null,
) : Parcelable
