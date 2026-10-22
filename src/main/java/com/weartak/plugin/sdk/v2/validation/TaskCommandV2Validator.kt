package com.weartak.plugin.sdk.v2.validation

import com.weartak.plugin.sdk.validation.CepRulesValidator
import com.weartak.plugin.sdk.validation.ContractIssue
import com.weartak.plugin.sdk.validation.ContractValidationResult
import com.weartak.plugin.sdk.validation.PluginStringPolicy
import com.weartak.plugin.sdk.v2.WearTakPluginV2Contract
import com.weartak.plugin.sdk.v2.model.TaskCommandCompletionParcel
import com.weartak.plugin.sdk.v2.model.TaskCommandOutcome
import com.weartak.plugin.sdk.v2.model.TaskCommandProgressParcel
import com.weartak.plugin.sdk.v2.model.TaskCommandSubmitParcel

object TaskCommandV2Validator {

    fun validateSubmit(request: TaskCommandSubmitParcel): ContractValidationResult<TaskCommandSubmitParcel> {
        validateIds(request.pluginId, request.commandId, request.sequence)?.let { return it }
        if (request.taskType.isBlank()) {
            return ContractValidationResult.Err(ContractIssue("taskType", "required"))
        }
        return when (val payload = validateJsonPayload(request.payloadJson, WearTakPluginV2Contract.MAX_TASK_PAYLOAD_JSON_BYTES)) {
            is ContractValidationResult.Ok -> ContractValidationResult.Ok(sanitizeSubmit(request, payload.value))
            is ContractValidationResult.Err -> ContractValidationResult.Err(payload.issues)
        }
    }

    fun validateProgress(progress: TaskCommandProgressParcel): ContractValidationResult<TaskCommandProgressParcel> {
        validateIds(progress.pluginId, progress.commandId, progress.sequence)?.let { return it }
        if (progress.progressPercent !in 0..100) {
            return ContractValidationResult.Err(ContractIssue("progressPercent", "must be 0..100"))
        }
        return ContractValidationResult.Ok(sanitizeProgress(progress))
    }

    fun validateCompletion(
        completion: TaskCommandCompletionParcel,
    ): ContractValidationResult<TaskCommandCompletionParcel> {
        validateIds(completion.pluginId, completion.commandId, completion.sequence)?.let { return it }
        when (completion.outcome) {
            TaskCommandOutcome.SUCCESS, TaskCommandOutcome.FAILED, TaskCommandOutcome.CANCELLED -> Unit
            else -> return ContractValidationResult.Err(ContractIssue("outcome", "unsupported"))
        }
        completion.resultJson?.let { json ->
            if (!CepRulesValidator.isProbablyJsonObject(json)) {
                return ContractValidationResult.Err(ContractIssue("resultJson", "must be a JSON object"))
            }
            if (json.toByteArray(Charsets.UTF_8).size > WearTakPluginV2Contract.MAX_TASK_RESULT_JSON_BYTES) {
                return ContractValidationResult.Err(ContractIssue("resultJson", "too large"))
            }
        }
        return ContractValidationResult.Ok(sanitizeCompletion(completion))
    }

    private fun validateIds(
        pluginId: String,
        commandId: String,
        sequence: Long,
    ): ContractValidationResult<Nothing>? {
        if (pluginId.isBlank()) return ContractValidationResult.Err(ContractIssue("pluginId", "required"))
        if (commandId.isBlank()) return ContractValidationResult.Err(ContractIssue("commandId", "required"))
        if (sequence < 0L) return ContractValidationResult.Err(ContractIssue("sequence", "must be >= 0"))
        return null
    }

    private fun validateJsonPayload(
        json: String,
        maxBytes: Int,
    ): ContractValidationResult<String> {
        if (json.isBlank()) return ContractValidationResult.Err(ContractIssue("payloadJson", "required"))
        if (!CepRulesValidator.isProbablyJsonObject(json)) {
            return ContractValidationResult.Err(ContractIssue("payloadJson", "must be a JSON object"))
        }
        if (json.toByteArray(Charsets.UTF_8).size > maxBytes) {
            return ContractValidationResult.Err(ContractIssue("payloadJson", "too large"))
        }
        return ContractValidationResult.Ok(json)
    }

    private fun sanitizeSubmit(request: TaskCommandSubmitParcel, payload: String): TaskCommandSubmitParcel =
        TaskCommandSubmitParcel(
            pluginId = PluginStringPolicy.truncateUtf8(request.pluginId, WearTakPluginV2Contract.MAX_PLUGIN_ID_BYTES),
            commandId = PluginStringPolicy.truncateUtf8(request.commandId, WearTakPluginV2Contract.MAX_TASK_COMMAND_ID_BYTES),
            taskType = PluginStringPolicy.truncateUtf8(request.taskType, WearTakPluginV2Contract.MAX_TASK_TYPE_BYTES),
            sequence = request.sequence,
            payloadJson = PluginStringPolicy.truncateUtf8(payload, WearTakPluginV2Contract.MAX_TASK_PAYLOAD_JSON_BYTES),
        )

    private fun sanitizeProgress(progress: TaskCommandProgressParcel): TaskCommandProgressParcel =
        TaskCommandProgressParcel(
            pluginId = PluginStringPolicy.truncateUtf8(progress.pluginId, WearTakPluginV2Contract.MAX_PLUGIN_ID_BYTES),
            commandId = PluginStringPolicy.truncateUtf8(progress.commandId, WearTakPluginV2Contract.MAX_TASK_COMMAND_ID_BYTES),
            sequence = progress.sequence,
            progressPercent = progress.progressPercent,
            message = progress.message?.let {
                PluginStringPolicy.truncateUtf8(it, WearTakPluginV2Contract.MAX_TASK_MESSAGE_BYTES)
            },
        )

    private fun sanitizeCompletion(completion: TaskCommandCompletionParcel): TaskCommandCompletionParcel =
        TaskCommandCompletionParcel(
            pluginId = PluginStringPolicy.truncateUtf8(completion.pluginId, WearTakPluginV2Contract.MAX_PLUGIN_ID_BYTES),
            commandId = PluginStringPolicy.truncateUtf8(completion.commandId, WearTakPluginV2Contract.MAX_TASK_COMMAND_ID_BYTES),
            sequence = completion.sequence,
            outcome = completion.outcome,
            message = completion.message?.let {
                PluginStringPolicy.truncateUtf8(it, WearTakPluginV2Contract.MAX_TASK_MESSAGE_BYTES)
            },
            resultJson = completion.resultJson?.let {
                PluginStringPolicy.truncateUtf8(it, WearTakPluginV2Contract.MAX_TASK_RESULT_JSON_BYTES)
            },
        )
}
