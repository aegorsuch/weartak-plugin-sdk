package com.weartak.plugin.sdk.v2.binder

import android.os.RemoteException
import com.weartak.plugin.sdk.v2.aidl.IWearTakPluginHostControlV2
import com.weartak.plugin.sdk.v2.model.DetectionEventParcel
import com.weartak.plugin.sdk.v2.model.V2ApplyResult
import com.weartak.plugin.sdk.v2.model.V2ApplyStatus
import com.weartak.plugin.sdk.v2.model.V2TransportPath
import com.weartak.plugin.sdk.v2.transport.V2StreamCoordinator
import com.weartak.plugin.sdk.v2.validation.DetectionEventsV2Validator
import com.weartak.plugin.sdk.validation.ContractValidationResult

/**
 * Plugin-author-facing **v2-only** detection / classification client.
 *
 * Uses synchronous [IWearTakPluginHostControlV2.publishDetectionEvent]; returns immediate [V2ApplyResult].
 * Rejects incompatible or non-v2 hosts — no v1 fallback.
 *
 * [pluginId], [streamKey], and [detectionId] are **advisory** — the host rebinds to caller package / UID.
 */
class PluginDetectionEventsClient(
    private val pluginId: String,
) {
    private val streams = V2StreamCoordinator()
    private val lastPayloadHash = mutableMapOf<String, Int>()

    fun resolveTransportPath(control: IWearTakPluginHostControlV2?): V2TransportPath =
        V2HostControlSupport.detectionPath(control)

    /**
     * Publishes a detection event. [detectionId] is advisory.
     * Identical consecutive payloads on the same stream are skipped (idempotent success).
     */
    fun publish(
        control: IWearTakPluginHostControlV2?,
        streamKey: String,
        detectionId: String,
        classificationLabel: String,
        timestampUtcMillis: Long = System.currentTimeMillis(),
        confidence: Float = DetectionEventParcel.CONFIDENCE_UNSPECIFIED,
        attributesJson: String? = null,
    ): V2ApplyResult {
        return when (val path = resolveTransportPath(control)) {
            V2TransportPath.V2_SYNC -> publishV2Sync(
                control,
                streamKey,
                detectionId,
                classificationLabel,
                timestampUtcMillis,
                confidence,
                attributesJson,
            )
            V2TransportPath.HOST_INCOMPATIBLE -> V2ApplyResult.incompatibleHost()
            V2TransportPath.UNAVAILABLE -> V2ApplyResult.unavailable()
        }
    }

    private fun publishV2Sync(
        control: IWearTakPluginHostControlV2?,
        streamKey: String,
        detectionId: String,
        classificationLabel: String,
        timestampUtcMillis: Long,
        confidence: Float,
        attributesJson: String?,
    ): V2ApplyResult {
        val stream = streamKey(streamKey)
        val hash = payloadHash(classificationLabel, confidence, attributesJson)
        if (lastPayloadHash[stream] == hash) {
            return V2ApplyResult(true, V2ApplyStatus.SUCCESS, V2TransportPath.V2_SYNC)
        }
        val c = control ?: return V2ApplyResult.unavailable()
        if (!streams.tryAcquire(stream)) {
            return V2ApplyResult.clientBackpressure()
        }
        val raw = DetectionEventParcel(
            pluginId = pluginId,
            streamKey = streamKey,
            detectionId = detectionId,
            sequence = streams.nextSequence(stream),
            timestampUtcMillis = timestampUtcMillis,
            classificationLabel = classificationLabel,
            confidence = confidence,
            attributesJson = attributesJson,
        )
        val event = when (val v = DetectionEventsV2Validator.validate(raw)) {
            is ContractValidationResult.Ok -> v.value
            is ContractValidationResult.Err -> {
                streams.releaseAcquire(stream)
                return V2ApplyResult.invalidPayload()
            }
        }
        return try {
            val host = c.publishDetectionEvent(event)
            if (host.accepted) {
                lastPayloadHash[stream] = hash
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

    private fun streamKey(streamKey: String): String = "$pluginId|$streamKey"

    private fun payloadHash(label: String, confidence: Float, attributesJson: String?): Int =
        31 * label.hashCode() + confidence.hashCode() + (attributesJson?.hashCode() ?: 0)
}
