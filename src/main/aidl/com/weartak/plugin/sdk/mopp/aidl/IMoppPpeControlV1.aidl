package com.weartak.plugin.sdk.mopp.aidl;

import com.weartak.plugin.sdk.mopp.model.MoppPpeResultParcel;

/** Host-owned, synchronous persisted MOPP/PPE state; advertised through PluginInitParcel.hostExtras. */
interface IMoppPpeControlV1 {
    MoppPpeResultParcel read();
    MoppPpeResultParcel save(long expectedRevision, String mutationId, String selection);
    MoppPpeResultParcel setEnabled(long expectedRevision, String mutationId, boolean enabled);
}
