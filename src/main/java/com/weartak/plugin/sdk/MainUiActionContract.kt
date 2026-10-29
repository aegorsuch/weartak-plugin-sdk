package com.weartak.plugin.sdk

import android.os.Bundle
import com.weartak.plugin.sdk.validation.BundleContractValidator
import com.weartak.plugin.sdk.validation.ContractIssue
import com.weartak.plugin.sdk.validation.ContractValidationResult
import org.json.JSONException
import org.json.JSONObject

/** ACTION card content. Only an explicit user tap may dispatch [Action.actionId]. */
object MainUiActionContract {
    const val ACTION_ID = "actionId"
    const val ACTION_LABEL = "actionLabel"
    const val ENABLED = "enabled"
    const val ACTION_ARGS_JSON = "actionArgsJson"
    const val MAX_ACTION_ID_BYTES = 128
    const val MAX_ACTION_LABEL_BYTES = 256

    data class Action(
        val actionId: String,
        val actionLabel: String,
        val enabled: Boolean,
        val args: Bundle,
    )

    fun createContent(
        actionId: String,
        actionLabel: String,
        enabled: Boolean = true,
        args: Map<String, String> = emptyMap(),
    ): Bundle {
        val content = Bundle().apply {
            putString(ACTION_ID, actionId)
            putString(ACTION_LABEL, actionLabel)
            putBoolean(ENABLED, enabled)
            putString(ACTION_ARGS_JSON, JSONObject(args).toString())
        }
        parseContent(content).toResult().getOrThrow()
        return content
    }

    @Suppress("DEPRECATION")
    fun parseContent(content: Bundle): ContractValidationResult<Action> {
        val issues = mutableListOf<ContractIssue>()
        val actionId = content.get(ACTION_ID)
        val actionLabel = content.get(ACTION_LABEL)
        val enabled = if (content.containsKey(ENABLED)) content.get(ENABLED) else true
        val argsJson = if (content.containsKey(ACTION_ARGS_JSON)) content.get(ACTION_ARGS_JSON) else ""
        if (actionId !is String || actionId.isBlank() ||
            actionId.toByteArray(Charsets.UTF_8).size > MAX_ACTION_ID_BYTES
        ) {
            issues += ContractIssue(ACTION_ID, "actionId requires a nonblank string of at most 128 UTF-8 bytes")
        }
        if (actionLabel !is String || actionLabel.isBlank() ||
            actionLabel.toByteArray(Charsets.UTF_8).size > MAX_ACTION_LABEL_BYTES
        ) {
            issues += ContractIssue(ACTION_LABEL, "actionLabel requires a nonblank string of at most 256 UTF-8 bytes")
        }
        if (enabled !is Boolean) issues += ContractIssue(ENABLED, "enabled requires a Boolean")
        if (argsJson !is String) issues += ContractIssue(ACTION_ARGS_JSON, "actionArgsJson requires a string")
        when (val result = BundleContractValidator.validateContentBundle(content)) {
            is ContractValidationResult.Err -> issues += result.issues
            is ContractValidationResult.Ok -> Unit
        }
        if (issues.isNotEmpty() || actionId !is String || actionLabel !is String || enabled !is Boolean) {
            return ContractValidationResult.Err(issues)
        }

        val args = Bundle()
        if (argsJson is String && argsJson.isNotEmpty()) {
            try {
                // JSONArray, scalar, null, and trailing data must not become successful empty arguments.
                val value = org.json.JSONTokener(argsJson)
                val json = value.nextValue()
                if (json !is JSONObject || value.nextClean() != '\u0000') {
                    return ContractValidationResult.Err(ContractIssue(ACTION_ARGS_JSON, "Expected a JSON object"))
                }
                for (key in json.keys()) {
                    val argument = json.get(key)
                    if (argument !is String) {
                        issues += ContractIssue(ACTION_ARGS_JSON, "Every argument requires a string")
                    } else {
                        args.putString(key, argument)
                    }
                }
            } catch (_: JSONException) {
                return ContractValidationResult.Err(ContractIssue(ACTION_ARGS_JSON, "Invalid JSON object"))
            }
        }
        when (val result = BundleContractValidator.validateContentBundle(args)) {
            is ContractValidationResult.Err -> issues += result.issues
            is ContractValidationResult.Ok -> Unit
        }
        return if (issues.isEmpty()) {
            ContractValidationResult.Ok(Action(actionId, actionLabel, enabled, args))
        } else {
            ContractValidationResult.Err(issues)
        }
    }
}
