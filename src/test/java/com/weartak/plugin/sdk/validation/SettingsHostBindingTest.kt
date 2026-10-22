package com.weartak.plugin.sdk.validation

import android.os.Bundle
import com.weartak.plugin.sdk.HostSettingsContract
import com.weartak.plugin.sdk.model.HostSettingsParcel
import com.weartak.plugin.sdk.model.SettingsApplyMode
import com.weartak.plugin.sdk.model.SettingsFieldParcel
import com.weartak.plugin.sdk.model.SettingsFieldTypes
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
class SettingsHostBindingTest {

    private val host = HostSettingsParcel(
        revision = 1L,
        hostApiVersion = 1,
        hostPackageName = "com.weartak.host",
        snapshotTimeUtcMillis = 1000L,
        callsign = "ALPHA",
        team = "Blue",
        role = "Lead",
        reportingIntervalSecs = 30,
        takConnected = true,
    )

    private val schema = SettingsSchemaParcel(
        fields = listOf(
            SettingsFieldParcel(
                fieldId = "callsign_mirror",
                label = "Callsign",
                fieldType = SettingsFieldTypes.STRING,
                hostSettingKey = HostSettingsContract.KEY_CALLSIGN,
                readOnly = true,
            ),
            SettingsFieldParcel(
                fieldId = "enable_feature",
                label = "Enable",
                fieldType = SettingsFieldTypes.TOGGLE,
                visibleWhenHostKey = HostSettingsContract.KEY_TAK_CONNECTED,
                visibleWhenHostLiteral = "true",
            ),
            SettingsFieldParcel(
                fieldId = "rate",
                label = "Rate",
                fieldType = SettingsFieldTypes.NUMBER,
            ),
        ),
    )

    @Test
    fun hostDrivenRecompute_populatesReadOnlyHostField() {
        val r = SettingsApplyValidator.mergeSettings(
            schema,
            Bundle(),
            Bundle(),
            host,
            SettingsApplyMode.HOST_DRIVEN_RECOMPUTE,
        )
        assertTrue(r is ContractValidationResult.Ok)
        val out = (r as ContractValidationResult.Ok).value
        assertEquals("ALPHA", out.values.getString("callsign_mirror"))
        assertTrue(out.hostDerivedFieldIds.contains("callsign_mirror"))
    }

    @Test
    fun readOnlyHostField_rejectsPluginPatch() {
        val r = SettingsApplyValidator.mergeSettings(
            schema,
            Bundle().apply { putString("callsign_mirror", "OLD") },
            Bundle().apply { putString("callsign_mirror", "HACK") },
            host,
            SettingsApplyMode.PARTIAL_PATCH,
        )
        assertTrue(r is ContractValidationResult.Err)
    }

    @Test
    fun hostVisibility_hidesFieldWhenDisconnected() {
        val disconnected = host.copy(takConnected = false)
        val r = SettingsApplyValidator.mergeSettings(
            schema,
            Bundle(),
            Bundle().apply { putBoolean("enable_feature", true) },
            disconnected,
            SettingsApplyMode.HOST_DRIVEN_RECOMPUTE,
        )
        assertTrue(r is ContractValidationResult.Ok)
        assertFalse((r as ContractValidationResult.Ok).value.values.getBoolean("enable_feature"))
    }

    @Test
    fun applyWithResult_returnsStructuredErrors() {
        val result = SettingsApplyValidator.applySettingsWithResult(
            schema,
            Bundle(),
            Bundle().apply { putString("unknown", "x") },
            host,
        )
        assertFalse(result.success)
        assertTrue(result.errors.isNotEmpty())
    }
}
