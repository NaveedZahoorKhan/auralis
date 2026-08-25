package com.auralis.ai

import java.util.Locale

class HeuristicBookAnalyzer : LocalBookAnalyzer {
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

    companion object {
        private val genreRegexes = mapOf(
            "mystery" to Regex("\\b(?:detective|murder|clue|investigation|case)\\b"),
            "fantasy" to Regex("\\b(?:kingdom|sword|magic|dragon|wizard)\\b"),
            "science fiction" to Regex("\\b(?:planet|spaceship|android|colony|galaxy)\\b"),
            "romance" to Regex("\\b(?:heart|kiss|beloved|marriage|desire)\\b"),
            "history" to Regex("\\b(?:empire|war|century|king|revolution)\\b")
        )
        private val darkRegex = Regex("\\b(?:shadow|fear|death|cold|blood|alone)\\b")
        private val warmRegex = Regex("\\b(?:warm|smile|home|friend|laugh|hope)\\b")
        private val urgentRegex = Regex("\\b(?:ran|suddenly|shouted|hurry|escape|danger)\\b")

        private val characterNameRegex = Regex("\\b[A-Z][a-z]{2,}(?:\\s+[A-Z][a-z]{2,})?\\b")
    }

    private fun detectGenre(lower: String): String {
        val scores = genreRegexes.mapValues { (_, regex) -> regex.findAll(lower).count() }
        return scores.maxByOrNull { it.value }?.takeIf { it.value > 1 }?.key ?: "literary"
    }

    private fun detectTone(lower: String): String {
        val dark = darkRegex.findAll(lower).count()
        val warm = warmRegex.findAll(lower).count()
        val urgent = urgentRegex.findAll(lower).count()
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
        val counts = characterNameRegex
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
