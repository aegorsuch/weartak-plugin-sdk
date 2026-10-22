package com.weartak.plugin.sdk.model

/**
 * Settings field `fieldType` values for hosted settings.
 *
 * **v1.x hosted settings:** [TOGGLE], [STRING], [SELECT], and [NUMBER] are accepted by
 * [com.weartak.plugin.sdk.validation.SettingsSchemaContractValidator] and merged by
 * [com.weartak.plugin.sdk.validation.SettingsApplyValidator]. Visibility conditions remain
 * boolean-only (`visibleWhenKey` / `visibleWhenLiteral` on toggle values).
 */
object SettingsFieldTypes {
    const val TOGGLE: String = "TOGGLE"
    const val STRING: String = "STRING"
    const val SELECT: String = "SELECT"
    const val NUMBER: String = "NUMBER"
}

/** Well-known `PluginErrorParcel.code` values for cross-plugin diagnostics. */
object PluginErrorCodes {
    const val NONE: Int = 0
    const val INVALID_ARGUMENT: Int = 1
    const val ILLEGAL_STATE: Int = 2
    const val HOST_UNAVAILABLE: Int = 3
    const val PAYLOAD_TOO_LARGE: Int = 4
    const val VALIDATION_FAILED: Int = 5
    const val SHARED_MEMORY_UNSUPPORTED: Int = 100
}
