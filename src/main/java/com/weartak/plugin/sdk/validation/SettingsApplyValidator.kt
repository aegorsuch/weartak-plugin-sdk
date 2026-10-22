package com.weartak.plugin.sdk.validation

import android.os.Bundle
import com.weartak.plugin.sdk.HostSettingsContract
import com.weartak.plugin.sdk.model.HostSettingsParcel
import com.weartak.plugin.sdk.model.SettingsApplyMode
import com.weartak.plugin.sdk.model.SettingsApplyResultParcel
import com.weartak.plugin.sdk.model.SettingsFieldParcel
import com.weartak.plugin.sdk.model.SettingsFieldTypes
import com.weartak.plugin.sdk.model.SettingsSchemaParcel

/**
 * Validates and merges hosted-settings [Bundle] payloads against a [SettingsSchemaParcel].
 *
 * Supports [SettingsFieldTypes.TOGGLE], [SettingsFieldTypes.STRING], [SettingsFieldTypes.SELECT], and
 * [SettingsFieldTypes.NUMBER]. Unknown keys are rejected; omitted keys preserve [previous] values
 * unless [SettingsApplyMode.FULL_REPLACEMENT] or host-driven recompute applies.
 */
object SettingsApplyValidator {

    data class SettingsMergeResult(
        val values: Bundle,
        /** True if any `requiresRestart` field's value changed. */
        val restartRequiredChanged: Boolean,
        /** True if any known field value changed vs [previous]. */
        val anyValueChanged: Boolean,
        val hostDerivedFieldIds: List<String> = emptyList(),
    )

    fun mergeSettings(
        schema: SettingsSchemaParcel,
        previous: Bundle,
        incoming: Bundle?,
        hostSettings: HostSettingsParcel? = null,
        applyMode: Int = SettingsApplyMode.PARTIAL_PATCH,
    ): ContractValidationResult<SettingsMergeResult> {
        if (incoming == null) {
            return ContractValidationResult.Err(ContractIssue("settings", "Bundle is null"))
        }
        val effectiveIncoming = Bundle(incoming)
        if (applyMode == SettingsApplyMode.HOST_DRIVEN_RECOMPUTE && hostSettings == null) {
            return ContractValidationResult.Err(
                ContractIssue("settings.host", "HOST_DRIVEN_RECOMPUTE requires host snapshot"),
            )
        }

        val allowed = schema.fields.map { it.fieldId }.toSet()
        for (key in effectiveIncoming.keySet()) {
            if (key !in allowed) {
                return ContractValidationResult.Err(ContractIssue("settings.unknown_key", key))
            }
        }

        for (field in schema.fields) {
            if (field.readOnly && field.hostSettingKey != null && effectiveIncoming.containsKey(field.fieldId)) {
                return ContractValidationResult.Err(
                    ContractIssue(
                        "settings.read_only",
                        "${field.fieldId} is host-bound read-only",
                    ),
                )
            }
        }

        if (applyMode == SettingsApplyMode.FULL_REPLACEMENT) {
            val missing = schema.fields.map { it.fieldId }.filter { !effectiveIncoming.containsKey(it) }
            if (missing.isNotEmpty()) {
                return ContractValidationResult.Err(
                    ContractIssue("settings.full_replace", "Missing keys: ${missing.joinToString()}"),
                )
            }
        }

        val hostDerivedIds = mutableListOf<String>()
        val merged = Bundle()
        for (field in schema.fields) {
            val useHostValue = shouldApplyHostValue(field, applyMode, hostSettings, effectiveIncoming)
            if (useHostValue && hostSettings != null) {
                when (val hostResolved = resolveAndCoerceHostField(field, hostSettings)) {
                    is ContractValidationResult.Ok -> {
                        writeFieldValue(merged, field, hostResolved.value)
                        hostDerivedIds += field.fieldId
                        continue
                    }
                    is ContractValidationResult.Err -> return ContractValidationResult.Err(hostResolved.issues)
                }
            }
            when (val parsed = parseFieldValue(field, previous, effectiveIncoming, applyMode)) {
                is ContractValidationResult.Ok -> writeFieldValue(merged, field, parsed.value)
                is ContractValidationResult.Err -> return ContractValidationResult.Err(parsed.issues)
            }
        }

        if (hostSettings != null) {
            applyHostVisibilityCoercion(schema.fields, merged, hostSettings)
        }
        applyVisibilityCoercion(schema.fields, merged)
        val restartRequiredChanged = schema.fields.any { field ->
            field.requiresRestart && fieldValueChanged(field, previous, merged)
        }
        val anyValueChanged = schema.fields.any { field ->
            fieldValueChanged(field, previous, merged)
        }
        return ContractValidationResult.Ok(
            SettingsMergeResult(
                values = merged,
                restartRequiredChanged = restartRequiredChanged,
                anyValueChanged = anyValueChanged,
                hostDerivedFieldIds = hostDerivedIds.distinct(),
            ),
        )
    }

