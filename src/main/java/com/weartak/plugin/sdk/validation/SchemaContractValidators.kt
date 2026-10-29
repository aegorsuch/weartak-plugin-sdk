package com.weartak.plugin.sdk.validation

import com.weartak.plugin.sdk.HostSettingsContract
import com.weartak.plugin.sdk.MainUiActionContract
import com.weartak.plugin.sdk.WearTakPluginContract
import com.weartak.plugin.sdk.model.MainUiCardTypes
import com.weartak.plugin.sdk.model.PluginMainUiSchemaParcel
import com.weartak.plugin.sdk.model.SettingsFieldTypes
import com.weartak.plugin.sdk.model.SettingsSchemaParcel
import com.weartak.plugin.sdk.model.SensorEventParcel

object SettingsSchemaContractValidator {
    /** v1.x hosted settings field types accepted by schema and apply validators. */
    private val allowedFieldTypes = setOf(
        SettingsFieldTypes.TOGGLE,
        SettingsFieldTypes.STRING,
        SettingsFieldTypes.SELECT,
        SettingsFieldTypes.NUMBER,
    )

    fun sanitize(schema: SettingsSchemaParcel): SettingsSchemaParcel =
        schema.copy(
            title = schema.title?.let { PluginStringPolicy.truncateUtf8(it, WearTakPluginContract.MAX_DISPLAY_NAME_BYTES) },
            fields = schema.fields.map { f ->
                f.copy(
                    fieldId = PluginStringPolicy.truncateUtf8(f.fieldId, WearTakPluginContract.MAX_SETTINGS_FIELD_ID_BYTES),
                    label = PluginStringPolicy.truncateUtf8(f.label, WearTakPluginContract.MAX_SETTINGS_LABEL_BYTES),
                    fieldType = f.fieldType.uppercase(),
                    helpText = f.helpText?.let { PluginStringPolicy.truncateUtf8(it, PluginStringPolicy.MAX_LONG_STRING) },
                    visibleWhenKey = f.visibleWhenKey?.let { ConditionGrammar.validateKey(it) },
                    visibleWhenLiteral = f.visibleWhenLiteral?.let {
                        PluginStringPolicy.truncateUtf8(it, PluginStringPolicy.MAX_MEDIUM_STRING)
                    },
                    visibleWhenHostKey = f.visibleWhenHostKey?.let { sanitizeHostKey(it) },
                    visibleWhenHostLiteral = f.visibleWhenHostLiteral?.let {
                        PluginStringPolicy.truncateUtf8(it, PluginStringPolicy.MAX_MEDIUM_STRING)
                    },
                    hostSettingKey = f.hostSettingKey?.let { sanitizeHostKey(it) },
                    defaultValueLiteral = f.defaultValueLiteral?.let {
                        PluginStringPolicy.truncateUtf8(it, PluginStringPolicy.MAX_MEDIUM_STRING)
                    },
                    options = f.options.map { o ->
                        o.copy(
                            value = PluginStringPolicy.truncateUtf8(o.value, PluginStringPolicy.MAX_SHORT_STRING),
                            label = PluginStringPolicy.truncateUtf8(o.label, PluginStringPolicy.MAX_MEDIUM_STRING),
                        )
                    },
                )
            },
        )

    fun validate(schema: SettingsSchemaParcel): ContractValidationResult<SettingsSchemaParcel> {
        val issues = mutableListOf<ContractIssue>()
        if (schema.fields.size > WearTakPluginContract.MAX_SETTINGS_FIELDS) {
            issues += ContractIssue("fields", "Too many fields")
        }
        val ids = mutableSetOf<String>()
        for (field in schema.fields) {
            if (field.fieldId.isBlank()) issues += ContractIssue("fieldId", "Blank fieldId")
            if (!ids.add(field.fieldId)) issues += ContractIssue("fieldId", "Duplicate ${field.fieldId}")
            if (field.label.isBlank()) issues += ContractIssue("label", "Blank label for ${field.fieldId}")
            if (field.fieldType.uppercase() !in allowedFieldTypes) {
                issues += ContractIssue("fieldType", "Unsupported type ${field.fieldType}")
            }
            when (field.fieldType.uppercase()) {
                SettingsFieldTypes.SELECT -> {
                    if (field.options.isEmpty()) {
                        issues += ContractIssue("options", "SELECT requires at least one option for ${field.fieldId}")
                    } else {
                        val optionValues = field.options.map { it.value }
                        if (optionValues.size != optionValues.toSet().size) {
                            issues += ContractIssue("options", "Duplicate option values for ${field.fieldId}")
                        }
                    }
                }
                SettingsFieldTypes.TOGGLE,
                SettingsFieldTypes.STRING,
                SettingsFieldTypes.NUMBER,
                -> {
                    if (field.options.isNotEmpty()) {
                        issues += ContractIssue("options", "Options ignored for ${field.fieldType}; must be empty for ${field.fieldId}")
                    }
                }
            }
            val key = field.visibleWhenKey
            val lit = field.visibleWhenLiteral
            when {
                key != null && lit == null -> issues += ContractIssue("condition", "visibleWhenLiteral required with key")
                key == null && lit != null -> issues += ContractIssue("condition", "visibleWhenKey required with literal")
                key != null && lit != null -> {
                    val op = if (field.visibilityNegated) "!=" else "=="
                    val expr = "$key $op $lit"
                    if (ConditionGrammar.parse(expr) == null) {
                        issues += ContractIssue("condition", "Illegal visibility condition for ${field.fieldId}")
                    }
                }
            }
            val hostKey = field.visibleWhenHostKey
            val hostLit = field.visibleWhenHostLiteral
            when {
                hostKey != null && hostLit == null ->
                    issues += ContractIssue("host_condition", "visibleWhenHostLiteral required for ${field.fieldId}")
                hostKey == null && hostLit != null ->
                    issues += ContractIssue("host_condition", "visibleWhenHostKey required for ${field.fieldId}")
                hostKey != null && hostKey !in HostSettingsContract.KNOWN_HOST_SETTING_KEYS ->
                    issues += ContractIssue("host_condition", "Unknown visibleWhenHostKey $hostKey")
            }
            val binding = field.hostSettingKey
            if (binding != null && binding !in HostSettingsContract.KNOWN_HOST_SETTING_KEYS) {
                issues += ContractIssue("host_binding", "Unknown hostSettingKey $binding for ${field.fieldId}")
            }
            if (field.readOnly && binding == null) {
                issues += ContractIssue("host_binding", "readOnly requires hostSettingKey for ${field.fieldId}")
            }
            if (field.required && field.defaultValueLiteral.isNullOrBlank() && binding == null) {
                issues += ContractIssue("default", "required field ${field.fieldId} needs defaultValueLiteral or host binding")
            }
        }
        val sanitized = sanitize(schema)
        return if (issues.isEmpty()) ContractValidationResult.Ok(sanitized) else ContractValidationResult.Err(issues)
    }

