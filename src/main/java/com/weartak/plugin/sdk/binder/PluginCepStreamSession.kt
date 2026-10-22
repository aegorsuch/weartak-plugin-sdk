package com.weartak.plugin.sdk.binder

import android.os.RemoteException
import com.weartak.plugin.sdk.WearTakPluginContract
import com.weartak.plugin.sdk.aidl.IWearTakPluginControl
import com.weartak.plugin.sdk.model.CepRuleEventParcel
import com.weartak.plugin.sdk.model.CepRuleOperation
import com.weartak.plugin.sdk.model.HostCapabilitiesParcel
import com.weartak.plugin.sdk.validation.CepRulesValidator
import com.weartak.plugin.sdk.validation.ContractValidationResult
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * Per-plugin CEP stream session (v1.x additive).
 *
 * **Semantics**
 * - [add] / [update] / [remove] are explicit operations.
 * - **Ordering:** [CepRuleEventParcel.sequence] increases monotonically per `(pluginId, sensorKey)`.
 * - **Idempotency:** identical consecutive UPDATE payloads for the same rule set are skipped client-side.
 * - **Backpressure:** at most [WearTakPluginContract.CEP_STREAM_MAX_PENDING_EVENTS] in-flight events per stream key;
 *   minimum [WearTakPluginContract.CEP_STREAM_MIN_INTERVAL_MS] between successful submissions per key.
 *
 * **Compatibility:** when [HostCapabilitiesParcel.cepRuleStreaming] is false, ADD/UPDATE map to
 * [IWearTakPluginControl.registerCepRules] and REMOVE maps to [IWearTakPluginControl.unregisterCepRules].
 * Plugin-supplied IDs remain advisory; the host rebinds to caller package / UID.
 */
class PluginCepStreamSession(
    private val pluginId: String,
) {
    private val sequences = ConcurrentHashMap<String, AtomicLong>()
    private val pending = ConcurrentHashMap<String, AtomicInteger>()
    private val lastSubmitAtMs = ConcurrentHashMap<String, Long>()
    private val lastUpdateHash = ConcurrentHashMap<String, Int>()

    fun add(
        control: IWearTakPluginControl?,
        sensorKey: String,
        ruleSetId: String,
        rulesJson: String,
    ): Boolean = apply(control, sensorKey, ruleSetId, CepRuleOperation.ADD, rulesJson)

    fun update(
        control: IWearTakPluginControl?,
        sensorKey: String,
        ruleSetId: String,
        rulesJson: String,
    ): Boolean {
        val streamKey = streamKey(sensorKey)
        val hash = rulesJson.hashCode()
        if (lastUpdateHash[streamKey] == hash) {
            return true
        }
        val ok = apply(control, sensorKey, ruleSetId, CepRuleOperation.UPDATE, rulesJson)
        if (ok) {
            lastUpdateHash[streamKey] = hash
        }
        return ok
    }

    fun remove(
        control: IWearTakPluginControl?,
        sensorKey: String,
        ruleSetId: String,
    ): Boolean {
        lastUpdateHash.remove(streamKey(sensorKey))
        return apply(control, sensorKey, ruleSetId, CepRuleOperation.REMOVE, null)
    }

    /**
     * v1 compatibility: register is treated as add-or-update for the default rule set id.
     */
    fun register(
        control: IWearTakPluginControl?,
        sensorKey: String,
        rulesJson: String,
        ruleSetId: String = DEFAULT_RULE_SET_ID,
    ): Boolean = update(control, sensorKey, ruleSetId, rulesJson)

    /**
     * v1 compatibility: unregister removes the stream's default rule set and clears local sequence state.
     */
    fun unregister(
        control: IWearTakPluginControl?,
        sensorKey: String,
        ruleSetId: String = DEFAULT_RULE_SET_ID,
    ): Boolean {
        val ok = remove(control, sensorKey, ruleSetId)
        if (ok) {
            val key = streamKey(sensorKey)
            sequences.remove(key)
            pending.remove(key)
            lastSubmitAtMs.remove(key)
            lastUpdateHash.remove(key)
        }
        return ok
    }

    private fun apply(
        control: IWearTakPluginControl?,
        sensorKey: String,
        ruleSetId: String,
        operation: Int,
        rulesJson: String?,
    ): Boolean {
        val c = control ?: return false
        val caps = try {
            c.hostCapabilities
        } catch (_: RemoteException) {
            return false
        }
        if (!caps.cepRules) return false

        if (!acquireBackpressure(streamKey(sensorKey))) {
            return false
        }

        val sequence = nextSequence(sensorKey)
        val raw = CepRuleEventParcel(
            operation = operation,
            pluginId = pluginId,
            sensorKey = sensorKey,
            ruleSetId = ruleSetId,
            sequence = sequence,
            rulesJson = rulesJson,
        )
        val event = when (val v = CepRulesValidator.validateEvent(raw)) {
            is ContractValidationResult.Ok -> v.value
            is ContractValidationResult.Err -> {
                releaseBackpressure(streamKey(sensorKey))
                return false
            }
        }

        return try {
            if (caps.cepRuleStreaming) {
                c.applyCepRuleEvent(event)
            } else {
                when (operation) {
                    CepRuleOperation.ADD, CepRuleOperation.UPDATE -> {
                        val json = event.rulesJson
                            ?: run {
                                releaseBackpressure(streamKey(sensorKey))
                                return false
                            }
                        c.registerCepRules(event.pluginId, event.sensorKey, json)
                    }
                    CepRuleOperation.REMOVE ->
                        c.unregisterCepRules(event.pluginId, event.sensorKey)
                    else -> {
                        releaseBackpressure(streamKey(sensorKey))
                        return false
                    }
                }
            }
            if (operation == CepRuleOperation.ADD || operation == CepRuleOperation.UPDATE) {
                rulesJson?.let { lastUpdateHash[streamKey(sensorKey)] = it.hashCode() }
            }
            markSubmitted(streamKey(sensorKey))
            true
        } catch (_: RemoteException) {
            releaseBackpressure(streamKey(sensorKey))
            false
        }
    }

    private fun streamKey(sensorKey: String): String = "$pluginId|$sensorKey"

    private fun nextSequence(sensorKey: String): Long =
        sequences.computeIfAbsent(streamKey(sensorKey)) { AtomicLong(0L) }.incrementAndGet()

    private fun acquireBackpressure(streamKey: String): Boolean {
        val counter = pending.computeIfAbsent(streamKey) { AtomicInteger(0) }
        if (counter.get() >= WearTakPluginContract.CEP_STREAM_MAX_PENDING_EVENTS) {
            return false
        }
        val now = System.currentTimeMillis()
        val last = lastSubmitAtMs[streamKey] ?: 0L
        if (now - last < WearTakPluginContract.CEP_STREAM_MIN_INTERVAL_MS) {
            return false
        }
        counter.incrementAndGet()
        return true
    }

    private fun markSubmitted(streamKey: String) {
        releaseBackpressure(streamKey)
        lastSubmitAtMs[streamKey] = System.currentTimeMillis()
    }

    private fun releaseBackpressure(streamKey: String) {
        pending[streamKey]?.updateAndGet { current -> (current - 1).coerceAtLeast(0) }
    }

    companion object {
        const val DEFAULT_RULE_SET_ID: String = "default"
    }
}
