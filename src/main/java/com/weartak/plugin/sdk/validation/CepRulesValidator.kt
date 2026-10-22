package com.weartak.plugin.sdk.validation

import com.weartak.plugin.sdk.WearTakPluginContract
import com.weartak.plugin.sdk.model.CepRuleEventParcel
import com.weartak.plugin.sdk.model.CepRuleOperation
import org.json.JSONException
import org.json.JSONObject

/**
 * Lightweight client-side guard for CEP JSON and stream events. The host remains authoritative
 * for schema enforcement, ordering, idempotency, and runtime admission.
 */
object CepRulesValidator {

    fun isProbablyJsonObject(rulesJson: String): Boolean {
        val trimmed = rulesJson.trim()
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) return false
        return try {
            JSONObject(trimmed)
            true
        } catch (_: JSONException) {
            false
        }
    }

    fun validateSize(rulesJson: String): Boolean =
        rulesJson.toByteArray(Charsets.UTF_8).size <= PluginStringPolicy.MAX_CEP_JSON_LENGTH

    fun validateRegistration(
        pluginId: String,
        sensorKey: String,
        rulesJson: String,
    ): ContractValidationResult<String> =
        validateRulesPayload(pluginId, sensorKey, rulesJson)

    /**
     * Validates a streaming CEP event before [com.weartak.plugin.sdk.aidl.IWearTakPluginControl.applyCepRuleEvent].
     */
    fun validateEvent(event: CepRuleEventParcel): ContractValidationResult<CepRuleEventParcel> {
        if (event.pluginId.isBlank()) {
            return ContractValidationResult.Err(ContractIssue("pluginId", "required"))
        }
        if (event.sensorKey.isBlank()) {
            return ContractValidationResult.Err(ContractIssue("sensorKey", "required"))
        }
        if (event.ruleSetId.isBlank()) {
            return ContractValidationResult.Err(ContractIssue("ruleSetId", "required"))
        }
        if (event.sequence < 0L) {
            return ContractValidationResult.Err(ContractIssue("sequence", "must be >= 0"))
        }
        when (event.operation) {
            CepRuleOperation.ADD, CepRuleOperation.UPDATE -> {
                val json = event.rulesJson
                    ?: return ContractValidationResult.Err(ContractIssue("rulesJson", "required"))
                when (val payload = validateRulesPayload(event.pluginId, event.sensorKey, json)) {
                    is ContractValidationResult.Ok -> Unit
                    is ContractValidationResult.Err -> return ContractValidationResult.Err(payload.issues)
                }
            }
            CepRuleOperation.REMOVE -> {
                if (!event.rulesJson.isNullOrBlank()) {
                    return ContractValidationResult.Err(ContractIssue("rulesJson", "must be null for REMOVE"))
                }
            }
            else -> return ContractValidationResult.Err(ContractIssue("operation", "unsupported"))
        }
        val sanitized = CepRuleEventParcel(
            operation = event.operation,
            pluginId = PluginStringPolicy.truncateUtf8(event.pluginId, WearTakPluginContract.MAX_PLUGIN_ID_BYTES),
            sensorKey = PluginStringPolicy.truncateUtf8(event.sensorKey, WearTakPluginContract.MAX_SENSOR_KEY_BYTES),
            ruleSetId = PluginStringPolicy.truncateUtf8(event.ruleSetId, WearTakPluginContract.MAX_CEP_RULE_SET_ID_BYTES),
            sequence = event.sequence,
            rulesJson = event.rulesJson?.let {
                PluginStringPolicy.truncateUtf8(it, PluginStringPolicy.MAX_CEP_JSON_LENGTH)
            },
        )
        return ContractValidationResult.Ok(sanitized)
    }

    private fun validateRulesPayload(
        pluginId: String,
        sensorKey: String,
        rulesJson: String,
    ): ContractValidationResult<String> {
        if (pluginId.isBlank()) return ContractValidationResult.Err(ContractIssue("pluginId", "required"))
        if (sensorKey.isBlank()) return ContractValidationResult.Err(ContractIssue("sensorKey", "required"))
        if (!validateSize(rulesJson)) return ContractValidationResult.Err(ContractIssue("rulesJson", "too large"))
        if (!isProbablyJsonObject(rulesJson)) {
            return ContractValidationResult.Err(ContractIssue("rulesJson", "must be a JSON object"))
        }
        return ContractValidationResult.Ok(rulesJson)
    }
}
