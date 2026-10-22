package com.weartak.plugin.sdk.v2

import com.weartak.plugin.sdk.v2.binder.V2HostControlSupport
import com.weartak.plugin.sdk.v2.binder.V2RecordingHostControl
import com.weartak.plugin.sdk.v2.model.HostCapabilitiesV2Parcel
import com.weartak.plugin.sdk.v2.model.V2TransportPath
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * SDK policy mirror of the §4.11 four-cell compatibility matrix.
 *
 * Cells 1 and 4 are satisfied by v1-only vs v2-only surfaces respectively.
 * Cells 2 and 3 are reject-at-negotiation / v2-helper guard behavior.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class V2CompatibilityMatrixTest {

    private val v2Host = V2RecordingHostControl(
        HostCapabilitiesV2Parcel(
            contractApiVersion = WearTakPluginV2Contract.CONTRACT_API_VERSION,
            cepRules = true,
            detectionEvents = true,
            taskCommands = true,
        ),
    )

    @Test
    fun cell4_v2Plugin_v2Host_syncPathAvailable() {
        assertEquals(V2TransportPath.V2_SYNC, V2HostControlSupport.cepPath(v2Host))
        assertEquals(V2TransportPath.V2_SYNC, V2HostControlSupport.detectionPath(v2Host))
        assertEquals(V2TransportPath.V2_SYNC, V2HostControlSupport.taskCommandPath(v2Host))
    }

    @Test
    fun cell2_v1Plugin_v2Host_v2HelpersRejectWrongContractVersion() {
        val mismatched = V2RecordingHostControl(
            HostCapabilitiesV2Parcel(
                contractApiVersion = 1,
                cepRules = true,
                detectionEvents = true,
                taskCommands = true,
            ),
        )
        assertEquals(V2TransportPath.HOST_INCOMPATIBLE, V2HostControlSupport.cepPath(mismatched))
        assertEquals(V2TransportPath.HOST_INCOMPATIBLE, V2HostControlSupport.detectionPath(mismatched))
        assertEquals(V2TransportPath.HOST_INCOMPATIBLE, V2HostControlSupport.taskCommandPath(mismatched))
    }

    @Test
    fun cell3_v2Plugin_v1Host_v2ControlUnavailable() {
        assertEquals(V2TransportPath.UNAVAILABLE, V2HostControlSupport.cepPath(null))
    }
}
