package com.weartak.plugin.sdk.validation

import com.weartak.plugin.sdk.model.MainUiCardParcel
import com.weartak.plugin.sdk.model.MainUiCardTypes
import com.weartak.plugin.sdk.model.PluginMainUiSchemaParcel
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MainUiSchemaContractValidatorTest {

    @Test
    fun strictV1_acceptsStatusAndKeyValue() {
        val s = PluginMainUiSchemaParcel(
            cards = listOf(
                MainUiCardParcel(MainUiCardTypes.STATUS, "a", "A"),
                MainUiCardParcel(MainUiCardTypes.KEY_VALUE_LIST, "b", "B"),
            ),
        )
        val r = MainUiSchemaContractValidator.validate(s, strictV1CardTypesOnly = true)
        assertTrue(r is ContractValidationResult.Ok)
    }

    @Test
    fun strictV1_rejectsUnknownCard() {
        val s = PluginMainUiSchemaParcel(
            cards = listOf(MainUiCardParcel("UNKNOWN", "x", "X")),
        )
        val r = MainUiSchemaContractValidator.validate(s, strictV1CardTypesOnly = true)
        assertTrue(r is ContractValidationResult.Err)
    }
}
