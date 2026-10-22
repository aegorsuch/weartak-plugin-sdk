package com.weartak.plugin.sdk.validation

/**
 * v1 settings / UI condition grammar: single comparisons only (plugin-template plan §6.6):
 * - `key == literal`
 * - `key != literal`
 * - `key == true` / `key == false`
 *
 * Compound expressions (e.g. `&&`, `||`, nested `==`) are rejected.
 */
object ConditionGrammar {
    data class ParsedCondition(
        val key: String,
        val op: Op,
        val literal: String?,
    )

    enum class Op {
        EQ,
        NE,
    }

    private val keyTokenRegex = Regex("^[a-zA-Z0-9_.-]+$")

    /**
     * Parses a single v1 comparison. Returns null if the expression is not v1-legal.
     */
    fun parse(raw: String): ParsedCondition? {
        val s = raw.trim()
        if (s.isEmpty()) return null
        val eqIdx = s.indexOf("==")
        val neIdx = s.indexOf("!=")
        val useEq = when {
            eqIdx < 0 && neIdx < 0 -> return null
            eqIdx < 0 -> false
            neIdx < 0 -> true
            else -> eqIdx <= neIdx
        }
        val opPos = if (useEq) eqIdx else neIdx
        val opLen = 2
        val op = if (useEq) Op.EQ else Op.NE
        return splitBinary(s, opPos, opLen, op)
    }

    private fun splitBinary(s: String, opPos: Int, opLen: Int, op: Op): ParsedCondition? {
        val left = s.substring(0, opPos).trim()
        val tail = s.substring(opPos + opLen).trim()
        if (left.isEmpty() || tail.isEmpty()) return null
        if (!isValidConditionKey(left)) return null
        if (!isValidConditionLiteral(tail)) return null
        return ParsedCondition(key = left, op = op, literal = tail)
    }

    /**
     * True if [key] is a single field-style token with no operators or whitespace (v1 grammar).
     */
    fun isValidConditionKey(key: String): Boolean {
        if (key.isEmpty() || key.any { it.isWhitespace() }) return false
        if (containsDisallowedOperators(key)) return false
        return keyTokenRegex.matches(key)
    }

    /**
     * True if [literal] is allowed on the right-hand side of `==` / `!=` for v1 (no compound / operator soup).
     */
    fun isValidConditionLiteral(literal: String): Boolean {
        if (literal.isEmpty()) return false
        if (literal.any { it.isWhitespace() }) return false
        if (containsDisallowedOperators(literal)) return false
        return true
    }

    private fun containsDisallowedOperators(fragment: String): Boolean =
        fragment.contains("&&") ||
            fragment.contains("||") ||
            fragment.contains("==") ||
            fragment.contains("!=") ||
            fragment.contains("<=") ||
            fragment.contains(">=") ||
            fragment.any { it in "<>&|()" } ||
            fragment.any { it == '=' }

    fun validateKey(key: String): String =
        PluginStringPolicy.truncateUtf8(key, PluginStringPolicy.MAX_KEY_LENGTH)
}
