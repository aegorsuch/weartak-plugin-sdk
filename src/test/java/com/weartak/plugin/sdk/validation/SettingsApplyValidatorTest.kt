package com.weartak.plugin.sdk.validation

import android.os.Bundle
import com.weartak.plugin.sdk.model.SettingsFieldParcel
import com.weartak.plugin.sdk.model.SettingsFieldTypes
import com.weartak.plugin.sdk.model.SettingsOptionParcel
import com.weartak.plugin.sdk.model.SettingsSchemaParcel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SettingsApplyValidatorTest {

    private val schema = SettingsSchemaParcel(
        title = "t",
        fields = listOf(
            SettingsFieldParcel("toggle_a", "A", SettingsFieldTypes.TOGGLE, requiresRestart = true),
            SettingsFieldParcel(
                fieldId = "toggle_b",
                label = "B",
                fieldType = SettingsFieldTypes.TOGGLE,
                visibleWhenKey = "toggle_a",
                visibleWhenLiteral = "true",
            ),
            SettingsFieldParcel(
                fieldId = "name",
                label = "Name",
                fieldType = SettingsFieldTypes.STRING,
                visibleWhenKey = "toggle_a",
                visibleWhenLiteral = "true",
            ),
            SettingsFieldParcel(
                fieldId = "mode",
                label = "Mode",
                fieldType = SettingsFieldTypes.SELECT,
                options = listOf(
                    SettingsOptionParcel("fast", "Fast"),
                    SettingsOptionParcel("slow", "Slow"),
                ),
            ),
            SettingsFieldParcel("rate", "Rate", SettingsFieldTypes.NUMBER),
        ),
    )

    private fun defaultPrevious(): Bundle = Bundle().apply {
        putBoolean("toggle_a", false)
        putBoolean("toggle_b", false)
        putString("name", "")
        putString("mode", "fast")
        putDouble("rate", 0.0)
    }

    @Test
    fun merge_updatesToggle() {
        val prev = defaultPrevious()
        val r = SettingsApplyValidator.mergeSettings(
            schema,
            prev,
            Bundle().apply { putBoolean("toggle_a", true) },
        )
        assertTrue(r is ContractValidationResult.Ok)
        val out = (r as ContractValidationResult.Ok).value
        assertEquals(true, out.values.getBoolean("toggle_a"))
        assertEquals(false, out.values.getBoolean("toggle_b"))
        assertTrue(out.anyValueChanged)
        assertTrue(out.restartRequiredChanged)
    }

    @Test
    fun merge_updatesString() {
        val prev = defaultPrevious().apply { putBoolean("toggle_a", true) }
        val r = SettingsApplyValidator.mergeSettings(
            schema,
            prev,
            Bundle().apply { putString("name", "alpha") },
        )
        assertTrue(r is ContractValidationResult.Ok)
        assertEquals("alpha", (r as ContractValidationResult.Ok).value.values.getString("name"))
    }

    @Test
    fun merge_selectRejectsInvalidOption() {
        val r = SettingsApplyValidator.mergeSettings(
            schema,
            defaultPrevious(),
            Bundle().apply { putString("mode", "invalid") },
        )
        assertTrue(r is ContractValidationResult.Err)
    }

    @Test
    fun merge_selectAcceptsValidOption() {
        val r = SettingsApplyValidator.mergeSettings(
            schema,
            defaultPrevious(),
            Bundle().apply { putString("mode", "slow") },
        )
        assertTrue(r is ContractValidationResult.Ok)
        assertEquals("slow", (r as ContractValidationResult.Ok).value.values.getString("mode"))
    }

    @Test
    fun merge_numberCoercesIntToDouble() {
        val r = SettingsApplyValidator.mergeSettings(
            schema,
            defaultPrevious(),
            Bundle().apply { putInt("rate", 5) },
        )
        assertTrue(r is ContractValidationResult.Ok)
        assertEquals(5.0, (r as ContractValidationResult.Ok).value.values.getDouble("rate"), 0.0001)
    }

    @Test
    fun merge_numberAcceptsDouble() {
        val r = SettingsApplyValidator.mergeSettings(
            schema,
            defaultPrevious(),
            Bundle().apply { putDouble("rate", 1.5) },
        )
        assertTrue(r is ContractValidationResult.Ok)
        assertEquals(1.5, (r as ContractValidationResult.Ok).value.values.getDouble("rate"), 0.0001)
    }

    @Test
    fun turningOffParent_hidesChildToggleAndString() {
        val prev = defaultPrevious().apply {
            putBoolean("toggle_a", true)
            putBoolean("toggle_b", true)
            putString("name", "visible")
        }
        val r = SettingsApplyValidator.mergeSettings(
            schema,
            prev,
            Bundle().apply { putBoolean("toggle_a", false) },
        )
        assertTrue(r is ContractValidationResult.Ok)
        val out = (r as ContractValidationResult.Ok).value.values
        assertEquals(false, out.getBoolean("toggle_b"))
        assertEquals("", out.getString("name"))
    }

    @Test
    fun unknownKeyRejected() {
        val r = SettingsApplyValidator.mergeSettings(
            schema,
            defaultPrevious(),
            Bundle().apply { putBoolean("nope", true) },
        )
        assertTrue(r is ContractValidationResult.Err)
    }

    @Test
    fun nullBundleRejected() {
        val r = SettingsApplyValidator.mergeSettings(schema, defaultPrevious(), null)
        assertTrue(r is ContractValidationResult.Err)
    }

    @Test
    fun nonBooleanToggleRejected() {
        val r = SettingsApplyValidator.mergeSettings(
            schema,
            defaultPrevious(),
            Bundle().apply { putString("toggle_a", "x") },
        )
        assertTrue(r is ContractValidationResult.Err)
    }

    @Test
    fun noChange_noRestartFlag() {
        val prev = defaultPrevious()
        val r = SettingsApplyValidator.mergeSettings(
            schema,
            prev,
            Bundle().apply { putBoolean("toggle_a", false) },
        )
        assertTrue(r is ContractValidationResult.Ok)
        assertFalse((r as ContractValidationResult.Ok).value.restartRequiredChanged)
    }

    @Test
    fun nonRestartFieldChange_restartFlagFalse() {
        val prev = defaultPrevious().apply {
            putBoolean("toggle_a", true)
            putBoolean("toggle_b", false)
        }
        val r = SettingsApplyValidator.mergeSettings(
            schema,
            prev,
            Bundle().apply { putBoolean("toggle_b", true) },
        )
        assertTrue(r is ContractValidationResult.Ok)
        val out = (r as ContractValidationResult.Ok).value
        assertTrue(out.anyValueChanged)
        assertFalse(out.restartRequiredChanged)
    }

    @Test
    fun omittedField_preservesPreviousValue() {
        val prev = defaultPrevious().apply { putString("mode", "slow") }
        val r = SettingsApplyValidator.mergeSettings(
            schema,
            prev,
            Bundle().apply { putDouble("rate", 2.0) },
        )
        assertTrue(r is ContractValidationResult.Ok)
        assertEquals("slow", (r as ContractValidationResult.Ok).value.values.getString("mode"))
    }

    @Test
    fun unsupportedFieldTypeInSchema_rejected() {
        val badSchema = SettingsSchemaParcel(
            fields = listOf(
                SettingsFieldParcel("slider", "S", fieldType = "SLIDER"),
            ),
        )
        val r = SettingsApplyValidator.mergeSettings(
            badSchema,
            Bundle(),
            Bundle().apply { putBoolean("slider", true) },
        )
        assertTrue(r is ContractValidationResult.Err)
    }

    @Test
    fun schemaValidated_mergeSucceedsForAllTypes() {
        val raw = SettingsSchemaParcel(
            fields = listOf(
                SettingsFieldParcel("x", "X", SettingsFieldTypes.TOGGLE),
                SettingsFieldParcel("y", "Y", SettingsFieldTypes.STRING),
                SettingsFieldParcel(
                    fieldId = "z",
                    label = "Z",
                    fieldType = SettingsFieldTypes.SELECT,
                    options = listOf(SettingsOptionParcel("on", "On")),
                ),
                SettingsFieldParcel("n", "N", SettingsFieldTypes.NUMBER),
            ),
        )
        val validated = when (val v = SettingsSchemaContractValidator.validate(raw)) {
            is ContractValidationResult.Ok -> v.value
            is ContractValidationResult.Err -> error(v.issues.joinToString())
        }
        val r = SettingsApplyValidator.mergeSettings(
            validated,
            Bundle(),
            Bundle().apply {
                putBoolean("x", true)
                putString("y", "ok")
                putString("z", "on")
                putInt("n", 3)
            },
        )
        assertTrue(r is ContractValidationResult.Ok)
        val out = (r as ContractValidationResult.Ok).value.values
        assertEquals(true, out.getBoolean("x"))
        assertEquals("ok", out.getString("y"))
        assertEquals("on", out.getString("z"))
        assertEquals(3.0, out.getDouble("n"), 0.0001)
    }
}
