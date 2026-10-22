package com.weartak.plugin.sdk.v2

/**
 * v2 contract constants. v2 lives in package [AIDL_PACKAGE] inside `:weartak-plugin-sdk` — no separate SDK module.
 *
 * v1 (`com.weartak.plugin.sdk`, [com.weartak.plugin.sdk.WearTakPluginContract.CONTRACT_API_VERSION]) remains frozen.
 *
 * **Event families** (separate parcel shapes; shared transport):
 * - CEP — [com.weartak.plugin.sdk.v2.binder.PluginCepRulesClient]
 * - Detection / classification — [com.weartak.plugin.sdk.v2.binder.PluginDetectionEventsClient]
 * - Task / command — [com.weartak.plugin.sdk.v2.binder.PluginTaskCommandClient]
 *
 * **Transport (chosen policy):** all plugin→host v2 event calls are **synchronous** and return [com.weartak.plugin.sdk.v2.model.V2HostResultParcel].
 * v2 helpers reject incompatible hosts ([com.weartak.plugin.sdk.v2.model.V2TransportPath.HOST_INCOMPATIBLE]) and do not fall back to v1 / v1.x.
 * Plugin-supplied IDs are **advisory** — the host rebinds to caller package / UID before registry insertion.
 */
object WearTakPluginV2Contract {
    /** Java/Kotlin and AIDL root for the v2 plugin↔host contract. */
    const val AIDL_PACKAGE: String = "com.weartak.plugin.sdk.v2"

    const val CONTRACT_API_VERSION: Int = 2

    const val MAX_PLUGIN_ID_BYTES: Int = com.weartak.plugin.sdk.WearTakPluginContract.MAX_PLUGIN_ID_BYTES
    const val MAX_SENSOR_KEY_BYTES: Int = com.weartak.plugin.sdk.WearTakPluginContract.MAX_SENSOR_KEY_BYTES
    const val MAX_CEP_RULE_SET_ID_BYTES: Int = com.weartak.plugin.sdk.WearTakPluginContract.MAX_CEP_RULE_SET_ID_BYTES

    const val CEP_STREAM_MAX_PENDING_EVENTS: Int =
        com.weartak.plugin.sdk.WearTakPluginContract.CEP_STREAM_MAX_PENDING_EVENTS
    const val CEP_STREAM_MIN_INTERVAL_MS: Long =
        com.weartak.plugin.sdk.WearTakPluginContract.CEP_STREAM_MIN_INTERVAL_MS

    /** Client-side backpressure before synchronous v2 host calls (all event families). */
    const val V2_STREAM_MAX_PENDING_EVENTS: Int = CEP_STREAM_MAX_PENDING_EVENTS
    const val V2_STREAM_MIN_INTERVAL_MS: Long = CEP_STREAM_MIN_INTERVAL_MS

    const val MAX_DETECTION_STREAM_KEY_BYTES: Int = MAX_SENSOR_KEY_BYTES
    const val MAX_DETECTION_ID_BYTES: Int = 128
    const val MAX_CLASSIFICATION_LABEL_BYTES: Int = 256
    const val MAX_DETECTION_ATTRIBUTES_JSON_BYTES: Int = 4096

    const val MAX_TASK_COMMAND_ID_BYTES: Int = 128
    const val MAX_TASK_TYPE_BYTES: Int = 64
    const val MAX_TASK_PAYLOAD_JSON_BYTES: Int = 8192
    const val MAX_TASK_MESSAGE_BYTES: Int = 512
    const val MAX_TASK_RESULT_JSON_BYTES: Int = 4096
}
