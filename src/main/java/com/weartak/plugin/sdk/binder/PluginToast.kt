package com.weartak.plugin.sdk.binder

import android.os.RemoteException
import com.weartak.plugin.sdk.aidl.IWearTakPluginControl
import com.weartak.plugin.sdk.model.ToastRequestParcel
import com.weartak.plugin.sdk.validation.ControlPlanePayloadValidator
import com.weartak.plugin.sdk.validation.ContractValidationResult

/**
 * Synchronous helper for plugin-originated host toasts on [IWearTakPluginControl].
 *
 * Returns `true` only when the host accepts the request; `false` for unsupported hosts,
 * validation failure, null control, or binder errors. Toast is best-effort — do not rely
 * on delivery as a guaranteed channel.
 */
object PluginToast {
    fun show(
        tag: String,
        control: IWearTakPluginControl?,
        message: String,
        duration: Int = ToastRequestParcel.DURATION_SHORT,
    ): Boolean {
        val c = control ?: return false
        val caps = try {
            c.hostCapabilities
        } catch (_: RemoteException) {
            return false
        }
        if (!caps.toasts) return false
        val request = ToastRequestParcel(message = message, duration = duration, tag = tag)
        val validated = when (val r = ControlPlanePayloadValidator.validateToast(request)) {
            is ContractValidationResult.Ok -> r.value
            is ContractValidationResult.Err -> return false
        }
        return try {
            c.requestToast(validated)
            true
        } catch (_: RemoteException) {
            false
        }
    }
}

/** Thin wrapper around [PluginToast.show]. */
fun IWearTakPluginControl?.showToast(
    tag: String,
    message: String,
    duration: Int = ToastRequestParcel.DURATION_SHORT,
): Boolean = PluginToast.show(tag, this, message, duration)
