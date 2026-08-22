package com.auralis.audio

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PcmWavWriterTest {
    @Test
    fun writeMono16CreatesPcmWaveFile() {
        val file = File.createTempFile("auralis-test", ".wav").also { it.deleteOnExit() }

        PcmWavWriter.writeMono16(file, floatArrayOf(-1f, 0f, 1f), 24_000)

        val bytes = file.readBytes()
        assertTrue(bytes.size > 44)
        assertEquals("RIFF", bytes.copyOfRange(0, 4).toString(Charsets.US_ASCII))
        assertEquals("WAVE", bytes.copyOfRange(8, 12).toString(Charsets.US_ASCII))
        assertEquals("fmt ", bytes.copyOfRange(12, 16).toString(Charsets.US_ASCII))
        assertEquals("data", bytes.copyOfRange(36, 40).toString(Charsets.US_ASCII))
        assertEquals(44 + 6, bytes.size)
    }
}
