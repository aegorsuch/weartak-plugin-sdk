package com.weartak.plugin.sdk.v2.transport

import com.weartak.plugin.sdk.v2.WearTakPluginV2Contract
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * Shared monotonic sequencing and client-side backpressure before synchronous v2 host calls.
 */
internal class V2StreamCoordinator {

    private val sequences = ConcurrentHashMap<String, AtomicLong>()
    private val pending = ConcurrentHashMap<String, AtomicInteger>()
    private val lastSubmitAtMs = ConcurrentHashMap<String, Long>()

    fun nextSequence(streamKey: String): Long =
        sequences.computeIfAbsent(streamKey) { AtomicLong(0L) }.incrementAndGet()

    fun tryAcquire(streamKey: String): Boolean {
        val counter = pending.computeIfAbsent(streamKey) { AtomicInteger(0) }
        if (counter.get() >= WearTakPluginV2Contract.V2_STREAM_MAX_PENDING_EVENTS) {
            return false
        }
        val now = System.currentTimeMillis()
        val last = lastSubmitAtMs[streamKey] ?: 0L
        if (now - last < WearTakPluginV2Contract.V2_STREAM_MIN_INTERVAL_MS) {
            return false
        }
        counter.incrementAndGet()
        return true
    }

    fun markSubmitted(streamKey: String) {
        pending[streamKey]?.updateAndGet { current -> (current - 1).coerceAtLeast(0) }
        lastSubmitAtMs[streamKey] = System.currentTimeMillis()
    }

    fun releaseAcquire(streamKey: String) {
        pending[streamKey]?.updateAndGet { current -> (current - 1).coerceAtLeast(0) }
    }

    fun clearStream(streamKey: String) {
        sequences.remove(streamKey)
        pending.remove(streamKey)
        lastSubmitAtMs.remove(streamKey)
    }
}
