package com.auralis.ai

import org.junit.Assert.assertEquals
import org.junit.Test

class BookAnalysisTest {
    @Test
    fun testBookAnalysisInput() {
        val input = BookAnalysisInput("Title", listOf("chap1"), "text")
        assertEquals("Title", input.title)
    }
}
