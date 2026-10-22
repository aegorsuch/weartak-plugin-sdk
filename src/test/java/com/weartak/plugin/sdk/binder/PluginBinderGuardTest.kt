package com.weartak.plugin.sdk.binder

import com.weartak.plugin.sdk.lifecycle.PluginLifecycleEvent
import com.weartak.plugin.sdk.lifecycle.PluginLifecycleMachine
import com.weartak.plugin.sdk.model.PluginLifecycleState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class PluginBinderGuardTest {

    @Test
    fun command_runsWhileActive_andStopsAfterShutdown() {
        val lifecycle = PluginLifecycleMachine()
        val guard = PluginBinderGuard("Test", lifecycle)
        var count = 0

        guard.command("start") { count++ }
        assertEquals(1, count)

        lifecycle.dispatch(PluginLifecycleEvent.HOST_SHUTDOWN)
        guard.command("start") { count++ }
        assertEquals(1, count)
        assertTrue(lifecycle.isShutdown())
    }

    @Test
    fun query_returnsFallbackAfterShutdown() {
        val lifecycle = PluginLifecycleMachine()
        val guard = PluginBinderGuard("Test", lifecycle)

        val active = guard.query("status", fallback = PluginLifecycleState.SHUTDOWN) {
            PluginLifecycleState.RUNNING
        }
        assertEquals(PluginLifecycleState.RUNNING, active)

        lifecycle.dispatch(PluginLifecycleEvent.HOST_SHUTDOWN)
        val afterShutdown = guard.query("status", fallback = PluginLifecycleState.SHUTDOWN) {
            PluginLifecycleState.RUNNING
        }
        assertEquals(PluginLifecycleState.SHUTDOWN, afterShutdown)
        assertFalse(afterShutdown == PluginLifecycleState.RUNNING)
    }

    @Test
    fun command_invokesOnShutdownWhenBlocked() {
        val lifecycle = PluginLifecycleMachine()
        val guard = PluginBinderGuard("Test", lifecycle)
        lifecycle.dispatch(PluginLifecycleEvent.HOST_SHUTDOWN)
        var shutdownHook = 0
        guard.command("apply", onShutdown = { shutdownHook++ }) { }
        assertEquals(1, shutdownHook)
    }

    @Test
    fun query_invokesOnShutdownWhenBlocked() {
        val lifecycle = PluginLifecycleMachine()
        val guard = PluginBinderGuard("Test", lifecycle)
        lifecycle.dispatch(PluginLifecycleEvent.HOST_SHUTDOWN)
        var shutdownHook = 0
        val v = guard.query("get", fallback = 0, onShutdown = { shutdownHook++ }) { 42 }
        assertEquals(0, v)
        assertEquals(1, shutdownHook)
    }
}
