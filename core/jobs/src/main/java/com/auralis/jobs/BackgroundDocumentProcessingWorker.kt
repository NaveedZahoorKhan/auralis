package com.auralis.jobs

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.auralis.ai.BookAnalysisInput
import com.auralis.ai.HeuristicBookAnalyzer
import com.auralis.ai.LocalBookAnalyzer
import com.auralis.database.AuralisDatabase
import com.auralis.database.BookMetadataEntity
import com.auralis.database.CharacterProfileEntity
import com.auralis.database.PronunciationHintEntity
import com.auralis.reader.core.BookImporter
import com.auralis.reader.core.ImportStatus
import com.auralis.reader.core.PdfBoxTextExtractionService
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Background WorkManager worker responsible for:
 * 1. Performing background document extraction (PDF/EPUB) for queued or newly added books.
 * 2. Running AI document intelligence analysis (characters, tone, synopsis, pronunciation hints).
 * 3. Safely updating Room database entities with extracted chapters, metadata, and character models.
 */
class BackgroundDocumentProcessingWorker(
    appContext: Context,
    params: WorkerParameters,
    private val bookAnalyzer: LocalBookAnalyzer = HeuristicBookAnalyzer()
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val targetBookId = inputData.getString(KEY_BOOK_ID)
        val fileUriString = inputData.getString(KEY_SOURCE_URI)
        val shouldAnalyzeAi = inputData.getBoolean(KEY_RUN_AI_ANALYSIS, true)

        val database = AuralisDatabase.get(applicationContext)
        val dao = database.dao()

        try {
            if (targetBookId != null) {
                processSingleBook(
                    bookId = targetBookId,
                    sourceUriString = fileUriString,
                    shouldAnalyzeAi = shouldAnalyzeAi,
                    dao = dao
                )
            } else {
                // Process all queued or pending books in the database
                val pendingBooks = dao.getBooksByStatus("pending") + dao.getBooksByStatus("importing")
                if (pendingBooks.isEmpty()) {
                    return@withContext Result.success(workDataOf(KEY_PROCESSED_COUNT to 0))
                }

                var processedCount = 0
                for (book in pendingBooks) {
                    processSingleBook(
                        bookId = book.id,
                        sourceUriString = book.sourceUri,
                        shouldAnalyzeAi = shouldAnalyzeAi,
                        dao = dao
                    )
                    processedCount++
                }

                return@withContext Result.success(workDataOf(KEY_PROCESSED_COUNT to processedCount))
            }

            Result.success(workDataOf(KEY_PROCESSED_COUNT to 1))
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (e: Exception) {
            targetBookId?.let { id ->
                val existingBook = dao.getBook(id)
                if (existingBook != null) {
                    dao.upsertBook(
                        existingBook.copy(
                            importStatus = "failed",
                            updatedAtMillis = System.currentTimeMillis()
                        )
                    )
                }
            }
            Result.retry()
        }
    }

    private suspend fun processSingleBook(
        bookId: String,
        sourceUriString: String?,
        shouldAnalyzeAi: Boolean,
        dao: com.auralis.database.AuralisDao
    ) {
        val existingBook = dao.getBook(bookId)
        val existingChapters = dao.getChapters(bookId)

        // If chapters are already extracted, enrich with AI intelligence if needed
        if (existingChapters.isNotEmpty()) {
            if (shouldAnalyzeAi) {
                runAiEnrichment(bookId, existingBook?.title ?: "Document", existingChapters, dao)
            }
            if (existingBook != null && existingBook.importStatus != "ready") {
                dao.upsertBook(
                    existingBook.copy(
                        importStatus = "ready",
                        updatedAtMillis = System.currentTimeMillis()
                    )
                )
            }
            return
        }

        // If chapters are missing, perform import from file or URI
        val uri = if (!sourceUriString.isNullOrBlank()) {
            Uri.parse(sourceUriString)
        } else if (existingBook != null && existingBook.localPath.isNotBlank() && File(existingBook.localPath).exists()) {
            Uri.fromFile(File(existingBook.localPath))
        } else {
            null
        }

        if (uri == null) {
            existingBook?.let {
                dao.upsertBook(it.copy(importStatus = "missing_source", updatedAtMillis = System.currentTimeMillis()))
            }
            return
        }

        val pdfService = PdfBoxTextExtractionService(applicationContext)
        val importer = BookImporter(applicationContext, pdfService)
        val imported = importer.import(uri)

        if (imported.importStatus == ImportStatus.Ready && imported.chapters.isNotEmpty()) {
            val chapters = imported.chapters.map { chapter ->
                com.auralis.database.ChapterEntity(
                    id = chapter.id,
                    bookId = bookId,
                    title = chapter.title,
                    sortIndex = chapter.sortIndex,
                    textPath = chapter.textPath,
                    characterCount = chapter.characterCount,
                    pageStart = chapter.pageStart,
                    pageEnd = chapter.pageEnd
                )
            }

            dao.insertChapters(chapters)

            if (shouldAnalyzeAi) {
                runAiEnrichment(bookId, imported.title, chapters, dao)
            }

            if (existingBook != null) {
                dao.upsertBook(
                    existingBook.copy(
                        title = imported.title.ifBlank { existingBook.title },
                        author = imported.author ?: existingBook.author,
                        format = imported.format.name,
                        localPath = imported.localPath,
                        importStatus = "ready",
                        updatedAtMillis = System.currentTimeMillis()
                    )
                )
            }
        } else {
            val statusString = when (imported.importStatus) {
                ImportStatus.NeedsOcr -> "needs_ocr"
                ImportStatus.Unsupported -> "unsupported"
                else -> "failed"
            }
            existingBook?.let {
                dao.upsertBook(it.copy(importStatus = statusString, updatedAtMillis = System.currentTimeMillis()))
            }
        }
    }

    private suspend fun runAiEnrichment(
        bookId: String,
        bookTitle: String,
        chapters: List<com.auralis.database.ChapterEntity>,
        dao: com.auralis.database.AuralisDao
    ) {
        val chapterTitles = chapters.map { it.title }
        val sampleText = chapters.take(3).joinToString("\n\n") { chapter ->
            val file = File(chapter.textPath)
            if (file.exists()) file.readText().take(15_000) else ""
        }

        val analysis = bookAnalyzer.analyze(
            BookAnalysisInput(
                title = bookTitle,
                chapterTitles = chapterTitles,
                textSample = sampleText
            )
        )

        dao.upsertMetadata(
            BookMetadataEntity(
                bookId = bookId,
                language = analysis.language,
                genre = analysis.genre,
                tone = analysis.tone,
                synopsis = analysis.synopsis,
                source = analysis.source,
                confidence = analysis.confidence,
                updatedAtMillis = System.currentTimeMillis()
            )
        )

        if (analysis.characters.isNotEmpty()) {
            val characterEntities = analysis.characters.map { candidate ->
                val id = java.util.UUID.nameUUIDFromBytes("$bookId:${candidate.name}".toByteArray()).toString()
                CharacterProfileEntity(
                    id = id,
                    bookId = bookId,
                    name = candidate.name,
                    aliases = candidate.aliases.joinToString(","),
                    description = candidate.description,
                    pronunciation = candidate.pronunciation,
                    confidence = candidate.confidence
                )
            }
            dao.insertCharacters(characterEntities)
        }

        if (analysis.pronunciationHints.isNotEmpty()) {
            val hintEntities = analysis.pronunciationHints.map { hint ->
                val id = java.util.UUID.nameUUIDFromBytes("$bookId:${hint.phrase}".toByteArray()).toString()
                PronunciationHintEntity(
                    id = id,
                    bookId = bookId,
                    phrase = hint.phrase,
                    hint = hint.hint,
                    source = hint.source
                )
            }
            dao.insertPronunciationHints(hintEntities)
        }
    }

    companion object {
        const val KEY_BOOK_ID = "book_id"
        const val KEY_SOURCE_URI = "source_uri"
        const val KEY_RUN_AI_ANALYSIS = "run_ai_analysis"
        const val KEY_PROCESSED_COUNT = "processed_count"

        const val WORK_NAME_PREFIX = "doc-processing-"
    }
}
