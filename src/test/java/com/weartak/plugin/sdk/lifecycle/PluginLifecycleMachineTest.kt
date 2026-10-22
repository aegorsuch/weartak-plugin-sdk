package com.weartak.plugin.sdk.lifecycle

import com.weartak.plugin.sdk.model.PluginLifecycleState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PluginLifecycleMachineTest {

    @Test
    fun happyPath_bindInitStartStopShutdown() {
        val m = PluginLifecycleMachine()
        assertEquals(PluginLifecycleState.UNBOUND, m.snapshot())
        assertMoved(m.dispatch(PluginLifecycleEvent.BIND_ATTACHED), PluginLifecycleState.BOUND)
        assertMoved(m.dispatch(PluginLifecycleEvent.INITIALIZED), PluginLifecycleState.READY)
        assertMoved(m.dispatch(PluginLifecycleEvent.HOST_STARTED), PluginLifecycleState.RUNNING)
        assertMoved(m.dispatch(PluginLifecycleEvent.HOST_STOPPED), PluginLifecycleState.STOPPED)
        assertMoved(m.dispatch(PluginLifecycleEvent.HOST_STARTED), PluginLifecycleState.RUNNING)
        assertMoved(m.dispatch(PluginLifecycleEvent.HOST_SHUTDOWN), PluginLifecycleState.SHUTDOWN)
        assertTrue(m.isShutdown())
    }

    @Test
    fun initIdempotentFromReady() {
        val m = PluginLifecycleMachine()
        m.dispatch(PluginLifecycleEvent.BIND_ATTACHED)
        m.dispatch(PluginLifecycleEvent.INITIALIZED)
        val t = m.dispatch(PluginLifecycleEvent.INITIALIZED)
        assertTrue(t is PluginLifecycleTransition.Unchanged)
        assertEquals(PluginLifecycleState.READY, m.snapshot())
    }

    @Test
    fun reinitializeFromRunning() {
        val m = PluginLifecycleMachine()
        m.dispatch(PluginLifecycleEvent.BIND_ATTACHED)
        m.dispatch(PluginLifecycleEvent.INITIALIZED)
        m.dispatch(PluginLifecycleEvent.HOST_STARTED)
        assertMoved(m.dispatch(PluginLifecycleEvent.INITIALIZED), PluginLifecycleState.READY)
    }

    @Test
    fun startRejectedFromBound() {
        val m = PluginLifecycleMachine()
        m.dispatch(PluginLifecycleEvent.BIND_ATTACHED)
        val t = m.dispatch(PluginLifecycleEvent.HOST_STARTED)
        assertTrue(t is PluginLifecycleTransition.Rejected)
    }

    @Test
    fun shutdownTerminalThenReset() {
        val m = PluginLifecycleMachine()
        m.dispatch(PluginLifecycleEvent.BIND_ATTACHED)
        m.dispatch(PluginLifecycleEvent.HOST_SHUTDOWN)
        assertTrue(m.isShutdown())
        val rej = m.dispatch(PluginLifecycleEvent.INITIALIZED)
        assertTrue(rej is PluginLifecycleTransition.Rejected)
        assertMoved(m.dispatch(PluginLifecycleEvent.RESET), PluginLifecycleState.UNBOUND)
    }

    private fun assertMoved(t: PluginLifecycleTransition, expected: PluginLifecycleState) {
        assertTrue(t is PluginLifecycleTransition.Moved)
        assertEquals(expected, (t as PluginLifecycleTransition.Moved).state)
    }
}
