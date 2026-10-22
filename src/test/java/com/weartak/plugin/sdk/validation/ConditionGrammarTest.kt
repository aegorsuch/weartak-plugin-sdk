package com.weartak.plugin.sdk.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConditionGrammarTest {

    @Test
    fun parse_eqTrue() {
        val p = ConditionGrammar.parse("parent == true")
        assertNotNull(p)
        assertEquals("parent", p!!.key)
        assertEquals(ConditionGrammar.Op.EQ, p.op)
        assertEquals("true", p.literal)
    }

    @Test
    fun parse_neFalse() {
        val p = ConditionGrammar.parse("flag != false")
        assertNotNull(p)
        assertEquals("flag", p!!.key)
        assertEquals(ConditionGrammar.Op.NE, p.op)
        assertEquals("false", p.literal)
    }

    @Test
    fun parse_prefersLeftmostOperator() {
        val p = ConditionGrammar.parse("a != b")
        assertNotNull(p)
        assertEquals("a", p!!.key)
        assertEquals(ConditionGrammar.Op.NE, p.op)
        assertEquals("b", p.literal)
    }

    @Test
    fun parse_rejectsMalformedTripleEquals() {
        assertNull(ConditionGrammar.parse("a!==b"))
    }

    @Test
    fun parse_rejectsCompoundAnd() {
        assertNull(ConditionGrammar.parse("a == true && b == false"))
    }

    @Test
    fun parse_rejectsCompoundOr() {
        assertNull(ConditionGrammar.parse("a == true || b == false"))
    }

    @Test
    fun parse_rejectsDoubleComparisonTail() {
        assertNull(ConditionGrammar.parse("a == b == c"))
    }

    @Test
    fun parse_rejectsEmbeddedOperatorInLiteral() {
        assertNull(ConditionGrammar.parse("a == x!=y"))
    }

    @Test
    fun parse_rejectsParentheses() {
        assertNull(ConditionGrammar.parse("a == (b)"))
    }

    @Test
    fun parse_rejectsWhitespaceInKey() {
        assertNull(ConditionGrammar.parse("bad key == true"))
    }

    @Test
    fun parse_rejectsWhitespaceInLiteral() {
        assertNull(ConditionGrammar.parse("a == hello world"))
    }

    @Test
    fun isValidConditionKey_acceptsFieldId() {
        assertTrue(ConditionGrammar.isValidConditionKey("enable_stream"))
    }

    @Test
    fun isValidConditionLiteral_acceptsSimpleToken() {
        assertTrue(ConditionGrammar.isValidConditionLiteral("v1"))
    }
}
