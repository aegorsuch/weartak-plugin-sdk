package com.weartak.plugin.sdk.aidl;

import com.weartak.plugin.sdk.model.SensorEventParcel;
import com.weartak.plugin.sdk.model.CotReadyEventParcel;
import com.weartak.plugin.sdk.model.PluginStatusParcel;
import com.weartak.plugin.sdk.model.PluginErrorParcel;
import com.weartak.plugin.sdk.model.SharedBufferParcel;
import java.util.List;

/**
 * Plugin-to-host callbacks for streaming data and contract notifications.
 */
interface IWearTakHostCallback {
    void onSensorEvent(in SensorEventParcel event);
    void onSensorBatchEvent(in List<SensorEventParcel> events);
    void onCotReadyEvent(in CotReadyEventParcel event);
    void onPluginSettingsSchemaChanged();
    void onPluginSettingsValuesChanged();
    void onPluginMainUiSchemaChanged();
    void onPluginMainUiStateChanged();
    void onPluginStatusChanged(in PluginStatusParcel status);
    void onPluginError(in PluginErrorParcel error);
    void onSharedBufferAvailable(in SharedBufferParcel buffer);
}
