package com.weartak.plugin.sdk.validation

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
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
        assertEquals("TEXT", action.actionPresentation)
        assertEquals(null, action.actionIcon)
        assertEquals("BODY", action.actionPlacement)
        assertEquals(null, action.actionGroup)
        assertTrue(action.showTitle)
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

    private fun iconContent(icon: String = "NOTE", placement: String = "BODY", group: String? = null) =
        MainUiActionContract.createPresentedContent(
            "takcad.action", "Accessible action", "ICON", icon, placement, group,
            showTitle = false, enabled = false, args = mapOf("incidentId" to "incident-1"),
        )

    @Test
    fun presentationRoundTripsThroughUnchangedCardParcel() {
        for (icon in listOf("NOTE", "BLOODHOUND", "STOP", "TARGET", "BACK")) {
            val original = MainUiCardParcel(
                MainUiCardTypes.ACTION, "action", "Semantic title",
                content = iconContent(icon, group = "incident-actions"),
            )
            val parcel = Parcel.obtain()
            val restored = try {
                original.writeToParcel(parcel, 0)
                parcel.setDataPosition(0)
                @Suppress("UNCHECKED_CAST")
                val creator = MainUiCardParcel::class.java.getField("CREATOR").get(null) as Parcelable.Creator<MainUiCardParcel>
                creator.createFromParcel(parcel)
            } finally {
                parcel.recycle()
            }
            val action = MainUiActionContract.parseContent(restored.content).toResult().getOrThrow()
            assertEquals("ICON", action.actionPresentation)
            assertEquals(icon, action.actionIcon)
            assertEquals("BODY", action.actionPlacement)
            assertEquals("incident-actions", action.actionGroup)
            assertEquals("Accessible action", action.actionLabel)
            assertEquals("incident-1", action.args.getString("incidentId"))
            assertFalse(action.showTitle)
            assertFalse(action.enabled)
            assertEquals("Semantic title", restored.title)
        }
    }

    @Test
    fun rejectsWrongTypesNullsUnknownEnumsAndUnknownKeys() {
        for (key in listOf("actionPresentation", "actionIcon", "actionPlacement", "actionGroup", "showTitle")) {
            val invalid = listOf(
                iconContent().apply { putString(key, null) },
                iconContent().apply { putInt(key, 1) },
                iconContent().apply { putBundle(key, Bundle()) },
            )
            invalid.forEach {
                assertTrue(key, MainUiActionContract.parseContent(it) is ContractValidationResult.Err)
            }
            val wrongPrimitive = iconContent().apply {
                if (key == "showTitle") putString(key, "false") else putBoolean(key, false)
            }
            assertTrue(key, MainUiActionContract.parseContent(wrongPrimitive) is ContractValidationResult.Err)
        }
        for (key in listOf("actionPresentation", "actionIcon", "actionPlacement")) {
            for (value in listOf("", " ", "unknown", "icon", "back", "body")) {
                assertTrue("$key=$value", MainUiActionContract.parseContent(iconContent().apply {
                    putString(key, value)
                }) is ContractValidationResult.Err)
            }
        }
        assertTrue(MainUiActionContract.parseContent(content().apply {
            putString("unknown", "value")
        }) is ContractValidationResult.Err)
    }

    @Test
    fun validatesCompletePresentationCombinationMatrix() {
        for (presentation in listOf("TEXT", "ICON")) {
            for (icon in listOf(null, "NOTE", "BLOODHOUND", "STOP", "TARGET", "BACK")) {
                for (placement in listOf("BODY", "HEADER_END")) {
                    for (group in listOf(null, "actions")) {
                        for (showTitle in listOf(false, true)) {
                            val candidate = content().apply {
                                putString("actionPresentation", presentation)
                                icon?.let { putString("actionIcon", it) }
                                putString("actionPlacement", placement)
                                group?.let { putString("actionGroup", it) }
                                putBoolean("showTitle", showTitle)
                            }
                            val valid = if (presentation == "TEXT") {
                                icon == null && group == null && placement == "BODY"
                            } else {
                                icon != null && !showTitle &&
                                    (placement == "BODY" || (icon == "BACK" && group == null))
                            }
                            assertEquals(
                                "$presentation/$icon/$placement/$group/$showTitle",
                                valid, MainUiActionContract.parseContent(candidate) is ContractValidationResult.Ok,
                            )
                        }
                    }
                }
            }
        }
    }

    @Test
    fun groupLimitsUseUtf8BytesWithoutTruncation() {
        for (group in listOf("x".repeat(128), "\u00e9".repeat(64))) {
            assertEquals(group, MainUiActionContract.parseContent(iconContent(group = group))
                .toResult().getOrThrow().actionGroup)
        }
        for (group in listOf("", " \t\n", "x".repeat(129), "\u00e9".repeat(65))) {
            assertTrue(MainUiActionContract.parseContent(iconContent().apply {
                putString("actionGroup", group)
            }) is ContractValidationResult.Err)
        }
    }

    @Test
    fun schemaAcceptsStopOnlyAsBodyIconInBothModes() {
        assertEquals("STOP", MainUiActionContract.STOP)
        val stop = MainUiCardParcel(
            MainUiCardTypes.ACTION, "stop", "Stop Bloodhound", content = iconContent("STOP"),
        )
        for (strict in listOf(false, true)) {
            assertTrue(MainUiSchemaContractValidator.validate(
                PluginMainUiSchemaParcel(cards = listOf(stop)), strict,
            ) is ContractValidationResult.Ok)
            for (invalid in listOf(
                stop.content.let { Bundle(it).apply { putString("actionPlacement", "HEADER_END") } },
                stop.content.let { Bundle(it).apply { putString("actionPresentation", "TEXT") } },
                stop.content.let { Bundle(it).apply { putBoolean("showTitle", true) } },
            )) {
                assertTrue(MainUiSchemaContractValidator.validate(
                    PluginMainUiSchemaParcel(cards = listOf(stop.copy(content = invalid))), strict,
                ) is ContractValidationResult.Err)
            }
        }
    }

    @Test
    fun schemaAllowsSingleHeaderButRejectsMultipleInBothModes() {
        val header = MainUiCardParcel(
            MainUiCardTypes.ACTION, "back", "Back", content = iconContent("BACK", "HEADER_END"),
        )
        for (strict in listOf(false, true)) {
            val valid = PluginMainUiSchemaParcel(cards = listOf(
                header,
                header.copy(cardId = "note", title = "Note", content = iconContent()),
            ))
            assertTrue(MainUiSchemaContractValidator.validate(valid, strict) is ContractValidationResult.Ok)
            assertTrue(MainUiSchemaContractValidator.validate(
                valid.copy(cards = listOf(header, header.copy(cardId = "back2"))), strict,
            ) is ContractValidationResult.Err)
            assertTrue(MainUiSchemaContractValidator.validate(
                valid.copy(cards = listOf(header.copy(title = " "))), strict,
            ) is ContractValidationResult.Err)
            assertTrue(MainUiSchemaContractValidator.validate(
                valid.copy(cards = (1..17).map { header.copy(cardId = "note$it", content = iconContent()) }), strict,
            ) is ContractValidationResult.Err)
        }
    }

    @Test
    fun legacyJvmEntryPointsRemainAvailable() {
        assertEquals(Bundle::class.java, MainUiActionContract::class.java.getMethod(
            "createContent", String::class.java, String::class.java,
            Boolean::class.javaPrimitiveType, Map::class.java,
        ).returnType)
        assertTrue(MainUiActionContract::class.java.declaredMethods.any {
            it.name == "createContent\$default" && it.parameterCount == 7
        })
        MainUiActionContract.Action::class.java.getConstructor(
            String::class.java, String::class.java, Boolean::class.javaPrimitiveType, Bundle::class.java,
        )
        assertEquals(
            setOf("actionId", "actionLabel", "enabled", "actionArgsJson"),
            MainUiActionContract.createContent("id", "Label").keySet(),
        )
        assertEquals("com.weartak.hostedUi.actionPresentationVersion", MainUiActionContract.HOST_PRESENTATION_VERSION_KEY)
        assertEquals(1, MainUiActionContract.PRESENTATION_VERSION)
    }

    @Test(expected = IllegalArgumentException::class)
    fun presentationCreatorRejectsInvalidCombination() {
        MainUiActionContract.createPresentedContent("back", "Back", "ICON", "BACK", "HEADER_END")
    }
}
