package com.weartak.plugin.sdk.v2.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * v2 detection / classification event (synchronous [com.weartak.plugin.sdk.v2.aidl.IWearTakPluginHostControlV2.publishDetectionEvent]).
 *
 * [pluginId], [streamKey], and [detectionId] are **advisory** — the host rebinds to caller package / UID.
 * [sequence] is monotonic per `(pluginId, streamKey)`.
 */
@Parcelize
data class DetectionEventParcel(
    val pluginId: String,
    /** Ordering scope (e.g. detector or sensor channel). */
    val streamKey: String,
    /** Advisory instance id for this detection. */
    val detectionId: String,
    val sequence: Long,
    val timestampUtcMillis: Long,
    val classificationLabel: String,
    /** In `[0, 1]` when set; [CONFIDENCE_UNSPECIFIED] when unknown. */
    val confidence: Float = CONFIDENCE_UNSPECIFIED,
    val attributesJson: String? = null,
) : Parcelable {
    companion object {
        const val CONFIDENCE_UNSPECIFIED: Float = -1f
    }
}
