package com.weartak.plugin.sdk.v2.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * v2 host capability snapshot for [com.weartak.plugin.sdk.v2.aidl.IWearTakPluginHostControlV2].
 *
 * [contractApiVersion] must equal [com.weartak.plugin.sdk.v2.WearTakPluginV2Contract.CONTRACT_API_VERSION]
 * or v2 helpers reject the host ([V2TransportPath.HOST_INCOMPATIBLE]).
 */
@Parcelize
data class HostCapabilitiesV2Parcel(
    val contractApiVersion: Int = 0,
    val cepRules: Boolean = false,
    val detectionEvents: Boolean = false,
    val taskCommands: Boolean = false,
) : Parcelable
