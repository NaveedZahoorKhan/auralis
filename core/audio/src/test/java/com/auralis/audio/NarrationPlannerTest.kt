package com.auralis.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NarrationPlannerTest {
    @Test
    fun planChapterSplitsLongParagraphsAndKeepsOffsets() {
        val text = buildString {
            append("This is the first paragraph for a narrated chapter. It has enough text to be useful.")
            append("\n\n")
            repeat(80) {
                append("Sentence $it keeps the narration segment long enough for chunking. ")
            }
        }

        val segments = NarrationPlanner().planChapter("chapter-1", text)

        assertTrue(segments.size > 2)
        assertEquals("chapter-1", segments.first().chapterId)
        assertTrue(segments.zipWithNext().all { (left, right) -> left.textEndOffset <= right.textEndOffset })
        assertTrue(segments.all { it.textStartOffset in 0 until text.length })
    }
}
