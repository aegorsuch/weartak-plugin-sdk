package com.weartak.plugin.sdk.binder

import android.os.RemoteException
import android.util.Log
import com.weartak.plugin.sdk.aidl.IWearTakHostCallback
import com.weartak.plugin.sdk.model.PluginStatusParcel

/**
 * Emits common host callback notifications with consistent [RemoteException] handling.
 */
object PluginHostNotifier {

    fun settingsValuesChanged(tag: String, callback: IWearTakHostCallback?) {
        val cb = callback ?: return
        try {
            cb.onPluginSettingsValuesChanged()
        } catch (ex: RemoteException) {
            Log.w(tag, "onPluginSettingsValuesChanged failed", ex)
        }
    }

    fun mainUiStateChanged(tag: String, callback: IWearTakHostCallback?) {
        val cb = callback ?: return
        try {
            cb.onPluginMainUiStateChanged()
        } catch (ex: RemoteException) {
            Log.w(tag, "onPluginMainUiStateChanged failed", ex)
        }
    }

    fun statusChanged(tag: String, callback: IWearTakHostCallback?, status: PluginStatusParcel) {
        val cb = callback ?: return
        try {
            cb.onPluginStatusChanged(status)
        } catch (ex: RemoteException) {
            Log.w(tag, "onPluginStatusChanged failed", ex)
        }
    }
}
