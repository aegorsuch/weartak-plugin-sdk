package com.weartak.plugin.sdk.v3.aidl;

import com.weartak.plugin.sdk.v3.aidl.IWearTakPcmCallback;
import com.weartak.plugin.sdk.v3.model.HostCapabilitiesV3Parcel;
import com.weartak.plugin.sdk.v3.model.PcmSubscriptionRequestParcel;
import com.weartak.plugin.sdk.v3.model.PcmSubscriptionResultParcel;
import com.weartak.plugin.sdk.nearbyimport.aidl.IWearTakPkcs12ImportControlV1;
import com.weartak.plugin.sdk.nearbyimport.model.Pkcs12ImportCapabilityParcel;
import com.weartak.plugin.sdk.serverpackage.aidl.IWearTakServerPackageControlV1;
import com.weartak.plugin.sdk.serverpackage.model.ServerPackageCapabilityParcel;

/** v3 host-implemented control plane for host-to-plugin PCM streaming. */
interface IWearTakPluginHostControlV3 {
    PcmSubscriptionResultParcel subscribePcm(in PcmSubscriptionRequestParcel request, in IWearTakPcmCallback callback);
    PcmSubscriptionResultParcel unsubscribePcm(String subscriptionId);
    HostCapabilitiesV3Parcel getHostCapabilities();
    Pkcs12ImportCapabilityParcel getPkcs12ImportCapability();
    IWearTakPkcs12ImportControlV1 getPkcs12ImportControlV1();
    ServerPackageCapabilityParcel getServerPackageCapability();
    IWearTakServerPackageControlV1 getServerPackageControlV1();
}
