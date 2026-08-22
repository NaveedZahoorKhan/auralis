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

    private fun detectGenre(lower: String): String {
        val scores = mapOf(
            "mystery" to listOf("detective", "murder", "clue", "investigation", "case"),
            "fantasy" to listOf("kingdom", "sword", "magic", "dragon", "wizard"),
            "science fiction" to listOf("planet", "spaceship", "android", "colony", "galaxy"),
            "romance" to listOf("heart", "kiss", "beloved", "marriage", "desire"),
            "history" to listOf("empire", "war", "century", "king", "revolution")
        ).mapValues { (_, terms) -> terms.sumOf { term -> Regex("\\b$term\\b").findAll(lower).count() } }
        return scores.maxByOrNull { it.value }?.takeIf { it.value > 1 }?.key ?: "literary"
    }

    private fun detectTone(lower: String): String {
        val dark = listOf("shadow", "fear", "death", "cold", "blood", "alone").sumOf { Regex("\\b$it\\b").findAll(lower).count() }
        val warm = listOf("warm", "smile", "home", "friend", "laugh", "hope").sumOf { Regex("\\b$it\\b").findAll(lower).count() }
        val urgent = listOf("ran", "suddenly", "shouted", "hurry", "escape", "danger").sumOf { Regex("\\b$it\\b").findAll(lower).count() }
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
        val counts = Regex("\\b[A-Z][a-z]{2,}(?:\\s+[A-Z][a-z]{2,})?\\b")
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
