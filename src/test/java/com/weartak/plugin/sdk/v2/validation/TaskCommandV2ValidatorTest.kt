package com.weartak.plugin.sdk.v2.validation

import com.weartak.plugin.sdk.validation.ContractValidationResult
import com.weartak.plugin.sdk.v2.WearTakPluginV2Contract
import com.weartak.plugin.sdk.v2.model.TaskCommandCompletionParcel
import com.weartak.plugin.sdk.v2.model.TaskCommandOutcome
import com.weartak.plugin.sdk.v2.model.TaskCommandProgressParcel
import com.weartak.plugin.sdk.v2.model.TaskCommandSubmitParcel
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TaskCommandV2ValidatorTest {

    @Test
    fun validateSubmit_acceptsMinimalRequest() {
        val r = TaskCommandV2Validator.validateSubmit(submit())
        assertTrue(r is ContractValidationResult.Ok)
    }

    @Test
    fun validateSubmit_rejectsBlankPayload() {
        val r = TaskCommandV2Validator.validateSubmit(submit(payloadJson = " "))
        assertTrue(r is ContractValidationResult.Err)
    }

    @Test
    fun validateSubmit_rejectsNonObjectPayload() {
        val r = TaskCommandV2Validator.validateSubmit(submit(payloadJson = "[]"))
        assertTrue(r is ContractValidationResult.Err)
    }

    @Test
    fun validateProgress_rejectsOutOfRangePercent() {
        val r = TaskCommandV2Validator.validateProgress(
            TaskCommandProgressParcel("p1", "cmd-1", 2L, 101, null),
        )
        assertTrue(r is ContractValidationResult.Err)
    }

    @Test
    fun validateCompletion_acceptsSuccess() {
        val r = TaskCommandV2Validator.validateCompletion(
            TaskCommandCompletionParcel("p1", "cmd-1", 3L, TaskCommandOutcome.SUCCESS, null, null),
        )
        assertTrue(r is ContractValidationResult.Ok)
    }

    @Test
    fun validateCompletion_rejectsOversizedResultJson() {
        val oversized = "{\"k\":\"" + "x".repeat(WearTakPluginV2Contract.MAX_TASK_RESULT_JSON_BYTES) + "\"}"
        val r = TaskCommandV2Validator.validateCompletion(
            TaskCommandCompletionParcel("p1", "cmd-1", 3L, TaskCommandOutcome.FAILED, null, oversized),
        )
        assertTrue(r is ContractValidationResult.Err)
    }

    private fun submit(payloadJson: String = """{"action":"sync"}""") =
        TaskCommandSubmitParcel(
            pluginId = "p1",
            commandId = "cmd-1",
            taskType = "sync",
            sequence = 1L,
            payloadJson = payloadJson,
        )
}
