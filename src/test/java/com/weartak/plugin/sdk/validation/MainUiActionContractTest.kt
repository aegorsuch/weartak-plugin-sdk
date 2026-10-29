package com.weartak.plugin.sdk.validation

import android.os.Bundle
import com.weartak.plugin.sdk.MainUiActionContract
import com.weartak.plugin.sdk.model.MainUiCardParcel
import com.weartak.plugin.sdk.model.MainUiCardTypes
import com.weartak.plugin.sdk.model.PluginMainUiSchemaParcel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MainUiActionContractTest {
    private fun content() = Bundle().apply {
        putString("actionId", "gotenna.refresh")
        putString("actionLabel", "Refresh")
    }

    @Test
    fun omittedOptionsDefaultToEnabledWithEmptyArgs() {
        val action = MainUiActionContract.parseContent(content()).toResult().getOrThrow()
        assertEquals("gotenna.refresh", action.actionId)
        assertEquals("Refresh", action.actionLabel)
        assertTrue(action.enabled)
        assertTrue(action.args.isEmpty)
    }

    @Test
    fun helperRoundTripsDisabledActionAndStringArguments() {
        val content = MainUiActionContract.createContent(
            "takcad.refresh", "Refresh", enabled = false,
            args = mapOf("incidentId" to "quoted \"value\"", "empty" to ""),
        )
        val action = MainUiActionContract.parseContent(content).toResult().getOrThrow()
        assertFalse(action.enabled)
        assertEquals("quoted \"value\"", action.args.getString("incidentId"))
        assertEquals("", action.args.getString("empty"))
    }

    @Test
    fun rejectsMissingBlankAndWrongTypedFields() {
        val invalid = listOf(
            content().apply { remove("actionId") },
            content().apply { putString("actionId", " ") },
            content().apply { putInt("actionId", 1) },
            content().apply { remove("actionLabel") },
            content().apply { putString("actionLabel", "") },
            content().apply { putBoolean("actionLabel", true) },
            content().apply { putString("enabled", "true") },
            content().apply { putString("enabled", null) },
            content().apply { putInt("actionArgsJson", 1) },
            content().apply { putString("actionArgsJson", null) },
        )
        invalid.forEach { assertTrue(MainUiActionContract.parseContent(it) is ContractValidationResult.Err) }
    }

    @Test
    fun rejectsMalformedNonObjectAndNonStringArguments() {
        listOf(
            "{", "[]", "null", "\"text\"",
            """{"key":1}""", """{"key":true}""", """{"key":null}""",
            """{"key":[]}""", """{"key":{}}""",
        ).forEach { json ->
            assertTrue(json, MainUiActionContract.parseContent(content().apply {
                putString("actionArgsJson", json)
            }) is ContractValidationResult.Err)
        }
        assertTrue(MainUiActionContract.parseContent(content().apply {
            putString("actionArgsJson", "{} trailing")
        }) is ContractValidationResult.Err)
    }

    @Test
    fun emptyJsonStringAndObjectMeanEmptyArgs() {
        listOf("", "{}").forEach { json ->
            val action = MainUiActionContract.parseContent(content().apply {
                putString("actionArgsJson", json)
            }).toResult().getOrThrow()
            assertTrue(action.args.isEmpty)
        }
    }

    @Test
    fun enforcesExactUtf8AndBundleLimits() {
        MainUiActionContract.createContent("é".repeat(64), "é".repeat(128))
        val invalid = listOf(
            content().apply { putString("actionId", "é".repeat(65)) },
            content().apply { putString("actionLabel", "é".repeat(129)) },
            content().apply { putString("actionArgsJson", " ".repeat(2049)) },
            content().apply { putString("actionArgsJson", """{"${"x".repeat(65)}":"v"}""") },
            content().apply {
                putString("actionArgsJson", (1..65).joinToString(",", "{", "}") { "\"k$it\":\"v\"" })
            },
        )
        invalid.forEach { assertTrue(MainUiActionContract.parseContent(it) is ContractValidationResult.Err) }
        MainUiActionContract.createContent("id", "label", args = mapOf("x".repeat(64) to "v"))
        MainUiActionContract.createContent("id", "label", args = (1..64).associate { "k$it" to "v" })
        val jsonAtLimit = """{"k":"${"x".repeat(2040)}"}"""
        assertEquals(2048, jsonAtLimit.toByteArray(Charsets.UTF_8).size)
        assertTrue(MainUiActionContract.parseContent(content().apply {
            putString("actionArgsJson", jsonAtLimit)
        }) is ContractValidationResult.Ok)
    }

    @Test
    fun schemaAcceptsActionInStrictModeAndRejectsBadContentInBothModes() {
        val schema = PluginMainUiSchemaParcel(cards = listOf(MainUiCardParcel(
            MainUiCardTypes.ACTION, "refresh", "Connection", content = content(),
        )))
        assertTrue(MainUiSchemaContractValidator.validate(schema, true) is ContractValidationResult.Ok)
        val invalid = schema.copy(cards = schema.cards.map { it.copy(content = Bundle()) })
        listOf(false, true).forEach {
            assertTrue(MainUiSchemaContractValidator.validate(invalid, it) is ContractValidationResult.Err)
        }
    }

    @Test
    fun actionCardsKeepExistingIdentityAndCountRules() {
        val card = MainUiCardParcel(MainUiCardTypes.ACTION, "refresh", "Connection", content = content())
        listOf(
            listOf(card, card),
            listOf(card.copy(cardId = "")),
            listOf(card.copy(title = " ")),
            (1..17).map { card.copy(cardId = "card$it") },
        ).forEach {
            assertTrue(MainUiSchemaContractValidator.validate(
                PluginMainUiSchemaParcel(cards = it),
            ) is ContractValidationResult.Err)
        }
    }

    @Test
    fun errorsDoNotEchoMalformedJsonOrArgumentNames() {
        listOf("{secret-token", """{"secret-token":null}""").forEach { json ->
            val result = MainUiActionContract.parseContent(content().apply {
                putString("actionArgsJson", json)
            })
            assertTrue(result is ContractValidationResult.Err)
            assertFalse((result as ContractValidationResult.Err).issues.toString().contains("secret-token"))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun creatorRejectsInvalidInput() {
        MainUiActionContract.createContent("", "Refresh")
    }
}
