package com.weartak.plugin.sdk.v2.model

/**
 * Outcome of a v2 helper call (CEP, detection, or task/command).
 *
 * Synchronous v2 AIDL methods return [V2HostResultParcel]; helpers map that into [status] immediately.
 * v2 helpers **reject** incompatible hosts — they never route through legacy v1 / v1.x paths.
 */
data class V2ApplyResult(
    val accepted: Boolean,
    val status: V2ApplyStatus,
    val transport: V2TransportPath,
    val hostRejectionCode: Int = V2HostRejectionCode.NONE,
) {
    companion object {
        fun fromHost(
            host: V2HostResultParcel,
            transport: V2TransportPath = V2TransportPath.V2_SYNC,
        ): V2ApplyResult = if (host.accepted) {
            V2ApplyResult(true, V2ApplyStatus.SUCCESS, transport)
        } else {
            V2ApplyResult(
                accepted = false,
                status = V2ApplyStatus.HOST_REJECTED,
                transport = transport,
                hostRejectionCode = host.rejectionCode,
            )
        }

        fun incompatibleHost(): V2ApplyResult = V2ApplyResult(
            accepted = false,
            status = V2ApplyStatus.INCOMPATIBLE_HOST,
            transport = V2TransportPath.HOST_INCOMPATIBLE,
        )

        fun missingCapability(transport: V2TransportPath = V2TransportPath.UNAVAILABLE): V2ApplyResult =
            V2ApplyResult(false, V2ApplyStatus.MISSING_CAPABILITY, transport)

        fun invalidPayload(transport: V2TransportPath = V2TransportPath.V2_SYNC): V2ApplyResult =
            V2ApplyResult(false, V2ApplyStatus.INVALID_PAYLOAD, transport)

        fun clientBackpressure(): V2ApplyResult =
            V2ApplyResult(false, V2ApplyStatus.CLIENT_BACKPRESSURE, V2TransportPath.V2_SYNC)

        fun remoteError(): V2ApplyResult =
            V2ApplyResult(false, V2ApplyStatus.REMOTE_ERROR, V2TransportPath.V2_SYNC)

        fun unavailable(): V2ApplyResult =
            V2ApplyResult(false, V2ApplyStatus.UNAVAILABLE, V2TransportPath.UNAVAILABLE)
    }
}
