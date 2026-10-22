package com.weartak.plugin.sdk

import android.os.Bundle
import android.os.Parcel
import com.weartak.plugin.sdk.model.CepRuleEventParcel
import com.weartak.plugin.sdk.model.CepRuleOperation
import com.weartak.plugin.sdk.model.CotReadyEventParcel
import com.weartak.plugin.sdk.model.CotRequestParcel
import com.weartak.plugin.sdk.model.PluginErrorCodes
import com.weartak.plugin.sdk.model.PluginErrorParcel
import com.weartak.plugin.sdk.model.AlertRequestParcel
import com.weartak.plugin.sdk.model.DeviceStatusParcel
import com.weartak.plugin.sdk.model.HostCapabilitiesParcel
import com.weartak.plugin.sdk.model.MainUiCardParcel
import com.weartak.plugin.sdk.model.MainUiCardTypes
import com.weartak.plugin.sdk.model.MarkerParcel
import com.weartak.plugin.sdk.model.ToastRequestParcel
import com.weartak.plugin.sdk.model.PluginCapabilitiesParcel
import com.weartak.plugin.sdk.model.PluginInfoParcel
import com.weartak.plugin.sdk.model.PluginInitParcel
import com.weartak.plugin.sdk.model.PluginMainUiSchemaParcel
import com.weartak.plugin.sdk.model.PluginStatusParcel
import com.weartak.plugin.sdk.model.SensorEventParcel
import com.weartak.plugin.sdk.model.SettingsFieldParcel
import com.weartak.plugin.sdk.model.SettingsFieldTypes
import com.weartak.plugin.sdk.model.HostSettingsParcel
import com.weartak.plugin.sdk.model.SettingsApplyResultParcel
import com.weartak.plugin.sdk.model.SettingsSchemaParcel
import com.weartak.plugin.sdk.model.SettingsValidationErrorParcel
import com.weartak.plugin.sdk.model.SharedBufferParcel
import com.weartak.plugin.sdk.model.PluginLifecycleState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ParcelRoundTripTest {

    @Test
    fun pluginInfo_roundTrips() {
        val actual = roundTrip(samplePluginInfo())
        assertEquals(samplePluginInfo().pluginId, actual.pluginId)
        assertEquals(samplePluginInfo().displayName, actual.displayName)
        assertEquals(samplePluginInfo().versionName, actual.versionName)
        assertEquals(samplePluginInfo().packageName, actual.packageName)
        assertEquals(samplePluginInfo().description, actual.description)
    }

    @Test
    fun init_and_status_roundTrip() {
        val init = roundTrip(sampleInit())
        assertEquals(sampleInit().hostPackageName, init.hostPackageName)
        assertEquals(sampleInit().hostUid, init.hostUid)
        assertEquals(sampleInit().pluginIdExpected, init.pluginIdExpected)
        assertEquals("world", init.hostExtras.getString("hello"))

        val status = roundTrip(sampleStatus())
        assertEquals(sampleStatus().state, status.state)
        assertEquals(sampleStatus().message, status.message)
        assertEquals(sampleStatus().detailCode, status.detailCode)
        assertEquals(9L, status.extras.getLong("seq"))
    }

    @Test
    fun schemaRoundTrips() {
        val settings = roundTrip(sampleSettingsSchema())
        assertEquals(sampleSettingsSchema().title, settings.title)
        assertEquals(1, settings.fields.size)
        assertEquals("enable", settings.fields.first().fieldId)
        assertEquals(true, settings.fields.first().requiresRestart)

        val ui = roundTrip(sampleMainUiSchema())
        assertEquals(1, ui.cards.size)
        assertEquals(MainUiCardTypes.STATUS, ui.cards.first().cardType)
        assertEquals("ok", ui.cards.first().content.getString("state"))
    }

    @Test
    fun auxiliaryContractParcels_roundTrip() {
        val ready = roundTrip(CotReadyEventParcel(true, "cot"))
        assertEquals(true, ready.ready)
        assertEquals("cot", ready.detail)

        val err = roundTrip(PluginErrorParcel(PluginErrorCodes.VALIDATION_FAILED, "e", fatal = true))
        assertEquals(PluginErrorCodes.VALIDATION_FAILED, err.code)
        assertEquals("e", err.message)
        assertEquals(true, err.fatal)

        val dev = roundTrip(
            DeviceStatusParcel(
                online = true,
                summary = "ok",
                telemetry = Bundle().apply { putInt("bat", 80) },
            ),
        )
        assertEquals(true, dev.online)
        assertEquals("ok", dev.summary)
        assertEquals(80, dev.telemetry.getInt("bat"))

        val hostCaps = roundTrip(
            HostCapabilitiesParcel(
                cotDispatch = true,
                toasts = true,
                cepRules = true,
            ),
        )
        assertEquals(true, hostCaps.cotDispatch)
        assertEquals(true, hostCaps.toasts)
        assertEquals(true, hostCaps.cepRules)
    }

    @Test
    fun hostSettingsAndApplyResult_roundTrip() {
        val host = roundTrip(
            HostSettingsParcel(
                revision = 9L,
                hostApiVersion = 1,
                hostPackageName = "com.weartak.host",
                snapshotTimeUtcMillis = 42L,
                callsign = "A",
                team = "B",
                role = "Lead",
                reportingIntervalSecs = 15,
                takConnected = true,
            ),
        )
        assertEquals(9L, host.revision)
        assertEquals("A", host.callsign)

        val apply = roundTrip(
            SettingsApplyResultParcel(
                success = true,
                values = Bundle().apply { putBoolean("x", true) },
                hostDerivedFieldIds = listOf("callsign_mirror"),
            ),
        )
        assertTrue(apply.success)
        assertEquals(true, apply.values.getBoolean("x"))

        val err = roundTrip(SettingsValidationErrorParcel(code = "settings.type", message = "bad"))
        assertEquals("settings.type", err.code)
    }

    @Test
    fun controlPayloads_roundTrip() {
        val sensor = roundTrip(sampleSensorEvent())
        assertEquals(sampleSensorEvent().sensorKey, sensor.sensorKey)
        assertEquals(sampleSensorEvent().sequence, sensor.sequence)
        assertEquals(1.5f, sensor.values.getFloat("mag"), 0.0001f)

        val alert = roundTrip(sampleAlert())
        assertEquals(sampleAlert().title, alert.title)
        assertEquals(sampleAlert().message, alert.message)
        assertEquals(sampleAlert().severity, alert.severity)
        assertEquals(sampleAlert().channelId, alert.channelId)

        val marker = roundTrip(sampleMarker())
        assertEquals(sampleMarker().markerId, marker.markerId)
        assertEquals(sampleMarker().title, marker.title)

        val cepEvent = roundTrip(sampleCepRuleEvent())
        assertEquals(sampleCepRuleEvent().operation, cepEvent.operation)
        assertEquals(sampleCepRuleEvent().pluginId, cepEvent.pluginId)
        assertEquals(sampleCepRuleEvent().sequence, cepEvent.sequence)

        val toast = roundTrip(sampleToast())
        assertEquals(sampleToast().message, toast.message)
        assertEquals(sampleToast().duration, toast.duration)
        assertEquals(sampleToast().tag, toast.tag)

        val cot = roundTrip(sampleCot())
        assertEquals(sampleCot().payloadFormat, cot.payloadFormat)
        assertEquals("round-trip", cot.payload.getString("note"))
        assertEquals(sampleCot().priority, cot.priority)

        val shared = roundTrip(SharedBufferParcel(42, 4096, 1, false))
        assertEquals(42L, shared.bufferToken)
        assertEquals(4096L, shared.sizeBytes)
        assertEquals(1, shared.flags)
        assertEquals(false, shared.transportSupported)

        val caps = roundTrip(PluginCapabilitiesParcel(hostedSettingsUi = true, hostedMainUi = true))
        assertEquals(true, caps.hostedSettingsUi)
        assertEquals(true, caps.hostedMainUi)
    }

    private fun samplePluginInfo() = PluginInfoParcel(
        pluginId = "com.weartak.plugin.sample.driver",
        displayName = "WearTAK Sample Plugin",
        versionName = "0.1.0-sample",
        packageName = "com.weartak.plugin.sample",
        description = "Sample plugin metadata for round-trip coverage",
    )

    private fun sampleInit() = PluginInitParcel(
        hostPackageName = "com.weartak.host",
        hostUid = 12345,
        pluginIdExpected = "com.weartak.plugin.sample.driver",
        hostExtras = Bundle().apply { putString("hello", "world") },
    )

    private fun sampleStatus() = PluginStatusParcel(
        state = PluginLifecycleState.RUNNING,
        message = "ok",
        detailCode = 7,
        extras = Bundle().apply { putLong("seq", 9L) },
    )

    private fun sampleSettingsSchema() = SettingsSchemaParcel(
        title = "Settings",
        fields = listOf(
            SettingsFieldParcel(
                fieldId = "enable",
                label = "Enable",
                fieldType = SettingsFieldTypes.TOGGLE,
                requiresRestart = true,
            ),
        ),
    )

    private fun sampleMainUiSchema() = PluginMainUiSchemaParcel(
        cards = listOf(
            MainUiCardParcel(
                cardType = MainUiCardTypes.STATUS,
                cardId = "status",
                title = "Sample",
                subtitle = "Round trip",
                content = Bundle().apply { putString("state", "ok") },
            ),
        ),
    )

    private fun sampleSensorEvent() = SensorEventParcel(
        sensorKey = "sample.motion",
        timestampUtcMillis = 1_234L,
        sequence = 99L,
        values = Bundle().apply { putFloat("mag", 1.5f) },
    )

    private fun sampleAlert() = AlertRequestParcel(
        title = "Alert",
        message = "Message",
        severity = 2,
        channelId = "sample",
    )

    private fun sampleMarker() = MarkerParcel(
        markerId = "marker-1",
        title = "Marker",
    )

    private fun sampleCepRuleEvent() = CepRuleEventParcel(
        operation = CepRuleOperation.UPDATE,
        pluginId = "com.weartak.plugin.sample.driver",
        sensorKey = "sample.motion",
        ruleSetId = "default",
        sequence = 42L,
        rulesJson = """{"version":1,"rules":[]}""",
    )

    private fun sampleToast() = ToastRequestParcel(
        message = "Saved",
        duration = ToastRequestParcel.DURATION_LONG,
        tag = "sample",
    )

    private fun sampleCot() = CotRequestParcel(
        payloadFormat = "sample.v1",
        payload = Bundle().apply { putString("note", "round-trip") },
        priority = 3,
    )

    private inline fun <reified T> roundTrip(value: T): T where T : android.os.Parcelable {
        val parcel = Parcel.obtain()
        try {
            val writeMethod = value!!::class.java.getMethod("writeToParcel", Parcel::class.java, Int::class.javaPrimitiveType)
            writeMethod.invoke(value, parcel, 0)
            parcel.setDataPosition(0)
            val creatorField = value::class.java.getField("CREATOR")
            @Suppress("UNCHECKED_CAST")
            val creator = creatorField.get(null) as android.os.Parcelable.Creator<T>
            return creator.createFromParcel(parcel)
        } finally {
            parcel.recycle()
        }
    }
}
