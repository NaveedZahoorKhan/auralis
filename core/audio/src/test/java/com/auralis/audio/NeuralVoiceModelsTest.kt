package com.auralis.audio

import org.junit.Assert.assertEquals
import org.junit.Test

class NeuralVoiceModelsTest {
    @Test
    fun testNarrationSegmentRequest() {
        val req = NarrationSegmentRequest("id1", "c1", 0, "text", 0, 4)
        assertEquals("id1", req.id)
        assertEquals("text", req.text)
    }
}
