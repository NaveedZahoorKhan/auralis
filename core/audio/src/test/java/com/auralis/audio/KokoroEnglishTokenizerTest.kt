package com.auralis.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KokoroEnglishTokenizerTest {
    @Test
    fun tokenizeAddsPaddingAndUsesKokoroVocabulary() {
        val tokens = KokoroEnglishTokenizer().tokenize("The quick thing.")

        assertEquals(0L, tokens.first())
        assertEquals(0L, tokens.last())
        assertTrue(tokens.size in 4..512)
        assertTrue(tokens.contains(81L))
        assertTrue(tokens.contains(83L))
        assertTrue(tokens.contains(4L))
    }

    @Test
    fun splitForModelKeepsChunksUnderContextTarget() {
        val text = List(80) { "This sentence should be split before it grows past the local model input window." }
            .joinToString(" ")

        val chunks = KokoroEnglishTokenizer().splitForModel(text)

        assertTrue(chunks.size > 1)
        assertTrue(chunks.all { it.length <= 430 })
    }
}
