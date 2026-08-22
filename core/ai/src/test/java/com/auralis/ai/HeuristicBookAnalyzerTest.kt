package com.auralis.ai

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HeuristicBookAnalyzerTest {
    @Test
    fun testBookAnalysisExtraction() = runBlocking {
        val analyzer = HeuristicBookAnalyzer()
        val result = analyzer.analyze(
            BookAnalysisInput(
                title = "The Time Machine",
                chapterTitles = listOf("Chapter 1: The Machine", "Chapter 2: The Fourth Dimension", "Chapter 3: The Journey"),
                textSample = "The Time Traveller was expounding a matter. The Time Traveller smiled at Mrs Watchett. Mrs Watchett nodded warmly."
            )
        )

        assertNotNull("Analysis should not be null", result)
        assertTrue("Genre should be identified", result.genre.isNotBlank())
        assertTrue("Tone should be identified", result.tone.isNotBlank())
        assertTrue("Synopsis should be populated", result.synopsis.isNotBlank())
        assertTrue("Confidence should be > 0", result.confidence > 0f)
        assertTrue("Characters should be extracted", result.characters.isNotEmpty())
    }
}
