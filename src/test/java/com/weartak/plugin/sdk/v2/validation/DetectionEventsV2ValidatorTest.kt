package com.weartak.plugin.sdk.v2.validation

import com.weartak.plugin.sdk.validation.ContractValidationResult
import com.weartak.plugin.sdk.v2.WearTakPluginV2Contract
import com.weartak.plugin.sdk.v2.model.DetectionEventParcel
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DetectionEventsV2ValidatorTest {

    @Test
    fun validate_acceptsMinimalEvent() {
        val r = DetectionEventsV2Validator.validate(sample())
        assertTrue(r is ContractValidationResult.Ok)
    }

    @Test
    fun validate_rejectsBlankPluginId() {
        val r = DetectionEventsV2Validator.validate(sample(pluginId = " "))
        assertTrue(r is ContractValidationResult.Err)
    }

    @Test
    fun validate_rejectsBlankClassificationLabel() {
        val r = DetectionEventsV2Validator.validate(sample(classificationLabel = ""))
        assertTrue(r is ContractValidationResult.Err)
    }

    @Test
    fun validate_rejectsConfidenceAboveOne() {
        val r = DetectionEventsV2Validator.validate(sample(confidence = 1.1f))
        assertTrue(r is ContractValidationResult.Err)
    }

    @Test
    fun validate_rejectsInvalidAttributesJson() {
        val r = DetectionEventsV2Validator.validate(sample(attributesJson = "[]"))
        assertTrue(r is ContractValidationResult.Err)
    }

    @Test
    fun validate_rejectsOversizedAttributesJson() {
        val oversized = "{\"k\":\"" + "x".repeat(WearTakPluginV2Contract.MAX_DETECTION_ATTRIBUTES_JSON_BYTES) + "\"}"
        val r = DetectionEventsV2Validator.validate(sample(attributesJson = oversized))
        assertTrue(r is ContractValidationResult.Err)
    }

    private fun sample(
        pluginId: String = "p1",
        classificationLabel: String = "motion",
        confidence: Float = DetectionEventParcel.CONFIDENCE_UNSPECIFIED,
        attributesJson: String? = null,
    ) = DetectionEventParcel(
        pluginId = pluginId,
        streamKey = "cam.front",
        detectionId = "det-1",
        sequence = 1L,
        timestampUtcMillis = 1_700_000_000_000L,
        classificationLabel = classificationLabel,
        confidence = confidence,
        attributesJson = attributesJson,
    )
}
