package com.weartak.plugin.sdk.validation

import com.weartak.plugin.sdk.PluginLimits
import java.nio.charset.StandardCharsets

/**
 * String policy for plugin-originated text (design §6.5): bounded, UTF-8 safe, truncated before host sees it.
 */
object PluginStringPolicy {
    const val MAX_KEY_LENGTH: Int = PluginLimits.MAX_KEY_LENGTH
    const val MAX_SHORT_STRING: Int = 128
    const val MAX_MEDIUM_STRING: Int = 512
    const val MAX_LONG_STRING: Int = 4096
    const val MAX_CEP_JSON_LENGTH: Int = 32_768

    fun truncateUtf8(input: String, maxBytes: Int): String {
        if (maxBytes <= 0) return ""
        val bytes = input.toByteArray(StandardCharsets.UTF_8)
        if (bytes.size <= maxBytes) return input
        var end = maxBytes
        while (end > 0 && (bytes[end - 1].toInt() and 0xC0) == 0x80) {
            end--
        }
        return String(bytes, 0, end.coerceAtLeast(0), StandardCharsets.UTF_8)
    }

    fun truncateChars(input: String, maxChars: Int): String {
        if (maxChars <= 0) return ""
        if (input.length <= maxChars) return input
        val end = input.offsetByCodePoints(0, maxChars)
        return input.substring(0, end)
    }

    fun requireUtf8Printable(input: String, maxBytes: Int): Result<String> {
        val t = truncateUtf8(input, maxBytes)
        val bad = t.any { ch -> ch.code < 0x20 && ch != '\n' && ch != '\t' }
        return if (bad) Result.failure(IllegalArgumentException("Non-printable characters")) else Result.success(t)
    }
}
