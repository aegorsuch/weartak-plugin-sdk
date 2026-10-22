package com.weartak.plugin.sdk.v2.aidl;

import com.weartak.plugin.sdk.v2.model.CepRuleStreamEventParcel;
import com.weartak.plugin.sdk.v2.model.DetectionEventParcel;
import com.weartak.plugin.sdk.v2.model.HostCapabilitiesV2Parcel;
import com.weartak.plugin.sdk.v2.model.TaskCommandCompletionParcel;
import com.weartak.plugin.sdk.v2.model.TaskCommandProgressParcel;
import com.weartak.plugin.sdk.v2.model.TaskCommandSubmitParcel;
import com.weartak.plugin.sdk.v2.model.V2HostResultParcel;

/**
 * v2 host-implemented control plane. Plugin→host event calls are synchronous and return [V2HostResultParcel].
 * Capability probes remain synchronous.
 */
interface IWearTakPluginHostControlV2 {
    V2HostResultParcel applyCepRuleEvent(in CepRuleStreamEventParcel event);
    V2HostResultParcel publishDetectionEvent(in DetectionEventParcel event);
    V2HostResultParcel submitTaskCommand(in TaskCommandSubmitParcel request);
    V2HostResultParcel reportTaskCommandProgress(in TaskCommandProgressParcel progress);
    V2HostResultParcel reportTaskCommandCompletion(in TaskCommandCompletionParcel completion);
    HostCapabilitiesV2Parcel getHostCapabilities();
}
