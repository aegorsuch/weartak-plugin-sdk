package com.weartak.plugin.sdk.binder

import android.util.Log
import com.weartak.plugin.sdk.lifecycle.PluginLifecycleMachine

/**
 * Shared binder guard for plugin-side entry points.
 *
 * Keeps the shutdown check in one place so template/sample binders do not drift.
 */
class PluginBinderGuard(
    private val tag: String,
    private val lifecycle: PluginLifecycleMachine,
) {
    fun command(
        operation: String,
        onShutdown: (() -> Unit)? = null,
        block: () -> Unit,
    ) {
        if (lifecycle.isShutdown()) {
            Log.w(tag, "$operation ignored after shutdown")
            onShutdown?.invoke()
            return
        }
        block()
    }

    fun <T> query(
        operation: String,
        fallback: T,
        onShutdown: (() -> Unit)? = null,
        block: () -> T,
    ): T {
        if (lifecycle.isShutdown()) {
            Log.d(tag, "$operation returned fallback after shutdown")
            onShutdown?.invoke()
            return fallback
        }
        return block()
    }
}