    fun applySettingsWithResult(
        schema: SettingsSchemaParcel,
        previous: Bundle,
        incoming: Bundle?,
        hostSettings: HostSettingsParcel? = null,
        applyMode: Int = SettingsApplyMode.PARTIAL_PATCH,
    ): SettingsApplyResultParcel =
        when (val r = mergeSettings(schema, previous, incoming, hostSettings, applyMode)) {
            is ContractValidationResult.Ok -> SettingsApplyResultParcel(
                success = true,
                applyMode = applyMode,
                values = r.value.values,
                restartRequired = r.value.restartRequiredChanged,
                anyValueChanged = r.value.anyValueChanged,
                hostDerivedFieldIds = r.value.hostDerivedFieldIds,
            )
            is ContractValidationResult.Err -> SettingsApplyResultParcel(
                success = false,
                applyMode = applyMode,
                values = Bundle(previous),
                errors = r.issues.toValidationErrors(),
            )
        }

    private fun shouldApplyHostValue(
        field: SettingsFieldParcel,
        applyMode: Int,
        hostSettings: HostSettingsParcel?,
        incoming: Bundle,
    ): Boolean {
        if (hostSettings == null || field.hostSettingKey.isNullOrBlank()) return false
        return when (applyMode) {
            SettingsApplyMode.HOST_DRIVEN_RECOMPUTE -> true
            SettingsApplyMode.PARTIAL_PATCH,
            SettingsApplyMode.FULL_REPLACEMENT,
            -> field.readOnly || !incoming.containsKey(field.fieldId)
            else -> field.readOnly || !incoming.containsKey(field.fieldId)
        }
    }

    private fun resolveAndCoerceHostField(
        field: SettingsFieldParcel,
        host: HostSettingsParcel,
    ): ContractValidationResult<Any> {
        val key = field.hostSettingKey.orEmpty()
        if (key !in HostSettingsContract.KNOWN_HOST_SETTING_KEYS) {
            return ContractValidationResult.Err(
                ContractIssue("settings.host_binding", "Unknown hostSettingKey $key for ${field.fieldId}"),
            )
        }
        val raw = HostSettingsResolver.resolveHostValue(field, host)
            ?: return ContractValidationResult.Err(
                ContractIssue("settings.host_binding", "No host value for $key"),
            )
        return HostSettingsResolver.coerceHostValueToField(field, raw)
    }

