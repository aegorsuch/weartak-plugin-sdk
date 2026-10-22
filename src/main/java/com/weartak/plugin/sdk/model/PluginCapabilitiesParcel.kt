package com.weartak.plugin.sdk.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Explicit capability flags. Unimplemented capabilities must be `false`, never omitted.
 */
@Parcelize
data class PluginCapabilitiesParcel(
    val hostedSettingsUi: Boolean = false,
    val externalSettingsActivity: Boolean = false,
    val hostedMainUi: Boolean = false,
    val externalMainActivity: Boolean = false,
    val cotReadyEvents: Boolean = false,
    val mapLayers: Boolean = false,
    val deviceIntegration: Boolean = false,
    val sharedMemory: Boolean = false,
) : Parcelable
