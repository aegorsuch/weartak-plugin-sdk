package com.weartak.plugin.sdk.aidl;

import com.weartak.plugin.sdk.model.PluginInfoParcel;
import com.weartak.plugin.sdk.model.PluginCapabilitiesParcel;
import com.weartak.plugin.sdk.model.PluginInitParcel;
import com.weartak.plugin.sdk.model.PluginStatusParcel;
import com.weartak.plugin.sdk.model.SettingsSchemaParcel;
import com.weartak.plugin.sdk.model.HostSettingsParcel;
import com.weartak.plugin.sdk.model.SettingsApplyResultParcel;
import com.weartak.plugin.sdk.model.PluginMainUiSchemaParcel;
import com.weartak.plugin.sdk.aidl.IWearTakHostCallback;
import com.weartak.plugin.sdk.aidl.IWearTakPluginControl;
import com.weartak.plugin.sdk.v2.aidl.IWearTakPluginHostControlV2;
import android.os.Bundle;

/**
 * Plugin-owned binder surface. Host binds with {@link com.weartak.plugin.sdk.WearTakPluginContract#PERMISSION_BIND_PLUGIN}.
 */
interface IWearTakPlugin {
    PluginInfoParcel getPluginInfo();
    PluginCapabilitiesParcel getCapabilities();
    int getApiVersion();
    PluginStatusParcel getStatus();
    /**
     * Legacy compatibility init. The host will no longer call this path in the consolidated
     * init flow; new plugins should implement [initializePluginV2] as the canonical startup
     * entry point.
     */
    void initializePlugin(in PluginInitParcel init, in IWearTakPluginControl control);
    /**
     * Canonical host-called initialization entry point.
     *
     * `controlV2 == null` means the host does not currently provide v2 event-family support.
     * The plugin should treat that as v1 runtime only and continue on [IWearTakPluginControl].
     */
    void initializePluginV2(in PluginInitParcel init, in IWearTakPluginControl control, in IWearTakPluginHostControlV2 controlV2);
    void startPlugin();
    void stopPlugin();
    void shutdownPlugin();
    void registerHostCallback(in IWearTakHostCallback callback);
    void unregisterHostCallback(in IWearTakHostCallback callback);
    SettingsSchemaParcel getSettingsSchema();
    Bundle getSettingsValues();
    /** Legacy apply; prefer [applySettingsWithResult] for structured outcomes. */
    void applySettings(in Bundle values);
    /**
     * Structured apply with explicit [applyMode] ([com.weartak.plugin.sdk.model.SettingsApplyMode]).
     * Host may pass a host snapshot via [onHostSettingsChanged] before apply.
     */
    SettingsApplyResultParcel applySettingsWithResult(in Bundle values, int applyMode);
    /** Host pushes an updated settings snapshot (additive; no-op on legacy plugins). */
    void onHostSettingsChanged(in HostSettingsParcel hostSettings);
    PluginMainUiSchemaParcel getMainUiSchema();
    Bundle getMainUiState();
    void invokeMainUiAction(String actionId, in Bundle args);
}
