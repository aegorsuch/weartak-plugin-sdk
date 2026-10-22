package com.weartak.plugin.sdk.validation

import com.weartak.plugin.sdk.WearTakPluginContract
import com.weartak.plugin.sdk.model.AlertRequestParcel
import com.weartak.plugin.sdk.model.MarkerParcel
import com.weartak.plugin.sdk.model.ToastRequestParcel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ControlPlanePayloadValidatorTest {

    @Test
    fun validateAlert_rejectsBlankTitle() {
        val r = ControlPlanePayloadValidator.validateAlert(AlertRequestParcel(" ", "ok"))
        assertTrue(r is ContractValidationResult.Err)
    }

    @Test
    fun validateAlert_rejectsBlankMessage() {
        val r = ControlPlanePayloadValidator.validateAlert(AlertRequestParcel("ok", " "))
        assertTrue(r is ContractValidationResult.Err)
    }

    @Test
    fun validateAlert_truncatesLongStrings() {
        val longTitle = "t".repeat(WearTakPluginContract.MAX_ALERT_TITLE_BYTES + 32)
        val longMessage = "m".repeat(WearTakPluginContract.MAX_ALERT_MESSAGE_BYTES + 64)
        val r = ControlPlanePayloadValidator.validateAlert(AlertRequestParcel(longTitle, longMessage))
        assertTrue(r is ContractValidationResult.Ok)
        val ok = r as ContractValidationResult.Ok
        assertTrue(ok.value.title.toByteArray().size <= WearTakPluginContract.MAX_ALERT_TITLE_BYTES)
        assertTrue(ok.value.message.toByteArray().size <= WearTakPluginContract.MAX_ALERT_MESSAGE_BYTES)
    }

    @Test
    fun validateToast_rejectsBlankMessage() {
        val r = ControlPlanePayloadValidator.validateToast(ToastRequestParcel(" "))
        assertTrue(r is ContractValidationResult.Err)
    }

    @Test
    fun validateToast_truncatesLongStrings() {
        val longMessage = "m".repeat(WearTakPluginContract.MAX_TOAST_MESSAGE_BYTES + 32)
        val longTag = "t".repeat(WearTakPluginContract.MAX_TOAST_TAG_BYTES + 16)
        val r = ControlPlanePayloadValidator.validateToast(
            ToastRequestParcel(longMessage, tag = longTag),
        )
        assertTrue(r is ContractValidationResult.Ok)
        val ok = r as ContractValidationResult.Ok
        assertTrue(ok.value.message.toByteArray().size <= WearTakPluginContract.MAX_TOAST_MESSAGE_BYTES)
        assertTrue(ok.value.tag!!.toByteArray().size <= WearTakPluginContract.MAX_TOAST_TAG_BYTES)
    }

    @Test
    fun validateToast_clampsUnknownDuration() {
        val r = ControlPlanePayloadValidator.validateToast(ToastRequestParcel("ok", duration = 99))
        assertTrue(r is ContractValidationResult.Ok)
        assertEquals(ToastRequestParcel.DURATION_SHORT, (r as ContractValidationResult.Ok).value.duration)
    }

    @Test
    fun validateMarker_rejectsBlankMarkerId() {
        val r = ControlPlanePayloadValidator.validateMarker(MarkerParcel(markerId = " "))
        assertTrue(r is ContractValidationResult.Err)
    }

    @Test
    fun validateMarker_truncatesLongMarkerId() {
        val longId = "x".repeat(WearTakPluginContract.MAX_MARKER_ID_BYTES + 16)
        val r = ControlPlanePayloadValidator.validateMarker(MarkerParcel(markerId = longId, title = "t"))
        assertTrue(r is ContractValidationResult.Ok)
        val ok = r as ContractValidationResult.Ok
        assertTrue(ok.value.markerId.toByteArray().size <= WearTakPluginContract.MAX_MARKER_ID_BYTES)
        assertEquals("t", ok.value.title)
    }
}
