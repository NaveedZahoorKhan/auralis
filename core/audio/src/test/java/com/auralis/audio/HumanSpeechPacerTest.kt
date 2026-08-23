package com.auralis.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HumanSpeechPacerTest {

    @Test
    fun `analyze returns empty list for blank input`() {
        val result = HumanSpeechPacer.analyze("   \n\t  ")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `analyze handles regular sentences`() {
        val text = "This is a very normal sentence with an average length."
        val result = HumanSpeechPacer.analyze(text)

        assertEquals(1, result.size)

        val cadence = result[0]
        assertEquals(1.00f, cadence.pitch, 0.01f)
        assertEquals(1.00f, cadence.speechRate, 0.01f)
        assertEquals(350L, cadence.pauseAfterMillis)
    }

    @Test
    fun `analyze handles question sentences`() {
        val text = "Is this a very normal sentence with an average length?"
        val result = HumanSpeechPacer.analyze(text)

        assertEquals(1, result.size)

        val cadence = result[0]
        assertEquals(1.08f, cadence.pitch, 0.01f)
        assertEquals(1.00f, cadence.speechRate, 0.01f)
        assertEquals(450L, cadence.pauseAfterMillis)
    }

    @Test
    fun `analyze handles exclamation sentences`() {
        val text = "This is an exciting sentence with an average length!"
        val result = HumanSpeechPacer.analyze(text)

        assertEquals(1, result.size)

        val cadence = result[0]
        assertEquals(1.06f, cadence.pitch, 0.01f)
        assertEquals(1.00f, cadence.speechRate, 0.01f)
        assertEquals(400L, cadence.pauseAfterMillis)
    }

    @Test
    fun `analyze handles short sentences`() {
        val text = "Hello world." // 2 words
        val result = HumanSpeechPacer.analyze(text)

        assertEquals(1, result.size)

        val cadence = result[0]
        assertEquals(1.00f, cadence.pitch, 0.01f)
        assertEquals(0.93f, cadence.speechRate, 0.01f)
        assertEquals(350L, cadence.pauseAfterMillis)
    }

    @Test
    fun `analyze handles long sentences`() {
        val text = "This is a very long sentence that just keeps going and going and going and never seems to stop at all." // 21 words
        val result = HumanSpeechPacer.analyze(text)

        assertEquals(1, result.size)

        val cadence = result[0]
        assertEquals(1.00f, cadence.pitch, 0.01f)
        assertEquals(1.03f, cadence.speechRate, 0.01f)
        assertEquals(350L, cadence.pauseAfterMillis)
    }

    @Test
    fun `analyze handles paragraph pauses`() {
        val text = "First sentence of paragraph one. Last sentence of paragraph one.\n\nFirst sentence of paragraph two."
        val result = HumanSpeechPacer.analyze(text)

        assertEquals(3, result.size)

        // End of first paragraph, should have a long pause
        val p1LastSentence = result[1]
        assertEquals(700L, p1LastSentence.pauseAfterMillis)

        // End of last paragraph, should NOT have the 700L pause
        val p2LastSentence = result[2]
        assertEquals(350L, p2LastSentence.pauseAfterMillis)
    }
}
