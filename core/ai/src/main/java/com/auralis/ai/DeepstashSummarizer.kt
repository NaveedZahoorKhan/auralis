package com.auralis.ai

import android.util.Log
import java.io.File
import java.util.Locale

enum class InsightType(val label: String) {
    KEY_IDEA("Key Idea"),
    ACTIONABLE_TAKEAWAY("Actionable Insight"),
    CORE_CONCEPT("Core Concept"),
    QUOTE("Memorable Quote")
}

data class DeepstashInsightCard(
    val id: String,
    val title: String,
    val type: InsightType,
    val content: String,
    val readTimeSeconds: Int = 30,
    val chapterTitle: String? = null,
    val confidence: Float = 0.88f
)

data class DeepstashSummaryResult(
    val bookTitle: String,
    val author: String,
    val executiveSummary: String,
    val keyTakeawaysCount: Int,
    val cards: List<DeepstashInsightCard>,
    val slmModelUsed: String = "Qwen 2.5 0.5B (Local SLM)",
    val scannedChaptersCount: Int = 0,
    val isOnnxActive: Boolean = false
)

class DeepstashSummarizer(
    private val llmRuntime: OnDeviceLlmRuntime = OnDeviceLlmRuntime()
) {

    fun generateSummary(
        bookTitle: String,
        author: String,
        chapters: List<Pair<String, String>>,
        slmModelFile: File? = null,
        slmModelName: String = "Qwen 2.5 0.5B (Local SLM)",
        bookDescription: String? = null
    ): DeepstashSummaryResult {
        Log.d("AuralisSLM", "DeepstashSummarizer: generateSummary requested for '$bookTitle' (Chapters: ${chapters.size}, slmModelFile: ${slmModelFile?.name})")
        
        // Try executing local ONNX SLM model if provided and valid
        if (slmModelFile != null && llmRuntime.validateModel(slmModelFile)) {
            Log.i("AuralisSLM", "DeepstashSummarizer: ONNX SLM model file validated (${slmModelFile.name}, size: ${slmModelFile.length()} bytes). Dispatching to ONNX Runtime engine.")
            val slmResult = llmRuntime.generateDeepstashWithOnnx(
                bookTitle = bookTitle,
                author = author,
                chapters = chapters,
                modelFile = slmModelFile,
                bookDescription = bookDescription
            )
            if (slmResult != null && slmResult.cards.isNotEmpty()) {
                Log.i("AuralisSLM", "DeepstashSummarizer: Successfully generated ${slmResult.cards.size} ONNX SLM insight cards.")
                return slmResult
            }
        }

        Log.w("AuralisSLM", "DeepstashSummarizer: ONNX SLM model inactive or produced empty result. Executing advanced Distillation Engine.")

        // Advanced Distillation Engine scanning whole book
        val extractedCards = extractHighQualityCards(bookTitle, author, chapters, bookDescription)
        
        val executiveSummaryText = generateExecutiveSummary(bookTitle, author, chapters, extractedCards, bookDescription)

        return DeepstashSummaryResult(
            bookTitle = bookTitle,
            author = author,
            executiveSummary = executiveSummaryText,
            keyTakeawaysCount = extractedCards.size,
            cards = extractedCards,
            slmModelUsed = slmModelName,
            scannedChaptersCount = chapters.size,
            isOnnxActive = false
        )
    }

    companion object {
        fun extractHighQualityCards(
            bookTitle: String,
            author: String,
            chapters: List<Pair<String, String>>,
            bookDescription: String? = null
        ): List<DeepstashInsightCard> {
            val cards = mutableListOf<DeepstashInsightCard>()
            var cardIndex = 0

            // Add metadata overview card if available
            if (!bookDescription.isNullOrBlank()) {
                cards.add(
                    DeepstashInsightCard(
                        id = "card_metadata_overview",
                        title = "Book Overview",
                        type = InsightType.KEY_IDEA,
                        content = bookDescription,
                        readTimeSeconds = calculateReadTime(bookDescription),
                        chapterTitle = "Metadata",
                        confidence = 0.99f
                    )
                )
            }

            // 0. Extract Table of Contents / Chapter Structure Card
            val tocCard = extractTocCardIfAvailable(bookTitle, chapters)
            if (tocCard != null) {
                cards.add(tocCard)
            }

            chapters.forEachIndexed { chapIdx, (chapTitle, chapText) ->
                val cleanSentences = prepareAndFilterSentences(chapText)
                if (cleanSentences.isEmpty()) return@forEachIndexed

                // 1. Extract Key Idea (Highest salient thesis sentence)
                val keyIdeaSentence = cleanSentences.maxByOrNull { scoreSentence(it, InsightType.KEY_IDEA) }
                keyIdeaSentence?.let { sentence ->
                    val title = generateContextualTitle(sentence, "Core Principle")
                    cards.add(
                        DeepstashInsightCard(
                            id = "card_${chapIdx}_${cardIndex++}_idea",
                            title = title,
                            type = InsightType.KEY_IDEA,
                            content = sentence,
                            readTimeSeconds = calculateReadTime(sentence),
                            chapterTitle = chapTitle,
                            confidence = 0.94f
                        )
                    )
                }

                // 2. Extract Actionable Insight
                val actionSentence = cleanSentences.filter { it != keyIdeaSentence }
                    .maxByOrNull { scoreSentence(it, InsightType.ACTIONABLE_TAKEAWAY) }
                actionSentence?.let { sentence ->
                    if (scoreSentence(sentence, InsightType.ACTIONABLE_TAKEAWAY) > 2.0f) {
                        val title = generateContextualTitle(sentence, "Actionable Takeaway")
                        cards.add(
                            DeepstashInsightCard(
                                id = "card_${chapIdx}_${cardIndex++}_takeaway",
                                title = title,
                                type = InsightType.ACTIONABLE_TAKEAWAY,
                                content = sentence,
                                readTimeSeconds = calculateReadTime(sentence),
                                chapterTitle = chapTitle,
                                confidence = 0.91f
                            )
                        )
                    }
                }

                // 3. Extract Core Concept
                val conceptSentence = cleanSentences.filter { it != keyIdeaSentence && it != actionSentence }
                    .maxByOrNull { scoreSentence(it, InsightType.CORE_CONCEPT) }
                conceptSentence?.let { sentence ->
                    if (scoreSentence(sentence, InsightType.CORE_CONCEPT) > 2.0f) {
                        val title = generateContextualTitle(sentence, "Core Framework")
                        cards.add(
                            DeepstashInsightCard(
                                id = "card_${chapIdx}_${cardIndex++}_concept",
                                title = title,
                                type = InsightType.CORE_CONCEPT,
                                content = sentence,
                                readTimeSeconds = calculateReadTime(sentence),
                                chapterTitle = chapTitle,
                                confidence = 0.89f
                            )
                        )
                    }
                }

                // 4. Extract Memorable Quote
                val quoteSentence = cleanSentences.filter { it != keyIdeaSentence && it != actionSentence && it != conceptSentence }
                    .filter { it.length in 35..130 }
                    .maxByOrNull { scoreSentence(it, InsightType.QUOTE) }
                quoteSentence?.let { sentence ->
                    val cleanQuote = sentence.trim().removeSurrounding("\"")
                    val title = generateContextualTitle(cleanQuote, "Key Quote")
                    cards.add(
                        DeepstashInsightCard(
                            id = "card_${chapIdx}_${cardIndex++}_quote",
                            title = title,
                            type = InsightType.QUOTE,
                            content = "\"$cleanQuote\"",
                            readTimeSeconds = calculateReadTime(cleanQuote),
                            chapterTitle = chapTitle,
                            confidence = 0.96f
                        )
                    )
                }
            }

            return if (cards.isNotEmpty()) {
                cards
            } else {
                listOf(
                    DeepstashInsightCard(
                        id = "card_fallback_1",
                        title = "Core Synthesis",
                        type = InsightType.KEY_IDEA,
                        content = "Explore the main themes of '$bookTitle' by $author through structured SLM chapter insights.",
                        readTimeSeconds = 20,
                        confidence = 0.90f
                    )
                )
            }
        }

        private fun extractTocCardIfAvailable(
            bookTitle: String,
            chapters: List<Pair<String, String>>
        ): DeepstashInsightCard? {
            // 1. Look for explicit TOC chapter
            val tocChapter = chapters.find { (title, text) ->
                val lowerTitle = title.lowercase(Locale.ROOT)
                val lowerText = text.take(1000).lowercase(Locale.ROOT)
                lowerTitle.contains("table of contents") || lowerTitle.contains("contents") || lowerTitle.contains("toc") ||
                        lowerText.contains("table of contents") || lowerText.contains("contents")
            }

            if (tocChapter != null) {
                val (title, text) = tocChapter
                val rawLines = text.lines()
                    .map { it.trim() }
                    .filter { line ->
                        line.length in 5..120 &&
                                (line.matches(Regex("(?i).*(chapter|part|section|[0-9]{1,2}\\.|[IVXLCDM]+\\.).*")) ||
                                 line.contains("...") || line.matches(Regex(".*\\s+\\d+$")))
                    }
                    .take(10)

                val tocLines = if (rawLines.isNotEmpty()) rawLines else chapters.mapIndexed { i, (t, _) -> "${i + 1}. $t" }.take(10)
                val tocContent = tocLines.joinToString("\n• ")

                return DeepstashInsightCard(
                    id = "card_toc_0",
                    title = "Table of Contents & Structure",
                    type = InsightType.CORE_CONCEPT,
                    content = "Parsed Outline for '$bookTitle':\n• $tocContent",
                    readTimeSeconds = 25,
                    chapterTitle = title.ifBlank { "Table of Contents" },
                    confidence = 0.98f
                )
            }

            // 2. Synthesize from extracted chapters list
            if (chapters.size >= 2) {
                val chapterOutline = chapters.mapIndexed { idx, (chapTitle, _) ->
                    val cleanTitle = chapTitle.ifBlank { "Chapter ${idx + 1}" }
                    "${idx + 1}. $cleanTitle"
                }.take(10).joinToString("\n• ")

                return DeepstashInsightCard(
                    id = "card_toc_0",
                    title = "Book Chapter Structure",
                    type = InsightType.CORE_CONCEPT,
                    content = "Parsed Navigation Outline for '$bookTitle':\n• $chapterOutline",
                    readTimeSeconds = 25,
                    chapterTitle = "Chapter Index",
                    confidence = 0.95f
                )
            }

            return null
        }

        fun generateExecutiveSummary(
            bookTitle: String,
            author: String,
            chapters: List<Pair<String, String>>,
            cards: List<DeepstashInsightCard>,
            bookDescription: String? = null
        ): String {
            if (!bookDescription.isNullOrBlank()) {
                val briefDescription = if (bookDescription.length > 200) {
                    bookDescription.substring(0, 197) + "..."
                } else {
                    bookDescription
                }
                return "\"$bookTitle\" by $author: $briefDescription (Analyzed ${chapters.size} chapters into ${cards.size} visual takeaways)."
            }

            val keyIdeas = cards.filter { it.type == InsightType.KEY_IDEA }.take(2).map { it.content }
            val takeaways = cards.filter { it.type == InsightType.ACTIONABLE_TAKEAWAY }.take(1).map { it.content }

            val mainTakeaway = when {
                keyIdeas.isNotEmpty() -> keyIdeas.first()
                cards.isNotEmpty() -> cards.first().content
                else -> "A foundational work exploring core principles, mental models, and practical wisdom."
            }

            val secondaryTheme = if (takeaways.isNotEmpty()) {
                " Key focus: ${takeaways.first()}"
            } else if (keyIdeas.size > 1) {
                " Core framework: ${keyIdeas[1]}"
            } else ""

            return "\"$bookTitle\" by $author distillment: $mainTakeaway$secondaryTheme (Analyzed ${chapters.size} chapters into ${cards.size} visual takeaways)."
        }

        private fun prepareAndFilterSentences(rawText: String): List<String> {
            val cleanText = rawText
                .replace(Regex("\\r\\n|\\r"), "\n")
                .replace(Regex("[ \\t]+"), " ")

            val rawSentences = cleanText.split(Regex("(?<=[.!?])\\s+|\n+"))

            return rawSentences.map { it.trim() }.filter { sentence ->
                // Boilerplate & Garbage filtering
                val sLower = sentence.lowercase(Locale.ROOT)
                val isBoilerplate = sLower.startsWith("chapter") ||
                        sLower.startsWith("part ") ||
                        sLower.startsWith("table of contents") ||
                        sLower.contains("copyright") ||
                        sLower.contains("all rights reserved") ||
                        sLower.contains("isbn") ||
                        sLower.contains("published by") ||
                        sLower.startsWith("page ") ||
                        sLower.matches(Regex("^[0-9\\s\\-._]+$"))

                val isValidLength = sentence.length in 30..350
                val hasAlpha = sentence.any { it.isLetter() }

                !isBoilerplate && isValidLength && hasAlpha
            }
        }

        private fun scoreSentence(sentence: String, type: InsightType): Float {
            val sLower = sentence.lowercase(Locale.ROOT)
            var score = 1.0f

            when (type) {
                InsightType.KEY_IDEA -> {
                    if (sLower.contains("important") || sLower.contains("fundamental") || sLower.contains("essence") || sLower.contains("key to")) score += 2.5f
                    if (sLower.contains("truth") || sLower.contains("principle") || sLower.contains("because") || sLower.contains("means")) score += 2.0f
                    if (sLower.contains("most") || sLower.contains("always") || sLower.contains("never")) score += 1.5f
                }
                InsightType.ACTIONABLE_TAKEAWAY -> {
                    if (sLower.contains("must") || sLower.contains("should") || sLower.contains("action") || sLower.contains("strategy")) score += 3.0f
                    if (sLower.contains("instead of") || sLower.contains("focus on") || sLower.contains("rule") || sLower.contains("habit")) score += 2.5f
                    if (sLower.contains("start") || sLower.contains("stop") || sLower.contains("how to") || sLower.contains("learn")) score += 2.0f
                }
                InsightType.CORE_CONCEPT -> {
                    if (sLower.contains("framework") || sLower.contains("concept") || sLower.contains("system") || sLower.contains("model")) score += 3.0f
                    if (sLower.contains("defined as") || sLower.contains("structure") || sLower.contains("process") || sLower.contains("method")) score += 2.5f
                }
                InsightType.QUOTE -> {
                    if (sentence.contains("\"") || sentence.contains("'")) score += 2.5f
                    if (sLower.contains("said") || sLower.contains("wrote") || sLower.contains("remember")) score += 2.0f
                    if (sentence.length in 45..110) score += 1.5f
                }
            }

            return score
        }

        private fun generateContextualTitle(sentence: String, defaultTitle: String): String {
            val clean = sentence.replace(Regex("[^a-zA-Z0-9 ]"), "").trim()
            val words = clean.split(Regex("\\s+")).filter { it.isNotBlank() }

            if (words.size < 3) return defaultTitle

            // Try picking an intriguing 3-5 word phrase
            val candidate = words.take(5).joinToString(" ") { word ->
                word.lowercase(Locale.ROOT).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            }

            return if (candidate.length in 8..35) candidate else defaultTitle
        }

        private fun calculateReadTime(text: String): Int {
            val wordCount = text.split(Regex("\\s+")).size
            val seconds = ((wordCount / 200.0) * 60).toInt().coerceAtLeast(15)
            return seconds
        }
    }
}
