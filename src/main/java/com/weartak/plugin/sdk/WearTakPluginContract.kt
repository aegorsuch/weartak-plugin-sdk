package com.weartak.plugin.sdk

/**
 * Canonical v1 contract constants. Host and plugins must align to these values.
 *
 * Authority: `docs/weartak-plugin-template-plan.md` and this module.
 */
object WearTakPluginContract {
    const val ACTION_PLUGIN_SERVICE: String = "com.weartak.plugin.action.PLUGIN_SERVICE"
    const val ACTION_PLUGIN_SETTINGS: String = "com.weartak.plugin.action.PLUGIN_SETTINGS"

    const val PERMISSION_BIND_PLUGIN: String = "com.weartak.plugin.permission.BIND_PLUGIN"

    /** Frozen contract API version for AIDL parcel shapes and method semantics. */
    const val CONTRACT_API_VERSION: Int = 1

    // --- Cross-process payload budgets (plugin-originated strings, design §6.5) ---

    const val MAX_PLUGIN_ID_BYTES: Int = 128
    const val MAX_DISPLAY_NAME_BYTES: Int = 256
    const val MAX_VERSION_NAME_BYTES: Int = 64
    const val MAX_PACKAGE_NAME_BYTES: Int = 256
    const val MAX_PLUGIN_DESCRIPTION_CHARS: Int = 100

    const val MAX_SETTINGS_FIELD_ID_BYTES: Int = PluginLimits.MAX_KEY_LENGTH
    const val MAX_SETTINGS_LABEL_BYTES: Int = 512
    const val MAX_SETTINGS_FIELDS: Int = 32

    const val MAX_MAIN_UI_CARDS: Int = 16
    const val MAX_MAIN_UI_CARD_ID_BYTES: Int = 128
    const val MAX_MAIN_UI_TITLE_BYTES: Int = 256

    const val MAX_SENSOR_KEY_BYTES: Int = 128
    const val MAX_BUNDLE_KEYS: Int = 64
    const val MAX_BUNDLE_STRING_VALUE_BYTES: Int = 2048

    const val MAX_MARKER_ID_BYTES: Int = 128
    const val MAX_ALERT_TITLE_BYTES: Int = 256
    const val MAX_ALERT_MESSAGE_BYTES: Int = 2048

    const val MAX_TOAST_MESSAGE_BYTES: Int = 256
    const val MAX_TOAST_TAG_BYTES: Int = 128

    const val MAX_COT_PAYLOAD_FORMAT_BYTES: Int = 64

    const val MAX_CEP_RULE_SET_ID_BYTES: Int = 128

    /** Max in-flight CEP events per `(pluginId, sensorKey)` stream before client backpressure applies. */
    const val CEP_STREAM_MAX_PENDING_EVENTS: Int = 8

    /** Minimum interval between successful CEP stream submissions per stream key (ms). */
    const val CEP_STREAM_MIN_INTERVAL_MS: Long = 50L
}

/** Shared numeric caps referenced by [WearTakPluginContract] and validators. */
object PluginLimits {
    const val MAX_KEY_LENGTH: Int = 64
}
