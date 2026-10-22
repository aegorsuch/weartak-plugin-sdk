package com.weartak.plugin.sdk.model

/**
 * Plugin process lifecycle as defined by the WearTAK plugin contract.
 * Transitions are idempotent; [SHUTDOWN] is terminal.
 */
enum class PluginLifecycleState {
    UNBOUND,
    BOUND,
    READY,
    RUNNING,
    STOPPED,
    SHUTDOWN,
}
