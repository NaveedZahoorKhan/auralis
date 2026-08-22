package com.auralis.audio

import com.auralis.database.VoiceModelEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlinx.coroutines.runBlocking
import java.io.File

class AudiobookPipelineTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testAudiobookAudioGenerationPipeline() = runBlocking {
        val outputDir = tempFolder.newFolder("audiobook_output")
        val text = "The Time Traveller was expounding a recondite matter to us. His grey eyes shone and twinkled."

        val planner = NarrationPlanner()
        val segments = planner.planChapter("chapter-101", text)
        assertTrue("Segments should not be empty", segments.isNotEmpty())

        val segment = segments.first()
        val engine = OnnxNaturalTtsEngine()

        val voice = VoiceModelEntity(
            id = "built-in-neural-en",
            displayName = "Neural Narration Voice",
            language = "en",
            runtime = "kokoro-onnx",
            status = "installed",
            modelPath = null,
            configPath = null,
            sizeBytes = 85_000_000L,
            updatedAtMillis = System.currentTimeMillis()
        )

        val rendered = engine.render(segment, voice, outputDir.absolutePath)

        val audioFile = File(rendered.filePath)
        assertTrue("Generated WAV file must exist", audioFile.exists())
        assertTrue("WAV file must be > 1000 bytes", audioFile.length() > 1000)
        assertTrue("Rendered duration must be > 0", rendered.durationMillis > 0)
        assertTrue("Checksum must not be blank", rendered.checksum.isNotBlank())

        val bytes = audioFile.readBytes()
        assertEquals("RIFF header magic", 'R'.code.toByte(), bytes[0])
        assertEquals("RIFF header magic", 'I'.code.toByte(), bytes[1])
        assertEquals("RIFF header magic", 'F'.code.toByte(), bytes[2])
        assertEquals("RIFF header magic", 'F'.code.toByte(), bytes[3])
        assertEquals("WAVE fmt magic", 'W'.code.toByte(), bytes[8])
        assertEquals("WAVE fmt magic", 'A'.code.toByte(), bytes[9])
        assertEquals("WAVE fmt magic", 'V'.code.toByte(), bytes[10])
        assertEquals("WAVE fmt magic", 'E'.code.toByte(), bytes[11])
    }
}
