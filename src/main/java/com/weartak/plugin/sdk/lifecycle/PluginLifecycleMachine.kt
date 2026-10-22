package com.weartak.plugin.sdk.lifecycle

import com.weartak.plugin.sdk.model.PluginLifecycleState
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Host/plugin lifecycle signals mapped to [PluginLifecycleState] (design §7).
 * Thread-safe for typical binder multi-threading; transitions are idempotent where defined.
 */
enum class PluginLifecycleEvent {
    /** Host bound the plugin service; plugin accepted the connection. */
    BIND_ATTACHED,

    /** Host registered `IWearTakHostCallback`. */
    HOST_CALLBACK_REGISTERED,

    /**
     * `initializePluginV2` completed. May be delivered again after settings restart (design §7);
     * transitions running/stopped plugins back to [PluginLifecycleState.READY].
     */
    INITIALIZED,

    HOST_STARTED,
    HOST_STOPPED,

    /** `shutdownPlugin`; terminal until [RESET]. */
    HOST_SHUTDOWN,

    /** Service/process reset to build a fresh machine (not part of the host contract). */
    RESET,
}

sealed class PluginLifecycleTransition {
    abstract val state: PluginLifecycleState

    data class Moved(
        override val state: PluginLifecycleState,
        val previous: PluginLifecycleState,
    ) : PluginLifecycleTransition()

    data class Unchanged(
        override val state: PluginLifecycleState,
    ) : PluginLifecycleTransition()

    data class Rejected(
        override val state: PluginLifecycleState,
        val reason: String,
    ) : PluginLifecycleTransition()
}

class PluginLifecycleMachine(
    initial: PluginLifecycleState = PluginLifecycleState.UNBOUND,
) {
    private val lock = ReentrantLock()
    private var phase: PluginLifecycleState = initial

    fun snapshot(): PluginLifecycleState = lock.withLock { phase }

    fun isShutdown(): Boolean = lock.withLock { phase == PluginLifecycleState.SHUTDOWN }

    fun dispatch(event: PluginLifecycleEvent): PluginLifecycleTransition = lock.withLock {
        val cur = phase
        if (cur == PluginLifecycleState.SHUTDOWN) {
            return@withLock when (event) {
                PluginLifecycleEvent.RESET -> {
                    phase = PluginLifecycleState.UNBOUND
                    PluginLifecycleTransition.Moved(phase, cur)
                }
                PluginLifecycleEvent.HOST_SHUTDOWN -> PluginLifecycleTransition.Unchanged(cur)
                else -> PluginLifecycleTransition.Rejected(cur, "Illegal event after SHUTDOWN")
            }
        }

        val next = resolve(cur, event)
        when {
            next == null -> PluginLifecycleTransition.Rejected(cur, "Illegal $event from $cur")
            next == cur -> PluginLifecycleTransition.Unchanged(cur)
            else -> {
                phase = next
                PluginLifecycleTransition.Moved(next, cur)
            }
        }
    }

    private fun resolve(
        cur: PluginLifecycleState,
        event: PluginLifecycleEvent,
    ): PluginLifecycleState? = when (event) {
        PluginLifecycleEvent.RESET -> PluginLifecycleState.UNBOUND
        PluginLifecycleEvent.BIND_ATTACHED -> when (cur) {
            PluginLifecycleState.UNBOUND -> PluginLifecycleState.BOUND
            else -> cur
        }
        PluginLifecycleEvent.HOST_CALLBACK_REGISTERED -> when (cur) {
            PluginLifecycleState.UNBOUND -> PluginLifecycleState.BOUND
            else -> cur
        }
        PluginLifecycleEvent.INITIALIZED -> when (cur) {
            PluginLifecycleState.UNBOUND -> null
            PluginLifecycleState.BOUND,
            PluginLifecycleState.READY,
            PluginLifecycleState.RUNNING,
            PluginLifecycleState.STOPPED,
            -> PluginLifecycleState.READY
            PluginLifecycleState.SHUTDOWN -> PluginLifecycleState.SHUTDOWN
        }
        PluginLifecycleEvent.HOST_STARTED -> when (cur) {
            PluginLifecycleState.READY,
            PluginLifecycleState.STOPPED,
            -> PluginLifecycleState.RUNNING
            PluginLifecycleState.RUNNING -> cur
            else -> null
        }
        PluginLifecycleEvent.HOST_STOPPED -> when (cur) {
            PluginLifecycleState.RUNNING -> PluginLifecycleState.STOPPED
            PluginLifecycleState.STOPPED -> cur
            else -> null
        }
        PluginLifecycleEvent.HOST_SHUTDOWN -> PluginLifecycleState.SHUTDOWN
    }
}
