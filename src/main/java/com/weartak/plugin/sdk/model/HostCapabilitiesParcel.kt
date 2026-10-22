package com.weartak.plugin.sdk.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** Snapshot of host-side features relevant to plugin control calls. */
@Parcelize
data class HostCapabilitiesParcel(
    val cotDispatch: Boolean = false,
    val alerts: Boolean = false,
    val toasts: Boolean = false,
    /** Publish, update, and clear marker lifecycle when true. */
    val markers: Boolean = false,
    val cepRules: Boolean = false,
    /** When true, prefer [com.weartak.plugin.sdk.aidl.IWearTakPluginControl.applyCepRuleEvent]. */
    val cepRuleStreaming: Boolean = false,
    val mapLayers: Boolean = false,
    val sharedMemory: Boolean = false,
) : Parcelable
