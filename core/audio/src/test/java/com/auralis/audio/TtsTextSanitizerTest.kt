package com.auralis.audio

import org.junit.Assert.assertEquals
import org.junit.Test

class TtsTextSanitizerTest {

    @Test
    fun testSanitizeAbbreviations() {
        val input = "Mr. Smith met Dr. Jones at 5 p.m. etc."
        val expected = "Mister Smith met Doctor Jones at 5 pm et cetera"
        val result = TtsTextSanitizer.sanitize(input)
        assertEquals(expected, result)
    }

    @Test
    fun testSanitizeDecimals() {
        val input = "The ratio is 3.14 or 4.5."
        val expected = "The ratio is 3 point 14 or 4 point 5."
        val result = TtsTextSanitizer.sanitize(input)
        assertEquals(expected, result)
    }

    @Test
    fun testSanitizeEllipses() {
        val input = "Wait... what happened.. really?"
        val expected = "Wait, what happened, really?"
        val result = TtsTextSanitizer.sanitize(input)
        assertEquals(expected, result)
    }

    @Test
    fun testSanitizeOrphanDots() {
        val input = "hello . world . "
        val expected = "hello. world."
        val result = TtsTextSanitizer.sanitize(input)
        assertEquals(expected, result)
    }
}
