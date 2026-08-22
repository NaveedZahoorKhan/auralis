package com.auralis.audio

class NarrationPlanner {
    fun planChapter(chapterId: String, chapterText: String): List<NarrationSegmentRequest> {
        val paragraphs = chapterText
            .split(Regex("\\n\\s*\\n"))
            .map { it.trim() }
            .filter { it.length > 20 }

        var cursor = 0
        return paragraphs.flatMapIndexed { paragraphIndex, paragraph ->
            chunkParagraph(paragraph).mapIndexed { chunkIndex, chunk ->
                val start = chapterText.indexOf(chunk.take(40), cursor).takeIf { it >= 0 } ?: cursor
                val end = (start + chunk.length).coerceAtMost(chapterText.length)
                cursor = end
                NarrationSegmentRequest(
                    id = "$chapterId-$paragraphIndex-$chunkIndex",
                    chapterId = chapterId,
                    sortIndex = paragraphIndex * 1000 + chunkIndex,
                    text = chunk,
                    textStartOffset = start,
                    textEndOffset = end
                )
            }
        }
    }

    private fun chunkParagraph(paragraph: String): List<String> {
        if (paragraph.length <= 900) return listOf(paragraph)
        val chunks = mutableListOf<String>()
        var cursor = 0
        while (cursor < paragraph.length) {
            val end = (cursor + 900).coerceAtMost(paragraph.length)
            val sentenceEnd = paragraph.lastIndexOf('.', end).takeIf { it > cursor + 350 } ?: end
            chunks += paragraph.substring(cursor, sentenceEnd).trim()
            cursor = sentenceEnd.coerceAtLeast(cursor + 1)
        }
        return chunks.filter { it.isNotBlank() }
    }
}
