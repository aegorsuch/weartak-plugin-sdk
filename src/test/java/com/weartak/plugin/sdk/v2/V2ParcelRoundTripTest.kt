package com.weartak.plugin.sdk.v2

import android.os.Parcel
import com.weartak.plugin.sdk.v2.model.CepRuleStreamEventParcel
import com.weartak.plugin.sdk.v2.model.CepStreamOperation
import com.weartak.plugin.sdk.v2.model.DetectionEventParcel
import com.weartak.plugin.sdk.v2.model.HostCapabilitiesV2Parcel
import com.weartak.plugin.sdk.v2.model.TaskCommandCompletionParcel
import com.weartak.plugin.sdk.v2.model.TaskCommandOutcome
import com.weartak.plugin.sdk.v2.model.TaskCommandProgressParcel
import com.weartak.plugin.sdk.v2.model.TaskCommandSubmitParcel
import com.weartak.plugin.sdk.v2.model.V2HostResultParcel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class V2ParcelRoundTripTest {

    @Test
    fun cepRuleStreamEvent_roundTrips() {
        val sample = CepRuleStreamEventParcel(
            operation = CepStreamOperation.UPDATE,
            pluginId = "com.weartak.plugin.sample",
            sensorKey = "sample.motion",
            ruleSetId = "default",
            sequence = 7L,
            rulesJson = """{"version":1}""",
        )
        val actual = roundTrip(sample)
        assertEquals(sample.operation, actual.operation)
        assertEquals(sample.pluginId, actual.pluginId)
        assertEquals(sample.sensorKey, actual.sensorKey)
        assertEquals(sample.ruleSetId, actual.ruleSetId)
        assertEquals(sample.sequence, actual.sequence)
        assertEquals(sample.rulesJson, actual.rulesJson)
    }

    @Test
    fun hostCapabilitiesV2_roundTrips() {
        val sample = HostCapabilitiesV2Parcel(
            contractApiVersion = WearTakPluginV2Contract.CONTRACT_API_VERSION,
            cepRules = true,
            detectionEvents = true,
            taskCommands = true,
        )
        val actual = roundTrip(sample)
        assertEquals(sample.contractApiVersion, actual.contractApiVersion)
        assertEquals(sample.cepRules, actual.cepRules)
        assertEquals(sample.detectionEvents, actual.detectionEvents)
        assertEquals(sample.taskCommands, actual.taskCommands)
    }

    @Test
    fun v2HostResult_roundTrips() {
        val sample = V2HostResultParcel(accepted = false, rejectionCode = 1)
        val actual = roundTrip(sample)
        assertEquals(sample.accepted, actual.accepted)
        assertEquals(sample.rejectionCode, actual.rejectionCode)
    }

    @Test
    fun detectionEvent_roundTrips() {
        val sample = DetectionEventParcel(
            pluginId = "com.weartak.plugin.sample",
            streamKey = "cam.front",
            detectionId = "det-1",
            sequence = 3L,
            timestampUtcMillis = 1_700_000_000_000L,
            classificationLabel = "person",
            confidence = 0.85f,
            attributesJson = """{"zone":"a"}""",
        )
        val actual = roundTrip(sample)
        assertEquals(sample.pluginId, actual.pluginId)
        assertEquals(sample.streamKey, actual.streamKey)
        assertEquals(sample.detectionId, actual.detectionId)
        assertEquals(sample.sequence, actual.sequence)
        assertEquals(sample.classificationLabel, actual.classificationLabel)
        assertEquals(sample.confidence, actual.confidence, 0f)
        assertEquals(sample.attributesJson, actual.attributesJson)
    }

    @Test
    fun taskCommandParcels_roundTrip() {
        val submit = TaskCommandSubmitParcel("p1", "cmd-1", "sync", 1L, """{"a":1}""")
        val progress = TaskCommandProgressParcel("p1", "cmd-1", 2L, 50, "half")
        val completion = TaskCommandCompletionParcel(
            "p1",
            "cmd-1",
            3L,
            TaskCommandOutcome.SUCCESS,
            "done",
            """{"ok":true}""",
        )
        assertEquals(submit.commandId, roundTrip(submit).commandId)
        assertEquals(progress.progressPercent, roundTrip(progress).progressPercent)
        assertEquals(completion.outcome, roundTrip(completion).outcome)
    }

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
