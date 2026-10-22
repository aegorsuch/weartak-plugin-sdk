package com.weartak.plugin.sdk.model

/**
 * How [com.weartak.plugin.sdk.aidl.IWearTakPlugin.applySettingsWithResult] merges [android.os.Bundle] input.
 */
object SettingsApplyMode {
    /** Merge only keys present in the incoming bundle; omit keys preserve previous values. */
    const val PARTIAL_PATCH: Int = 0
    /** Incoming bundle must include every schema field (after validation). */
    const val FULL_REPLACEMENT: Int = 1
    /** Host snapshot repopulates host-bound fields; plugin values merge for editable fields. */
    const val HOST_DRIVEN_RECOMPUTE: Int = 2
}
