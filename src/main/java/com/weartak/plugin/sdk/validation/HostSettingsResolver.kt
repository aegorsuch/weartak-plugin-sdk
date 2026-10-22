package com.weartak.plugin.sdk.validation

import com.weartak.plugin.sdk.HostSettingsContract
import com.weartak.plugin.sdk.model.HostSettingsParcel
import com.weartak.plugin.sdk.model.SettingsFieldParcel
import com.weartak.plugin.sdk.model.SettingsFieldTypes

/**
 * Resolves [com.weartak.plugin.sdk.model.SettingsFieldParcel.hostSettingKey] values from a
 * [HostSettingsParcel] for merge/validation.
 */
object HostSettingsResolver {

    fun resolveHostValue(field: SettingsFieldParcel, host: HostSettingsParcel): Any? {
        val key = field.hostSettingKey?.trim().orEmpty()
        if (key.isEmpty()) return null
        return when (key) {
            HostSettingsContract.KEY_CALLSIGN -> host.callsign
            HostSettingsContract.KEY_TEAM -> host.team
            HostSettingsContract.KEY_ROLE -> host.role
            HostSettingsContract.KEY_REPORTING_INTERVAL_SECS ->
                host.reportingIntervalSecs.toDouble()
            HostSettingsContract.KEY_DESIRED_REPORTING_INTERVAL_SECS ->
                host.desiredReportingIntervalSecs.toDouble()
            HostSettingsContract.KEY_TAK_CONNECTED -> host.takConnected
            HostSettingsContract.KEY_SITX_CONNECTED -> host.sitxConnected
            HostSettingsContract.KEY_COT_CONNECTED -> host.cotConnected
            else -> null
        }
    }

    fun hostLiteralValue(host: HostSettingsParcel, hostKey: String): String? =
        when (hostKey) {
            HostSettingsContract.KEY_CALLSIGN -> host.callsign
            HostSettingsContract.KEY_TEAM -> host.team
            HostSettingsContract.KEY_ROLE -> host.role
            HostSettingsContract.KEY_REPORTING_INTERVAL_SECS -> host.reportingIntervalSecs.toString()
            HostSettingsContract.KEY_DESIRED_REPORTING_INTERVAL_SECS ->
                host.desiredReportingIntervalSecs.toString()
            HostSettingsContract.KEY_TAK_CONNECTED -> host.takConnected.toString()
            HostSettingsContract.KEY_SITX_CONNECTED -> host.sitxConnected.toString()
            HostSettingsContract.KEY_COT_CONNECTED -> host.cotConnected.toString()
            else -> null
        }

    fun coerceHostValueToField(field: SettingsFieldParcel, raw: Any): ContractValidationResult<Any> {
        return when (field.fieldType.uppercase()) {
            SettingsFieldTypes.TOGGLE -> when (raw) {
                is Boolean -> ContractValidationResult.Ok(raw)
                else -> ContractValidationResult.Err(
                    ContractIssue("settings.host_type", "${field.fieldId} host binding is not Boolean"),
                )
            }
            SettingsFieldTypes.STRING, SettingsFieldTypes.SELECT -> when (raw) {
                is String -> ContractValidationResult.Ok(raw)
                is Number -> ContractValidationResult.Ok(raw.toString())
                is Boolean -> ContractValidationResult.Ok(raw.toString())
                else -> ContractValidationResult.Err(
                    ContractIssue("settings.host_type", "${field.fieldId} host binding is not String"),
                )
            }
            SettingsFieldTypes.NUMBER -> when (raw) {
                is Double -> ContractValidationResult.Ok(raw)
                is Float -> ContractValidationResult.Ok(raw.toDouble())
                is Int -> ContractValidationResult.Ok(raw.toDouble())
                is Long -> ContractValidationResult.Ok(raw.toDouble())
                else -> ContractValidationResult.Err(
                    ContractIssue("settings.host_type", "${field.fieldId} host binding is not numeric"),
                )
            }
            else -> ContractValidationResult.Err(
                ContractIssue("settings.unsupported_type", field.fieldId),
            )
        }
    }
}
