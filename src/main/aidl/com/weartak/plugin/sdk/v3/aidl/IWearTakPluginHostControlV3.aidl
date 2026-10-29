package com.weartak.plugin.sdk.v3.aidl;

import com.weartak.plugin.sdk.v3.aidl.IWearTakPcmCallback;
import com.weartak.plugin.sdk.v3.model.HostCapabilitiesV3Parcel;
import com.weartak.plugin.sdk.v3.model.PcmSubscriptionRequestParcel;
import com.weartak.plugin.sdk.v3.model.PcmSubscriptionResultParcel;

/** v3 host-implemented control plane for host-to-plugin PCM streaming. */
interface IWearTakPluginHostControlV3 {
    PcmSubscriptionResultParcel subscribePcm(in PcmSubscriptionRequestParcel request, in IWearTakPcmCallback callback);
    PcmSubscriptionResultParcel unsubscribePcm(String subscriptionId);
    HostCapabilitiesV3Parcel getHostCapabilities();
}
