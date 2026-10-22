package com.weartak.plugin.sdk.v2.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * v2 ordered CEP rule mutation (synchronous [com.weartak.plugin.sdk.v2.aidl.IWearTakPluginHostControlV2.applyCepRuleEvent]).
 *
 * [pluginId], [sensorKey], and [ruleSetId] are **advisory** — the host must rebind them to the
 * caller package / UID before registry insertion. [sequence] is monotonic per `(pluginId, sensorKey)`.
 */
@Parcelize
data class CepRuleStreamEventParcel(
    val operation: Int,
    val pluginId: String,
    val sensorKey: String,
    val ruleSetId: String,
    val sequence: Long,
    val rulesJson: String? = null,
) : Parcelable
