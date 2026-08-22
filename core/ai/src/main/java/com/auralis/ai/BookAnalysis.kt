package com.auralis.ai

data class BookAnalysisInput(
    val title: String,
    val chapterTitles: List<String>,
    val textSample: String
)

data class BookAnalysisResult(
    val language: String,
    val genre: String,
    val tone: String,
    val synopsis: String,
    val source: String,
    val confidence: Float,
    val characters: List<CharacterCandidate>,
    val pronunciationHints: List<PronunciationCandidate>
)

data class CharacterCandidate(
    val name: String,
    val aliases: List<String>,
    val description: String,
    val pronunciation: String?,
    val confidence: Float
)

data class PronunciationCandidate(
    val phrase: String,
    val hint: String,
    val source: String
)

interface LocalBookAnalyzer {
    suspend fun analyze(input: BookAnalysisInput): BookAnalysisResult
}
