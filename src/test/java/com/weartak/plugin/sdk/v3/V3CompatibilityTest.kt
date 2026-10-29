package com.weartak.plugin.sdk.v3

import com.weartak.plugin.sdk.v3.aidl.IWearTakPcmCallback
import com.weartak.plugin.sdk.v3.aidl.IWearTakPluginHostControlV3
import com.weartak.plugin.sdk.v3.binder.PcmHostControlSupport
import com.weartak.plugin.sdk.v3.model.HostCapabilitiesV3Parcel
import com.weartak.plugin.sdk.v3.model.PcmSubscriptionRequestParcel
import com.weartak.plugin.sdk.v3.model.PcmSubscriptionResultParcel
import com.weartak.plugin.sdk.v3.model.PcmSubscriptionStatus
import com.weartak.plugin.sdk.v3.model.PcmTransportPath
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class V3CompatibilityTest {
    @Test
    fun compatiblePcmHost_selectsBatchedBinder() {
        assertEquals(
            PcmTransportPath.V3_BATCHED_BINDER,
            PcmHostControlSupport.resolveTransportPath(control(version = 3, enabled = true)),
        )
    }

    @Test
    fun wrongVersion_isRejectedWithoutFallback() {
        assertEquals(
            PcmTransportPath.HOST_INCOMPATIBLE,
            PcmHostControlSupport.resolveTransportPath(control(version = 2, enabled = true)),
        )
    }

    @Test
    fun missingControl_isUnavailable() {
        assertEquals(PcmTransportPath.UNAVAILABLE, PcmHostControlSupport.resolveTransportPath(null))
    }

    private fun control(version: Int, enabled: Boolean): IWearTakPluginHostControlV3 =
        object : IWearTakPluginHostControlV3.Stub() {
            override fun subscribePcm(
                request: PcmSubscriptionRequestParcel?,
                callback: IWearTakPcmCallback?,
            ) = PcmSubscriptionResultParcel(false, status = PcmSubscriptionStatus.NOT_SUPPORTED)

            override fun unsubscribePcm(subscriptionId: String?) =
                PcmSubscriptionResultParcel(true, subscriptionId)

            override fun getHostCapabilities() = HostCapabilitiesV3Parcel(
                contractApiVersion = version,
                pcmStreaming = enabled,
            )
        }
}
