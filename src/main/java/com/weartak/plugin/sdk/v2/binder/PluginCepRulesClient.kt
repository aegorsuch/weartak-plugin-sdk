package com.weartak.plugin.sdk.v2.binder

import android.os.RemoteException
import com.weartak.plugin.sdk.binder.PluginCepStreamSession
import com.weartak.plugin.sdk.v2.aidl.IWearTakPluginHostControlV2
import com.weartak.plugin.sdk.v2.model.CepRuleStreamEventParcel
import com.weartak.plugin.sdk.v2.model.CepStreamOperation
import com.weartak.plugin.sdk.v2.model.V2ApplyResult
import com.weartak.plugin.sdk.v2.model.V2ApplyStatus
import com.weartak.plugin.sdk.v2.model.V2TransportPath
import com.weartak.plugin.sdk.v2.transport.V2StreamCoordinator
import com.weartak.plugin.sdk.v2.validation.CepRulesV2Validator
import com.weartak.plugin.sdk.validation.ContractValidationResult

/**
 * Plugin-author-facing **v2-only** CEP client.
 *
 * Uses synchronous [IWearTakPluginHostControlV2.applyCepRuleEvent] and returns immediate [V2ApplyResult].
 * Does **not** fall back to v1 / v1.x — legacy callers use frozen [PluginCepStreamSession] instead.
 *
 * Plugin-supplied [pluginId], [sensorKey], and [ruleSetId] are **advisory** — the host rebinds to caller
 * package / UID before registry insertion.
 */
class PluginCepRulesClient(
    private val pluginId: String,
) {
    private val streams = V2StreamCoordinator()
    private val lastUpdateHash = mutableMapOf<String, Int>()

    fun resolveTransportPath(controlV2: IWearTakPluginHostControlV2?): V2TransportPath =
        V2HostControlSupport.cepPath(controlV2)

    fun add(
        controlV2: IWearTakPluginHostControlV2?,
        sensorKey: String,
        ruleSetId: String,
        rulesJson: String,
    ): V2ApplyResult = applyMutation(controlV2, sensorKey, ruleSetId, CepStreamOperation.ADD, rulesJson)

    fun update(
        controlV2: IWearTakPluginHostControlV2?,
        sensorKey: String,
        ruleSetId: String,
        rulesJson: String,
    ): V2ApplyResult {
        val path = resolveTransportPath(controlV2)
        if (path == V2TransportPath.V2_SYNC) {
            val key = streamKey(sensorKey)
            if (lastUpdateHash[key] == rulesJson.hashCode()) {
                return V2ApplyResult(true, V2ApplyStatus.SUCCESS, path)
            }
        }
        return applyMutation(controlV2, sensorKey, ruleSetId, CepStreamOperation.UPDATE, rulesJson)
    }

    fun remove(
        controlV2: IWearTakPluginHostControlV2?,
        sensorKey: String,
        ruleSetId: String,
    ): V2ApplyResult {
        lastUpdateHash.remove(streamKey(sensorKey))
        return applyMutation(controlV2, sensorKey, ruleSetId, CepStreamOperation.REMOVE, null)
    }

    private fun applyMutation(
        controlV2: IWearTakPluginHostControlV2?,
        sensorKey: String,
        ruleSetId: String,
        operation: Int,
        rulesJson: String?,
    ): V2ApplyResult {
        return when (val path = resolveTransportPath(controlV2)) {
            V2TransportPath.V2_SYNC -> applyV2Sync(controlV2, sensorKey, ruleSetId, operation, rulesJson)
            V2TransportPath.HOST_INCOMPATIBLE -> V2ApplyResult.incompatibleHost()
            V2TransportPath.UNAVAILABLE -> V2ApplyResult.unavailable()
        }
    }

    private fun applyV2Sync(
        control: IWearTakPluginHostControlV2?,
        sensorKey: String,
        ruleSetId: String,
        operation: Int,
        rulesJson: String?,
    ): V2ApplyResult {
        val c = control ?: return V2ApplyResult.unavailable()
        val stream = streamKey(sensorKey)
        if (!streams.tryAcquire(stream)) {
            return V2ApplyResult.clientBackpressure()
        }
        val raw = CepRuleStreamEventParcel(
            operation = operation,
            pluginId = pluginId,
            sensorKey = sensorKey,
            ruleSetId = ruleSetId,
            sequence = streams.nextSequence(stream),
            rulesJson = rulesJson,
        )
        val event = when (val v = CepRulesV2Validator.validateStreamEvent(raw)) {
            is ContractValidationResult.Ok -> v.value
            is ContractValidationResult.Err -> {
                streams.releaseAcquire(stream)
                return V2ApplyResult.invalidPayload()
            }
        }
        return try {
            val host = c.applyCepRuleEvent(event)
            if (host.accepted) {
                if (operation == CepStreamOperation.ADD || operation == CepStreamOperation.UPDATE) {
                    rulesJson?.let { lastUpdateHash[stream] = it.hashCode() }
                }
                streams.markSubmitted(stream)
            } else {
                streams.releaseAcquire(stream)
            }
            V2ApplyResult.fromHost(host)
        } catch (_: RemoteException) {
            streams.releaseAcquire(stream)
            V2ApplyResult.remoteError()
        }
    }

    private fun streamKey(sensorKey: String): String = "$pluginId|$sensorKey"

    companion object {
        const val DEFAULT_RULE_SET_ID: String = PluginCepStreamSession.DEFAULT_RULE_SET_ID
    }
}
