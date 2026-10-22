package com.weartak.plugin.sdk.validation

import com.weartak.plugin.sdk.model.CepRuleEventParcel
import com.weartak.plugin.sdk.model.CepRuleOperation
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CepRulesValidatorTest {

    @Test
    fun registrationValid() {
        val r = CepRulesValidator.validateRegistration("p1", "s1", """{"k":1}""")
        assertTrue(r is ContractValidationResult.Ok)
    }

    @Test
    fun validateRegistration_rejectsBlankPluginId() {
        val r = CepRulesValidator.validateRegistration(" ", "s1", """{}""")
        assertTrue(r is ContractValidationResult.Err)
    }

    @Test
    fun validateRegistration_rejectsBlankSensorKey() {
        val r = CepRulesValidator.validateRegistration("p1", " ", """{}""")
        assertTrue(r is ContractValidationResult.Err)
    }

    @Test
    fun validateRegistration_rejectsNonJsonObject() {
        val r = CepRulesValidator.validateRegistration("p1", "s1", "[]")
        assertTrue(r is ContractValidationResult.Err)
    }

    @Test
    fun validateRegistration_rejectsOversizedJson() {
        val oversized = "{\"k\":\"" + "x".repeat(PluginStringPolicy.MAX_CEP_JSON_LENGTH) + "\"}"
        val r = CepRulesValidator.validateRegistration("p1", "s1", oversized)
        assertTrue(r is ContractValidationResult.Err)
    }

    @Test
    fun validateEvent_acceptsAdd() {
        val r = CepRulesValidator.validateEvent(
            CepRuleEventParcel(
                operation = CepRuleOperation.ADD,
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
    fun validateEvent_rejectsRemoveWithJson() {
        val r = CepRulesValidator.validateEvent(
            CepRuleEventParcel(
                operation = CepRuleOperation.REMOVE,
                pluginId = "p1",
                sensorKey = "s1",
                ruleSetId = "default",
                sequence = 2L,
                rulesJson = """{}""",
            ),
        )
        assertTrue(r is ContractValidationResult.Err)
    }

    @Test
    fun validateEvent_rejectsBlankRuleSetId() {
        val r = CepRulesValidator.validateEvent(
            CepRuleEventParcel(
                operation = CepRuleOperation.UPDATE,
                pluginId = "p1",
                sensorKey = "s1",
                ruleSetId = " ",
                sequence = 1L,
                rulesJson = """{}""",
            ),
        )
        assertTrue(r is ContractValidationResult.Err)
    }
}
