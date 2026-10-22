package com.weartak.plugin.sdk.validation

import com.weartak.plugin.sdk.model.SettingsFieldParcel
import com.weartak.plugin.sdk.model.SettingsFieldTypes
import com.weartak.plugin.sdk.model.SettingsOptionParcel
import com.weartak.plugin.sdk.model.SettingsSchemaParcel
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsSchemaContractValidatorTest {

    @Test
    fun validSchema_allSupportedFieldTypes() {
        val s = SettingsSchemaParcel(
            title = "T",
            fields = listOf(
                SettingsFieldParcel("toggle", "Toggle", SettingsFieldTypes.TOGGLE),
                SettingsFieldParcel("label", "Label", SettingsFieldTypes.STRING),
                SettingsFieldParcel(
                    fieldId = "mode",
                    label = "Mode",
                    fieldType = SettingsFieldTypes.SELECT,
                    options = listOf(
                        SettingsOptionParcel("a", "A"),
                        SettingsOptionParcel("b", "B"),
                    ),
                ),
                SettingsFieldParcel("count", "Count", SettingsFieldTypes.NUMBER),
            ),
        )
        val r = SettingsSchemaContractValidator.validate(s)
        assertTrue(r is ContractValidationResult.Ok)
    }

    @Test
    fun stringField_acceptedInV1x() {
        val s = SettingsSchemaParcel(
            fields = listOf(
                SettingsFieldParcel("s", "S", SettingsFieldTypes.STRING),
            ),
        )
        assertTrue(SettingsSchemaContractValidator.validate(s) is ContractValidationResult.Ok)
    }

    @Test
    fun selectField_acceptedWithOptions() {
        val s = SettingsSchemaParcel(
            fields = listOf(
                SettingsFieldParcel(
                    fieldId = "sel",
                    label = "Sel",
                    fieldType = SettingsFieldTypes.SELECT,
                    options = listOf(SettingsOptionParcel("x", "X")),
                ),
            ),
        )
        assertTrue(SettingsSchemaContractValidator.validate(s) is ContractValidationResult.Ok)
    }

    @Test
    fun selectField_rejectedWithoutOptions() {
        val s = SettingsSchemaParcel(
            fields = listOf(
                SettingsFieldParcel("sel", "Sel", SettingsFieldTypes.SELECT, options = emptyList()),
            ),
        )
        assertTrue(SettingsSchemaContractValidator.validate(s) is ContractValidationResult.Err)
    }

    @Test
    fun selectField_rejectedDuplicateOptionValues() {
        val s = SettingsSchemaParcel(
            fields = listOf(
                SettingsFieldParcel(
                    fieldId = "sel",
                    label = "Sel",
                    fieldType = SettingsFieldTypes.SELECT,
                    options = listOf(
                        SettingsOptionParcel("dup", "One"),
                        SettingsOptionParcel("dup", "Two"),
                    ),
                ),
            ),
        )
        assertTrue(SettingsSchemaContractValidator.validate(s) is ContractValidationResult.Err)
    }

    @Test
    fun toggleField_rejectedWhenOptionsPresent() {
        val s = SettingsSchemaParcel(
            fields = listOf(
                SettingsFieldParcel(
                    fieldId = "t",
                    label = "T",
                    fieldType = SettingsFieldTypes.TOGGLE,
                    options = listOf(SettingsOptionParcel("x", "X")),
                ),
            ),
        )
        assertTrue(SettingsSchemaContractValidator.validate(s) is ContractValidationResult.Err)
    }

    @Test
    fun numberField_acceptedInV1x() {
        val s = SettingsSchemaParcel(
            fields = listOf(
                SettingsFieldParcel("n", "N", SettingsFieldTypes.NUMBER),
            ),
        )
        assertTrue(SettingsSchemaContractValidator.validate(s) is ContractValidationResult.Ok)
    }

    @Test
    fun visibilityConditionParsed() {
        val s = SettingsSchemaParcel(
            fields = listOf(
                SettingsFieldParcel(
                    fieldId = "child",
                    label = "Child",
                    fieldType = SettingsFieldTypes.TOGGLE,
                    visibleWhenKey = "parent",
                    visibleWhenLiteral = "true",
                ),
            ),
        )
        val r = SettingsSchemaContractValidator.validate(s)
        assertTrue(r is ContractValidationResult.Ok)
    }

    @Test
    fun visibilityCondition_rejectsCompoundLiteral() {
        val s = SettingsSchemaParcel(
            fields = listOf(
                SettingsFieldParcel(
                    fieldId = "child",
                    label = "Child",
                    fieldType = SettingsFieldTypes.TOGGLE,
                    visibleWhenKey = "parent",
                    visibleWhenLiteral = "true&&false",
                ),
            ),
        )
        assertTrue(SettingsSchemaContractValidator.validate(s) is ContractValidationResult.Err)
    }
}
