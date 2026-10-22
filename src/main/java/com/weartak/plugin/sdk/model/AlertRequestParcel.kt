package com.weartak.plugin.sdk.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Plugin-originated emergency alert. Host mapping (WearTAK):
 * - [title] → alert category
 * - [message] → alert description (with [channelId] prefixed when set)
 * - [severity] → alert priority (0 = default)
 * - SMS is not triggered from the plugin control plane.
 */
@Parcelize
data class AlertRequestParcel(
    val title: String,
    val message: String,
    /** 0 = default; higher = more urgent — host maps severities. */
    val severity: Int = 0,
    /** Optional logical channel; hosts without a channel field embed this in [message]. */
    val channelId: String? = null,
) : Parcelable
