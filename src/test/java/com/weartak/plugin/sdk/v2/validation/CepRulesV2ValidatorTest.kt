package com.weartak.plugin.sdk.v2.validation

import com.weartak.plugin.sdk.validation.ContractValidationResult
import com.weartak.plugin.sdk.validation.PluginStringPolicy
import com.weartak.plugin.sdk.v2.model.CepRuleStreamEventParcel
import com.weartak.plugin.sdk.v2.model.CepStreamOperation
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CepRulesV2ValidatorTest {

    @Test
    fun validateStreamEvent_acceptsAdd() {
        val r = CepRulesV2Validator.validateStreamEvent(
            CepRuleStreamEventParcel(
                operation = CepStreamOperation.ADD,
                pluginId = "p1",
                sensorKey = "s1",
                ruleSetId = "default",
                sequence = 1L,
                rulesJson = """{"version":1}""",
            ),
        )
        assertTrue(r is ContractValidationResult.Ok)
    }

    @Test
    fun validateStreamEvent_rejectsBlankPluginId() {
        val r = CepRulesV2Validator.validateStreamEvent(
            CepRuleStreamEventParcel(
                CepStreamOperation.ADD,
                " ",
                "s1",
                "default",
                1L,
                """{}""",
            ),
        )
        assertTrue(r is ContractValidationResult.Err)
    }

    @Test
    fun validateStreamEvent_rejectsRemoveWithJson() {
        val r = CepRulesV2Validator.validateStreamEvent(
            CepRuleStreamEventParcel(
                CepStreamOperation.REMOVE,
                "p1",
                "s1",
                "default",
                2L,
                """{}""",
            ),
        )
        assertTrue(r is ContractValidationResult.Err)
    }

    @Test
    fun validateStreamEvent_rejectsOversizedJson() {
        val oversized = "{\"k\":\"" + "x".repeat(PluginStringPolicy.MAX_CEP_JSON_LENGTH) + "\"}"
        val r = CepRulesV2Validator.validateStreamEvent(
            CepRuleStreamEventParcel(
                CepStreamOperation.UPDATE,
                "p1",
                "s1",
                "default",
                1L,
                oversized,
            ),
        )
        assertTrue(r is ContractValidationResult.Err)
    }
}