    private fun parseFieldValue(
        field: SettingsFieldParcel,
        previous: Bundle,
        incoming: Bundle,
        applyMode: Int,
    ): ContractValidationResult<Any> {
        val fieldId = field.fieldId
        if (!incoming.containsKey(fieldId)) {
            if (applyMode == SettingsApplyMode.FULL_REPLACEMENT) {
                return ContractValidationResult.Err(
                    ContractIssue("settings.full_replace", "Missing $fieldId"),
                )
            }
            return ContractValidationResult.Ok(readPreviousOrDefault(field, previous))
        }
        @Suppress("DEPRECATION")
        val raw = incoming.get(fieldId)
        return when (field.fieldType.uppercase()) {
            SettingsFieldTypes.TOGGLE -> when (raw) {
                is Boolean -> ContractValidationResult.Ok(raw)
                else -> ContractValidationResult.Err(
                    ContractIssue("settings.type", "$fieldId must be Boolean"),
                )
            }
            SettingsFieldTypes.STRING -> when (raw) {
                is String -> ContractValidationResult.Ok(raw)
                else -> ContractValidationResult.Err(
                    ContractIssue("settings.type", "$fieldId must be String"),
                )
            }
            SettingsFieldTypes.SELECT -> when (raw) {
                is String -> {
                    val allowed = field.options.map { it.value }.toSet()
                    if (raw !in allowed) {
                        ContractValidationResult.Err(
                            ContractIssue("settings.select", "$fieldId value not in options"),
                        )
                    } else {
                        ContractValidationResult.Ok(raw)
                    }
                }
                else -> ContractValidationResult.Err(
                    ContractIssue("settings.type", "$fieldId must be String"),
                )
            }
            SettingsFieldTypes.NUMBER -> when (raw) {
                is Double -> ContractValidationResult.Ok(raw)
                is Float -> ContractValidationResult.Ok(raw.toDouble())
                is Int -> ContractValidationResult.Ok(raw.toDouble())
                is Long -> ContractValidationResult.Ok(raw.toDouble())
                else -> ContractValidationResult.Err(
                    ContractIssue("settings.type", "$fieldId must be numeric"),
                )
            }
            else -> ContractValidationResult.Err(
                ContractIssue("settings.unsupported_type", fieldId),
            )
        }
    }

    private fun readPreviousOrDefault(field: SettingsFieldParcel, previous: Bundle): Any {
        if (previous.containsKey(field.fieldId)) {
            return readPreviousValue(field, previous)
        }
        return parseDefaultLiteral(field) ?: readPreviousValue(field, previous)
    }

    private fun parseDefaultLiteral(field: SettingsFieldParcel): Any? {
        val lit = field.defaultValueLiteral?.trim().orEmpty()
        if (lit.isEmpty()) return null
        return when (field.fieldType.uppercase()) {
            SettingsFieldTypes.TOGGLE -> when (lit.lowercase()) {
                "true" -> true
                "false" -> false
                else -> null
            }
            SettingsFieldTypes.STRING, SettingsFieldTypes.SELECT -> lit
            SettingsFieldTypes.NUMBER -> lit.toDoubleOrNull()
            else -> null
        }
    }

    private fun readPreviousValue(field: SettingsFieldParcel, previous: Bundle): Any =
        when (field.fieldType.uppercase()) {
            SettingsFieldTypes.TOGGLE -> previous.getBoolean(field.fieldId, false)
            SettingsFieldTypes.STRING -> previous.getString(field.fieldId) ?: ""
            SettingsFieldTypes.SELECT ->
                previous.getString(field.fieldId) ?: field.options.firstOrNull()?.value.orEmpty()
            SettingsFieldTypes.NUMBER -> readNumber(previous, field.fieldId, 0.0)
            else -> false
        }

    private fun writeFieldValue(bundle: Bundle, field: SettingsFieldParcel, value: Any) {
        when (field.fieldType.uppercase()) {
            SettingsFieldTypes.TOGGLE -> bundle.putBoolean(field.fieldId, value as Boolean)
            SettingsFieldTypes.STRING -> bundle.putString(field.fieldId, value as String)
            SettingsFieldTypes.SELECT -> bundle.putString(field.fieldId, value as String)
            SettingsFieldTypes.NUMBER -> bundle.putDouble(field.fieldId, value as Double)
        }
    }

