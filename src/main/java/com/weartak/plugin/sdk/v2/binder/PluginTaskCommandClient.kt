package com.weartak.plugin.sdk.v2.binder

import android.os.RemoteException
import com.weartak.plugin.sdk.v2.aidl.IWearTakPluginHostControlV2
import com.weartak.plugin.sdk.v2.model.TaskCommandCompletionParcel
import com.weartak.plugin.sdk.v2.model.TaskCommandProgressParcel
import com.weartak.plugin.sdk.v2.model.TaskCommandSubmitParcel
import com.weartak.plugin.sdk.v2.model.V2ApplyResult
import com.weartak.plugin.sdk.v2.model.V2TransportPath
import com.weartak.plugin.sdk.v2.transport.V2StreamCoordinator
import com.weartak.plugin.sdk.v2.validation.TaskCommandV2Validator
import com.weartak.plugin.sdk.validation.ContractValidationResult

/**
 * Plugin-author-facing **v2-only** task / command client.
 *
 * Multi-step synchronous flow: [submit] → optional [reportProgress] → [complete].
 * Each step returns immediate [V2ApplyResult] from the host. Rejects incompatible hosts — no v1 fallback.
 *
 * [commandId] is **advisory** — the host rebinds to caller package / UID.
 * [sequence] is monotonic per `(pluginId, commandId)`.
 */
class PluginTaskCommandClient(
    private val pluginId: String,
) {
    private val streams = V2StreamCoordinator()

    fun resolveTransportPath(control: IWearTakPluginHostControlV2?): V2TransportPath =
        V2HostControlSupport.taskCommandPath(control)

    fun submit(
        control: IWearTakPluginHostControlV2?,
        commandId: String,
        taskType: String,
        payloadJson: String,
    ): V2ApplyResult = when (val path = resolveTransportPath(control)) {
        V2TransportPath.V2_SYNC -> submitV2Sync(control, commandId, taskType, payloadJson)
        V2TransportPath.HOST_INCOMPATIBLE -> V2ApplyResult.incompatibleHost()
        V2TransportPath.UNAVAILABLE -> V2ApplyResult.unavailable()
    }

    fun reportProgress(
        control: IWearTakPluginHostControlV2?,
        commandId: String,
        progressPercent: Int,
        message: String? = null,
    ): V2ApplyResult = when (val path = resolveTransportPath(control)) {
        V2TransportPath.V2_SYNC -> progressV2Sync(control, commandId, progressPercent, message)
        V2TransportPath.HOST_INCOMPATIBLE -> V2ApplyResult.incompatibleHost()
        V2TransportPath.UNAVAILABLE -> V2ApplyResult.unavailable()
    }

    fun complete(
        control: IWearTakPluginHostControlV2?,
        commandId: String,
        outcome: Int,
        message: String? = null,
        resultJson: String? = null,
    ): V2ApplyResult = when (val path = resolveTransportPath(control)) {
        V2TransportPath.V2_SYNC -> completeV2Sync(control, commandId, outcome, message, resultJson)
        V2TransportPath.HOST_INCOMPATIBLE -> V2ApplyResult.incompatibleHost()
        V2TransportPath.UNAVAILABLE -> V2ApplyResult.unavailable()
    }

    private fun submitV2Sync(
        control: IWearTakPluginHostControlV2?,
        commandId: String,
        taskType: String,
        payloadJson: String,
    ): V2ApplyResult {
        val c = control ?: return V2ApplyResult.unavailable()
        val stream = commandStreamKey(commandId)
        if (!streams.tryAcquire(stream)) {
            return V2ApplyResult.clientBackpressure()
        }
        val raw = TaskCommandSubmitParcel(
            pluginId = pluginId,
            commandId = commandId,
            taskType = taskType,
            sequence = streams.nextSequence(stream),
            payloadJson = payloadJson,
        )
        val request = when (val v = TaskCommandV2Validator.validateSubmit(raw)) {
            is ContractValidationResult.Ok -> v.value
            is ContractValidationResult.Err -> {
                streams.releaseAcquire(stream)
                return V2ApplyResult.invalidPayload()
            }
        }
        return invokeSync(stream) { c.submitTaskCommand(request) }
    }

    private fun progressV2Sync(
        control: IWearTakPluginHostControlV2?,
        commandId: String,
        progressPercent: Int,
        message: String?,
    ): V2ApplyResult {
        val c = control ?: return V2ApplyResult.unavailable()
        val stream = commandStreamKey(commandId)
        if (!streams.tryAcquire(stream)) {
            return V2ApplyResult.clientBackpressure()
        }
        val raw = TaskCommandProgressParcel(
            pluginId = pluginId,
            commandId = commandId,
            sequence = streams.nextSequence(stream),
            progressPercent = progressPercent,
            message = message,
        )
        val progress = when (val v = TaskCommandV2Validator.validateProgress(raw)) {
            is ContractValidationResult.Ok -> v.value
            is ContractValidationResult.Err -> {
                streams.releaseAcquire(stream)
                return V2ApplyResult.invalidPayload()
            }
        }
        return invokeSync(stream) { c.reportTaskCommandProgress(progress) }
    }

    private fun completeV2Sync(
        control: IWearTakPluginHostControlV2?,
        commandId: String,
        outcome: Int,
        message: String?,
        resultJson: String?,
    ): V2ApplyResult {
        val c = control ?: return V2ApplyResult.unavailable()
        val stream = commandStreamKey(commandId)
        if (!streams.tryAcquire(stream)) {
            return V2ApplyResult.clientBackpressure()
        }
        val raw = TaskCommandCompletionParcel(
            pluginId = pluginId,
            commandId = commandId,
            sequence = streams.nextSequence(stream),
            outcome = outcome,
            message = message,
            resultJson = resultJson,
        )
        val completion = when (val v = TaskCommandV2Validator.validateCompletion(raw)) {
            is ContractValidationResult.Ok -> v.value
            is ContractValidationResult.Err -> {
                streams.releaseAcquire(stream)
                return V2ApplyResult.invalidPayload()
            }
        }
        val result = invokeSync(stream) { c.reportTaskCommandCompletion(completion) }
        if (result.accepted) {
            streams.clearStream(stream)
        }
        return result
    }

    private inline fun invokeSync(
        stream: String,
        call: () -> com.weartak.plugin.sdk.v2.model.V2HostResultParcel,
    ): V2ApplyResult =
        try {
            val host = call()
            if (host.accepted) {
                streams.markSubmitted(stream)
            } else {
                streams.releaseAcquire(stream)
            }
            V2ApplyResult.fromHost(host)
        } catch (_: RemoteException) {
            streams.releaseAcquire(stream)
            V2ApplyResult.remoteError()
        }

    private fun commandStreamKey(commandId: String): String = "$pluginId|cmd|$commandId"
}
