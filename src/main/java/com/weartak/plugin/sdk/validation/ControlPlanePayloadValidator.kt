package com.weartak.plugin.sdk.validation

import com.weartak.plugin.sdk.WearTakPluginContract
import com.weartak.plugin.sdk.model.AlertRequestParcel
import com.weartak.plugin.sdk.model.CotRequestParcel
import com.weartak.plugin.sdk.model.MarkerParcel
import com.weartak.plugin.sdk.model.ToastRequestParcel

object ControlPlanePayloadValidator {
    fun validateAlert(a: AlertRequestParcel): ContractValidationResult<AlertRequestParcel> {
        if (a.title.isBlank()) return ContractValidationResult.Err(ContractIssue("title", "required"))
        if (a.message.isBlank()) return ContractValidationResult.Err(ContractIssue("message", "required"))
        val t = PluginStringPolicy.truncateUtf8(a.title, WearTakPluginContract.MAX_ALERT_TITLE_BYTES)
        val m = PluginStringPolicy.truncateUtf8(a.message, WearTakPluginContract.MAX_ALERT_MESSAGE_BYTES)
        val ch = a.channelId?.let { PluginStringPolicy.truncateUtf8(it, PluginStringPolicy.MAX_SHORT_STRING) }
        return ContractValidationResult.Ok(AlertRequestParcel(t, m, a.severity, ch))
    }

    fun validateToast(t: ToastRequestParcel): ContractValidationResult<ToastRequestParcel> {
        if (t.message.isBlank()) return ContractValidationResult.Err(ContractIssue("message", "required"))
        val m = PluginStringPolicy.truncateUtf8(t.message, WearTakPluginContract.MAX_TOAST_MESSAGE_BYTES)
        val tag = t.tag?.let { PluginStringPolicy.truncateUtf8(it, WearTakPluginContract.MAX_TOAST_TAG_BYTES) }
        val duration = when (t.duration) {
            ToastRequestParcel.DURATION_SHORT, ToastRequestParcel.DURATION_LONG -> t.duration
            else -> ToastRequestParcel.DURATION_SHORT
        }
        return ContractValidationResult.Ok(ToastRequestParcel(m, duration, tag))
    }

    fun validateMarker(m: MarkerParcel): ContractValidationResult<MarkerParcel> {
        if (m.markerId.isBlank()) return ContractValidationResult.Err(ContractIssue("markerId", "required"))
        val id = PluginStringPolicy.truncateUtf8(m.markerId, WearTakPluginContract.MAX_MARKER_ID_BYTES)
        val title = m.title?.let { PluginStringPolicy.truncateUtf8(it, WearTakPluginContract.MAX_ALERT_TITLE_BYTES) }
        return when (val b = BundleContractValidator.validateContentBundle(m.attributes)) {
            is ContractValidationResult.Ok -> ContractValidationResult.Ok(MarkerParcel(id, title, b.value))
            is ContractValidationResult.Err -> ContractValidationResult.Err(b.issues)
        }
    }

    fun validateCot(c: CotRequestParcel): ContractValidationResult<CotRequestParcel> {
        if (c.payloadFormat.isBlank()) return ContractValidationResult.Err(ContractIssue("payloadFormat", "required"))
        val f = PluginStringPolicy.truncateUtf8(c.payloadFormat, WearTakPluginContract.MAX_COT_PAYLOAD_FORMAT_BYTES)
        return when (val b = BundleContractValidator.validateContentBundle(c.payload)) {
            is ContractValidationResult.Ok -> ContractValidationResult.Ok(CotRequestParcel(f, b.value, c.priority))
            is ContractValidationResult.Err -> ContractValidationResult.Err(b.issues)
        }
    }
}
