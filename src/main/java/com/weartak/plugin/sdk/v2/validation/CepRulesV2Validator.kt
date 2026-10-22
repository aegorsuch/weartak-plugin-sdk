package com.weartak.plugin.sdk.v2.validation

import com.weartak.plugin.sdk.validation.CepRulesValidator
import com.weartak.plugin.sdk.validation.ContractIssue
import com.weartak.plugin.sdk.validation.ContractValidationResult
import com.weartak.plugin.sdk.validation.PluginStringPolicy
import com.weartak.plugin.sdk.v2.WearTakPluginV2Contract
import com.weartak.plugin.sdk.v2.model.CepRuleStreamEventParcel
import com.weartak.plugin.sdk.v2.model.CepStreamOperation

/**
 * Advisory structural guard for v2 CEP stream events. Host remains authoritative for schema and admission.
 */
object CepRulesV2Validator {

    fun validateStreamEvent(
        event: CepRuleStreamEventParcel,
    ): ContractValidationResult<CepRuleStreamEventParcel> {
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
            CepStreamOperation.ADD, CepStreamOperation.UPDATE -> {
                val json = event.rulesJson
                    ?: return ContractValidationResult.Err(ContractIssue("rulesJson", "required"))
                when (val payload = validateRulesPayload(event.pluginId, event.sensorKey, json)) {
                    is ContractValidationResult.Ok -> Unit
                    is ContractValidationResult.Err -> return ContractValidationResult.Err(payload.issues)
                }
            }
            CepStreamOperation.REMOVE -> {
                if (!event.rulesJson.isNullOrBlank()) {
                    return ContractValidationResult.Err(ContractIssue("rulesJson", "must be null for REMOVE"))
                }
            }
            else -> return ContractValidationResult.Err(ContractIssue("operation", "unsupported"))
        }
        return ContractValidationResult.Ok(sanitize(event))
    }

    private fun validateRulesPayload(
        pluginId: String,
        sensorKey: String,
        rulesJson: String,
    ): ContractValidationResult<String> {
        if (pluginId.isBlank()) return ContractValidationResult.Err(ContractIssue("pluginId", "required"))
        if (sensorKey.isBlank()) return ContractValidationResult.Err(ContractIssue("sensorKey", "required"))
        if (!CepRulesValidator.validateSize(rulesJson)) {
            return ContractValidationResult.Err(ContractIssue("rulesJson", "too large"))
        }
        if (!CepRulesValidator.isProbablyJsonObject(rulesJson)) {
            return ContractValidationResult.Err(ContractIssue("rulesJson", "must be a JSON object"))
        }
        return ContractValidationResult.Ok(rulesJson)
    }

    private fun sanitize(event: CepRuleStreamEventParcel): CepRuleStreamEventParcel =
        CepRuleStreamEventParcel(
            operation = event.operation,
            pluginId = PluginStringPolicy.truncateUtf8(event.pluginId, WearTakPluginV2Contract.MAX_PLUGIN_ID_BYTES),
            sensorKey = PluginStringPolicy.truncateUtf8(event.sensorKey, WearTakPluginV2Contract.MAX_SENSOR_KEY_BYTES),
            ruleSetId = PluginStringPolicy.truncateUtf8(event.ruleSetId, WearTakPluginV2Contract.MAX_CEP_RULE_SET_ID_BYTES),
            sequence = event.sequence,
            rulesJson = event.rulesJson?.let {
                PluginStringPolicy.truncateUtf8(it, PluginStringPolicy.MAX_CEP_JSON_LENGTH)
            },
        )
}
