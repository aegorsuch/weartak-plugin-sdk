package com.weartak.plugin.sdk

import android.os.Bundle
import com.weartak.plugin.sdk.validation.BundleContractValidator
import com.weartak.plugin.sdk.validation.ContractIssue
import com.weartak.plugin.sdk.validation.ContractValidationResult
import org.json.JSONException
import org.json.JSONObject

/** ACTION card content. Only an explicit user tap may dispatch [Action.actionId]. */
object MainUiActionContract {
    const val HOST_PRESENTATION_VERSION_KEY = "com.weartak.hostedUi.actionPresentationVersion"
    const val PRESENTATION_VERSION = 1
    const val ACTION_ID = "actionId"
    const val ACTION_LABEL = "actionLabel"
    const val ENABLED = "enabled"
    const val ACTION_ARGS_JSON = "actionArgsJson"
    const val ACTION_PRESENTATION = "actionPresentation"
    const val ACTION_ICON = "actionIcon"
    const val ACTION_PLACEMENT = "actionPlacement"
    const val ACTION_GROUP = "actionGroup"
    const val SHOW_TITLE = "showTitle"
    const val TEXT = "TEXT"
    const val ICON = "ICON"
    const val NOTE = "NOTE"
    const val BLOODHOUND = "BLOODHOUND"
    const val STOP = "STOP"
    const val TARGET = "TARGET"
    const val BACK = "BACK"
    const val BODY = "BODY"
    const val HEADER_END = "HEADER_END"
    const val MAX_ACTION_ID_BYTES = 128
    const val MAX_ACTION_LABEL_BYTES = 256
    const val MAX_ACTION_GROUP_BYTES = 128

    private val allowedKeys = setOf(
        ACTION_ID, ACTION_LABEL, ENABLED, ACTION_ARGS_JSON, ACTION_PRESENTATION,
        ACTION_ICON, ACTION_PLACEMENT, ACTION_GROUP, SHOW_TITLE,
    )
    private val icons = setOf(NOTE, BLOODHOUND, STOP, TARGET, BACK)

    data class Action @JvmOverloads constructor(
        val actionId: String,
        val actionLabel: String,
        val enabled: Boolean,
        val args: Bundle,
        val actionPresentation: String = TEXT,
        val actionIcon: String? = null,
        val actionPlacement: String = BODY,
        val actionGroup: String? = null,
        val showTitle: Boolean = true,
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

    /** Extended presentation without changing the legacy [createContent] JVM signatures. */
    fun createPresentedContent(
        actionId: String,
        actionLabel: String,
        actionPresentation: String,
        actionIcon: String? = null,
        actionPlacement: String = BODY,
        actionGroup: String? = null,
        showTitle: Boolean = true,
        enabled: Boolean = true,
        args: Map<String, String> = emptyMap(),
    ): Bundle {
        val content = createContent(actionId, actionLabel, enabled, args).apply {
            putString(ACTION_PRESENTATION, actionPresentation)
            actionIcon?.let { putString(ACTION_ICON, it) }
            putString(ACTION_PLACEMENT, actionPlacement)
            actionGroup?.let { putString(ACTION_GROUP, it) }
            putBoolean(SHOW_TITLE, showTitle)
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
        val presentation = if (content.containsKey(ACTION_PRESENTATION)) content.get(ACTION_PRESENTATION) else TEXT
        val icon = content.get(ACTION_ICON)
        val placement = if (content.containsKey(ACTION_PLACEMENT)) content.get(ACTION_PLACEMENT) else BODY
        val group = content.get(ACTION_GROUP)
        val showTitle = if (content.containsKey(SHOW_TITLE)) content.get(SHOW_TITLE) else true
        if (content.keySet().any { it !in allowedKeys }) {
            issues += ContractIssue("content", "Unknown ACTION content key")
        }
        if (presentation !is String || presentation !in setOf(TEXT, ICON)) {
            issues += ContractIssue(ACTION_PRESENTATION, "actionPresentation requires TEXT or ICON")
        }
        if (placement !is String || placement !in setOf(BODY, HEADER_END)) {
            issues += ContractIssue(ACTION_PLACEMENT, "actionPlacement requires BODY or HEADER_END")
        }
        if (content.containsKey(ACTION_ICON) && (icon !is String || icon !in icons)) {
            issues += ContractIssue(ACTION_ICON, "actionIcon requires NOTE, BLOODHOUND, STOP, TARGET or BACK")
        }
        if (content.containsKey(ACTION_GROUP) && (group !is String || group.isBlank() ||
                group.toByteArray(Charsets.UTF_8).size > MAX_ACTION_GROUP_BYTES)
        ) {
            issues += ContractIssue(ACTION_GROUP, "actionGroup requires a nonblank string of at most 128 UTF-8 bytes")
        }
        if (showTitle !is Boolean) issues += ContractIssue(SHOW_TITLE, "showTitle requires a Boolean")
        if (presentation == TEXT && (content.containsKey(ACTION_ICON) ||
                content.containsKey(ACTION_GROUP) || placement != BODY)
        ) {
            issues += ContractIssue(ACTION_PRESENTATION, "TEXT requires BODY placement without actionIcon or actionGroup")
        }
        if (presentation == ICON && (icon !is String || icon !in icons || showTitle != false)) {
            issues += ContractIssue(ACTION_PRESENTATION, "ICON requires actionIcon and showTitle=false")
        }
        if (content.containsKey(ACTION_GROUP) && (presentation != ICON || placement != BODY)) {
            issues += ContractIssue(ACTION_GROUP, "actionGroup requires BODY ICON")
        }
        if (placement == HEADER_END && (presentation != ICON || icon != BACK ||
                showTitle != false || content.containsKey(ACTION_GROUP))
        ) {
            issues += ContractIssue(ACTION_PLACEMENT, "HEADER_END requires BACK ICON, showTitle=false and no actionGroup")
        }
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
        if (issues.isNotEmpty() || actionId !is String || actionLabel !is String || enabled !is Boolean ||
            presentation !is String || placement !is String || showTitle !is Boolean
        ) {
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
            ContractValidationResult.Ok(Action(
                actionId, actionLabel, enabled, args, presentation, icon as? String,
                placement, group as? String, showTitle,
            ))
        } else {
            ContractValidationResult.Err(issues)
        }
    }
}
