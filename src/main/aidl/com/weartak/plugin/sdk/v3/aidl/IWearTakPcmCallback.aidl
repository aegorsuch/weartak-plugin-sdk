package com.weartak.plugin.sdk.v3.aidl;

import com.weartak.plugin.sdk.v3.model.PcmBatchParcel;
import com.weartak.plugin.sdk.v3.model.PcmStreamStateParcel;

/** Plugin-implemented callback for host-owned PCM delivery. */
interface IWearTakPcmCallback {
    void onPcmBatch(in PcmBatchParcel batch);
    void onPcmStreamState(in PcmStreamStateParcel state);
}