    private fun sanitizeHostKey(key: String): String? {
        val trimmed = key.trim()
        return if (trimmed.isEmpty()) null else trimmed
    }
}

object MainUiSchemaContractValidator {
    private val supportedV1CardTypes = setOf(MainUiCardTypes.STATUS, MainUiCardTypes.KEY_VALUE_LIST, MainUiCardTypes.ACTION)

    fun sanitize(schema: PluginMainUiSchemaParcel): PluginMainUiSchemaParcel =
        schema.copy(
            cards = schema.cards.map { c ->
                c.copy(
                    cardType = c.cardType.uppercase(),
                    cardId = PluginStringPolicy.truncateUtf8(c.cardId, WearTakPluginContract.MAX_MAIN_UI_CARD_ID_BYTES),
                    title = PluginStringPolicy.truncateUtf8(c.title, WearTakPluginContract.MAX_MAIN_UI_TITLE_BYTES),
                    subtitle = c.subtitle?.let { PluginStringPolicy.truncateUtf8(it, WearTakPluginContract.MAX_MAIN_UI_TITLE_BYTES) },
                )
            },
        )

    fun validate(
        schema: PluginMainUiSchemaParcel,
        strictV1CardTypesOnly: Boolean = false,
    ): ContractValidationResult<PluginMainUiSchemaParcel> {
        val issues = mutableListOf<ContractIssue>()
        if (schema.cards.size > WearTakPluginContract.MAX_MAIN_UI_CARDS) {
            issues += ContractIssue("cards", "Too many cards")
        }
        val ids = mutableSetOf<String>()
        for (card in schema.cards) {
            if (card.cardId.isBlank()) issues += ContractIssue("cardId", "Blank cardId")
            if (!ids.add(card.cardId)) issues += ContractIssue("cardId", "Duplicate ${card.cardId}")
            if (card.title.isBlank()) issues += ContractIssue("title", "Blank title")
            val t = card.cardType.uppercase()
            if (strictV1CardTypesOnly && t !in supportedV1CardTypes) {
                issues += ContractIssue("cardType", "Unsupported in strict v1: $t")
            }
            if (t == MainUiCardTypes.ACTION) {
                when (val result = MainUiActionContract.parseContent(card.content)) {
                    is ContractValidationResult.Err -> issues += result.issues
                    is ContractValidationResult.Ok -> Unit
                }
            }
            when (BundleContractValidator.validateContentBundle(card.content)) {
                is ContractValidationResult.Ok -> Unit
                is ContractValidationResult.Err -> issues += ContractIssue("content", "Invalid card content")
            }
        }
        val sanitized = sanitize(schema)
        return if (issues.isEmpty()) ContractValidationResult.Ok(sanitized) else ContractValidationResult.Err(issues)
    }
}

object SensorEventContractValidator {
    fun validate(event: SensorEventParcel): ContractValidationResult<SensorEventParcel> {
        if (event.sensorKey.isBlank()) {
            return ContractValidationResult.Err(ContractIssue("sensorKey", "required"))
        }
        val keyBytes = event.sensorKey.toByteArray(Charsets.UTF_8).size
        if (keyBytes > WearTakPluginContract.MAX_SENSOR_KEY_BYTES) {
            return ContractValidationResult.Err(ContractIssue("sensorKey", "too long"))
        }
        return when (val b = BundleContractValidator.validateContentBundle(event.values)) {
            is ContractValidationResult.Ok -> ContractValidationResult.Ok(
                event.copy(
                    sensorKey = PluginStringPolicy.truncateUtf8(event.sensorKey, WearTakPluginContract.MAX_SENSOR_KEY_BYTES),
                    values = b.value,
                ),
            )
            is ContractValidationResult.Err -> ContractValidationResult.Err(b.issues)
        }
    }
}
