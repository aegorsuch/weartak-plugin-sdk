package com.weartak.plugin.sdk.binder

import android.os.RemoteException
import android.util.Log
import com.weartak.plugin.sdk.aidl.IWearTakHostCallback
import com.weartak.plugin.sdk.model.PluginErrorParcel

/**
 * Centralized host error reporting for plugin-side binders.
 * Safe when [callback] is null, after shutdown, or when the host dies mid-call.
 */
object PluginBinderErrorReporter {

    fun report(
        tag: String,
        operation: String,
        error: PluginErrorParcel,
        callback: IWearTakHostCallback?,
    ) {
        val cb = callback ?: return
        try {
            cb.onPluginError(error)
        } catch (ex: RemoteException) {
            Log.w(tag, "onPluginError failed for $operation", ex)
        } catch (ex: RuntimeException) {
            Log.w(tag, "onPluginError failed for $operation", ex)
        }
    }

    fun validationFailed(
        tag: String,
        operation: String,
        message: String,
        callback: IWearTakHostCallback?,
        code: Int = com.weartak.plugin.sdk.model.PluginErrorCodes.VALIDATION_FAILED,
    ) {
        report(
            tag = tag,
            operation = operation,
            error = PluginErrorParcel(code = code, message = message, fatal = false),
            callback = callback,
        )
    }
}
