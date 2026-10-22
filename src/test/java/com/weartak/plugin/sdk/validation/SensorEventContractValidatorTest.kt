package com.weartak.plugin.sdk.validation

import android.os.Bundle
import com.weartak.plugin.sdk.model.SensorEventParcel
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SensorEventContractValidatorTest {

    @Test
    fun validSensor() {
        val e = SensorEventParcel("motion", 1L, 1L, Bundle())
        assertTrue(SensorEventContractValidator.validate(e) is ContractValidationResult.Ok)
    }

    @Test
    fun blankKeyRejected() {
        val e = SensorEventParcel(" ", 1L, values = Bundle())
        val r = SensorEventContractValidator.validate(e)
        assertTrue(r is ContractValidationResult.Err)
    }
}
