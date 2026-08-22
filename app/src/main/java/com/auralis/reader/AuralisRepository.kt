package com.auralis.reader

import android.content.Context
import android.net.Uri
import com.auralis.ai.BookAnalysisInput
import com.auralis.ai.HeuristicBookAnalyzer
import com.auralis.audio.VoiceModelRepository
import com.auralis.database.AudiobookJobEntity
import com.auralis.database.AuralisDatabase
import com.auralis.database.BookEntity
import com.auralis.database.BookMetadataEntity
import com.auralis.database.BookmarkEntity
import com.auralis.database.ChapterEntity
import com.auralis.database.CharacterProfileEntity
import com.auralis.database.HighlightEntity
import com.auralis.database.PronunciationHintEntity
import com.auralis.database.ReadingPositionEntity
import com.auralis.jobs.AudiobookJobScheduler
import com.auralis.reader.core.BookImporter
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AuralisRepository(private val context: Context) {
    private val dao = AuralisDatabase.get(context).dao()
    private val importer = BookImporter(context)
    private val analyzer = HeuristicBookAnalyzer()
    private val voiceRepository = VoiceModelRepository(context, dao)
    private val audiobookJobScheduler = AudiobookJobScheduler(context)

    val books: Flow<List<BookEntity>> = dao.observeBooks()
    val voices = dao.observeVoiceModels()

    fun observeBook(bookId: String) = dao.observeBook(bookId)
    fun observeChapters(bookId: String) = dao.observeChapters(bookId)
    fun observeMetadata(bookId: String) = dao.observeMetadata(bookId)
    fun observeCharacters(bookId: String) = dao.observeCharacters(bookId)
    fun observeJob(bookId: String) = dao.observeLatestAudiobookJob(bookId)
    fun observeBookmarks(bookId: String) = dao.observeBookmarks(bookId)
    fun observeHighlights(bookId: String) = dao.observeHighlights(bookId)

    suspend fun seedVoiceCatalog() = voiceRepository.seedCatalog()

    suspend fun importBook(uri: Uri): String = withContext(Dispatchers.IO) {
        val imported = importer.import(uri)
        val now = System.currentTimeMillis()
        val chapters = imported.chapters.map {
            ChapterEntity(
                id = it.id,
                bookId = imported.id,
                title = it.title,
                sortIndex = it.sortIndex,
                textPath = it.textPath,
                characterCount = it.characterCount,
                pageStart = it.pageStart,
                pageEnd = it.pageEnd
            )
        }
        val sample = imported.chapters
            .take(3)
            .joinToString("\n\n") { File(it.textPath).readText().take(12_000) }
        val analysis = analyzer.analyze(
            BookAnalysisInput(
                title = imported.title,
                chapterTitles = imported.chapters.map { it.title },
                textSample = sample
            )
        )

        dao.insertImportedBook(
            book = BookEntity(
                id = imported.id,
                title = imported.title,
                author = imported.author,
                format = imported.format.name,
                sourceUri = imported.sourceUri,
                localPath = imported.localPath,
                importStatus = imported.importStatus.name,
                createdAtMillis = now,
                updatedAtMillis = now
            ),
            chapters = chapters,
            metadata = BookMetadataEntity(
                bookId = imported.id,
                language = analysis.language,
                genre = analysis.genre,
                tone = analysis.tone,
                synopsis = analysis.synopsis,
                source = analysis.source,
                confidence = analysis.confidence,
                updatedAtMillis = now
            ),
            characters = analysis.characters.map {
                CharacterProfileEntity(
                    id = stableId(imported.id, it.name),
                    bookId = imported.id,
                    name = it.name,
                    aliases = it.aliases.joinToString("|"),
                    description = it.description,
                    pronunciation = it.pronunciation,
                    confidence = it.confidence
                )
            },
            hints = analysis.pronunciationHints.map {
                PronunciationHintEntity(
                    id = stableId(imported.id, it.phrase),
                    bookId = imported.id,
                    phrase = it.phrase,
                    hint = it.hint,
                    source = it.source
                )
            },
            job = AudiobookJobEntity(
                id = "job-${imported.id}",
                bookId = imported.id,
                voiceModelId = null,
                status = "not_started",
                currentChapterId = null,
                completedSegments = 0,
                totalSegments = chapters.size,
                lastError = null,
                updatedAtMillis = now
            )
        )
        imported.id
    }

    suspend fun saveReadingPosition(bookId: String, chapterId: String?) {
        dao.upsertReadingPosition(
            ReadingPositionEntity(
                bookId = bookId,
                chapterId = chapterId,
                textOffset = 0,
                pageIndex = null,
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun addBookmark(bookId: String, chapterId: String, label: String) {
        dao.insertBookmark(
            BookmarkEntity(
                bookId = bookId,
                chapterId = chapterId,
                textOffset = 0,
                label = label,
                createdAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun addHighlight(bookId: String, chapterId: String, note: String) {
        dao.insertHighlight(
            HighlightEntity(
                bookId = bookId,
                chapterId = chapterId,
                startOffset = 0,
                endOffset = 160,
                note = note,
                colorName = "sage",
                createdAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun installVoice(uri: Uri) = withContext(Dispatchers.IO) {
        voiceRepository.installOnnxVoice(uri)
    }

    suspend fun downloadDefaultVoice() = withContext(Dispatchers.IO) {
        voiceRepository.downloadDefaultKokoroVoice()
    }

    fun prepareAudiobook(bookId: String) {
        audiobookJobScheduler.enqueue(bookId)
    }

    fun readChapterText(chapter: ChapterEntity): String {
        return File(chapter.textPath).takeIf { it.exists() }?.readText().orEmpty()
    }

    private fun stableId(bookId: String, value: String): String {
        return UUID.nameUUIDFromBytes("$bookId:$value".toByteArray()).toString()
    }
}
