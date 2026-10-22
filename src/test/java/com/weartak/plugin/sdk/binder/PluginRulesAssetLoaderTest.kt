package com.weartak.plugin.sdk.binder

import java.io.ByteArrayInputStream
import org.junit.Assert.assertEquals
import org.junit.Test

class PluginRulesAssetLoaderTest {

    @Test
    fun loadFromStream_readsUtf8Payload() {
        val raw = """
            {
              "features": [],
              "rules": []
            }
        """.trimIndent()

        val actual = PluginRulesAssetLoader.loadFromStream(
            ByteArrayInputStream(raw.toByteArray(Charsets.UTF_8))
        )

        assertEquals(raw, actual)
    }
}
