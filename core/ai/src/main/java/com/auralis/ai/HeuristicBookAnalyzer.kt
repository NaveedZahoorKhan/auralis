package com.auralis.ai

import java.util.Locale

class HeuristicBookAnalyzer : LocalBookAnalyzer {

    companion object {
        private val GENRE_TERMS = mapOf(
            "mystery" to listOf("detective", "murder", "clue", "investigation", "case"),
            "fantasy" to listOf("kingdom", "sword", "magic", "dragon", "wizard"),
            "science fiction" to listOf("planet", "spaceship", "android", "colony", "galaxy"),
            "romance" to listOf("heart", "kiss", "beloved", "marriage", "desire"),
            "history" to listOf("empire", "war", "century", "king", "revolution")
        ).mapValues { (_, terms) -> terms.map { Regex("\\b$it\\b") } }

        private val DARK_TERMS = listOf("shadow", "fear", "death", "cold", "blood", "alone").map { Regex("\\b$it\\b") }
        private val WARM_TERMS = listOf("warm", "smile", "home", "friend", "laugh", "hope").map { Regex("\\b$it\\b") }
        private val URGENT_TERMS = listOf("ran", "suddenly", "shouted", "hurry", "escape", "danger").map { Regex("\\b$it\\b") }

        private val CHARACTER_REGEX = Regex("\\b[A-Z][a-z]{2,}(?:\\s+[A-Z][a-z]{2,})?\\b")
    }

    override suspend fun analyze(input: BookAnalysisInput): BookAnalysisResult {
        val sample = input.textSample.take(80_000)
        val lower = sample.lowercase(Locale.US)
        val genre = detectGenre(lower)
        val tone = detectTone(lower)
        val characters = detectCharacterNames(sample)
        val synopsis = buildSynopsis(input.title, input.chapterTitles, genre, tone)

        return BookAnalysisResult(
            language = "en",
            genre = genre,
            tone = tone,
            synopsis = synopsis,
            source = "local-heuristic",
            confidence = 0.42f,
            characters = characters,
            pronunciationHints = characters.take(8).map {
                PronunciationCandidate(
                    phrase = it.name,
                    hint = "Preserve the name as written unless the voice model supplies a stronger pronunciation.",
                    source = "local-heuristic"
                )
            }
        )
    }

    private fun detectGenre(lower: String): String {
        val scores = GENRE_TERMS.mapValues { (_, regexes) -> regexes.sumOf { regex -> regex.findAll(lower).count() } }
        return scores.maxByOrNull { it.value }?.takeIf { it.value > 1 }?.key ?: "literary"
    }

    private fun detectTone(lower: String): String {
        val dark = DARK_TERMS.sumOf { it.findAll(lower).count() }
        val warm = WARM_TERMS.sumOf { it.findAll(lower).count() }
        val urgent = URGENT_TERMS.sumOf { it.findAll(lower).count() }
        return when {
            urgent >= dark && urgent >= warm && urgent > 2 -> "urgent"
            dark > warm && dark > 2 -> "tense"
            warm > dark && warm > 2 -> "warm"
            else -> "measured"
        }
    }

    private fun detectCharacterNames(sample: String): List<CharacterCandidate> {
        val commonWords = setOf(
            "The", "A", "An", "Chapter", "Book", "Part", "When", "Then", "There", "This",
            "That", "He", "She", "They", "It", "I", "We", "You", "But", "And", "For"
        )
        val counts = CHARACTER_REGEX
            .findAll(sample)
            .map { it.value.trim() }
            .filterNot { it in commonWords }
            .groupingBy { it }
            .eachCount()

        return counts.entries
            .filter { it.value >= 2 }
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .take(12)
            .map { (name, count) ->
                CharacterCandidate(
                    name = name,
                    aliases = emptyList(),
                    description = "Mentioned $count times in the extracted sample.",
                    pronunciation = null,
                    confidence = (0.35f + count.coerceAtMost(12) / 24f).coerceAtMost(0.85f)
                )
            }
    }

    private fun buildSynopsis(
        title: String,
        chapterTitles: List<String>,
        genre: String,
        tone: String
    ): String {
        val visibleChapters = chapterTitles.take(4).joinToString(", ")
        return if (visibleChapters.isBlank()) {
            "$title is indexed as a $tone $genre work. Detailed summaries will improve after the local LLM model is installed."
        } else {
            "$title is indexed as a $tone $genre work. Early sections include $visibleChapters."
        }
    }
}
