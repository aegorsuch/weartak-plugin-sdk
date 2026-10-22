package com.weartak.plugin.sdk.aidl;

import com.weartak.plugin.sdk.model.MapLayerCommandParcel;

/**
 * Map-layer sub-surface. Phase-gated: hosts may return unsupported until Phase 5.
 */
interface IWearTakMapLayerControl {
    boolean isMapLayerSupported();
    int applyLayerCommand(in MapLayerCommandParcel command);
}