    private fun fieldValueChanged(field: SettingsFieldParcel, previous: Bundle, merged: Bundle): Boolean {
        val prev = readPreviousValue(field, previous)
        val next = readPreviousValue(field, merged)
        return when (field.fieldType.uppercase()) {
            SettingsFieldTypes.NUMBER -> {
                val p = prev as Double
                val n = next as Double
                p != n && !(p.isNaN() && n.isNaN())
            }
            else -> prev != next
        }
    }

    private fun readNumber(bundle: Bundle, key: String, default: Double): Double {
        if (!bundle.containsKey(key)) return default
        @Suppress("DEPRECATION")
        return when (val raw = bundle.get(key)) {
            is Double -> raw
            is Float -> raw.toDouble()
            is Int -> raw.toDouble()
            is Long -> raw.toDouble()
            else -> default
        }
    }

    private fun applyVisibilityCoercion(fields: List<SettingsFieldParcel>, values: Bundle) {
        val toggleSnapshot = buildToggleSnapshot(fields, values)
        for (field in fields) {
            val key = field.visibleWhenKey ?: continue
            val lit = field.visibleWhenLiteral ?: continue
            if (!isFieldVisible(field, key, lit, toggleSnapshot)) {
                writeFieldValue(values, field, hiddenDefault(field))
            }
        }
    }

    private fun applyHostVisibilityCoercion(
        fields: List<SettingsFieldParcel>,
        values: Bundle,
        host: HostSettingsParcel,
    ) {
        for (field in fields) {
            val key = field.visibleWhenHostKey ?: continue
            val lit = field.visibleWhenHostLiteral ?: continue
            if (!isHostFieldVisible(field, key, lit, host)) {
                writeFieldValue(values, field, hiddenDefault(field))
            }
        }
    }

    private fun buildToggleSnapshot(fields: List<SettingsFieldParcel>, values: Bundle): Map<String, Boolean> {
        val map = LinkedHashMap<String, Boolean>()
        for (field in fields) {
            if (field.fieldType.uppercase() == SettingsFieldTypes.TOGGLE) {
                map[field.fieldId] = values.getBoolean(field.fieldId)
            }
        }
        return map
    }

    private fun hiddenDefault(field: SettingsFieldParcel): Any =
        parseDefaultLiteral(field) ?: when (field.fieldType.uppercase()) {
            SettingsFieldTypes.TOGGLE -> false
            SettingsFieldTypes.STRING -> ""
            SettingsFieldTypes.SELECT -> field.options.firstOrNull()?.value.orEmpty()
            SettingsFieldTypes.NUMBER -> 0.0
            else -> false
        }

    private fun isFieldVisible(
        field: SettingsFieldParcel,
        condKey: String,
        condLiteral: String,
        toggleValues: Map<String, Boolean>,
    ): Boolean {
        val lhs = toggleValues[condKey] ?: false
        val rhsBool = when (condLiteral) {
            "true" -> true
            "false" -> false
            else -> null
        }
        val positive = if (rhsBool != null) {
            lhs == rhsBool
        } else {
            false
        }
        return if (field.visibilityNegated) !positive else positive
    }

    private fun isHostFieldVisible(
        field: SettingsFieldParcel,
        hostKey: String,
        hostLiteral: String,
        host: HostSettingsParcel,
    ): Boolean {
        val lhs = HostSettingsResolver.hostLiteralValue(host, hostKey).orEmpty()
        val rhsBool = when (hostLiteral.lowercase()) {
            "true" -> true
            "false" -> false
            else -> null
        }
        val positive = if (rhsBool != null && hostKey in HOST_BOOLEAN_KEYS) {
            lhs.equals(hostLiteral, ignoreCase = true)
        } else {
            lhs == hostLiteral
        }
        return if (field.hostVisibilityNegated) !positive else positive
    }

    private val HOST_BOOLEAN_KEYS = setOf(
        HostSettingsContract.KEY_TAK_CONNECTED,
        HostSettingsContract.KEY_SITX_CONNECTED,
        HostSettingsContract.KEY_COT_CONNECTED,
    )
}
