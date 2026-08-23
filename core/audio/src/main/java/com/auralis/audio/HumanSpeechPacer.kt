package com.auralis.audio

data class SentenceCadence(
    val text: String,
    val pitch: Float = 1.0f,
    val speechRate: Float = 1.0f,
    val pauseAfterMillis: Long = 350L
)

object HumanSpeechPacer {

    fun analyze(text: String): List<SentenceCadence> {
        if (text.isBlank()) return emptyList()

        val result = mutableListOf<SentenceCadence>()
        val rawParagraphs = text.split(Regex("\n\n+"))

        rawParagraphs.forEachIndexed { pIdx, rawParagraph ->
            if (rawParagraph.isBlank()) return@forEachIndexed
            val isLastParagraph = (pIdx == rawParagraphs.lastIndex)

            val rawSentences = rawParagraph.split(Regex("(?<=[.!?])\\s+"))

            rawSentences.forEachIndexed { sIdx, rawSentence ->
                val trimmed = rawSentence.trim()
                if (trimmed.isBlank()) return@forEachIndexed

                val cleanText = TtsTextSanitizer.sanitize(trimmed)
                if (cleanText.isBlank()) return@forEachIndexed

                val isLastSentenceInParagraph = (sIdx == rawSentences.lastIndex)
                val isQuestion = trimmed.endsWith("?")
                val isExclamation = trimmed.endsWith("!")

                val wordCount = cleanText.split(Regex("\\s+")).filter { it.isNotBlank() }.size

                // Human inflection rules
                val pitch = when {
                    isQuestion -> 1.08f
                    isExclamation -> 1.06f
                    else -> 1.00f
                }

                val rate = when {
                    isShortSentence(wordCount) -> 0.93f
                    isLongSentence(wordCount) -> 1.03f
                    else -> 1.00f
                }

                val pauseMillis = when {
                    isLastSentenceInParagraph && !isLastParagraph -> 700L
                    isQuestion -> 450L
                    isExclamation -> 400L
                    else -> 350L
                }

                result.add(
                    SentenceCadence(
                        text = cleanText,
                        pitch = pitch,
                        speechRate = rate,
                        pauseAfterMillis = pauseMillis
                    )
                )
            }
        }

        return result
    }

    private fun isShortSentence(count: Int): Boolean = count in 1..5
    private fun isLongSentence(count: Int): Boolean = count > 18
}
