package com.weartak.plugin.sdk.aidl;

import com.weartak.plugin.sdk.model.CotRequestParcel;
import com.weartak.plugin.sdk.model.AlertRequestParcel;
import com.weartak.plugin.sdk.model.ToastRequestParcel;
import com.weartak.plugin.sdk.model.MarkerParcel;
import com.weartak.plugin.sdk.model.DeviceStatusParcel;
import com.weartak.plugin.sdk.model.HostCapabilitiesParcel;
import com.weartak.plugin.sdk.model.CepRuleEventParcel;
import com.weartak.plugin.sdk.aidl.IWearTakMapLayerControl;

/**
 * Host-implemented control plane passed to the plugin during initialization.
 */
interface IWearTakPluginControl {
    void requestCotDispatch(in CotRequestParcel request);
    void requestAlert(in AlertRequestParcel request);
    void requestToast(in ToastRequestParcel request);
    /** Create/drop a marker; stable [MarkerParcel.markerId] is the CoT uid. */
    void requestMarkerPublish(in MarkerParcel marker);
    /** Update an existing marker by [MarkerParcel.markerId]; no-op if uid unknown. */
    void requestMarkerUpdate(in MarkerParcel marker);
    /** Clear/delete a marker by [MarkerParcel.markerId]. */
    void requestMarkerClear(in MarkerParcel marker);
    DeviceStatusParcel getDeviceStatus();
    void registerCepRules(String pluginId, String sensorKey, String rulesJson);
    void unregisterCepRules(String pluginId, String sensorKey);
    /** v1.x streaming path; host orders by [CepRuleEventParcel.sequence] per plugin + sensor key. */
    void applyCepRuleEvent(in CepRuleEventParcel event);
    boolean isCotConnected();
    HostCapabilitiesParcel getHostCapabilities();
    IWearTakMapLayerControl getMapLayerControl();
}
