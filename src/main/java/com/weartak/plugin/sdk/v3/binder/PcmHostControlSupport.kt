package com.weartak.plugin.sdk.v3.binder

import android.os.RemoteException
import com.weartak.plugin.sdk.v3.WearTakPluginV3Contract
import com.weartak.plugin.sdk.v3.aidl.IWearTakPluginHostControlV3
import com.weartak.plugin.sdk.v3.model.HostCapabilitiesV3Parcel
import com.weartak.plugin.sdk.v3.model.PcmTransportPath

object PcmHostControlSupport {
    fun readCapabilities(control: IWearTakPluginHostControlV3?): HostCapabilitiesV3Parcel? =
        try {
            control?.hostCapabilities
        } catch (_: RemoteException) {
            null
        }

    fun resolveTransportPath(control: IWearTakPluginHostControlV3?): PcmTransportPath {
        if (control == null) return PcmTransportPath.UNAVAILABLE
        val capabilities = readCapabilities(control) ?: return PcmTransportPath.UNAVAILABLE
        if (capabilities.contractApiVersion != WearTakPluginV3Contract.CONTRACT_API_VERSION) {
            return PcmTransportPath.HOST_INCOMPATIBLE
        }
        return if (capabilities.pcmStreaming) {
            PcmTransportPath.V3_BATCHED_BINDER
        } else {
            PcmTransportPath.UNAVAILABLE
        }
    }
}
