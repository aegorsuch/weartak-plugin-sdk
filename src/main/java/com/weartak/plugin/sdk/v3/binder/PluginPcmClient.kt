package com.weartak.plugin.sdk.v3.binder

import android.os.RemoteException
import com.weartak.plugin.sdk.v3.aidl.IWearTakPcmCallback
import com.weartak.plugin.sdk.v3.aidl.IWearTakPluginHostControlV3
import com.weartak.plugin.sdk.v3.model.PcmSubscriptionRequestParcel
import com.weartak.plugin.sdk.v3.model.PcmSubscriptionResultParcel
import com.weartak.plugin.sdk.v3.model.PcmSubscriptionStatus
import com.weartak.plugin.sdk.v3.model.PcmTransportPath
import com.weartak.plugin.sdk.v3.validation.PcmContractValidator

/** Small plugin-side helper for v3 PCM capability negotiation and lifecycle calls. */
class PluginPcmClient(
    private val callback: IWearTakPcmCallback,
) {
    fun subscribe(
        control: IWearTakPluginHostControlV3?,
        request: PcmSubscriptionRequestParcel = PcmSubscriptionRequestParcel(),
    ): PcmSubscriptionResultParcel {
        if (!PcmContractValidator.isSupported(request)) {
            return rejected(PcmSubscriptionStatus.INVALID_REQUEST)
        }
        return when (PcmHostControlSupport.resolveTransportPath(control)) {
            PcmTransportPath.V3_BATCHED_BINDER -> try {
                control?.subscribePcm(request, callback) ?: rejected(PcmSubscriptionStatus.NOT_SUPPORTED)
            } catch (_: RemoteException) {
                rejected(PcmSubscriptionStatus.REMOTE_ERROR)
            }
            PcmTransportPath.HOST_INCOMPATIBLE -> rejected(PcmSubscriptionStatus.HOST_INCOMPATIBLE)
            PcmTransportPath.UNAVAILABLE -> rejected(PcmSubscriptionStatus.NOT_SUPPORTED)
        }
    }

    fun unsubscribe(
        control: IWearTakPluginHostControlV3?,
        subscriptionId: String,
    ): PcmSubscriptionResultParcel = try {
        control?.unsubscribePcm(subscriptionId) ?: rejected(PcmSubscriptionStatus.NOT_SUPPORTED)
    } catch (_: RemoteException) {
        rejected(PcmSubscriptionStatus.REMOTE_ERROR)
    }

    private fun rejected(status: Int): PcmSubscriptionResultParcel =
        PcmSubscriptionResultParcel(accepted = false, status = status)
}
