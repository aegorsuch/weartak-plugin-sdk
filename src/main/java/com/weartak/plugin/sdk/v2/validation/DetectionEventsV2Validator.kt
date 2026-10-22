package com.weartak.plugin.sdk.v2.validation

import com.weartak.plugin.sdk.validation.CepRulesValidator
import com.weartak.plugin.sdk.validation.ContractIssue
import com.weartak.plugin.sdk.validation.ContractValidationResult
import com.weartak.plugin.sdk.validation.PluginStringPolicy
import com.weartak.plugin.sdk.v2.WearTakPluginV2Contract
import com.weartak.plugin.sdk.v2.model.DetectionEventParcel

object DetectionEventsV2Validator {

    fun validate(event: DetectionEventParcel): ContractValidationResult<DetectionEventParcel> {
        if (event.pluginId.isBlank()) {
            return ContractValidationResult.Err(ContractIssue("pluginId", "required"))
        }
        if (event.streamKey.isBlank()) {
            return ContractValidationResult.Err(ContractIssue("streamKey", "required"))
        }
        if (event.detectionId.isBlank()) {
            return ContractValidationResult.Err(ContractIssue("detectionId", "required"))
        }
        if (event.sequence < 0L) {
            return ContractValidationResult.Err(ContractIssue("sequence", "must be >= 0"))
        }
        if (event.timestampUtcMillis < 0L) {
            return ContractValidationResult.Err(ContractIssue("timestampUtcMillis", "must be >= 0"))
        }
        if (event.classificationLabel.isBlank()) {
            return ContractValidationResult.Err(ContractIssue("classificationLabel", "required"))
        }
        if (event.confidence >= 0f && event.confidence > 1f) {
            return ContractValidationResult.Err(ContractIssue("confidence", "must be in [0, 1]"))
        }
        event.attributesJson?.let { json ->
            if (!CepRulesValidator.isProbablyJsonObject(json)) {
                return ContractValidationResult.Err(ContractIssue("attributesJson", "must be a JSON object"))
            }
            if (json.toByteArray(Charsets.UTF_8).size > WearTakPluginV2Contract.MAX_DETECTION_ATTRIBUTES_JSON_BYTES) {
                return ContractValidationResult.Err(ContractIssue("attributesJson", "too large"))
            }
        }
        return ContractValidationResult.Ok(sanitize(event))
    }

    private fun sanitize(event: DetectionEventParcel): DetectionEventParcel =
        DetectionEventParcel(
            pluginId = PluginStringPolicy.truncateUtf8(event.pluginId, WearTakPluginV2Contract.MAX_PLUGIN_ID_BYTES),
            streamKey = PluginStringPolicy.truncateUtf8(event.streamKey, WearTakPluginV2Contract.MAX_DETECTION_STREAM_KEY_BYTES),
            detectionId = PluginStringPolicy.truncateUtf8(event.detectionId, WearTakPluginV2Contract.MAX_DETECTION_ID_BYTES),
            sequence = event.sequence,
            timestampUtcMillis = event.timestampUtcMillis,
            classificationLabel = PluginStringPolicy.truncateUtf8(
                event.classificationLabel,
                WearTakPluginV2Contract.MAX_CLASSIFICATION_LABEL_BYTES,
            ),
            confidence = event.confidence,
            attributesJson = event.attributesJson?.let {
                PluginStringPolicy.truncateUtf8(it, WearTakPluginV2Contract.MAX_DETECTION_ATTRIBUTES_JSON_BYTES)
            },
        )
}
