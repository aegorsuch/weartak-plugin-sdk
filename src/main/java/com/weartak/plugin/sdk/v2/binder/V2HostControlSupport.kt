package com.weartak.plugin.sdk.v2.binder

import android.os.RemoteException
import com.weartak.plugin.sdk.v2.WearTakPluginV2Contract
import com.weartak.plugin.sdk.v2.aidl.IWearTakPluginHostControlV2
import com.weartak.plugin.sdk.v2.model.HostCapabilitiesV2Parcel
import com.weartak.plugin.sdk.v2.model.V2TransportPath

internal object V2HostControlSupport {

    fun readCapabilities(control: IWearTakPluginHostControlV2?): HostCapabilitiesV2Parcel? =
        try {
            control?.hostCapabilities
        } catch (_: RemoteException) {
            null
        }

    fun isCompatibleVersion(caps: HostCapabilitiesV2Parcel?): Boolean =
        caps?.contractApiVersion == WearTakPluginV2Contract.CONTRACT_API_VERSION

    fun cepPath(control: IWearTakPluginHostControlV2?): V2TransportPath =
        familyPath(control) { it.cepRules }

    fun detectionPath(control: IWearTakPluginHostControlV2?): V2TransportPath =
        familyPath(control) { it.detectionEvents }

    fun taskCommandPath(control: IWearTakPluginHostControlV2?): V2TransportPath =
        familyPath(control) { it.taskCommands }

    private inline fun familyPath(
        control: IWearTakPluginHostControlV2?,
        familyEnabled: (HostCapabilitiesV2Parcel) -> Boolean,
    ): V2TransportPath {
        if (control == null) return V2TransportPath.UNAVAILABLE
        val caps = readCapabilities(control) ?: return V2TransportPath.UNAVAILABLE
        if (!isCompatibleVersion(caps)) return V2TransportPath.HOST_INCOMPATIBLE
        return if (familyEnabled(caps)) V2TransportPath.V2_SYNC else V2TransportPath.UNAVAILABLE
    }
}
