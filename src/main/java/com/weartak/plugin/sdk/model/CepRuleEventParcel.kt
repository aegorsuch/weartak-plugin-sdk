package com.weartak.plugin.sdk.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * One ordered CEP rule mutation in a plugin-owned stream.
 *
 * [pluginId], [sensorKey], and [ruleSetId] are advisory; the host rebinds them to the caller
 * package / UID before registry insertion. [sequence] is monotonic per `(pluginId, sensorKey)` stream.
 */
@Parcelize
data class CepRuleEventParcel(
    val operation: Int,
    val pluginId: String,
    val sensorKey: String,
    /** Stable rule-set identity within the stream; host may remap. */
    val ruleSetId: String,
    /** Monotonic per `(pluginId, sensorKey)`; host orders events by this value. */
    val sequence: Long,
    /** Required for [CepRuleOperation.ADD] / [CepRuleOperation.UPDATE]; null for [CepRuleOperation.REMOVE]. */
    val rulesJson: String? = null,
) : Parcelable
